package it.gov.pagopa.payment.notices.service.service.impl;

import it.gov.pagopa.payment.notices.service.entity.PaymentNoticeGenerationRequest;
import it.gov.pagopa.payment.notices.service.entity.PaymentNoticeGenerationRequestError;
import it.gov.pagopa.payment.notices.service.exception.AppError;
import it.gov.pagopa.payment.notices.service.exception.AppException;
import it.gov.pagopa.payment.notices.service.model.GetGenerationRequestStatusResource;
import it.gov.pagopa.payment.notices.service.repository.PaymentGenerationRequestErrorRepository;
import it.gov.pagopa.payment.notices.service.repository.PaymentGenerationRequestRepository;
import it.gov.pagopa.payment.notices.service.service.NoticeGenerationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NoticeGenerationServiceImpl implements NoticeGenerationService {

    private final PaymentGenerationRequestRepository paymentGenerationRequestRepository;
    private final PaymentGenerationRequestErrorRepository paymentGenerationRequestErrorRepository;

    public NoticeGenerationServiceImpl(
            PaymentGenerationRequestRepository paymentGenerationRequestRepository,
            PaymentGenerationRequestErrorRepository paymentGenerationRequestErrorRepository) {
        this.paymentGenerationRequestRepository = paymentGenerationRequestRepository;
        this.paymentGenerationRequestErrorRepository = paymentGenerationRequestErrorRepository;
    }

    @Override
    public GetGenerationRequestStatusResource getFolderStatus(String folderId, String userId) {
        Optional<PaymentNoticeGenerationRequest> paymentNoticeGenerationRequestOptional =
                paymentGenerationRequestRepository.findByIdAndUserId(folderId, userId);
        if (paymentNoticeGenerationRequestOptional.isEmpty()) {
            throw new AppException(AppError.FOLDER_NOT_AVAILABLE);
        }
        List<String> errors = paymentGenerationRequestErrorRepository.findErrors(folderId)
                .stream().map(PaymentNoticeGenerationRequestError::getId).toList();
        PaymentNoticeGenerationRequest paymentNoticeGenerationRequest = paymentNoticeGenerationRequestOptional.get();
        return GetGenerationRequestStatusResource
                .builder()
                .status(paymentNoticeGenerationRequest.getStatus())
                .processedNotices(paymentNoticeGenerationRequest.getItems())
                .noticesInError(errors)
                .build();
    }

}
