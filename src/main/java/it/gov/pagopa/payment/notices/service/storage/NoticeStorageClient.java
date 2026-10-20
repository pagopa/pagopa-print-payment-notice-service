package it.gov.pagopa.payment.notices.service.storage;

import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import it.gov.pagopa.payment.notices.service.exception.AppError;
import it.gov.pagopa.payment.notices.service.exception.AppException;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.regex.Pattern;

import static it.gov.pagopa.payment.notices.service.util.CommonUtility.sanitizeLogParam;

@Component
@Slf4j
public class NoticeStorageClient {

    private static final Pattern ENTRA_ERROR_CODE_PATTERN = Pattern.compile("\\bAADSTS\\d+\\b");

    private BlobContainerClient blobContainerClient;

    private BlobServiceClient blobServiceClient;

    private String externalUrl;

    @Autowired
    public NoticeStorageClient(
            @Value("${spring.cloud.azure.storage.blob.notices.enabled}") String enabled,
            @Value("${spring.cloud.azure.storage.blob.notices.endpoint}") String endpoint,
            @Value("${spring.cloud.azure.storage.blob.notices.tenantId}") String tenantId,
            @Value("${spring.cloud.azure.storage.blob.notices.clientId}") String clientId,
            @Value("${spring.cloud.azure.storage.blob.notices.clientSecret}") String clientSecret,
            @Value("${spring.cloud.azure.storage.blob.notices.containerName}") String containerName,
            @Value("${spring.cloud.azure.storage.blob.notices.externalUrl}") String externalUrl) {
        if(Boolean.TRUE.toString().equals(enabled)) {
            TokenCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                    .tenantId(tenantId)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build();
            blobServiceClient = new BlobServiceClientBuilder()
                    .endpoint(endpoint).credential(clientSecretCredential).buildClient();
            blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
            this.externalUrl = externalUrl;
        }
    }

    public NoticeStorageClient(
            Boolean enabled,
            BlobServiceClient blobServiceClient,
            BlobContainerClient blobContainerClient,
            String externalUrl) {
        if(Boolean.TRUE.equals(enabled)) {
            this.blobServiceClient = blobServiceClient;
            this.blobContainerClient = blobContainerClient;
            this.externalUrl = externalUrl;
        }
    }

    public String getFileSignedUrl(String folderId, String fileId) {

        if (blobContainerClient == null) {
            throw new AppException(AppError.NOTICE_CLIENT_UNAVAILABLE);
        }

        String phase = "GET_USER_DELEGATION_KEY";

        try {
            OffsetDateTime referenceTime = OffsetDateTime.now(ZoneOffset.UTC).withNano(0);

            OffsetDateTime startTime = referenceTime.minusMinutes(5);

            // The delegation key and SAS expire five minutes after the reference time.
            OffsetDateTime expiryTime = referenceTime.plusMinutes(5);

            UserDelegationKey userDelegationKey = blobServiceClient.getUserDelegationKey(startTime, expiryTime);

            phase = "PREPARE_SAS";

            // Assign read permissions to the SAS token
            BlobSasPermission sasPermission = new BlobSasPermission().setReadPermission(true);

            BlobClient blobClient = blobContainerClient.getBlobClient(String.join("/", folderId, fileId));

            BlobServiceSasSignatureValues sasSignatureValues = new BlobServiceSasSignatureValues(expiryTime,
                    sasPermission).setStartTime(startTime);

            phase = "GENERATE_SAS";

            String sasToken = blobClient.generateUserDelegationSas(sasSignatureValues, userDelegationKey);

            phase = "BUILD_SIGNED_URL";

            String[] blobUrl = blobClient.getBlobUrl().split("/");
            blobUrl[2] = externalUrl;

            return StringUtils.joinWith("?", URLDecoder.decode(String.join("/", blobUrl), StandardCharsets.UTF_8),
                    sasToken);

        } catch (RuntimeException e) {
            logSignedUrlFailure(phase, folderId, e);
            throw new AppException(AppError.ERROR_ON_GET_FILE_URL_REQUEST, e);
        }
    }

    public void deleteFolder(String folderId) {

        if(blobContainerClient == null) {
            throw new AppException(AppError.NOTICE_CLIENT_UNAVAILABLE);
        }

        try {
            ListBlobsOptions options = new ListBlobsOptions().setPrefix(folderId)
                    .setDetails(new BlobListDetails().setRetrieveDeletedBlobs(false).setRetrieveSnapshots(false));
            blobContainerClient.listBlobs(options, null).iterator()
                    .forEachRemaining(item -> blobContainerClient.getBlobClient(item.getName()).delete());
        } catch (BlobStorageException blobStorageException) {
            throw new AppException(AppError.COULD_NOT_DELETE_FOLDER_ERROR, blobStorageException);
        }

    }

    private void logSignedUrlFailure(String phase, String folderId, RuntimeException exception) {

        Integer upstreamHttpStatus = null;
        String azureErrorCode = null;
        Throwable current = exception;

        for (int depth = 0; current != null && depth < 16; depth++) {

            if (upstreamHttpStatus == null) {
                upstreamHttpStatus = extractUpstreamHttpStatus(current);
            }

            if (azureErrorCode == null) {
                azureErrorCode = extractAzureErrorCode(current);
            }

            current = current.getCause();
        }

        log.error(
                "Signed URL generation failed: phase={} folderId={} "
                        + "upstreamHttpStatus={} azureErrorCode={} exceptionType={}",
                phase, folderId != null ? sanitizeLogParam(folderId) : "N/A",
                upstreamHttpStatus != null ? upstreamHttpStatus.toString() : "N/A",
                azureErrorCode != null ? sanitizeLogParam(azureErrorCode) : "N/A", exception.getClass().getName());
    }

    private Integer extractUpstreamHttpStatus(Throwable exception) {

        if (exception instanceof HttpResponseException httpException && httpException.getResponse() != null) {

            return httpException.getResponse().getStatusCode();
        }

        return null;
    }

    private String extractAzureErrorCode(Throwable exception) {

        if (exception instanceof BlobStorageException storageException && storageException.getResponse() != null
                && storageException.getResponse().getHeaders() != null) {

            var errorCode = storageException.getErrorCode();

            if (errorCode != null) {
                return errorCode.toString();
            }
        }

        String message = exception.getMessage();

        if (message == null) {
            return null;
        }

        var matcher = ENTRA_ERROR_CODE_PATTERN.matcher(message);

        return matcher.find() ? matcher.group() : null;
    }
}
