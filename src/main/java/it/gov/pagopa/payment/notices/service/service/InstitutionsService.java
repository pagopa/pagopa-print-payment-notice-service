package it.gov.pagopa.payment.notices.service.service;

import it.gov.pagopa.payment.notices.service.model.institutions.UploadData;

import java.io.File;

public interface InstitutionsService {
    void uploadInstitutionsData(UploadData institutionsData, File logo);

}
