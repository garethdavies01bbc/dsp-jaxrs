package bbc.forge.dsp.jaxrs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.util.reflection.Whitebox.setInternalState;

import java.net.URI;
import java.util.Date;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import bbc.forge.dsp.common.InvalidAcceptException;
import bbc.forge.dsp.common.InvalidApiConfigException;
import bbc.forge.dsp.common.InvalidContentException;
import bbc.forge.dsp.common.InvalidDescriptorException;
import bbc.forge.dsp.common.RepositoryFailureException;
import bbc.forge.dsp.common.Resource;
import bbc.forge.dsp.common.ResourceAlreadyExistsException;
import bbc.forge.dsp.common.ResourceBuilder;
import bbc.forge.dsp.common.ResourceModifiedException;
import bbc.forge.dsp.common.ResourceNotAvailableException;
import bbc.forge.dsp.common.ResourceNotFoundException;
import bbc.forge.dsp.common.ResourceNotUpdatedException;
import bbc.forge.dsp.common.utils.InvalidHttpDateException;
import bbc.forge.dsp.descriptor.StringRequest;
import bbc.forge.dsp.mockito.MockitoTestBase;
import bbc.forge.dsp.validation.ValidationException;
import bbc.forge.dsp.validation.ValidationException.VALIDATION_ERROR_TYPE;
import bbc.forge.dsp.xml.validation.ValidationSchemaFileException;

public class JaxRsResponseHandlerTest extends MockitoTestBase {

	@Mock ExceptionRenderer exceptionRenderer;
	@Mock JaxRsStats jaxRsStats;
	@Mock JaxRsConfiguration configuration;

	private JaxRsResponseHandler responseHandler;


	@Before
	public void setUp() {
		responseHandler = new JaxRsResponseHandler();

		when(exceptionRenderer.render(any(Exception.class))).thenReturn("default reason");

		setInternalState(responseHandler, "exceptionRenderer", exceptionRenderer);
		setInternalState(responseHandler, "configuration", configuration);
		setInternalState(responseHandler, "jaxRsStats", jaxRsStats);
		when(configuration.isVaryEnabled()).thenReturn(true);
		when(exceptionRenderer.render(any(Exception.class))).thenReturn("rendered");
	}

	@Test
	public void aRenderedExceptionHasAPlainTextContentType() {
		Exception exception = new Exception("Reason");

		Response response = responseHandler.handleException(exception, null);

		assertEquals("rendered", response.getEntity());
		assertEquals("text/plain", response.getMetadata().get("Content-Type").get(0));
	}

	@Test
	public void handlingAnUnknownExceptionResultsInA500() {
		when(configuration.getMaxAge500()).thenReturn(5);
		Response response = responseHandler.handleException(new Exception("Reason"), null);
		assertEquals(500, response.getStatus());
		assertEquals("no-transform,max-age=5", response.getMetadata().get("Cache-Control").get(0));
	}

	@Test
	public void handlingAValidationSchemaFileExceptionResultsInA500() {
		when(configuration.getMaxAge500()).thenReturn(5);
		Response response = responseHandler.handleException(
				new ValidationSchemaFileException(VALIDATION_ERROR_TYPE.INVALID_SCHEMA, "Reason"), null);

		assertEquals("no-transform,max-age=5", response.getMetadata().get("Cache-Control").get(0));
		assertEquals(500, response.getStatus());
	}

	@Test
	public void handlingAInvalidDescriptorExceptionResultsInA500() {
		when(configuration.getMaxAge500()).thenReturn(5);
		Response response = responseHandler.handleException(
				new InvalidDescriptorException("Reason"), null);

		assertEquals(500, response.getStatus());
		assertEquals("no-transform,max-age=5", response.getMetadata().get("Cache-Control").get(0));
	}

