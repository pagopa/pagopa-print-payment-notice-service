package it.gov.pagopa.payment.notices.service.service;

import it.gov.pagopa.payment.notices.service.model.GetGenerationRequestStatusResource;
import it.gov.pagopa.payment.notices.service.model.NoticeGenerationMassiveRequest;

/**
 * Service interface for notice generation
 */
public interface NoticeGenerationService {

    /**
     * Retrieve generation request folder status, including error and processed items. The folder
     * status will be returned only if the userId matches the allowed pairing with the folderId used
     * as input
     *
     * @param folderId folderId to retrieve generation status
     * @param userId   userId contained in the X-User-Id to be used for data retrieval
     * @return instance of GetGenerationRequestStatusResource containing a folder status
     */
    GetGenerationRequestStatusResource getFolderStatus(String folderId, String userId);

    /**
     * Generate the request of massive notice generation using input data, sending data through the EH channel
     * to kickstart notice generation in an async manner. Any error will be saved into notice generation request
     * error
     *
     * @param noticeGenerationMassiveRequest generation request data, containing a list of notice data and templates
     *                                       to use
     * @param userId                         userId requiring the generation. the request will refer to this user when recovery of data regarding
     *                                       the folder is executed
     * @return folderId produced when inserting the request
     */
    String generateMassive(NoticeGenerationMassiveRequest noticeGenerationMassiveRequest, String userId);
}
