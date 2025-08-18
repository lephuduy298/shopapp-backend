package com.project.shopapp.error;

import com.project.shopapp.dto.rest.RestResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalException {

//        public static final String DEFAULT_ERROR_VIEW = "error";

    @ExceptionHandler(value = {
            Exception.class,
            PostException.class,
            DataNotFoundException.class,
            UserNotFoundException.class,
            IndvalidRuntimeException.class
    })
//        @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<RestResponse<Object>> handleException(Exception ex) {
        RestResponse<Object> rest = new RestResponse<>();
        rest.setMessage(ex.getMessage());
        rest.setStatusCode(HttpStatus.BAD_REQUEST.value());
        rest.setError("Exception occurs......");

        return ResponseEntity.badRequest().body(rest);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<RestResponse<Object>> handleException(MethodArgumentNotValidException ex) {

        BindingResult bindingResult = ex.getBindingResult();
        final List<FieldError> fieldErrors = bindingResult.getFieldErrors();

        RestResponse<Object> rest = new RestResponse<>();
        rest.setStatusCode(HttpStatus.BAD_REQUEST.value());
        rest.setError(ex.getBody().getDetail());
        List<String> errors = fieldErrors.stream().map(error -> error.getDefaultMessage()).collect(Collectors.toList());
        rest.setMessage(errors.size() > 1 ? errors :errors.get(0));

        return ResponseEntity.badRequest().body(rest);
    }

    @ExceptionHandler(value = {
            StorageException.class,
    })
    public ResponseEntity<RestResponse<Object>> handleFileUploadException(Exception ex) {
        RestResponse<Object> res = new RestResponse<Object>();
        res.setStatusCode(HttpStatus.PAYLOAD_TOO_LARGE.value());
        res.setMessage(ex.getMessage());
        res.setError("Exception upload file...");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }
}
