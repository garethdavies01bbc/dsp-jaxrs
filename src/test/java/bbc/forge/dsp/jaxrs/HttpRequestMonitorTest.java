package bbc.forge.dsp.jaxrs;

import bbc.forge.dsp.mockito.MockitoTestBase;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.container.ContainerRequestContext;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PhaseInterceptorChain.class)
public class HttpRequestMonitorTest extends MockitoTestBase {

	@Mock Message mockMessage;

    @Mock
    private ContainerRequestContext mockRequest;

	@Test
	public void anyRequestIncrementsTheRequestCounter() {
		PowerMockito.mockStatic(PhaseInterceptorChain.class);
		when(PhaseInterceptorChain.getCurrentMessage()).thenReturn(mockMessage);

		when(mockMessage.get(anyString())).thenReturn("thing");

		HttpRequestMonitor monitor = new HttpRequestMonitor();

		assertEquals(0, monitor.getNumberOfRequests().get());

		monitor.filter(mockRequest);

		assertEquals(1, monitor.getNumberOfRequests().get());
	}

}
