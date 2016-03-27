/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.Constants;
import org.ocelotds.core.CdiBeanResolver;
import org.ocelotds.core.ws.CallServiceManager;
import org.ocelotds.topic.UserContextFactory;
import org.ocelotds.topic.TopicManager;
import org.ocelotds.messaging.MessageFromClient;
import org.ocelotds.topic.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class WSEndpointTest {

	@InjectMocks
	@Spy
	private WSEndpoint instance;
	
	@Mock
	private TopicManager topicManager;

	@Mock
	private CallServiceManager callServiceManager;
	
	@Mock
	private UserContextFactory userContextFactory;
	
	@Mock
	private SessionManager sessionManager;

	/**
	 * Test of handleOpenConnexion method, of class WSEndpoint.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void testHandleOpenConnexionFromBrowser() throws IOException {
		System.out.println("handleOpenConnexion");
		HandshakeRequest request = mock(HandshakeRequest.class);
		HttpSession httpSession = mock(HttpSession.class);
		EndpointConfig config = mock(EndpointConfig.class);
		Map<String, Object> map = new HashMap<>();
		map.put(Constants.HANDSHAKEREQUEST, request);
		Session session = mock(Session.class);

		when(config.getUserProperties()).thenReturn(map);
		when(request.getHttpSession()).thenReturn(httpSession);
		when(httpSession.getId()).thenReturn("HTTPSESSIONID");
		when(session.getId()).thenReturn("WSSESSIONID");


		instance.handleOpenConnexion(session, config);

		verify(sessionManager).addSession(anyString(), any(Session.class));
		verify(userContextFactory).createUserContext(any(HandshakeRequest.class), eq("WSSESSIONID"));
	}

	/**
	 * Test of onError method, of class WSEndpoint.
	 */
	@Test
	public void testOnError() {
		System.out.println("onError");
		Session session = mock(Session.class);
		when(session.getId()).thenReturn(UUID.randomUUID().toString());
		Throwable t = new Exception();
		instance.onError(session, t);
	}

	/**
	 * Test of handleClosedConnection method, of class WSEndpoint.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void testHandleClosedConnection() throws IOException {
		System.out.println("handleClosedConnection");
		Session session = mock(Session.class);
		when(session.getId()).thenReturn(UUID.randomUUID().toString());
		when(session.isOpen()).thenReturn(true);
		CloseReason closeReason = mock(CloseReason.class);
		when(closeReason.getCloseCode()).thenReturn(CloseReason.CloseCodes.TOO_BIG);
		instance.handleClosedConnection(session, closeReason);
		doThrow(IOException.class).when(session).close();
		instance.handleClosedConnection(session, closeReason);
		doThrow(IllegalStateException.class).when(session).close();
		instance.handleClosedConnection(session, closeReason);
		when(session.isOpen()).thenReturn(false);
		instance.handleClosedConnection(session, closeReason);
	}

	/**
	 * Test of receiveCommandMessage method, of class WSEndpoint.
	 */
	@Test
	public void testReceiveCommandMessage() {
		System.out.println("receiveCommandMessage");
		Session client = mock(Session.class);
		String[] parameterNames = new String[]{"\"a\"", "\"b\"", "\"c\""};
		String[] parameters = new String[]{"\"toto\"", "5", "true"};
		String json = String.format("{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":%s,\"%s\":%s}",
				  Constants.Message.ID, "111",
				  Constants.Message.DATASERVICE, "ClassName",
				  Constants.Message.OPERATION, "methodName",
				  Constants.Message.ARGUMENTNAMES, Arrays.toString(parameterNames),
				  Constants.Message.ARGUMENTS, Arrays.toString(parameters));

		instance.receiveCommandMessage(client, json);

		ArgumentCaptor<MessageFromClient> captureMsg = ArgumentCaptor.forClass(MessageFromClient.class);
		ArgumentCaptor<Session> captureSession = ArgumentCaptor.forClass(Session.class);
		verify(callServiceManager, times(1)).sendMessageToClient(captureMsg.capture(), captureSession.capture());

		MessageFromClient result = captureMsg.getValue();
		assertThat(result.getId()).isEqualTo("111");
		assertThat(result.getDataService()).isEqualTo("ClassName");
		assertThat(result.getOperation()).isEqualTo("methodName");
		assertThat(result.getParameterNames()).containsExactly("a", "b", "c");
		assertThat(result.getParameters()).containsExactly("\"toto\"", "5", "true");
	}
	
	@Test
	public void testGetSessionManager() {
		System.out.println("getSessionManager");
		WSEndpoint oe = spy(new WSEndpoint());
		CdiBeanResolver resolver = mock(CdiBeanResolver.class);
		when(resolver.getBean(eq(TopicManager.class))).thenReturn(new TopicManager());
		doReturn(resolver).when(oe).getCdiBeanResolver();

		TopicManager result = oe.getTopicManager();

		assertThat(result).isInstanceOf(TopicManager.class);
	}
	
	@Test
	public void testGetRequestManager() {
		System.out.println("getRequestManager");
		WSEndpoint oe = spy(new WSEndpoint());
		CdiBeanResolver resolver = mock(CdiBeanResolver.class);
		when(resolver.getBean(eq(UserContextFactory.class))).thenReturn(new UserContextFactory());
		doReturn(resolver).when(oe).getCdiBeanResolver();

		UserContextFactory result = oe.getUserContextFactory();

		assertThat(result).isInstanceOf(UserContextFactory.class);
	}

	@Test
	public void testGetCallServiceManager() {
		System.out.println("getCallServiceManager");
		WSEndpoint oe = spy(new WSEndpoint());
		CdiBeanResolver resolver = mock(CdiBeanResolver.class);
		when(resolver.getBean(eq(CallServiceManager.class))).thenReturn(new CallServiceManager());
		doReturn(resolver).when(oe).getCdiBeanResolver();
	
		CallServiceManager result = oe.getCallServiceManager();
		
		assertThat(result).isInstanceOf(CallServiceManager.class);
	}
	
	@Test
	public void testGetCDIBeanResolver() {
		System.out.println("getCDIBeanResolver");
		CdiBeanResolver cdiBeanResolver = instance.getCdiBeanResolver();
		assertThat(cdiBeanResolver).isInstanceOf(CdiBeanResolver.class);
	}
}
