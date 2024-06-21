package it.gov.pagopa.payment.notices.service.service.impl;

import it.gov.pagopa.payment.notices.service.client.ApiConfigClient;
import it.gov.pagopa.payment.notices.service.exception.AppError;
import it.gov.pagopa.payment.notices.service.exception.AppException;
import it.gov.pagopa.payment.notices.service.model.CreditorInstitutionsView;
import it.gov.pagopa.payment.notices.service.service.BrokerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class BrokerServiceImpl implements BrokerService {

    private final ApiConfigClient apiConfigClient;
    private final Integer pageLimit;

    public BrokerServiceImpl(ApiConfigClient apiConfigClient,
                             @Value("${notice.check.page.limit}") Integer pageLimit) {
        this.apiConfigClient = apiConfigClient;
        this.pageLimit = pageLimit;
    }

    @Override
    @Cacheable(value = "checkBrokerAllowance")
    public boolean checkBrokerAllowance(String brokerTaxCode, String targetTaxCode, String noticeCode) {

        try {

            int pageNumber = 0;

            while( true ) {

                CreditorInstitutionsView creditorInstitutionsView =
                        apiConfigClient.getCreditorInstitutionsAssociatedToBrokerStations(
                                pageLimit, pageNumber, targetTaxCode, brokerTaxCode);

                if(creditorInstitutionsView == null ||
                        creditorInstitutionsView.getCreditorInstitutionList().size() == 0) {
                    return false;
                }

                boolean checkResult = creditorInstitutionsView.getCreditorInstitutionList()
                        .stream().anyMatch(item -> {
                    String codeToCheck =
                            String.valueOf(Objects.requireNonNullElse(
                                        item.getAuxDigit(), 3))
                                    .concat(
                                            String.valueOf(Objects.requireNonNullElse(
                                                    item.getProgressivo(), 0))
                                    )
                                    .concat(
                                            String.valueOf(Objects.requireNonNullElse(
                                                    item.getSegregazione(), 0))
                                    );
                    return noticeCode.startsWith(codeToCheck);
                });

                if(checkResult || creditorInstitutionsView.getCreditorInstitutionList().size() != pageLimit) {
                    return checkResult;
                }

            }

        } catch (Exception e) {
          throw new AppException(AppError.ERROR_ON_PT_ALLOWANCE_CHECK);
        }

    }

}
