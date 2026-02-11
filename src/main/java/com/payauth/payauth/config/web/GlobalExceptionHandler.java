package com.payauth.payauth.config.web;

import com.payauth.payauth.common.error.ApiErrorResponse;
import com.payauth.payauth.common.error.BusinessException;
import com.payauth.payauth.common.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 전역 예외 처리 핸들러
 * 모든 예외를 일관된 ApiErrorResponse 형식으로 변환하여 클라이언트에게 반환
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * BusinessException 처리
     * 비즈니스 로직에서 발생하는 예외를 처리
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ApiErrorResponse> handleBusinessException(
            BusinessException e,
            HttpServletRequest request) {

        log.warn("BusinessException occurred: code={}, message={}, path={}",
                e.getErrorCode().getCode(),
                e.getMessage(),
                request.getRequestURI());

        ErrorCode errorCode = e.getErrorCode();
        ApiErrorResponse response = ApiErrorResponse.of(
                errorCode,
                e.getDetail(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    /**
     * @Valid 검증 실패 예외 처리 (@RequestBody + @ModelAttribute 통합)
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    protected ResponseEntity<ApiErrorResponse> handleBindingException(Exception e, HttpServletRequest request) {
        BindingResult bindingResult;
        if (e instanceof MethodArgumentNotValidException) {
            bindingResult = ((MethodArgumentNotValidException) e).getBindingResult();
        } else {
            bindingResult = ((BindException) e).getBindingResult();
        }

        log.warn("Validation/Binding failed: path={}, errors={}", request.getRequestURI(), bindingResult.getFieldErrors());

        List<ApiErrorResponse.FieldError> fieldErrors = bindingResult.getFieldErrors()
                .stream()
                .map(error -> new ApiErrorResponse.FieldError(
                        error.getField(),
                        error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                        error.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, fieldErrors, request.getRequestURI()));
    }

    /**
     * 타입 변환 실패 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e,
            HttpServletRequest request) {

        log.warn("Type mismatch: path={}, parameter={}, value={}",
                request.getRequestURI(),
                e.getName(),
                e.getValue());

        ApiErrorResponse response = ApiErrorResponse.of(
                ErrorCode.INVALID_TYPE_VALUE,
                String.format("'%s' 파라미터의 타입이 올바르지 않습니다", e.getName()),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * JSON 파싱 실패 예외 처리
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e,
            HttpServletRequest request) {

        log.warn("Message not readable: path={}, message={}",
                request.getRequestURI(),
                e.getMessage());

        ApiErrorResponse response = ApiErrorResponse.of(
                ErrorCode.INVALID_INPUT_VALUE,
                "요청 본문을 읽을 수 없습니다. JSON 형식을 확인해주세요",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * 지원하지 않는 HTTP 메서드 예외 처리
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ApiErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e,
            HttpServletRequest request) {

        log.warn("Method not supported: path={}, method={}",
                request.getRequestURI(),
                e.getMethod());

        ApiErrorResponse response = ApiErrorResponse.of(
                ErrorCode.METHOD_NOT_ALLOWED,
                String.format("'%s' 메서드는 지원하지 않습니다", e.getMethod()),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(response);
    }

    /**
     * 핸들러를 찾을 수 없는 예외 처리 (404)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    protected ResponseEntity<ApiErrorResponse> handleNoHandlerFoundException(
            NoHandlerFoundException e,
            HttpServletRequest request) {

        log.warn("No handler found: path={}, method={}",
                request.getRequestURI(),
                e.getHttpMethod());

        ApiErrorResponse response = ApiErrorResponse.of(
                ErrorCode.ENTITY_NOT_FOUND,
                "요청한 리소스를 찾을 수 없습니다",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    /**
     * 그 외 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiErrorResponse> handleException(
            Exception e,
            HttpServletRequest request) {

        log.error("Unexpected error occurred: path={}, message={}",
                request.getRequestURI(),
                e.getMessage(),
                e);

        ApiErrorResponse response = ApiErrorResponse.of(
                ErrorCode.INTERNAL_SERVER_ERROR,
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
