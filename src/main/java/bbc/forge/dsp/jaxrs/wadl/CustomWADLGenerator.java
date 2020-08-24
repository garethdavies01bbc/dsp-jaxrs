package bbc.forge.dsp.jaxrs.wadl;

import java.awt.*;
import java.io.StringReader;
import java.io.StringWriter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.TransformerFactoryImpl;

import org.apache.cxf.jaxrs.impl.UriInfoImpl;
import org.apache.cxf.jaxrs.model.wadl.WadlGenerator;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.log4j.Logger;

import bbc.forge.dsp.classpath.CachedClasspathResourceLoader;

import com.google.common.base.Preconditions;

public class CustomWADLGenerator extends WadlGenerator implements ContainerResponseFilter {

	private static final String WADL_DOCUMENTATION_2006_07_XSL = "/wadl_documentation-2006-07.xsl";

	private Logger LOG = Logger.getLogger(this.getClass());

	private CachedClasspathResourceLoader cachedClasspathResourceLoader;

	public CustomWADLGenerator(CachedClasspathResourceLoader cachedClasspathResourceLoader) {
		this.cachedClasspathResourceLoader = cachedClasspathResourceLoader;
	}

	@Override
	public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) {
		Message message = PhaseInterceptorChain.getCurrentMessage();
		UriInfo ui = new UriInfoImpl(message);

		if (!ui.getQueryParameters().containsKey("html")) {
			Response response = Response.status(Response.Status.BAD_REQUEST).build();
			containerRequestContext.abortWith(response);
			return;
		}

		String wadl = containerRequestContext.getEntityStream().toString();
		String transformedWadl = simpleTransform(wadl);
		MediaType type = MediaType.APPLICATION_XHTML_XML_TYPE;

		containerResponseContext.setEntity(transformedWadl, null, type);
		containerResponseContext.setStatus(200);
	}

	public String simpleTransform(String content) {
		TransformerFactory tFactory = new TransformerFactoryImpl();
		StringWriter stringWriter = null;
		try {
			String xsltString = cachedClasspathResourceLoader.load(WADL_DOCUMENTATION_2006_07_XSL);
			Preconditions.checkNotNull(xsltString);
			Transformer transformer = tFactory.newTransformer(new StreamSource(new StringReader(xsltString)));
			stringWriter = new StringWriter();
			transformer.transform(new StreamSource(new StringReader(content)), new StreamResult(stringWriter));
		} catch (Exception e) {
			LOG.error("Error on transforming wadl with xsl : " + WADL_DOCUMENTATION_2006_07_XSL, e);
		}
		return stringWriter.toString();
	}

}
