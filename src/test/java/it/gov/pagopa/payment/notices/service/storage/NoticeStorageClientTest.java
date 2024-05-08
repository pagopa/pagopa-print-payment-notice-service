package it.gov.pagopa.payment.notices.service.storage;

import com.azure.data.tables.TableClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobStorageException;
import it.gov.pagopa.payment.notices.service.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoticeStorageClientTest {

    private BlobContainerClient blobContainerClient;

    private static BlobClient blobClientMock;

    private static BlobServiceClient blobServiceClient;

    private NoticeStorageClient noticeStorageClient;

    @BeforeEach
    public void init() {
        blobContainerClient = mock(BlobContainerClient.class);
        blobServiceClient = mock(BlobServiceClient.class);

        noticeStorageClient = new NoticeStorageClient(
                true, blobServiceClient, blobContainerClient);
        blobClientMock = mock(BlobClient.class);
        lenient().doReturn(blobClientMock).when(blobContainerClient).getBlobClient(anyString());
    }


    @Test
    void getFileSignedUrlShouldReturnOK() {
        when(blobClientMock.getBlobUrl()).thenReturn("http://localhost:8080");
        when(blobClientMock.generateUserDelegationSas(any(),any())).thenReturn("token");
        String url = noticeStorageClient.getFileSignedUrl("folderId","fileId");
        assertNotNull(url);
    }

    @Test
    void getFileSignedUrlShouldReturnKO() {
        when(blobClientMock.generateUserDelegationSas(any(),any())).thenAnswer(item -> {
            throw new BlobStorageException("test", null, null);
        });
        assertThrows(AppException.class, () ->noticeStorageClient
                .getFileSignedUrl("folderId","fileId"));
    }

    @Test
    void shouldReturnExceptionOnMissingClient() {
        assertThrows(AppException.class, () ->
                new NoticeStorageClient(false, null, null)
                        .getFileSignedUrl("testFile",""));
    }



}
