package it.gov.pagopa.payment.notices.service.service;

public interface BrokerService {

    boolean checkBrokerAllowance(String brokerTaxCode, String ecTaxCode, String noticeCode);

}
