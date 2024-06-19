package it.gov.pagopa.payment.notices.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * CreditorInstitutions
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditorInstitutionsView {

    @JsonProperty("creditor_institutions")
    private List<CreditorInstitutionView> creditorInstitutionList;

    @JsonProperty("page_info")
    private PageInfo pageInfo;
}
