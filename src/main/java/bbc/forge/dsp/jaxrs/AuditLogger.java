package bbc.forge.dsp.jaxrs;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.log4j.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RequestHandler to log out all DELETE requests to the audit.log file.
 */
public class AuditLogger implements ContainerRequestFilter {

    private Logger log = Logger.getLogger("AuditLogger");

    @SuppressWarnings("unchecked")
    @Override
    public void filter(ContainerRequestContext containerRequestContext) {

        Message message = PhaseInterceptorChain.getCurrentMessage();

        Object method = message.get(Message.HTTP_REQUEST_METHOD);
        if ("DELETE".equals(method)) {
            Object url = message.get(Message.REQUEST_URL);
            Object queryString = message.get(Message.QUERY_STRING);
            Map<String, List<String>> headers = (Map<String, List<String>>) message.get(Message.PROTOCOL_HEADERS);
            String email = "<no-email>";
            if (headers != null) {
                List<String> emailHeaders = headers.get("sslclientcertsubject");
                if (emailHeaders != null && !emailHeaders.isEmpty()) {
                    HashMap<String, String> subjectMap = parseSubject(emailHeaders.get(0));
                    if (subjectMap.containsKey("Email")) {
                        email = subjectMap.get("Email");
                    }
                }
            }
            if (queryString != null) {
                log.info(method + " " + url + "?" + queryString + " " + email);
            } else {
                log.info(method + " " + url + " " + email);
            }
        }
    }

    private HashMap<String, String> parseSubject(String subject) {
        HashMap<String, String> subjectMap = new HashMap<String, String>();
        String[] parts = subject.split(", ");
        for (String part : parts) {
            String[] pair = part.split("=");
            subjectMap.put(pair[0], pair[1]);
        }
        return subjectMap;
    }
}
