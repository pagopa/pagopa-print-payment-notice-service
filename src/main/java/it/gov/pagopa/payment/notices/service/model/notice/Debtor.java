package it.gov.pagopa.payment.notices.service.model.notice;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class Debtor {

    private String taxCode;
    @NotNull
    @NotEmpty
    private String fullName;
    @NotNull
    @NotEmpty
    private String address;
}
