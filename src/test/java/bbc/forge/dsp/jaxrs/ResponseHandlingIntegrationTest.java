package bbc.forge.dsp.jaxrs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import bbc.forge.dsp.mockito.MockitoTestBase;

import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

public class ResponseHandlingIntegrationTest extends MockitoTestBase {

	private static final String ROOT_URL = "http://localhost:19348/jaxrs-test/";

	private WebConversation wc;

	@Before
	public void setUp() {
		HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);
		wc = new WebConversation();
	}

	@Test
	public void getNothingReturnsAPlainText404WithMaxAge60() throws IOException, SAXException {
		WebResponse response = wc.getResponse(ROOT_URL + "nothing");

		assertEquals(404, response.getResponseCode());
		assertTrue(response.getText().contains("key"));
		assertEquals("no-transform,max-age=60", response.getHeaderField("Cache-Control"));
		assertEquals("text/plain", response.getHeaderField("Content-Type"));
	}

	@Test
	public void getNotAvailableReturnsA503WithMaxAge5() throws IOException, SAXException {
		WebResponse response = wc.getResponse(ROOT_URL + "unavailable");

		assertEquals(503, response.getResponseCode());
		assertEquals("no-transform,max-age=5", response.getHeaderField("Cache-Control"));
	}

	@Test
	public void getErrorReturnsAPlainTextException500() throws IOException, SAXException {
		WebResponse response = wc.getResponse(ROOT_URL + "error");

		assertEquals(500, response.getResponseCode());
		assertTrue(response.getText(), response.getText().contains("java.lang.Exception: Error!"));
		assertEquals("no-transform,max-age=5", response.getHeaderField("Cache-Control"));
		assertEquals("text/plain", response.getHeaderField("Content-Type"));
	}

	@Test
	public void getThingReturnsTheResourceDataAsXml() throws IOException, SAXException {
		WebResponse response = wc.getResponse(ROOT_URL + "thing");

		assertEquals(200, response.getResponseCode());
		assertEquals("<resource-data>data</resource-data>", response.getText());
		assertEquals("no-transform,max-age=360",
				response.getHeaderField("Cache-Control"));
		assertEquals("Accept", response.getHeaderField("Vary"));
		assertEquals("application/xml", response.getHeaderField("Content-Type"));
	}

	@Test
	public void getThingReturnsTheResourceDataAsXmlWithVersionHeaderPresent() throws IOException, SAXException {
		WebResponse response = wc.getResponse(ROOT_URL + "thing");

		assertEquals(200, response.getResponseCode());
		assertEquals("<resource-data>data</resource-data>", response.getText());
		assertEquals("no-transform,max-age=360",
				response.getHeaderField("Cache-Control"));
		assertEquals("Accept", response.getHeaderField("Vary"));
		assertEquals("application/xml", response.getHeaderField("Content-Type"));
		assertEquals("1.1.11", response.getHeaderField(HttpResponseVersionProvider.VERSION_HEADER));
	}

	@Test
	public void getThingReturnsTheResourceDataAsXmlWithCustomHeadersToVaryBy() throws IOException, SAXException {
		WebResponse response = wc.getResponse(ROOT_URL + "thing/custom-headers-to-vary-by");

		assertEquals(200, response.getResponseCode());
		assertEquals("<resource-data>data</resource-data>", response.getText());
		assertEquals("no-transform,max-age=360",
				response.getHeaderField("Cache-Control"));
		assertEquals("Accept,X-Some-Custom-Header,X-Another-Custom-Header", response.getHeaderField("Vary"));
		assertEquals("application/xml", response.getHeaderField("Content-Type"));
	}

	@Test
	public void a500WithAMaxAgeResponseIsReturned() throws IOException, SAXException {
		WebResponse response = wc.getResponse(ROOT_URL + "error-with-max-age");

		assertEquals(500, response.getResponseCode());
		assertTrue("3", response.getHeaderField("Cache-Control").contains("max-age=3"));
	}

	@Test
	public void a500WithAMaxAgeResponseIsReturnedWithoutMaxAgeIfIfNoneMatchHeaderIsPresentToAvoidCachingErrorsOnRevalidation() throws IOException, SAXException {
		wc.setHeaderField("If-None-Match", "blah");
		WebResponse response = wc.getResponse(ROOT_URL + "error-with-max-age");

		assertEquals(500, response.getResponseCode());
		assertNull(response.getHeaderField("Cache-Control"));
	}

	@Test
	public void getRepositoryErrorReturnsAPlainTextException500() throws IOException, SAXException {
		WebResponse response = wc.getResponse(ROOT_URL + "repo-error");

		assertEquals(500, response.getResponseCode());
		assertTrue(response.getText(), response.getText().contains("bbc.forge.dsp.common.RepositoryFailureException: fail"));
		assertEquals("text/plain", response.getHeaderField("Content-Type"));
	}

	@Test
	public void handlingAValidationExceptionResultsInA400WithPlainTextReasonShown() throws IOException, SAXException {
		WebResponse response = wc.getResponse(ROOT_URL + "validation-error");

		assertEquals(400, response.getResponseCode());
		assertTrue(response.getText(), response.getText().contains("bbc.forge.dsp.validation.ValidationException: Invalid!"));
		assertEquals("text/plain", response.getHeaderField("Content-Type"));
	}

	@Test
	public void handlingAValidationExceptionResultsInA400WithPlainTextReasonShownAndVersionHeaderPresent() throws IOException, SAXException {
		WebResponse response = wc.getResponse(ROOT_URL + "validation-error");

		assertEquals(400, response.getResponseCode());
		assertTrue(response.getText(), response.getText().contains("bbc.forge.dsp.validation.ValidationException: Invalid!"));
		assertEquals("text/plain", response.getHeaderField("Content-Type"));
		assertEquals("1.1.11", response.getHeaderField(HttpResponseVersionProvider.VERSION_HEADER));
	}

	@Test
	public void ifWeGetAnUnchangingThingItReturns200() throws IOException, SAXException {
		WebResponse response = wc.getResponse(ROOT_URL + "thing-that-never-changes");

		assertEquals(200, response.getResponseCode());
		assertEquals("\"never-changes\"", response.getHeaderField("ETag"));
	}

	@Test
	public void ifWeGetAnUnchangingThingAndSupplyTheETagInAnIfNoneMatchHeaderItReturns304() throws IOException, SAXException {
		wc.setHeaderField("If-None-Match", "\"never-changes\"");
		WebResponse response = wc.getResponse(ROOT_URL + "thing-that-never-changes");

		assertEquals(304, response.getResponseCode());
	}

	@Test
	public void ifWeGetThingThatChangedIn2005AndSupplyAnIfUnmodifiedSince2006HeaderItReturns304() throws IOException, SAXException {
		wc.setHeaderField("If-Modified-Since", "Wed, 24 Aug 2011 10:43:31 GMT");
		WebResponse response = wc.getResponse(ROOT_URL + "thing-that-changed-in-2005");

		assertEquals(304, response.getResponseCode());
	}

	@Test
	public void theSecondTimeWeGetAChangableThingItReturnsDataAsXml() throws IOException, SAXException {
		wc.setHeaderField("If-None-Match", "\"1\"");
		WebResponse response = wc.getResponse(ROOT_URL + "thing-that-changes-every-time");

		assertEquals(304, response.getResponseCode());

		wc.setHeaderField("If-None-Match", "\"1\"");
		response = wc.getResponse(ROOT_URL + "thing-that-changes-every-time");
		assertEquals(200, response.getResponseCode());
		assertEquals("\"2\"", response.getHeaderField("ETag"));
		assertEquals("<resource-data>data</resource-data>", response.getText());
	}

	@Test
	public void ifWePutAThingItReturnsAReturns201() throws IOException, SAXException {
		String putUrl = ROOT_URL + "thing";
		WebResponse response = wc.getResponse(new PutMethodWebRequest(putUrl, IOUtils.toInputStream("content"), "text/plain"));

		assertEquals(201, response.getResponseCode());
		assertEquals(ROOT_URL + "thing", response.getHeaderField("Location"));
	}

	@Test
	public void ifWePutAThingThatAlreadyExistsItReturnsAReturns409() throws SAXException, IOException {
		String putUrl = ROOT_URL + "thing-already-exists";

		WebResponse response = wc.getResponse(new PutMethodWebRequest(putUrl, IOUtils.toInputStream("content"), "text/plain"));
		assertEquals(409, response.getResponseCode());
	}

	@Test
	public void ifWePostAThingItReturnsAReturns200() throws IOException, SAXException {
		String postUrl = ROOT_URL + "thing";
		WebResponse response = wc.getResponse(new PostMethodWebRequest(postUrl, IOUtils.toInputStream("content"), "text/plain"));

		assertEquals(200, response.getResponseCode());
	}

	@Test
	public void ifWePostAMissingThingItReturnsAReturns404() throws IOException, SAXException {
		String postUrl = ROOT_URL + "nothing";
		WebResponse response = wc.getResponse(new PostMethodWebRequest(postUrl, IOUtils.toInputStream("content"), "text/plain"));

		assertEquals(404, response.getResponseCode());
	}

	@Test
	public void ifWePostAThingWithNotMatchingEtagReturns412() throws SAXException, IOException {
		String postUrl = ROOT_URL + "thing-modified-at-start-of-2010";
		PostMethodWebRequest request = new PostMethodWebRequest(postUrl, IOUtils.toInputStream("content"), "text/plain");
		request.setHeaderField("If-Match", "invalid");

		WebResponse response = wc.getResponse(request);
		assertEquals(412, response.getResponseCode());
	}

	@Test
	public void ifWePostAThingWithMatchingEtagReturns200() throws IOException, SAXException {
		String postUrl = ROOT_URL + "thing-modified-at-start-of-2010";
		PostMethodWebRequest request = new PostMethodWebRequest(postUrl, IOUtils.toInputStream("content"), "text/plain");
		request.setHeaderField("If-Match", "valid");
		WebResponse response = wc.getResponse(request);

		assertEquals(200, response.getResponseCode());
	}

}