	@Test
	public void handlingAInvalidApiConfigExceptionResultsInA500() {
		when(configuration.getMaxAge500()).thenReturn(5);
		Response response = responseHandler.handleException(
				new InvalidApiConfigException("Reason"), null);

		assertEquals(500, response.getStatus());
		assertEquals("no-transform,max-age=5", response.getMetadata().get("Cache-Control").get(0));

		verify(jaxRsStats).incrementInvalidApiConfig();
	}

	@Test
	public void handlingAValidationExceptionResultsInA400() {
		Response response = responseHandler.handleException(
				new ValidationException(VALIDATION_ERROR_TYPE.INVALID_CONTENT, "Reason"), null);

		assertEquals(400, response.getStatus());
	}

	@Test
	public void handlingAWebApplicationExceptionReturnsTheContainedResponse() {
		Response expectedResponse = Response.ok().build();
		WebApplicationException exception = new WebApplicationException(expectedResponse);
		when(exceptionRenderer.render(exception)).thenReturn("the reason");

		Response response = responseHandler.handleException(exception, null);

		assertEquals(expectedResponse, response);
	}

	@Test
	public void handlingAnIllegalArgumentExceptionResultsInA400() {
		Response response = responseHandler.handleException(new IllegalArgumentException("error"), null);

		assertEquals(400, response.getStatus());
	}

	@Test
	public void handlingASubTypeOfIllegalArgumentExceptionResultsInA400() {
		Response response = responseHandler.handleException(new InvalidHttpDateException("error"), null);

		assertEquals(400, response.getStatus());
	}

	@Test
	public void handlingAnInvalidContentExceptionResultsInA400StatusCode() {
		Response response = responseHandler.handleException(new InvalidContentException("Error"), null);

		assertEquals(400, response.getStatus());
	}

	@Test
	public void handlingAResourceAlreadyExistsExceptionResultsInA409StatusCode() {
		Response response = responseHandler.handleException(new ResourceAlreadyExistsException("Error"), null);

		assertEquals(409, response.getStatus());
	}

	@Test
	public void handlingAResourceNotUpdatedExceptionResultsInA201StatusCodeWithInformationalMessage() {
		Response response = responseHandler.handleException(new ResourceNotUpdatedException("Error"), null);

		assertEquals(201, response.getStatus());
		assertEquals("Error", response.getEntity());
	}

	@Test
	public void handlingAResourceModifiedExceptionResultsInA412StatusCodeAndTheResourceModifiedStatIsIncremented() {
		Response response = responseHandler.handleException(new ResourceModifiedException("Error"), null);

		assertEquals(412, response.getStatus());
		verify(jaxRsStats).incrementResourceModified();
	}

	@Test
	public void handlingAResourceNotFoundExceptionResultsInA404WithCacheControlMaxAge60() {
		when(configuration.getMaxAge404()).thenReturn(60);

		Response response = responseHandler.handleException(new ResourceNotFoundException("error"), null);

		assertEquals(404, response.getStatus());
		assertEquals("no-transform,max-age=60", response.getMetadata().get("Cache-Control").get(0));
	}


	@Test
	public void handlingAResourceNotFoundExceptionResultsInA404WithCacheControlSetAsPerRequestProfile() {
		when(configuration.getMaxAge404()).thenReturn(60);

		Response response = responseHandler.handleException(new ResourceNotFoundException("error"),
				StringRequest
					.create("descriptor")
					.cacheKey("key")
					.accept("accept")
					.maxAge404(1234)
					.build());

		assertEquals(404, response.getStatus());
		assertEquals("no-transform,max-age=1234", response.getMetadata().get("Cache-Control").get(0));
	}

	@Test
	public void handlingResultsInA500ShouldHaveMaxAgeTo5seconds() {
		when(configuration.getMaxAge500()).thenReturn(5);

		Response response = responseHandler.handleException(new RuntimeException("error"),
				StringRequest
					.create("descriptor")
					.cacheKey("key")
					.accept("accept")
					.build());

		assertEquals(500, response.getStatus());
		assertEquals("no-transform,max-age=5", response.getMetadata().get("Cache-Control").get(0));
	}

