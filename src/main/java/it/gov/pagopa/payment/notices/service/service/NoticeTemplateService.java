package it.gov.pagopa.payment.notices.service.service;

import org.springframework.core.io.ByteArrayResource;

public interface NoticeTemplateService {

    ByteArrayResource getTemplate(String templateId);

}
