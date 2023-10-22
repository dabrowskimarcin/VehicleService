package pl.dabrowski.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Serdeable
@Introspected
@Setter
@Getter
@AllArgsConstructor
public class VehicleReportResponse {

    public VehicleReportResponse(String requestId, String vin) {
        this.requestId = requestId;
        this.vin = vin;
    }

    @NotEmpty
    @JsonProperty("request_id")
    private String requestId;

    @NotEmpty
    @JsonProperty("vin")
    private String vin;

    @JsonProperty("accident_free")
    private Boolean accidentFree;

    @NotBlank
    @JsonProperty("maintenance_score")
    private String maintenanceScore;
}