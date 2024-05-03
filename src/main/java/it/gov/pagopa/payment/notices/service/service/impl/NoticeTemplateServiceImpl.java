package it.gov.pagopa.payment.notices.service.service.impl;

import it.gov.pagopa.payment.notices.service.model.TemplateResource;
import it.gov.pagopa.payment.notices.service.service.NoticeTemplateService;
import it.gov.pagopa.payment.notices.service.storage.NoticeTemplateStorageClient;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class NoticeTemplateServiceImpl implements NoticeTemplateService {

    private final NoticeTemplateStorageClient noticeTemplateStorageClient;

    public NoticeTemplateServiceImpl(NoticeTemplateStorageClient noticeTemplateStorageClient) {
        this.noticeTemplateStorageClient = noticeTemplateStorageClient;
    }

    @Override
    public List<TemplateResource> getTemplates() {
        return noticeTemplateStorageClient.getTemplates();
    }

    @Override
    public File getTemplate(String templateId) {
        return noticeTemplateStorageClient.getTemplate(templateId);
    }

}
