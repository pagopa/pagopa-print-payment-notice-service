package it.gov.pagopa.payment.notices.service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import it.gov.pagopa.payment.notices.service.model.notice.NoticeRequestData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoticeGenerationRequestItem {

    @Schema(description = "The template identifier. Use the appropriate GET service to retrieve the list of the ids", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String templateId;

    @Schema(description = "The data used to fill the template", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotNull
    private NoticeRequestData data;

}
