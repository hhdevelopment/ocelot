/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.ocelotds.web;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import javax.enterprise.inject.Instance;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ocelotds.Constants;
import org.ocelotds.IServicesProvider;
import org.ocelotds.configuration.OcelotConfiguration;
import org.ocelotds.objects.FakeCDI;
import org.ocelotds.objects.HtmlServiceProviderImpl;
import org.ocelotds.objects.JsServiceProviderImpl;
import org.ocelotds.security.SecurityServices;
import org.slf4j.Logger;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class ContextListenerTest {

	private String ocelotjspath = null;
	private String ocelotminjspath = null;
	private String ocelothtmlpath = null;

	@Mock
	private Logger logger;

	@InjectMocks
	@Spy
	private ContextListener contextListener;

	@Mock
	private OcelotConfiguration configuration;

	@Mock 
	private SecurityServices subjectServices;

	@Spy
	private Instance<IServicesProvider> jsServicesProviders = new FakeCDI<>();
	@Spy
	private Instance<IServicesProvider> htmlServicesProviders = new FakeCDI<>();

	/**
	 * Test of contextInitialized method, of class ContextListener.
	 */
	@Test
	public void testContextInitialized() {
		System.out.println("contextInitialized");
		ServletContext sc = mock(ServletContext.class);
		ServletContextEvent sce = mock(ServletContextEvent.class);
		when(sce.getServletContext()).thenReturn(sc);
		when(sc.getContextPath()).thenReturn("/");

		contextListener.contextInitialized(sce);

		ArgumentCaptor<String> captureKey = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> capturePath = ArgumentCaptor.forClass(String.class);
		verify(sc, times(3)).setInitParameter(captureKey.capture(), capturePath.capture());
		List<String> keys = captureKey.getAllValues();
		List<String> paths = capturePath.getAllValues();

		assertThat(keys.get(0)).isEqualTo(Constants.OCELOT);
		assertThat(keys.get(1)).isEqualTo(Constants.OCELOT_MIN);
		assertThat(keys.get(2)).isEqualTo(Constants.OCELOT_HTML);

		ocelotjspath = paths.get(0);
		ocelotminjspath = paths.get(1);
		ocelothtmlpath = paths.get(2);

		File ocelotjs = new File(ocelotjspath);
		assertThat(ocelotjs).exists();
		File ocelotminjs = new File(ocelotminjspath);
		assertThat(ocelotminjs).exists();
		File ocelothtml = new File(ocelothtmlpath);
		assertThat(ocelothtml).exists();
	}

	/**
	 * Test of createJsFile method, of class ContextListener.
	 * @throws java.io.IOException
	 */
	@Test
	public void testCreateJsFileFailed() throws IOException {
		System.out.println("createJsFile");
		ServletContext sc = mock(ServletContext.class);
		when(sc.getContextPath()).thenReturn("/");
		when(contextListener.createOcelotJsFile(anyString(), anyString())).thenThrow(IOException.class);

		contextListener.createJsFile(sc);
		
		ArgumentCaptor<String> captureLog = ArgumentCaptor.forClass(String.class);
		verify(logger).error(captureLog.capture(), any(IOException.class));
		assertThat(captureLog.getValue()).isEqualTo("Fail to create ocelot.js.");
	}

	/**
	 * Test of createHtmlFile method, of class ContextListener.
	 * @throws java.io.IOException
	 */
	@Test
	public void testCreateHtmlFileFailed() throws IOException {
		System.out.println("createHtmlFile");
		ServletContext sc = mock(ServletContext.class);
		when(sc.getContextPath()).thenReturn("/");
		when(contextListener.createOcelotHtmlFile(anyString())).thenThrow(IOException.class);

		contextListener.createHtmlFile(sc);
		
		ArgumentCaptor<String> captureLog = ArgumentCaptor.forClass(String.class);
		verify(logger).error(captureLog.capture(), any(IOException.class));
		assertThat(captureLog.getValue()).isEqualTo("Fail to create ocelot.html.");
	}

	@Test
	public void testGetWSProtocol() {
		System.out.println("getWSProtocol");
		ServletContext sc = mock(ServletContext.class);
		when(sc.getInitParameter(Constants.Options.SECURE)).thenReturn("false").thenReturn("true");
		String result = contextListener.getWSProtocol(sc);
		assertThat(result).isEqualTo(Constants.WS);
		result = contextListener.getWSProtocol(sc);
		assertThat(result).isEqualTo(Constants.WSS);
	}
	/**
	 * Test of defineStacktraceConfig method, of class ContextListener.
	 */
	@Test
	public void testDefineStacktraceConfig() {
		System.out.println("defineStacktraceConfig");
		ServletContext sc = mock(ServletContext.class);

		when(sc.getInitParameter(eq(Constants.Options.STACKTRACE_LENGTH))).thenReturn(null);
		contextListener.defineStacktraceConfig(sc);

		when(sc.getInitParameter(eq(Constants.Options.STACKTRACE_LENGTH))).thenReturn("20");
		contextListener.defineStacktraceConfig(sc);

		ArgumentCaptor<Integer> captureLength = ArgumentCaptor.forClass(Integer.class);
		verify(configuration, times(2)).setStacktracelength(captureLength.capture());

		assertThat(captureLength.getAllValues().get(0)).isEqualTo(50);
		assertThat(captureLength.getAllValues().get(1)).isEqualTo(20);
	}

	/**
	 * Test of contextDestroyed method, of class ContextListener.
	 */
	@Test
	public void testZContextDestroyed() {
		testContextInitialized();
		System.out.println("contextDestroyed");
		ServletContext sc = mock(ServletContext.class);
		ServletContextEvent sce = mock(ServletContextEvent.class);
		when(sce.getServletContext()).thenReturn(sc);
		when(sc.getInitParameter(eq(Constants.OCELOT))).thenReturn(ocelotjspath);
		when(sc.getInitParameter(eq(Constants.OCELOT_MIN))).thenReturn(ocelotminjspath);
		when(sc.getInitParameter(eq(Constants.OCELOT_HTML))).thenReturn(ocelothtmlpath);

		
		contextListener.contextDestroyed(sce);

		File ocelotjs = new File(ocelotjspath);
		assertThat(ocelotjs).doesNotExist();
		File ocelotminjs = new File(ocelotminjspath);
		assertThat(ocelotminjs).doesNotExist();
		File ocelothtml = new File(ocelothtmlpath);
		assertThat(ocelothtml).doesNotExist();
	}

	/**
	 * Test of deleteFile method, of class ContextListener.
	 */
	@Test
	public void testDeleteFile() throws IOException {
		System.out.println("deleteFile");
		String filename = null;
		contextListener.deleteFile(filename);
		filename = "";
		contextListener.deleteFile(filename);
		filename = "file.js";
		contextListener.deleteFile(filename);
		File f = File.createTempFile("file", ".js");
		filename = f.getAbsolutePath();
		contextListener.deleteFile(filename);
	}

	/**
	 * Test of setInitParameterAnMinifyJs method, of class ContextListener.
	 * @throws java.io.IOException
	 */
	@Test
	public void testSetInitParameterAnMinifyJs() throws IOException {
		ServletContext sc = mock(ServletContext.class);
		File file = mock(File.class);
		when(file.getAbsolutePath()).thenReturn("path");
		contextListener.setInitParameterAnMinifyJs(sc, file, "str1", "str2");
		ArgumentCaptor<String> captureLog = ArgumentCaptor.forClass(String.class);
		verify(logger).error(captureLog.capture());
		assertThat(captureLog.getValue()).isEqualTo("Minification from str1 to str2 failed. minify version will be equals to normal version.");
	}

	/**
	 * Test of createOcelotJsFile method, of class ContextListener.
	 * @throws java.io.IOException
	 */
	@Test
	public void testCreateOcelotJsFile() throws IOException {
		System.out.println("createOcelotJsFile");
		((FakeCDI)jsServicesProviders).add(new JsServiceProviderImpl());
		File file = contextListener.createOcelotJsFile("/", "ws");
		assertThat(file).exists();
	}
	
	/**
	 * Test of writeOcelotCoreJsFile method, of class ContextListener.
	 * @throws java.io.IOException
	 */
	@Test(expected = IOException.class)
	public void testWriteOcelotCoreJsFile() throws IOException {
		System.out.println("writeOcelotCoreJsFile");
		OutputStream out = mock(OutputStream.class);
		contextListener.OCELOT_CORE_RESOURCE = "/badfile";
		contextListener.writeOcelotCoreJsFile(out, "/", "ws");
	}

	/**
	 * Test of createOcelotHtmlFile method, of class ContextListener.
	 * @throws java.io.IOException
	 */
	@Test
	public void testCreateOcelotHtmlFile() throws IOException {
		System.out.println("createOcelotJsFile");
		((FakeCDI)htmlServicesProviders).add(new HtmlServiceProviderImpl());
		File file = contextListener.createOcelotHtmlFile("/");
		assertThat(file).exists();
	}
}
