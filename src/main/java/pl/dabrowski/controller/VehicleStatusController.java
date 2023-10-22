package pl.dabrowski.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.client.exceptions.ReadTimeoutException;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.validation.Validated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import pl.dabrowski.model.VehicleReportRequest;
import pl.dabrowski.model.VehicleReportResponse;
import pl.dabrowski.service.VehicleStatusService;
import reactor.core.publisher.Mono;

@Validated
@Controller("/check")
@Slf4j
public class VehicleStatusController {

    @Inject
    private VehicleStatusService vehicleStatusService;

    @Post("/")
    public Mono<MutableHttpResponse<VehicleReportResponse>> checkVehicleStatus(@Body @Valid VehicleReportRequest request) {
        // Handle the request to check vehicle status
        return Mono.just(HttpResponse.ok(vehicleStatusService.generateVehicleReport(request)))
                .doOnError(throwable -> log.error("We have an error", throwable))
                .onErrorResume(throwable -> Mono.just(HttpResponse.badRequest()));
    }

    @Error
    public HttpResponse<JsonError> handleValidationException(ValidationException e) {
        log.error("Request validation has arrived");
        // Return a bad request with a JSON error message
        return HttpResponse.badRequest(new JsonError(e.getMessage()));
    }

    @Error
    public HttpResponse<JsonError> handleValidationException(HttpClientResponseException e) {
        log.error("Internal server error");
        // Return a server error with a JSON error message
        return HttpResponse.serverError(new JsonError(e.getMessage()));
    }

    @Error
    public HttpResponse<JsonError> handleValidationException(ReadTimeoutException e) {
        log.error("Read timeout has arrived");
        // Return a request timeout with a JSON error message
        return HttpResponse.status(HttpStatus.REQUEST_TIMEOUT, e.getMessage()).body(new JsonError(e.getMessage()));
    }
}