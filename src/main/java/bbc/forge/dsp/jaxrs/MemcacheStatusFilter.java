package bbc.forge.dsp.jaxrs;

import bbc.forge.dsp.common.AsynchronousListenerWrapper;
import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;

import javax.ws.rs.core.Response;

/**
 * User: iviec01
 * Date: 01/02/2012
 * Time: 10:24
 */
public class MemcacheStatusFilter implements RequestHandler {

    private AsynchronousListenerWrapper asynchronousListenerWrapper;

    @Override
    public Response handleRequest(Message message, ClassResourceInfo classResourceInfo) {

        if (!asynchronousListenerWrapper.isServiceDependencyWorking()) {
            return Response.status(500).entity("Memcached is down...").build();
        }
        return null;

    }

    public void setAsynchronousListenerWrapper(AsynchronousListenerWrapper asynchronousListenerWrapper) {
        this.asynchronousListenerWrapper = asynchronousListenerWrapper;
    }
}
