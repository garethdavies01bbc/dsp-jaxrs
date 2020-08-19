package bbc.forge.dsp.jaxrs;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.google.inject.Inject;

@Provider
public class JaxRsExceptionHandler implements ExceptionMapper<Exception> {

	@Inject
	private JaxRsResponseHandler responseHandler;

	public Response toResponse(Exception exception) {
		return responseHandler.handleException(exception, null);
	}

}
