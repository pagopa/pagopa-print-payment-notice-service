package it.gov.pagopa.payment.notices.service.model.notice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoticeRequestData {

    private Notice notice;
    private CreditorInstitution creditorInstitution;
    private Debtor debtor;
    private Map<String, Object> extraData;

}