	@Test
	public void handlingAResourceNotAvailableExceptionResultsInA503WithNoTransformAndMaxAgeZeroAndNotAvailableStatIsIncremented() {
		Response response = responseHandler.handleException(new ResourceNotAvailableException("error"), null);

		assertEquals(503, response.getStatus());
		assertEquals("no-transform,max-age=0", response.getMetadata().get("Cache-Control").get(0));

		verify(jaxRsStats).incrementResourceNotAvailable();
	}


	@Test
	public void handlingAResourceNotAvailableExceptionChangesTheMaxAgeIfSet() {
		when(configuration.getMaxAge503()).thenReturn(20);

		Response response = responseHandler.handleException(new ResourceNotAvailableException("error"), null);

		assertEquals("no-transform,max-age=20", response.getMetadata().get("Cache-Control").get(0));
	}

	@Test
	public void handlingAResourceNotAvailableExceptionWithNoMessageResultsInADefaultMessageInTheResponse() {
		when(configuration.getMaxAge503()).thenReturn(20);

		Response response = responseHandler.handleException(
				new ResourceNotAvailableException((String) null), null);

		assertEquals("Not available", response.getEntity());
	}

	@Test
	public void handlingAInvalidDescriptorExceptionResultsInA500AndInvalidDescriptorStatIsIncremented() {
		when(configuration.getMaxAge500()).thenReturn(5);
		Response response = responseHandler.handleException(
				new InvalidDescriptorException("Reason"), null);

		assertEquals(500, response.getStatus());
		assertEquals("no-transform,max-age=5", response.getMetadata().get("Cache-Control").get(0));
		verify(jaxRsStats).incrementInvalidDescriptor();
	}


	@Test
	public void handlingAnInvalidAcceptExceptionResultsInA406() {
		Response response = responseHandler.handleException(
				new InvalidAcceptException("Reason"), null);

		assertEquals(406, response.getStatus());

	}

	@Test
	public void handlingARepositoryFailureExceptionResultsInA500AndRepositoryFailureStatIsIncremented() {
		when(configuration.getMaxAge500()).thenReturn(5);
		Response response =  responseHandler.handleException(new RepositoryFailureException("Reason"), null);

		assertEquals(500, response.getStatus());
		assertEquals("no-transform,max-age=5", response.getMetadata().get("Cache-Control").get(0));
		verify(jaxRsStats).incrementRepositoryFailure();
	}

	@Test
	public void handlingAnOKResultsInA200ResponseContainingResourceDataWithNoTransformConfigurationSettingsAndAcceptAsContentTypeAndVaryAccept() {
		Resource resource = ResourceBuilder.create("resource-data").build();
		StringRequest resourceRequest = StringRequest.create("request").accept("text/plain").build();

		when(configuration.getMaxAge200()).thenReturn(30);

		Response response = responseHandler.handleOK(null, resourceRequest, resource);

		assertEquals(200, response.getStatus());
		assertEquals("resource-data", response.getEntity());
		assertEquals("no-transform,max-age=30",
				response.getMetadata().get("Cache-Control").get(0));
		assertEquals("Accept", response.getMetadata().get("Vary").get(0));
		assertEquals(resourceRequest.getAccept(), response.getMetadata().get("Content-Type").get(0));
	}

	@Test
	public void handlingAnOKResultsInA200ResponseWithoutVaryHeaderIfItIsDisabled() {
		when(configuration.isVaryEnabled()).thenReturn(false);
		Resource resource = ResourceBuilder.create("resource-data").build();
		StringRequest resourceRequest = StringRequest.create("request").accept("text/plain").build();

		when(configuration.getMaxAge200()).thenReturn(30);

		Response response = responseHandler.handleOK(null, resourceRequest, resource);

		assertEquals(200, response.getStatus());
		assertEquals("resource-data", response.getEntity());
		assertEquals("no-transform,max-age=30",
				response.getMetadata().get("Cache-Control").get(0));
		assertNull(response.getMetadata().get("Vary"));
		assertEquals(resourceRequest.getAccept(), response.getMetadata().get("Content-Type").get(0));
	}

