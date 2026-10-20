package it.gov.pagopa.payment.notices.service.storage;

import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import it.gov.pagopa.payment.notices.service.exception.AppError;
import it.gov.pagopa.payment.notices.service.exception.AppException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

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

    @Test
    void shouldUseSameValidityWindowForDelegationKeyAndSas() {
        UserDelegationKey delegationKey = new UserDelegationKey();

        when(blobServiceClient.getUserDelegationKey(any(), any())).thenReturn(delegationKey);

        when(blobClientMock.generateUserDelegationSas(any(), same(delegationKey))).thenReturn("test-token");

        when(blobClientMock.getBlobUrl()).thenReturn("https://storage.example.invalid/notices/folderId/fileId.pdf");

        String url = noticeStorageClient.getFileSignedUrl("folderId", "fileId.pdf");

        assertNotNull(url);

        ArgumentCaptor<OffsetDateTime> keyStartCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);

        ArgumentCaptor<OffsetDateTime> keyExpiryCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);

        verify(blobServiceClient).getUserDelegationKey(keyStartCaptor.capture(), keyExpiryCaptor.capture());

        ArgumentCaptor<BlobServiceSasSignatureValues> sasCaptor = ArgumentCaptor
                .forClass(BlobServiceSasSignatureValues.class);

        verify(blobClientMock).generateUserDelegationSas(sasCaptor.capture(), same(delegationKey));

        OffsetDateTime keyStart = keyStartCaptor.getValue();
        OffsetDateTime keyExpiry = keyExpiryCaptor.getValue();
        BlobServiceSasSignatureValues sasValues = sasCaptor.getValue();

        assertEquals(keyStart, sasValues.getStartTime());
        assertEquals(keyExpiry, sasValues.getExpiryTime());

        assertEquals(keyStart.plusMinutes(10), keyExpiry);

        assertEquals(ZoneOffset.UTC, keyStart.getOffset());
        assertEquals(ZoneOffset.UTC, keyExpiry.getOffset());

        assertEquals(0, keyStart.getNano());
        assertEquals(0, keyExpiry.getNano());

        verify(blobContainerClient).getBlobClient("folderId/fileId.pdf");
    }

    @Test
    void shouldSanitizeExternalValuesInFailureLog() {
        String injectedFolderId = "folderId\r\nFORGED_FOLDER_ENTRY";

        String injectedAzureCode = "AuthorizationPermissionMismatch\r\nFORGED_AZURE_ENTRY";

        HttpHeaders headers = mock(HttpHeaders.class);
        when(headers.getValue("x-ms-error-code")).thenReturn(injectedAzureCode);

        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(403);
        when(response.getHeaders()).thenReturn(headers);

        BlobStorageException cause = new BlobStorageException("Delegation denied", response, null);

        when(blobServiceClient.getUserDelegationKey(any(), any())).thenThrow(cause);

        AppException exception = assertThrows(AppException.class,
                () -> noticeStorageClient.getFileSignedUrl(injectedFolderId, "fileId"));

        assertSame(cause, exception.getCause());

        ILoggingEvent event = getSignedUrlFailureLog();
        String message = event.getFormattedMessage();

        assertTrue(message.contains("phase=GET_USER_DELEGATION_KEY"));
        assertTrue(message.contains("upstreamHttpStatus=403"));
        assertTrue(message.contains("folderId=suspicious log param"));
        assertTrue(message.contains("azureErrorCode=suspicious log param"));

        assertFalse(message.contains("\r"));
        assertFalse(message.contains("\n"));
        assertFalse(message.contains("FORGED_FOLDER_ENTRY"));
        assertFalse(message.contains("FORGED_AZURE_ENTRY"));

        assertNull(event.getThrowableProxy());

        verify(blobContainerClient, never()).getBlobClient(anyString());
        verifyNoInteractions(blobClientMock);
    }

    private ILoggingEvent getSignedUrlFailureLog() {
        return logAppender.list.stream()
                .filter(event -> event.getFormattedMessage().startsWith("Signed URL generation failed:")).findFirst()
                .orElseThrow(() -> new AssertionError("Expected signed URL failure log"));
    }

}
