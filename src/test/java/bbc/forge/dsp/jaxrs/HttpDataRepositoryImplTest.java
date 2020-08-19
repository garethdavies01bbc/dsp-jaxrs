package bbc.forge.dsp.jaxrs;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.util.reflection.Whitebox.setInternalState;

import java.net.URI;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import junit.framework.AssertionFailedError;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;

import bbc.forge.dsp.common.DataRepository;
import bbc.forge.dsp.common.Resource;
import bbc.forge.dsp.common.ResourceBuilder;
import bbc.forge.dsp.common.ResourceId;
import bbc.forge.dsp.common.ResourceNotFoundException;
import bbc.forge.dsp.descriptor.ResourceLocation;
import bbc.forge.dsp.descriptor.ResourceRequest;
import bbc.forge.dsp.descriptor.StringLocation;
import bbc.forge.dsp.descriptor.StringRequest;
import bbc.forge.dsp.mockito.MockitoTestBase;

public class HttpDataRepositoryImplTest extends MockitoTestBase {

	@Mock Request request;
	@Mock DataRepository dataRepository;
	@Mock JaxRsResponseHandler responseHandler;
	@Mock JaxRsConfiguration configuration;

	private HttpDataRepository httpDataRepository;
	private ResourceRequest resourceRequest;
	private ResourceLocation resourceLocation;

	private Response expectedResponse;

	@Before
	public void setUp() {
		httpDataRepository = new HttpDataRepositoryImpl();
		resourceRequest = StringRequest.create("key").accept("application/xml").build();
		resourceLocation = StringLocation.create("here").build();

		setInternalState(httpDataRepository, "dataRepository", dataRepository);
		setInternalState(httpDataRepository, "responseHandler", responseHandler);
		setInternalState(httpDataRepository, "configuration", configuration);

		when(configuration.getMaxAge404()).thenReturn(60);
		when(configuration.getMaxAge200()).thenReturn(30);

		expectedResponse = Response.ok().build();
	}

	@Test
	public void aNullResponseFromTheRepositoryAResourceNotFoundExceptionBeingHandled()
			throws Exception {
		when(dataRepository.retrieveResource(resourceRequest)).thenReturn(null);
		when(responseHandler.handleException(any(ResourceNotFoundException.class),
				eq(resourceRequest))).thenReturn(expectedResponse);

		Response response = httpDataRepository.retrieveResource(request, resourceRequest);

		assertEquals(expectedResponse, response);
	}

	@Test
	public void ifTheRepositoryThrowsAResourceNotFoundExceptionResultsIn()
	throws Exception {
		when(dataRepository.retrieveResource(resourceRequest)).thenThrow(new ResourceNotFoundException("Reason"));
		when(responseHandler.handleException(any(ResourceNotFoundException.class),
				eq(resourceRequest))).thenReturn(expectedResponse);

		Response response = httpDataRepository.retrieveResource(request, resourceRequest);

		assertEquals(expectedResponse, response);
	}

	@Test
	public void ifTheRepositoryReturnsAResourceThenHandleOkIsCalled()
			throws Exception {
		Resource resource = ResourceBuilder.create("resource-data").build();
		when(dataRepository.retrieveResource(resourceRequest)).thenReturn(resource);
		when(responseHandler.handleOK(request, resourceRequest, resource)).thenReturn(expectedResponse);

		Response response = httpDataRepository.retrieveResource(request, resourceRequest);

		assertEquals(expectedResponse, response);
	}

	@Test
	public void ifTextHtmlIsRequestedTheDefaultMimeTypeIsUsedIfProvided()
			throws Exception {
		String defaultMimeType = "default/default";
		resourceRequest = mock(ResourceRequest.class);

		when(configuration.getTextHtmlOverrideMimeType()).thenReturn(defaultMimeType);
		when(resourceRequest.getAccept()).thenReturn("text/html");
		when(dataRepository.retrieveResource(resourceRequest)).thenReturn(ResourceBuilder.create("resource-data", defaultMimeType).build());

		httpDataRepository.retrieveResource(request, resourceRequest);

		verify(resourceRequest).setAccept(defaultMimeType);
	}

	@Test
	public void ifAcceptAnyIsRequestedTheDefaultMimeTypeIsUsedIfProvided()
	throws Exception {
		String defaultMimeType = "default/default";
		resourceRequest = mock(ResourceRequest.class);

		when(configuration.getTextHtmlOverrideMimeType()).thenReturn(defaultMimeType);
		when(resourceRequest.getAccept()).thenReturn("*/*");
		when(dataRepository.retrieveResource(resourceRequest)).thenReturn(ResourceBuilder.create("resource-data", defaultMimeType).build());

		httpDataRepository.retrieveResource(request, resourceRequest);

		verify(resourceRequest).setAccept(defaultMimeType);
	}

