package it.gov.pagopa.payment.notices.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.gov.pagopa.payment.notices.service.model.GetGenerationRequestStatusResource;
import it.gov.pagopa.payment.notices.service.model.ProblemJson;
import it.gov.pagopa.payment.notices.service.service.NoticeGenerationService;
import it.gov.pagopa.payment.notices.service.service.NoticeTemplateService;
import it.gov.pagopa.payment.notices.service.util.OpenApiTableMetadata;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Rest Controller containing APIs for generation request management
 */
@RestController
@RequestMapping(value = "/notices/templates", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@Slf4j
@Tag(name = "Notice Templates APIs")
public class NoticeTemplatesController {

    private final NoticeTemplateService noticeTemplateService;

    public NoticeTemplatesController(NoticeTemplateService noticeTemplateService) {
        this.noticeTemplateService = noticeTemplateService;
    }

    /**
     *
     * @param templateId
     * @return
     */
    @Operation(summary = "getTemplate",
            description = "Return templates",
            security = {@SecurityRequirement(name = "ApiKey")})
    @OpenApiTableMetadata(readWriteIntense = OpenApiTableMetadata.ReadWrite.READ,
            cacheable = true, external = true, internal = false)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return template data",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE
                    )),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Folder not found or unavailable for the requirer",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "429",
                    description = "Too many requests", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500",
                    description = "Service error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "503",
                    description = "Service or template storage unavailable",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemJson.class)))
    })
    @GetMapping("/{template_id}")
    public ResponseEntity<Resource> getTemplate(
            @Parameter(description = "templateId to use for retrieval")
            @PathVariable("template_id") String templateId) {
        File file = noticeTemplateService.getTemplate(templateId);
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + templateId + ".zip\"");
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .headers(headers)
                    .body(new ByteArrayResource(inputStream.readAllBytes()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            clearTempDirectory(file.toPath().getParent());
        }
    }

    private void clearTempDirectory(java.nio.file.Path workingDirPath) {
        try {
            FileUtils.deleteDirectory(workingDirPath.toFile());
        } catch (IOException e) {
            log.warn("Unable to clear working directory", e);
        }
    }

}
