package it.gov.pagopa.payment.notices.service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateResource {

    @Schema(description = "templateId, to use when requiring generation of a notice with the selected template", requiredMode = Schema.RequiredMode.REQUIRED)
    private String templateId;
    @Schema(description = "Template description", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;
    @Schema(description = "Template example url", requiredMode = Schema.RequiredMode.REQUIRED)
    private String templateExampleUrl;

}
