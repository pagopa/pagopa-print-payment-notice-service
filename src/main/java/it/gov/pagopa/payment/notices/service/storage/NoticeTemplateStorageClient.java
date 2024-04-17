package it.gov.pagopa.payment.notices.service.storage;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import it.gov.pagopa.payment.notices.service.exception.AppError;
import it.gov.pagopa.payment.notices.service.exception.AppException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component
public class NoticeTemplateStorageClient {

    private BlobContainerClient blobContainerClient;

    public NoticeTemplateStorageClient(
            @Value("spring.cloud.azure.storage.blob.templates.enabled") String enabled,
            @Value("spring.cloud.azure.storage.blob.templates.connection_string") String connectionString,
            @Value("spring.cloud.azure.storage.blob.templates.containerName") String containerName) {
        if (Boolean.TRUE.toString().equals(enabled)) {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString).buildClient();
            blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
        }
    }

    public ByteArrayResource getTemplate(String templateId) {

        if (blobContainerClient == null) {
            throw new AppException(AppError.TEMPLATE_CLIENT_UNAVAILABLE);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            blobContainerClient.getBlobClient(templateId).download(outputStream);
            final byte[] bytes = outputStream.toByteArray();
            if (bytes.length == 0) {
                throw new AppException(AppError.TEMPLATE_NOT_FOUND);
            }
            return new ByteArrayResource(bytes);
        } catch (NullPointerException nullPointerException) {
            throw new AppException(AppError.TEMPLATE_NOT_FOUND);
        }
    }

}
