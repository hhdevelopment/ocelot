/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.dashboard;

import java.io.IOException;
import java.net.URL;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.annotations.OcelotResource;
import org.ocelotds.dashboard.security.DashboardSecureProvider;
import org.ocelotds.security.OcelotSecured;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhfrancois
 */
@RequestScoped
@OcelotResource
@Path("dashboard")
@OcelotSecured(provider = DashboardSecureProvider.class)
public class RsDashboard {

	@Context
	private UriInfo context;
	
	@Inject
	@OcelotLogger
	Logger logger;

	@GET
	@Path("")
	public Response getRoot(@PathParam("type") String type) throws IOException {
		MediaType mtype = new MediaType("text", "html");
		return getResponse("dashboard/index.html", mtype);
	}
	
	@GET
	@Path("{res:.+}.{type}")
	public Response getResponse(@PathParam("type") String type) throws IOException {
		MediaType mtype = new MediaType("text", "js".equals(type) ? "javascript" : type);
		return getResponse(context.getPath(), mtype);
	}

	public Response getResponse(String path, MediaType type) throws IOException {
		try {
			return Response.ok((Object) getResource("/" + path).openStream(), type).build();
		} catch (NullPointerException e) {
			getLogger().error("Request "+path+" but not present on server.");
			return Response.noContent().build();
		}
	}

	/**
	 * GEt URL Resource
	 *
	 * @param name
	 * @return
	 */
	URL getResource(String name) {
		return RsDashboard.class.getResource(name);
	}

	Logger getLogger() {
		if(null == logger) {
			logger = LoggerFactory.getLogger(RsDashboard.class);
		}
		return logger;
	}
}
