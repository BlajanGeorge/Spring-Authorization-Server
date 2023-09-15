package com.authorizationserver.exception;

import com.authorizationserver.model.ErrorInformation;
import com.authorizationserver.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.sql.SQLException;

/**
 * Exceptions handler
 *
 * @author Blajan George
 */
@Slf4j
@SuppressWarnings("java:S2259")
@ControllerAdvice
public class AuthorizationExceptionsHandler {

    /**
     * Handle {@link RuntimeException}
     *
     * @param ex Exception instance
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(final RuntimeException ex) {
        log.error("Exception", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErrorResponse(
                        new ErrorInformation(
                                ex.getMessage(),
                                ex.getClass().getSimpleName())));
    }


    /**
     * Handle {@link IllegalArgumentException}
     *
     * @param ex Exception instance
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(final IllegalArgumentException ex) {
        log.error("Exception", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErrorResponse(
                        new ErrorInformation(
                                ex.getMessage(),
                                ex.getClass().getSimpleName())));
    }

    /**
     * Handle {@link @DataIntegrityViolationException}
     *
     * @param ex Exception instance
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityException(final DataIntegrityViolationException ex) {
        log.error("Exception", ex);
        if (ex.getMostSpecificCause() instanceof SQLException sqlException
                && sqlException.getSQLState().equals("23505")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    new ErrorResponse(
                            new ErrorInformation(
                                    "client_id must be unique",
                                    ex.getClass().getSimpleName())));
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ErrorResponse(
                        new ErrorInformation(
                                ex.getMessage(),
                                ex.getClass().getSimpleName())));
    }

    /**
     * Handle {@link InsufficientAuthenticationException}
     *
     * @param ex Exception instance
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientAuthenticationException(final InsufficientAuthenticationException ex) {
        log.error("Exception", ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ErrorResponse(
                        new ErrorInformation(
                                ex.getMessage(),
                                ex.getClass().getSimpleName())));
    }

    /**
     * Handle {@link MethodArgumentNotValidException}
     *
     * @param ex Exception instance
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(final MethodArgumentNotValidException ex) {
        log.error("Exception", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErrorResponse(
                        new ErrorInformation(
                                ex.getBindingResult().getFieldError() == null ? "" : ex.getBindingResult().getFieldError().getDefaultMessage(),
                                ex.getClass().getSimpleName())));
    }

    /**
     * Handle {@link EntityNotFoundException}
     *
     * @param ex Exception instance
     * @return {@link ResponseEntity}
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(final EntityNotFoundException ex) {
        log.error("Exception", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse(
                        new ErrorInformation(
                                ex.getMessage(),
                                ex.getClass().getSimpleName())));
    }

}
