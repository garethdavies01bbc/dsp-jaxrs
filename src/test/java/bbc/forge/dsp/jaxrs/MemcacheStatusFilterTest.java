package bbc.forge.dsp.jaxrs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import bbc.forge.dsp.common.AsynchronousListenerWrapper;

/**
 * User: iviec01
 * Date: 01/02/2012
 * Time: 10:25
 */
@RunWith(MockitoJUnitRunner.class)
public class MemcacheStatusFilterTest {

    private MemcacheStatusFilter memcacheStatusFilter;
    @Mock
    private AsynchronousListenerWrapper mockMemcachedListener;
    @Mock
    private Message mockMessage;
    @Mock
    private ClassResourceInfo mockClassResourceInfo;
    @Mock
    private ContainerResponseContext mockResponse;

    @Before
    public void setUp() throws Exception {
        memcacheStatusFilter = new MemcacheStatusFilter();
        memcacheStatusFilter.setAsynchronousListenerWrapper(mockMemcachedListener);
    }

    @Test
    public void handleRequestShouldReturnNullIfMemcacheIsUp() {

        when(mockMemcachedListener.isServiceDependencyWorking()).thenReturn(true);

        memcacheStatusFilter.filter(null, mockResponse);

        verify(mockMemcachedListener).isServiceDependencyWorking();
        assertNull(mockResponse);
    }

    @Test
    public void handleRequestShouldReturnAResponseMessageIfMemcacheIsDown () {
        when(mockMemcachedListener.isServiceDependencyWorking()).thenReturn(false);

        memcacheStatusFilter.filter(null, mockResponse);

        verify(mockMemcachedListener).isServiceDependencyWorking();

        assertNotNull(mockResponse);
        assertEquals("Memcached is down...", mockResponse.getEntity().toString());
    }

    @Test
    public void handleRequestShouldReturnA500ResponseCodeIfMemcacheIsDown () {
        when(mockMemcachedListener.isServiceDependencyWorking()).thenReturn(false);

        memcacheStatusFilter.filter(null, mockResponse);

        verify(mockMemcachedListener).isServiceDependencyWorking();

        assertNotNull(mockResponse);
        assertEquals(500, mockResponse.getStatus());
    }
}
