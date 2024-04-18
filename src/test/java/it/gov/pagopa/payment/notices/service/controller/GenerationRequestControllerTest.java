package it.gov.pagopa.payment.notices.service.controller;

import it.gov.pagopa.payment.notices.service.exception.AppError;
import it.gov.pagopa.payment.notices.service.exception.AppException;
import it.gov.pagopa.payment.notices.service.model.GetGenerationRequestStatusResource;
import it.gov.pagopa.payment.notices.service.service.NoticeGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GenerationRequestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private NoticeGenerationService noticeGenerationService;

    @BeforeEach
    void setUp() {
        Mockito.reset(noticeGenerationService);
    }

    @Test
    void getFolderStatusShouldReturnDataOn200() throws Exception {
        when(noticeGenerationService.getFolderStatus(any(),any()))
                .thenReturn(GetGenerationRequestStatusResource.builder().build());
        String url = "/notices/folder/folderTest/status";
        mvc.perform(get(url)
                        .header("X-User-Id", "userTest")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
        verify(noticeGenerationService).getFolderStatus("folderTest","userTest");
    }

    @Test
    void getFolderStatusShouldReturnDataOn400() throws Exception {
        when(noticeGenerationService.getFolderStatus(any(),any()))
                .thenAnswer(item -> {
                    throw new AppException(AppError.FOLDER_NOT_AVAILABLE);
                });
        String url = "/notices/folder/folderTest/status";
        mvc.perform(get(url)
                        .header("X-User-Id", "userTest")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json"));
        verify(noticeGenerationService).getFolderStatus("folderTest","userTest");
    }

    @Test
    void getFolderStatusShouldReturnDataOn500() throws Exception {
        when(noticeGenerationService.getFolderStatus(any(),any()))
                .thenAnswer(item -> {
                    throw new RuntimeException();
                });
        String url = "/notices/folder/folderTest/status";
        mvc.perform(get(url)
                        .header("X-User-Id", "userTest")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType("application/json"));
        verify(noticeGenerationService).getFolderStatus("folderTest","userTest");
    }

}
