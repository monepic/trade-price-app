package com.monepic.tradeprice.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class RestExceptionHandlerTest {

    private RestExceptionHandler reh = new RestExceptionHandler();

    @Mock
    BindingResult formErrors;

    @Test
    public void everythingElseTest() {
        String msg = "my error message";
        assertEquals(msg, reh.everythingElse(new RuntimeException(msg)));
    }

    @Test
    public void validationErrsTest() {
        ResponseEntity<Object> result = reh.handleMethodArgumentNotValid(new MethodArgumentNotValidException(null, formErrors), null, null, null);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }
}
