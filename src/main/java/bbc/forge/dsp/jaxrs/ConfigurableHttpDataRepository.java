package bbc.forge.dsp.jaxrs;

import java.net.URI;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import bbc.forge.dsp.annotation.DspInjectorProvider;
import bbc.forge.dsp.common.DataRepository;
import bbc.forge.dsp.descriptor.ResourceDescriptor;
import bbc.forge.dsp.descriptor.ResourceLocation;
import bbc.forge.dsp.descriptor.ResourceRequest;

import com.google.inject.Injector;

public class ConfigurableHttpDataRepository implements HttpDataRepository {

	private final DataRepository dataRepository;
	private JaxRsConfiguration jaxRsConfiguration;

	private JaxRsStats jaxRsStats;
	private HttpDataRepositoryImpl httpDataRepository;
	private JaxRsResponseHandler responseHandler;
	private JaxRsExceptionHandler jaxRsExceptionHandler;

	public ConfigurableHttpDataRepository(DataRepository dataRepository) {
		this.dataRepository = dataRepository;
		jaxRsConfiguration = new JaxRsConfiguration();
	}

	public void initialise() {
		Injector jaxRsInjector =
			new DspInjectorProvider().getInjector()
			.createChildInjector(new JaxRsModule(dataRepository, jaxRsConfiguration));

		jaxRsStats = jaxRsInjector.getInstance(JaxRsStats.class);
		httpDataRepository = jaxRsInjector.getInstance(HttpDataRepositoryImpl.class);
		responseHandler = jaxRsInjector.getInstance(JaxRsResponseHandler.class);
		jaxRsExceptionHandler = jaxRsInjector.getInstance(JaxRsExceptionHandler.class);
	}

	private void checkInitialised() {
		if (httpDataRepository == null) throw new IllegalArgumentException("init-method=\"initialise\" is required in Spring configuration");
	}

	public Response retrieveResource(Request request, ResourceRequest resourceRequest) {
		checkInitialised();
		return httpDataRepository.retrieveResource(request, resourceRequest);
	}

	public Response createResource(Request request, ResourceLocation location, String content, String contentType, URI locationURI) {
		checkInitialised();
		return httpDataRepository.createResource(request, location, content, contentType, locationURI);
	}

	public Response createOrUpdateResource(Request request,
			ResourceLocation location, String content, String contentType, URI locationURI) {
		checkInitialised();
		return httpDataRepository.createOrUpdateResource(request, location, content, contentType, locationURI);
	}

	public Response updateResource(Request request, ResourceLocation location,
			String content, String contentType) {
		checkInitialised();
		return httpDataRepository.updateResource(request, location, content, contentType);
	}

	public Response deleteResource(Request request, ResourceLocation location) {
		checkInitialised();
		return httpDataRepository.deleteResource(request, location);
	}

	public Response handleException(Exception ex, ResourceDescriptor descriptor) {
		checkInitialised();
		return responseHandler.handleException(ex, descriptor);
	}

	public void setConfiguration(JaxRsConfiguration jaxRsConfiguration) {
		this.jaxRsConfiguration = jaxRsConfiguration;
	}

	public JaxRsStats getStats() {
		return jaxRsStats;
	}

	public JaxRsExceptionHandler getExceptionHandler() {
		return jaxRsExceptionHandler;
	}

}