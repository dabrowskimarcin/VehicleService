package pl.dabrowski.clients;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.client.annotation.Client;
import pl.dabrowski.model.MaintenanceData;
import reactor.core.publisher.Mono;

@Client("https://topgarage.com")
public interface MaintenanceProviderClient {

    @Get("/cars/{vin}")
    Mono<MaintenanceData> getMaintenanceData(@PathVariable String vin);
}
