package it.gov.pagopa.payment.notices.service.storage;

import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobStorageException;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import it.gov.pagopa.payment.notices.service.exception.AppError;
import it.gov.pagopa.payment.notices.service.exception.AppException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoticeStorageClientTest {

    private static BlobClient blobClientMock;
    private static BlobServiceClient blobServiceClient;
    private BlobContainerClient blobContainerClient;
    private NoticeStorageClient noticeStorageClient;
    private Logger storageLogger;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void init() {
        MDC.clear();

        blobContainerClient = mock(BlobContainerClient.class);
        blobServiceClient = mock(BlobServiceClient.class);

        noticeStorageClient = new NoticeStorageClient(true, blobServiceClient, blobContainerClient, "testurl");
        blobClientMock = mock(BlobClient.class);
        lenient().doReturn(blobClientMock).when(blobContainerClient).getBlobClient(anyString());

        storageLogger = (Logger) LoggerFactory.getLogger(NoticeStorageClient.class);
        logAppender = new ListAppender<>();
        logAppender.setContext(storageLogger.getLoggerContext());
        logAppender.start();
        storageLogger.addAppender(logAppender);
    }
    
    @AfterEach
    void cleanupLogging() {
        storageLogger.detachAppender(logAppender);
        logAppender.stop();
        MDC.clear();
    }

    @Test
    void getFileSignedUrlShouldReturnOK() {
        when(blobClientMock.getBlobUrl()).thenReturn("http://localhost:8080");
        when(blobClientMock.generateUserDelegationSas(any(), any())).thenReturn("token");
        String url = noticeStorageClient.getFileSignedUrl("folderId", "fileId");
        assertNotNull(url);
    }

    @Test
    void getFileSignedUrlShouldReturnKO() {
        BlobStorageException cause = new BlobStorageException("Signing failed", null, null);

        when(blobClientMock.generateUserDelegationSas(any(), any())).thenThrow(cause);

        AppException exception = assertThrows(AppException.class,
                () -> noticeStorageClient.getFileSignedUrl("folderId", "fileId"));

        assertSame(cause, exception.getCause());
        assertEquals(AppError.ERROR_ON_GET_FILE_URL_REQUEST.getHttpStatus(), exception.getHttpStatus());
        assertEquals(AppError.ERROR_ON_GET_FILE_URL_REQUEST.getDetails(), exception.getMessage());

        ILoggingEvent event = getSignedUrlFailureLog();
        String message = event.getFormattedMessage();

        assertTrue(message.contains("phase=GENERATE_SAS"));
        assertTrue(message.contains("upstreamHttpStatus=N/A"));
        assertTrue(message.contains("azureErrorCode=N/A"));
        assertNull(event.getThrowableProxy());
    }

    @Test
    void shouldPreserveStorageFailureWhenRetrievingDelegationKey() {
        BlobStorageException cause = new BlobStorageException("Delegation denied", null, null);

        when(blobServiceClient.getUserDelegationKey(any(), any())).thenThrow(cause);

        AppException exception = assertThrows(AppException.class,
                () -> noticeStorageClient.getFileSignedUrl("folderId", "fileId"));

        assertSame(cause, exception.getCause());
        assertEquals(AppError.ERROR_ON_GET_FILE_URL_REQUEST.getHttpStatus(), exception.getHttpStatus());
        assertEquals(AppError.ERROR_ON_GET_FILE_URL_REQUEST.getDetails(), exception.getMessage());

        verify(blobContainerClient, never()).getBlobClient(anyString());
        verifyNoInteractions(blobClientMock);
    }

    @Test
    void shouldPreserveAuthenticationFailureWhenRetrievingDelegationKey() {
        ClientAuthenticationException cause = new ClientAuthenticationException("Authentication failed", null);

        when(blobServiceClient.getUserDelegationKey(any(), any())).thenThrow(cause);

        AppException exception = assertThrows(AppException.class,
                () -> noticeStorageClient.getFileSignedUrl("folderId", "fileId"));

        assertSame(cause, exception.getCause());
        assertEquals(AppError.ERROR_ON_GET_FILE_URL_REQUEST.getHttpStatus(), exception.getHttpStatus());
        assertEquals(AppError.ERROR_ON_GET_FILE_URL_REQUEST.getDetails(), exception.getMessage());

        verify(blobContainerClient, never()).getBlobClient(anyString());
        verifyNoInteractions(blobClientMock);
    }

    @Test
    void shouldReturnExceptionOnMissingClient() {
        var noticeClient = new NoticeStorageClient(false, null, null, "testUrl");
        assertThrows(AppException.class, () -> noticeClient.getFileSignedUrl("testFile", ""));
    }

    @Test
    void deleteShouldReturnKO() {
        when(blobContainerClient.listBlobs(any(), any())).thenAnswer(item -> {
            throw new BlobStorageException("test", null, null);
        });
        assertThrows(AppException.class, () -> noticeStorageClient
                .deleteFolder("folderId"));
    }

    @Test
    void shouldReturnExceptionOnMissingClientForDelete() {
        assertThrows(AppException.class, () ->
                new NoticeStorageClient(false, null, null, "testUrl")
                        .deleteFolder("testFile"));
    }
    
    @Test
    void shouldLogStorageMetadataWithoutSensitiveData() {
        HttpResponse response = mock(HttpResponse.class);

        when(response.getStatusCode()).thenReturn(403);
        when(response.getHeaders())
                .thenReturn(new HttpHeaders().set("x-ms-error-code", "AuthorizationPermissionMismatch"));

        BlobStorageException cause = new BlobStorageException(
                "client_secret=secret-for-log-test " + "https://example.invalid/file?sig=sas-for-log-test", response,
                null);

        when(blobServiceClient.getUserDelegationKey(any(), any())).thenThrow(cause);

        assertThrows(AppException.class, () -> noticeStorageClient.getFileSignedUrl("folderId", "fileId"));

        ILoggingEvent event = getSignedUrlFailureLog();
        String message = event.getFormattedMessage();

        assertTrue(message.contains("phase=GET_USER_DELEGATION_KEY"));
        assertTrue(message.contains("folderId=folderId"));
        assertTrue(message.contains("upstreamHttpStatus=403"));
        assertTrue(message.contains("azureErrorCode=AuthorizationPermissionMismatch"));
        assertTrue(message.contains("exceptionType=" + BlobStorageException.class.getName()));

        assertFalse(message.contains("secret-for-log-test"));
        assertFalse(message.contains("sas-for-log-test"));
        assertFalse(message.contains("https://example.invalid"));
        assertNull(event.getThrowableProxy());
    }
    
    @Test
    void shouldLogNestedAuthenticationMetadataWithoutSensitiveData() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(400);

        ClientAuthenticationException authenticationCause = new ClientAuthenticationException(
                "AADSTS7000222: client_secret=secret-for-log-test", response);

        RuntimeException cause = new RuntimeException("Wrapper containing sig=sas-for-log-test", authenticationCause);

        when(blobServiceClient.getUserDelegationKey(any(), any())).thenThrow(cause);

        AppException exception = assertThrows(AppException.class,
                () -> noticeStorageClient.getFileSignedUrl("folderId", "fileId"));

        assertSame(cause, exception.getCause());

        ILoggingEvent event = getSignedUrlFailureLog();
        String message = event.getFormattedMessage();

        assertTrue(message.contains("phase=GET_USER_DELEGATION_KEY"));
        assertTrue(message.contains("upstreamHttpStatus=400"));
        assertTrue(message.contains("azureErrorCode=AADSTS7000222"));

        assertFalse(message.contains("secret-for-log-test"));
        assertFalse(message.contains("sas-for-log-test"));
        assertNull(event.getThrowableProxy());
    }

    private ILoggingEvent getSignedUrlFailureLog() {
        return logAppender.list.stream()
                .filter(event -> event.getFormattedMessage().startsWith("Signed URL generation failed:")).findFirst()
                .orElseThrow(() -> new AssertionError("Expected signed URL failure log"));
    }

}
