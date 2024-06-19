package it.gov.pagopa.payment.notices.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditorInstitutionView {

    @JsonProperty("creditor_institution_code")
    private String idDominio;

    @JsonProperty("broker_code")
    private String idIntermediarioPa;

    @JsonProperty("station_code")
    private String idStazione;

    @JsonProperty("aux_digit")
    private Long auxDigit;

    @JsonProperty("application_code")
    private Long progressivo;

    @JsonProperty("segregation_code")
    private Long segregazione;

    @JsonProperty("mod4")
    private Boolean quartoModello;

    @JsonProperty("station_enabled")
    private Boolean stationEnabled;
}
