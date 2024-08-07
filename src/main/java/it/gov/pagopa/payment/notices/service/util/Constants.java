package it.gov.pagopa.payment.notices.service.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    public static final String HEADER_REQUEST_ID = "X-Request-Id";
    public static final String X_USER_ID = "X-User-Id";

    public static final String APIM_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";

    public static final String IDEMPOTENCY_KEY = "Idempotency-Key";
}
