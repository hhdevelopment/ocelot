/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ocelotds.objects;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author hhfrancois
 */
@RunWith(MockitoJUnitRunner.class)
public class OptionsTest {

	@InjectMocks
	@Spy
	Options instance;

	/**
	 * Test of isMonitor method, of class Options.
	 */
	@Test
	public void testIsMonitor() {
		System.out.println("isMonitor");
		instance.monitor = false;
		boolean result = instance.isMonitor();
		assertThat(result).isFalse();
		instance.monitor = true;
		result = instance.isMonitor();
		assertThat(result).isTrue();
	}

	/**
	 * Test of setMonitor method, of class Options.
	 */
	@Test
	public void testSetMonitor() {
		System.out.println("setMonitor");
		instance.setMonitor(false);
		assertThat(instance.monitor).isFalse();
		instance.setMonitor(true);
		assertThat(instance.monitor).isTrue();
	}

	/**
	 * Test of isDebug method, of class Options.
	 */
	@Test
	public void testIsDebug() {
		System.out.println("isDebug");
		instance.debug = false;
		boolean result = instance.isDebug();
		assertThat(result).isFalse();
		instance.debug = true;
		result = instance.isDebug();
		assertThat(result).isTrue();
	}

	/**
	 * Test of setDebug method, of class Options.
	 */
	@Test
	public void testSetDebug() {
		System.out.println("setDebug");
		instance.setDebug(false);
		assertThat(instance.debug).isFalse();
		instance.setDebug(true);
		assertThat(instance.debug).isTrue();
	}
}