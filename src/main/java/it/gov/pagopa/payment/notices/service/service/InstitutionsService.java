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
     * @throws it.gov.pagopa.payment.notices.service.exception.AppException
     * @param institutionsData institution data to upload
     * @param logo institution logo to upload
     */
    void uploadInstitutionsData(UploadData institutionsData, File logo);

    /**
     * Retrieving institution data, related to the provided taxCode
     * @throws it.gov.pagopa.payment.notices.service.exception.AppException
     * @param taxCode institution data to be used retrieval
     * @return institution data
     */
    UploadData getInstitutionData(String taxCode);
}
