package it.gov.pagopa.payment.notices.service.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import it.gov.pagopa.payment.notices.service.client.NoticeGenerationClient;
import it.gov.pagopa.payment.notices.service.entity.PaymentNoticeGenerationRequest;
import it.gov.pagopa.payment.notices.service.entity.PaymentNoticeGenerationRequestError;
import it.gov.pagopa.payment.notices.service.events.NoticeGenerationRequestProducer;
import it.gov.pagopa.payment.notices.service.exception.Aes256Exception;
import it.gov.pagopa.payment.notices.service.exception.AppError;
import it.gov.pagopa.payment.notices.service.exception.AppException;
import it.gov.pagopa.payment.notices.service.model.GetGenerationRequestStatusResource;
import it.gov.pagopa.payment.notices.service.model.GetSignedUrlResource;
import it.gov.pagopa.payment.notices.service.model.NoticeGenerationMassiveRequest;
import it.gov.pagopa.payment.notices.service.model.NoticeGenerationRequestItem;
import it.gov.pagopa.payment.notices.service.model.enums.PaymentGenerationRequestStatus;
import it.gov.pagopa.payment.notices.service.model.notice.Notice;
import it.gov.pagopa.payment.notices.service.model.notice.NoticeRequestData;
import it.gov.pagopa.payment.notices.service.repository.PaymentGenerationRequestErrorRepository;
import it.gov.pagopa.payment.notices.service.repository.PaymentGenerationRequestRepository;
import it.gov.pagopa.payment.notices.service.storage.NoticeStorageClient;
import it.gov.pagopa.payment.notices.service.service.AsyncService;
import it.gov.pagopa.payment.notices.service.util.Aes256Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {NoticeGenerationServiceImpl.class, AsyncService.class})
class NoticeGenerationServiceImplTest {

    @MockBean
    NoticeGenerationClient noticeGenerationClient;
    @MockBean
    private PaymentGenerationRequestRepository paymentGenerationRequestRepository;
    @MockBean
    private PaymentGenerationRequestErrorRepository paymentGenerationRequestErrorRepository;
    @MockBean
    private NoticeGenerationRequestProducer noticeGenerationRequestProducer;
    @SpyBean
    private ObjectMapper objectMapper;
    @SpyBean
    private Aes256Utils aes256Utils;

    @MockBean
    NoticeStorageClient noticeStorageClient;

    @Autowired
    @InjectMocks
    private AsyncService asyncService;

    private NoticeGenerationServiceImpl noticeGenerationService;

    @BeforeEach
    public void init() {
        Mockito.reset(
                paymentGenerationRequestErrorRepository,
                paymentGenerationRequestRepository, noticeGenerationRequestProducer);
        noticeGenerationService = new NoticeGenerationServiceImpl(
                paymentGenerationRequestRepository,
                paymentGenerationRequestErrorRepository,
                asyncService, noticeGenerationClient
                , noticeStorageClient);
    }

    @Test
    void getFolderStatusShouldReturnResourceWhenOk() {
        when(paymentGenerationRequestRepository.findByIdAndUserId(any(), any()))
                .thenReturn(
                        Optional.of(
                                PaymentNoticeGenerationRequest.builder()
                                        .status(PaymentGenerationRequestStatus.INSERTED)
                                        .items(Collections.emptyList())
                                        .build()
                        )
                );
        when(paymentGenerationRequestErrorRepository.findErrors(any()))
                .thenReturn(
                        Collections.singletonList(
                                PaymentNoticeGenerationRequestError.builder()
                                        .id("errorId")
                                        .build()
                        )
                );
        GetGenerationRequestStatusResource getGenerationRequestStatusResource =
                assertDoesNotThrow(() -> noticeGenerationService.getFolderStatus("folderId", "userId"));
        assertNotNull(getGenerationRequestStatusResource);
        assertEquals(PaymentGenerationRequestStatus.INSERTED, getGenerationRequestStatusResource.getStatus());
        assertEquals(0, getGenerationRequestStatusResource.getProcessedNotices().size());
        assertEquals(1, getGenerationRequestStatusResource.getNoticesInError().size());
        verify(paymentGenerationRequestRepository).findByIdAndUserId(any(), any());
        verify(paymentGenerationRequestErrorRepository).findErrors(any());
    }

