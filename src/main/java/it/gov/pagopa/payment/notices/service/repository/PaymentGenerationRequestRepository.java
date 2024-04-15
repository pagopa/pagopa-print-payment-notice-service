package it.gov.pagopa.payment.notices.service.repository;

import it.gov.pagopa.payment.notices.service.entity.PaymentNoticeGenerationRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentGenerationRequestRepository extends MongoRepository<PaymentNoticeGenerationRequest, String> {

    @Query(value = "{ id : ?0, userId: ?1 }", fields = "{}")
    Optional<PaymentNoticeGenerationRequest> findAllowedPaymentNoticeRequest(String folderId, String userId);

}
