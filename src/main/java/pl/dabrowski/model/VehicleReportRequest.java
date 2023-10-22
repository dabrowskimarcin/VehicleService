package pl.dabrowski.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Serdeable
@Introspected
@AllArgsConstructor
public class VehicleReportRequest {

    @NotEmpty
    @JsonProperty("vin")
    private String vin;

    @NotEmpty
    @JsonProperty("features")
    private List<String> features;
}
