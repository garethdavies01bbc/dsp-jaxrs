package bbc.forge.dsp.jaxrs;

import bbc.forge.dsp.common.security.SSLHeadersCertificateProcessor;
import bbc.forge.dsp.common.security.Whitelist;
import bbc.forge.dsp.mockito.MockitoTestBase;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.internal.util.reflection.Whitebox.setInternalState;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PhaseInterceptorChain.class)
public class HttpRequestProductionGatewatCertificateAuthTest extends MockitoTestBase {

	@Mock ClassResourceInfo info;
	@Mock Message mockMessage;
	@Mock Whitelist whitelist;
	@Mock private ContainerRequestContext mockRequest;

	@Mock private ContainerResponseContext mockResponse;

	private HttpRequestProductionGatewayCertificateAuth authRequest;
	
	@Before
	public void setUp() throws Exception {
		PowerMockito.mockStatic(PhaseInterceptorChain.class);
		when(PhaseInterceptorChain.getCurrentMessage()).thenReturn(mockMessage);

		authRequest = createHttpRequestProductionGatewayCertificateAuth();
	}
	
	@Test
	public void shouldNotAbortRequestIfUserAuthorised() throws Exception{
		when(whitelist.isUserAuthorised("email")).thenReturn(true);
		when(mockMessage.get(Message.REQUEST_URL)).thenReturn("http://www.bbc.co.uk");

		authRequest.filter(mockRequest);

		verify(mockRequest, never()).abortWith(Matchers.<Response>any());
	}

	@Test
	public void shouldAbortWith403IfUserNotAuthorised() throws Exception {
		when(whitelist.isUserAuthorised("email")).thenReturn(false);
		when(mockMessage.get(Message.REQUEST_URL)).thenReturn("http://www.bbc.co.uk");

		ArgumentCaptor<Response> argument = ArgumentCaptor.forClass(Response.class);
		authRequest.filter(mockRequest);

		verify(mockRequest).abortWith(argument.capture());
		assertEquals(403, argument.getValue().getStatus());
	}

	@Test
	public void shouldNotAbortRequestIfNotActive() throws Exception{
		setInternalState(authRequest, "active", false);
		when(mockMessage.get(Message.REQUEST_URL)).thenReturn("http://www.bbc.co.uk");

		authRequest.filter(mockRequest);

		verify(mockRequest, never()).abortWith(Matchers.<Response>any());
	}

	@Test
	public void shouldNotAbortRequestIfNoProductionSSLEmailHeader() throws Exception {
		SSLHeadersCertificateProcessor processor = mock(SSLHeadersCertificateProcessor.class);
		when(processor.validateAndExtractEmailAddressForProductionGatewayCertificate(null)).thenReturn(null);
		setInternalState(authRequest, "headersProcessor", processor);
		when(mockMessage.get(Message.REQUEST_URL)).thenReturn("http://www.bbc.co.uk");

		authRequest.filter(mockRequest);

		verify(mockRequest, never()).abortWith(Matchers.<Response>any());
	}

	private HttpRequestProductionGatewayCertificateAuth createHttpRequestProductionGatewayCertificateAuth() throws Exception {
		HttpRequestProductionGatewayCertificateAuth authRequest = new HttpRequestProductionGatewayCertificateAuth();
		authRequest.setActive(true);

		SSLHeadersCertificateProcessor processor = mock(SSLHeadersCertificateProcessor.class);
		when(processor.validateAndExtractEmailAddressForProductionGatewayCertificate(null)).thenReturn("email");
		setInternalState(authRequest, "headersProcessor", processor);
		setInternalState(authRequest, "whitelist", whitelist);
		return authRequest;
	}
}
