package bbc.forge.dsp.jaxrs;

import bbc.forge.dsp.common.security.SSLCertificateParsingException;
import com.google.common.collect.Lists;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.log4j.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

/**
 * This filter will authorise access only to people:
 * - who have a valid BBC certificate.
 * - are listed in the list of the users authorised for this application
 * - The filter can be disabled (active=false) for sandbox and integration.
 *
 * Most of the time, you should enumerate the list of authorised users
 * in the /etc/bbc/authorisation/your-application/ folder.
 */
public class HttpRequestCertificateAuth extends AbstractHttpRequestCertificateAuth {

	private final Logger LOG = Logger.getLogger(this.getClass());
	
	private List<String> whiteOuList = Lists.newArrayList();

	@Override
	public void filter(ContainerRequestContext requestContext) {

		Message message = PhaseInterceptorChain.getCurrentMessage();
LOG.info("message:"+message);
		try {
			@SuppressWarnings("unchecked")
			String ou = headersProcessor.validateAndExtractOrganisationUnit(
					(Map<String, List<String>>)message.get(Message.PROTOCOL_HEADERS));
			if (whiteOuList.contains(ou)) {
				if(LOG.isInfoEnabled()) LOG.info("Access granted for organisational unit: "+ ou);
			}

			@SuppressWarnings("unchecked")
			String email = headersProcessor.validateAndExtractEmailAddress(
					(Map<String, List<String>>)message.get(Message.PROTOCOL_HEADERS));

			if (email == null) {
				if(LOG.isInfoEnabled()) LOG.info("Access granted. No certificate indicates non-SSL gateway");
			}
			else {
				if (!whitelist.isUserAuthorised(email)) {
					if (!active) {
						LOG.error("Authorisation disabled. Access would have been denied to: " +
								"" + email + " for: " + getRequestInfo(message));
					} else {
						LOG.error("Access denied to: " + email + " for: " + getRequestInfo(message));
						requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
						return;
					}
				}
			}
		} catch (SSLCertificateParsingException e) {
			LOG.warn("Certificate validation failed.", e);

			if (active) {
				requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
				return;
			}

			else {
				if(LOG.isDebugEnabled())
					LOG.debug("Authorisation disabled, but certificate validation failed.");
			}
		}

		if(LOG.isDebugEnabled())
			LOG.debug("Access granted");
	}

	public void setWhiteOuList(List<String> whiteOuList) {
		this.whiteOuList = whiteOuList;
	}

}
