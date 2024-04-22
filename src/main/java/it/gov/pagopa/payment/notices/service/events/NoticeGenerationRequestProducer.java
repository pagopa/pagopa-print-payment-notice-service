package it.gov.pagopa.payment.notices.service.events;

import it.gov.pagopa.payment.notices.service.model.NoticeGenerationRequestEH;

/**
 * Interface to use when required to execute sending of a notice generation request through
 * the eventhub channel
 */
public interface NoticeGenerationRequestProducer {

    /**
     * Send notige generation request through EH
     * @param noticeGenerationRequestEH data to send
     * @return boolean referring if the insertion on the sending channel was successfully
     */
    boolean noticeGeneration(NoticeGenerationRequestEH noticeGenerationRequestEH);

}
