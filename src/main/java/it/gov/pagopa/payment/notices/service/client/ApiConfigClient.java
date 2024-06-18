package it.gov.pagopa.payment.notices.service.client;

import feign.FeignException;
import it.gov.pagopa.payment.notices.service.config.feign.ApiConfigFeignConfig;
import it.gov.pagopa.payment.notices.service.model.CreditorInstitutionsView;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "api-config", url = "${rest-client.api-config.base-url}", configuration = ApiConfigFeignConfig.class)
public interface ApiConfigClient {

    @GetMapping(value = "/creditorinstitutions/view", produces = MediaType.APPLICATION_JSON_VALUE)
    @Retryable(
            noRetryFor = FeignException.FeignClientException.class,
            maxAttemptsExpression = "${retry.utils.maxAttempts}",
            backoff = @Backoff(delayExpression = "${retry.utils.maxDelay}"))
    @Valid
    CreditorInstitutionsView getCreditorInstitutionsAssociatedToBrokerStations(
            @RequestParam(required = false, defaultValue = "50") Integer limit,
            @RequestParam Integer page,
            @RequestParam(required = false, name = "creditorInstitutionCode") String creditorInstitutionCode,
            @RequestParam(required = false, name = "paBrokerCode") String paBrokerCode
    );

}
