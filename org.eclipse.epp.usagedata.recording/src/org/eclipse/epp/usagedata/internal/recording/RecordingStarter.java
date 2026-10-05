/*******************************************************************************
 * Copyright (c) 2026 The Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    The Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.epp.usagedata.internal.recording;

import org.eclipse.epp.usagedata.internal.gathering.services.UsageDataService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Activates this bundle as soon as the {@link UsageDataService} is registered,
 * so that the recorder listens from the start instead of from whenever a page
 * first loads the bundle. The activator does the actual work.
 */
@Component(immediate = true,reference = @Reference(name = "usageDataService", service = UsageDataService.class))
public class RecordingStarter {
}
