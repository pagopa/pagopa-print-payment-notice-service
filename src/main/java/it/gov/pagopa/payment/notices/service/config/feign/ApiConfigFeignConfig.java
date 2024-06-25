package it.gov.pagopa.payment.notices.service.config.feign;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import static it.gov.pagopa.payment.notices.service.util.Constants.APIM_SUBSCRIPTION_KEY;

public class ApiConfigFeignConfig {

    @Value("${authorization.api-config.subscriptionKey}")
    private String apiConfigSubscriptionKey;


    @Bean
    public RequestInterceptor subscriptionKey() {
        return requestTemplate -> requestTemplate.header(APIM_SUBSCRIPTION_KEY, apiConfigSubscriptionKey);
    }

}
