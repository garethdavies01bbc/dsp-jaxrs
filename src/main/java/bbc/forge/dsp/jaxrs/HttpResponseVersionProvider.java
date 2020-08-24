package bbc.forge.dsp.jaxrs;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

public class HttpResponseVersionProvider implements ContainerResponseFilter {

	public static final String VERSION_HEADER = "X-API-Version";
	
	private String version;
	
	public HttpResponseVersionProvider(String version) {
		this.version = version;
	}

	@Override
	public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) {
		containerResponseContext.getHeaders().add(VERSION_HEADER, version);
	}
}
