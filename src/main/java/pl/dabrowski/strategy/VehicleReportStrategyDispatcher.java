package pl.dabrowski.strategy;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import pl.dabrowski.model.VehicleReportRequest;
import pl.dabrowski.model.VehicleReportResponse;

import java.util.List;

@Singleton
@Slf4j
public class VehicleReportStrategyDispatcher {

    @Inject
    private List<VehicleReportStrategy> strategies;

    public void execute(VehicleReportRequest request, VehicleReportResponse response) {
        log.info("Response preparation");

        // Iterate through the requested features and execute the corresponding strategies
        request.getFeatures().forEach(feature -> strategies.stream()
                .filter(s -> s.getType().equals(feature))
                .forEach(s -> s.execute(request, response)));

        log.info("Response preparation finished");
    }
}
