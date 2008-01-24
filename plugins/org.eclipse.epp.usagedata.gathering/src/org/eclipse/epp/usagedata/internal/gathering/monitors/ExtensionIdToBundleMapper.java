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

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IRegistryEventListener;
import org.eclipse.core.runtime.Platform;

public class ExtensionIdToBundleMapper {
	private Map<String, String> map;
	private final String extensionPointId;
	private IRegistryEventListener listener;

	public ExtensionIdToBundleMapper(String extensionPointId) {
		this.extensionPointId = extensionPointId;
		hookListeners();
	}
	
	void hookListeners() {
		listener = new IRegistryEventListener() {

			public void added(IExtension[] extensions) {
				clearCache();
			}

			public void added(IExtensionPoint[] extensionPoints) {
			}

			public void removed(IExtension[] extensions) {
				clearCache();				
			}

			public void removed(IExtensionPoint[] extensionPoints) {
			}
		};
		Platform.getExtensionRegistry().addListener(listener, extensionPointId);
	}
	
	public void dispose() {
		Platform.getExtensionRegistry().removeListener(listener);
	}		
	
	/**
	 * This method fetches the bundle that defines
	 * the extension, extensionId. Since extensions are defined in the plugin.xml, the bundle
	 * that defines it must be a singleton which means that there will only be one version
	 * of the bundle loaded. Happy day.
	 * 
	 * @param extensionId
	 * @return
	 */
	protected synchronized String getBundleId(String extensionId) {
		updateCommandToBundleMappings();
		return map.get(extensionId);
	}

	private synchronized void clearCache() {
		map = null;		
	}
	
	/**
	 * This method walks through the commands registered via the extension registry
	 * and creates the {@link #map}.
	 */
	private synchronized void updateCommandToBundleMappings() {
		if (map != null) return;
		map = new HashMap<String, String>();
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(extensionPointId);
		for (IConfigurationElement element : elements) {
			map.put(element.getAttribute("id"), element.getContributor().getName());
		}
	}
}