	@Test
	public void handlingAnOKWithAResourceContainingCustomVaryHeadersPassesTheseHeadersThrough() {
		Resource resource = ResourceBuilder
			.create("resource-data")
			.vary("X-Some-Custom-Header")
			.vary("X-Another-Custom-Header")
			.build();
		StringRequest resourceRequest = StringRequest.create("request").accept("text/plain").build();

		Response response = responseHandler.handleOK(null, resourceRequest, resource);

		assertEquals("Accept,X-Some-Custom-Header,X-Another-Custom-Header", response.getMetadata().get("Vary").get(0));
	}

	@Test
	public void handlingAnOKWithAResourceWithAContentTypeResultsInContentTypeHeader() {
		Resource resource = ResourceBuilder.create("resource-data", "text/html").build();
		StringRequest resourceRequest = StringRequest.create("request").accept("text/plain").build();

		Response response = responseHandler.handleOK(null, resourceRequest, resource);

		assertEquals("text/html", response.getMetadata().get("Content-Type").get(0));
	}

	@Test
	public void handlingAnOKWhenMaxAge200HasBeenConfiguredResultsInMaxAgeHeaderBeingSet() {
		Resource resource = ResourceBuilder.create("resource-data").build();
		StringRequest resourceRequest = StringRequest.create("request").accept("text/plain").build();

		when(configuration.getMaxAge200()).thenReturn(123);

		Response response = responseHandler.handleOK(null, resourceRequest, resource);

		assertEquals("no-transform,max-age=123", response.getMetadata().get("Cache-Control").get(0));
	}

	@Test
	public void handlingAnOKWhenMaxAge200HasBeenProvidedInTheRequestProfileResultsInMaxAgeHeaderBeingSet() {
		Resource resource = ResourceBuilder.create("resource-data").build();
		StringRequest resourceRequest = StringRequest.create("request").accept("text/plain").maxAge(321).build();

		when(configuration.getMaxAge200()).thenReturn(123);

		Response response = responseHandler.handleOK(null, resourceRequest, resource);

		assertEquals("no-transform,max-age=321", response.getMetadata().get("Cache-Control").get(0));
	}
	
	@Test
	public void handlingAnOKWhenMaxAge200HasBeenProvidedInTheResourceResultsInMaxAgeHeaderBeingSet() {
		Request request = mock(Request.class);
		Resource resource = ResourceBuilder
			.create("resource-data")
			.entityTag("etag")
			.maxAge(120)
			.build();
		StringRequest resourceRequest = StringRequest.create("request").accept("text/plain").build();

		when(configuration.getMaxAge200()).thenReturn(123);
		Response response = responseHandler.handleOK(request, resourceRequest, resource);

		assertEquals("no-transform,max-age=120", response.getMetadata().get("Cache-Control").get(0));
	}
	
	@Test
	public void handlingAnOKWhenMaxAge200HasBeenProvidedInTheResourceOverridesRequestProfileMaxAgeResultingInMaxAgeHeaderBeingSet() {
		Request request = mock(Request.class);
		Resource resource = ResourceBuilder
			.create("resource-data")
			.entityTag("etag")
			.maxAge(120)
			.build();
		StringRequest resourceRequest = StringRequest.create("request").accept("text/plain").maxAge(321).build();

		when(configuration.getMaxAge200()).thenReturn(123);
		Response response = responseHandler.handleOK(request, resourceRequest, resource);

		assertEquals("no-transform,max-age=120", response.getMetadata().get("Cache-Control").get(0));
	}