	@Test
	public void ifTheAcceptHeaderIsBlankTheDefaultMimeTypeIsUsedIfProvided() throws Exception {
		String defaultMimeType = "default/default";
		resourceRequest = mock(ResourceRequest.class);

		when(configuration.getTextHtmlOverrideMimeType()).thenReturn(defaultMimeType);
		when(resourceRequest.getAccept()).thenReturn(null);
		when(dataRepository.retrieveResource(resourceRequest)).thenReturn(ResourceBuilder.create("resource-data", defaultMimeType).build());

		httpDataRepository.retrieveResource(request, resourceRequest);

		verify(resourceRequest).setAccept(defaultMimeType);
	}

	@Test
	public void handleCreatedIsCalledWhenCreateResourceSucceeds() throws Exception {
		final String content = "content";
		final String contentType = "text/plain";
		URI locationUri = new URI("/location/uri");

		when(dataRepository.createResource(
				eq(resourceLocation),
				argThat(new ResourceMatcher(content, contentType))))
					.thenReturn(mock(ResourceId.class));


		httpDataRepository.createResource(request, resourceLocation, content, contentType, locationUri);

		verify(responseHandler).handleCreated(locationUri);
	}

	@Test
	public void handleExceptionIsCalledWhenCreateResourceThrowsAnException() throws Exception {
		final String content = "content";
		final String contentType = "text/plain";
		URI locationUri = new URI("/location/uri");
		RuntimeException exception = new RuntimeException("error");

		when(dataRepository.createResource(
				eq(resourceLocation),
				argThat(new ResourceMatcher(content, contentType))))
					.thenThrow(exception);


		httpDataRepository.createResource(request, resourceLocation, content, contentType, locationUri);

		verify(responseHandler).handleException(exception, resourceLocation);
	}

	@Test
	public void handleUpdatedIsCalledWhenUpdateResourceSucceeds() {
		final String content = "content";
		final String contentType = "text/plain";

		httpDataRepository.updateResource(request, resourceLocation, content, contentType);

		verify(responseHandler).handle(Status.OK);
	}

	@Test
	public void handleExceptionIsCalledWhenUpdateResourceThrowsAnException() throws Exception {
		final String content = "content";
		final String contentType = "text/plain";
		RuntimeException exception = new RuntimeException("error");

		doThrow(exception).when(dataRepository).updateResource(
				eq(resourceLocation),
				argThat(new ResourceMatcher(content, contentType)));


		httpDataRepository.updateResource(request, resourceLocation, content, contentType);

		verify(responseHandler).handleException(exception, resourceLocation);
	}

	@Test
	public void handleCreateOrUpdatedIsCalledWhenCreateOrUpdateResourceSucceeds() throws Exception {
		final String content = "content";
		final String contentType = "text/plain";
		URI locationUri = new URI("/location/uri");

		doReturn(mock(ResourceId.class)).when(dataRepository).createOrUpdateResource(
				eq(resourceLocation),
				argThat(new ResourceMatcher(content, contentType)));


		httpDataRepository.createOrUpdateResource(request, resourceLocation, content, contentType, locationUri);

		verify(responseHandler).handleCreated(locationUri);
	}

	@Test
	public void handleExceptionIsCalledWhenCreateOrUpdateResourceThrowsAnException() throws Exception {
		final String content = "content";
		final String contentType = "text/plain";
		URI locationUri = new URI("/location/uri");
		RuntimeException exception = new RuntimeException("error");

		doThrow(exception).when(dataRepository).createOrUpdateResource(
				eq(resourceLocation),
				argThat(new ResourceMatcher(content, contentType)));


		httpDataRepository.createOrUpdateResource(request, resourceLocation, content, contentType, locationUri);

		verify(responseHandler).handleException(exception, resourceLocation);
	}

	@Test
	public void handleDeletedIsCalledWhenDeleteResourceSucceeds() {
		httpDataRepository.deleteResource(request, resourceLocation);

		verify(responseHandler).handle(Status.OK);
	}

	@Test
	public void handleExceptionIsCalledWhenDeleteResourceThrowsAnException() throws Exception {
		RuntimeException exception = new RuntimeException("error");

		doThrow(exception).when(dataRepository).deleteResource(
				eq(resourceLocation));


		httpDataRepository.deleteResource(request, resourceLocation);

		verify(responseHandler).handleException(exception, resourceLocation);
	}


	public class ResourceMatcher extends ArgumentMatcher<Resource> {

		private String content;
		private String contentType;

		private ResourceMatcher(String content, String contentType) {
			this.content = content;
			this.contentType = contentType;
		}

		public boolean matches(Object argument) {
			Resource resource = (Resource)argument;
			String actualContent = resource.getContent(String.class);
			String actualContentType = resource.getContentType();
			if (!actualContent.equals(content))
				throw new AssertionFailedError("The content " + actualContent +
						" did not match " + content);
			if (!actualContentType.equals(contentType))
				throw new AssertionFailedError("The content type " + actualContentType +
						" did not match " + contentType);
			return true;
		}

	}

}
