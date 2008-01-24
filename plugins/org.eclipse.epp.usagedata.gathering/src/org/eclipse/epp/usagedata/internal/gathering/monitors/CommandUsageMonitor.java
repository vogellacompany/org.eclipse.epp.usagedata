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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.epp.usagedata.internal.gathering.services.UsageDataService;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

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
	
	private ExtensionIdToBundleMapper commandToBundleIdMapper;
		
	public void startMonitoring(final UsageDataService usageDataService) {		
		executionListener = new IExecutionListener() {
			public void notHandled(String commandId, NotHandledException exception) {
				recordEvent("no handler", usageDataService, commandId);				
			}

			public void postExecuteFailure(String commandId, ExecutionException exception) {
				recordEvent("failed", usageDataService, commandId);				
			}

			public void postExecuteSuccess(String commandId, Object returnValue) {
				recordEvent("executed", usageDataService, commandId);				
			}

			public void preExecute(String commandId, ExecutionEvent event) {
				
			}			
		};
		getCommandService().addExecutionListener(executionListener);
		commandToBundleIdMapper = new ExtensionIdToBundleMapper("org.eclipse.ui.commands");
	}

	private ICommandService getCommandService() {
		return (ICommandService) PlatformUI.getWorkbench().getAdapter(ICommandService.class);
	}
	
	public void stopMonitoring() {
		ICommandService commandService = getCommandService();
		if (commandService != null) commandService.removeExecutionListener(executionListener);
		commandToBundleIdMapper.dispose();
	}

	private void recordEvent(String what,
			final UsageDataService usageDataService, String commandId) {
		usageDataService.recordEvent(what, "command", commandId, getBundleId(commandId));
	}
	
	/**
	 * This method fetches the bundle id (symbolic name) of the bundle that defines
	 * the command, commandId. 
	 * 
	 * @param commandId
	 * @return
	 */
	protected synchronized String getBundleId(String commandId) {
		return commandToBundleIdMapper.getBundleId(commandId);
	}
}