	@Test
	public void handlingAnOKWithAResourceWithAnETagWhenThePreconditionsFailEvaluationWithTheETagThenA200IsReturnedWithTheETagHeader() {
		Request request = mock(Request.class);

		Resource resource = ResourceBuilder
			.create("resource-data")
			.entityTag("etag")
			.build();
		StringRequest resourceRequest = StringRequest.create("request").accept("text/plain").build();

		when(request.evaluatePreconditions(new EntityTag("etag"))).thenReturn(null);

		Response response = responseHandler.handleOK(request, resourceRequest, resource);

		assertEquals(200, response.getStatus());
		assertEquals("\"etag\"", response.getMetadata().get("ETag").get(0));
	}

	@Test
	public void handlingAnOKWithAResourceWithAnETagWhenThePreconditionsSucceedEvaluationWithTheETagThenTheEvaluatedResponseReturned() {
		Request request = mock(Request.class);

		Resource resource = ResourceBuilder
			.create("resource-data")
			.entityTag("etag")
			.build();
		StringRequest resourceRequest = StringRequest.create("request").accept("text/plain").build();

		when(request.evaluatePreconditions(new EntityTag("etag"))).thenReturn(Response.status(123));

		Response response = responseHandler.handleOK(request, resourceRequest, resource);

		assertEquals(123, response.getStatus());
	}

	@Test
	public void handlingAnOKWithAResourceWithAWeakETagWhenThePreconditionsSucceedEvaluationWithTheWeakETagThenTheEvaluatedResponseReturned() {
		Request request = mock(Request.class);

		Resource resource = ResourceBuilder
			.create("resource-data")
			.entityTag("etag")
			.weakEntityTag(true)
			.build();
		StringRequest resourceRequest = StringRequest.create("request").accept("text/plain").build();

		when(request.evaluatePreconditions(new EntityTag("etag", true))).thenReturn(Response.status(123));

		Response response = responseHandler.handleOK(request, resourceRequest, resource);

		assertEquals(123, response.getStatus());
	}

	@Test
	public void handlingAnOKWithAResourceWithALastModifiedDateWhenThePreconditionsSucceedEvaluationWithTheLastModifiedDateThenTheEvaluatedResponseReturned() {
		Date lastModified = new Date();
		Request request = mock(Request.class);

		Resource resource = ResourceBuilder
			.create("resource-data")
			.lastModified(lastModified)
			.build();
		StringRequest resourceRequest = StringRequest.create("request").accept("text/plain").build();

		when(request.evaluatePreconditions(lastModified)).thenReturn(Response.status(123));

		Response response = responseHandler.handleOK(request, resourceRequest, resource);

		assertEquals(123, response.getStatus());
	}

	@Test
	public void handlingAnOKWithAResourceWithALastModifiedDateAndEntityTagWhenThePreconditionsSucceedEvaluationWithBothTheEvaluatedResponseReturned() {
		Date lastModified = new Date();
		Request request = mock(Request.class);

		Resource resource = ResourceBuilder
			.create("resource-data")
			.entityTag("etag")
			.lastModified(lastModified)
			.build();
		StringRequest resourceRequest = StringRequest.create("request").accept("text/plain").build();

		when(request.evaluatePreconditions(lastModified, new EntityTag("etag"))).thenReturn(Response.status(123));

		Response response = responseHandler.handleOK(request, resourceRequest, resource);

		assertEquals(123, response.getStatus());
	}

	@Test
	public void handlingCreatedResultsInA201ReturnedWithTheLocationHeader()
			throws Exception {
		URI locationUri = new URI("http://location.co.uk");

		Response response = responseHandler.handleCreated(locationUri);

		assertEquals(201, response.getStatus());
		assertEquals(locationUri.toString(), response.getMetadata().get("Location").get(0));
	}

	@Test
	public void handlingCreatedResultsInA201ReturnedWithANullLocationHeader() {
		Response response = responseHandler.handleCreated(null);

		assertEquals(201, response.getStatus());
		assertNull(response.getMetadata().get("Location"));
	}

}
