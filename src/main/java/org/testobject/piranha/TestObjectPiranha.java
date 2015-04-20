package org.testobject.piranha;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class TestObjectPiranha {

	public static String TESTOBJECT_BASE_URL = "https://app.testobject.com:443/api/";

	public static void main(String... args) throws IOException {

		//File appFile = new File("/home/leonti/development/citrix/ForTestObject/gotomeeting-5.0.799.1290-SNAPSHOT.apk");
		//int id = TestObjectPiranha.uploadApp("42995311C3724F21A9266E24643DA754", appFile);
		//System.out.println("App id is: " + id);

		//	File appFrameworkFile = new File("/home/leonti/development/citrix/ForTestObject/piranha-android-server-5.0.30-SNAPSHOT.apk");
		//	int id = TestObjectPiranha.uploadFrameworkApp("42995311C3724F21A9266E24643DA754", appFrameworkFile);
		//	System.out.println("Framework id is: " + id);

		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability("testobject_api_key", "42995311C3724F21A9266E24643DA754");
		capabilities.setCapability("testobject_app_id", "1");
		capabilities.setCapability("testobject_framework_app_id", "2");
		capabilities.setCapability("testobject_device", "Sony_Xperia_T_real");

		Map<String, String> piranhaCaps = new HashMap<String, String>();
		piranhaCaps.put("className", "com.citrixonline.universal.ui.activities.LauncherActivity");
		piranhaCaps.put("intent",
				"com.citrixonline.piranha.androidserver/com.citrixonline.piranha.androidserver.PiranhaAndroidInstrumentation");
		piranhaCaps.put("packageName", "com.citrixonline.android.gotomeeting");

		capabilities.setCapability("piranha_params", new GsonBuilder().create().toJson(piranhaCaps));

		TestObjectPiranha testObjectPiranha = new TestObjectPiranha(capabilities);
		testObjectPiranha.close();

		//		System.out.println("Server started ...");
		//		System.in.read();
	}

	private final String baseUrl;
	
	private String sessionId;
	private Server server;
	private int port;

	public TestObjectPiranha(DesiredCapabilities desiredCapabilities) {
		this(TESTOBJECT_BASE_URL, desiredCapabilities);
	}
	
	public TestObjectPiranha(String baseUrl, DesiredCapabilities desiredCapabilities) {

		this.baseUrl = baseUrl;
		
		Map<String, Map<String, String>> fullCapabilities = new HashMap<String, Map<String, String>>();
		fullCapabilities.put("desiredCapabilities", desiredCapabilities.getCapabilities());

		String capsAsJson = new GsonBuilder().create().toJson(fullCapabilities);

		try {
			String response = createWebTarget()
					.path("session")
					.request(MediaType.TEXT_PLAIN)
					.post(Entity.entity(capsAsJson, MediaType.APPLICATION_JSON), String.class);

			Map<String, String> map = jsonToMap(response);
			sessionId = map.get("sessionId");

		} catch (InternalServerErrorException e) {
			rethrow(e);
		}

		startProxyServer(sessionId);
	}

	public void startProxyServer(String sessionId) {
		port = findFreePort();
		server = new Server(port);
		ServletContextHandler handler = new ServletContextHandler();
		handler.setContextPath("");
		// adds Jersey Servlet with a customized ResourceConfig
		handler.addServlet(new ServletHolder(new ServletContainer(resourceConfig())), "/*");
		server.setHandler(handler);
		try {
			server.start();
		} catch (Exception e) {
			throw new RuntimeException("Could not start the server", e);
		}
	}

	private static int findFreePort() {
		int port;
		try {
			ServerSocket socket = new ServerSocket(0);
			port = socket.getLocalPort();
			socket.close();
		} catch (Exception e) {
			port = -1;
		}
		return port;
	}

	private ResourceConfig resourceConfig() {
		return new ResourceConfig().register(new Proxy(baseUrl + "piranha", sessionId));
	}

	public int getPort() {
		return port;
	}

	private void rethrow(InternalServerErrorException e) {
		String response = e.getResponse().readEntity(String.class);

		throw new RuntimeException(response);
	}

	private Map<String, String> jsonToMap(String json) {
		Gson gson = new Gson();
		Type stringStringMap = new TypeToken<Map<String, String>>() {}.getType();
		return gson.fromJson(json, stringStringMap);
	}

	public void close() {
		deleteSession();

		try {
			server.stop();
		} catch (Exception e) {
			// ignored
		}

	}

	private void deleteSession() {
		try {
			createWebTarget().path("session/" + sessionId)
					.request(MediaType.APPLICATION_JSON)
					.delete();
		} catch (InternalServerErrorException e) {
			rethrow(e);
		}
	}

	private WebTarget createWebTarget() {
		Client client = ClientBuilder.newClient();

		return client.target(baseUrl + "piranha");
	}

	public static int uploadApp(String apiKey, File appFile) {
		return uploadApp(apiKey, appFile, false);
	}

	public static int uploadFrameworkApp(String apiKey, File appFile) {
		return uploadApp(apiKey, appFile, true);
	}

	private static int uploadApp(String apiKey, File appFile, boolean isFramework) {

		HttpAuthenticationFeature authFeature = HttpAuthenticationFeature.basicBuilder()
				.credentials("testobject-api", apiKey).build();

		Client client = ClientBuilder.newClient().register(authFeature);

		Invocation.Builder invocationBuilder = client.target(TESTOBJECT_BASE_URL + "storage/upload")
				.request(MediaType.TEXT_PLAIN);

		try {
			if (isFramework) {
				invocationBuilder.header("App-Type", "framework");
			}

			String appId = invocationBuilder.post(Entity.entity(FileUtils.openInputStream(appFile), MediaType.APPLICATION_OCTET_STREAM),
					String.class);
			return Integer.valueOf(appId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}