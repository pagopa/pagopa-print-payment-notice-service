package it.gov.pagopa.payment.notices.service.service.impl;

import it.gov.pagopa.payment.notices.service.service.NoticeTemplateService;
import it.gov.pagopa.payment.notices.service.storage.NoticeTemplateStorageClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

@Service
public class NoticeTemplateServiceImpl implements NoticeTemplateService {

    private final NoticeTemplateStorageClient noticeTemplateStorageClient;

    public NoticeTemplateServiceImpl(NoticeTemplateStorageClient noticeTemplateStorageClient) {
        this.noticeTemplateStorageClient = noticeTemplateStorageClient;
    }

    @Override
    public ByteArrayResource getTemplate(String templateId) {
        return noticeTemplateStorageClient.getTemplate(templateId);
    }

}
