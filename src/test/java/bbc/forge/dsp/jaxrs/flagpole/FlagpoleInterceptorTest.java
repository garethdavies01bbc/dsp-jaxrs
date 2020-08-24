package bbc.forge.dsp.jaxrs.flagpole;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cxf.message.Message;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import bbc.forge.dsp.flagpole.FlagpoleStatusService;
import bbc.forge.dsp.jaxrs.JaxRsStats;
import bbc.forge.dsp.mockito.MockitoTestBase;

import com.google.common.collect.Maps;

public class FlagpoleInterceptorTest extends MockitoTestBase {

	@Mock Message message;
	@Mock FlagpoleStatusService statusService;
	@Mock JaxRsStats jaxRsStats;

	private FlagpoleInterceptor flagpoleInterceptor;

	@Before
	public void setUp() {
		flagpoleInterceptor = new FlagpoleInterceptor(statusService, jaxRsStats);
	}

	@Test
	public void aNullHeaderMapResultsInTheFlagpoleValuesBeingReset() {
		when(message.get(Message.PROTOCOL_HEADERS)).thenReturn(null);

		flagpoleInterceptor.handleMessage(message);

		verify(statusService).updateFlagpoleStatuses(Maps.<String, String>newHashMap());
	}

	@Test
	public void anEmptyHeaderMapResultsInTheFlagpoleValuesBeingReset() {
		when(message.get(Message.PROTOCOL_HEADERS)).thenReturn(new HashMap<String, List<String>>());

		flagpoleInterceptor.handleMessage(message);

		verify(statusService).updateFlagpoleStatuses(Maps.<String, String>newHashMap());
	}

	@Test
	public void anNonFlagpoleHeaderResultsInTheFlagpoleValuesBeingReset() {
		Map<String, List<String>> headerMap = new HashMap<String, List<String>>();
		headerMap.put("X-Something-Else-Not-A-Flagpole", Arrays.asList("Value"));

		when(message.get(Message.PROTOCOL_HEADERS)).thenReturn(headerMap);

		flagpoleInterceptor.handleMessage(message);

		verify(statusService).updateFlagpoleStatuses(Maps.<String, String>newHashMap());
	}

	@Test
	public void aNullFlagpoleHeaderResultsInTheFlagpoleValuesBeingReset() {
		Map<String, List<String>> headerMap = new HashMap<String, List<String>>();
		headerMap.put("X-Flagpole-Flagpole-Name", null);

		when(message.get(Message.PROTOCOL_HEADERS)).thenReturn(headerMap);

		flagpoleInterceptor.handleMessage(message);

		verify(statusService).updateFlagpoleStatuses(Maps.<String, String>newHashMap());
	}

	@Test
	public void aNullFlagpoleDataItemResultsInTheFlagpoleValuesBeingReset() {
		Map<String, List<String>> headerMap = new HashMap<String, List<String>>();
		headerMap.put("X-Flagpole-Flagpole-Name", Arrays.asList((String)null));

		when(message.get(Message.PROTOCOL_HEADERS)).thenReturn(headerMap);

		flagpoleInterceptor.handleMessage(message);

		verify(statusService).updateFlagpoleStatuses(Maps.<String, String>newHashMap());
	}

