package bbc.forge.dsp.jaxrs;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.internal.util.reflection.Whitebox.setInternalState;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.cxf.jaxrs.model.wadl.Description;
import org.apache.cxf.jaxrs.model.wadl.Descriptions;
import org.apache.cxf.jaxrs.model.wadl.DocTarget;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.mockito.Mockito;

import bbc.forge.dsp.common.DataRepository;
import bbc.forge.dsp.common.MimeTypes;
import bbc.forge.dsp.common.RepositoryFailureException;
import bbc.forge.dsp.common.Resource;
import bbc.forge.dsp.common.ResourceAlreadyExistsException;
import bbc.forge.dsp.common.ResourceBuilder;
import bbc.forge.dsp.common.ResourceModifiedException;
import bbc.forge.dsp.common.ResourceNotAvailableException;
import bbc.forge.dsp.common.ResourceNotFoundException;
import bbc.forge.dsp.descriptor.PrefixedLocationBuilder;
import bbc.forge.dsp.descriptor.ResourceLocation;
import bbc.forge.dsp.descriptor.ResourceRequest;
import bbc.forge.dsp.descriptor.StringLocation;
import bbc.forge.dsp.descriptor.StringRequest;
import bbc.forge.dsp.validation.ValidationException;
import bbc.forge.dsp.validation.ValidationException.VALIDATION_ERROR_TYPE;

@Path("")
public class ThingApi {

	private ConfigurableHttpDataRepository httpDataRepository;

	private DataRepository dataRepository;

	private ResourceRequest resourceRequest;

	private int INCREMENTED_NUMBER = 1;

	private ResourceLocation location;

	private Logger LOG = Logger.getLogger(this.getClass());

	public ThingApi(ConfigurableHttpDataRepository httpDataRepository) {
		this.httpDataRepository = httpDataRepository;
		dataRepository = mock(DataRepository.class);
		setInternalState(httpDataRepository, "dataRepository", dataRepository);
		httpDataRepository.initialise();
		resourceRequest = StringRequest.create("key").accept("application/xml").build();
		location = StringLocation.create("location").cacheable(false).build();
	}

	@Path("/nothing")
	@GET
	@Produces({ MediaType.TEXT_PLAIN })
	@Descriptions({ @Description(value = "A test API endpoint to obtain a 404", target = DocTarget.METHOD) })
	public Response getNothing(@Context
	Request request) throws Exception {

		// Mock out repo to return nothing
		Mockito.reset(dataRepository);
		when(dataRepository.retrieveResource(resourceRequest)).thenReturn(null);

		return httpDataRepository.retrieveResource(request, resourceRequest);
	}

	@Path("/thing")
	@GET
	@Produces({ MediaType.TEXT_PLAIN })
	@Descriptions({ @Description(value = "A test API endpoint to obtain some sample data", target = DocTarget.METHOD) })
	public Response getThing(@Context
	Request request) throws Exception {
		// Mock out repo to return data
		Mockito.reset(dataRepository);
		when(dataRepository.retrieveResource(resourceRequest)).thenReturn(
				ResourceBuilder.create("<resource-data>data</resource-data>").build());

		return httpDataRepository.retrieveResource(request, resourceRequest);
	}

	@Path("/thing/custom-headers-to-vary-by")
	@GET
	@Produces({ MediaType.TEXT_PLAIN })
	@Descriptions({ @Description(value = "A test API endpoint to obtain some sample data and add some custom headers to vary by",
			target = DocTarget.METHOD) })
	public Response getThingWithCustomHeadersToVaryBy(@Context
	Request request) throws Exception {
		// Mock out repo to return data
		Mockito.reset(dataRepository);

		ResourceBuilder resourceBuilder = ResourceBuilder.create("<resource-data>data</resource-data>");
		resourceBuilder.vary("X-Some-Custom-Header").vary("X-Another-Custom-Header");

		when(dataRepository.retrieveResource(resourceRequest)).thenReturn(resourceBuilder.build());
		return httpDataRepository.retrieveResource(request, resourceRequest);
	}

	@Path("repo-error")
	@GET
	@Produces({ MediaType.TEXT_PLAIN })
	@Descriptions({ @Description(value = "A test API endpoint to handle a repository error", target = DocTarget.METHOD) })
	public Response getRepositoryError(@Context
	Request request) throws Exception {
		// Mock out repo to return data
		Mockito.reset(dataRepository);
		when(dataRepository.retrieveResource(resourceRequest)).thenThrow(new RepositoryFailureException("fail"));

		return httpDataRepository.retrieveResource(request, resourceRequest);
	}

	@Path("unavailable")
	@GET
	@Produces({ MediaType.TEXT_PLAIN })
	@Descriptions({ @Description(value = "A test API endpoint to handle an unavailable resource", target = DocTarget.METHOD) })
	public Response getUnavailableResource(@Context
	Request request) throws Exception {
		// Mock out repo to return data
		Mockito.reset(dataRepository);
		when(dataRepository.retrieveResource(resourceRequest)).thenThrow(new ResourceNotAvailableException("unavailable"));

		return httpDataRepository.retrieveResource(request, resourceRequest);
	}

	@Path("validation-error")
	@GET
	@Produces({ MediaType.TEXT_PLAIN })
	@Descriptions({ @Description(value = "A test API endpoint to handle a validation error", target = DocTarget.METHOD) })
	public Response getValidationError(@Context
	Request request) throws ValidationException {
		throw new ValidationException(VALIDATION_ERROR_TYPE.INVALID_CONTENT, "Invalid!");
	}

