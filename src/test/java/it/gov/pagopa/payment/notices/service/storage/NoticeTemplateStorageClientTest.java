package it.gov.pagopa.payment.notices.service.storage;

import com.azure.core.util.Context;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class NoticeTemplateStorageClientTest {

    private BlobContainerClient blobContainerClient;

    private static BlobClient blobClientMock;
    private NoticeTemplateStorageClient noticeTemplateStorageClient;

    @BeforeEach
    public void init() {
        blobContainerClient = mock(BlobContainerClient.class);
        noticeTemplateStorageClient = new NoticeTemplateStorageClient(true, blobContainerClient);
        blobClientMock = mock(BlobClient.class);
        doReturn(blobClientMock).when(blobContainerClient).getBlobClient(anyString());
    }

    @Test
    void shouldReturnTemplate() {
        doReturn(null).when(blobClientMock)
                .downloadToFileWithResponse(
                        any(BlobDownloadToFileOptions.class),
                        any(Duration.class),
                        any(Context.class)
                );
        File result = noticeTemplateStorageClient.getTemplate("testFile");
        assertNotNull(result);
    }

}
