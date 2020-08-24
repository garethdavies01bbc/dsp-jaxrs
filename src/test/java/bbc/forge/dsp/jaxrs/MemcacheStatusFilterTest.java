package bbc.forge.dsp.jaxrs;

import static org.mockito.Mockito.*;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import bbc.forge.dsp.common.AsynchronousListenerWrapper;

@RunWith(MockitoJUnitRunner.class)
public class MemcacheStatusFilterTest {

    @Mock private AsynchronousListenerWrapper mockMemcachedListener;
    @Mock private ContainerResponseContext mockResponse;
    @Mock private ContainerRequestContext mockRequest;

    private MemcacheStatusFilter memcacheStatusFilter;

    @Before
    public void setUp() {
        memcacheStatusFilter = new MemcacheStatusFilter();
        memcacheStatusFilter.setAsynchronousListenerWrapper(mockMemcachedListener);
    }

    @Test
    public void filterShouldNotSetStatusAndEntityIfMemcacheIsUp() {
        when(mockMemcachedListener.isServiceDependencyWorking()).thenReturn(true);

        memcacheStatusFilter.filter(mockRequest, mockResponse);

        verify(mockMemcachedListener).isServiceDependencyWorking();
        verify(mockResponse, never()).setStatus(anyInt());
        verify(mockResponse, never()).setEntity(anyString());
    }

    @Test
    public void filterShouldSetStatusAndEntityCorrectlyIfMemcacheIDown() {
        when(mockMemcachedListener.isServiceDependencyWorking()).thenReturn(false);

        memcacheStatusFilter.filter(mockRequest, mockResponse);

        verify(mockMemcachedListener).isServiceDependencyWorking();
        verify(mockResponse).setStatus(500);
        verify(mockResponse).setEntity("Memcached is down...");
    }
}
