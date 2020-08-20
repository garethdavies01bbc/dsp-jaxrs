package bbc.forge.dsp.jaxrs;

import java.util.List;
import java.util.Map;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.log4j.Logger;

/**
 * As part of the BBC's caching policy, we do not want to return a 500 with a Cache-Control header if
 * it a revalidation request from mod-cache or another caching layer outside the application.
 *
 * To implement this as part of the HttpDataRepository would involve passing around too much
 * additional information. So, to focus this quite 'hacky' feature in one place, it
 * has been implemented within a ResponseHandler.
 *
 * https://confluence.dev.bbc.co.uk/display/prog2012/Caching+policy
 */
public class Caching500ErrorsForARevalidationPreventer implements ContainerRequestFilter, ContainerResponseFilter {

	private Logger LOG = Logger.getLogger(this.getClass());

	private ThreadLocal<Boolean> revalidationRequest = new ThreadLocal<Boolean>();

	@Override
	public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) {
		try {
			if (containerResponseContext.getStatus() > 499 && revalidationRequest.get() != null && revalidationRequest.get().booleanValue()) {
				LOG.info("Removing Cache-Control from 5xx response because this is a revalidation request");
				containerResponseContext.getHeaders().remove("Cache-Control");
			}
		} catch (Throwable t) {
			LOG.error("Error whilst checking for request header " +
					"If-None-Match and 5xx error and supplied Cache-Control", t);
		}
	}

	@Override
	public void filter(ContainerRequestContext containerRequestContext) {

		Message message = PhaseInterceptorChain.getCurrentMessage();

		try {
			revalidationRequest.set(false);

			if (((Map<String, List<String>>)message.get(Message.PROTOCOL_HEADERS))
					.containsKey("If-None-Match")) {
				LOG.info("Marking as revalidation request");
				revalidationRequest.set(true);
			}
		} catch(Throwable t) {
			LOG.error("Error whilst checking if revalidation request (If-None-Match is present)", t);
		}
	}
}
