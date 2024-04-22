package it.gov.pagopa.payment.notices.service.repository;

import it.gov.pagopa.payment.notices.service.entity.PaymentNoticeGenerationRequestError;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentGenerationRequestErrorRepository extends MongoRepository<PaymentNoticeGenerationRequestError, String> {

    @Query(value = "{ folderId : ?0}", fields = "{}")
    List<PaymentNoticeGenerationRequestError> findErrors(String folderId);

}
