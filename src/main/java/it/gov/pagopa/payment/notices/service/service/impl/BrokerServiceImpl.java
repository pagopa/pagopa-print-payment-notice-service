package it.gov.pagopa.payment.notices.service.service.impl;

import it.gov.pagopa.payment.notices.service.client.ApiConfigClient;
import it.gov.pagopa.payment.notices.service.model.CreditorInstitutionView;
import it.gov.pagopa.payment.notices.service.model.CreditorInstitutionsView;
import it.gov.pagopa.payment.notices.service.service.BrokerService;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class BrokerServiceImpl implements BrokerService {

  private final ApiConfigClient apiConfigClient;
  private final Integer pageLimit;

  public BrokerServiceImpl(
      ApiConfigClient apiConfigClient, @Value("${notice.check.page.limit}") Integer pageLimit) {
    this.apiConfigClient = apiConfigClient;
    this.pageLimit = pageLimit;
  }

  /**
   * @param brokerTaxCode the code of the broker
   * @param ecTaxCode the code of the EC
   * @param noticeCode the notice number (NAV)
   * @return true if NAV is valid.
   *     <p>The NAV is valid if the prefix identifies a broker's station associated with the EC.
   */
  @Override
  @Cacheable(value = "checkBrokerAllowance")
  public boolean checkBrokerAllowance(String brokerTaxCode, String ecTaxCode, String noticeCode) {

    int pageNumber = 0;
    boolean isValid;

    CreditorInstitutionsView creditorInstitutionsView;

    do {
      // get a page of stations
      creditorInstitutionsView =
          apiConfigClient.getCreditorInstitutionsAssociatedToBrokerStations(
              pageLimit, pageNumber, ecTaxCode, brokerTaxCode);

      // check every station in the page
      isValid = checkNoticeNumberInPage(noticeCode, creditorInstitutionsView);

      pageNumber += 1;

      // check another page is we don't find any configured station
    } while (!isValid && !isPagesFinished(creditorInstitutionsView));

    return isValid;
  }

  /**
   * @param noticeCode the notice code is the NAV
   * @param creditorInstitutionsView the list of configured stations for the EC
   * @return true if the NAV starts with a valid configuration
   *     <p>if auxDigit = null the NAV must start with (3 + segregationCode or 0 + applicationCode)
   *     <p>if auxDigit = 1 the NAV must start with 1
   *     <p>if auxDigit = 2 the NAV must start with 2
   */
  private static boolean checkNoticeNumberInPage(
      String noticeCode, CreditorInstitutionsView creditorInstitutionsView) {

    // check input
    if (creditorInstitutionsView == null
        || creditorInstitutionsView.getCreditorInstitutionList() == null
        || creditorInstitutionsView.getCreditorInstitutionList().isEmpty()) {
      return false;
    }

    // find a configured station
    return creditorInstitutionsView.getCreditorInstitutionList().stream()
        .anyMatch(
            item -> {

              // Note: auxDigit = null is 0 or 3
              // if auxDigit is 0 the NAV must start with 0 + applicationCode
              // if auxDigit is 3 the NAV must start with 3 + segregationCode
              if (item.getAuxDigit() == null) {
                List<String> prefixes = getPrefixes(item);

                return prefixes.stream().anyMatch(noticeCode::startsWith);
              }

              // if auxDigit = 1 the NAV must start with 1
              if (item.getAuxDigit() == 1L) {
                return noticeCode.startsWith("1");
              }

              // if auxDigit = 2 the NAV must start with 2
              if (item.getAuxDigit() == 2L) {
                return noticeCode.startsWith("2");
              }

              return false;
            });
  }

  /**
   * @param stationConfiguration the item of the page
   * @return a list of valid prefixes extracted from the configuration of the station for the EC
   */
  @NotNull
  private static List<String> getPrefixes(CreditorInstitutionView stationConfiguration) {
    List<String> prefixes = new ArrayList<>();

    if (stationConfiguration.getSegregazione() != null) {
      String segregationCode = String.format("%02d", stationConfiguration.getSegregazione());
      prefixes.add("3" + segregationCode);
    }
    if (stationConfiguration.getProgressivo() != null) {
      String applicationCode = String.format("%02d", stationConfiguration.getProgressivo());
      prefixes.add("0" + applicationCode);
    }
    return prefixes;
  }

  /**
   * @param creditorInstitutionsView the page return from ApiConfig
   * @return true if the page is the last
   */
  private boolean isPagesFinished(CreditorInstitutionsView creditorInstitutionsView) {
    return creditorInstitutionsView == null
        || creditorInstitutionsView.getCreditorInstitutionList() == null
        || creditorInstitutionsView.getCreditorInstitutionList().isEmpty()
        || creditorInstitutionsView.getCreditorInstitutionList().size() < this.pageLimit;
  }
}
