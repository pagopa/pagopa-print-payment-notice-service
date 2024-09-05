package it.gov.pagopa.payment.notices.service.repository;

import it.gov.pagopa.payment.notices.service.entity.PaymentNoticeGenerationRequestError;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentGenerationRequestErrorRepository extends MongoRepository<PaymentNoticeGenerationRequestError, String> {

    @Query(value = "{ folderId : ?0}", fields = "{}")
    List<PaymentNoticeGenerationRequestError> findErrors(String folderId);

    @Query(value = "{ folderId : ?0, errorId : ?1 }", fields = "{}")
    Optional<PaymentNoticeGenerationRequestError> findByFolderIdAndErrorId(String folderId, String errorId);

}