	@Path("error")
	@GET
	@Produces({ MediaType.TEXT_PLAIN })
	@Descriptions({ @Description(value = "A test API endpoint to handle an error", target = DocTarget.METHOD) })
	public Response getError(@Context Request request) throws Exception {
		throw new Exception("Error!");
	}

	@Path("error-with-max-age")
	@GET
	@Produces({ MediaType.TEXT_PLAIN })
	@Descriptions({ @Description(value = "A test API endpoint to handle an error with a max-age", target = DocTarget.METHOD) })
	public Response getErrorWithMaxAge(@Context Request request) {
		CacheControl cacheControl = new CacheControl();
		cacheControl.setMaxAge(3);
		return Response.status(500).cacheControl(cacheControl).entity("Error!").type(MediaType.TEXT_PLAIN).build();
	}

	@Path("thing-that-changes-every-time")
	@GET
	@Produces({ MediaType.TEXT_PLAIN })
	@Descriptions({ @Description(value = "A test API endpoint to obtain some sample data", target = DocTarget.METHOD) })
	public Response getThingThatChangesEveryTime(@Context
	Request request) throws Exception {
		Mockito.reset(dataRepository);
		when(dataRepository.retrieveResource(resourceRequest)).thenReturn(
				ResourceBuilder.create("<resource-data>data</resource-data>").entityTag(INCREMENTED_NUMBER++ + "").build());

		return httpDataRepository.retrieveResource(request, resourceRequest);
	}

	@Path("thing-that-never-changes")
	@GET
	@Produces({ MediaType.TEXT_PLAIN })
	@Descriptions({ @Description(value = "A test API endpoint to obtain some sample data", target = DocTarget.METHOD) })
	public Response getThingThatNeverChanges(@Context
	Request request) throws Exception {
		Mockito.reset(dataRepository);
		when(dataRepository.retrieveResource(resourceRequest)).thenReturn(
				ResourceBuilder.create("<resource-data>data</resource-data>").entityTag("never-changes").build());

		return httpDataRepository.retrieveResource(request, resourceRequest);
	}

	@Path("thing-that-changed-in-2005")
	@GET
	@Produces({ MediaType.TEXT_PLAIN })
	@Descriptions({ @Description(value = "A test API endpoint to obtain some sample data", target = DocTarget.METHOD) })
	public Response getThingThatChangedIn2005(@Context
	Request request) throws Exception {
		Mockito.reset(dataRepository);
		when(dataRepository.retrieveResource(resourceRequest)).thenReturn(
				ResourceBuilder.create("<resource-data>data</resource-data>").lastModified(new DateTime(2005, 1, 1, 0, 0, 0).toDate()).build());

		return httpDataRepository.retrieveResource(request, resourceRequest);
	}

	@Path("/thing")
	@PUT
	@Descriptions({ @Description(value = "A test API endpoint to put some sample data", target = DocTarget.METHOD) })
	public Response putThing(@Context
			Request request, String content) throws Exception {
		Mockito.reset(dataRepository);
		when(dataRepository.createResource(eq(location), any(Resource.class))).thenReturn(null);

		return httpDataRepository.createResource(request, location, content, MimeTypes.APPLICATION_XML, UriBuilder.fromResource(ThingApi.class).path("/thing").build());
	}

	@Path("/thing-already-exists")
	@PUT
	@Descriptions({ @Description(value = "A test API endpoint to put some sample data that already exists", target = DocTarget.METHOD) })
	public Response putThingThatAlreadyExists(@Context
			Request request, String content) throws Exception {
		Mockito.reset(dataRepository);
		when(dataRepository.createResource(eq(location), any(Resource.class))).thenThrow(new ResourceAlreadyExistsException("Dupe!"));

		return httpDataRepository.createResource(request, location, content, MimeTypes.APPLICATION_XML, UriBuilder.fromResource(ThingApi.class).path("/thing").build());
	}

	@Path("/thing")
	@POST
	@Descriptions({ @Description(value = "A test API endpoint to post some sample data", target = DocTarget.METHOD) })
	public Response postThing(@Context
			Request request, String content) {
		Mockito.reset(dataRepository);

		return httpDataRepository.updateResource(request, location, content, MimeTypes.APPLICATION_XML);
	}

	@Path("/nothing")
	@POST
	@Descriptions({ @Description(value = "A test API endpoint to post some sample data for a non-existant resource", target = DocTarget.METHOD) })
	public Response postNothing(@Context
			Request request, String content) throws Exception {
		Mockito.reset(dataRepository);
		doThrow(new ResourceNotFoundException("Not found")).when(dataRepository).updateResource(eq(location), any(Resource.class));

		return httpDataRepository.updateResource(request, location, content, MimeTypes.APPLICATION_XML);
	}

	@Path("/thing-modified-at-start-of-2010")
	@POST
	@Descriptions({ @Description(value = "A test API endpoint to post some sample data for a resource modified at a particular date and time", target = DocTarget.METHOD) })
	public Response postModified(@Context
			Request request, String content,
			@HeaderParam("If-Match") String ifMatch
			) throws Exception {
		LOG.info("If Match HEADER: " + ifMatch);
		Mockito.reset(dataRepository);

		ResourceLocation location = new PrefixedLocationBuilder("")
			.create("thing-modified-at-start-of-2010").ifMatch(ifMatch).toLocation("");

		if (!ifMatch.equals("valid")){
			doThrow(new ResourceModifiedException("Modified!!")).when(dataRepository).updateResource(any(ResourceLocation.class), any(Resource.class));
		}
		return httpDataRepository.updateResource(request, location, content, MimeTypes.APPLICATION_XML);
	}

}
