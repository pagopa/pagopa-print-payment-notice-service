package it.gov.pagopa.payment.notices.service.storage;

import com.azure.core.util.Context;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import it.gov.pagopa.payment.notices.service.exception.AppError;
import it.gov.pagopa.payment.notices.service.exception.AppException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;

@Component
public class NoticeTemplateStorageClient {

    private BlobContainerClient blobContainerClient;

    private Integer maxRetry;

    private Integer timeout;

    @Autowired
    public NoticeTemplateStorageClient(
            @Value("${spring.cloud.azure.storage.blob.templates.enabled}") String enabled,
            @Value("${spring.cloud.azure.storage.blob.templates.connection_string}") String connectionString,
            @Value("${spring.cloud.azure.storage.blob.templates.containerName}") String containerName,
            @Value("${spring.cloud.azure.storage.blob.templates.retry}") Integer maxRetry,
            @Value("${spring.cloud.azure.storage.blob.templates.timeout}") Integer timeout) {
        if (Boolean.TRUE.toString().equals(enabled)) {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString).buildClient();
            blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
            this.maxRetry = maxRetry;
            this.timeout = timeout;
        }
    }
    public NoticeTemplateStorageClient(
            Boolean enabled,
            BlobContainerClient blobContainerClient) {
        if (Boolean.TRUE.equals(enabled)) {
             this.blobContainerClient = blobContainerClient;
             this.maxRetry=3;
             this.timeout=10;
        }
    }

    public File getTemplate(String templateId) {

        if (blobContainerClient == null) {
            throw new AppException(AppError.TEMPLATE_CLIENT_UNAVAILABLE);
        }

        String filePath = createTempDirectory(templateId);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            blobContainerClient.getBlobClient(templateId.concat("/template.zip"))
                    .downloadToFileWithResponse(
                    getBlobDownloadToFileOptions(filePath),
                    Duration.ofSeconds(timeout),
                    Context.NONE);
            return new File(filePath);
        } catch (BlobStorageException blobStorageException) {
            throw new AppException(AppError.TEMPLATE_NOT_FOUND);
        }
    }

    private BlobDownloadToFileOptions getBlobDownloadToFileOptions(String filePath) {
        return new BlobDownloadToFileOptions(filePath)
                .setDownloadRetryOptions(new DownloadRetryOptions().setMaxRetryRequests(maxRetry))
                .setOpenOptions(new HashSet<>(
                        Arrays.asList(
                                StandardOpenOption.CREATE_NEW,
                                StandardOpenOption.WRITE,
                                StandardOpenOption.READ
                        ))
                );
    }

    private String createTempDirectory(String templateId) {
        try {
            File workingDirectory = createWorkingDirectory();
            Path tempDirectory = Files.createTempDirectory(workingDirectory.toPath(), "receipt-pdf-service");
            return tempDirectory.toAbsolutePath() + "/" + templateId + ".zip";
        } catch (IOException e) {
            throw new AppException(AppError.TEMPLATE_CLIENT_ERROR);
        }
    }


    private File createWorkingDirectory() throws IOException {
        File workingDirectory = new File("temp");
        if (!workingDirectory.exists()) {
            Files.createDirectory(workingDirectory.toPath());
        }
        return workingDirectory;
    }

}
