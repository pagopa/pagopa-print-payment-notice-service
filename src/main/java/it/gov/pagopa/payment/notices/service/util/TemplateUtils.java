package it.gov.pagopa.payment.notices.service.util;

import it.gov.pagopa.payment.notices.service.exception.AppError;
import it.gov.pagopa.payment.notices.service.exception.AppException;
import it.gov.pagopa.payment.notices.service.model.NoticeGenerationRequestItem;
import it.gov.pagopa.payment.notices.service.model.notice.Notice;

public class TemplateUtils {

    private static final String TEMPLATE_SINGLE_INSTALMENT = "TemplateSingleInstalment";

    private static final String TEMPLATE_SINGLE_INSTALMENT_POSTE = "TemplateSingleInstalmentPoste";

    private static final String TEMPLATE_INSTALMENTS = "TemplateInstalments";

    private static final String TEMPLATE_INSTALMENTS_POSTE = "TemplateInstalmentsPoste";

    private static final String TEMPLATE_MANY_INSTALMENTS = "TemplateManyInstalments";

    private static final String TEMPLATE_CDS_INFRACTION = "TemplateCdsInfraction";

    private static final String TEMPLATE_CDS_INFRACTION_POSTE = "TemplateCdsInfractionPoste";

    private static final String TEMPLATE_CDS_INFRACTION_THERMAL = "TemplateCdsInfractionThermal";

    private static final String TEMPLATE_CDS_INFRACTION_THERMAL_IMMEDIATE = "TemplateCdsInfractionThermalImmediateNotification";

    public static String retrieveTemplateId(NoticeGenerationRequestItem noticeGenerationRequestItem) {

        Boolean withPoste = noticeGenerationRequestItem.getWithPoste();
        Notice notice = noticeGenerationRequestItem.getData().getNotice();

        if (withPoste == null || noticeGenerationRequestItem.getWithThermal() == null) {
            throw new AppException(AppError.MISSING_TEMPLATE_DATA);
        }

        if (notice.getDiscounted() != null) {
            if (noticeGenerationRequestItem.getWithThermal()) {
                return notice.getReduced() != null ? TEMPLATE_CDS_INFRACTION_THERMAL : TEMPLATE_CDS_INFRACTION_THERMAL_IMMEDIATE;
            } else {
                return withPoste != null ? TEMPLATE_CDS_INFRACTION_POSTE : TEMPLATE_CDS_INFRACTION;
            }
        } else if (notice.getInstallments() != null) {
            if (notice.getInstallments().size() >= 9) {
                return TEMPLATE_MANY_INSTALMENTS;
            } else {
                return withPoste ? TEMPLATE_INSTALMENTS_POSTE : TEMPLATE_INSTALMENTS;
            }
        } else {
            return withPoste ? TEMPLATE_SINGLE_INSTALMENT_POSTE : TEMPLATE_SINGLE_INSTALMENT;
        }

    }
}
