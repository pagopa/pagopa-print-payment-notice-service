package it.gov.pagopa.payment.notices.service.model.institutions;

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
public class UploadData {

    @Schema(description = "CI tax code", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @NotEmpty
    private String taxCode;

    @Schema(description = "CI full name", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @NotEmpty
    private String fullName;

    @Schema(description = "CI organization unit managing the payment ")
    private String organization;

    @Schema(description = "CI info", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @NotEmpty
    private String info;

    @Schema(description = "Boolean to refer if it has a web channel", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Boolean webChannel;

    @Schema(description = "CI physical channel data")
    private String physicalChannel;

    @Schema(description = "CI cbill", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @NotEmpty
    private String cbill;

    @Schema(description = "Poste account number")
    private String posteAccountNumber;

    @Schema(description = "Poste auth code")
    private String posteAuth;

    @Schema(hidden = true)
    private String logo;

}
