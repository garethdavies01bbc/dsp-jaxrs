package bbc.forge.dsp.jaxrs;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.cxf.message.Message;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditLoggerTest {

    private AuditLogger auditLogger;

    @Mock Logger logger;

    @Before
    public void setUp() {
        auditLogger = new AuditLogger();
        Whitebox.setInternalState(auditLogger, "log", logger);
    }

    @Test
    public void with_the_GET_verb_no_log_is_made() throws Exception {
        Message message = mock(Message.class);
        when(message.get(Message.HTTP_REQUEST_METHOD)).thenReturn("GET");
        auditLogger.handleRequest(message, null);
        verifyZeroInteractions(logger);
    }

    @Test
    public void with_the_DELETE_verb_a_log_is_made() throws Exception {
        Message message = mock(Message.class);
        when(message.get(Message.HTTP_REQUEST_METHOD)).thenReturn("DELETE");
        when(message.get(Message.REQUEST_URL)).thenReturn("url");
        when(message.get(Message.PROTOCOL_HEADERS)).thenReturn(
                ImmutableMap.<String, List<String>>builder()
                        .put("sslclientcertsubject",
                                Lists.newArrayList("Email=test@bbc.co.uk, CN=Mr Test, OU=BBC - FMT - Journalism Development, O=British Broadcasting Corporation, L=London, C=GB"))
                        .build());

        auditLogger.handleRequest(message, null);
        verify(logger).info("DELETE url test@bbc.co.uk");
    }

    @Test
    public void with_a_query_string_a_log_is_made_including_the_string() throws Exception {
        Message message = mock(Message.class);
        when(message.get(Message.HTTP_REQUEST_METHOD)).thenReturn("DELETE");
        when(message.get(Message.REQUEST_URL)).thenReturn("url");
        when(message.get(Message.QUERY_STRING)).thenReturn("queryString");
        when(message.get(Message.PROTOCOL_HEADERS)).thenReturn(
                ImmutableMap.<String, List<String>>builder()
                        .put("sslclientcertsubject",
                                Lists.newArrayList("Email=test@bbc.co.uk, CN=Mr Test, OU=BBC - FMT - Journalism Development, O=British Broadcasting Corporation, L=London, C=GB"))
                        .build());

        auditLogger.handleRequest(message, null);
        verify(logger).info("DELETE url?queryString test@bbc.co.uk");
    }

    @Test
    public void with_no_email_header_a_log_is_made() throws Exception {
        Message message = mock(Message.class);
        when(message.get(Message.HTTP_REQUEST_METHOD)).thenReturn("DELETE");
        when(message.get(Message.REQUEST_URL)).thenReturn("url");
        when(message.get(Message.PROTOCOL_HEADERS)).thenReturn(
                ImmutableMap.<String, List<String>>builder()
                        .build());

        auditLogger.handleRequest(message, null);
        verify(logger).info("DELETE url <no-email>");
    }

    @Test
    public void with_badly_formatted_email_header_a_log_is_made() throws Exception {
        Message message = mock(Message.class);
        when(message.get(Message.HTTP_REQUEST_METHOD)).thenReturn("DELETE");
        when(message.get(Message.REQUEST_URL)).thenReturn("url");
        when(message.get(Message.PROTOCOL_HEADERS)).thenReturn(
                ImmutableMap.<String, List<String>>builder()
                        .put("sslclientcertsubject_email",
                                Lists.newArrayList("test@bbc.co.uk"))
                        .build());

        auditLogger.handleRequest(message, null);
        verify(logger).info("DELETE url <no-email>");
    }

}
