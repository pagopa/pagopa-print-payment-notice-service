package it.gov.pagopa.payment.notices.service.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import it.gov.pagopa.payment.notices.service.model.GetGenerationRequestStatusResource;
import it.gov.pagopa.payment.notices.service.service.NoticeGenerationService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/notices", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@Tag(name = "Notice Generation Request APIs")
public class GenerationRequestController {
    private final NoticeGenerationService noticeGenerationService;

    public GenerationRequestController(NoticeGenerationService noticeGenerationService) {
        this.noticeGenerationService = noticeGenerationService;
    }

    @GetMapping("/folder/{folder_id}/status")
    public GetGenerationRequestStatusResource getFolderStatus(
            @PathVariable("folder_id") String folderId,
            @RequestHeader("X-User-Id") String userId) {
        return noticeGenerationService.getFolderStatus(folderId, userId);
    }

}
