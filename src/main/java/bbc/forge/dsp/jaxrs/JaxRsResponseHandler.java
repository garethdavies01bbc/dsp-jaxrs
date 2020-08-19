package bbc.forge.dsp.jaxrs;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.Date;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import bbc.forge.dsp.common.InvalidAcceptException;
import bbc.forge.dsp.common.InvalidApiConfigException;
import bbc.forge.dsp.common.InvalidContentException;
import bbc.forge.dsp.common.InvalidDescriptorException;
import bbc.forge.dsp.common.RepositoryFailureException;
import bbc.forge.dsp.common.Resource;
import bbc.forge.dsp.common.ResourceAlreadyExistsException;
import bbc.forge.dsp.common.ResourceModifiedException;
import bbc.forge.dsp.common.ResourceNotAvailableException;
import bbc.forge.dsp.common.ResourceNotFoundException;
import bbc.forge.dsp.common.ResourceNotUpdatedException;
import bbc.forge.dsp.descriptor.ResourceDescriptor;
import bbc.forge.dsp.descriptor.ResourceRequest;
import bbc.forge.dsp.validation.ValidationException;

import com.google.inject.Inject;

public class JaxRsResponseHandler {

	private static final String NOT_FOUND_MESSAGE = "Not found";
	private static final String NOT_AVAILABLE_MESSAGE = "Not available";

	private Logger LOG = Logger.getLogger(this.getClass());

	@Inject
	private ExceptionRenderer exceptionRenderer;

	@Inject
	private JaxRsConfiguration configuration;

	@Inject
	private JaxRsStats jaxRsStats;

	protected Response handleOK(Request request, ResourceRequest resourceRequest, Resource resource) {

		int maxAge200 = configuration.getMaxAge200();

		if (resourceRequest.getProfile().getMaxAge() != null) {
			maxAge200 = resourceRequest.getProfile().getMaxAge();
		}

		if(resource.getMaxAge() != null) {
			maxAge200 = resource.getMaxAge();
		}

		ResponseBuilder evaluatePreconditions = evaluatePreconditions(request, resource);
		if (evaluatePreconditions != null) {
			LOG.info("HTTP Preconditions not met: " + resourceRequest.toString());
			return evaluatePreconditions
					.cacheControl(createCacheControl(maxAge200))
					.build();
		}

		ResponseBuilder builder = Response.status(Status.OK)
				.cacheControl(createCacheControl(
						maxAge200))
				.entity(resource.getContent(Serializable.class));

		if(configuration.isVaryEnabled()){
			builder.header(HttpHeaders.VARY, resource.headersToVaryBy());
		}

		if (resource.getContentType() != null) {
			builder.type(resource.getContentType());
		} else {
			builder.type(resourceRequest.getAccept());
		}

		if (resource.getEntityTag() != null) {
			builder.tag(new EntityTag(resource.getEntityTag(), resource.isEntityTagWeak()));
		}
		if (resource.getLastModified() != null) {
			builder.lastModified(resource.getLastModified());
		}

		return builder.build();
	}

	public Response handleCreated(URI locationUri) {
		ResponseBuilder builder = Response.status(Status.CREATED);

		if (locationUri != null) {
			builder.location(locationUri);
		}

		return builder.build();
	}

	public Response handle(Status status) {
		return Response.status(status).build();
	}

	public Response handleWithMessage(Status status, String defaultMessage, int maxAge) {
		return handleWithMessage(status, null, defaultMessage, maxAge);
	}

	public Response handleWithMessage(Status status, Exception e, String defaultMessage) {
		return Response.status(status).entity(e == null || e.getMessage() == null ? defaultMessage : e.getMessage())
				.type(MediaType.TEXT_PLAIN).build();
	}

	public Response handleWithMessage(Status status, Exception e, String defaultMessage, int maxAge) {
		return Response.status(status).entity(e == null || e.getMessage() == null ? defaultMessage : e.getMessage())
			.cacheControl(createCacheControl(maxAge))
			.type(MediaType.TEXT_PLAIN).build();
	}

	public Response handleWithMessage(Status status, Exception e) {
		return handleWithMessage(status, e, null);
	}

	public Response handleWithStackTrace(Status status, Exception e) {
		ResponseBuilder responseBuilder = Response.status(status).entity(exceptionRenderer.render(e))
				.type(MediaType.TEXT_PLAIN);
		if(status.equals(Status.INTERNAL_SERVER_ERROR)){
			responseBuilder.cacheControl(createCacheControl(configuration.getMaxAge500()));
		}

		return responseBuilder.build();
	}

