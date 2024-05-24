package it.gov.pagopa.payment.notices.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.payment.notices.service.model.institutions.UploadData;
import it.gov.pagopa.payment.notices.service.service.InstitutionsService;
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

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InstitutionsControllerTest {

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

}
