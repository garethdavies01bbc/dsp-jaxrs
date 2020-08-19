package bbc.forge.dsp.jaxrs;

import bbc.forge.dsp.common.Configuration;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.log4j.Logger;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@ManagedResource
public class HttpResponseMonitor implements ContainerResponseFilter {

    private Logger LOG = Logger.getLogger(this.getClass());
    private Map<String, AtomicInteger> responseCounts;
    private AtomicLong totalResponseSize = new AtomicLong();
    protected static final String MAX_RESPONSE_SIZE_DYNAMIC_CONFIG_KEY = "HttpResponseMonitor.maxResponseSizeInBytes";
    protected static final Integer MAX_RESPONSE_SIZE_DEFAULT = 614400;
    private Configuration dynamicConfig;

    public HttpResponseMonitor() {
        responseCounts = new HashMap<String, AtomicInteger>();
    }

    @Override
    public void filter(ContainerRequestContext inContext, ContainerResponseContext outContext) {
        Message message = PhaseInterceptorChain.getCurrentMessage();
        OperationResourceInfo cri = message.getExchange().get(OperationResourceInfo.class);

        LOG.info("Responding with status " + outContext.getStatus());
        LOG.debug("Responding with " + outContext.getEntity());
        count(outContext.getStatus()).incrementAndGet();

        Object entity = outContext.getEntity();
        if (entity != null) {
            int responseSize = entity.toString().length();
            LOG.info("Response size " + responseSize);
            totalResponseSize.addAndGet(responseSize);
            incrementResponseCountOverThreshold(responseSize, cri);
        }
    }

    @ManagedAttribute(description = "valueType=COUNTER|kpiName=Number_of_responses_over_500k")
    public AtomicInteger getReponsesOverResponseThreshold() {
        return count("responses_over_500k");
    }

    @ManagedAttribute(description = "valueType=COUNTER|kpiName=Number_of_200_responses")
    public AtomicInteger get200s() {
       return count(200);
    }

    @ManagedAttribute(description = "valueType=COUNTER|kpiName=Number_of_503_responses")
    public AtomicInteger get503s() {
        return count(503);
    }

    @ManagedAttribute(description = "valueType=COUNTER|kpiName=Number_of_400_responses")
    public AtomicInteger get400s() {
        return count(400);
    }

    @ManagedAttribute(description = "valueType=COUNTER|kpiName=Number_of_404_responses")
    public AtomicInteger get404s() {
        return count(404);
    }

    @ManagedAttribute(description = "valueType=COUNTER|kpiName=Number_of_405_responses")
    public AtomicInteger get405s() {
        return count(405);
    }

    @ManagedAttribute(description = "valueType=COUNTER|kpiName=Number_of_406_responses")
    public AtomicInteger get406s() {
        return count(406);
    }

    @ManagedAttribute(description = "valueType=COUNTER|kpiName=Number_of_409_responses")
    public AtomicInteger get409s() {
        return count(409);
    }

    @ManagedAttribute(description = "valueType=COUNTER|kpiName=Number_of_412_responses")
    public AtomicInteger get412s() {
        return count(412);
    }

    @ManagedAttribute(description = "valueType=COUNTER|kpiName=Number_of_internal_errors")
    public AtomicInteger get500s() {
        return count(500);
    }

    @ManagedAttribute(description = "valueType=COUNTER|kpiName=Sum_of_all_response_sizes")
    public AtomicLong getTotalResponseSize() {
        return totalResponseSize;
    }

    @ManagedAttribute(description = "valueType=COUNTER|kpiName=Number_of_invalid_requests")
    public long getInvalidRequestErrors() {
        return count(400).get() +
                count(404).get() +
                count(405).get() +
                count(406).get() +
                count(409).get() +
                count(412).get();
    }

    private synchronized AtomicInteger count(int key) {
        return count(String.valueOf(key));
    }

    private synchronized AtomicInteger count(String key) {

        AtomicInteger count = responseCounts.get(key);

        if (count == null) {
            count = new AtomicInteger();
            responseCounts.put(key, count);
        }

        return count;
    }

    private void incrementResponseCountOverThreshold(int responseSize, OperationResourceInfo info) {

        int maxResponseThreshold = MAX_RESPONSE_SIZE_DEFAULT;

        if (dynamicConfig != null) {
            maxResponseThreshold = dynamicConfig.getIntegerValue(
                    MAX_RESPONSE_SIZE_DYNAMIC_CONFIG_KEY,
                    MAX_RESPONSE_SIZE_DEFAULT
            );
        }


        if (responseSize > maxResponseThreshold) {
            count("responses_over_500k").incrementAndGet();

            String method = "unknown";
            if (info.getMethodToInvoke() != null) method = info.getMethodToInvoke().getName();

			LOG.error("The response size has exceeded the threshold of "
                    + maxResponseThreshold
                    + " bytes. actual response size:  "
                    + responseSize
                    + " bytes. endpoint: " + info.getClassResourceInfo().getPath().value() + ", " + method);
        }
    }


	public void setDynamicConfig(Configuration dynamicConfig) {
        this.dynamicConfig = dynamicConfig;
    }

}
