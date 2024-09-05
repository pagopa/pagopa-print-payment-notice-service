package it.gov.pagopa.payment.notices.service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Resource model containing the generation request status data
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetErrorResource implements Serializable {

    @Schema(description = "folder id of the massive generation request")
    private String folderId;

    @Schema(description = "id of the item inside the generation request producing the error")
    private String errorId;

    @Schema(description = "code identifying the error")
    private String errorCode;

    @Schema(description = "description of the specific error")
    private String errorDescription;

    @Schema(description = "error creation date")
    private Instant createdAt;

}
