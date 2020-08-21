package bbc.forge.dsp.jaxrs;

import bbc.forge.dsp.common.AsynchronousListenerWrapper;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

public class MemcacheStatusFilter implements ContainerResponseFilter {
    private AsynchronousListenerWrapper asynchronousListenerWrapper;

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) {

        if (!asynchronousListenerWrapper.isServiceDependencyWorking()) {
            containerResponseContext.setStatus(500);
            containerResponseContext.setEntity("Memcached is down...");
        }
    }

    public void setAsynchronousListenerWrapper(AsynchronousListenerWrapper asynchronousListenerWrapper) {
        this.asynchronousListenerWrapper = asynchronousListenerWrapper;
    }
}
