package org.testobject.piranha;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

@Path("")
public class Proxy {

	private final String baseUrl;
	private final String sessionId;

	public Proxy(String baseUrl, String sessionId) {
		this.baseUrl = baseUrl;
		this.sessionId = sessionId;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces("application/json-rpc")
	public String post(String command) {

		
		//System.out.println("Session id: " + sessionId);
		//System.out.println("Content: '" + command + "'");

		Client client = ClientBuilder.newClient();

		String response = client.target(baseUrl)
				.path("session")
				.path(sessionId)
				.request("application/json-rpc")
				.post(Entity.entity(command, MediaType.APPLICATION_FORM_URLENCODED), String.class);

		//System.out.println("Response: '" + response + "'");

		return response;
	}
}
