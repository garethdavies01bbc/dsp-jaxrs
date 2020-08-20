package bbc.forge.dsp.jaxrs;

import bbc.forge.dsp.common.AsynchronousListenerWrapper;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

/**
 * User: iviec01
 * Date: 01/02/2012
 * Time: 10:24
 */
public class MemcacheStatusFilter implements ContainerResponseFilter {

    private AsynchronousListenerWrapper asynchronousListenerWrapper;

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) {

        if (!asynchronousListenerWrapper.isServiceDependencyWorking()) {
            System.out.println("Starting status code:" + containerResponseContext.getStatus());

            containerResponseContext.setStatus(500);
            containerResponseContext.setEntity("Memcached is down...");

            System.out.println("Ending status code:" + containerResponseContext.getStatus());
        }
    }

    public void setAsynchronousListenerWrapper(AsynchronousListenerWrapper asynchronousListenerWrapper) {
        this.asynchronousListenerWrapper = asynchronousListenerWrapper;
    }
}
