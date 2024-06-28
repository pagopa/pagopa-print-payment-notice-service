package it.gov.pagopa.payment.notices.service.model.notice;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notice {

    @Schema(description = "Notice subject", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @NotEmpty
    @Size(max = 90)
    private String subject;

    @Schema(description = "Notice total amount to pay", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Max(value = 99999999999L)
    private Long paymentAmount;

    @Schema(description = "Notice reduced amount to pay (used in reduced amount for infractions). " +
            " Mandatory for CDS infraction notices")
    private InstallmentData reduced;

    @Schema(description = "Notice discounted amount to pay (used in discounted amount for infractions)." +
            " Mandatory for CDS infraction notices")
    private InstallmentData discounted;

    @Schema(description = "Notice due date", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String dueDate;

    @Schema(description = "Notice code, mandatory whenever the template contains a general payment",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(min = 18, max = 18)
    private String code;

    @Schema(description = "Notice Installments")
    @Size(min = 2)
    private List<@Valid InstallmentData> installments;

}
