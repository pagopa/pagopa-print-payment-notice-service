package it.gov.pagopa.payment.notices.service.service;

import it.gov.pagopa.payment.notices.service.model.TemplateResource;

import java.io.File;
import java.util.List;

/**
 * Service interface for template management
 */
public interface NoticeTemplateService {

    /**
     * Retrieve list of available templates
     * @return list of templates available for notice generation
     */
    List<TemplateResource> getTemplates();

    /**
     * Recover the template file referring to the provided templateId
     * @param templateId id to be used to retrieve the template
     * @return template zipped file
     */
    File getTemplate(String templateId);

}
