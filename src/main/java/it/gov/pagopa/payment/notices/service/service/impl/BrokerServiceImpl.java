package it.gov.pagopa.payment.notices.service.service.impl;

import it.gov.pagopa.payment.notices.service.service.BrokerService;
import org.springframework.stereotype.Service;

@Service
public class BrokerServiceImpl implements BrokerService {
    @Override
    public boolean checkBrokerAllowance(String brokerTaxCode, String targetTaxCode) {
        //TODO: Define broker allowance logic
        return false;
    }

}
