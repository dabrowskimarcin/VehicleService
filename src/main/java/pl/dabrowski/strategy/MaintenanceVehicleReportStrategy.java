package pl.dabrowski.strategy;

import io.micronaut.retry.annotation.Retryable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import pl.dabrowski.clients.MaintenanceProviderClient;
import pl.dabrowski.model.VehicleReportRequest;
import pl.dabrowski.model.VehicleReportResponse;

import java.util.List;
import java.util.Objects;

@Singleton
@Slf4j
public class MaintenanceVehicleReportStrategy implements VehicleReportStrategy {

    private MaintenanceProviderClient maintenanceClient;

    @Inject
    public MaintenanceVehicleReportStrategy(MaintenanceProviderClient maintenanceClient) {
        this.maintenanceClient = maintenanceClient;
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

        // Retrieve maintenance data for the given VIN
        maintenanceClient.getMaintenanceData(request.getVin())
                .subscribe(maintenanceData
                        -> response.setMaintenanceScore(calculateMaintenanceScore(maintenanceData.getFrequency())));

        log.info("Executing strategy finish");
    }

    @Override
    public String getType() {
        return "maintenance";
    }

    private String calculateMaintenanceScore(String frequency) {

        if (List.of("very_low", "low").contains(frequency)) {
            return "poor";
        } else if ("medium".equals(frequency)) {
            return "average";
        } else if ("high".equals(frequency)) {
            return "good";
        }
        return "unknown";
    }
}
