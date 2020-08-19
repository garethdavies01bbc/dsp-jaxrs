package bbc.forge.dsp.jaxrs.managedresources;

import java.lang.reflect.Method;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.model.wadl.Description;
import org.apache.cxf.jaxrs.model.wadl.Descriptions;
import org.apache.cxf.jaxrs.model.wadl.DocTarget;
import org.springframework.jmx.export.annotation.ManagedAttribute;

@Path("/managed-resources")
public class ManagedResourcesApi {

	private List<Object> managedResources;

	public ManagedResourcesApi(List<Object> managedResources) {
		this.managedResources = managedResources;
	}

	@GET
	@Descriptions({ @Description(value = "Information about a ManagedResource beans", target = DocTarget.METHOD) })
	public Response get() {
		String resourceData = extractManagedAttributeData(managedResources);
		return Response.ok(resourceData).type(MediaType.TEXT_PLAIN).build();
	}

	private String extractManagedAttributeData(List<Object> managedResources) {
		StringBuilder data = new StringBuilder();
		for (Object managedResource : managedResources) {
			data.append("*** ");
			data.append(managedResource.getClass().getSimpleName());
			data.append(" ***\n");
			Method[] methods = managedResource.getClass().getMethods();
			for (Method method : methods) {
				if (method.isAnnotationPresent(ManagedAttribute.class)) {
					data.append(method.getAnnotation(ManagedAttribute.class).description());
					data.append(": ");
					try {
						data.append(method.invoke(managedResource));
					} catch (Exception e) {
						data.append("Error");
					}
					data.append("\n");
				}
			}
			data.append("\n\n");
		}
		return data.toString();
	}

}
