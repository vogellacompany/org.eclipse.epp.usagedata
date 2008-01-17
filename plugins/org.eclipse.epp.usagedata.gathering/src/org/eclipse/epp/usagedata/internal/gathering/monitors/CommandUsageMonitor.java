/*******************************************************************************
 * Copyright (c) 2007 The Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    The Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.epp.usagedata.internal.gathering.monitors;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.epp.usagedata.internal.gathering.Activator;
import org.eclipse.epp.usagedata.internal.gathering.services.UsageDataService;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

/**
 * Instances of this class monitor invocations of commands in the workbench.
 * 
 * @author Wayne Beaton
 */
public class CommandUsageMonitor implements UsageMonitor {

	/**
	 * The {@link #executionListener} is installed into the {@link ICommandService}
	 * so that it can be notified when a command is invoked.
	 */
	private IExecutionListener executionListener;
	
	/**
	 * The {@link #commandToBundleMappings} maps the id of a command
	 * to the symbolic name of the bundle that defines it.
	 */
	private Map<String, String> commandToBundleMappings;
	
	/**
	 * The {@link #bundleListener} keeps track of bundle installations
	 * events; when a bundle is installed or uninstalled, the {@link #commandToBundleMappings}
	 * must be recomputed.
	 */
	private BundleListener bundleListener;
		
	// TODO Sort out why this is causing compiler errors.
	//@Override
	public void startMonitoring(final UsageDataService usageDataService) {
		bundleListener = new BundleListener() {
			public void bundleChanged(BundleEvent event) {
				if (needToRebuildCommandToBundleMapping(event)) {
					clearCommandToBundleMapping();
				}
			}	
		};
		getBundleContext().addBundleListener(bundleListener);
		
		executionListener = new IExecutionListener() {
			// TODO Sort out why this is causing compiler errors.
			//@Override
			public void notHandled(String commandId, NotHandledException exception) {
				recordEvent("no handler", usageDataService, commandId);				
			}

			// TODO Sort out why this is causing compiler errors.
			//@Override
			public void postExecuteFailure(String commandId, ExecutionException exception) {
				recordEvent("failed", usageDataService, commandId);				
			}

			// TODO Sort out why this is causing compiler errors.
			//@Override
			public void postExecuteSuccess(String commandId, Object returnValue) {
				recordEvent("executed", usageDataService, commandId);				
			}

			// TODO Sort out why this is causing compiler errors.
			//@Override
			public void preExecute(String commandId, ExecutionEvent event) {
				
			}			
		};
		getCommandService().addExecutionListener(executionListener);
	}

	private ICommandService getCommandService() {
		return (ICommandService) PlatformUI.getWorkbench().getAdapter(ICommandService.class);
	}
	
	// TODO Sort out why this is causing compiler errors.
	//@Override
	public void stopMonitoring() {
		ICommandService commandService = getCommandService();
		if (commandService != null) commandService.removeExecutionListener(executionListener);
		getBundleContext().removeBundleListener(bundleListener);
	}

	private void recordEvent(String what,
			final UsageDataService usageDataService, String commandId) {
		usageDataService.recordEvent(what, "command", commandId, getBundleName(commandId));
	}

	private BundleContext getBundleContext() {
		return Activator.getDefault().getBundle().getBundleContext();
	}
	
	/**
	 * This method answers true if the {@link #commandToBundleMappings} needs to
	 * be rebuilt. This is only true when a bundle has been installed or uninstalled.
	 * AFAIK, these are the only bundle events that would trigger a change in the
	 * extension registry.
	 * 
	 * @param event
	 * @return
	 */
	private boolean needToRebuildCommandToBundleMapping(BundleEvent event) {
		if (event.getType() == BundleEvent.INSTALLED) return true;
		if (event.getType() == BundleEvent.UNINSTALLED) return true;
		return false;
	}		
	
	/**
	 * This method fetches the bundle id (symbolic name) of the bundle that defines
	 * the command, commandId. Since commands are defined in the plugin.xml, the bundle
	 * that defines it must be a singleton which means that there will only be one version
	 * of the bundle loaded. Happy day.
	 * 
	 * @param commandId
	 * @return
	 */
	protected synchronized String getBundleName(String commandId) {
		// TODO Can this implementation be generalized?
		/*
		 * This method is synchronized. Frankly, I hate synchronizing methods, but since
		 * this is currently the only activity that needs synchronization, and the entire
		 * activity needs to be synchronized, I'm going for it. 
		 */
		updateCommandToBundleMappings();
		return commandToBundleMappings.get(commandId);
	}

	private synchronized void clearCommandToBundleMapping() {
		commandToBundleMappings = null;		
	}
	
	/**
	 * This method walks through the commands registered via the extension registry
	 * and creates the {@link #commandToBundleMappings}.
	 */
	private synchronized void updateCommandToBundleMappings() {
		if (commandToBundleMappings != null) return;
		commandToBundleMappings = new HashMap<String, String>();
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.ui.commands");
		for (IConfigurationElement element : elements) {
			if ("command".equals(element.getName())) {
				commandToBundleMappings.put(element.getAttribute("id"), element.getContributor().getName());
			}
		}
	}
}
