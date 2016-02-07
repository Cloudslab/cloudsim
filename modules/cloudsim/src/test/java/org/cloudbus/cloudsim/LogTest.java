/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

/**
 * @author		Anton Beloglazov
 * @since		CloudSim Toolkit 2.0
 */
public class LogTest {

	private static final ByteArrayOutputStream OUTPUT = new ByteArrayOutputStream();
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
        private static final DecimalFormatSymbols dfs = 
            DecimalFormatSymbols.getInstance(Locale.getDefault(Locale.Category.FORMAT));

	@Before
	public void setUp() throws Exception {
		Log.setOutput(OUTPUT);
	}

	@Test
	public void testPrint() throws IOException {
		Log.print("test test");
		assertEquals("test test", OUTPUT.toString());
		OUTPUT.reset();

		Log.print(123);
		assertEquals("123", OUTPUT.toString());
		OUTPUT.reset();

		Log.print(123L);
		assertEquals("123", OUTPUT.toString());
		OUTPUT.reset();

		Log.print(123.0);
		assertEquals("123.0", OUTPUT.toString());
		OUTPUT.reset();
	}

	@Test
	public void testPrintLine() throws IOException {
		Log.printLine("test test");
		assertEquals("test test" + LINE_SEPARATOR, OUTPUT.toString());
		OUTPUT.reset();

		Log.printLine(123);
		assertEquals("123" + LINE_SEPARATOR, OUTPUT.toString());
		OUTPUT.reset();

		Log.printLine(123L);
		assertEquals("123" + LINE_SEPARATOR, OUTPUT.toString());
		OUTPUT.reset();

		Log.printLine(123.0);
		assertEquals("123.0" + LINE_SEPARATOR, OUTPUT.toString());
		OUTPUT.reset();
	}

	@Test
	public void testFormat() throws IOException {
		Log.format("test %s test", "test");
		assertEquals("test test test", OUTPUT.toString());
		OUTPUT.reset();

		Log.format("%d", 123);
		assertEquals("123", OUTPUT.toString());
		OUTPUT.reset();

		Log.format("%d", 123L);
		assertEquals("123", OUTPUT.toString());
		OUTPUT.reset();

		Log.format("%.2f", 123.01);
		assertEquals("123"+dfs.getDecimalSeparator()+"01", OUTPUT.toString());
		OUTPUT.reset();
	}

	@Test
	public void testFormatLine() throws IOException {
                OUTPUT.reset();
		Log.formatLine("test %s test", "test");
		assertEquals("test test test" + LINE_SEPARATOR, OUTPUT.toString());
		OUTPUT.reset();

		Log.formatLine("%d", 123);
		assertEquals("123" + LINE_SEPARATOR, OUTPUT.toString());
		OUTPUT.reset();

		Log.formatLine("%d", 123L);
		assertEquals("123" + LINE_SEPARATOR, OUTPUT.toString());
		OUTPUT.reset();

		Log.formatLine("%.2f", 123.01);
		assertEquals("123"+dfs.getDecimalSeparator()+"01" + LINE_SEPARATOR, OUTPUT.toString());
		OUTPUT.reset();
	}

	@Test
	public void testDisable() throws IOException {
		OUTPUT.reset();
		assertFalse(Log.isDisabled());

		Log.print("test test");
		assertEquals("test test", OUTPUT.toString());
		OUTPUT.reset();

		Log.printLine("test test");
		assertEquals("test test" + LINE_SEPARATOR, OUTPUT.toString());
		OUTPUT.reset();

		Log.format("test %s test", "test");
		assertEquals("test test test", OUTPUT.toString());
		OUTPUT.reset();

		Log.formatLine("test %s test", "test");
		assertEquals("test test test" + LINE_SEPARATOR, OUTPUT.toString());
		OUTPUT.reset();

		Log.disable();

		assertTrue(Log.isDisabled());

		Log.print("test test");
		assertEquals("", OUTPUT.toString());
		OUTPUT.reset();

		Log.printLine("test test");
		assertEquals("", OUTPUT.toString());
		OUTPUT.reset();

		Log.format("test %s test", "test");
		assertEquals("", OUTPUT.toString());
		OUTPUT.reset();

		Log.formatLine("test %s test", "test");
		assertEquals("", OUTPUT.toString());
		OUTPUT.reset();

		Log.enable();

		assertFalse(Log.isDisabled());

		Log.print("test test");
		assertEquals("test test", OUTPUT.toString());
		OUTPUT.reset();

		Log.printLine("test test");
		assertEquals("test test" + LINE_SEPARATOR, OUTPUT.toString());
		OUTPUT.reset();

		Log.format("test %s test", "test");
		assertEquals("test test test", OUTPUT.toString());
		OUTPUT.reset();

		Log.formatLine("test %s test", "test");
		assertEquals("test test test" + LINE_SEPARATOR, OUTPUT.toString());
		OUTPUT.reset();
	}

}
