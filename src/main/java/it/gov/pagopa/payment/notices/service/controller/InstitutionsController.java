package it.gov.pagopa.payment.notices.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import it.gov.pagopa.payment.notices.service.model.ProblemJson;
import it.gov.pagopa.payment.notices.service.model.institutions.UploadData;
import it.gov.pagopa.payment.notices.service.service.InstitutionsService;
import it.gov.pagopa.payment.notices.service.util.OpenApiTableMetadata;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static it.gov.pagopa.payment.notices.service.util.WorkingDirectoryUtils.createWorkingDirectory;

/**
 * Rest Controller containing APIs for institution data management
 */

@RestController
@RequestMapping(value = "/institutions", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@Tag(name = "Institutions Data APIs")
public class InstitutionsController {

    private final InstitutionsService institutionsService;

    private final ObjectMapper objectMapper;

    private final Validator validator;

    public InstitutionsController(InstitutionsService institutionsService, ObjectMapper objectMapper, Validator validator) {
        this.institutionsService = institutionsService;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    /**
     * Uploads institutions data to the related storage, using the taxCode provided within
     * the UploadData instance, if the institution is already on the storage, the content
     * will be updated. The institution json data will include the link on the uploaded logo
     *
     * @param institutionsDataContent institution data to upload
     * @param logo                    institution logo to upload
     */
    @Operation(summary = "uploadInstitutionData",
            description = "Uploads or updates the provided institution data and logo on the related storage," +
                    " to be used in the payment notice generation process",
            security = {@SecurityRequirement(name = "ApiKey")})
    @OpenApiTableMetadata(readWriteIntense = OpenApiTableMetadata.ReadWrite.WRITE,
            external = true, internal = false)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
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
    @PostMapping(value = "/data", consumes = {
            MediaType.MULTIPART_FORM_DATA_VALUE})
    public void updateInstitutions(
            @Parameter(description = "String containing the json data to upload " +
                    "```{\n" +
                    "  \"taxCode\": \"ABC345678h\",\n" +
                    "  \"fullName\": \"Comune di Roma\",\n" +
                    "  \"organization\": \"organization_unit\",\n" +
                    "  \"info\": \"info@contacts.it\",\n" +
                    "  \"webChannel\": false,\n" +
                    "  \"appChannel\": false,\n" +
                    "  \"physicalChannel\": \"physicalChannel_f0abb45cbc34\",\n" +
                    "  \"cbill\": \"cbill_9c5ff5908c72\",\n" +
                    "  \"posteAccountNumber\": \"posteAccountNumber_2177702a81c2\",\n" +
                    "  \"posteAuth\": \"code_poste_auth_332\",\n" +
                    "} ```",
                    required = true,
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE)})
            @Valid @NotNull @RequestPart("institutions-data") String institutionsDataContent,
            @Parameter(description = "logo file to upload")
            @RequestParam(value = "file", required = false) MultipartFile logo
    ) {

        File logoImage = null;
        try {

            UploadData institutionsData = objectMapper.readValue(institutionsDataContent, UploadData.class);
            Set<ConstraintViolation<UploadData>> validationResults = validator.validate(institutionsData);

            if(!validationResults.isEmpty()) {
                throw new AppException(AppError.BAD_REQUEST, String.format("Validation errors on provided input: %s", getValidationErrorString(validationResults)));
            }

            if (logo != null) {
                File workingDir = createWorkingDirectory();
                Path tempDirectory = Files.createTempDirectory(workingDir.toPath(), "notice-service")
                        .normalize()
                        .toAbsolutePath();
                logoImage = File.createTempFile("logo", ".png", tempDirectory.toFile());
                logo.transferTo(logoImage);
            } else if (institutionsData.getLogo() == null) {
                throw new AppException(AppError.BAD_REQUEST,
                        "Existing logo should be passed in the json data," +
                                " or a file should be provided");
            }

            institutionsService.uploadInstitutionsData(institutionsData, logoImage);
        } catch (IOException e) {
            throw new AppException(AppError.LOGO_FILE_INPUT_ERROR, e);
        }

    }

    /**
     * Retrieving institution data, related to the provided taxCode
     *
     * @param taxCode institution data to be used retrieval
     * @return institution data
     */
    @Operation(summary = "getInstitutionData",
            description = "Retrieves saved institution data and logo on the related storage," +
                    " to be used in the payment notice generation process",
            security = {@SecurityRequirement(name = "ApiKey")})
    @OpenApiTableMetadata(readWriteIntense = OpenApiTableMetadata.ReadWrite.WRITE,
            external = true, internal = false)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "401",
                    description = "Unauthorized", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404",
                    description = "Not Found", content = @Content(
                    schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "429",
                    description = "Too many requests", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500",
                    description = "Service unavailable",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemJson.class)))
    })
    @GetMapping(value = "/data/{taxCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public UploadData getInstitutionData(
            @Parameter(description = "tax code of the CI to use for retrieval")
            @PathVariable(name = "taxCode") String taxCode) {
        return institutionsService.getInstitutionData(taxCode);
    }

    private String getValidationErrorString(Set<ConstraintViolation<UploadData>> validationResults){
        var sb = new StringBuilder();

        for (ConstraintViolation<UploadData> error : validationResults) {
            sb.append(error.getPropertyPath()).append(" ").append(error.getMessage()).append(". ");
        }
        return StringUtils.chop(sb.toString());
    }

}
