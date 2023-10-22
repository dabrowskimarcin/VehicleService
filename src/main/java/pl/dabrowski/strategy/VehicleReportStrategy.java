package pl.dabrowski.strategy;

import pl.dabrowski.model.VehicleReportRequest;
import pl.dabrowski.model.VehicleReportResponse;

public interface VehicleReportStrategy {

    void execute(VehicleReportRequest request, VehicleReportResponse response);

    String getType();
}
