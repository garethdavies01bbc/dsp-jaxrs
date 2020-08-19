package bbc.forge.dsp.jaxrs;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.message.Message;
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
public class Caching500ErrorsForARevalidationPreventer implements org.apache.cxf.jaxrs.ext.ResponseHandler, org.apache.cxf.jaxrs.ext.RequestHandler {

	private Logger LOG = Logger.getLogger(this.getClass());

	private ThreadLocal<Boolean> revalidationRequest = new ThreadLocal<Boolean>();

	@Override
	public Response handleResponse(Message message, OperationResourceInfo info,
			Response response) {
		try {
			if (response.getStatus() > 499 && revalidationRequest.get() != null && revalidationRequest.get().booleanValue()) {
				LOG.info("Removing Cache-Control from 5xx response because this is a revalidation request");
				return Response.fromResponse(response).cacheControl(null).build();
			}
		} catch(Throwable t) {
			LOG.error("Error whilst checking for request header " +
					"If-None-Match and 5xx error and supplied Cache-Control", t);
		}

		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response handleRequest(Message message, ClassResourceInfo info) {
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

		return null;
	}

}
