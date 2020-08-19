package bbc.forge.dsp.jaxrs;

import bbc.forge.dsp.annotation.Performance;
import bbc.forge.dsp.common.*;
import bbc.forge.dsp.descriptor.ResourceDescriptor;
import bbc.forge.dsp.descriptor.ResourceLocation;
import bbc.forge.dsp.descriptor.ResourceRequest;
import com.google.inject.Inject;
import org.apache.log4j.Logger;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.URI;

public class HttpDataRepositoryImpl implements HttpDataRepository {

	private Logger LOG = Logger.getLogger(this.getClass());

	@Inject
	private DataRepository dataRepository;

	@Inject
	private JaxRsResponseHandler responseHandler;

	@Inject
	private JaxRsConfiguration configuration;

	@Performance
	public Response retrieveResource(Request request, ResourceRequest resourceRequest) {
		try {
			LOG.info("Requested: " + resourceRequest.toString());
			if((resourceRequest.getAccept() == null ||
				resourceRequest.getAccept().equals(MimeTypes.TEXT_HTML) ||
				resourceRequest.getAccept().equals(MimeTypes.ANY)) &&
				configuration.getTextHtmlOverrideMimeType() != null){

				resourceRequest.setAccept(configuration.getTextHtmlOverrideMimeType());
			}
			Resource resource = dataRepository.retrieveResource(resourceRequest);
			if (resource == null) {
				throw new ResourceNotFoundException("Not found: " + resourceRequest.toString());
			} else {
				LOG.info("Found: " + resourceRequest.toString());
				return responseHandler.handleOK(request, resourceRequest, resource);
			}
		}
		catch (Exception e) {
			return responseHandler.handleException(e, resourceRequest);
		}
	}

	@Performance
	public Response createResource(Request request, ResourceLocation location, String content, String contentType, URI locationUri) {
		try {
			LOG.debug("Creating: " + location + "\n" + content);

			dataRepository.createResource(location, ResourceBuilder.create(content, contentType).build());
			return responseHandler.handleCreated(locationUri);
		} catch (Exception e) {
			return responseHandler.handleException(e, location);
		}
	}

	@Performance
	public Response createOrUpdateResource(Request request,
			ResourceLocation location, String content, String contentType, URI locationURI) {
		try {
			LOG.debug("Create or updating: " + location + "\n" + content);

			dataRepository.createOrUpdateResource(location, ResourceBuilder.create(content, contentType).build());
			return responseHandler.handleCreated(locationURI);
		} catch (Exception e) {
			return responseHandler.handleException(e, location);
		}
	}

	@Performance
	public Response updateResource(Request request, ResourceLocation location, String content, String contentType) {
		try {
			LOG.debug("Updating: " + location + "\n" + content);

			dataRepository.updateResource(location, ResourceBuilder.create(content, contentType).build());
			return responseHandler.handle(Status.OK);
		} catch (Exception e) {
			return responseHandler.handleException(e, location);
		}
	}

	@Performance
	public Response deleteResource(Request request, ResourceLocation location) {

        // to see what kind of resourcelocation we have in order to perform the right task

		try {
			LOG.debug("Deleting: " + location);

			dataRepository.deleteResource(location);
			return responseHandler.handle(Status.OK);
		} catch (Exception e) {
			return responseHandler.handleException(e, location);
		}
	}

	@Override
	public Response handleException(Exception exception, ResourceDescriptor descriptor) {
		return responseHandler.handleException(exception, descriptor);
	}

}
