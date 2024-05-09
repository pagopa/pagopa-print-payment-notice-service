package it.gov.pagopa.payment.notices.service.storage;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.storage.blob.*;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import it.gov.pagopa.payment.notices.service.exception.AppError;
import it.gov.pagopa.payment.notices.service.exception.AppException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class NoticeStorageClient {

    private BlobContainerClient blobContainerClient;

    private BlobServiceClient blobServiceClient;

    @Autowired
    public NoticeStorageClient(
            @Value("${spring.cloud.azure.storage.blob.notices.enabled}") String enabled,
            @Value("${spring.cloud.azure.storage.blob.notices.endpoint}") String endpoint,
            @Value("${spring.cloud.azure.storage.blob.notices.clientId}") String clientId,
            @Value("${spring.cloud.azure.storage.blob.notices.containerName}") String containerName) {
        if(Boolean.TRUE.toString().equals(enabled)) {
            TokenCredential tokenCredential = new ManagedIdentityCredentialBuilder()
                    .clientId(clientId)
                    .build();
            blobServiceClient = new BlobServiceClientBuilder()
                    .endpoint(endpoint)
                    .credential(tokenCredential).buildClient();
            blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
        }
    }

    public NoticeStorageClient(
            Boolean enabled,
            BlobServiceClient blobServiceClient,
            BlobContainerClient blobContainerClient) {
        if(Boolean.TRUE.equals(enabled)) {
            this.blobServiceClient = blobServiceClient;
            this.blobContainerClient = blobContainerClient;
        }
    }

    public String getFileSignedUrl(String folderId, String fileId) {

        if(blobContainerClient == null) {
            throw new AppException(AppError.NOTICE_CLIENT_UNAVAILABLE);
        }

        UserDelegationKey userDelegationKey = blobServiceClient.getUserDelegationKey(
                OffsetDateTime.now().minusMinutes(5),
                OffsetDateTime.now().plusMinutes(5));

        // Create a SAS token that's valid for 1 day, as an example
        OffsetDateTime expiryTime = OffsetDateTime.now().plusMinutes(5);

        // Assign read permissions to the SAS token
        BlobSasPermission sasPermission = new BlobSasPermission()
                .setReadPermission(true);

        BlobClient blobClient = blobContainerClient.getBlobClient(
                String.join("/", folderId, fileId));
        BlobServiceSasSignatureValues sasSignatureValues = new BlobServiceSasSignatureValues
                (expiryTime, sasPermission)
                .setStartTime(OffsetDateTime.now().minusMinutes(5));

        try {
            String sasToken = blobClient.generateUserDelegationSas(sasSignatureValues, userDelegationKey);
            return new BlobClientBuilder()
                    .endpoint(blobClient.getBlobUrl())
                    .blobName(fileId)
                    .sasToken(sasToken)
                    .buildClient().getBlobUrl();
        } catch (BlobStorageException blobStorageException) {
            throw new AppException(AppError.COULD_NOT_GET_FILE_URL_ERROR, blobStorageException);
        }

    }

}
