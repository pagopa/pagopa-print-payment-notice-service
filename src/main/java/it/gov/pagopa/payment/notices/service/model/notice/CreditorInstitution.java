package it.gov.pagopa.payment.notices.service.model.notice;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class CreditorInstitution {

    @NotNull
    @NotEmpty
    private String taxCode;
    @NotNull
    @NotEmpty
    private String fullName;
    private String organization;
    @NotNull
    @NotEmpty
    private String info;
    @NotNull
    private Boolean webChannel;
    private String physicalChannel;
    @NotNull
    @NotEmpty
    private String cbill;


}
