package pl.dabrowski.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaintenanceData {

    @JsonProperty("maintenance_frequency")
    private String frequency;
}