package bbc.forge.dsp.jaxrs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.internal.util.reflection.Whitebox.setInternalState;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import bbc.forge.dsp.common.security.SSLHeadersCertificateProcessor;
import bbc.forge.dsp.common.security.Whitelist;
import bbc.forge.dsp.mockito.MockitoTestBase;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

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
		authRequest = createHttpRequestProductionGatewayCertificateAuth();
		PowerMockito.mockStatic(PhaseInterceptorChain.class);
	}
	
//	@Test
//	public void handleRequest_if_authorised_should_return_null() throws Exception{
//		when(whitelist.isUserAuthorised("email")).thenReturn(true);
//		when(message.get(Message.REQUEST_URL)).thenReturn("http://www.bbc.co.uk");
//
//		assertNull(authRequest.filter(mockRequest, mockResponse));
//	}

	@Test
	public void handleRequest_if_not_authorised_should_return_403() throws Exception{
		when(whitelist.isUserAuthorised("email")).thenReturn(false);
		when(mockMessage.get(Message.REQUEST_URL)).thenReturn("http://www.bbc.co.uk");
		when(PhaseInterceptorChain.getCurrentMessage()).thenReturn(mockMessage);

		authRequest.filter(null, mockResponse);
		assertNotNull(mockResponse);
		assertEquals(403, mockResponse.getStatus());
	}

//	@Test
//	public void handleRequest_if_not_active_should_return_null() throws Exception{
//		setInternalState(authRequest, "active", false);
//		when(mockMessage.get(Message.REQUEST_URL)).thenReturn("http://www.bbc.co.uk");
//		when(PhaseInterceptorChain.getCurrentMessage()).thenReturn(mockMessage);
//
//		assertNull(authRequest.filter(mockRequest, mockResponse));
//	}

//	@Test
//	public void handleRequestNoProductionSSLEmailHeaderShouldReturnNull() throws Exception{
//		SSLHeadersCertificateProcessor processor = mock(SSLHeadersCertificateProcessor.class);
//		when(processor.validateAndExtractEmailAddressForProductionGatewayCertificate(null)).thenReturn(null);
//		when(PhaseInterceptorChain.getCurrentMessage()).thenReturn(mockMessage);
//
//		setInternalState(authRequest, "headersProcessor", processor);
//		when(mockMessage.get(Message.REQUEST_URL)).thenReturn("http://www.bbc.co.uk");
//
//		assertNull(authRequest.filter(mockRequest, mockResponse));
//	}

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
