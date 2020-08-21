package bbc.forge.dsp.jaxrs.filters;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;

import bbc.forge.dsp.jaxrs.JaxRsStats;

/**
 * Created for OLYMPICDATA-1622, this is a response filter that provides
 * a good place to log what kind of response is being returned through
 * JMX and in turn Zenoss.
 */
public class HappyMetricFilter implements ContainerResponseFilter {
    private static final Logger logger = Logger.getLogger(HappyMetricFilter.class);

    private JaxRsStats jaxRsStats;

    @Context
    private HttpServletRequest request;

    public HappyMetricFilter(JaxRsStats jaxRsStats) {
        this.jaxRsStats = jaxRsStats;
    }

    @Override
	public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) {
    	try {
	        if (containerResponseContext.getStatus() == 200) {
	            jaxRsStats.incrementTotal200ResponseCounter();
	            String url = request.getRequestURI();
	            logger.debug("Logging request for url " + url);
	            if (url.contains("/public")) {
	                logger.debug("Url contains /public");
	                jaxRsStats.incrementPublic200ResponseCounter();
	            } else {
	                logger.debug("Url does not contain /public");
	                jaxRsStats.incrementPrivate200ResponseCounter();
	            }
	            if (url.contains("/stats")) {
	                logger.debug("Url contains /stats");
	                jaxRsStats.incrementStats200ResponseCounter();
	            }
	        } else if (containerResponseContext.getStatus() == 201) {
	            jaxRsStats.incrementTotal201ResponseCounter();
	        }
    	} catch(Exception e) {
    		logger.error("Happy metric filter failure: " + e.getClass());
    	}
    }
}
