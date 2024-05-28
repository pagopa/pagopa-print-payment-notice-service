package it.gov.pagopa.payment.notices.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.payment.notices.service.exception.AppError;
import it.gov.pagopa.payment.notices.service.exception.AppException;
import it.gov.pagopa.payment.notices.service.model.institutions.UploadData;
import it.gov.pagopa.payment.notices.service.service.InstitutionsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InstitutionsControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MockMvc mvc;
    @MockBean
    private InstitutionsService institutionsService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        Mockito.reset(institutionsService);
    }

    @Test
    void updateInstitutionsShouldReturnOk() throws Exception {
        UploadData uploadData =
                UploadData.builder()
                        .cbill("cbill")
                        .info("info")
                        .webChannel(true)
                        .taxCode("123132")
                        .posteAccountNumber("1313")
                        .fullName("121212")
                        .organization("test")
                        .physicalChannel("1212")
                .build();
        String url = "/institutions/data";
        mvc.perform(multipart(url)
                        .file("file","".getBytes())
                        .part(new MockPart("institutions-data",
                                objectMapper.writeValueAsString(uploadData).getBytes()))
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk());
        verify(institutionsService).uploadInstitutionsData(any(), any());
    }

    @Test
    void updateInstitutionsShouldReturnKoOnValidationException() throws Exception {
        UploadData uploadData =
                UploadData.builder()
                        .cbill("cbill")
                        .info("info")
                        .webChannel(true)
                        .posteAccountNumber("1313")
                        .fullName("121212")
                        .organization("test")
                        .physicalChannel("1212")
                        .build();
        String url = "/institutions/data";
        mvc.perform(multipart(url)
                        .file("file", "".getBytes())
                        .part(new MockPart("institutions-data",
                                objectMapper.writeValueAsString(uploadData).getBytes()))
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateInstitutionsShouldReturnKoOnIoException() throws Exception {
        UploadData uploadData =
                UploadData.builder()
                        .cbill("cbill")
                        .info("info")
                        .webChannel(true)
                        .taxCode("123132")
                        .posteAccountNumber("1313")
                        .fullName("121212")
                        .organization("test")
                        .physicalChannel("1212")
                        .build();
        doAnswer(invocationOnMock -> {
            throw new IOException();
        }).when(institutionsService).uploadInstitutionsData(any(), any());
        String url = "/institutions/data";
        mvc.perform(multipart(url)
                        .file("file", "".getBytes())
                        .part(new MockPart("institutions-data",
                                objectMapper.writeValueAsString(uploadData).getBytes()))
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().is5xxServerError());
        verify(institutionsService).uploadInstitutionsData(any(), any());
    }

    @Test
    void getInstitutionsShouldReturnOk() throws Exception {
        UploadData uploadData =
                UploadData.builder()
                        .cbill("cbill")
                        .info("info")
                        .webChannel(true)
                        .taxCode("123132")
                        .posteAccountNumber("1313")
                        .fullName("121212")
                        .organization("test")
                        .physicalChannel("1212")
                        .build();
        doReturn(uploadData).when(institutionsService).getInstitutionData(any());
        String url = "/institutions/data/211212";
        MvcResult mvcResult = mvc.perform(get(url))
                .andExpect(status().isOk())
                .andReturn();
        UploadData result = objectMapper.readValue(mvcResult.getResponse()
                .getContentAsString(), UploadData.class);
        Assertions.assertEquals(uploadData, result);
        verify(institutionsService).getInstitutionData(any());
    }

    @Test
    void getInstitutionsShouldReturnKOForNotFound() throws Exception {
        doAnswer(item -> {
            throw new AppException(AppError.INSTITUTION_NOT_FOUND);
        }).when(institutionsService).getInstitutionData(any());
        String url = "/institutions/data/211212";
        MvcResult mvcResult = mvc.perform(get(url))
                .andExpect(status().isNotFound())
                .andReturn();
        verify(institutionsService).getInstitutionData(any());
    }

    @Test
    void getInstitutionsShouldReturnKOForUnexpectedError() throws Exception {
        doAnswer(item -> {
            throw new AppException(AppError.INSTITUTION_RETRIEVE_ERROR);
        }).when(institutionsService).getInstitutionData(any());
        String url = "/institutions/data/211212";
        MvcResult mvcResult = mvc.perform(get(url))
                .andExpect(status().isInternalServerError())
                .andReturn();
        verify(institutionsService).getInstitutionData(any());
    }

}
