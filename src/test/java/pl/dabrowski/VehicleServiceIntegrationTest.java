package pl.dabrowski;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.client.exceptions.ReadTimeoutException;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import pl.dabrowski.clients.InsuranceProviderClient;
import pl.dabrowski.clients.MaintenanceProviderClient;
import pl.dabrowski.model.*;
import reactor.core.publisher.Mono;

import java.util.List;

@MicronautTest
class VehicleServiceIntegrationTest {

    private static final String VIN = "4Y1SL65848Z411439";

    @Inject
    EmbeddedApplication<?> application;

    @Inject
    InsuranceProviderClient insuranceProviderClient;

    @Inject
    MaintenanceProviderClient maintenanceProviderClient;

    @Inject
    @Client("/")
    HttpClient httpClient;

    @MockBean(InsuranceProviderClient.class)
    InsuranceProviderClient insuranceProviderClientMock() {
        return Mockito.mock(InsuranceProviderClient.class);
    }

    @MockBean(MaintenanceProviderClient.class)
    MaintenanceProviderClient maintenanceProviderClientMock() {
        return Mockito.mock(MaintenanceProviderClient.class);
    }

    @Test
    void shouldConfirmThatApplicationIsRunning() {
        Assertions.assertTrue(application.isRunning());
    }

    @Test
    void shouldReturnBadRequest() {
        try {
            httpClient.toBlocking().exchange(HttpRequest.POST("/check", new VehicleReportRequest(null, null))
                    .contentType(MediaType.APPLICATION_JSON_TYPE), VehicleReportResponse.class);
        } catch (HttpClientResponseException e) {
            HttpResponse response = e.getResponse();

            Assertions.assertEquals(400, response.getStatus().getCode());
        }
    }

    @Test
    void shouldReturnInternalServerError() {
        //given
        BDDMockito.given(insuranceProviderClient.getInsuranceData(VIN)).willThrow(new HttpClientResponseException("Internal Server Error", HttpResponse.serverError()));

        //when
        try {
            httpClient.toBlocking().exchange(HttpRequest.POST("/check", new VehicleReportRequest(VIN, List.of("accident_free", "maintenance")))
                    .contentType(MediaType.APPLICATION_JSON_TYPE), VehicleReportResponse.class);
        } catch (HttpClientResponseException e) {
            HttpResponse response = e.getResponse();

            Assertions.assertEquals(500, response.getStatus().getCode());
        }
    }

    @Test
    void shouldReturnReadTimeout() {
        //given
        BDDMockito.given(insuranceProviderClient.getInsuranceData(VIN)).willThrow(ReadTimeoutException.TIMEOUT_EXCEPTION);

        //when
        try {
            httpClient.toBlocking().exchange(HttpRequest.POST("/check", new VehicleReportRequest(VIN, List.of("accident_free", "maintenance")))
                    .contentType(MediaType.APPLICATION_JSON_TYPE), VehicleReportResponse.class);
        } catch (HttpClientResponseException e) {
            HttpResponse response = e.getResponse();

            Assertions.assertEquals(408, response.getStatus().getCode());
        }
    }

    @Test
    void shouldReturnReportWithAccidentFreeAndMaintenance() {
        //given
        BDDMockito.given(insuranceProviderClient.getInsuranceData(VIN)).willReturn(Mono.just(insuranceData(3)));
        BDDMockito.given(maintenanceProviderClient.getMaintenanceData(VIN)).willReturn(Mono.just(maintenanceData("very_low")));

        //when
        HttpResponse<VehicleReportResponse> response = httpClient.toBlocking().exchange(HttpRequest.POST("/check",
                        new VehicleReportRequest(VIN, List.of("accident_free", "maintenance")))
                .contentType(MediaType.APPLICATION_JSON_TYPE), VehicleReportResponse.class);

        //then
        Assertions.assertEquals(200, response.getStatus().getCode());
        Assertions.assertTrue(response.getBody().isPresent());
        VehicleReportResponse vehicleReportResponse = response.getBody().get();
        Assertions.assertEquals(VIN, vehicleReportResponse.getVin());
        Assertions.assertNotNull(vehicleReportResponse.getRequestId());
        Assertions.assertEquals("poor", vehicleReportResponse.getMaintenanceScore());
        Assertions.assertFalse(vehicleReportResponse.getAccidentFree());
    }

    @Test
    void shouldReturnReportWithAccidentFree() {
        //given
        BDDMockito.given(insuranceProviderClient.getInsuranceData(VIN)).willReturn(Mono.just(insuranceData(0)));
        BDDMockito.given(maintenanceProviderClient.getMaintenanceData(VIN)).willReturn(Mono.just(maintenanceData("very_low")));

        //when
        HttpResponse<VehicleReportResponse> response = httpClient.toBlocking().exchange(HttpRequest.POST("/check",
                        new VehicleReportRequest(VIN, List.of("accident_free")))
                .contentType(MediaType.APPLICATION_JSON_TYPE), VehicleReportResponse.class);

        //then
        Assertions.assertEquals(200, response.getStatus().getCode());
        Assertions.assertTrue(response.getBody().isPresent());
        VehicleReportResponse vehicleReportResponse = response.getBody().get();
        Assertions.assertEquals(VIN, vehicleReportResponse.getVin());
        Assertions.assertNotNull(vehicleReportResponse.getRequestId());
        Assertions.assertNull(vehicleReportResponse.getMaintenanceScore());
        Assertions.assertTrue(vehicleReportResponse.getAccidentFree());
    }

    @Test
    void shouldReturnReportWithMaintenance() {
        //given
        BDDMockito.given(insuranceProviderClient.getInsuranceData(VIN)).willReturn(Mono.just(insuranceData(0)));
        BDDMockito.given(maintenanceProviderClient.getMaintenanceData(VIN)).willReturn(Mono.just(maintenanceData("high")));

        //when
        HttpResponse<VehicleReportResponse> response = httpClient.toBlocking().exchange(HttpRequest.POST("/check",
                        new VehicleReportRequest(VIN, List.of("maintenance")))
                .contentType(MediaType.APPLICATION_JSON_TYPE), VehicleReportResponse.class);

        //then
        Assertions.assertEquals(200, response.getStatus().getCode());
        Assertions.assertTrue(response.getBody().isPresent());
        VehicleReportResponse vehicleReportResponse = response.getBody().get();
        Assertions.assertEquals(VIN, vehicleReportResponse.getVin());
        Assertions.assertNotNull(vehicleReportResponse.getRequestId());
        Assertions.assertEquals("good", vehicleReportResponse.getMaintenanceScore());
        Assertions.assertNull(vehicleReportResponse.getAccidentFree());
    }

    private InsuranceData insuranceData(int claims) {
        InsuranceReportData reportData = new InsuranceReportData();
        reportData.setClaims(claims);
        InsuranceData insuranceData = new InsuranceData();
        insuranceData.setReport(reportData);
        return insuranceData;
    }

    private MaintenanceData maintenanceData(String frequency) {
        MaintenanceData maintenanceData = new MaintenanceData();
        maintenanceData.setFrequency(frequency);
        return maintenanceData;
    }
}
