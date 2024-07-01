package it.gov.pagopa.payment.notices.service.repository;

import it.gov.pagopa.payment.notices.service.entity.PaymentNoticeGenerationRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentGenerationRequestRepository extends MongoRepository<PaymentNoticeGenerationRequest, String> {
    Optional<PaymentNoticeGenerationRequest> findByIdAndUserId(String folderId, String userId);

    @Update("{ '$inc' : { 'numberOfElementsFailed' : 1 } }")
    long findAndIncrementNumberOfElementsFailedById(String folderId);

    Optional<PaymentNoticeGenerationRequest> findByIdempotencyKeyAndUserId(String idempotencyKey, String userId);
}