	@Test
	public void aFlagpoleStatusIsSetInTheStatusService() {
		Map<String, List<String>> headerMap = new HashMap<String, List<String>>();
		headerMap.put("X-Flagpole-Flagpole-Name", Arrays.asList("FLAGPOLE-VALUE"));

		when(message.get(Message.PROTOCOL_HEADERS)).thenReturn(headerMap);

		flagpoleInterceptor.handleMessage(message);

		verify(statusService).updateFlagpoleStatuses(argThat(hasEntry("flagpole-name", "FLAGPOLE-VALUE")));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void twoFlagpoleStatusesAreSetInTheStatusService() {
		Map<String, List<String>> headerMap = new HashMap<String, List<String>>();
		headerMap.put("X-Flagpole-Flagpole-Name", Arrays.asList("FLAGPOLE-VALUE"));
		headerMap.put("X-Flagpole-Flagpole-Name-2", Arrays.asList("FLAGPOLE-VALUE-2"));

		when(message.get(Message.PROTOCOL_HEADERS)).thenReturn(headerMap);

		flagpoleInterceptor.handleMessage(message);

		verify(statusService).updateFlagpoleStatuses(argThat(allOf(hasEntry("flagpole-name", "FLAGPOLE-VALUE"), hasEntry("flagpole-name-2", "FLAGPOLE-VALUE-2"))));
	}

	@Test
	public void aChangeToAFlagpoleStatusThatHappensWithinTheConfiguredTimeoutIsIgnored() throws InterruptedException {
		Map<String, List<String>> headerMap = new HashMap<String, List<String>>();
		headerMap.put("X-Flagpole-Flagpole-Name", Arrays.asList("FLAGPOLE-VALUE"));

		Map<String, List<String>> headerMap2 = new HashMap<String, List<String>>();
		headerMap2.put("X-Flagpole-Flagpole-Name-2", Arrays.asList("FLAGPOLE-VALUE-2"));

		when(message.get(Message.PROTOCOL_HEADERS))
			.thenReturn(headerMap)
			.thenReturn(headerMap2);

		flagpoleInterceptor.setTimeBetweenFlagpoleUpdatesInMs(2000);
		flagpoleInterceptor.handleMessage(message);

		Thread.sleep(1000);

		flagpoleInterceptor.handleMessage(message);

		verify(statusService).updateFlagpoleStatuses(
				argThat(hasEntry("flagpole-name", "FLAGPOLE-VALUE")));
		verify(statusService, times(0)).updateFlagpoleStatuses(
				argThat(hasEntry("flagpole-name-2", "FLAGPOLE-VALUE-2")));
	}

	@Test
	public void aChangeToAFlagpoleStatusThatHappensAfterTheConfiguredTimeoutIsNotIgnored() throws InterruptedException {
		Map<String, List<String>> headerMap = new HashMap<String, List<String>>();
		headerMap.put("X-Flagpole-Flagpole-Name", Arrays.asList("FLAGPOLE-VALUE"));

		Map<String, List<String>> headerMap2 = new HashMap<String, List<String>>();
		headerMap2.put("X-Flagpole-Flagpole-Name-2", Arrays.asList("FLAGPOLE-VALUE-2"));

		when(message.get(Message.PROTOCOL_HEADERS))
			.thenReturn(headerMap)
			.thenReturn(headerMap2);

		flagpoleInterceptor.setTimeBetweenFlagpoleUpdatesInMs(1000);
		flagpoleInterceptor.handleMessage(message);

		Thread.sleep(2000);

		flagpoleInterceptor.handleMessage(message);

		verify(statusService).updateFlagpoleStatuses(
				argThat(hasEntry("flagpole-name", "FLAGPOLE-VALUE")));
		verify(statusService).updateFlagpoleStatuses(
				argThat(hasEntry("flagpole-name-2", "FLAGPOLE-VALUE-2")));
	}

	@Test
	public void aLowercaseFlagpoleStatusIsSetInTheStatusService() {
		Map<String, List<String>> headerMap = new HashMap<String, List<String>>();
		headerMap.put("x-flagpole-flagpole-name", Arrays.asList("FLAGPOLE-VALUE"));

		when(message.get(Message.PROTOCOL_HEADERS)).thenReturn(headerMap);

		flagpoleInterceptor.handleMessage(message);

		verify(statusService).updateFlagpoleStatuses(argThat(hasEntry("flagpole-name", "FLAGPOLE-VALUE")));
	}

	@Test
	public void anExceptionWhenProcessingAFlagpoleResultsInALoggedErrorAndAFlagpoleReadErrorStatsIncrement() {
		when(message.get(Message.PROTOCOL_HEADERS)).thenThrow(new RuntimeException("Error!"));

		flagpoleInterceptor.handleMessage(message);

		verify(jaxRsStats).incrementFlagpoleReadError();
	}

}
