package it.gov.pagopa.payment.notices.service.storage;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import it.gov.pagopa.payment.notices.service.exception.AppError;
import it.gov.pagopa.payment.notices.service.exception.AppException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

public class NoticeTemplateStorageClient {

    private final BlobContainerClient blobContainerClient;

    public NoticeTemplateStorageClient(
            @Value("spring.cloud.azure.storage.blob.templates.connection_string") String connectionString,
            @Value("spring.cloud.azure.storage.blob.templates.containerName") String containerName) {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString).buildClient();
        blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
    }

    public ByteArrayResource getTemplate(String templateId) {
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
