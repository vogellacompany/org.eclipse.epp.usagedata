/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.epp.usagedata.internal.ui.preview;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;

/**
 * Opens a file outside the workspace in an editor. Kept apart from the
 * preview because it needs the optional IDE bundle.
 */
class RecordedDataEditor {

	static void open(File file, String editorId) throws PartInitException {
		FileStoreEditorInput input = new FileStoreEditorInput(EFS.getLocalFileSystem().getStore(file.toURI()));
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input, editorId);
	}
}
