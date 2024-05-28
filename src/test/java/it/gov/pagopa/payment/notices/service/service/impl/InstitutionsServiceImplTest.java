package it.gov.pagopa.payment.notices.service.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.gov.pagopa.payment.notices.service.exception.AppError;
import it.gov.pagopa.payment.notices.service.exception.AppException;
import it.gov.pagopa.payment.notices.service.model.institutions.UploadData;
import it.gov.pagopa.payment.notices.service.storage.InstitutionsStorageClient;
import it.gov.pagopa.payment.notices.service.storage.NoticeTemplateStorageClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstitutionsServiceImplTest {

    @Mock
    private InstitutionsStorageClient institutionStorageClient;

    private InstitutionsServiceImpl institutionsService;

    @BeforeEach
    public void init() {
        Mockito.reset(institutionStorageClient);
        institutionsService = new InstitutionsServiceImpl(institutionStorageClient);
    }

    @Test
    void shouldReturnMappedTemplatesDataOnSuccess() throws IOException {
        when(institutionStorageClient.saveInstitutionsData(any(),any(),any()))
                .thenReturn(true);
        File tempDirectory = Files.createTempDirectory("test").toFile();
        File file = Files.createTempFile(tempDirectory.toPath(), "test", ".zip").toFile();
        institutionsService.uploadInstitutionsData(UploadData.builder().build(),file);
        verify(institutionStorageClient).saveInstitutionsData(any(),any(),any());
    }

    @Test
    void shouldReturnKOOnException() throws IOException {
        when(institutionStorageClient.saveInstitutionsData(any(),any(),any()))
                .thenThrow(new AppException(AppError.INSTITUTION_DATA_UPLOAD_ERROR));
        File tempDirectory = Files.createTempDirectory("test").toFile();
        File file = Files.createTempFile(tempDirectory.toPath(), "test", ".zip").toFile();
        AppException appException = assertThrows(AppException.class,
                () -> institutionsService.uploadInstitutionsData(UploadData.builder().build(), file));
        assertEquals(AppError.INSTITUTION_DATA_UPLOAD_ERROR.title, appException.getTitle());
        verify(institutionStorageClient).saveInstitutionsData(any(), any(), any());
    }

    @Test
    void shouldReturnTableClientClientOnException() throws IOException {
        when(institutionStorageClient.saveInstitutionsData(any(),any(),any()))
                .thenThrow(new RuntimeException("test"));
        File tempDirectory = Files.createTempDirectory("test").toFile();
        File file = Files.createTempFile(tempDirectory.toPath(), "test", ".zip").toFile();
        AppException appException = assertThrows(AppException.class,
                () -> institutionsService.uploadInstitutionsData(UploadData.builder().build(),file));
        assertEquals(AppError.INSTITUTION_DATA_UPLOAD_ERROR.title, appException.getTitle());
        verify(institutionStorageClient).saveInstitutionsData(any(),any(),any());
    }

    @Test
    void shouldReturnCreditorInstitutionDataOnValidRequest() {
        when(institutionStorageClient.getInstitutionData(any())).thenReturn(
                UploadData.builder().build());
        UploadData uploadData = assertDoesNotThrow(
                () -> institutionsService.getInstitutionData("test"));
        assertNotNull(uploadData);
        verify(institutionStorageClient).getInstitutionData(any());
    }

    @Test
    void shouldThrowExceptionOnInstitutionDataRecoveryKO() {
        when(institutionStorageClient.getInstitutionData(any())).thenAnswer(item -> {
            throw new AppException(AppError.INSTITUTION_NOT_FOUND);
        });
        AppException appException = assertThrows(AppException.class,
                () -> institutionsService.getInstitutionData("test"));
        assertNotNull(appException);
        assertEquals(AppError.INSTITUTION_NOT_FOUND.title, appException.getTitle());
    }

    @Test
    void shouldThrowExceptionOnInstitutionDataRecoveryKOUnexpected() {
        when(institutionStorageClient.getInstitutionData(any())).thenAnswer(item -> {
            throw new RuntimeException("error");
        });
        AppException appException = assertThrows(AppException.class,
                () -> institutionsService.getInstitutionData("test"));
        assertNotNull(appException);
        assertEquals(AppError.INSTITUTION_RETRIEVE_ERROR.title, appException.getTitle());
    }

}
