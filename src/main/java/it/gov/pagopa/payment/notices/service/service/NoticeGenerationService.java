package it.gov.pagopa.payment.notices.service.service;

import it.gov.pagopa.payment.notices.service.model.GetGenerationRequestStatusResource;

/**
 * Service interface for notice generation
 */
public interface NoticeGenerationService {

    /**
     * Retrieve generation request folder status, including error and processed items. The folder
     * status will be returned only if the userId matches the allowed pairing with the folderId used
     * as input
     * @param folderId folderId to retrieve generation status
     * @param userId userId contained in the X-User-Id to be used for data retrieval
     * @return instance of GetGenerationRequestStatusResource containing a folder status
     */
    GetGenerationRequestStatusResource getFolderStatus(String folderId, String userId);

}
