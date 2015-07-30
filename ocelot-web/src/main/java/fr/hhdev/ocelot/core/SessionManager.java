/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
package fr.hhdev.ocelot.core;

import fr.hhdev.ocelot.annotations.JsTopicAccessControl;
import fr.hhdev.ocelot.security.JsTopicAccessController;
import fr.hhdev.ocelot.security.JsTopicACAnnotationLiteral;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton sessions manager
 *
 * @author hhfrancois
 */
@Singleton
public class SessionManager {

	private final static Logger logger = LoggerFactory.getLogger(SessionManager.class);
	private final Map<String, Collection<Session>> sessionsByTopic = new HashMap<>();

	private static final Annotation DEFAULT_AT = new AnnotationLiteral<Default>() {
		private static final long serialVersionUID = 1L;
	};

	@Inject
	@Any
	Instance<JsTopicAccessController> topicAccessController;

	/**
	 * Process Access Topic Controller
	 * 
	 * @param session
	 * @param topic
	 * @throws IllegalAccessException 
	 */
	private void checkAccessTopic(Session session, String topic) throws IllegalAccessException {
		JsTopicACAnnotationLiteral tcal = new JsTopicACAnnotationLiteral(topic);
		Instance<JsTopicAccessController> accessControls = topicAccessController.select(DEFAULT_AT);
		boolean tacPresent = false;
		for (JsTopicAccessController accessControl : accessControls) {
			accessControl.checkAccess(session, topic);
			tacPresent = true;
		}
		accessControls = topicAccessController.select(tcal);
		for (JsTopicAccessController accessControl : accessControls) {
			accessControl.checkAccess(session, topic);
			tacPresent = true;
		}
		if (!tacPresent) {
			logger.info("No topic access control found in project, add {} implementation with optional Qualifier {} in your project for add topic security.", JsTopicAccessController.class, JsTopicAccessControl.class);
		} else {
			logger.info("Topic access control found in project.");
		}
	}
	
	/**
	 * Register session for topic
	 *
	 * @param topic
	 * @param session
	 * @throws IllegalAccessException
	 */
	public void registerTopicSession(String topic, Session session) throws IllegalAccessException {
		checkAccessTopic(session, topic);
		Collection<Session> sessions;
		if (sessionsByTopic.containsKey(topic)) {
			sessions = sessionsByTopic.get(topic);
		} else {
			sessions = Collections.synchronizedCollection(new ArrayList<Session>());
			sessionsByTopic.put(topic, sessions);
		}
		logger.info("'{}' subscribe to '{}'", session.getId(), topic);
		sessions.add(session);
	}

	/**
	 * Unregister session for topic
	 * topic 'ALL' remove session for all topics
	 *
	 * @param topic
	 * @param session
	 */
	public void unregisterTopicSession(String topic, Session session) {
		logger.debug("'{}' unsubscribe to '{}'", session.getId(), topic);
		if("ALL".equals(topic)) {
			for (Collection<Session> sessions : sessionsByTopic.values()) {
				if (sessions != null && sessions.contains(session)) {
					sessions.remove(session);
				}
			}
		} else {
			Collection<Session> sessions = sessionsByTopic.get(topic);
			if (sessions != null && sessions.contains(session)) {
				sessions.remove(session);
			}
		}
	}

	/**
	 * Remove session cause it's close by the endpoint
	 *
	 * @param session
	 */
	public void removeSessionToTopic(Session session) {
		for (String topic : sessionsByTopic.keySet()) {
			unregisterTopicSession(topic, session);
		}
	}

	/**
	 * Get Sessions for topics
	 *
	 * @param topic
	 * @return
	 */
	public Collection<Session> getSessionsForTopic(String topic) {
		Collection<Session> result;
		if (existsSessionForTopic(topic)) {
			return sessionsByTopic.get(topic);
		} else {
			result = Collections.EMPTY_LIST;
		}
		return result;
	}

	/**
	 * check if sessions exist for topic
	 *
	 * @param topic
	 * @return
	 */
	public boolean existsSessionForTopic(String topic) {
		return sessionsByTopic.containsKey(topic) && !sessionsByTopic.get(topic).isEmpty();
	}
}
