package it.gov.pagopa.payment.notices.service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import it.gov.pagopa.payment.notices.service.model.enums.PaymentGenerationRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Resource model containing the generation request status data
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetGenerationRequestStatusResource implements Serializable {

    @Schema(description = "Generation request status", requiredMode = Schema.RequiredMode.REQUIRED)
    private PaymentGenerationRequestStatus status;
    @Schema(description = "Successfully processed items in request", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> processedNotices;
    @Schema(description = "items related to the request that produced an error", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> noticesInError;

}
