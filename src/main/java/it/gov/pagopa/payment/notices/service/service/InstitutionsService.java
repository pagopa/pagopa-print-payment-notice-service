package it.gov.pagopa.payment.notices.service.service;

import it.gov.pagopa.payment.notices.service.model.institutions.UploadData;

import java.io.File;

/**
 * Includes methods to manage institutions data from the related storage
 */
public interface InstitutionsService {

    /**
     * Uploads institutions data to the related storage, using the taxCode provided within
     * the UploadData instance, if the institution is already on the storage, the content
     * will be updated. The institution json data will include the link on the uploaded logo
     *
     * @param institutionsData institution data to upload
     * @param logo             institution logo to upload
     * @throws it.gov.pagopa.payment.notices.service.exception.AppException
     */
    void uploadInstitutionsData(UploadData institutionsData, File logo);

}
