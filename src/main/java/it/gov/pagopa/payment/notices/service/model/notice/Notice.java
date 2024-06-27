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

    @Schema(description = "Notice total amount to pay", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Max(value = 99999999999L)
    private Long paymentAmount;

    @Schema(description = "Notice reduced amount to pay (used for example in reduced amount for infractions)")
    @Max(value = 99999999999L)
    private Long reducedAmount;

    @Schema(description = "Notice discounted amount to pay (used for example in discounted amount for infractions)")
    @Max(value = 99999999999L)
    private Long discountedAmount;

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
