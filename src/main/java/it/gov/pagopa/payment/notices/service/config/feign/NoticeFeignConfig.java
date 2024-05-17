package it.gov.pagopa.payment.notices.service.config.feign;

import feign.RequestInterceptor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import static it.gov.pagopa.payment.notices.service.util.Constants.APIM_SUBSCRIPTION_KEY;
import static it.gov.pagopa.payment.notices.service.util.Constants.HEADER_REQUEST_ID;


public class NoticeFeignConfig {

    @Value("${authorization.notice.generator.subscriptionKey}")
    private String subscriptionKey;


    @Bean
    public RequestInterceptor headersInterceptor() {
        return requestTemplate -> requestTemplate
                .header(APIM_SUBSCRIPTION_KEY, subscriptionKey)
                .header(HEADER_REQUEST_ID, MDC.get("requestId"));

    }

}
