package it.gov.pagopa.payment.notices.service.model.notice;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    @NotEmpty
    private String subject;

    private Long paymentAmount;

    private String dueDate;

    @NotNull
    @NotEmpty
    private String code;

    private List<Installments> installments;

}
