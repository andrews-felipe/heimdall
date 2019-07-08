/*
 * Copyright (C) 2018 Conductor Tecnologia SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.conductor.heimdall.api.configuration;

import br.com.conductor.heimdall.api.configuration.GlobalExceptionHandler.BindExceptionInfo.BindError;
import br.com.conductor.heimdall.core.exception.*;
import br.com.conductor.heimdall.core.util.UrlUtil;
import com.fasterxml.jackson.core.JsonParseException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
//import org.apache.http.NoHttpResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * This class captures the exceptions generated by the system and redirects them to the Heimdall custom exceptions.
 *
 * @author Thiago Sampaio
 * @author Filipe Germano
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Method that captures all the {@link NotFoundException} exceptions.
     *
     * @param response  {@link HttpServletResponse}
     * @param request   {@link HttpServletRequest}
     * @param exception {@link Exception}
     * @return {@link ErroInfo}.
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public @ResponseBody
    ErroInfo handleExceptionNotFound(HttpServletResponse response, HttpServletRequest request, Exception exception) {

        ErroInfo erroInfo = buildErrorInfo(request, exception);
        return erroInfo;

    }

    /**
     * Method that captures all the {@link ServerErrorException} exceptions.
     *
     * @param response  {@link HttpServletResponse}
     * @param request   {@link HttpServletRequest}
     * @param exception {@link Exception}
     * @return {@link ErroInfo}.
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ServerErrorException.class)
    public @ResponseBody
    ErroInfo handleExceptionServerError(HttpServletResponse response, HttpServletRequest request, Exception exception) {

        ErroInfo erroInfo = buildErrorInfo(request, exception);
        return erroInfo;

    }

    /**
     * Method that captures all the {@link ServerErrorException} exceptions.
     *
     * @param response  {@link HttpServletResponse}
     * @param request   {@link HttpServletRequest}
     * @param exception {@link Exception}
     * @return {@link ErroInfo}.
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public @ResponseBody
    ErroInfo handleException(HttpServletResponse response, HttpServletRequest request, Exception exception) {

        ErroInfo erroInfo = buildErrorInfoException(request, exception);
        log.error(exception.getMessage(), exception);
        return erroInfo;

    }

//    /**
//     * Method that captures all the {@link ServerErrorException} exceptions.
//     *
//     * @param response  {@link HttpServletResponse}
//     * @param request   {@link HttpServletRequest}
//     * @param exception {@link Exception}
//     * @return {@link ErroInfo}.
//     */
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    @ExceptionHandler(NoHttpResponseException.class)
//    public @ResponseBody
//    ErroInfo noHttpResponseExceptionServerError(HttpServletResponse response, HttpServletRequest request, NoHttpResponseException exception) {
//
//        ErroInfo erroInfo = buildErrorInfoException(request, exception);
//        log.error(exception.getMessage(), exception);
//        return erroInfo;
//
//    }

    /**
     * Method that captures all the {@link BadRequestException} exceptions.
     *
     * @param response  {@link HttpServletResponse}
     * @param request   {@link HttpServletRequest}
     * @param exception {@link Exception}
     * @return {@link ErroInfo}.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestException.class)
    public @ResponseBody
    ErroInfo handleExceptionBadRequest(HttpServletResponse response, HttpServletRequest request, Exception exception) {

        ErroInfo erroInfo = buildErrorInfo(request, exception);
        return erroInfo;

    }

    /**
     * Method that captures all the {@link UnauthorizedException} exceptions.
     *
     * @param response  {@link HttpServletResponse}
     * @param request   {@link HttpServletRequest}
     * @param exception {@link Exception}
     * @return {@link ErroInfo}.
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthorizedException.class)
    public @ResponseBody
    ErroInfo handleExceptionUnauthorized(HttpServletResponse response, HttpServletRequest request, Exception exception) {

        ErroInfo erroInfo = buildErrorInfo(request, exception);
        return erroInfo;

    }

    /**
     * Method that captures all the {@link ForbiddenException} exceptions.
     *
     * @param response  {@link HttpServletResponse}
     * @param request   {@link HttpServletRequest}
     * @param exception {@link Exception}
     * @return {@link ErroInfo}.
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(ForbiddenException.class)
    public @ResponseBody
    ErroInfo handleExceptionForbidden(HttpServletResponse response, HttpServletRequest request, Exception exception) {

        ErroInfo erroInfo = buildErrorInfo(request, exception);
        return erroInfo;

    }

    @Autowired
    private MessageSource messageSource;

    /**
     * Method that captures all the {@link BindExceptionInfo} exceptions.
     *
     * @param response  {@link HttpServletResponse}
     * @param request   {@link HttpServletRequest}
     * @param exception {@link BindExceptionInfo}
     * @return {@link BindExceptionInfo}.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public @ResponseBody
    BindExceptionInfo validationBindException(HttpServletResponse response, HttpServletRequest request, BindException exception) {

        BindExceptionInfo bindException = new BindExceptionInfo();
        List<BindError> errors = new ArrayList<>();
        List<ObjectError> objectsError = exception.getBindingResult().getAllErrors();

        objectsError.forEach(objectError -> {
            FieldError fieldError = (FieldError) objectError;

            String message = null;

            try {

                String code = fieldError.getCodes()[0];
                message = messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
            } catch (Exception e) {

                message = null;
            }

            bindException.timestamp = LocalDateTime.now();
            bindException.status = 400;
            bindException.exception = "BindException";

            BindError error = bindException.new BindError();
            error.defaultMessage = message != null ? message : fieldError.getDefaultMessage();
            error.objectName = fieldError.getObjectName();
            error.field = fieldError.getField();
            error.reason = fieldError.getCode();

            errors.add(error);
        });

        bindException.errors = errors;

        return bindException;
    }

    /**
     * Method that captures all the {@link BindExceptionInfo} exceptions.
     *
     * @param response  {@link HttpServletResponse}
     * @param request   {@link HttpServletRequest}
     * @param exception {@link BindExceptionInfo}
     * @return {@link BindExceptionInfo}.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public @ResponseBody
    BindExceptionInfo validationMethodArgumentNotValidException(HttpServletResponse response, HttpServletRequest request, MethodArgumentNotValidException exception) {

        BindExceptionInfo bindException = new BindExceptionInfo();
        List<BindError> errors = new ArrayList<>();
        List<ObjectError> objectsError = exception.getBindingResult().getAllErrors();

        objectsError.forEach(objectError -> {
            FieldError fieldError = (FieldError) objectError;

            bindException.timestamp = LocalDateTime.now();
            bindException.status = 400;
            bindException.exception = "MethodArgumentNotValidException";

            BindError error = bindException.new BindError();
            error.defaultMessage = fieldError.getDefaultMessage();
            error.objectName = fieldError.getObjectName();
            error.field = fieldError.getField();
            error.reason = fieldError.getCode();

            errors.add(error);
        });

        bindException.errors = errors;

        return bindException;
    }

    /**
     * Method that captures all the {@link AccessDeniedException} exceptions.
     *
     * @param response  {@link HttpServletResponse}
     * @param request   {@link HttpServletRequest}
     * @param exception {@link Exception}
     * @return {@link ErroInfo}.
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AccessDeniedException.class)
    public @ResponseBody
    ErroInfo handleExceptionAccessDenied(HttpServletResponse response, HttpServletRequest request, Exception exception) {

        ErroInfo erroInfo = buildErrorInfo(request, new HeimdallException(ExceptionMessage.ACCESS_DENIED));
        return erroInfo;

    }

    /**
     * Method that captures all the {@link HttpMessageNotReadableException} exceptions.
     *
     * @param response  {@link HttpServletResponse}
     * @param request   {@link HttpServletRequest}
     * @param exception {@link Exception}
     * @return {@link ErroInfo}.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public @ResponseBody
    ErroInfo handleExceptionHttpMessageNotReadable(HttpServletResponse response, HttpServletRequest request, HttpMessageNotReadableException exception) {

        ErroInfo erroInfo = buildErrorInfo(request, new HeimdallException(ExceptionMessage.GLOBAL_JSON_INVALID_FORMAT));
        return erroInfo;

    }

    /**
     * Method that captures all the {@link JsonParseException} exceptions.
     *
     * @param response  {@link HttpServletResponse}
     * @param request   {@link HttpServletRequest}
     * @param exception {@link Exception}
     * @return {@link ErroInfo}.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(JsonParseException.class)
    public @ResponseBody
    ErroInfo handleExceptionJsonParse(HttpServletResponse response, HttpServletRequest request, JsonParseException exception) {

        ErroInfo erroInfo = buildErrorInfo(request, new HeimdallException(ExceptionMessage.GLOBAL_JSON_INVALID_FORMAT));
        return erroInfo;

    }

    /**
     * Method that captures all the {@link DataIntegrityViolationException} exceptions.
     *
     * @param response  {@link HttpServletResponse}
     * @param request   {@link HttpServletRequest}
     * @param exception {@link Exception}
     * @return {@link ErroInfo}.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public @ResponseBody
    ErroInfo handleSqlException(HttpServletResponse response, HttpServletRequest request, DataIntegrityViolationException exception) {

        if (exception.getMessage().contains("email")) {
            return buildErrorInfo(request, new HeimdallException(ExceptionMessage.EMAIL_ALREADY_EXIST));
        }

        if (exception.getMessage().contains("username")) {
            return buildErrorInfo(request, new HeimdallException(ExceptionMessage.USERNAME_ALREADY_EXIST));
        }

        return buildErrorInfo(request, new HeimdallException(ExceptionMessage.GLOBAL_RESOURCE_NOT_FOUND));

    }

    /**
     * Method responsible to create the exception object.
     *
     * @param request   {@link HttpServletRequest}
     * @param exception {@link Exception}
     * @return {@link ErroInfo}.
     */
    private ErroInfo buildErrorInfo(HttpServletRequest request, Exception exception) {

        HeimdallException heimdallException = (HeimdallException) exception;
        ErroInfo erroInfo = new ErroInfo(LocalDateTime.now(), heimdallException.getMsgEnum().getHttpCode(), heimdallException.getClass().getSimpleName(), heimdallException.getMessage(), UrlUtil.getCurrentUrl(request));
        return erroInfo;
    }

    /**
     * Method responsible to create the exception object.
     *
     * @param request   {@link HttpServletRequest}
     * @param exception {@link Exception}
     * @return {@link ErroInfo}.
     */
    private ErroInfo buildErrorInfoException(HttpServletRequest request, Exception exception) {

        HeimdallException heimdallException = new HeimdallException(ExceptionMessage.GLOBAL_ERROR_ZUUL);
        ErroInfo erroInfo = new ErroInfo(LocalDateTime.now(), heimdallException.getMsgEnum().getHttpCode(), heimdallException.getClass().getSimpleName(), heimdallException.getMessage(), UrlUtil.getCurrentUrl(request));
        return erroInfo;
    }

    /**
     * Class that represents the return object used by all Heimdall Exceptions.
     *
     * @author Thiago Sampaio
     */
    @AllArgsConstructor
    @Getter
    public class ErroInfo {

        /**
         * TImestamp from the moment that the exception was created.
         */
        private LocalDateTime timestamp;

        /**
         * Exception identifier.
         */
        private Integer status;

        /**
         * Exception class name.
         */
        private String exception;

        /**
         * Exception description.
         */
        private String message;

        /**
         * Path that generated the request that caused the exception.
         */
        private String path;

    }

    /**
     * Class that represents the exceptions created by the Heimdall validations.
     *
     * @author Filipe Germano
     */
    public class BindExceptionInfo {

        @Getter
        private LocalDateTime timestamp;

        @Getter
        private Integer status;

        @Getter
        private String exception;

        @Getter
        private List<BindError> errors;

        @Data
        public class BindError {

            private String defaultMessage;

            private String objectName;

            private String field;

            private String reason;
        }

    }

}
