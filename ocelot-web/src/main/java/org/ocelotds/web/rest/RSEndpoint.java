/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web.rest;

import javax.ws.rs.core.Context;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.core.MediaType;
import org.ocelotds.Constants;
import org.ocelotds.annotations.OcelotLogger;
import org.ocelotds.annotations.OcelotResource;
import org.ocelotds.context.ThreadLocalContextHolder;
import org.ocelotds.core.mtc.RSMessageToClientService;
import org.ocelotds.messaging.MessageFromClient;
import org.ocelotds.messaging.MessageToClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST Web Service
 *
 * @author hhfrancois
 */
@Path("endpoint")
@RequestScoped
@OcelotResource
public class RSEndpoint implements IRSEndpoint {

	@Inject
	@OcelotLogger
	private Logger logger;

	@Context
	private HttpServletRequest request;

	@Inject
	private RSMessageToClientService messageToClientService;

	/**
	 * Retrieves representation of an instance of org.ocelotds.GenericResource
	 *
	 * @param json
	 * @return an instance of java.lang.String
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Override
	public String getMessageToClient(@FormParam(Constants.Message.MFC) String json) {
		HttpSession httpSession = getHttpSession();
		setContext(httpSession);
		MessageFromClient message = MessageFromClient.createFromJson(json);
		MessageToClient mtc = getMessageToClientService().createMessageToClient(message, httpSession);
		return mtc.toJson();
	}

	@Override
	public HttpSession getHttpSession() {
		return request.getSession();
	}

	void setContext(HttpSession httpSession) {
		ThreadLocalContextHolder.put(Constants.HTTPREQUEST, request);
		ThreadLocalContextHolder.put(Constants.HTTPSESSION, httpSession);
	}

	Logger getLogger() {
		if(null == logger) {
			logger = LoggerFactory.getLogger(RSEndpoint.class);
		}
		return logger;
	}

	RSMessageToClientService getMessageToClientService() {
		if(null == messageToClientService) {
			messageToClientService = CDI.current().select(RSMessageToClientService.class).get();
		}
		return messageToClientService;
	}
}