    @Test
    void getFolderStatusShouldReturnNotFoundWhenMissingFolder() {
        when(paymentGenerationRequestRepository.findByIdAndUserId(any(), any()))
                .thenReturn(
                        Optional.empty()
                );
        assertThrows(AppException.class, () ->
                noticeGenerationService.getFolderStatus("folderId", "userId"));
        verify(paymentGenerationRequestRepository).findByIdAndUserId(any(), any());
        verifyNoInteractions(paymentGenerationRequestErrorRepository);
    }

    @Test
    void getFolderStatusShouldReturnExceptionWhenUnexpectedErrorOnRepository() {
        when(paymentGenerationRequestRepository.findByIdAndUserId(any(), any()))
                .thenAnswer(item -> {
                    throw new RuntimeException();
                });
        assertThrows(RuntimeException.class, () ->
                noticeGenerationService.getFolderStatus("folderId", "userId"));
        verify(paymentGenerationRequestRepository).findByIdAndUserId(any(), any());
        verifyNoInteractions(paymentGenerationRequestErrorRepository);
    }

    @Test
    void getFolderStatusShouldReturnExceptionWhenUnexpectedExceptionOnErrorRepository() {
        when(paymentGenerationRequestRepository.findByIdAndUserId(any(), any()))
                .thenReturn(
                        Optional.of(
                                PaymentNoticeGenerationRequest.builder()
                                        .status(PaymentGenerationRequestStatus.INSERTED)
                                        .items(Collections.emptyList())
                                        .build()
                        )
                );
        when(paymentGenerationRequestErrorRepository.findErrors(any()))
                .thenAnswer(item -> {
                    throw new RuntimeException();
                });
        assertThrows(RuntimeException.class, () ->
                noticeGenerationService.getFolderStatus("folderId", "userId"));
        verify(paymentGenerationRequestRepository).findByIdAndUserId(any(), any());
        verify(paymentGenerationRequestErrorRepository).findErrors(any());
    }

    @Test
    void generateMassiveRequestShouldSendEventOnSuccess() {
        NoticeGenerationRequestItem noticeGenerationRequestItem = NoticeGenerationRequestItem.builder()
                .templateId("testTemplate")
                .data(NoticeRequestData.builder().notice(
                                Notice.builder().code("testCode")
                                        .build()
                        ).build()
                ).build();
        NoticeGenerationMassiveRequest noticeGenerationMassiveRequest =
                NoticeGenerationMassiveRequest.builder().notices(
                        Collections.singletonList(noticeGenerationRequestItem)).build();
        when(paymentGenerationRequestRepository.save(any())).thenReturn(
                PaymentNoticeGenerationRequest.builder().id("testFolderId").build());
        when(noticeGenerationRequestProducer.noticeGeneration(any())).thenReturn(true);
        String folderId = noticeGenerationService.generateMassive(noticeGenerationMassiveRequest, "testUserId");
        assertNotNull(folderId);
        assertEquals("testFolderId", folderId);
        verify(paymentGenerationRequestRepository).save(any());
        verify(noticeGenerationRequestProducer).noticeGeneration(any());
        verifyNoInteractions(paymentGenerationRequestErrorRepository);
    }

    @Test
    void generateMassiveRequestShouldSaveErrorEventOnSendFailure() throws Aes256Exception {
        NoticeGenerationRequestItem noticeGenerationRequestItem = NoticeGenerationRequestItem.builder()
                .templateId("testTemplate")
                .data(NoticeRequestData.builder().notice(
                                Notice.builder().code("testCode")
                                        .build()
                        ).build()
                ).build();
        NoticeGenerationMassiveRequest noticeGenerationMassiveRequest =
                NoticeGenerationMassiveRequest.builder().notices(
                        Collections.singletonList(noticeGenerationRequestItem)).build();
        when(paymentGenerationRequestRepository.save(any())).thenReturn(
                PaymentNoticeGenerationRequest.builder().id("testFolderId").build());
        when(noticeGenerationRequestProducer.noticeGeneration(any())).thenReturn(false);
        String folderId = noticeGenerationService.generateMassive(noticeGenerationMassiveRequest, "testUserId");
        assertNotNull(folderId);
        assertEquals("testFolderId", folderId);
        verify(paymentGenerationRequestRepository).save(any());
        verify(noticeGenerationRequestProducer).noticeGeneration(any());
        verify(paymentGenerationRequestErrorRepository).save(any());
    }

