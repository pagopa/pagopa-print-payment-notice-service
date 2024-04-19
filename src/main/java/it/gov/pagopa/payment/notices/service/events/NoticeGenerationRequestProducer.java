package it.gov.pagopa.payment.notices.service.events;

import it.gov.pagopa.payment.notices.service.model.NoticeGenerationRequestEH;

public interface NoticeGenerationRequestProducer {
    boolean noticeGeneration(NoticeGenerationRequestEH noticeGenerationRequestEH);

}
