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
package org.eclipse.epp.usagedata.ui.editors.myusage;

import org.eclipse.epp.usagedata.ui.Activator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class MyUsageDataEditorInput implements IEditorInput {

	private final String usageDataUrl;

	public MyUsageDataEditorInput(String usageDataUrl) {
		this.usageDataUrl = usageDataUrl;
	}

	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		return Activator.getDefault().getImageDescriptor("icons/usage.gif");
	}

	public String getName() {
		return "My Usage Data";
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return "My usage data";
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((usageDataUrl == null) ? 0 : usageDataUrl.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MyUsageDataEditorInput other = (MyUsageDataEditorInput) obj;
		if (usageDataUrl == null) {
			if (other.usageDataUrl != null)
				return false;
		} else if (!usageDataUrl.equals(other.usageDataUrl))
			return false;
		return true;
	}

	public String getUsageDataUrl() {
		return usageDataUrl;
	}

}
