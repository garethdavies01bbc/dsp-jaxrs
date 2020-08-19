package bbc.forge.dsp.jaxrs;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;

import bbc.forge.dsp.annotation.PerformanceLogger;

public class JaxRsPerformanceLogger extends PerformanceLogger {

	private JaxRsStats jaxRsStats;
	
	public JaxRsPerformanceLogger(JaxRsStats JaxRsStats){
		this.jaxRsStats = JaxRsStats;
	}
	
	@Override
	protected void recordElapsedTime(MethodInvocation invocation, Logger log,
			long startTime, Throwable throwable) {
		
		super.recordElapsedTime(invocation, log, startTime, throwable);
		
		jaxRsStats.incrementResponseTime(System.currentTimeMillis() - startTime);
	}
}
