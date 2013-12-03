package no.dega.couchpotatoer;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

public class APIUtilitiesTest extends TestCase {

	public void testMakeRequest() {
		String response = APIUtilities.makeRequest("app.available");
		assertNotNull("Response string is null", response);
		assertFalse("Response is 0-length", response.length() <= 0);
		
		try {
			JSONObject jsonResponse = new JSONObject(response);
			assertNotNull("JSON created from response is null", jsonResponse);
			assertFalse("JSON created from response is 0-length", jsonResponse.length() <= 0);
	
			assertTrue("API not operational.", jsonResponse.getBoolean("success"));
		} catch (JSONException e) {
			fail("JSONException caused by: " + e.getCause());
		}
	}

}
