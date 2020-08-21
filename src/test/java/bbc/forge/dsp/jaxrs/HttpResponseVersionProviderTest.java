package bbc.forge.dsp.jaxrs;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.junit.Test;
import org.mockito.Mock;

import bbc.forge.dsp.mockito.MockitoTestBase;


public class HttpResponseVersionProviderTest extends MockitoTestBase {

	private static final String API_VERSION_NUMBER = "1.1.11";

	@Mock
	private ContainerResponseContext mockResponse;

	@Test
	public void handleResponseShouldAddXAPIVersionHeader() {
		HttpResponseVersionProvider versionProvider = new HttpResponseVersionProvider(API_VERSION_NUMBER);

		versionProvider.filter(null, mockResponse);

		assertEquals(API_VERSION_NUMBER, mockResponse.getHeaderString(HttpResponseVersionProvider.VERSION_HEADER));
	}

	@Test
	public void handleResponseShouldMaintainOriginalResponseValuesAndAddXAPIVersionHeader() {
		String responseEntity = "Response";
		mockResponse.setStatus(200);
		mockResponse.setEntity(responseEntity);

		HttpResponseVersionProvider versionProvider = new HttpResponseVersionProvider(API_VERSION_NUMBER);

		versionProvider.filter(null, mockResponse);

		assertEquals(200, mockResponse.getStatus());
		assertEquals(responseEntity, mockResponse.getEntity());
		assertEquals(API_VERSION_NUMBER, mockResponse.getHeaderString(HttpResponseVersionProvider.VERSION_HEADER));
	}
}
