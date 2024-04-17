package it.gov.pagopa.payment.notices.service.service;

import java.io.File;

/**
 * Service interface for template management
 */
public interface NoticeTemplateService {

    /**
     * Recover the template file referring to the provided templateId
     * @param templateId id to be used to retrieve the template
     * @return template zipped file
     */
    File getTemplate(String templateId);

}
