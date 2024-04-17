package it.gov.pagopa.payment.notices.service.service.impl;

import it.gov.pagopa.payment.notices.service.service.NoticeTemplateService;
import it.gov.pagopa.payment.notices.service.storage.NoticeTemplateStorageClient;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class NoticeTemplateServiceImpl implements NoticeTemplateService {

    private final NoticeTemplateStorageClient noticeTemplateStorageClient;

    public NoticeTemplateServiceImpl(NoticeTemplateStorageClient noticeTemplateStorageClient) {
        this.noticeTemplateStorageClient = noticeTemplateStorageClient;
    }

    @Override
    public File getTemplate(String templateId) {
        return noticeTemplateStorageClient.getTemplate(templateId);
    }

}
