package it.gov.pagopa.payment.notices.service.service.async;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.payment.notices.service.entity.PaymentNoticeGenerationRequestError;
import it.gov.pagopa.payment.notices.service.events.NoticeGenerationRequestProducer;
import it.gov.pagopa.payment.notices.service.exception.Aes256Exception;
import it.gov.pagopa.payment.notices.service.model.NoticeGenerationMassiveRequest;
import it.gov.pagopa.payment.notices.service.model.NoticeGenerationRequestEH;
import it.gov.pagopa.payment.notices.service.model.NoticeGenerationRequestItem;
import it.gov.pagopa.payment.notices.service.repository.PaymentGenerationRequestErrorRepository;
import it.gov.pagopa.payment.notices.service.repository.PaymentGenerationRequestRepository;
import it.gov.pagopa.payment.notices.service.util.Aes256Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static it.gov.pagopa.payment.notices.service.util.CommonUtility.sanitizeLogParam;

@Service
@Slf4j
public class AsyncService {

    private final NoticeGenerationRequestProducer noticeGenerationRequestProducer;
    private final PaymentGenerationRequestErrorRepository paymentGenerationRequestErrorRepository;
    private final PaymentGenerationRequestRepository paymentGenerationRequestRepository;
    private final ObjectMapper objectMapper;
    private final Aes256Utils aes256Utils;

    public AsyncService(NoticeGenerationRequestProducer noticeGenerationRequestProducer,
                        PaymentGenerationRequestRepository paymentGenerationRequestRepository,
                        PaymentGenerationRequestErrorRepository paymentGenerationRequestErrorRepository,
                        ObjectMapper objectMapper, Aes256Utils aes256Utils) {
        this.noticeGenerationRequestProducer = noticeGenerationRequestProducer;
        this.paymentGenerationRequestErrorRepository = paymentGenerationRequestErrorRepository;
        this.paymentGenerationRequestRepository = paymentGenerationRequestRepository;
        this.objectMapper = objectMapper;
        this.aes256Utils = aes256Utils;
    }

    @Async
    public void sendNotices(NoticeGenerationMassiveRequest noticeGenerationMassiveRequest, String folderId) {
        noticeGenerationMassiveRequest.getNotices().parallelStream().forEach(noticeGenerationRequestItem -> {
            try {
                if(!noticeGenerationRequestProducer.noticeGeneration(
                        NoticeGenerationRequestEH.builder()
                                .noticeData(noticeGenerationRequestItem)
                                .folderId(folderId)
                                .build())
                ) {
                    saveErrorEvent(folderId, noticeGenerationRequestItem);
                }
            } catch (Exception e) {
                saveErrorEvent(folderId, noticeGenerationRequestItem);
            }
        });
    }

    private void saveErrorEvent(String folderId, NoticeGenerationRequestItem noticeGenerationRequestItem) {
        try {
            paymentGenerationRequestErrorRepository.save(
                    PaymentNoticeGenerationRequestError.builder()
                            .errorDescription("Encountered error sending notice on EH")
                            .folderId(folderId)
                            .data(aes256Utils.encrypt(objectMapper
                                    .writeValueAsString(noticeGenerationRequestItem)))
                            .createdAt(Instant.now())
                            .numberOfAttempts(0)
                            .build()
            );
            paymentGenerationRequestRepository.findAndIncrementNumberOfElementsFailedById(folderId);
        } catch (JsonProcessingException | Aes256Exception e) {
            log.error("Unable to save notice data into error repository for notice with folder {} and noticeId {}",
                    folderId,
                    sanitizeLogParam(noticeGenerationRequestItem.getData().getNotice().getCode())
            );
        }
    }

}
