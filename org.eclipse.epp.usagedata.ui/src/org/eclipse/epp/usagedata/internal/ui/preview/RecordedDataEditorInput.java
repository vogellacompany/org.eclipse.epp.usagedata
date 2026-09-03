/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.epp.usagedata.internal.ui.preview;

import java.io.File;
import java.net.URI;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IURIEditorInput;

/**
 * Opens a file outside the workspace in an editor without depending on the
 * IDE bundle.
 */
class RecordedDataEditorInput implements IURIEditorInput {

	private final File file;

	RecordedDataEditorInput(File file) {
		this.file = file;
	}

	public URI getURI() {
		return file.toURI();
	}

	public boolean exists() {
		return file.exists();
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return file.getName();
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return file.getAbsolutePath();
	}

	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof RecordedDataEditorInput && file.equals(((RecordedDataEditorInput) other).file);
	}

	@Override
	public int hashCode() {
		return file.hashCode();
	}
}