    @Test
    void shouldReturnDataOnValidNoticeGenerationRequest() throws IOException {
        when(noticeGenerationClient.generateNotice(any(), any()))
                .thenReturn(Response.builder().status(200)
                        .request(Request.create(
                                Request.HttpMethod.GET, "test", new HashMap<>(),
                                "".getBytes(), Charset.defaultCharset(), null))
                        .body("".getBytes()).build());
        File returnFile = noticeGenerationService.generateNotice(NoticeGenerationRequestItem.builder().build(),
                null, null);
        assertNotNull(returnFile);
        verify(noticeGenerationClient).generateNotice(any(), any());
        verifyNoInteractions(paymentGenerationRequestRepository);
    }

    @Test
    void shouldReturnExceptionOnMissingFolderRequest() throws IOException {
        NoticeGenerationRequestItem generationRequestItem = NoticeGenerationRequestItem.builder().build();
        assertThrows(AppException.class, () -> noticeGenerationService.generateNotice(generationRequestItem, "test", "test"));
        verify(paymentGenerationRequestRepository).findByIdAndUserId(any(), any());
        verifyNoInteractions(noticeGenerationClient);
    }

    @Test
    void getFileSignedUrlSholdReturnDataOnValidRequest() {
        when(paymentGenerationRequestRepository.findByIdAndUserId(any(),any())).thenReturn(
                Optional.of(PaymentNoticeGenerationRequest.builder().build()));
        when(noticeStorageClient.getFileSignedUrl(any(),any())).thenReturn("signedUrl");
        GetSignedUrlResource resource = noticeGenerationService
                .getFileSignedUrl("test","test","test");
        verify(noticeStorageClient).getFileSignedUrl(any(),any());
        verify(paymentGenerationRequestRepository).findByIdAndUserId(any(),any());
        assertNotNull(resource);
        assertNotNull(resource.getSignedUrl());
    }

    @Test
    void getFileUrlShouldReturnNotFoundWhenMissingFolder() {
        when(paymentGenerationRequestRepository.findByIdAndUserId(any(),any()))
                .thenReturn(
                        Optional.empty()
                );
        assertThrows(AppException.class, () ->
                noticeGenerationService.getFileSignedUrl("test","folderId","userId"));
        verify(paymentGenerationRequestRepository).findByIdAndUserId(any(),any());
        verifyNoInteractions(noticeStorageClient);
    }

    @Test
    void getFileSignedUrlSholdReturnExceptionOnClientKo() {
        when(paymentGenerationRequestRepository.findByIdAndUserId(any(),any())).thenReturn(
                Optional.of(PaymentNoticeGenerationRequest.builder().build()));
        when(noticeStorageClient.getFileSignedUrl(any(),any())).thenThrow(
                new AppException(AppError.NOTICE_CLIENT_UNAVAILABLE));
        assertThrows(AppException.class, () -> noticeGenerationService
                .getFileSignedUrl("test","test","test"));
        verify(noticeStorageClient).getFileSignedUrl(any(),any());
        verify(paymentGenerationRequestRepository).findByIdAndUserId(any(),any());
    }

    @Test
    void deleteShouldReturnNotFoundWhenMissingFolder() {
        when(paymentGenerationRequestRepository.findByIdAndUserId(any(),any()))
                .thenReturn(
                        Optional.empty()
                );
        assertThrows(AppException.class, () ->
                noticeGenerationService.deleteFolder("test", "test"));
        verify(paymentGenerationRequestRepository).findByIdAndUserId(any(),any());
        verifyNoInteractions(noticeStorageClient);
    }

    @Test
    void deleteFolderShouldReturnOk() {
        when(paymentGenerationRequestRepository.findByIdAndUserId(any(), any()))
                .thenReturn(
                        Optional.of(
                                PaymentNoticeGenerationRequest.builder()
                                        .status(PaymentGenerationRequestStatus.INSERTED)
                                        .items(Collections.emptyList())
                                        .build()
                        )
                );
        noticeGenerationService.deleteFolder("test", "test");
        verify(paymentGenerationRequestRepository).findByIdAndUserId(any(),any());
        verify(paymentGenerationRequestRepository).deleteById(any());
        verify(noticeStorageClient).deleteFolder(any());
    }

