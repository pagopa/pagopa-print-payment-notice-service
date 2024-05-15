package it.gov.pagopa.payment.notices.service.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import it.gov.pagopa.payment.notices.service.exception.AppError;
import it.gov.pagopa.payment.notices.service.exception.AppException;
import it.gov.pagopa.payment.notices.service.model.institutions.UploadData;
import it.gov.pagopa.payment.notices.service.service.InstitutionsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

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

    public InstitutionsController(InstitutionsService institutionsService) {
        this.institutionsService = institutionsService;
    }

    @PostMapping(value = "/data", consumes = {
            MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE})
    public void updateInstitutions(
            @Valid @NotNull @RequestPart("institutions-data") UploadData institutionsData,
            @Valid @NotNull @RequestPart("file") FilePart logo
    ) {

        File logoImage;
        try {
            File workingDir = createWorkingDirectory();
            Path tempDirectory = Files.createTempDirectory(workingDir.toPath(), "notice-service")
                    .normalize()
                    .toAbsolutePath();
            logoImage = File.createTempFile("logo", ".png", tempDirectory.toFile());
            //logo.transferTo(logoImage);
            institutionsService.uploadInstitutionsData(institutionsData, logoImage);
        } catch (IOException e) {
            throw new AppException(AppError.LOGO_FILE_INPUT_ERROR, e);
        }

    }

}
