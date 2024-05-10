package it.gov.pagopa.payment.notices.service.model.notice;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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

    @ToString.Exclude
    @Schema(description = "Debtor postal code", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @NotEmpty
    private String postalCode;

    @ToString.Exclude
    @Schema(description = "Debtor city", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @NotEmpty
    private String city;

    @ToString.Exclude
    @Schema(description = "Debtor building number", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @NotEmpty
    private String buildingNumber;

    @ToString.Exclude
    @Schema(description = "Debtor province", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @NotEmpty
    private String province;

}
