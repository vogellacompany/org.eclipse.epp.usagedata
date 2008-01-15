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
package org.eclipse.epp.usagedata.ui.wizards;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.epp.usagedata.gathering.events.UsageDataEvent;
import org.eclipse.epp.usagedata.recording.uploading.UsageDataFileReader;
import org.eclipse.epp.usagedata.ui.uploaders.AskUserUploader;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.deferred.DeferredContentProvider;
import org.eclipse.jface.viewers.deferred.SetModel;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class UploadPreviewPage extends WizardPage {

	private TableViewer viewer;
	private final AskUserUploader uploader;
	private Job contentJob;
	private SetModel events = new SetModel();
	
	private static final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
	
	private static final Comparator<UsageDataEvent> sortByTimeStampComparator = new Comparator<UsageDataEvent>() {
		public int compare(UsageDataEvent event1, UsageDataEvent event2) {
			if (event1.when == event2.when) return 0;
			return event1.when > event2.when ? 1 : -1;
		}	
	};
	
	private UsageDataTableViewerColumn whatColumn;
	private UsageDataTableViewerColumn kindColumn;
	private UsageDataTableViewerColumn descriptionColumn;
	private UsageDataTableViewerColumn bundleIdColumn;
	private UsageDataTableViewerColumn bundleVersionColumn;
	private UsageDataTableViewerColumn timestampColumn;
	private Color colorGray;
	private Color colorBlack;

	public UploadPreviewPage(AskUserUploader uploader) {
		super("wizardPage");
		this.uploader = uploader;
		setTitle("Upload Preview");
	}

	public void createControl(Composite parent) {
		colorGray = parent.getDisplay().getSystemColor(SWT.COLOR_GRAY);
		colorBlack = parent.getDisplay().getSystemColor(SWT.COLOR_BLACK);
		
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());
		viewer = new TableViewer(container,SWT.VIRTUAL | SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(false);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		viewer.getTable().setLayoutData(layoutData);
		
		OwnerDrawLabelProvider.setUpOwnerDraw(viewer);
		
		createWhatColumn();		
		createKindColumn();		
		createDescriptionColumn();
		createBundleIdColumn();		
		createBundleVersionColumn();
		createTimestampColumn();
		
		DeferredContentProvider provider = new DeferredContentProvider(sortByTimeStampComparator);
		viewer.setContentProvider(provider);

		viewer.setInput(events);
		setControl(container);
		
		startContentJob();
	}

	private void createWhatColumn() {
		whatColumn = new UsageDataTableViewerColumn(SWT.LEFT);
		whatColumn.setText("What");
		whatColumn.setLabelProvider(new UsageDataColumnProvider() {
			@Override
			public String getText(UsageDataEvent event) {
				return event.what;
			}
		});
	}

	private void createKindColumn() {
		kindColumn = new UsageDataTableViewerColumn(SWT.LEFT);
		kindColumn.setText("Kind");
		kindColumn.setLabelProvider(new UsageDataColumnProvider() {
			@Override
			public String getText(UsageDataEvent event) {
				return event.kind;
			}
		});
	}

	private void createDescriptionColumn() {
		descriptionColumn = new UsageDataTableViewerColumn(SWT.LEFT);
		descriptionColumn.setText("Description");
		descriptionColumn.setLabelProvider(new UsageDataColumnProvider() {
			@Override
			public String getText(UsageDataEvent event) {
				return event.description;
			}
		});
	}

	private void createBundleIdColumn() {
		bundleIdColumn = new UsageDataTableViewerColumn(SWT.LEFT);
		bundleIdColumn.setText("Bundle Id");
		bundleIdColumn.setLabelProvider(new UsageDataColumnProvider() {
			@Override
			public String getText(UsageDataEvent event) {
				return event.bundleId;
			}			
		});
	}

	private void createBundleVersionColumn() {
		bundleVersionColumn = new UsageDataTableViewerColumn(SWT.LEFT);
		bundleVersionColumn.setText("Version");
		bundleVersionColumn.setLabelProvider(new UsageDataColumnProvider() {
			@Override
			public String getText(UsageDataEvent event) {
				return event.bundleVersion;
			}
		});
	}

	private void createTimestampColumn() {
		timestampColumn = new UsageDataTableViewerColumn(SWT.LEFT);
		timestampColumn.setText("When");
		timestampColumn.setLabelProvider(new UsageDataColumnProvider() {
			@Override
			public String getText(UsageDataEvent event) {
				return dateFormat.format(new Date(event.when));
			}
		});
		timestampColumn.setSorter(sortByTimeStampComparator);
	}

	private void startContentJob() {
		contentJob = new Job("Generate Usage Data Upload Preview") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				File[] files = uploader.getFiles();
				for (File file : files) {
					if (isDisposed()) break; 
					if (monitor.isCanceled()) return Status.CANCEL_STATUS;
					processFile(file, monitor);
				}
				return Status.OK_STATUS;
			}			
		};
		contentJob.schedule();
	}

	// TODO Add a progress bar to the page?
	void processFile(File file, IProgressMonitor monitor) {
		List<UsageDataEvent> events = new ArrayList<UsageDataEvent>();
		UsageDataFileReader reader = null;
		try {
			reader = new UsageDataFileReader(file);
			UsageDataEvent event = null;
			while ((event = reader.next()) != null) {
				if (isDisposed()) return;
				if (monitor.isCanceled()) return;
				events.add(event);
				if (events.size() > 50) {
					addEvents(events);
					events.clear();
				}
			}
			addEvents(events);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private boolean isDisposed() {
		if (viewer == null) return true;
		if (viewer.getTable() == null) return true;
		return viewer.getTable().isDisposed();
	}

	private void addEvents(List<UsageDataEvent> events) { 
		this.events.addAll(events);
		resizeColumns();
	}

	/**
	 * Oddly enough, this method resizes the columns. In order to figure out how
	 * wide to make the columns, we need to use a GC (specifially, the
	 * {@link GC#textExtent(String)} method). To avoid creating too many of
	 * them, we create one in this method and pass it into the helper method
	 * {@link #resizeColumn(GC, UsageDataTableViewerColumn)} which does most of
	 * the heavy lifting.
	 */
	private void resizeColumns() {
		viewer.getTable().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				GC gc = new GC(viewer.getTable().getDisplay());
				gc.setFont(viewer.getTable().getFont());
				resizeColumn(gc, whatColumn);
				resizeColumn(gc, kindColumn);
				resizeColumn(gc, bundleIdColumn);
				resizeColumn(gc, bundleVersionColumn);
				resizeColumn(gc, descriptionColumn);
				resizeColumn(gc, timestampColumn);
				gc.dispose();
			}
		});
	}

	private void resizeColumn(GC gc, UsageDataTableViewerColumn column) {
		column.resize(gc, events.getElements());
	}

	/**
	 * The {@link UsageDataTableViewerColumn} provides a level of abstraction
	 * for building table columns specifically for the table displaying
	 * instances of {@link UsageDataEvent}. Instances automatically know how to
	 * sort themselves (ascending only) with help from the label provider. This
	 * behaviour can be overridden by providing an alternative
	 * {@link Comparator}.
	 */
	class UsageDataTableViewerColumn {
		private TableViewerColumn column;
		private UsageDataColumnProvider usageDataColumnProvider;
		private Comparator<UsageDataEvent> comparator = new Comparator<UsageDataEvent>() {	
			@Override
			public int compare(UsageDataEvent event1, UsageDataEvent event2) {
				if (usageDataColumnProvider == null) return 0;
				String text1 = usageDataColumnProvider.getText(event1);
				String text2 = usageDataColumnProvider.getText(event2);
				
				if (text1 == null && text2 == null) return 0;
				if (text1 == null) return -1;
				if (text2 == null) return 1;
				
				return text1.compareTo(text2);
			}	
		};
		private SelectionListener selectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getTable().setSortColumn(getColumn());
				getTable().setSortDirection(SWT.DOWN);
				getContentProvider().setSortOrder(comparator);
			}		
		};
	
		public UsageDataTableViewerColumn(int style) {
			column = new TableViewerColumn(viewer, style);
			initialize();
		}

		private void initialize() {
			getColumn().addSelectionListener(selectionListener);
			getColumn().setWidth(100);
		}
	
		DeferredContentProvider getContentProvider() {
			return (DeferredContentProvider)viewer.getContentProvider();
		}
	
		TableColumn getColumn() {
			return column.getColumn();
		}
	
		Table getTable() {
			return viewer.getTable();
		}
	
		public void setSorter(Comparator<UsageDataEvent> comparator) {
			// TODO May need to handle the case when the active comparator is changed.
			this.comparator = comparator;
		}
	
		public void resize(GC gc, Object[] objects) {
			int width = usageDataColumnProvider.getMaximumWidth(gc, objects) + 20;
			getColumn().setWidth(width);
		}
	
		public void setLabelProvider(UsageDataColumnProvider usageDataColumnProvider) {
			this.usageDataColumnProvider = usageDataColumnProvider;
			column.setLabelProvider(usageDataColumnProvider);
		}
	
		public void setWidth(int width) {
			getColumn().setWidth(width);
		}
	
		public void setText(String text) {
			getColumn().setText(text);
		}
	}
	
	abstract class UsageDataColumnProvider extends ColumnLabelProvider {
		public int getMaximumWidth(GC gc, Object[] events) {
			int width = 0;
			for (Object event : events) {
				Point extent = gc.textExtent(getText(event));
				if (extent.x > width) width = extent.x;
			}
			return width;
		}
			
		@Override
		public Color getForeground(Object element) {
			if (uploader.getFilter().includes((UsageDataEvent)element)) {
				return colorBlack;
			}
			else {
				return colorGray;
			}
		}
		
		@Override
		public String getText(Object element) {
			return getText((UsageDataEvent)element);
		}
	
		public abstract String getText(UsageDataEvent element);
	}
}