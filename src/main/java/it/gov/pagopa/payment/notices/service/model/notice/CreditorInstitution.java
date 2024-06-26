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

    @Schema(hidden = true, description = "CI full name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fullName;

    @Schema(hidden = true, description = "CI organization unit managing the payment ")
    private String organization;

    @Schema(hidden = true, description = "CI info", requiredMode = Schema.RequiredMode.REQUIRED)
    private String info;

    @Schema(hidden = true, description = "Boolean to refer if it has a web channel", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean webChannel;

    @Schema(hidden = true, description = "Boolean to refer if it has an app channel", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean appChannel;

    @Schema(hidden = true, description = "CI physical channel data")
    private String physicalChannel;

    @Schema(hidden = true, description = "CI cbill", requiredMode = Schema.RequiredMode.REQUIRED)
    private String cbill;

    @Schema(hidden = true, description = "Poste name")
    private String posteName;

    @Schema(hidden = true, description = "Poste account number")
    private String posteAccountNumber;

    @Schema(hidden = true, description = "Poste auth code")
    private String posteAuth;

}
