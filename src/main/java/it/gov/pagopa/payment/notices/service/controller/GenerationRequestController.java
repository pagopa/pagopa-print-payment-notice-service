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
import it.gov.pagopa.payment.notices.service.model.NoticeGenerationMassiveRequest;
import it.gov.pagopa.payment.notices.service.model.ProblemJson;
import it.gov.pagopa.payment.notices.service.service.NoticeGenerationService;
import it.gov.pagopa.payment.notices.service.util.OpenApiTableMetadata;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Rest Controller containing APIs for generation request management
 */
@RestController
@RequestMapping(value = "/notices", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@Tag(name = "Notice Generation Request APIs")
public class GenerationRequestController {
    private final NoticeGenerationService noticeGenerationService;

    public GenerationRequestController(NoticeGenerationService noticeGenerationService) {
        this.noticeGenerationService = noticeGenerationService;
    }

    /**
     * Retrieve generation request folder status, including error and processed items. The folder
     * status will be returned only if the userId matches the allowed pairing with the folderId used
     * as input
     * @param folderId folderId to retrieve generation status
     * @param userId userId contained in the X-User-Id to be used for data retrieval
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
            @Parameter(description = "folderId to use for request status retrieval") @PathVariable("folder_id") String folderId,
            @Parameter(description = "userId to use for request status retrieval") @RequestHeader("X-User-Id") String userId) {
        return noticeGenerationService.getFolderStatus(folderId, userId);
    }


    @PostMapping("/generate-massive")
    public String getFolderStatus(
            @RequestBody NoticeGenerationMassiveRequest noticeGenerationMassiveRequest,
            @Parameter(description = "userId to use for request status retrieval") @RequestHeader("X-User-Id") String userId) {
        return noticeGenerationService.generateMassive(noticeGenerationMassiveRequest, userId);
    }

}
