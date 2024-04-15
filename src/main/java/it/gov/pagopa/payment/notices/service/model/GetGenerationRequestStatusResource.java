package it.gov.pagopa.payment.notices.service.model;

import it.gov.pagopa.payment.notices.service.model.enums.PaymentGenerationRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetGenerationRequestStatusResource implements Serializable {

    private PaymentGenerationRequestStatus status;
    private List<String> processedNotices;
    private List<String> noticesInError;

}
