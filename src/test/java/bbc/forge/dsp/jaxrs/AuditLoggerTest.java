package bbc.forge.dsp.jaxrs;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.container.ContainerRequestContext;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PhaseInterceptorChain.class)
public class AuditLoggerTest {

    private AuditLogger auditLogger;

    @Mock
    private ContainerRequestContext mockRequest;

    @Mock Logger logger;

    @Before
    public void setUp() {
        auditLogger = new AuditLogger();
        Whitebox.setInternalState(auditLogger, "log", logger);
        PowerMockito.mockStatic(PhaseInterceptorChain.class);
    }

    @Test
    public void with_the_GET_verb_no_log_is_made() throws Exception {
        Message mockMessage = mock(Message.class);
        when(mockMessage.get(Message.HTTP_REQUEST_METHOD)).thenReturn("GET");
        when(PhaseInterceptorChain.getCurrentMessage()).thenReturn(mockMessage);

        auditLogger.filter(mockRequest);
        verifyZeroInteractions(logger);
    }

    @Test
    public void with_the_DELETE_verb_a_log_is_made() throws Exception {
        Message mockMessage = mock(Message.class);
        when(mockMessage.get(Message.HTTP_REQUEST_METHOD)).thenReturn("DELETE");
        when(mockMessage.get(Message.REQUEST_URL)).thenReturn("url");
        when(mockMessage.get(Message.PROTOCOL_HEADERS)).thenReturn(
                ImmutableMap.<String, List<String>>builder()
                        .put("sslclientcertsubject",
                                Lists.newArrayList("Email=test@bbc.co.uk, CN=Mr Test, OU=BBC - FMT - Journalism Development, O=British Broadcasting Corporation, L=London, C=GB"))
                        .build());
        when(PhaseInterceptorChain.getCurrentMessage()).thenReturn(mockMessage);

        auditLogger.filter(mockRequest);
        verify(logger).info("DELETE url test@bbc.co.uk");
    }

    @Test
    public void with_a_query_string_a_log_is_made_including_the_string() throws Exception {
        Message mockMessage = mock(Message.class);
        when(mockMessage.get(Message.HTTP_REQUEST_METHOD)).thenReturn("DELETE");
        when(mockMessage.get(Message.REQUEST_URL)).thenReturn("url");
        when(mockMessage.get(Message.QUERY_STRING)).thenReturn("queryString");
        when(mockMessage.get(Message.PROTOCOL_HEADERS)).thenReturn(
                ImmutableMap.<String, List<String>>builder()
                        .put("sslclientcertsubject",
                                Lists.newArrayList("Email=test@bbc.co.uk, CN=Mr Test, OU=BBC - FMT - Journalism Development, O=British Broadcasting Corporation, L=London, C=GB"))
                        .build());
        when(PhaseInterceptorChain.getCurrentMessage()).thenReturn(mockMessage);

        auditLogger.filter(mockRequest);
        verify(logger).info("DELETE url?queryString test@bbc.co.uk");
    }

    @Test
    public void with_no_email_header_a_log_is_made() throws Exception {
        Message mockMessage = mock(Message.class);
        when(mockMessage.get(Message.HTTP_REQUEST_METHOD)).thenReturn("DELETE");
        when(mockMessage.get(Message.REQUEST_URL)).thenReturn("url");
        when(mockMessage.get(Message.PROTOCOL_HEADERS)).thenReturn(
                ImmutableMap.<String, List<String>>builder()
                        .build());
        when(PhaseInterceptorChain.getCurrentMessage()).thenReturn(mockMessage);

        auditLogger.filter(mockRequest);
        verify(logger).info("DELETE url <no-email>");
    }

    @Test
    public void with_badly_formatted_email_header_a_log_is_made() throws Exception {
        Message mockMessage = mock(Message.class);
        when(mockMessage.get(Message.HTTP_REQUEST_METHOD)).thenReturn("DELETE");
        when(mockMessage.get(Message.REQUEST_URL)).thenReturn("url");
        when(mockMessage.get(Message.PROTOCOL_HEADERS)).thenReturn(
                ImmutableMap.<String, List<String>>builder()
                        .put("sslclientcertsubject_email",
                                Lists.newArrayList("test@bbc.co.uk"))
                        .build());
        when(PhaseInterceptorChain.getCurrentMessage()).thenReturn(mockMessage);

        auditLogger.filter(mockRequest);
        verify(logger).info("DELETE url <no-email>");
    }
}