    @Test
    void deleteFolderShouldReturnKoOnBlobException() {
        when(paymentGenerationRequestRepository.findByIdAndUserId(any(), any()))
                .thenReturn(
                        Optional.of(
                                PaymentNoticeGenerationRequest.builder()
                                        .status(PaymentGenerationRequestStatus.INSERTED)
                                        .items(Collections.emptyList())
                                        .build()
                        )
                );
        doThrow(new AppException(AppError.COULD_NOT_DELETE_FOLDER_ERROR))
                .when(noticeStorageClient).deleteFolder(any());
        assertThrows(AppException.class, () -> noticeGenerationService.deleteFolder("test", "test"));
        verify(paymentGenerationRequestRepository).findByIdAndUserId(any(),any());
        verify(paymentGenerationRequestRepository).deleteById(any());
        verify(noticeStorageClient).deleteFolder(any());
    }

    @Test
    void deleteFolderShouldReturnKoOnRepositoryException() {
        when(paymentGenerationRequestRepository.findByIdAndUserId(any(), any()))
                .thenReturn(
                        Optional.of(
                                PaymentNoticeGenerationRequest.builder()
                                        .status(PaymentGenerationRequestStatus.INSERTED)
                                        .items(Collections.emptyList())
                                        .build()
                        )
                );
        doThrow(new RuntimeException("test"))
                .when(paymentGenerationRequestRepository).deleteById(any());
        assertThrows(AppException.class, () -> noticeGenerationService.deleteFolder("test", "test"));
        verify(paymentGenerationRequestRepository).findByIdAndUserId(any(),any());
        verify(paymentGenerationRequestRepository).deleteById(any());
        verifyNoInteractions(noticeStorageClient);
    }

    @Test
    void getFolderSignedUrlShouldReturnDataOnSuccess() {
        when(paymentGenerationRequestRepository.findByIdAndUserId(any(),any())).thenReturn(
                Optional.of(PaymentNoticeGenerationRequest.builder()
                        .status(PaymentGenerationRequestStatus.PROCESSED).build()));
        when(noticeStorageClient.getFileSignedUrl(any(),any())).thenReturn("signedUrl");
        GetSignedUrlResource resource = noticeGenerationService
                .getFolderSignedUrl("test","test");
        verify(noticeStorageClient).getFileSignedUrl(any(),any());
        verify(paymentGenerationRequestRepository).findByIdAndUserId(any(),any());
        assertNotNull(resource);
        assertNotNull(resource.getSignedUrl());
    }

    @Test
    void getFolderUrlShouldReturnNotFoundWhenMissingFolder() {
        when(paymentGenerationRequestRepository.findByIdAndUserId(any(),any()))
                .thenReturn(
                        Optional.empty()
                );
        assertThrows(AppException.class, () ->
                noticeGenerationService.getFolderSignedUrl("test","userId"));
        verify(paymentGenerationRequestRepository).findByIdAndUserId(any(),any());
        verifyNoInteractions(noticeStorageClient);
    }

    @Test
    void getFolderSignedUrlSholdReturnExceptionOnClientKo() {
        when(paymentGenerationRequestRepository.findByIdAndUserId(any(),any())).thenReturn(
                Optional.of(PaymentNoticeGenerationRequest.builder()
                        .status(PaymentGenerationRequestStatus.PROCESSED).build()));
        when(noticeStorageClient.getFileSignedUrl(any(),any())).thenThrow(
                new AppException(AppError.NOTICE_CLIENT_UNAVAILABLE));
        assertThrows(AppException.class, () -> noticeGenerationService
                .getFolderSignedUrl("test","test"));
        verify(noticeStorageClient).getFileSignedUrl(any(),any());
        verify(paymentGenerationRequestRepository).findByIdAndUserId(any(),any());
    }

    @Test
    void getFolderUrlShouldReturnNotFoundWhenFolderYetToComplete() {
        when(paymentGenerationRequestRepository.findByIdAndUserId(any(),any()))
                .thenReturn(
                        Optional.of(PaymentNoticeGenerationRequest.builder()
                                .status(PaymentGenerationRequestStatus.PROCESSING).build())
                );
        assertThrows(AppException.class, () ->
                noticeGenerationService.getFolderSignedUrl("test","userId"));
        verify(paymentGenerationRequestRepository).findByIdAndUserId(any(),any());
        verifyNoInteractions(noticeStorageClient);
    }

}
