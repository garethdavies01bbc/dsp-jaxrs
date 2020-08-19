package bbc.forge.dsp.jaxrs;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.ResponseHandler;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.message.Message;

public class HttpResponseVersionProvider implements ResponseHandler {

	public static final String VERSION_HEADER = "X-API-Version";
	
	private String version;
	
	public HttpResponseVersionProvider(String version) {
		this.version = version;
	}

	@Override
	public Response handleResponse(Message message, OperationResourceInfo info, Response response) {
		return Response.fromResponse(response).header(VERSION_HEADER, version).build();
	}
	
}
