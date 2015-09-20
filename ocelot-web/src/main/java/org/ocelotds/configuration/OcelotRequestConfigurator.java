/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.configuration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.security.auth.Subject;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import javax.ws.rs.core.HttpHeaders;
import org.ocelotds.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class extract info in request, and expose them to userProperties ServerEnpoint
 *
 * @author hhfrancois
 */
public class OcelotRequestConfigurator extends ServerEndpointConfig.Configurator {

	private final Logger logger = LoggerFactory.getLogger(OcelotRequestConfigurator.class);

	@Override
	public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
		Map<String, List<String>> headers = request.getHeaders();
		Locale locale = new Locale("en", "US");
		List<String> accepts = headers.get(HttpHeaders.ACCEPT_LANGUAGE);
		logger.debug("Get accept-language from client headers : {}", accepts);
		if(null != accepts) {
			Iterator<String> iterator = accepts.iterator();
			while(iterator.hasNext()) {
				String accept = iterator.next();
				Pattern pattern = Pattern.compile(".*(\\w\\w)-(\\w\\w).*");
				Matcher matcher = pattern.matcher(accept);
				if (matcher.matches() && matcher.groupCount() == 2) {
					locale = new Locale(matcher.group(1), matcher.group(2));
					break;
				}
			}
		}
		Subject subject = null;
		// Glassfish implementation
		try {
			Class<?> secuCtxClass = Class.forName("com.sun.enterprise.security.SecurityContext");
			Method getCurrent = secuCtxClass.getMethod("getCurrent");
			Object current = getCurrent.invoke(null);
			Method getSubject = current.getClass().getMethod("getSubject");
			subject = (Subject) getSubject.invoke(current);
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
		}

		// init from request only on openHandler
		sec.getUserProperties().put(Constants.SUBJECT, subject);
		sec.getUserProperties().put(Constants.LOCALE, locale);
		sec.getUserProperties().put(Constants.PRINCIPAL, request.getUserPrincipal());
		super.modifyHandshake(sec, request, response);
	}
}
