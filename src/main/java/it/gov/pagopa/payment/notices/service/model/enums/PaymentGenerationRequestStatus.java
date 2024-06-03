package it.gov.pagopa.payment.notices.service.model.enums;

/**
 * Enum containing generation request status
 */
public enum PaymentGenerationRequestStatus {

    INSERTED,
    PROCESSING,
    COMPLETING,
    FAILED,
    PROCESSED,
    PROCESSED_WITH_FAILURES

}
