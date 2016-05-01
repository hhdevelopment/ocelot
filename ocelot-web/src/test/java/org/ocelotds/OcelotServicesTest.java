/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.enterprise.inject.Instance;
import javax.servlet.http.HttpSession;
import javax.websocket.Session;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.context.OcelotContext;
import org.ocelotds.core.UnProxyClassServices;
import org.ocelotds.topic.TopicManager;
import org.ocelotds.core.UpdatedCacheManager;
import org.ocelotds.core.services.ClassAsDataService;
import org.ocelotds.marshallers.JsonMarshallerException;
import org.ocelotds.objects.OcelotMethod;
import org.slf4j.Logger;
import org.ocelotds.marshalling.IJsonMarshaller;
import org.ocelotds.objects.FakeCDI;
import org.ocelotds.objects.OcelotService;
import org.ocelotds.objects.Options;
import org.ocelotds.topic.SessionManager;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class OcelotServicesTest {

	@Mock
	private Logger logger;

	@Mock
	private ServiceTools serviceTools;

	@Mock
	private UpdatedCacheManager updatedCacheManager;

	@Mock
	private TopicManager topicManager;
	
	@Mock
	private OcelotContext ocelotContext;

	@Mock
	private SessionManager sessionManager;

	@Mock
	private HttpSession httpSession;

	@Mock
	private UnProxyClassServices unProxyClassServices;

	@Spy
	private Instance<Object> dataservices = new FakeCDI();

	@InjectMocks
	@Spy
	private OcelotServices instance;

	/**
	 * Test of initCore method, of class.
	 */
	@Test
	public void test_getInitCoreMonitorTrue() {
		System.out.println("initCore");
		Options options = new Options();
		options.setMonitor(true);
		instance.initCore(options);
		verify(httpSession).setAttribute(eq(Constants.Options.MONITOR), eq(true));
	}

	/**
	 * Test of initCore method, of class.
	 */
	@Test
	public void test_getInitCoreMonitorFalse() {
		System.out.println("initCore");
		Options options = new Options();
		options.setMonitor(false);
		instance.initCore(options);
		verify(httpSession).setAttribute(eq(Constants.Options.MONITOR), eq(false));
	}

	/**
	 * Test of getLocale method, of class OcelotServices.
	 */
	@Test
	public void testGetLocale() {
		System.out.println("getLocale");
		Locale l = Locale.FRANCE;
		Locale l2 = Locale.US;

		when(ocelotContext.getLocale()).thenReturn(l).thenReturn(l2);

		assertThat(instance.getLocale()).isEqualTo(l);
		assertThat(instance.getLocale()).isEqualTo(l2);
	}

	/**
	 * Test of setLocale method, of class OcelotServices.
	 */
	@Test
	public void testSetLocale() {
		System.out.println("setLocale");
		Locale l = Locale.FRANCE;
		Locale l2 = Locale.US;

		instance.setLocale(l);
		instance.setLocale(l2);

		ArgumentCaptor<Locale> localeCaptor = ArgumentCaptor.forClass(Locale.class);
		verify(ocelotContext, times(2)).setLocale(localeCaptor.capture());
		List<Locale> allValues = localeCaptor.getAllValues();
		assertThat(allValues.get(0)).isEqualTo(l);
		assertThat(allValues.get(1)).isEqualTo(l2);
	}

	/**
	 * Test of getUsername method, of class OcelotServices.
	 */
	@Test
	public void testGetUsername() {
		System.out.println("getUsername");
		Principal p1 = mock(Principal.class);
		Principal p2 = mock(Principal.class);
		String u1 = Constants.ANONYMOUS;
		String u2 = "username";

		when(p1.getName()).thenReturn(u1);
		when(p2.getName()).thenReturn(u2);
		when(ocelotContext.getPrincipal()).thenReturn(p1).thenReturn(p2);

		assertThat(instance.getUsername()).isEqualTo(u1);
		assertThat(instance.getUsername()).isEqualTo(u2);
	}

	/**
	 * Test of getOutDatedCache method, of class OcelotServices.
	 */
	@Test
	public void testGetOutDatedCache() {
		System.out.println("getOutDatedCache");
		Map<String, Long> states = new HashMap<>();
		when(updatedCacheManager.getOutDatedCache(any(Map.class))).thenReturn(Collections.EMPTY_LIST);
		Collection<String> result = instance.getOutDatedCache(states);
		assertThat(result).isInstanceOf(Collection.class);
	}

	/**
	 * Test of subscribe method, of class OcelotServices.
	 * @throws java.lang.IllegalAccessException
	 */
	@Test
	public void testSubscribe_String() throws IllegalAccessException {
		System.out.println("subscribe");
		when(sessionManager.getSessionById(anyString())).thenReturn(mock(Session.class));
		instance.subscribe("TOPIC");
		verify(topicManager).registerTopicSession(eq("TOPIC"), any(Session.class));
	}

	/**
	 * Test of unsubscribe method, of class OcelotServices.
	 */
	@Test
	public void testUnsubscribe_String() {
		System.out.println("unsubscribe");
		when(sessionManager.getSessionById(anyString())).thenReturn(mock(Session.class));
		instance.unsubscribe("TOPIC");
		verify(topicManager).unregisterTopicSession(eq("TOPIC"), any(Session.class));
	}

	/**
	 * Test of subscribe method, of class OcelotServices.
	 * @throws java.lang.IllegalAccessException
	 */
	@Test
	public void testSubscribe_Session_String() throws IllegalAccessException {
		System.out.println("subscribe");
		when(topicManager.registerTopicSession(anyString(), any(Session.class))).thenReturn(1);
		Integer result = instance.subscribe("TOPIC");
		assertThat(result).isEqualTo(1);
	}

	/**
	 * Test of unsubscribe method, of class OcelotServices.
	 */
	@Test
	public void testUnsubscribe_Session_String() {
		System.out.println("unsubscribe");
		when(topicManager.unregisterTopicSession(anyString(), any(Session.class))).thenReturn(0);
		Integer result = instance.unsubscribe("TOPIC");
		assertThat(result).isEqualTo(0);
	}

	/**
	 * Test of getNumberSubscribers method, of class OcelotServices.
	 */
	@Test
	public void testGetNumberSubscribers() {
		System.out.println("getNumberSubscribers");
		when(topicManager.getNumberSubscribers(anyString())).thenReturn(0);
		Integer result = instance.getNumberSubscribers("TOPIC");
		assertThat(result).isEqualTo(0);
	}
	
	@Test
	public void testGetServices() {
		System.out.println("getServices");
		((FakeCDI)dataservices).add(new ClassAsDataService());
		when(unProxyClassServices.getRealClass(any(Class.class))).thenReturn(ClassAsDataService.class);
		when(serviceTools.getInstanceNameFromDataservice(any(Class.class))).thenReturn("ClassAsDataService");
		doNothing().when(instance).addMethodsToMethodsService(any(Method[].class), any(List.class));
		List<OcelotService> services = instance.getServices();
		assertThat(services).hasSize(1);
	}
	
	/**
	 * Test of addMethodsToMethodsService method, of class OcelotServices.
	 */
	@Test
	public void testAddMethodsToMethodsService() {
		System.out.println("addMethodsToMethodsService");
		Method[] methods = ClassAsDataService.class.getDeclaredMethods();
		List<OcelotMethod> methodsService = mock(List.class);
		when(serviceTools.isConsiderateMethod(any(Method.class))).thenReturn(Boolean.FALSE).thenReturn(Boolean.TRUE).thenReturn(Boolean.TRUE).thenReturn(Boolean.FALSE);
		doReturn(mock(OcelotMethod.class)).when(instance).getOcelotMethod(any(Method.class));
		instance.addMethodsToMethodsService(methods, methodsService);
		verify(methodsService, times(2)).add(any(OcelotMethod.class));
	}

	@Test
	public void testGetOcelotMethod0Arg() throws NoSuchMethodException {
		System.out.println("getOcelotMethod");
		Method method = this.getClass().getDeclaredMethod("methodWith0Arg");
		when(serviceTools.getShortName(anyString())).thenReturn("returntype");
		OcelotMethod result = instance.getOcelotMethod(method);
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo("methodWith0Arg");
		assertThat(result.getReturntype()).isEqualTo("returntype");
		assertThat(result.getArgtypes()).isEmpty();
		assertThat(result.getArgnames()).isEmpty();
		assertThat(result.getArgtemplates()).isEmpty();
	}
	
	@Test
	public void testGetOcelotMethod2Args() throws NoSuchMethodException, JsonMarshallerException {
		System.out.println("getOcelotMethod");
		Method method = this.getClass().getDeclaredMethod("methodWith2Args", String.class, String.class);
		when(serviceTools.getShortName(anyString())).thenReturn("returntype").thenReturn("argtype");
		when(serviceTools.getTemplateOfType(any(Type.class), any(IJsonMarshaller.class))).thenReturn("template");
		OcelotMethod result = instance.getOcelotMethod(method);
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo("methodWith2Args");
		assertThat(result.getReturntype()).isEqualTo("returntype");
		assertThat(result.getArgtypes()).hasSize(2);
		assertThat(result.getArgnames()).hasSize(2);
		assertThat(result.getArgtemplates()).hasSize(2);
	}

	@Test
	public void testGetOcelotMethod2ArgsWithOneUnmarshalled() throws NoSuchMethodException, JsonMarshallerException {
		System.out.println("getOcelotMethod");
		Method method = this.getClass().getDeclaredMethod("methodWith2Args", String.class, String.class);
		when(serviceTools.getShortName(anyString())).thenReturn("returntype").thenReturn("argtype");
		when(serviceTools.getTemplateOfType(any(Type.class), any(IJsonMarshaller.class))).thenReturn("template").thenThrow(JsonMarshallerException.class);
		OcelotMethod result = instance.getOcelotMethod(method);
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo("methodWith2Args");
		assertThat(result.getReturntype()).isEqualTo("returntype");
		assertThat(result.getArgtypes()).hasSize(2);
		assertThat(result.getArgnames()).hasSize(2);
		assertThat(result.getArgtemplates()).hasSize(2);
		assertThat(result.getArgtemplates().get(1)).isEqualTo("java.lang.String");
	}

	private void methodWith0Arg() {
		
	}
	private void methodWith2Args(String a, String b) {
		
	}
}
