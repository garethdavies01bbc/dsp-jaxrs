package bbc.forge.dsp.jaxrs;

import bbc.forge.dsp.annotation.Performance;
import bbc.forge.dsp.common.DataRepository;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

public class JaxRsModule extends AbstractModule {

	private final DataRepository dataRepository;
	private final JaxRsConfiguration jaxRsConfiguration;

	public JaxRsModule(DataRepository dataRepository, JaxRsConfiguration jaxRsConfiguration) {
		this.dataRepository = dataRepository;
		this.jaxRsConfiguration = jaxRsConfiguration;
	}

	@Override
	protected void configure() {
		JaxRsStats jaxRsStats = new JaxRsStats();
		bind(JaxRsConfiguration.class).toInstance(jaxRsConfiguration);
		bind(DataRepository.class).toInstance(dataRepository);
		bind(JaxRsStats.class).toInstance(jaxRsStats);
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(Performance.class), new JaxRsPerformanceLogger(jaxRsStats));
	}

}