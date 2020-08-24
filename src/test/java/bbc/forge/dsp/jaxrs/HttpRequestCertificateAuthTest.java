package bbc.forge.dsp.jaxrs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.internal.util.reflection.Whitebox.setInternalState;

import java.util.ArrayList;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.mockito.Mock;

import bbc.forge.dsp.common.security.SSLHeadersCertificateProcessor;
import bbc.forge.dsp.common.security.Whitelist;
import bbc.forge.dsp.mockito.MockitoTestBase;

import com.google.common.collect.Lists;

public class HttpRequestCertificateAuthTest extends MockitoTestBase {

	@Mock ClassResourceInfo info;
	@Mock Message message;
	@Mock Whitelist whitelist;

	@Test
	public void handleRequest_if_authorised_should_return_null() throws Exception{
		when(whitelist.isUserAuthorised("email")).thenReturn(true);
		HttpRequestCertificateAuth authRequest = createHttpRequestCertificateAuth();
		when(message.get(Message.REQUEST_URL)).thenReturn("http://www.bbc.co.uk");

		assertNull(authRequest.handleRequest(message, info));
	}

	@Test
	public void handleRequest_if_authorised_by_ou_should_return_null() throws Exception{
		when(whitelist.isUserAuthorised("email")).thenReturn(false);
		HttpRequestCertificateAuth authRequest = createHttpRequestCertificateAuthForOu();
		when(message.get(Message.REQUEST_URL)).thenReturn("http://www.bbc.co.uk");

		assertNull(authRequest.handleRequest(message, info));
	}

	@Test
	public void handleRequest_if_not_authorised_should_return_403() throws Exception{
		when(whitelist.isUserAuthorised("email")).thenReturn(false);
		HttpRequestCertificateAuth authRequest = createHttpRequestCertificateAuth();
		when(message.get(Message.REQUEST_URL)).thenReturn("http://www.bbc.co.uk");

		Response response = authRequest.handleRequest(message, info);
		assertNotNull(response);
		assertEquals(403, response.getStatus());
	}

	@Test
	public void handleRequest_if_not_active_should_return_null() throws Exception{
		HttpRequestCertificateAuth authRequest = createHttpRequestCertificateAuth();
		setInternalState(authRequest, "active", false);
		when(message.get(Message.REQUEST_URL)).thenReturn("http://www.bbc.co.uk");

		assertNull(authRequest.handleRequest(message, info));
	}

	/**
	 * If the request comes from http://open.bbc.co.uk, for example, then no SSL
	 * client certificate will be supplied, so we should let this through.
	 */
	@Test
	public void handleRequestNoSSLEmailHeaderShouldReturnNull() throws Exception{
		HttpRequestCertificateAuth authRequest = new HttpRequestCertificateAuth();
		authRequest.setActive(true);

		SSLHeadersCertificateProcessor processor = mock(SSLHeadersCertificateProcessor.class);
		when(processor.validateAndExtractEmailAddress(null)).thenReturn(null);
		ArrayList<String> whiteOuList = Lists.newArrayList();
		setInternalState(authRequest, "headersProcessor", processor);
		setInternalState(authRequest, "whiteOuList", whiteOuList);

		when(message.get(Message.REQUEST_URL)).thenReturn("http://www.bbc.co.uk");

		assertNull(authRequest.handleRequest(message, info));
	}

	private HttpRequestCertificateAuth createHttpRequestCertificateAuth() throws Exception {
		HttpRequestCertificateAuth authRequest = new HttpRequestCertificateAuth();
		authRequest.setActive(true);

		SSLHeadersCertificateProcessor processor = mock(SSLHeadersCertificateProcessor.class);
		ArrayList<String> whiteOuList = Lists.newArrayList();
		when(processor.validateAndExtractEmailAddress(null)).thenReturn("email");
		setInternalState(authRequest, "headersProcessor", processor);
		setInternalState(authRequest, "whitelist", whitelist);
		setInternalState(authRequest, "whiteOuList", whiteOuList);
		return authRequest;
	}

	private HttpRequestCertificateAuth createHttpRequestCertificateAuthForOu() throws Exception {
		HttpRequestCertificateAuth authRequest = new HttpRequestCertificateAuth();
		authRequest.setActive(true);
		ArrayList<String> whiteOuList = Lists.newArrayList("organisational-unit");
		SSLHeadersCertificateProcessor processor = mock(SSLHeadersCertificateProcessor.class);
		when(processor.validateAndExtractEmailAddress(null)).thenReturn("email");
		when(processor.validateAndExtractOrganisationUnit(null)).thenReturn("organisational-unit");
		setInternalState(authRequest, "headersProcessor", processor);
		setInternalState(authRequest, "whitelist", whitelist);
		setInternalState(authRequest, "whiteOuList", whiteOuList);
		return authRequest;
	}


}
