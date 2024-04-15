package it.gov.pagopa.payment.notices.service.service;

import it.gov.pagopa.payment.notices.service.model.GetGenerationRequestStatusResource;

public interface NoticeGenerationService {

    GetGenerationRequestStatusResource getFolderStatus(String folderId, String userId);

}
