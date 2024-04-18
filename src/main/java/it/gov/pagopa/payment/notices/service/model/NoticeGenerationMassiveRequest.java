package it.gov.pagopa.payment.notices.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoticeGenerationMassiveRequest {

    private List<NoticeGenerationRequestItem> notices;

}
