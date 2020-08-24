package bbc.forge.dsp.jaxrs;

import bbc.forge.dsp.mockito.MockitoTestBase;
import org.junit.Test;
import org.mockito.Mock;

import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MultivaluedMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HttpResponseVersionProviderTest extends MockitoTestBase {

	private static final String API_VERSION_NUMBER = "1.1.11";

	@Mock private ContainerResponseContext mockResponse;

	@Mock final MultivaluedMap<String, Object> mockHeadersMap = mock(MultivaluedMap.class);

	@Test
	public void handleResponseShouldAddXAPIVersionHeader() {
		when(mockResponse.getHeaders()).thenReturn(mockHeadersMap);
		HttpResponseVersionProvider versionProvider = new HttpResponseVersionProvider(API_VERSION_NUMBER);
		versionProvider.filter(null, mockResponse);
		verify(mockHeadersMap).add("X-API-Version", API_VERSION_NUMBER);
	}
}
