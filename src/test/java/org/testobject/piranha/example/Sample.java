package org.testobject.piranha.example;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.JSONArray;
import org.json.JSONException;
import org.jsonrpc.JSONRPCRequest;
import org.testobject.piranha.DesiredCapabilities;
import org.testobject.piranha.TestObjectDevice;
import org.testobject.piranha.TestObjectPiranha;

import com.citrixonline.piranha.ControlType;
import com.citrixonline.piranha.androidclient.PiranhaAndroidClient;
import com.citrixonline.piranha.androidclient.Robotium.RobotiumClient;
import com.google.gson.GsonBuilder;
import com.thoughtworks.selenium.Wait;

public class Sample {

	static boolean UPLOAD_APP = false;
	
	public static void main(String[] args) throws JSONException, InterruptedException, ExecutionException {
		if (UPLOAD_APP == true) {
			int frameworkAppId = TestObjectPiranha.uploadFrameworkApp("FE51FF78F8AE4729B07D3DDD8F151FBE", new File(
					"/home/leonti/Downloads/AndroidPiranhaServer_1.0.apk"));
			System.out.println("FW app id: " + frameworkAppId);
			return;
		}
		
		//String apiKey = TestObjectPiranha.regenerateApiKey("user", "password", "project");

		List<String> devices = getAvailableDevices();

		Future<Void> first = performTestAsync(devices.get(0));
		Future<Void> second = performTestAsync(devices.get(1));

		first.get();
		second.get();
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
		capabilities.setCapability("testobject_api_key", "<YOUR KEY>");
		capabilities.setCapability("testobject_app_id", "1");
		capabilities.setCapability("testobject_framework_app_id", "2");
		capabilities.setCapability("testobject_device", deviceId);

		Map<String, String> piranhaCaps = new HashMap<String, String>();
		piranhaCaps.put("className", "com.citrixonline.universal.ui.activities.LauncherActivity");
		piranhaCaps
				.put("intent",
						"com.citrixonline.piranha.androidserver/com.citrixonline.piranha.androidserver.PiranhaAndroidInstrumentation");
		piranhaCaps.put("packageName", "com.citrixonline.android.gotomeeting");

		capabilities.setCapability("piranha_params", new GsonBuilder().create().toJson(piranhaCaps));

		TestObjectPiranha testObjectPiranha = null;

		try {

			testObjectPiranha = new TestObjectPiranha(capabilities);

			int port = testObjectPiranha.getPort();
			// final PiranhaAndroidClient c = new PiranhaAndroidClient(ip, 7100,
			// true);
			System.out.println("Port is: " + port + ", session id is '" + testObjectPiranha.getSessionId() + "' ("
					+ deviceId + ")");
			final PiranhaAndroidClient c = new PiranhaAndroidClient("localhost", port, false);

			sleep(15000);

			boolean ret1 = c.robotium().waiter().waitForViewToBeEnabled(ControlType.TEXT, "</#JoinMeetingId/>", 30);

			takeScreenshot(c.robotium());

			if (ret1) {
				c.robotium().setter().clearEditText("</#JoinMeetingId/>");

				c.robotium().setter().setText("</#JoinMeetingId/>", "555-000-000");
			}

			takeScreenshot(c.robotium());

			new Wait() {

				@Override
				public boolean until() {
					c.robotium().clicker().clickImageView("</#JoinMeetingButton/>");
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					takeScreenshot(c.robotium());

					return c.robotium().waiter().waitForViewToBeEnabled(ControlType.BUTTON, "OK", 5);
				}
			}.wait("Error Dialog did not show up", 60 * 1000, 2 * 1000);

			c.robotium().clicker().clickButton("OK");

			sleep(5000);

			takeScreenshot(c.robotium());

			System.out.println("Test if succesfully finished on " + deviceId);

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
			if (device.isAvailable && device.apiLevel >= 17) {
				available.add(device.id);
			}
		}

		return available;
	}

	private static void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void takeScreenshot(RobotiumClient roboriumClient) {
		try {
			JSONArray params = new JSONArray();
			JSONRPCRequest request = new JSONRPCRequest("PiranhaSystemUtils::takeScreenShot", params);
			roboriumClient.execute(request);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

}
