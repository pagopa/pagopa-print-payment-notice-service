package it.gov.pagopa.payment.notices.service.service.impl;

import it.gov.pagopa.payment.notices.service.entity.PaymentNoticeGenerationRequest;
import it.gov.pagopa.payment.notices.service.entity.PaymentNoticeGenerationRequestError;
import it.gov.pagopa.payment.notices.service.exception.AppException;
import it.gov.pagopa.payment.notices.service.model.GetGenerationRequestStatusResource;
import it.gov.pagopa.payment.notices.service.model.enums.PaymentGenerationRequestStatus;
import it.gov.pagopa.payment.notices.service.repository.PaymentGenerationRequestErrorRepository;
import it.gov.pagopa.payment.notices.service.repository.PaymentGenerationRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoticeGenerationServiceImplTest {

    @Mock
    private PaymentGenerationRequestRepository paymentGenerationRequestRepository;

    @Mock
    private PaymentGenerationRequestErrorRepository paymentGenerationRequestErrorRepository;

    private NoticeGenerationServiceImpl noticeGenerationService;

    @BeforeEach
    public void init() {
        Mockito.reset(paymentGenerationRequestErrorRepository, paymentGenerationRequestRepository);
        noticeGenerationService = new NoticeGenerationServiceImpl(
                paymentGenerationRequestRepository, paymentGenerationRequestErrorRepository);
    }

    @Test
    void getFolderStatusShouldReturnResourceWhenOk() {
        when(paymentGenerationRequestRepository.findAllowedPaymentNoticeRequest(any(),any()))
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
                assertDoesNotThrow(() -> noticeGenerationService.getFolderStatus("folderId","userId"));
        assertNotNull(getGenerationRequestStatusResource);
        assertEquals(PaymentGenerationRequestStatus.INSERTED,getGenerationRequestStatusResource.getStatus());
        assertEquals(0, getGenerationRequestStatusResource.getProcessedNotices().size());
        assertEquals(1, getGenerationRequestStatusResource.getNoticesInError().size());
        verify(paymentGenerationRequestRepository).findAllowedPaymentNoticeRequest(any(),any());
        verify(paymentGenerationRequestErrorRepository).findErrors(any());
    }

    @Test
    void getFolderStatusShouldReturnNotFoundWhenMissingFolder() {
        when(paymentGenerationRequestRepository.findAllowedPaymentNoticeRequest(any(),any()))
                .thenReturn(
                        Optional.empty()
                );
        assertThrows(AppException.class, () ->
                noticeGenerationService.getFolderStatus("folderId","userId"));
        verify(paymentGenerationRequestRepository).findAllowedPaymentNoticeRequest(any(),any());
        verifyNoInteractions(paymentGenerationRequestErrorRepository);
    }

    @Test
    void getFolderStatusShouldReturnExceptionWhenUnexpectedErrorOnRepository() {
        when(paymentGenerationRequestRepository.findAllowedPaymentNoticeRequest(any(),any()))
                .thenAnswer(item -> {
                    throw new RuntimeException();
                });
        assertThrows(RuntimeException.class, () ->
                noticeGenerationService.getFolderStatus("folderId","userId"));
        verify(paymentGenerationRequestRepository).findAllowedPaymentNoticeRequest(any(),any());
        verifyNoInteractions(paymentGenerationRequestErrorRepository);
    }

    @Test
    void getFolderStatusShouldReturnExceptionWhenUnexpectedExceptionOnErrorRepository() {
        when(paymentGenerationRequestRepository.findAllowedPaymentNoticeRequest(any(),any()))
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
                noticeGenerationService.getFolderStatus("folderId","userId"));
        verify(paymentGenerationRequestRepository).findAllowedPaymentNoticeRequest(any(),any());
        verify(paymentGenerationRequestErrorRepository).findErrors(any());
    }

}
