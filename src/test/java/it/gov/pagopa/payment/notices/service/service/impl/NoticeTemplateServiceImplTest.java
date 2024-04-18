package it.gov.pagopa.payment.notices.service.service.impl;

import it.gov.pagopa.payment.notices.service.exception.AppError;
import it.gov.pagopa.payment.notices.service.exception.AppException;
import it.gov.pagopa.payment.notices.service.storage.NoticeTemplateStorageClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoticeTemplateServiceImplTest {

    @Mock
    private NoticeTemplateStorageClient noticeTemplateStorageClient;

    private NoticeTemplateServiceImpl noticeTemplateService;

    @BeforeEach
    public void init() {
        Mockito.reset(noticeTemplateStorageClient);
        noticeTemplateService = new NoticeTemplateServiceImpl(noticeTemplateStorageClient);
    }

    @Test
    public void shouldReturnDataOnValidTemplateRequest() throws IOException {
        File tempDirectory = Files.createTempDirectory("test").toFile();
        File file = Files.createTempFile(tempDirectory.toPath(), "test", ".zip").toFile();
        when(noticeTemplateService.getTemplate(any()))
                .thenReturn(file);
        when(noticeTemplateStorageClient.getTemplate("validTemplate"))
                .thenReturn(file);
        File returnedResource = assertDoesNotThrow(
                () -> noticeTemplateService.getTemplate("validTemplate"));
        assertNotNull(returnedResource);
        assertEquals(file, returnedResource);
        verify(noticeTemplateStorageClient).getTemplate("validTemplate");
    }

    @Test
    public void shouldReturnKOOnException() {
        when(noticeTemplateStorageClient.getTemplate("missingTemplate"))
                .thenThrow(new AppException(AppError.TEMPLATE_NOT_FOUND));
        AppException appException = assertThrows(AppException.class,
                () -> noticeTemplateService.getTemplate("missingTemplate"));
        assertEquals(AppError.TEMPLATE_NOT_FOUND.title, appException.getTitle());
        verify(noticeTemplateStorageClient).getTemplate("missingTemplate");
    }

    @Test
    public void shouldReturnMissingStorageClientOnException() {
        when(noticeTemplateStorageClient.getTemplate("missingClient"))
                .thenThrow(new AppException(AppError.TEMPLATE_CLIENT_UNAVAILABLE));
        AppException appException = assertThrows(AppException.class,
                () -> noticeTemplateService.getTemplate("missingClient"));
        assertEquals(AppError.TEMPLATE_CLIENT_UNAVAILABLE.title, appException.getTitle());
        verify(noticeTemplateStorageClient).getTemplate("missingClient");
    }

}