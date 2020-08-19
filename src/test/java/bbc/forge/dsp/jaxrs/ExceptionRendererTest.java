package bbc.forge.dsp.jaxrs;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import bbc.forge.dsp.mockito.MockitoTestBase;
import bbc.forge.dsp.validation.ValidationException;
import bbc.forge.dsp.validation.ValidationException.VALIDATION_ERROR_TYPE;

public class ExceptionRendererTest extends MockitoTestBase {

	private ExceptionRenderer renderer;

	@Before
	public void setUp() {
		renderer = new ExceptionRenderer();
	}

	@Test
	public void rendersExceptionWithStackTraceAndMessage() {
		try {
			throw new Exception("message");
		}
		catch (Exception e) {

			String error = renderer.render(e);
			assertTrue(error.contains("java.lang.Exception: message"));
			assertTrue(error
					.contains("at bbc.forge.dsp.jaxrs.ExceptionRendererTest.rendersExceptionWithStackTraceAndMessage(ExceptionRendererTest"));
		}
	}
	
	@Test
	public void rendersExceptionWithStackTraceAndMessageAndErrorCode() {
		try {
			throw new ValidationException(VALIDATION_ERROR_TYPE.INVALID_CONTENT, "message");
		}
		catch (ValidationException e) {
			String error = renderer.render(e);
			assertTrue(error.contains("bbc.forge.dsp.validation.ValidationException: message"));
			assertTrue(error.contains(VALIDATION_ERROR_TYPE.INVALID_CONTENT.toString()));
		}
	}

}
