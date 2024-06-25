package it.gov.pagopa.payment.notices.service.service.impl;

import it.gov.pagopa.payment.notices.service.client.ApiConfigClient;
import it.gov.pagopa.payment.notices.service.model.CreditorInstitutionView;
import it.gov.pagopa.payment.notices.service.model.CreditorInstitutionsView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BrokerServiceImplTest {

    @Mock
    public ApiConfigClient apiConfigClient;

    private BrokerServiceImpl brokerService;

    @BeforeEach
    public void init() {
        Mockito.reset(apiConfigClient);
        brokerService = new BrokerServiceImpl(apiConfigClient, 2);
    }

    @Test
    public void shouldReturnFalseOnNullList() {
        boolean result = brokerService.checkBrokerAllowance(
                "test","test","3202323");
        assertFalse(result);
        verify(apiConfigClient).getCreditorInstitutionsAssociatedToBrokerStations(any(),any(),any(),any());
    }

    @Test
    public void shouldReturnFalseOnEmptyList() {
        doReturn(CreditorInstitutionsView.builder().creditorInstitutionList(Collections.emptyList()).build())
                .when(apiConfigClient).getCreditorInstitutionsAssociatedToBrokerStations(any(),any(),any(),any());
        boolean result = brokerService.checkBrokerAllowance(
                "test","test","3202323");
        assertFalse(result);
        verify(apiConfigClient).getCreditorInstitutionsAssociatedToBrokerStations(any(),any(),any(),any());
    }

    @Test
    public void shouldReturnTrueOnValidElementOnList() {
        doReturn(CreditorInstitutionsView.builder().creditorInstitutionList(Collections.singletonList(
                CreditorInstitutionView.builder().auxDigit(3L).progressivo(2L).segregazione(0L).build()
        )).build()).when(apiConfigClient).getCreditorInstitutionsAssociatedToBrokerStations(any(),any(),any(),any());
        boolean result = brokerService.checkBrokerAllowance(
                "test","test","3202323");
        assertTrue(result);
        verify(apiConfigClient).getCreditorInstitutionsAssociatedToBrokerStations(any(),any(),any(),any());
    }

    @Test
    public void shouldReturnTrueOnValidElementOnListWithDefaultValues() {
        doReturn(CreditorInstitutionsView.builder().creditorInstitutionList(Collections.singletonList(
                CreditorInstitutionView.builder().auxDigit(null).progressivo(null).segregazione(null).build()
        )).build()).when(apiConfigClient).getCreditorInstitutionsAssociatedToBrokerStations(any(),any(),any(),any());
        boolean result = brokerService.checkBrokerAllowance(
                "test","test","3002323");
        assertTrue(result);
        verify(apiConfigClient).getCreditorInstitutionsAssociatedToBrokerStations(any(),any(),any(),any());
    }

}
