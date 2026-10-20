package it.gov.pagopa.payment.notices.service.exception;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.*;

class ErrorHandlerTest {

    private final ErrorHandler errorHandler = new ErrorHandler();

    private final Logger logger = (Logger) LoggerFactory.getLogger(ErrorHandler.class);

    private final WebRequest request = new ServletWebRequest(new MockHttpServletRequest());

    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setupLogging() {
        MDC.clear();

        logAppender = new ListAppender<>();
        logAppender.setContext(logger.getLoggerContext());
        logAppender.start();

        logger.addAppender(logAppender);
    }

    @AfterEach
    void cleanupLogging() {
        logger.detachAppender(logAppender);
        logAppender.stop();
        MDC.clear();
    }

    @ParameterizedTest
    @EnumSource(value = AppError.class, names = { "ERROR_ON_GET_FILE_URL_REQUEST", "ERROR_ON_GET_FOLDER_URL_REQUEST" })
    void shouldLogSignedUrlErrorWithoutSensitiveData(AppError error) {
        IllegalStateException cause = new IllegalStateException(
                "client_secret=secret-for-log-test " + "https://example.invalid/file?sig=sas-for-log-test");

        AppException exception = new AppException(error, cause);

        var response = errorHandler.handleAppException(exception, request);

        assertEquals(error.getHttpStatus(), response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(error.getHttpStatus().value(), response.getBody().getStatus());
        assertEquals(error.getTitle(), response.getBody().getTitle());
        assertEquals(error.getDetails(), response.getBody().getDetail());
        assertSame(cause, exception.getCause());

        ILoggingEvent event = getSingleLogEvent();
        String message = event.getFormattedMessage();

        assertEquals(Level.WARN, event.getLevel());
        assertTrue(message.contains("Signed URL request failed:"));
        assertTrue(message.contains("httpStatus=500"));
        assertTrue(message.contains("causeType=" + IllegalStateException.class.getName()));

        assertFalse(message.contains("secret-for-log-test"));
        assertFalse(message.contains("sas-for-log-test"));
        assertFalse(message.contains("https://example.invalid"));
        assertNull(event.getThrowableProxy());
    }

    @Test
    void shouldHandleSignedUrlErrorWithoutCause() {
        AppError error = AppError.ERROR_ON_GET_FILE_URL_REQUEST;
        AppException exception = new AppException(error);

        var response = errorHandler.handleAppException(exception, request);

        assertEquals(error.getHttpStatus(), response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(error.getDetails(), response.getBody().getDetail());

        ILoggingEvent event = getSingleLogEvent();

        assertTrue(event.getFormattedMessage().contains("causeType=N/A"));
        assertNull(event.getThrowableProxy());
    }

    @Test
    void shouldPreserveExistingHandlingForOtherAppErrors() {
        AppError error = AppError.ERROR_ON_GET_ERROR;
        IllegalStateException cause = new IllegalStateException("Unrelated failure");

        AppException exception = new AppException(error, cause);

        var response = errorHandler.handleAppException(exception, request);

        assertEquals(error.getHttpStatus(), response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(error.getTitle(), response.getBody().getTitle());
        assertEquals(error.getDetails(), response.getBody().getDetail());

        ILoggingEvent event = getSingleLogEvent();

        assertTrue(event.getFormattedMessage().contains("App Exception raised:"));
        assertNotNull(event.getThrowableProxy());
        assertEquals(IllegalStateException.class.getName(), event.getThrowableProxy().getClassName());
    }

    private ILoggingEvent getSingleLogEvent() {
        assertEquals(1, logAppender.list.size());
        return logAppender.list.get(0);
    }
}