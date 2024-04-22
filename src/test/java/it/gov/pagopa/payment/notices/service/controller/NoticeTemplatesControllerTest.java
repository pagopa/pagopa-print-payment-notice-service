package it.gov.pagopa.payment.notices.service.controller;

import it.gov.pagopa.payment.notices.service.exception.AppError;
import it.gov.pagopa.payment.notices.service.exception.AppException;
import it.gov.pagopa.payment.notices.service.model.GetGenerationRequestStatusResource;
import it.gov.pagopa.payment.notices.service.service.NoticeGenerationService;
import it.gov.pagopa.payment.notices.service.service.NoticeTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NoticeTemplatesControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private NoticeTemplateService noticeTemplateService;

    @BeforeEach
    void setUp() {
        Mockito.reset(noticeTemplateService);
    }

    @Test
    void getTemplatesShouldReturnDataOn200() throws Exception {
        when(noticeTemplateService.getTemplates())
                .thenReturn(Collections.emptyList());
        String url = "/notices/templates";
        mvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        verify(noticeTemplateService).getTemplates();
    }

    @Test
    void getFolderStatusShouldReturn500OnTemplateNotFoundException() throws Exception {
        when(noticeTemplateService.getTemplates())
                .thenAnswer(item -> {
                    throw new AppException(AppError.TEMPLATE_TABLE_CLIENT_ERROR);
                });
        String url = "/notices/templates";
        mvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType("application/json"));
        verify(noticeTemplateService).getTemplates();
    }

    @Test
    void getTemplatesStatusShouldReturn503OnUnavailableTemplateClient() throws Exception {
        when(noticeTemplateService.getTemplates())
                .thenAnswer(item -> {
                    throw new AppException(AppError.TEMPLATE_CLIENT_UNAVAILABLE);
                });
        String url = "/notices/templates";
        mvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType("application/json"));
        verify(noticeTemplateService).getTemplates();
    }

    @Test
    void getTemplatesShouldReturn500aOnGenericException() throws Exception {
        when(noticeTemplateService.getTemplates())
                .thenAnswer(item -> {
                    throw new RuntimeException();
                });
        String url = "/notices/templates";
        mvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType("application/json"));
        verify(noticeTemplateService).getTemplates();
    }

    @Test
    void getTemplateShouldReturnDataOn200() throws Exception {
        File tempDirectory = Files.createTempDirectory("test").toFile();
        File file = Files.createTempFile(tempDirectory.toPath(), "test", ".zip").toFile();
        when(noticeTemplateService.getTemplate(any()))
                .thenReturn(file);
        String url = "/notices/templates/validTemplate";
        mvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
        verify(noticeTemplateService).getTemplate("validTemplate");
    }

    @Test
    void getFolderStatusShouldReturn400OnTemplateNotFoundException() throws Exception {
        when(noticeTemplateService.getTemplate(any()))
                .thenAnswer(item -> {
                    throw new AppException(AppError.TEMPLATE_NOT_FOUND);
                });
        String url = "/notices/templates/missingTemplate";
        mvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json"));
        verify(noticeTemplateService).getTemplate("missingTemplate");
    }

    @Test
    void getTemplateStatusShouldReturn503OnUnavailableTemplateClient() throws Exception {
        when(noticeTemplateService.getTemplate(any()))
                .thenAnswer(item -> {
                    throw new AppException(AppError.TEMPLATE_CLIENT_UNAVAILABLE);
                });
        String url = "/notices/templates/missingTemplate";
        mvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType("application/json"));
        verify(noticeTemplateService).getTemplate("missingTemplate");
    }

    @Test
    void getTemplateShouldReturn500aOnGenericException() throws Exception {
        when(noticeTemplateService.getTemplate(any()))
                .thenAnswer(item -> {
                    throw new RuntimeException();
                });
        String url = "/notices/templates/error";
        mvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType("application/json"));
        verify(noticeTemplateService).getTemplate("error");
    }

}
