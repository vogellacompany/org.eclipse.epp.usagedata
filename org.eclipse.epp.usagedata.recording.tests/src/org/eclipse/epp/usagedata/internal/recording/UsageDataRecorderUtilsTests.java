/*******************************************************************************
 * Copyright (c) 2009 The Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    The Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.epp.usagedata.internal.recording;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;

import org.eclipse.epp.usagedata.internal.gathering.events.UsageDataEvent;
import org.junit.Test;

public class UsageDataRecorderUtilsTests {

	@Test
	public void testWriteHeader() throws Exception {
		StringWriter writer = new StringWriter();
		UsageDataRecorderUtils.writeHeader(writer);
		assertEquals("what,kind,bundleId,bundleVersion,description,time\n", writer.toString());
	}

	@Test
	public void testWriteEvent() throws Exception {
		UsageDataEvent event = new UsageDataEvent("activate", "view", "myview", "mybundle", "1.0", 1000);
		StringWriter writer = new StringWriter();
		UsageDataRecorderUtils.writeEvent(writer, event);
		assertEquals("activate,view,mybundle,1.0,\"myview\",1000\n", writer.toString());
	}

	@Test
	public void testEncode() {
		assertEquals("\"first\"", UsageDataRecorderUtils.encode("first"));
		assertEquals("\"first, second\"", UsageDataRecorderUtils.encode("first, second"));
		assertEquals("\"first, \"\"second\"\"\"", UsageDataRecorderUtils.encode("first, \"second\""));
		assertEquals("\"first\\nsecond\"", UsageDataRecorderUtils.encode("first\nsecond"));
	}	
	
	@Test
	public void testSplitLine() {
		String[] strings = UsageDataRecorderUtils.splitLine("x,y,z");
		assertEquals(3, strings.length);
		assertEquals("x", strings[0]);
		assertEquals("y", strings[1]);
		assertEquals("z", strings[2]);
	}
	
	@Test
	public void testSplitLineWithEscapedQuotes() {
		String[] strings = UsageDataRecorderUtils.splitLine("x,\"\"\"y\"\"\",z");
		assertEquals(3, strings.length);
		assertEquals("x", strings[0]);
		assertEquals("\"y\"", strings[1]);
		assertEquals("z", strings[2]);
	}

	@Test
	public void testSplitLineWithQuotesAndCommas() {
		String[] strings = UsageDataRecorderUtils.splitLine("first,\"second, third\",fourth");
		assertEquals(3, strings.length);
		assertEquals("first", strings[0]);
		assertEquals("second, third", strings[1]);
		assertEquals("fourth", strings[2]);
	}
	
	@Test
	public void testSplitLineWithEscapedQuotesAndCommas() {
		String[] strings = UsageDataRecorderUtils.splitLine("first,\"\"\"second\"\", third\",fourth");
		assertEquals(3, strings.length);
		assertEquals("first", strings[0]);
		assertEquals("\"second\", third", strings[1]);
		assertEquals("fourth", strings[2]);
	}

	@Test
	public void testSplitLineUnquotedEscapedQuote() throws Exception {
		String[] strings = UsageDataRecorderUtils.splitLine("first,\"\"second");
		assertEquals(2, strings.length);
		assertEquals("first", strings[0]);
		assertEquals("\"second", strings[1]);
	}

	/**
	 * A line, very much like the one listed here (names have been changed
	 * to protect the innocent) caused some grief with an older implementation
	 * of the splitLine method (bug 278857). 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSplitLineTroublesomeLineFoundInField() throws Exception {
		String[] strings = UsageDataRecorderUtils.splitLine("error,log,,,\"java.lang.NullPointerException\r\n\tat stuff.actions.StuffAction.selectionChanged(StuffAction.java:99)\r\n\tat org.eclipse.ui.internal.PluginAction.refreshEnablement(PluginAction.java:206)\r\n\tat org.eclipse.ui.internal.PluginAction.selectionChanged(PluginAction.java:277)\r\n\tat org.eclipse.ui.internal.ObjectPluginAction.partClosed(ObjectPluginAction.java:49)\r\n\tat org.eclipse.ui.internal.PartListenerList2$3.run(PartListenerList2.java:100)\r\n\tat org.eclipse.core.runtime.SafeRunner.run(SafeRunner.java:42)\r\n\tat org.eclipse.core.runtime.Platform.run(Platform.java:888)\r\n\tat org.eclipse.ui.internal.PartListenerList2.fireEvent(PartListenerList2.java:55)\r\n\tat org.eclipse.ui.internal.PartListenerList2.firePartClosed(PartListenerList2.java:98)\r\n\tat org.eclipse.ui.internal.PartService.firePartClosed(PartService.java:227)\r\n\tat org.eclipse.ui.internal.WorkbenchPagePartList.firePartClosed(WorkbenchPagePartList.java:39)\r\n\tat org.eclipse.ui.internal.PartList.partClosed(PartList.java:274)\r\n\tat org.eclipse.ui.internal.PartList.removePart(PartList.java:186)\r\n\tat org.eclipse.ui.internal.WorkbenchPage.disposePart(WorkbenchPage.java:1714)\r\n\tat org.eclipse.ui.internal.WorkbenchPage.handleDeferredEvents(WorkbenchPage.java:1422)\r\n\tat org.eclipse.ui.internal.WorkbenchPage.deferUpdates(WorkbenchPage.java:1406)\r\n\tat org.eclipse.ui.internal.WorkbenchPage.closeEditors(WorkbenchPage.java:1380)\r\n\tat org.eclipse.ui.internal.WorkbenchPage.closeEditor(WorkbenchPage.java:1444)\r\n\tat org.eclipse.ui.texteditor.AbstractTextEditor$23.run(AbstractTextEditor.java:4234)\r\n\tat org.eclipse.swt.widgets.RunnableLock.run(RunnableLock.java:35)\r\n\tat org.eclipse.swt.widgets.Synchronizer.runAsyncMessages(Synchronizer.java:134)\r\n\tat org.eclipse.swt.widgets.Display.runAsyncMessages(Display.java:3855)\r\n\tat org.eclipse.swt.widgets.Display.readAndDispatch(Display.java:3476)\r\n\tat org.eclipse.jface.window.Window.runEventLoop(Window.java:825)\r\n\tat org.eclipse.jface.window.Window.open(Window.java:801)\r\n\tat org.eclipse.jface.dialogs.MessageDialog.open(MessageDialog.java:327)\r\n\tat org.eclipse.ui.internal.EditorManager.saveAll(EditorManager.java:1164)\r\n\tat org.eclipse.ui.internal.Workbench$17.run(Workbench.java:1005)\r\n\tat org.eclipse.core.runtime.SafeRunner.run(SafeRunner.java:42)\r\n\tat org.eclipse.ui.internal.Workbench.saveAllEditors(Workbench.java:954)\r\n\tat org.eclipse.ui.internal.Workbench.busyClose(Workbench.java:872)\r\n\tat org.eclipse.ui.internal.Workbench.access$15(Workbench.java:856)\r\n\tat org.eclipse.ui.internal.Workbench$23.run(Workbench.java:1100)\r\n\tat org.eclipse.swt.custom.BusyIndicator.showWhile(BusyIndicator.java:70)\r\n\tat org.eclipse.ui.internal.Workbench.close(Workbench.java:1098)\r\n\tat org.eclipse.ui.internal.Workbench.close(Workbench.java:1070)\r\n\tat org.eclipse.ui.internal.WorkbenchWindow.busyClose(WorkbenchWindow.java:720)\r\n\tat org.eclipse.ui.internal.WorkbenchWindow.access$0(WorkbenchWindow.java:699)\r\n\tat org.eclipse.ui.internal.WorkbenchWindow$5.run(WorkbenchWindow.java:815)\r\n\tat org.eclipse.swt.custom.BusyIndicator.showWhile(BusyIndicator.java:70)\r\n\tat org.eclipse.ui.internal.WorkbenchWindow.close(WorkbenchWindow.java:813)\r\n\tat org.eclipse.jface.window.Window.handleShellCloseEvent(Window.java:741)\r\n\tat org.eclipse.jface.window.Window$3.shellClosed(Window.java:687)\r\n\tat org.eclipse.swt.widgets.TypedListener.handleEvent(TypedListener.java:92)\r\n\tat org.eclipse.swt.widgets.EventTable.sendEvent(EventTable.java:84)\r\n\tat org.eclipse.swt.widgets.Widget.sendEvent(Widget.java:1003)\r\n\tat org.eclipse.swt.widgets.Widget.sendEvent(Widget.java:1027)\r\n\tat org.eclipse.swt.widgets.Widget.sendEvent(Widget.java:1012)\r\n\tat org.eclipse.swt.widgets.Decorations.closeWidget(Decorations.java:308)\r\n\tat org.eclipse.swt.widgets.Decorations.WM_CLOSE(Decorations.java:1645)\r\n\tat org.eclipse.swt.widgets.Control.windowProc(Control.java:3948)\r\n\tat org.eclipse.swt.widgets.Canvas.windowProc(Canvas.java:342)\r\n\tat org.eclipse.swt.widgets.Decorations.windowProc(Decorations.java:1578)\r\n\tat org.eclipse.swt.widgets.Shell.windowProc(Shell.java:2010)\r\n\tat org.eclipse.swt.widgets.Display.windowProc(Display.java:4589)\r\n\tat org.eclipse.swt.internal.win32.OS.DefWindowProcW(Native Method)\r\n\tat org.eclipse.swt.internal.win32.OS.DefWindowProc(OS.java:2404)\r\n\tat org.eclipse.swt.widgets.Shell.callWindowProc(Shell.java:492)\r\n\tat org.eclipse.swt.widgets.Control.windowProc(Control.java:4036)\r\n\tat org.eclipse.swt.widgets.Canvas.windowProc(Canvas.java:342)\r\n\tat org.eclipse.swt.widgets.Decorations.windowProc(Decorations.java:1578)\r\n\tat org.eclipse.swt.widgets.Shell.windowProc(Shell.java:2010)\r\n\tat org.eclipse.swt.widgets.Display.windowProc(Display.java:4589)\r\n\tat org.eclipse.swt.internal.win32.OS.DefWindowProcW(Native Method)\r\n\tat org.eclipse.swt.internal.win32.OS.DefWindowProc(OS.java:2404)\r\n\tat org.eclipse.swt.widgets.Shell.callWindowProc(Shell.java:492)\r\n\tat org.eclipse.swt.widgets.Control.windowProc(Control.java:4036)\r\n\tat org.eclipse.swt.widgets.Canvas.windowProc(Canvas.java:342)\r\n\tat org.eclipse.swt.widgets.Decorations.windowProc(Decorations.java:1578)\r\n\tat org.eclipse.swt.widgets.Shell.windowProc(Shell.java:2010)\r\n\tat org.eclipse.swt.widgets.Display.windowProc(Display.java:4589)\r\n\tat org.eclipse.swt.internal.win32.OS.DispatchMessageW(Native Method)\r\n\tat org.eclipse.swt.internal.win32.OS.DispatchMessage(OS.java:2409)\r\n\tat org.eclipse.swt.widgets.Display.readAndDispatch(Display.java:3471)\r\n\tat org.eclipse.ui.internal.Workbench.runEventLoop(Workbench.java:2405)\r\n\tat org.eclipse.ui.internal.Workbench.runUI(Workbench.java:2369)\r\n\tat org.eclipse.ui.internal.Workbench.access$4(Workbench.java:2221)\r\n\tat org.eclipse.ui.internal.Workbench$5.run(Workbench.java:500)\r\n\tat org.eclipse.core.databinding.observable.Realm.runWithDefault(Realm.java:332)\r\n\tat org.eclipse.ui.internal.Workbench.createAndRunWorkbench(Workbench.java:493)\r\n\tat org.eclipse.ui.PlatformUI.createAndRunWorkbench(PlatformUI.java:149)\r\n\tat org.eclipse.ui.internal.ide.application.IDEApplication.start(IDEApplication.java:113)\r\n\tat org.eclipse.equinox.internal.app.EclipseAppHandle.run(EclipseAppHandle.java:194)\r\n\tat org.eclipse.core.runtime.internal.adaptor.EclipseAppLauncher.runApplication(EclipseAppLauncher.java:110)\r\n\tat org.eclipse.core.runtime.internal.adaptor.EclipseAppLauncher.start(EclipseAppLauncher.java:79)\r\n\tat org.eclipse.core.runtime.adaptor.EclipseStarter.run(EclipseStarter.java:368)\r\n\tat org.eclipse.core.runtime.adaptor.EclipseStarter.run(EclipseStarter.java:179)\r\n\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\r\n\tat sun.reflect.NativeMethodAccessorImpl.invoke(Unknown Source)\r\n\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(Unknown Source)\r\n\tat java.lang.reflect.Method.invoke(Unknown Source)\r\n\tat org.eclipse.equinox.launcher.Main.invokeFramework(Main.java:559)\r\n\tat org.eclipse.equinox.launcher.Main.basicRun(Main.java:514)\r\n\tat org.eclipse.equinox.launcher.Main.run(Main.java:1311)\r\n\",1243528984485");
		assertEquals(6, strings.length);
		assertEquals("error", strings[0]);
		assertEquals("log", strings[1]);
		assertEquals("1243528984485", strings[5]);
	}
	
	/**
	 * The input that splitLine expects is generated by UsageDataRecorderUtils
	 * and so should be of the correct format. But what if it isn't?
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSplitLineMissingEndQuote() throws Exception {
		String[] strings = UsageDataRecorderUtils.splitLine("first,\"second");
		assertEquals(2, strings.length);
		assertEquals("first", strings[0]);
		assertEquals("second", strings[1]);
	}
		
	@Test
	public void testSplitLineCommaAtEnd() throws Exception {
		String[] strings = UsageDataRecorderUtils.splitLine("first,second,");
		assertEquals(3, strings.length);
		assertEquals("first", strings[0]);
		assertEquals("second", strings[1]);
		assertEquals("", strings[2]);
	}

	@Test
	public void testSplitLineCommaAtBeginning() throws Exception {
		String[] strings = UsageDataRecorderUtils.splitLine(",first,second");
		assertEquals(3, strings.length);
		assertEquals("", strings[0]);
		assertEquals("first", strings[1]);
		assertEquals("second", strings[2]);
	}

	@Test
	public void testSplitLineEmptyMiddleField() throws Exception {
		String[] strings = UsageDataRecorderUtils.splitLine("first,,second");
		assertEquals(3, strings.length);
		assertEquals("first", strings[0]);
		assertEquals("", strings[1]);
		assertEquals("second", strings[2]);
	}

	@Test
	public void testSplitLineWithJustOneField() throws Exception {
		String[] strings = UsageDataRecorderUtils.splitLine("first");
		assertEquals(1, strings.length);
		assertEquals("first", strings[0]);
	}

	@Test
	public void testSplitLineWithEmptyInput() throws Exception {
		String[] strings = UsageDataRecorderUtils.splitLine("");
		assertEquals(1, strings.length);
		assertEquals("", strings[0]);
	}
	
	@Test
	public void testSplitLineWithWhitespaceOnly() throws Exception {
		String[] strings = UsageDataRecorderUtils.splitLine(" ");
		assertEquals(1, strings.length);
		assertEquals(" ", strings[0]);
	}
	
}
