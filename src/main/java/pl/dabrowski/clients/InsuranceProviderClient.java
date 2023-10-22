package pl.dabrowski.clients;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.client.annotation.Client;
import pl.dabrowski.model.InsuranceData;
import reactor.core.publisher.Mono;

@Client("https://insurance.com")
public interface InsuranceProviderClient {

    @Get("/accidents/report")
    Mono<InsuranceData> getInsuranceData(@QueryValue String vin);
}

