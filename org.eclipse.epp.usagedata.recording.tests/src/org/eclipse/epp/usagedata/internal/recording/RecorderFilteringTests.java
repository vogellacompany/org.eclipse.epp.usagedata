/*******************************************************************************
 * Copyright (c) 2008 The Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    The Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.epp.usagedata.internal.recording;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.epp.usagedata.internal.gathering.events.UsageDataEvent;
import org.eclipse.epp.usagedata.internal.recording.filtering.UsageDataEventFilter;
import org.eclipse.epp.usagedata.internal.recording.settings.UsageDataRecordingSettings;
import org.junit.jupiter.api.Test;

/**
 * These tests confirm that the recorder asks the filter before an event is
 * buffered, so that what the filter leaves out never reaches the disk.
 */
public class RecorderFilteringTests {

	@Test
	public void testEventTheFilterRejectsIsNotRecorded() {
		UsageDataRecorder recorder = recorderFiltering(event -> false);

		assertFalse(recorder.isIncluded(event()));
	}

	@Test
	public void testEventTheFilterAcceptsIsRecorded() {
		UsageDataRecorder recorder = recorderFiltering(event -> true);

		assertTrue(recorder.isIncluded(event()));
	}

	@Test
	public void testEventIsRecordedWhenTheFilterThrows() {
		UsageDataRecorder recorder = recorderFiltering(event -> {
			throw new IllegalStateException("the filter is broken"); //$NON-NLS-1$
		});

		assertTrue(recorder.isIncluded(event()));
	}

	@Test
	public void testEventIsRecordedWhenThereAreNoSettings() {
		UsageDataRecorder recorder = new UsageDataRecorder() {
			@Override
			protected UsageDataRecordingSettings getSettings() {
				return null;
			}
		};

		assertTrue(recorder.isIncluded(event()));
	}

	private UsageDataEvent event() {
		return new UsageDataEvent("started", "bundle", "org.eclipse.core", "org.eclipse.core", "1.0.0", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				System.currentTimeMillis());
	}

	private UsageDataRecorder recorderFiltering(Decision decision) {
		final UsageDataRecordingSettings settings = new UsageDataRecordingSettings() {
			@Override
			public UsageDataEventFilter getFilter() {
				return new StubFilter(decision);
			}
		};
		return new UsageDataRecorder() {
			@Override
			protected UsageDataRecordingSettings getSettings() {
				return settings;
			}
		};
	}

	private interface Decision {
		boolean includes(UsageDataEvent event);
	}

	private static class StubFilter implements UsageDataEventFilter {
		private final Decision decision;

		StubFilter(Decision decision) {
			this.decision = decision;
		}

		public boolean includes(UsageDataEvent event) {
			return decision.includes(event);
		}

		public void addFilterChangeListener(org.eclipse.epp.usagedata.internal.recording.filtering.FilterChangeListener listener) {
		}

		public void removeFilterChangeListener(org.eclipse.epp.usagedata.internal.recording.filtering.FilterChangeListener listener) {
		}
	}
}
