package api;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.reactivex.Flowable;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by Salvador Montiel on 03/oct/2018.
 */
public class ApiError {
    public static HttpResponse asResponse(MutableHttpResponse<ApiError> response, String message) {
        ApiError error = new ApiError(response.status(), message);
        return response.body(error);
    }

    public static Flowable<HttpResponse> of(MutableHttpResponse<ApiError> response, String message) {
        ApiError error = new ApiError(response.status(), message);
        return Flowable.just(response.body(error));
    }

    public final HttpStatus error;
    public final String message;
    public final String timestamp;

    private ApiError(HttpStatus error, String message) {
        this.error = error;
        this.message = message;
        this.timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
