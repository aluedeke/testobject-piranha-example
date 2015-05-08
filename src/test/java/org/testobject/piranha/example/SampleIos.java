package org.testobject.piranha.example;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.json.JSONException;
import org.testobject.piranha.DesiredCapabilities;
import org.testobject.piranha.TestObjectPiranha;

import com.citrixonline.piranha.ControlType;
import com.citrixonline.piranha.androidclient.PiranhaAndroidClient;
import com.google.gson.GsonBuilder;
import com.thoughtworks.selenium.Wait;

public class SampleIos {

	static boolean UPLOAD_APP = false;
	
	public static void main(String[] args) throws JSONException, InterruptedException, ExecutionException {
		if (UPLOAD_APP == true) {
			int frameworkAppId = TestObjectPiranha.uploadFrameworkApp("78F386B29979493397AD418FB092E93B", new File(
					"/home/leonti/development/git/citrix/iOSAutomationServer-5.0.30-SNAPSHOT-jar-with-dependencies.jar"));
			System.out.println("FW app id: " + frameworkAppId);
			return;
		}
		
		performTest("iPad_mini_3_16GB_real");
	}
	
	private static void performTest(String deviceId) {

		System.out.println("Performing test on " + deviceId);

		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability("testobject_api_key", "78F386B29979493397AD418FB092E93B");
		capabilities.setCapability("testobject_app_id", "1");
		capabilities.setCapability("testobject_framework_app_id", "2");
		capabilities.setCapability("testobject_device", deviceId);

		TestObjectPiranha testObjectPiranha = null;

		try {

			testObjectPiranha = new TestObjectPiranha("http://localhost:7070/", capabilities);

			int port = testObjectPiranha.getPort();
			// final PiranhaAndroidClient c = new PiranhaAndroidClient(ip, 7100,
			// true);
			System.out.println("Port is: " + port + ", session id is '" + testObjectPiranha.getSessionId() + "' ("
					+ deviceId + ")");

			
			System.in.read();

			System.out.println("Finishing the session ...");
			
		} catch (Exception e) {
			System.out.println("Test failed on " + deviceId + " because of " + e.getMessage());
		} finally {
			if (testObjectPiranha != null) {
				testObjectPiranha.close();
			}
		}
	}
	
	
}
