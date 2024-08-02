package it.gov.pagopa.payment.notices.service.util;

import it.gov.pagopa.payment.notices.service.exception.AppError;
import it.gov.pagopa.payment.notices.service.exception.AppException;
import it.gov.pagopa.payment.notices.service.model.NoticeGenerationRequestEH;
import it.gov.pagopa.payment.notices.service.model.NoticeGenerationRequestItem;
import it.gov.pagopa.payment.notices.service.service.BrokerService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonUtility {


    private static final Logger log = LoggerFactory.getLogger(CommonUtility.class);

    /**
     * @param value value to deNullify.
     * @return return empty string if value is null
     */
    public static String deNull(String value) {
        return Optional.ofNullable(value).orElse("");
    }

    /**
     * @param value value to deNullify.
     * @return return empty string if value is null
     */
    public static String deNull(Object value) {
        return Optional.ofNullable(value).orElse("").toString();
    }

    /**
     * @param value value to deNullify.
     * @return return false if value is null
     */
    public static Boolean deNull(Boolean value) {
        return Optional.ofNullable(value).orElse(false);
    }

    /**
     * @param headers header of the CSV file
     * @param rows    data of the CSV file
     * @return byte array of the CSV using commas (;) as separator
     */
    public static byte[] createCsv(List<String> headers, List<List<String>> rows) {
        var csv = new StringBuilder();
        csv.append(String.join(";", headers));
        rows.forEach(row -> csv.append(System.lineSeparator()).append(String.join(";", row)));
        return csv.toString().getBytes();
    }

    public static long getTimelapse(long startTime) {
        return Calendar.getInstance().getTimeInMillis() - startTime;
    }


    /**
     * Utility method to sanitize log params
     *
     * @param logParam log param to be sanitized
     * @return the sanitized param
     */
    public static String sanitizeLogParam(String logParam) {
        if (logParam.matches("\\w*")) {
            return logParam;
        }
        return "suspicious log param";
    }

    public static void checkUserId(String userId, NoticeGenerationRequestItem noticeGenerationRequestItem, BrokerService brokerService) {
        String ciTaxCode = noticeGenerationRequestItem.getData().getCreditorInstitution().getTaxCode();

        if (userId != null && !userId.toUpperCase().startsWith("ADMIN") &&
                !userId.equals(ciTaxCode) && !brokerService.checkBrokerAllowance(userId, ciTaxCode,
                noticeGenerationRequestItem.getData().getNotice().getCode())) {
            throw new AppException(AppError.NOT_ALLOWED_ON_CI_CODE);
        }
    }


    public static String getItemId(NoticeGenerationRequestEH noticeGenerationRequestEH) {
        return String.format("%s-%s-%s-%s", "pagopa-avviso",
                noticeGenerationRequestEH.getNoticeData().getData().getCreditorInstitution().getTaxCode(),
                noticeGenerationRequestEH.getNoticeData().getData().getNotice().getCode(),
                noticeGenerationRequestEH.getNoticeData().getTemplateId());
    }
}
