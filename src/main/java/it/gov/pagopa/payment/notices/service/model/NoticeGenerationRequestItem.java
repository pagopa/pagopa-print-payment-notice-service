package it.gov.pagopa.payment.notices.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoticeGenerationRequestItem {

    private String templateId;
    private String data;

}
