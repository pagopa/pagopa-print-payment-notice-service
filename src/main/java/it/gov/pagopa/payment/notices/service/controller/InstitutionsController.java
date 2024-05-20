package it.gov.pagopa.payment.notices.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.gov.pagopa.payment.notices.service.exception.AppError;
import it.gov.pagopa.payment.notices.service.exception.AppException;
import it.gov.pagopa.payment.notices.service.model.institutions.UploadData;
import it.gov.pagopa.payment.notices.service.service.InstitutionsService;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static it.gov.pagopa.payment.notices.service.util.WorkingDirectoryUtils.createWorkingDirectory;

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

    @PostMapping(value = "/data", consumes = {
            MediaType.MULTIPART_FORM_DATA_VALUE})
    public void updateInstitutions(
            @Valid @NotNull @RequestPart("institutions-data") String institutionsDataContent,
            @RequestParam(value = "file", required = false) MultipartFile logo
    ) {

        File logoImage;
        try {

            UploadData institutionsData = objectMapper.readValue(
                    institutionsDataContent, UploadData.class);

            if (!validator.validate(institutionsData).isEmpty()) {
                throw new AppException(AppError.BAD_REQUEST, "Validation errors on provided input");
            }

            File workingDir = createWorkingDirectory();
            Path tempDirectory = Files.createTempDirectory(workingDir.toPath(), "notice-service")
                    .normalize()
                    .toAbsolutePath();
            logoImage = File.createTempFile("logo", ".png", tempDirectory.toFile());
            logo.transferTo(logoImage);
            institutionsService.uploadInstitutionsData(institutionsData, logoImage);
        } catch (IOException e) {
            throw new AppException(AppError.LOGO_FILE_INPUT_ERROR, e);
        }

    }

}
