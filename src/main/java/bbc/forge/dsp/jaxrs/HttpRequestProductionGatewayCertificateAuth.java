package bbc.forge.dsp.jaxrs;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.apache.log4j.Logger;

import bbc.forge.dsp.common.security.SSLCertificateParsingException;

/**
 * This filter will authorise access to production gateway applications only to people:
 * - populate the sslproductionclientcertsubject with the originating client cert
 * - who have a valid BBC certificate.
 * - are listed in the list of the users authorised for this application
 * - The filter can be disabled (active=false) for sandbox and integration.
 *
 * Most of the time, you should enumerate the list of authorised users
 * in the /etc/bbc/authorisation/your-application/ folder.
 */
public class HttpRequestProductionGatewayCertificateAuth extends AbstractHttpRequestCertificateAuth {

	private Logger LOG = Logger.getLogger(this.getClass());

	@Override
	@SuppressWarnings("unchecked")
	public Response handleRequest(Message message, ClassResourceInfo info) {
		String email = null;
		try {
			email = headersProcessor.validateAndExtractEmailAddressForProductionGatewayCertificate(
					(Map<String, List<String>>)message.get(Message.PROTOCOL_HEADERS));

			if (email == null) {
				if(LOG.isInfoEnabled())
					LOG.info("Access granted. No certificate indicates non-production gateway");
				return null;
			}

			if (!whitelist.isUserAuthorised(email)) {
				if (!active) {
					LOG.error("Authorisation disabled. Access would have been denied to: " +
							"" + email + " for: " + getRequestInfo(message));
				} else {
					LOG.error("Access denied to: " + email + " for: " + getRequestInfo(message));
					return Response.status(403).build();
				}
			}
		} catch (SSLCertificateParsingException e) {
			LOG.warn("Certificate validation failed.", e);

			if (active)	return Response.status(403).build();
			else {
				if(LOG.isDebugEnabled())
					LOG.debug("Authorisation disabled, but certificate validation failed.");
			}
		}

		if(LOG.isDebugEnabled())
			LOG.debug("Access granted to: " + email + " for: "+ getRequestInfo(message));

		return null;
	}

	
}