	public Response handleException(Exception exception, ResourceDescriptor descriptor) {
		String contextDescription = "API top-level";

		if (descriptor != null) {
			contextDescription = descriptor.toString();
		}

		try {
			throw exception;
		} catch (RepositoryFailureException e) {
			if (e.showStackTrace())
				LOG.error("Repository failure: " + contextDescription, e);
			else
				LOG.error("Repository failure: " + contextDescription);
			jaxRsStats.incrementRepositoryFailure();
			return handleWithStackTrace(Status.INTERNAL_SERVER_ERROR, e);
		} catch (ResourceNotFoundException e) {
			LOG.info("Resource not found: " + contextDescription);
			jaxRsStats.incrementResourceNotFound();
			int maxAge404 = configuration.getMaxAge404();

			if (descriptor !=null && descriptor instanceof ResourceRequest) {
				Integer requestMaxAge404 = ((ResourceRequest)descriptor).getProfile().get404MaxAge();
				if (requestMaxAge404 != null) {
					maxAge404 = requestMaxAge404;
				}
			}

			return handleWithMessage(Status.NOT_FOUND, e, NOT_FOUND_MESSAGE, maxAge404);
		} catch (ResourceModifiedException e) {
			LOG.warn("Resource has already been modified: " + contextDescription + "\n" + e.getMessage());
			jaxRsStats.incrementResourceModified();
			return handleWithMessage(Status.PRECONDITION_FAILED, e);
		} catch (ResourceNotAvailableException e) {
			LOG.warn("Resource not available: " + contextDescription, e);
			jaxRsStats.incrementResourceNotAvailable();
			return handleWithMessage(Status.SERVICE_UNAVAILABLE, e, NOT_AVAILABLE_MESSAGE, configuration.getMaxAge503());
		} catch (ResourceNotUpdatedException e) {
			LOG.warn("Resource not updated: " + contextDescription, e);
			jaxRsStats.incrementResourceAlreadyExists();
			return handleWithMessage(Status.CREATED, e);
		} catch (ResourceAlreadyExistsException e) {
			LOG.warn("Resource already exists: " + contextDescription, e);
			jaxRsStats.incrementResourceAlreadyExists();
			return handleWithMessage(Status.CONFLICT, e);
		} catch (InvalidAcceptException e) {
			LOG.warn("Invalid accept: " + contextDescription, e);
			return handleWithMessage(Status.NOT_ACCEPTABLE, e);
		} catch (InvalidContentException e) {
			LOG.warn("Invalid content: " + contextDescription, e);
			jaxRsStats.incrementInvalidContent();
			return handleWithMessage(Status.BAD_REQUEST, e);
		} catch (InvalidDescriptorException e) {
			LOG.warn("Invalid descriptor: " + contextDescription, e);
			jaxRsStats.incrementInvalidDescriptor();
			return handleWithStackTrace(Status.INTERNAL_SERVER_ERROR, e);
		} catch (InvalidApiConfigException e) {
			LOG.warn("Invalid API Config: " + contextDescription, e);
			jaxRsStats.incrementInvalidApiConfig();
			return handleWithStackTrace(Status.INTERNAL_SERVER_ERROR, e);
		} catch (IOException e) {
			LOG.warn("Invalid content (IO): " + contextDescription, e);
			jaxRsStats.incrementInvalidContent();
			return handleWithMessage(Status.BAD_REQUEST, e);
		} catch (ValidationException e) {
			LOG.warn("Validation failed: " + contextDescription, e);
			if (((ValidationException)e).isError())
				return handleWithStackTrace(Status.INTERNAL_SERVER_ERROR, e);
			return handleWithStackTrace(Status.BAD_REQUEST, e);
		} catch (IllegalArgumentException e) {
			LOG.warn("Illegal argument supplied: " + contextDescription, e);
			return handleWithStackTrace(Status.BAD_REQUEST, e);
		} catch(WebApplicationException e) {
			return e.getResponse();
		} catch (Exception e) {
			LOG.warn("Unknown exception: " + contextDescription, e);
			jaxRsStats.incrementInvalidDescriptor();
			return handleWithStackTrace(Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	private ResponseBuilder evaluatePreconditions(Request request, Resource resource) {
		String entityTag = resource.getEntityTag();
		Date lastModified = resource.getLastModified();

		if (entityTag == null && lastModified != null)
			return request.evaluatePreconditions(lastModified);
		if (entityTag != null && lastModified == null)
			return request.evaluatePreconditions(new EntityTag(entityTag, resource.isEntityTagWeak()));
		if (entityTag != null && lastModified != null)
			return request.evaluatePreconditions(lastModified, new EntityTag(entityTag, resource.isEntityTagWeak()));

		return null;
	}

	private CacheControl createCacheControl(final int maxAge) {
		CacheControl cacheControl = new CacheControl();
		cacheControl.setMaxAge(maxAge);
		cacheControl.setNoTransform(true);
		return cacheControl;
	}


}
