package it.gov.pagopa.payment.notices.service.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import it.gov.pagopa.payment.notices.service.model.GetSignedUrlResource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.CodeSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoggingAspectTest {

    private static final String SIGNED_URL = "https://example.invalid/notices/file.zip?sig=sas-for-log-test";

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private CodeSignature signature;

    private LoggingAspect loggingAspect;
    private Logger logger;
    private Level originalLevel;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setup() {
        MDC.clear();

        loggingAspect = new LoggingAspect(request, response);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getParameterNames()).thenReturn(new String[0]);

        logger = (Logger) LoggerFactory.getLogger(LoggingAspect.class);
        originalLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);

        logAppender = new ListAppender<>();
        logAppender.setContext(logger.getLoggerContext());
        logAppender.start();

        logger.addAppender(logAppender);
    }

    @AfterEach
    void cleanup() {
        logger.detachAppender(logAppender);
        logAppender.stop();
        logger.setLevel(originalLevel);
        MDC.clear();
    }

    @ParameterizedTest(name = "signed URL: controller={0}")
    @ValueSource(booleans = { true, false })
    void shouldRedactSignedUrlWithoutChangingReturnedResource(boolean controller) throws Throwable {

        GetSignedUrlResource resource = GetSignedUrlResource.builder().signedUrl(SIGNED_URL).build();

        Object returned = invokeAspect(controller, resource);

        assertSame(resource, returned);
        assertEquals(SIGNED_URL, resource.getSignedUrl());

        Level expectedLevel = controller ? Level.INFO : Level.DEBUG;

        assertTrue(logAppender.list.stream().anyMatch(event -> event.getLevel().equals(expectedLevel)
                && event.getFormattedMessage().contains("result: [SIGNED_URL_REDACTED]")));

        for (ILoggingEvent event : logAppender.list) {
            String message = event.getFormattedMessage();

            assertFalse(message.contains(SIGNED_URL));
            assertFalse(message.contains("sas-for-log-test"));
            assertFalse(message.contains("sig="));
            assertNull(event.getThrowableProxy());
        }
    }

    @ParameterizedTest(name = "ordinary result: controller={0}")
    @ValueSource(booleans = { true, false })
    void shouldPreserveLoggingForOtherResultTypes(boolean controller) throws Throwable {

        Object resource = new Object();

        Object returned = invokeAspect(controller, resource);

        assertSame(resource, returned);

        Level expectedLevel = controller ? Level.INFO : Level.DEBUG;

        assertTrue(logAppender.list.stream().anyMatch(event -> event.getLevel().equals(expectedLevel)
                && event.getFormattedMessage().contains("result: " + resource)));
    }

    private Object invokeAspect(boolean controller, Object resource) throws Throwable {

        when(joinPoint.proceed()).thenReturn(resource);

        if (controller) {
            when(signature.getName()).thenReturn("testOperation");
            when(response.getStatus()).thenReturn(200);

            return loggingAspect.logApiInvocation(joinPoint);
        }

        when(signature.toShortString()).thenReturn("TestService.testOperation()");

        return loggingAspect.logTrace(joinPoint);
    }
}