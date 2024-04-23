package it.gov.pagopa.payment.notices.service.storage;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableServiceException;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import it.gov.pagopa.payment.notices.service.exception.AppException;
import it.gov.pagopa.payment.notices.service.model.TemplateResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoticeTemplateStorageClientTest {

    private BlobContainerClient blobContainerClient;

    private static BlobClient blobClientMock;

    private static TableClient tableClientMock;

    private NoticeTemplateStorageClient noticeTemplateStorageClient;

    @BeforeEach
    public void init() {
        blobContainerClient = mock(BlobContainerClient.class);
        tableClientMock = mock(TableClient.class);

        noticeTemplateStorageClient = new NoticeTemplateStorageClient(
                true, tableClientMock, blobContainerClient);
        blobClientMock = mock(BlobClient.class);
        lenient().doReturn(blobClientMock).when(blobContainerClient).getBlobClient(anyString());
    }


    @Test
    void shouldReturnTemplates() {
        TableEntity tableEntity = new TableEntity("test","test");
        tableEntity.addProperty("templateId","test");
        tableEntity.addProperty("description","test");
        tableEntity.addProperty("templateExampleUrl","testUrl");
        PagedIterable pagedIterable = Mockito.mock(PagedIterable.class);
        doReturn(Collections.singletonList(tableEntity).stream()).when(pagedIterable).stream();
        doReturn(pagedIterable).when(tableClientMock).listEntities();
        List<TemplateResource> result = noticeTemplateStorageClient.getTemplates();
        assertNotNull(result);
        assertEquals(1, result.size());
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

    @Test
    void shouldReturnException() {
        doThrow(new BlobStorageException("test", null, null)).when(blobClientMock)
                .downloadToFileWithResponse(
                        any(BlobDownloadToFileOptions.class),
                        any(Duration.class),
                        any(Context.class)
                );
        assertThrows(AppException.class, () -> noticeTemplateStorageClient.getTemplate("testFile"));
    }

    @Test
    void shouldReturnExceptionOnMissingClient() {
        assertThrows(AppException.class, () ->
                new NoticeTemplateStorageClient(false, null, null)
                        .getTemplate("testFile"));
    }

    @Test
    void shouldReturnExceptionOnMissingClientForGetTemplates() {
        assertThrows(AppException.class, () ->
                new NoticeTemplateStorageClient(false, null, null)
                        .getTemplates());
    }

    @Test
    void shouldReturnExceptionForGetTemplates() {
        doThrow(new TableServiceException("test", null, null)).when(tableClientMock)
                .listEntities();
        assertThrows(AppException.class, () -> noticeTemplateStorageClient.getTemplates());
    }

    @Test
    void shouldReturnKOOnWrongFile() {
        assertThrows(AppException.class, () ->
                noticeTemplateStorageClient.getTemplate("../../testFile"));
    }

}
