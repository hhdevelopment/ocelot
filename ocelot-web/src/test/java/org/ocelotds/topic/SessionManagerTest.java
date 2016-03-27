/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ocelotds.topic;

import java.util.Map;
import javax.websocket.Session;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class SessionManagerTest {

	@InjectMocks
	@Spy
	SessionManager instance;

	/**
	 * Test of getMap method, of class SessionManager.
	 */
	@Test
	public void testGetMap() {
		System.out.println("getMap");
		Map<String, Session> result = instance.getMap();
		assertThat(result).isNotNull();
	}

	/**
	 * Test of addSession method, of class SessionManager.
	 */
	@Test
	public void testAddSession() {
		System.out.println("addSession");
		instance.getMap().clear();
		String id = "ID0";
		Session session = mock(Session.class);
		instance.addSession(id, session);
		assertThat(instance.getMap()).isNotEmpty();
		verify(instance).removeSession(any(Session.class));
		instance.getMap().clear();
	}

	/**
	 * Test of removeSession method, of class SessionManager.
	 */
	@Test
	public void testRemoveSession_Session() {
		System.out.println("removeSession");
		instance.getMap().clear();
		Session session = mock(Session.class);
		instance.getMap().put("ID0", session);
		instance.getMap().put("ID1", session);

		instance.removeSession(session);

		verify(instance, times(2)).removeSession(any(String.class));
	}

	/**
	 * Test of removeSession method, of class SessionManager.
	 */
	@Test
	public void testRemoveSession_String() {
		System.out.println("removeSession");
		instance.getMap().clear();
		String id = "ID0";
		Session session = mock(Session.class);
		doNothing().when(instance).removeSession(any(Session.class));
		instance.addSession(id, session);
		assertThat(instance.getMap()).isNotEmpty();
		instance.removeSession(id);
		assertThat(instance.getMap()).isEmpty();
	}

	/**
	 * Test of getSessionById method, of class SessionManager.
	 */
	@Test
	public void testGetSessionById() {
		System.out.println("getSessionById");
		instance.getMap().clear();
		String id = "ID0";
		Session session = mock(Session.class);
		doNothing().when(instance).removeSession(any(Session.class));
		instance.addSession(id, session);
		assertThat(instance.getMap()).isNotEmpty();

		Session result = instance.getSessionById(id);
		assertThat(result).isEqualTo(session);
	}

}