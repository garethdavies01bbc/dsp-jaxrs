package bbc.forge.dsp.jaxrs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.internal.util.reflection.Whitebox.setInternalState;

import java.util.ArrayList;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;

import bbc.forge.dsp.common.security.SSLHeadersCertificateProcessor;
import bbc.forge.dsp.common.security.Whitelist;
import bbc.forge.dsp.mockito.MockitoTestBase;

import com.google.common.collect.Lists;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PhaseInterceptorChain.class)
public class HttpRequestCertificateAuthTest extends MockitoTestBase {

	@Mock ClassResourceInfo info;
	@Mock Message mockMessage;
	@Mock Whitelist whitelist;
	@Mock private ContainerRequestContext mockRequest;

	@Before
	public void setUp() throws Exception {
		PowerMockito.mockStatic(PhaseInterceptorChain.class);
		when(PhaseInterceptorChain.getCurrentMessage()).thenReturn(mockMessage);
	}

	@Test
	public void shouldNotAbortRequestIfUserAuthorised() throws Exception{
		when(whitelist.isUserAuthorised("email")).thenReturn(true);
		HttpRequestCertificateAuth authRequest = createHttpRequestCertificateAuth();
		when(mockMessage.get(Message.REQUEST_URL)).thenReturn("http://www.bbc.co.uk");

		authRequest.filter(mockRequest);

		verify(mockRequest, never()).abortWith(Matchers.<Response>any());
	}

	@Test
	public void shouldNotAbortRequestIfUserAuthorisedByOu() throws Exception{
		when(whitelist.isUserAuthorised("email")).thenReturn(false);
		HttpRequestCertificateAuth authRequest = createHttpRequestCertificateAuthForOu();
		when(mockMessage.get(Message.REQUEST_URL)).thenReturn("http://www.bbc.co.uk");

		authRequest.filter(mockRequest);

		verify(mockRequest, never()).abortWith(Matchers.<Response>any());
	}

	@Test
	public void shouldAbortWith403IfUserNotAuthorised() throws Exception{
		when(whitelist.isUserAuthorised("email")).thenReturn(false);
		HttpRequestCertificateAuth authRequest = createHttpRequestCertificateAuth();
		when(mockMessage.get(Message.REQUEST_URL)).thenReturn("http://www.bbc.co.uk");

		ArgumentCaptor<Response> argument = ArgumentCaptor.forClass(Response.class);
		authRequest.filter(mockRequest);

		verify(mockRequest).abortWith(argument.capture());
		assertEquals(403, argument.getValue().getStatus());
	}

	@Test
	public void shouldNotAbortRequestIfNotActive() throws Exception{
		HttpRequestCertificateAuth authRequest = createHttpRequestCertificateAuth();
		setInternalState(authRequest, "active", false);
		when(mockMessage.get(Message.REQUEST_URL)).thenReturn("http://www.bbc.co.uk");

		authRequest.filter(mockRequest);

		verify(mockRequest, never()).abortWith(Matchers.<Response>any());
	}

	/**
	 * If the request comes from http://open.bbc.co.uk, for example, then no SSL
	 * client certificate will be supplied, so we should let this through.
	 */
	@Test
	public void shouldNotAbortRequestIfNoSSLEmailHeader() throws Exception{
		HttpRequestCertificateAuth authRequest = new HttpRequestCertificateAuth();
		authRequest.setActive(true);

		SSLHeadersCertificateProcessor processor = mock(SSLHeadersCertificateProcessor.class);
		when(processor.validateAndExtractEmailAddress(null)).thenReturn(null);
		ArrayList<String> whiteOuList = Lists.newArrayList();
		setInternalState(authRequest, "headersProcessor", processor);
		setInternalState(authRequest, "whiteOuList", whiteOuList);

		when(mockMessage.get(Message.REQUEST_URL)).thenReturn("http://www.bbc.co.uk");

		verify(mockRequest, never()).abortWith(Matchers.<Response>any());
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
