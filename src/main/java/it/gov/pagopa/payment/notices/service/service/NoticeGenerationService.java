package it.gov.pagopa.payment.notices.service.service;

import it.gov.pagopa.payment.notices.service.model.GetGenerationRequestStatusResource;
import it.gov.pagopa.payment.notices.service.model.GetSignedUrlResource;
import it.gov.pagopa.payment.notices.service.model.NoticeGenerationMassiveRequest;
import it.gov.pagopa.payment.notices.service.model.NoticeGenerationRequestItem;

import java.io.File;

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

    /**
     * Kickstarts and returns the notice using the services provided by the generator API,
     * if a folderId is provided it will return an exception if the folder is non-existent, or not
     * available for the user
     *
     * @param noticeGenerationRequestItem item to use containing request data
     * @param folderId                    folderId to use for saving the produced notice (optional)
     * @param userId                      userId used for security checks if a folderId is provided
     * @return generated notice
     */
    File generateNotice(NoticeGenerationRequestItem noticeGenerationRequestItem, String folderId, String userId);

    /**
     * Return a file signed url
     * @param folderId folder id to use for file retrieval
     * @param fileId id of the file to retrieve
     * @param userId user id to use for permission check
     * @return instance of GetSignedUrlResource containing a signed url
     */
    GetSignedUrlResource getFileSignedUrl(String folderId, String fileId, String userId);

    /**
     * Delete a folder using the provided folderId, if allowed, or throw exception if
     * missing or not allowed
     * @param folderId folderId to use for deletioj
     * @param userId user id to use for permission check
     */
    void deleteFolder(String folderId, String userId);

    /**
     * Return a folder signed url
     * @param folderId folder id to use for folder retrieval
     * @param userId user id to use for permission check
     * @return instance of GetSignedUrlResource containing a signed url
     */
    GetSignedUrlResource getFolderSignedUrl(String folderId, String userId);
}
