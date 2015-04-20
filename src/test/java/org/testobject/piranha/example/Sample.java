package test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.jsonrpc.JSONRPCRequest;
import org.testobject.piranha.DesiredCapabilities;
import org.testobject.piranha.TestObjectPiranha;

import com.citrixonline.piranha.ControlType;
import com.citrixonline.piranha.androidclient.PiranhaAndroidClient;
import com.citrixonline.piranha.androidclient.Robotium.RobotiumClient;
import com.google.gson.GsonBuilder;
import com.thoughtworks.selenium.Wait;

public class Sample {

    static boolean UPLOAD_APP = false;

    public static void main(String[] args) throws JSONException {
        if (UPLOAD_APP == true) {
            int frameworkAppId = TestObjectPiranha.uploadFrameworkApp("FE51FF78F8AE4729B07D3DDD8F151FBE",
                    new File("/home/leonti/development/citrix/ForTestObject/piranha-android-server-5.0.30-SNAPSHOT.apk"));
            System.out.println("FW app id: " + frameworkAppId);
            return;
        }

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("testobject_api_key", "FE51FF78F8AE4729B07D3DDD8F151FBE");
        capabilities.setCapability("testobject_app_id", "1");
        capabilities.setCapability("testobject_framework_app_id", "2");
        capabilities.setCapability("testobject_device", "Cat_B15_real");

        Map<String, String> piranhaCaps = new HashMap<String, String>();
        piranhaCaps.put("className", "com.citrixonline.universal.ui.activities.LauncherActivity");
        piranhaCaps.put("intent",
                "com.citrixonline.piranha.androidserver/com.citrixonline.piranha.androidserver.PiranhaAndroidInstrumentation");
        piranhaCaps.put("packageName", "com.citrixonline.android.gotomeeting");

        capabilities.setCapability("piranha_params", new GsonBuilder().create().toJson(piranhaCaps));

        TestObjectPiranha testObjectPiranha = null;

        try {

            testObjectPiranha = new TestObjectPiranha(capabilities);

            int port = testObjectPiranha.getPort();
            //final PiranhaAndroidClient c = new PiranhaAndroidClient(ip, 7100, true);
            System.out.println("Port is: " + port);
            final PiranhaAndroidClient c = new PiranhaAndroidClient("localhost", port, true);

            sleep(10000);

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

                    return c.robotium().waiter()
                            .waitForViewToBeEnabled(ControlType.BUTTON, "OK", 5);
                }
            }.wait("Error Dialog did not show up", 60 * 1000, 2 * 1000);

            c.robotium().clicker().clickButton("OK");

            sleep(5000);

            takeScreenshot(c.robotium());

        } finally {
            if (testObjectPiranha != null) {
                testObjectPiranha.close();
            }
        }

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


