package it.gov.pagopa.payment.notices.service.model.notice;

import lombok.Data;

import java.util.List;

@Data
public class Installments {

    private Integer number;
    private List<InstallmentData> data;

}
