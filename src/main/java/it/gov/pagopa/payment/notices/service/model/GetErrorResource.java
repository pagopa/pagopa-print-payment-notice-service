package it.gov.pagopa.payment.notices.service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import it.gov.pagopa.payment.notices.service.model.enums.PaymentGenerationRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * Resource model containing the generation request status data
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetErrorResource implements Serializable {

    private String folderId;

    private String errorId;

    private String errorCode;

    private String errorDescription;

    private Instant createdAt;

    private boolean compressionError;

}
