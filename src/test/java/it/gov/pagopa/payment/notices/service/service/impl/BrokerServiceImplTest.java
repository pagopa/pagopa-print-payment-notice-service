package it.gov.pagopa.payment.notices.service.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import it.gov.pagopa.payment.notices.service.client.ApiConfigClient;
import it.gov.pagopa.payment.notices.service.model.CreditorInstitutionView;
import it.gov.pagopa.payment.notices.service.model.CreditorInstitutionsView;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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
    public void shouldReturnTrueOnValidElementOnList_applicationCode() {
        doReturn(CreditorInstitutionsView.builder().creditorInstitutionList(Collections.singletonList(
                CreditorInstitutionView.builder().auxDigit(null).progressivo(2L).segregazione(1L).build()
        )).build()).when(apiConfigClient).getCreditorInstitutionsAssociatedToBrokerStations(any(),any(),any(),any());
        boolean result = brokerService.checkBrokerAllowance(
                "test","test","30242323");
        assertTrue(result);
        verify(apiConfigClient).getCreditorInstitutionsAssociatedToBrokerStations(any(),any(),any(),any());
    }

    @Test
    public void shouldReturnTrueOnValidElementOnList_segregationCode() {
        doReturn(CreditorInstitutionsView.builder().creditorInstitutionList(Collections.singletonList(
                CreditorInstitutionView.builder().auxDigit(null).progressivo(null).segregazione(19L).build()
        )).build()).when(apiConfigClient).getCreditorInstitutionsAssociatedToBrokerStations(any(),any(),any(),any());
        boolean result = brokerService.checkBrokerAllowance(
                "test","test","31942323");
        assertTrue(result);
        verify(apiConfigClient).getCreditorInstitutionsAssociatedToBrokerStations(any(),any(),any(),any());
    }

    @Test
    public void shouldReturnTrueOnValidElementOnList_auxDigit1() {
        doReturn(CreditorInstitutionsView.builder().creditorInstitutionList(Collections.singletonList(
                CreditorInstitutionView.builder().auxDigit(1L).progressivo(null).segregazione(19L).build()
        )).build()).when(apiConfigClient).getCreditorInstitutionsAssociatedToBrokerStations(any(),any(),any(),any());
        boolean result = brokerService.checkBrokerAllowance(
                "test","test","18842323");
        assertTrue(result);
        verify(apiConfigClient).getCreditorInstitutionsAssociatedToBrokerStations(any(),any(),any(),any());
    }

    @Test
    public void shouldReturnTrueOnValidElementOnList_auxDigit2() {
        doReturn(CreditorInstitutionsView.builder().creditorInstitutionList(Collections.singletonList(
                CreditorInstitutionView.builder().auxDigit(2L).progressivo(null).segregazione(19L).build()
        )).build()).when(apiConfigClient).getCreditorInstitutionsAssociatedToBrokerStations(any(),any(),any(),any());
        boolean result = brokerService.checkBrokerAllowance(
                "test","test","28842323");
        assertTrue(result);
        verify(apiConfigClient).getCreditorInstitutionsAssociatedToBrokerStations(any(),any(),any(),any());
    }

    @Test
    public void shouldReturnFalseOnInvalidElement() {
        doReturn(CreditorInstitutionsView.builder().creditorInstitutionList(Collections.singletonList(
                CreditorInstitutionView.builder().auxDigit(null).progressivo(null).segregazione(null).build()
        )).build()).when(apiConfigClient).getCreditorInstitutionsAssociatedToBrokerStations(any(),any(),any(),any());
        boolean result = brokerService.checkBrokerAllowance(
                "test","test","3002323");
        assertFalse(result);
        verify(apiConfigClient).getCreditorInstitutionsAssociatedToBrokerStations(any(),any(),any(),any());
    }

}
