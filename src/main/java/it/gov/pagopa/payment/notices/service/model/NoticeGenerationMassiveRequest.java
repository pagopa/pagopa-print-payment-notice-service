package it.gov.pagopa.payment.notices.service.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotNull
    @Size(min = 1, max = 1000)
    private List<NoticeGenerationRequestItem> notices;

}
