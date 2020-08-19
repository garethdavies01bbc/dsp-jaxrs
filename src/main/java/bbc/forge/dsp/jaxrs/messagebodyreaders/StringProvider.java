package bbc.forge.dsp.jaxrs.messagebodyreaders;


import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.apache.cxf.helpers.IOUtils;

@Provider
public final class StringProvider implements MessageBodyReader<String>  {

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType) {
		return true;
	}

	@Override
	public String readFrom(Class<String> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
			InputStream entityStream) throws IOException, WebApplicationException {
		return IOUtils.toString(entityStream, "UTF-8");
	}


}