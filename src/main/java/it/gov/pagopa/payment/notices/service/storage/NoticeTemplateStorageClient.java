package it.gov.pagopa.payment.notices.service.storage;

import com.azure.core.util.Context;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableClientBuilder;
import com.azure.data.tables.models.TableServiceException;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import it.gov.pagopa.payment.notices.service.exception.AppError;
import it.gov.pagopa.payment.notices.service.exception.AppException;
import it.gov.pagopa.payment.notices.service.model.TemplateResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Component
public class NoticeTemplateStorageClient {

    private BlobContainerClient blobContainerClient;

    private TableClient tableClient;

    private Integer maxRetry;

    private Integer timeout;

    @Autowired
    public NoticeTemplateStorageClient(
            @Value("${spring.cloud.azure.storage.blob.templates.enabled}") String enabled,
            @Value("${spring.cloud.azure.storage.blob.templates.connection_string}") String connectionString,
            @Value("${spring.cloud.azure.storage.blob.templates.containerName}") String containerName,
            @Value("${spring.cloud.azure.storage.blob.templates.tableName}") String tableName,
            @Value("${spring.cloud.azure.storage.blob.templates.retry}") Integer maxRetry,
            @Value("${spring.cloud.azure.storage.blob.templates.timeout}") Integer timeout) {
        if (Boolean.TRUE.toString().equals(enabled)) {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString).buildClient();
            blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
            tableClient = new TableClientBuilder().connectionString(connectionString)
                    .tableName(tableName).buildClient();
            this.maxRetry = maxRetry;
            this.timeout = timeout;
        }
    }
    public NoticeTemplateStorageClient(
            Boolean enabled,
            TableClient tableClient,
            BlobContainerClient blobContainerClient) {
        if (Boolean.TRUE.equals(enabled)) {
            this.tableClient = tableClient;
             this.blobContainerClient = blobContainerClient;
             this.maxRetry=3;
             this.timeout=10;
        }
    }

    /**
     * Recovers the template list data available for notice generation from
     * Azure Table Storage
     * @return template data
     */
    public List<TemplateResource> getTemplates() {

        if (tableClient == null) {
            throw new AppException(AppError.TEMPLATE_CLIENT_UNAVAILABLE);
        }

        try {
            return tableClient.listEntities().stream().map(item -> TemplateResource.builder()
                   .templateId(String.valueOf(item.getProperty("templateId")))
                   .description(String.valueOf(item.getProperty("description")))
                   .templateExampleUrl(String.valueOf(item.getProperty("templateExampleUrl")))
                   .build()).toList();
        } catch (TableServiceException tableServiceException) {
            throw new AppException(AppError.TEMPLATE_TABLE_CLIENT_ERROR, tableServiceException);
        }

    }

    /**
     * Retrieve the template from the Blob Storage
     *
     * @param templateId the name of the file to be retrieved
     * @return the File with the reference to the downloaded template
     * @throws AppException thrown for error when retrieving the template
     */
    public File getTemplate(String templateId) {

        if (blobContainerClient == null) {
            throw new AppException(AppError.TEMPLATE_CLIENT_UNAVAILABLE);
        }
        String filePath = createTempDirectory(templateId);
        try {
            blobContainerClient.getBlobClient(templateId.concat("/template.zip"))
                    .downloadToFileWithResponse(
                    getBlobDownloadToFileOptions(filePath),
                    Duration.ofSeconds(timeout),
                    Context.NONE);
            return new File(filePath);
        } catch (BlobStorageException blobStorageException) {
            throw new AppException(AppError.TEMPLATE_NOT_FOUND, blobStorageException);
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
            Path tempDirectory = Files.createTempDirectory(workingDirectory.toPath(), "notice-generation-service")
                    .normalize().toAbsolutePath();
            Path filePath = tempDirectory.resolve(templateId + ".zip").normalize().toAbsolutePath();
            if (!filePath.startsWith(tempDirectory + File.separator)) {
                throw new IllegalArgumentException("Invalid filename");
            }
            return filePath.toFile().getAbsolutePath();
        } catch (IllegalArgumentException | IOException e) {
            throw new AppException(AppError.TEMPLATE_CLIENT_ERROR, e);
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
