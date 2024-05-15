package it.gov.pagopa.payment.notices.service.storage;

import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.payment.notices.service.exception.AppError;
import it.gov.pagopa.payment.notices.service.exception.AppException;
import it.gov.pagopa.payment.notices.service.model.institutions.UploadData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;

@Component
@Slf4j
public class InstitutionsStorageClient {

    private BlobContainerClient blobContainerClient;

    private ObjectMapper objectMapper;

    @Autowired
    public InstitutionsStorageClient(
            @Value("${spring.cloud.azure.storage.blob.institutions.enabled}") String enabled,
            @Value("${spring.cloud.azure.storage.blob.institutions.connection_string}") String connectionString,
            @Value("${spring.cloud.azure.storage.blob.institutions.containerName}") String containerName,
            ObjectMapper objectMapper) {
        if (Boolean.TRUE.toString().equals(enabled)) {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString).buildClient();
            this.blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
            this.objectMapper = objectMapper;
        }
    }
    public InstitutionsStorageClient(
            Boolean enabled,
            BlobContainerClient blobContainerClient) {
        if (Boolean.TRUE.equals(enabled)) {
             this.blobContainerClient = blobContainerClient;
             this.objectMapper = new ObjectMapper();
        }
    }

    /**
     * Handles saving the institutions data to the blob storage
     *
     * @param data Institutions data
     * @param logo Institutions logo
     * @return boolean to define if upload is succesfull
     */
    public boolean saveInstitutionsData(String taxCode, UploadData data, InputStream logo) throws JsonProcessingException {

        if(blobContainerClient == null) {
            throw new AppException(AppError.INSTITUTION_CLIENT_UNAVAILABLE);
        }

        try {

            //Get a reference to a blob
            BlobClient blobClient = blobContainerClient.getBlobClient(String.join("/", taxCode,
                    "logo.png"));

            //Upload the blob
            Response<BlockBlobItem> blockBlobItemResponse = blobClient.uploadWithResponse(
                    new BlobParallelUploadOptions(
                            logo
                    ), null, null);

            data.setLogo(blobClient.getBlobUrl());

            //Get a reference to a blob
            BlobClient jsonBlobClient = blobContainerClient.getBlobClient(
                    String.join("/", taxCode, "data.json"));

            //Upload the blob
            Response<BlockBlobItem> jsonBlockBlobItemResponse = jsonBlobClient.uploadWithResponse(
                    new BlobParallelUploadOptions(
                            BinaryData.fromBytes(
                                    objectMapper.writeValueAsString(data).getBytes())
                    ), null, null);

            //Build response accordingly
            int statusCode = blockBlobItemResponse.getStatusCode();
            int jsonBlobCode = jsonBlockBlobItemResponse.getStatusCode();

            return statusCode == HttpStatus.CREATED.value() && jsonBlobCode == HttpStatus.CREATED.value();
        } catch (BlobStorageException blobStorageException) {
            log.error(blobStorageException.getMessage(), blobStorageException);
            throw new AppException(AppError.INSTITUTION_DATA_UPLOAD_ERROR, blobStorageException);
        }

    }

}
