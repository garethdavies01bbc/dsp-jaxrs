package bbc.forge.dsp.jaxrs;

import java.net.URI;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import bbc.forge.dsp.descriptor.ResourceDescriptor;
import bbc.forge.dsp.descriptor.ResourceLocation;
import bbc.forge.dsp.descriptor.ResourceRequest;

public interface HttpDataRepository {

	public Response retrieveResource(
			Request request,
			ResourceRequest resourceRequest);

	public Response createResource(
			Request request,
			ResourceLocation location,
			String content,
			String contentType,
			URI locationUri);

	public Response createOrUpdateResource(
			Request request,
			ResourceLocation location,
			String content,
			String contentType,
			URI locationURI);

	public Response updateResource(
			Request request,
			ResourceLocation location,
			String content,
			String contentType);

	public Response deleteResource(
			Request request,
			ResourceLocation location);

	public Response handleException(
			Exception ex,
			ResourceDescriptor descriptor);

}