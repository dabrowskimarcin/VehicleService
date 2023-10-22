package pl.dabrowski.service;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import pl.dabrowski.model.VehicleReportRequest;
import pl.dabrowski.model.VehicleReportResponse;
import pl.dabrowski.strategy.VehicleReportStrategyDispatcher;

import java.util.UUID;

@Singleton
@Slf4j
public class VehicleStatusService {
    private final VehicleReportStrategyDispatcher dispatcher;

    @Inject
    public VehicleStatusService(VehicleReportStrategyDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public VehicleReportResponse generateVehicleReport(VehicleReportRequest request) {
        log.info("Report generation started");

        // Create a new VehicleReportResponse with a unique request ID and the VIN from the request
        VehicleReportResponse response = new VehicleReportResponse(UUID.randomUUID().toString(), request.getVin());

        // Execute the report generation using the dispatcher
        dispatcher.execute(request, response);

        log.info("Report generation finished");

        // Return the generated VehicleReportResponse
        return response;
    }
}

