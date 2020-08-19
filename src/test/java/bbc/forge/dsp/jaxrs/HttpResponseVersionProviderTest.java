package bbc.forge.dsp.jaxrs;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.junit.Test;
import org.mockito.Mock;

import bbc.forge.dsp.mockito.MockitoTestBase;


public class HttpResponseVersionProviderTest extends MockitoTestBase {

	private static final String API_VERSION_NUMBER = "1.1.11";

	@Mock
	private Response response;

	@Test
	public void handleResponseShouldAddXAPIVersionHeader() {
		when(response.getMetadata()).thenReturn(new MetadataMap<String, Object>());

		HttpResponseVersionProvider versionProvider = new HttpResponseVersionProvider(API_VERSION_NUMBER);

		Response versionedResponse = versionProvider.handleResponse(null, null, response);
		assertEquals(API_VERSION_NUMBER, versionedResponse.getMetadata().get(HttpResponseVersionProvider.VERSION_HEADER).get(0));
	}

	@Test
	public void handleResponseShouldMaintainOriginalResponseValuesAndAddXAPIVersionHeader() {
		String responseEntity = "Response";
		when(response.getStatus()).thenReturn(200);
		when(response.getEntity()).thenReturn(responseEntity);
		when(response.getMetadata()).thenReturn(new MetadataMap<String, Object>());

		HttpResponseVersionProvider versionProvider = new HttpResponseVersionProvider(API_VERSION_NUMBER);

		Response versionedResponse = versionProvider.handleResponse(null, null, response);
		assertEquals(200, versionedResponse.getStatus());
		assertEquals(responseEntity, versionedResponse.getEntity());
		assertEquals(API_VERSION_NUMBER, versionedResponse.getMetadata().get(HttpResponseVersionProvider.VERSION_HEADER).get(0));
	}
}
