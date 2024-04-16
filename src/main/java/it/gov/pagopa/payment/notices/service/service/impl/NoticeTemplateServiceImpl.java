package it.gov.pagopa.payment.notices.service.service.impl;

import it.gov.pagopa.payment.notices.service.service.NoticeTemplateService;
import it.gov.pagopa.payment.notices.service.storage.NoticeTemplateStorageClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;

public class NoticeTemplateServiceImpl implements NoticeTemplateService {

    @Autowired
    private NoticeTemplateStorageClient noticeTemplateStorageClient;

    @Override
    public ByteArrayResource getTemplate(String templateId) {
        return noticeTemplateStorageClient.getTemplate(templateId);
    }

}
