package bbc.forge.dsp.jaxrs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.internal.util.reflection.Whitebox.setInternalState;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import bbc.forge.dsp.common.security.SSLHeadersCertificateProcessor;
import bbc.forge.dsp.common.security.Whitelist;
import bbc.forge.dsp.mockito.MockitoTestBase;

public class HttpRequestProductionGatewatCertificateAuthTest extends MockitoTestBase {

	@Mock ClassResourceInfo info;
	@Mock Message message;
	@Mock Whitelist whitelist;

	private HttpRequestProductionGatewayCertificateAuth authRequest;
	
	@Before
	public void setUp() throws Exception {
		authRequest = createHttpRequestProductionGatewayCertificateAuth();
	}
	
	@Test
	public void handleRequest_if_authorised_should_return_null() throws Exception{
		when(whitelist.isUserAuthorised("email")).thenReturn(true);
		when(message.get(Message.REQUEST_URL)).thenReturn("http://www.bbc.co.uk");

		assertNull(authRequest.handleRequest(message, info));
	}

	@Test
	public void handleRequest_if_not_authorised_should_return_403() throws Exception{
		when(whitelist.isUserAuthorised("email")).thenReturn(false);
		when(message.get(Message.REQUEST_URL)).thenReturn("http://www.bbc.co.uk");

		Response response = authRequest.handleRequest(message, info);
		assertNotNull(response);
		assertEquals(403, response.getStatus());
	}

	@Test
	public void handleRequest_if_not_active_should_return_null() throws Exception{
		setInternalState(authRequest, "active", false);
		when(message.get(Message.REQUEST_URL)).thenReturn("http://www.bbc.co.uk");

		assertNull(authRequest.handleRequest(message, info));
	}

	@Test
	public void handleRequestNoProductionSSLEmailHeaderShouldReturnNull() throws Exception{
		SSLHeadersCertificateProcessor processor = mock(SSLHeadersCertificateProcessor.class);
		when(processor.validateAndExtractEmailAddressForProductionGatewayCertificate(null)).thenReturn(null);
		
		setInternalState(authRequest, "headersProcessor", processor);
		when(message.get(Message.REQUEST_URL)).thenReturn("http://www.bbc.co.uk");

		assertNull(authRequest.handleRequest(message, info));
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
