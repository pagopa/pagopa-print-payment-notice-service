package it.gov.pagopa.payment.notices.service.model.notice;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoticeRequestData {

    @Schema(description = "Notice data", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Valid
    private Notice notice;

    @Schema(description = "Creditor Institution data", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Valid
    private CreditorInstitution creditorInstitution;

    @Schema(description = "Debtor data", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Valid
    @ToString.Exclude
    private Debtor debtor;

}
