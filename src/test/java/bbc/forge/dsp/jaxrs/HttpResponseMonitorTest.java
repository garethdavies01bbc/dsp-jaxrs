package bbc.forge.dsp.jaxrs;

import bbc.forge.dsp.common.Configuration;
import bbc.forge.dsp.mockito.MockitoTestBase;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerResponseContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PhaseInterceptorChain.class)
public class HttpResponseMonitorTest extends MockitoTestBase {

    @Mock private ContainerResponseContext mockResponse;
    @Mock private Message mockMessage;
    @Mock private Exchange mockExchange;
    @Mock private OperationResourceInfo mockOperationResourceInfo;
    @Mock private ClassResourceInfo mockClassResourceInfo;
    @Mock private Path mockJaxrsPath;
    @Mock private Configuration mockDynamicConfig;

    private HttpResponseMonitor monitor;

    @Before
    public void setup(){
        when(mockMessage.getExchange()).thenReturn(mockExchange);
        when(mockExchange.get(OperationResourceInfo.class)).thenReturn(mockOperationResourceInfo);
        when(mockOperationResourceInfo.getClassResourceInfo()).thenReturn(mockClassResourceInfo);
        when(mockClassResourceInfo.getPath()).thenReturn(mockJaxrsPath);

        PowerMockito.mockStatic(PhaseInterceptorChain.class);
        when(PhaseInterceptorChain.getCurrentMessage()).thenReturn(mockMessage);

        monitor = new HttpResponseMonitor();
        monitor.setDynamicConfig(mockDynamicConfig);
    }

    @Test
    public void a200ResponseIncrementsThe200Counter() {
        when(mockResponse.getStatus()).thenReturn(200);

        when(mockDynamicConfig.getIntegerValue(
                HttpResponseMonitor.MAX_RESPONSE_SIZE_DYNAMIC_CONFIG_KEY,
                HttpResponseMonitor.MAX_RESPONSE_SIZE_DEFAULT)
        ).thenReturn(HttpResponseMonitor.MAX_RESPONSE_SIZE_DEFAULT);

        assertEquals(0, monitor.get200s().get());

        monitor.filter(null, mockResponse);
        monitor.filter(null, mockResponse);

        assertEquals(2, monitor.get200s().get());
    }

    @Test
    public void incrementingTheTotalResponseSize() {
        String responseEntity = "Response";

        when(mockResponse.getStatus()).thenReturn(200);
        when(mockResponse.getEntity()).thenReturn(responseEntity);

        monitor.filter(null, mockResponse);
        assertEquals(responseEntity.length(), monitor.getTotalResponseSize().get());
    }

    @Test
    public void increment_over_500k_responses() {
        String bigString = RandomStringUtils.random(10);

        System.out.println(Integer.valueOf(bigString.length()));

        assertTrue(bigString.length() > 9);

        when(mockResponse.getStatus()).thenReturn(200);
        when(mockResponse.getEntity()).thenReturn(bigString);

        when(mockDynamicConfig.getIntegerValue(
                HttpResponseMonitor.MAX_RESPONSE_SIZE_DYNAMIC_CONFIG_KEY,
                HttpResponseMonitor.MAX_RESPONSE_SIZE_DEFAULT)
        ).thenReturn(9);

        assertEquals(0, monitor.getReponsesOverResponseThreshold().get());

        monitor.filter(null, mockResponse);

        assertEquals(1, monitor.getReponsesOverResponseThreshold().get());
    }

    @Test
    public void do_not_increment_over_500k_responses() {
        String bigString = RandomStringUtils.random(10);

        System.out.println(Integer.valueOf(bigString.length()));

        assertTrue(bigString.length() > 9);

        when(mockResponse.getStatus()).thenReturn(200);
        when(mockResponse.getEntity()).thenReturn(bigString);

        when(mockDynamicConfig.getIntegerValue(
                HttpResponseMonitor.MAX_RESPONSE_SIZE_DYNAMIC_CONFIG_KEY,
                HttpResponseMonitor.MAX_RESPONSE_SIZE_DEFAULT)
        ).thenReturn(10);

        assertEquals(0, monitor.getReponsesOverResponseThreshold().get());

        monitor.filter(null, mockResponse);

        assertEquals(0, monitor.getReponsesOverResponseThreshold().get());
    }
}

