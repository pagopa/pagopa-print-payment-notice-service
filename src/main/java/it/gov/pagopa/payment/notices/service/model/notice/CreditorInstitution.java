package it.gov.pagopa.payment.notices.service.model.notice;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class CreditorInstitution {

    @Schema(description = "CI tax code", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @NotEmpty
    private String taxCode;

    @Schema(hidden = true, description = "CI full name", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @NotEmpty
    private String fullName;

    @Schema(hidden = true, description = "CI organization unit managing the payment ")
    private String organization;

    @Schema(hidden = true, description = "CI info", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @NotEmpty
    private String info;

    @Schema(hidden = true, description = "Boolean to refer if it has a web channel", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Boolean webChannel;

    @Schema(hidden = true, description = "CI physical channel data")
    private String physicalChannel;

    @Schema(hidden = true, description = "CI cbill", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @NotEmpty
    private String cbill;

    @Schema(hidden = true, description = "Poste account number")
    private String posteAccountNumber;

}
