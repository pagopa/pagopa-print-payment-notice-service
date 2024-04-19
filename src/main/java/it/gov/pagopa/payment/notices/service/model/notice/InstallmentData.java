package it.gov.pagopa.payment.notices.service.model.notice;

import lombok.Data;

@Data
public class InstallmentData {

    private Integer amount;
    private String dueDate;

}
