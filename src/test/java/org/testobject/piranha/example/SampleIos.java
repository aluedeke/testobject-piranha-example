package org.testobject.piranha.example;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.json.JSONException;
import org.testobject.piranha.DesiredCapabilities;
import org.testobject.piranha.TestObjectDevice;
import org.testobject.piranha.TestObjectPiranha;

import com.google.common.collect.Lists;

public class SampleIos {

	static boolean UPLOAD_APP = false;

	public static void main(String[] args) throws JSONException {
		if (UPLOAD_APP == true) {
			int frameworkAppId = TestObjectPiranha.uploadFrameworkApp("<YOUR API KEY>", new File(
					"/home/leonti/development/citrix/ios/iOSAutomationServer-5.0.30-SNAPSHOT-jar-with-dependencies.jar"));
			System.out.println("FW app id: " + frameworkAppId);
			return;
		}

		List<Future<Void>> futures = Lists.newLinkedList();
		for (String device : getAvailableDevices()) {
			if (!device.equals("iPhone_3GS_8GB_real")) {
				futures.add(performTestAsync(device));
			}
		}

		for (Future<Void> future : futures) {
			try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

		System.out.println("All tests are done!");
	}

	private static Future<Void> performTestAsync(final String deviceId) {
		return Executors.newFixedThreadPool(1).submit(new Callable<Void>() {

			@Override
			public Void call() {

				performTest(deviceId);
				return null;
			}
		});
	}

	private static void performTest(String deviceId) {

		System.out.println("Performing test on " + deviceId);

		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability("testobject_api_key", "<YOUR API KEY>");
		capabilities.setCapability("testobject_app_id", "1");
		capabilities.setCapability("testobject_framework_app_id", "2");
		capabilities.setCapability("testobject_suite_name", "Piranha test");
		capabilities.setCapability("testobject_test_name", "Test on " + deviceId);
		capabilities.setCapability("testobject_device", deviceId);

		TestObjectPiranha testObjectPiranha = null;

		try {
			testObjectPiranha = new TestObjectPiranha(capabilities);

			int port = testObjectPiranha.getPort();
			System.out.println("Port is: " + port + ", session id is '" + testObjectPiranha.getSessionId() + "' ("
					+ deviceId + ")");

			System.out.println("Taking screenshot ...");
			ClientBuilder.newClient().target("http://localhost:" + port)
					.request(MediaType.APPLICATION_JSON)
					.post(Entity.entity("{ \"action\": \"CaptureBase64Screen\"}", MediaType.APPLICATION_JSON), String.class);

			System.out.println("Finishing the session ...");

		} catch (Exception e) {
			System.out.println("Test failed on " + deviceId + " because of " + e.getMessage());
		} finally {
			if (testObjectPiranha != null) {
				testObjectPiranha.close();
			}
		}
	}

	private static List<String> getAvailableDevices() {
		List<String> available = new LinkedList<>();

		for (TestObjectDevice device : TestObjectPiranha.listDevices()) {
			if (device.isAvailable && device.os == TestObjectDevice.OS.IOS) {
				available.add(device.id);
			}
		}

		return available;
	}

}
