package it.gov.pagopa.payment.notices.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.gov.pagopa.payment.notices.service.exception.AppError;
import it.gov.pagopa.payment.notices.service.exception.AppException;
import it.gov.pagopa.payment.notices.service.model.*;
import it.gov.pagopa.payment.notices.service.service.NoticeGenerationService;
import it.gov.pagopa.payment.notices.service.util.Constants;
import it.gov.pagopa.payment.notices.service.util.OpenApiTableMetadata;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import static it.gov.pagopa.payment.notices.service.util.WorkingDirectoryUtils.clearTempDirectory;

/**
 * Rest Controller containing APIs for generation request management
 */
@RestController
@RequestMapping(value = "/notices", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@Tag(name = "Notice Generation Request APIs")
public class NoticesController {
    private final NoticeGenerationService noticeGenerationService;

    public NoticesController(NoticeGenerationService noticeGenerationService) {
        this.noticeGenerationService = noticeGenerationService;
    }

    @Operation(summary = "generateNotice",
            description = "Request notice generation using the generator API and returns the pdf that has been produced" +
                    "if a folderId is provided and it is available for the userId, it will be saved for future recovery",
            security = {@SecurityRequirement(name = "ApiKey")})
    @OpenApiTableMetadata(readWriteIntense = OpenApiTableMetadata.ReadWrite.WRITE,
            external = true, internal = false)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "OK",
                    content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE,
                            schema = @Schema(implementation = Resource.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "429",
                    description = "Too many requests", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500",
                    description = "Service unavailable", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemJson.class)))
    })

    /*
     * POST method to generate a single notice, if a folderId is provided the content will be saved inside the provided
     * folder
     * @param folderId optional parameter to use if the content generates has to be saved
     * @param noticeGenerationRequestItem data containing notice generation request
     * @return generated pdf
     */
    @PostMapping("/generate")
    public ResponseEntity<Resource> generateNotice(
            @Schema(description = "If the file exists you can specify the folder id. Pass it if you want to regenerate the file") @RequestParam(value = "folderId", required = false) String folderId,
            @Parameter(description = "templateId to use for retrieval")
            @Valid @NotNull @RequestBody NoticeGenerationRequestItem noticeGenerationRequestItem,
            @Schema(description = "User ID. Required if `folderId` is provided") @RequestHeader(value = Constants.X_USER_ID, required = false) String userId) {
        if(folderId != null && userId == null) {
            throw new AppException(AppError.BAD_REQUEST, "Invalid Data");
        }
        File file = noticeGenerationService.generateNotice(noticeGenerationRequestItem, folderId, userId);
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + file.getName() + ".pdf\"");
            return ResponseEntity.status(HttpStatus.CREATED)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .headers(headers)
                    .body(new ByteArrayResource(inputStream.readAllBytes()));
        } catch (Exception e) {
            throw new AppException(AppError.INTERNAL_SERVER_ERROR, e);
        } finally {
            clearTempDirectory(file.toPath().getParent());
        }
    }

    /**
     * Retrieve generation request folder status, including error and processed items. The folder
     * status will be returned only if the userId matches the allowed pairing with the folderId used
     * as input
     *
     * @param folderId folderId to retrieve generation status
     * @param userId   userId contained in the X-User-Id to be used for data retrieval
     * @return instance of GetGenerationRequestStatusResource containing a folder status
     */
    @Operation(summary = "getFolderStatus",
            description = "Return generation request status for a folder of notices",
            security = {@SecurityRequirement(name = "ApiKey")})
    @OpenApiTableMetadata(readWriteIntense = OpenApiTableMetadata.ReadWrite.READ,
            cacheable = true, external = true, internal = false)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GetGenerationRequestStatusResource.class))),
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
                    description = "Service unavailable", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemJson.class)))
    })
    @GetMapping("/folder/{folder_id}/status")
    public GetGenerationRequestStatusResource getFolderStatus(
            @Valid @NotNull @Parameter(description = "folderId to use for request status retrieval") @PathVariable("folder_id") String folderId,
            @Valid @NotNull @Parameter(description = "userId to use for request status retrieval") @RequestHeader(Constants.X_USER_ID) String userId) {
        return noticeGenerationService.getFolderStatus(folderId, userId);
    }

    /**
     * Generate the request of massive notice generation using input data, sending data through the EH channel
     * to kickstart notice generation in an async manner. Any error will be saved into notice generation request
     * error
     *
     * @param noticeGenerationMassiveRequest generation request data, containing a list of notice data and templates
     *                                       to use
     * @param userId                         userId requiring the generation. the request will refer to this user when recovery of data regarding
     *                                       the folder is executed
     * @return folderId produced when inserting the request
     */
    @Operation(summary = "generateNoticeMassiveRequest",
            description = "Insert massive notice generation request and returns folderId for reference an" +
                    " future recovery",
            security = {@SecurityRequirement(name = "ApiKey")})
    @OpenApiTableMetadata(readWriteIntense = OpenApiTableMetadata.ReadWrite.WRITE,
            external = true, internal = false)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "429",
                    description = "Too many requests", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500",
                    description = "Service unavailable", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemJson.class)))
    })
    @PostMapping("/generate-massive")
    public String generateNoticeMassiveRequest(
            @Parameter(description = "massive notice generation request data")
            @Valid @NotNull @RequestBody NoticeGenerationMassiveRequest noticeGenerationMassiveRequest,
            @Parameter(description = "userId to use for request status retrieval")
            @Valid @NotNull @RequestHeader(Constants.X_USER_ID) String userId) {
        return noticeGenerationService.generateMassive(noticeGenerationMassiveRequest, userId);
    }

    /**
     * Retrieve the signed url for a file.
     *
     * @param folderId folderId to use for recovery
     * @param fileId   fileId to use for recovery
     * @return signed url for file
     */
    @Operation(summary = "getSignedUrlResource",
            description = "Return file signedUrl",
            security = {@SecurityRequirement(name = "ApiKey")})
    @OpenApiTableMetadata(readWriteIntense = OpenApiTableMetadata.ReadWrite.READ,
            cacheable = true, external = true, internal = false)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return file signed url",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GetSignedUrlResource.class)
                    )),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Folder or file not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "429",
                    description = "Too many requests", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500",
                    description = "Service error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "503",
                    description = "Service or notice storage unavailable",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemJson.class)))
    })
    @GetMapping("/folder/{folderId}/file/{fileId}/url")
    public GetSignedUrlResource getSignedUrlResource(
            @Valid @NotNull @Parameter(description = "folderId to use for request retrieval") @PathVariable("folderId") String folderId,
            @Valid @NotNull @Parameter(description = "userId to use for request retrieval") @RequestHeader("X-User-Id") String userId,
            @Valid @NotNull @Parameter(description = "fileId to use for request retrieval") @PathVariable("fileId") String fileId) {
        return noticeGenerationService.getFileSignedUrl(folderId, fileId, userId);
    }

    /**
     * Delete a folder related to a generation request
     *
     * @param folderId folderId to use for deletion
     * @param userId   userId to use for permission check
     */
    @Operation(summary = "deleteFolder",
            description = "Delete selected folder, if allowed",
            security = {@SecurityRequirement(name = "ApiKey")})
    @OpenApiTableMetadata(readWriteIntense = OpenApiTableMetadata.ReadWrite.READ,
            cacheable = true, external = true, internal = false)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Folder Deleted"),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Folder not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "429",
                    description = "Too many requests", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500",
                    description = "Service error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "503",
                    description = "Service or notice storage unavailable",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemJson.class)))
    })
    @DeleteMapping("/folder/{folderId}")
    public void deleteFolder(
            @Valid @NotNull @Parameter(description = "folderId to use for request retrieval") @PathVariable("folderId") String folderId,
            @Valid @NotNull @Parameter(description = "userId to use for request retrieval") @RequestHeader("X-User-Id") String userId) {
        noticeGenerationService.deleteFolder(folderId, userId);
    }


    /**
     * Return a folder signed url
     *
     * @param folderId folder id to use for folder retrieval
     * @param userId   user id to use for permission check
     * @return instance of GetSignedUrlResource containing a signed url
     */
    @Operation(summary = "getFolderSignedUrlResource",
            description = "Return compressed folder file signedUrl",
            security = {@SecurityRequirement(name = "ApiKey")})
    @OpenApiTableMetadata(readWriteIntense = OpenApiTableMetadata.ReadWrite.READ,
            cacheable = true, external = true, internal = false)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Return folder signed url",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GetSignedUrlResource.class)
                    )),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Folder or file not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "429",
                    description = "Too many requests", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500",
                    description = "Service error", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "503",
                    description = "Service or notice storage unavailable",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemJson.class)))
    })
    @GetMapping("/folder/{folderId}/url")
    public GetSignedUrlResource getFolderSignedUrlResource(
            @Valid @NotNull @Parameter(description = "folderId to use for request retrieval") @PathVariable("folderId") String folderId,
            @Valid @NotNull @Parameter(description = "userId to use for request retrieval") @RequestHeader("X-User-Id") String userId) {
        return noticeGenerationService.getFolderSignedUrl(folderId, userId);
    }

}
