package it.gov.pagopa.payment.notices.service.service;

import java.io.File;

public interface NoticeTemplateService {

    File getTemplate(String templateId);

}
