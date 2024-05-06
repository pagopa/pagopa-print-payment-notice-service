package it.gov.pagopa.payment.notices.service.client;

import it.gov.pagopa.payment.notices.service.model.NoticeGenerationRequestItem;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import feign.Response;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "notice-generation", url = "${rest-client.notice-generation.base-url}")
public interface NoticeGenerationClient {

    @GetMapping(value = "/notices/generate", produces = MediaType.APPLICATION_JSON_VALUE)
    Response generateNotice(
            @RequestParam(value = "folderId", required = false) String folderId,
            @RequestBody NoticeGenerationRequestItem noticeGenerationRequestItem
    );

}
