package it.gov.pagopa.payment.notices.service.config;

import feign.RequestInterceptor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import static it.gov.pagopa.payment.notices.service.util.Constants.APIM_SUBSCRIPTION_KEY;
import static it.gov.pagopa.payment.notices.service.util.Constants.HEADER_REQUEST_ID;

public class NoticeGenerationFeignConfig  {

    @Value("${authorization.notice.generator.subscriptionKey}")
    private String taxonomySubscriptionKey;

    @Bean
    public RequestInterceptor subscriptionKey() {
        return requestTemplate -> requestTemplate.header(APIM_SUBSCRIPTION_KEY, taxonomySubscriptionKey);
    }

    @Bean
    public RequestInterceptor commonHeaderInterceptor() {
        return requestTemplate -> requestTemplate
                .header(HEADER_REQUEST_ID, MDC.get("requestId"));
    }

}
