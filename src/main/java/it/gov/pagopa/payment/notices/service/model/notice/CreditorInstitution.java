package it.gov.pagopa.payment.notices.service.model.notice;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreditorInstitution {

    @Schema(description = "CI tax code", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @NotEmpty
    private String taxCode;

}
