package it.gov.pagopa.payment.notices.service.service.impl;

import it.gov.pagopa.payment.notices.service.exception.AppError;
import it.gov.pagopa.payment.notices.service.exception.AppException;
import it.gov.pagopa.payment.notices.service.model.institutions.UploadData;
import it.gov.pagopa.payment.notices.service.service.InstitutionsService;
import it.gov.pagopa.payment.notices.service.storage.InstitutionsStorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;

/**
 * @see it.gov.pagopa.payment.notices.service.service.InstitutionsService
 */
@Service
@Slf4j
public class InstitutionsServiceImpl implements InstitutionsService {

    private final InstitutionsStorageClient institutionsStorageClient;

    public InstitutionsServiceImpl(InstitutionsStorageClient institutionsStorageClient) {
        this.institutionsStorageClient = institutionsStorageClient;
    }

    @Override
    public void uploadInstitutionsData(UploadData institutionsData, File logo) {
        try (BufferedInputStream fis = new BufferedInputStream(new FileInputStream(logo))) {
            boolean result = institutionsStorageClient.saveInstitutionsData(
                    institutionsData.getTaxCode(),institutionsData,fis);
            if (!result) {
                throw new AppException(AppError.INSTITUTION_DATA_UPLOAD_ERROR);
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException(AppError.INSTITUTION_DATA_UPLOAD_ERROR, e);
        }
    }

    @Override
    public UploadData getInstitutionData(String taxCode) {
        try {
            return institutionsStorageClient.getInstitutionData(taxCode);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(AppError.INSTITUTION_RETRIEVE_ERROR);
        }
    }
}
