package it.gov.pagopa.payment.notices.service.client;

import feign.Response;
import it.gov.pagopa.payment.notices.service.config.feign.NoticeFeignConfig;
import it.gov.pagopa.payment.notices.service.model.NoticeGenerationRequestItem;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client instance to be used to contact notices-generation APIs
 */
@FeignClient(name = "notice-generation", url = "${rest-client.notice-generation.base-url}", configuration = NoticeFeignConfig.class)
public interface NoticeGenerationClient {

    /**
     * Generate a single PDF Payment Notice
     *
     * @param folderId                    folderId to use for saving the produced pdf (optional)
     * @param noticeGenerationRequestItem generation data, containing a templateId and notice data
     * @return generated pdf
     */
    @PostMapping(value = "/notices/generate", produces = MediaType.APPLICATION_JSON_VALUE)
    Response generateNotice(
            @RequestParam(value = "folderId", required = false) String folderId,
            @RequestBody NoticeGenerationRequestItem noticeGenerationRequestItem
    );

}
