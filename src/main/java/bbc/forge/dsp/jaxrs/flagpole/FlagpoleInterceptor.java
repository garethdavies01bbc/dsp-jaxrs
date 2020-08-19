package bbc.forge.dsp.jaxrs.flagpole;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.interceptor.AbstractInDatabindingInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bbc.forge.dsp.flagpole.FlagpoleStatusService;
import bbc.forge.dsp.jaxrs.JaxRsStats;

import com.google.common.collect.Maps;

public class FlagpoleInterceptor extends AbstractInDatabindingInterceptor {

	public static final String FLAGPOLE_PREFIX = "x-flagpole-";
	private static final long DEFAULT_TIME_BETWEEN_FLAGPOLE_UPDATES_IN_MS = 10000;

    private static final Logger LOG = LoggerFactory.getLogger(FlagpoleInterceptor.class);


	private final FlagpoleStatusService statusService;
	private final JaxRsStats jaxRsStats;
	private long timeBetweenFlagpoleUpdatesInMs = DEFAULT_TIME_BETWEEN_FLAGPOLE_UPDATES_IN_MS;
	private Long timeOfLastUpdate;

	public FlagpoleInterceptor(FlagpoleStatusService statusService) {
		super(Phase.PRE_INVOKE);

		this.jaxRsStats = null;
		this.statusService = statusService;
	}

	public FlagpoleInterceptor(FlagpoleStatusService statusService, JaxRsStats jaxRsStats) {
        super(Phase.PRE_INVOKE);

		this.statusService = statusService;
		this.jaxRsStats = jaxRsStats;
    }


	private synchronized boolean readyToUpdateFlagpoles() {
		long currentTimeMillis = System.currentTimeMillis();

		if (timeOfLastUpdate == null) {
			timeOfLastUpdate = currentTimeMillis;
			return true;
		}
		if (timeOfLastUpdate + timeBetweenFlagpoleUpdatesInMs < currentTimeMillis) {
			timeOfLastUpdate = currentTimeMillis;
			return true;
		}
		return false;
	}

    @SuppressWarnings("unchecked")
	@Override
    public void handleMessage(Message message) throws Fault {
    	if (readyToUpdateFlagpoles()) {
	    	try {
		        Map<String, List<String>> headers = (Map<String, List<String>>)
		        	message.get(Message.PROTOCOL_HEADERS);

		        Map<String, String> flagpoleStatuses = Maps.newHashMap();

				if (MapUtils.isNotEmpty(headers)) {
					for (Entry<String, List<String>> header : headers.entrySet()) {
						String caseInsensistiveHeader = header.getKey().toLowerCase();
						if (caseInsensistiveHeader.startsWith(FLAGPOLE_PREFIX)) {
							String flagpoleData = CollectionUtils.isNotEmpty(header.getValue())
													? header.getValue().get(0)
													: null;

							if (StringUtils.isNotEmpty(flagpoleData)) {
		        				if (LOG.isInfoEnabled())
		        					LOG.info("Read flagpole " + header + ": '" + flagpoleData + "'");

		        				flagpoleStatuses.put(caseInsensistiveHeader
		        						.replaceFirst(FLAGPOLE_PREFIX, ""), flagpoleData);
		        			}
		        		}
		        	}
		        }

		        statusService.updateFlagpoleStatuses(flagpoleStatuses);
	    	} catch (Exception e) {
	    		LOG.error("Error when attempting to read flagpole values", e);
	    		if (jaxRsStats != null) {
	    			jaxRsStats.incrementFlagpoleReadError();
	    		}
	    	}
    	}
    }

	public void setTimeBetweenFlagpoleUpdatesInMs(
			long timeBetweenFlagpoleUpdatesInMs) {
		this.timeBetweenFlagpoleUpdatesInMs = timeBetweenFlagpoleUpdatesInMs;
	}

}


