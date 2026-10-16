package com.dunnhumby.rmn.insights.api;

import com.dunnhumby.rmn.insights.service.CampaignNotFoundException;
import com.dunnhumby.rmn.insights.service.InvalidRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Turns exceptions into clear JSON error responses (RFC 9457 problem details). */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvalidRequestException.class)
    ProblemDetail invalid(InvalidRequestException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail badParameter(MethodArgumentTypeMismatchException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Parameter '" + e.getName() + "' has an invalid value (use ISO-8601, e.g. 2026-10-01T00:00:00Z)");
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    ProblemDetail missingHeader(MissingRequestHeaderException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Header '" + e.getHeaderName() + "' is required");
    }

    @ExceptionHandler(CampaignNotFoundException.class)
    ProblemDetail notFound(CampaignNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
