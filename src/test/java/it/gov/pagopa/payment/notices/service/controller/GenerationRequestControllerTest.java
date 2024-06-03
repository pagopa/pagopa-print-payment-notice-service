package it.gov.pagopa.payment.notices.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.payment.notices.service.exception.AppError;
import it.gov.pagopa.payment.notices.service.exception.AppException;
import it.gov.pagopa.payment.notices.service.model.GetGenerationRequestStatusResource;
import it.gov.pagopa.payment.notices.service.model.GetSignedUrlResource;
import it.gov.pagopa.payment.notices.service.model.NoticeGenerationMassiveRequest;
import it.gov.pagopa.payment.notices.service.model.NoticeGenerationRequestItem;
import it.gov.pagopa.payment.notices.service.model.notice.*;
import it.gov.pagopa.payment.notices.service.service.NoticeGenerationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GenerationRequestControllerTest {

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private NoticeGenerationService noticeGenerationService;

    private static NoticeGenerationRequestItem getNoticeGenerationRequestItem() {
        return NoticeGenerationRequestItem.builder()
                .templateId("template")
                .data(NoticeRequestData.builder()
                        .notice(Notice.builder()
                                .code("code")
                                .dueDate("24/10/2024")
                                .subject("subject")
                                .paymentAmount(100L)
                                .installments(Collections.singletonList(
                                        InstallmentData.builder()
                                                .amount(100L)
                                                .code("codeRate")
                                                .dueDate("24/10/2024")
                                                .build()
                                ))
                                .build())
                        .creditorInstitution(CreditorInstitution.builder()
                                .taxCode("taxCode")
                                .build())
                        .debtor(Debtor.builder()
                                .taxCode("taxCode")
                                .address("address")
                                .city("city")
                                .buildingNumber("101")
                                .postalCode("00135")
                                .province("RM")
                                .fullName("Test Name")
                                .build())
                        .build())
                .build();
    }

    @BeforeEach
    void setUp() {
        Mockito.reset(noticeGenerationService);
    }

    @Test
    void getFolderStatusShouldReturnDataOn200() throws Exception {
        when(noticeGenerationService.getFolderStatus(any(), any()))
                .thenReturn(GetGenerationRequestStatusResource.builder().build());
        String url = "/notices/folder/folderTest/status";
        mvc.perform(get(url)
                        .header("X-User-Id", "userTest")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
        verify(noticeGenerationService).getFolderStatus("folderTest", "userTest");
    }

    @Test
    void getFolderStatusShouldReturnDataOn400() throws Exception {
        when(noticeGenerationService.getFolderStatus(any(), any()))
                .thenAnswer(item -> {
                    throw new AppException(AppError.FOLDER_NOT_AVAILABLE);
                });
        String url = "/notices/folder/folderTest/status";
        mvc.perform(get(url)
                        .header("X-User-Id", "userTest")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json"));
        verify(noticeGenerationService).getFolderStatus("folderTest", "userTest");
    }

    @Test
    void getFolderStatusShouldReturnDataOn500() throws Exception {
        when(noticeGenerationService.getFolderStatus(any(), any()))
                .thenAnswer(item -> {
                    throw new RuntimeException();
                });
        String url = "/notices/folder/folderTest/status";
        mvc.perform(get(url)
                        .header("X-User-Id", "userTest")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType("application/json"));
        verify(noticeGenerationService).getFolderStatus("folderTest", "userTest");
    }

    @Test
    void generateMassiveShouldReturnFolderIdOn200() throws Exception {
        when(noticeGenerationService.generateMassive(any(), any()))
                .thenReturn("folderTests");
        String url = "/notices/generate-massive";
        String folderId = mvc.perform(post(url)
                        .content(new ObjectMapper().writeValueAsBytes(
                                NoticeGenerationMassiveRequest.builder()
                                        .notices(Collections.singletonList(
                                                NoticeGenerationRequestItem.builder().build()
                                        )).build()
                        ))
                        .header("X-User-Id", "userTest")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("application/json"))
                .andReturn().getResponse().getContentAsString();
        Assertions.assertNotNull(folderId);
        Assertions.assertEquals("folderTests", folderId);
        verify(noticeGenerationService).generateMassive(any(), any());
    }

    @Test
    void generateMassiveShouldReturn400OnMissingItems() throws Exception {
        String url = "/notices/generate-massive";
        mvc.perform(post(url)
                        .content(new ObjectMapper().writeValueAsBytes(
                                NoticeGenerationMassiveRequest.builder()
                                        .notices(Collections.emptyList(

                                        )).build()
                        ))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void generateMassiveShouldReturn400OnMissingBody() throws Exception {
        String url = "/notices/generate-massive";
        mvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void generateMassiveShouldReturn400OnMissingUserId() throws Exception {
        when(noticeGenerationService.generateMassive(any(), any()))
                .thenReturn("folderTests");
        String url = "/notices/generate-massive";
        mvc.perform(post(url)
                        .content(new ObjectMapper().writeValueAsBytes(
                                NoticeGenerationMassiveRequest.builder()
                                        .notices(Collections.singletonList(
                                                NoticeGenerationRequestItem.builder().build()
                                        )).build()
                        ))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

    }

    @Test
    void generateNoticeShouldReturnFileOnOk() throws Exception {
        File tempDirectory = Files.createTempDirectory("test").toFile();
        File file = Files.createTempFile(tempDirectory.toPath(), "test", ".zip").toFile();
        when(noticeGenerationService.generateNotice(any(), any(), any()))
                .thenReturn(file);
        String url = "/notices/generate";
        mvc.perform(post(url)
                        .param("folderId", "test")
                        .header("X-User-Id", "test")
                        .content(objectMapper.writeValueAsString(
                                getNoticeGenerationRequestItem()))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
        verify(noticeGenerationService).generateNotice(any(), any(), any());
    }

    @Test
    void generateNoticeShouldReturnBadRequestOnMissingUserForFolder() throws Exception {
        File tempDirectory = Files.createTempDirectory("test").toFile();
        File file = Files.createTempFile(tempDirectory.toPath(), "test", ".zip").toFile();
        when(noticeGenerationService.generateNotice(any(), any(), any()))
                .thenReturn(file);
        String url = "/notices/generate";
        mvc.perform(post(url)
                        .param("folderId", "test")
                        .content(objectMapper.writeValueAsString(
                                getNoticeGenerationRequestItem()))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
        verifyNoInteractions(noticeGenerationService);
    }

    @Test
    void generateNoticeShouldReturnKOonErrorFile() throws Exception {
        when(noticeGenerationService.generateNotice(any(), any(), any()))
                .thenReturn(null);
        String url = "/notices/generate";
        mvc.perform(post(url)
                        .param("folderId", "test")
                        .header("X-User-Id", "test")
                        .content(objectMapper.writeValueAsString(
                                getNoticeGenerationRequestItem()))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is5xxServerError());
        verify(noticeGenerationService).generateNotice(any(), any(), any());
    }

    @Test
    void getSignedUrlShouldReturnDataOnOk() throws Exception {
        when(noticeGenerationService.getFileSignedUrl(any(), any(), any()))
                .thenReturn(GetSignedUrlResource.builder().signedUrl("test").build());
        String url = "/notices/folderId/file/fileId/url";
        mvc.perform(get(url)
                        .header("X-User-Id", "test")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        verify(noticeGenerationService).getFileSignedUrl(any(), any(), any());
    }

    @Test
    void getSignedUrlShouldReturnKOonErrorFile() throws Exception {
        when(noticeGenerationService.getFileSignedUrl(any(), any(), any()))
                .thenThrow(new AppException(AppError.INTERNAL_SERVER_ERROR));
        String url = "/notices/folderId/file/fileId/url";
        mvc.perform(get(url)
                        .header("X-User-Id", "test")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is5xxServerError());
        verify(noticeGenerationService).getFileSignedUrl(any(), any(), any());
    }

    @Test
    void deleteShouldReturnDataOnOk() throws Exception {
        String url = "/notices/folder/folderId";
        mvc.perform(delete(url)
                        .header("X-User-Id", "test")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
        verify(noticeGenerationService).deleteFolder(any(), any());
    }

    @Test
    void getFolderUrlShouldReturnDataOnOk() throws Exception {
        when(noticeGenerationService.getFolderSignedUrl(any(), any()))
                .thenReturn(GetSignedUrlResource.builder().signedUrl("test").build());
        String url = "/notices/folderId/url";
        mvc.perform(get(url)
                        .header("X-User-Id", "test")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        verify(noticeGenerationService).getFolderSignedUrl(any(), any());
    }

    @Test
    void getFolderSignedUrlShouldReturnKOonErrorFile() throws Exception {
        when(noticeGenerationService.getFolderSignedUrl(any(), any()))
                .thenThrow(new AppException(AppError.INTERNAL_SERVER_ERROR));
        String url = "/notices/folderId/url";
        mvc.perform(get(url)
                        .header("X-User-Id", "test")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is5xxServerError());
        verify(noticeGenerationService).getFolderSignedUrl(any(), any());
    }

}
