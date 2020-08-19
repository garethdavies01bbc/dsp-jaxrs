package bbc.forge.dsp.jaxrs;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.log4j.Logger;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.util.concurrent.atomic.AtomicInteger;

@ManagedResource
public class HttpRequestMonitor implements ContainerRequestFilter {
	private final Logger LOG = Logger.getLogger(this.getClass());

	private final AtomicInteger numberOfRequests = new AtomicInteger();

	@Override
	public void filter(ContainerRequestContext containerRequestContext) {

		Message message = PhaseInterceptorChain.getCurrentMessage();

		Object url = message.get(Message.REQUEST_URL);
		Object method = message.get(Message.HTTP_REQUEST_METHOD);
		Object contentType = message.get(Message.ACCEPT_CONTENT_TYPE);
		LOG.info("Request: " + method + "|" + url + "|" + contentType);

		numberOfRequests.incrementAndGet();
	}

	@ManagedAttribute(description = "valueType=COUNTER|kpiName=Number_of_requests")
	public AtomicInteger getNumberOfRequests() {
		return numberOfRequests;
	}
}