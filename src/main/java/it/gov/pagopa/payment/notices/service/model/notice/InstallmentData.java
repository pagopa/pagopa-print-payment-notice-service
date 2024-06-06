package it.gov.pagopa.payment.notices.service.model.notice;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InstallmentData {

    @Schema(description = "Installment code", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Size(min = 18, max = 18)
    private String code;

    @Schema(description = "Installment amount", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long amount;

    @Schema(description = "Installment dueDate", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String dueDate;

}
