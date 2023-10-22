package pl.dabrowski.strategy;

import io.micronaut.retry.annotation.Retryable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import pl.dabrowski.clients.InsuranceProviderClient;
import pl.dabrowski.model.VehicleReportRequest;
import pl.dabrowski.model.VehicleReportResponse;

import java.util.Objects;

@Singleton
@Slf4j
public class InsuranceVehicleReportStrategy implements VehicleReportStrategy {

    private final InsuranceProviderClient insuranceClient;

    @Inject
    public InsuranceVehicleReportStrategy(InsuranceProviderClient insuranceClient) {
        this.insuranceClient = insuranceClient;
    }

    @Retryable(
            attempts = "3",
            delay = "1s",
            multiplier = "2",
            maxDelay = "10s"
    )
    @Override
    public void execute(VehicleReportRequest request, VehicleReportResponse response) {
        log.info("Executing strategy start");

        // Check if VehicleReportResponse or VehicleReportRequest is null
        if (Objects.isNull(response)) {
            log.error("VehicleReportResponse can't be null");
        }
        if (Objects.isNull(request)) {
            log.error("VehicleReportRequest can't be null");
        }

        // Retrieve insurance data for the given VIN
        insuranceClient.getInsuranceData(request.getVin())
                .subscribe(insuranceData -> {
                    // Extract the number of insurance claims
                    int claims = insuranceData.getReport().getClaims();
                    log.info("Claims {}", claims);
                    // Set the "accidentFree" field in the response based on the number of claims
                    response.setAccidentFree(claims == 0);
                });

        log.info("Executing strategy finish");
    }

    @Override
    public String getType() {
        return "accident_free";
    }
}
