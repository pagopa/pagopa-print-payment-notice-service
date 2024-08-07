package it.gov.pagopa.payment.notices.service.storage;

import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.BlockBlobItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.payment.notices.service.exception.AppException;
import it.gov.pagopa.payment.notices.service.model.institutions.UploadData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstitutionsStorageClientTest {

    private static BlobClient blobClientMock;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private BlobContainerClient blobContainerClient;
    private InstitutionsStorageClient institutionsStorageClient;

    @BeforeEach
    public void init() {
        blobContainerClient = mock(BlobContainerClient.class);
        institutionsStorageClient = new InstitutionsStorageClient(
                true, blobContainerClient, "testUrl");
        blobClientMock = mock(BlobClient.class);
        lenient().doReturn(blobClientMock).when(blobContainerClient).getBlobClient(anyString());
    }

    @Test
    void shouldUploadCreditorInstitutionsData() throws IOException {
        Response response = Mockito.mock(Response.class);
        when(response.getStatusCode()).thenReturn(200);
        doReturn(response).when(blobClientMock).uploadWithResponse(any(),any(),any());
        doReturn("https://api.dev.platform.pagopa.it/printit-blob/v1/institutionslogoblob/99999000013%2Flogo.png").when(blobClientMock).getBlobUrl();
        try (ByteArrayInputStream bis = new ByteArrayInputStream("".getBytes())) {
            Boolean result = institutionsStorageClient.saveInstitutionsData(
                    "testFile", new UploadData(), bis);
            assertNotNull(result);
        }
    }

    @Test
    void shouldReturnException() throws IOException {
        doThrow(new BlobStorageException("test", null, null)).when(blobClientMock)
                .uploadWithResponse(any(),any(),any());
        try (ByteArrayInputStream bis = new ByteArrayInputStream("".getBytes())) {
            assertThrows(AppException.class, () -> institutionsStorageClient
                    .saveInstitutionsData("testFile", new UploadData(), bis));
        }
    }

    @Test
    void shouldReturnExceptionOnMissingClient() throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream("".getBytes())) {
            assertThrows(AppException.class, () ->
                    new InstitutionsStorageClient(false, null, "testUrl")
                            .saveInstitutionsData("testFile", new UploadData(),
                                    bis));
        }
    }

    @Test
    void shouldReturnCreditorInstitutions() throws JsonProcessingException {
        doReturn(BinaryData.fromBytes(objectMapper.writeValueAsString(
                UploadData.builder().build()).getBytes()))
                .when(blobClientMock)
                .downloadContent();
        UploadData result = institutionsStorageClient.getInstitutionData("testFile");
        assertNotNull(result);
    }

    @Test
    void shouldReturnExceptionOnReturnData() {
        doThrow(new BlobStorageException("test", null, null)).when(blobClientMock)
                .downloadContent();
        assertThrows(AppException.class, () -> institutionsStorageClient.getInstitutionData("testFile"));
    }

    @Test
    void shouldReturnExceptionOnIoError() {
        doAnswer(item -> {
            throw new IOException("test");
        }).when(blobClientMock)
                .downloadContent();
        assertThrows(AppException.class, () ->
                institutionsStorageClient.getInstitutionData("testFile"));
    }


    @Test
    void shouldReturnExceptionOnMissingClientForDataRetrieve() {
        assertThrows(AppException.class, () ->
                new InstitutionsStorageClient(false, null, "testUrl")
                        .getInstitutionData("testFile"));
    }

}
