package it.gov.pagopa.payment.notices.service.model.notice;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.ToString;

public class Debtor {

    @ToString.Exclude
    @Schema(description = "Debtor taxCode")
    private String taxCode;
    @Schema(description = "Debtor full name", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @NotEmpty
    @ToString.Exclude
    private String fullName;
    @ToString.Exclude
    @Schema(description = "Debtor address", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @NotEmpty
    private String address;
}
