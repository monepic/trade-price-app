package com.monepic.tradeprice.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handle REST exceptions, and provide the appropriate response
 */
@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {
        return new ResponseEntity<>(new RequestErrors(ex.getBindingResult()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String everythingElse(Exception ex) {
        log.debug("Handled generic request exception", ex);
        return ex.getMessage();
    }

    /**
     * For returning validation field errors as JSON
     */
    public static class SubmissionFieldError {

        private String field, message;
        private Object rejectedValue;

        public SubmissionFieldError(FieldError fe) {
            this.field = fe.getField();
            this.message = fe.getDefaultMessage();
            this.rejectedValue = fe.getRejectedValue();
        }

        public String getField() { return field; }

        public String getMessage() { return message; }

        public Object getRejectedValue() { return rejectedValue; }
    }

    /**
     * For returning validation errors as JSON
     */
    public static class RequestErrors {

        private final List<SubmissionFieldError> fieldErrors;
        private final List<String> errors;

        public RequestErrors(BindingResult br) {
            this.fieldErrors = br.getFieldErrors().stream()
                    .map(SubmissionFieldError::new)
                    .collect(Collectors.toList());

            this.errors = br.getGlobalErrors().stream()
                    .map(e -> e.getDefaultMessage())
                    .collect(Collectors.toList());
        }

        @JsonInclude(value = Include.NON_EMPTY)
        public List<SubmissionFieldError> getFieldErrors() { return fieldErrors; }

        @JsonInclude(value = Include.NON_EMPTY)
        public List<String> getErrors() { return errors; }
    }
}