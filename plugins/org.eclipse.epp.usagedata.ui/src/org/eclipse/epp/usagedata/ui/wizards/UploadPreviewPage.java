package org.eclipse.epp.usagedata.ui.wizards;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Comparator;
import java.util.Date;

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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

public class UploadPreviewPage extends WizardPage {

	private TableViewer viewer;
	private final AskUserUploader uploader;
	private Job contentJob;
	private SetModel events = new SetModel();
	
	private static final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
	
	private static final Comparator<UsageDataEvent> sortByTimeStampComparator = new Comparator<UsageDataEvent>() {
		public int compare(UsageDataEvent event1, UsageDataEvent event2) {
			if (event1.when == event2.when) return 0;
			return event1.when < event2.when ? 1 : -1;
		}
		
	};
	private TableViewerColumn bundleIdColumn;

	public UploadPreviewPage(AskUserUploader uploader) {
		super("wizardPage");
		this.uploader = uploader;
		setTitle("Upload Preview");
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());
		viewer = new TableViewer(container,SWT.VIRTUAL | SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(false);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		viewer.getTable().setLayoutData(layoutData);
		
		OwnerDrawLabelProvider.setUpOwnerDraw(viewer);
		
		TableViewerColumn whatColumn = new TableViewerColumn(viewer, SWT.LEFT);
		whatColumn.getColumn().setText("What");
		whatColumn.getColumn().setWidth(100);
		whatColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object event) {
				return ((UsageDataEvent)event).what;
			}
		});
		
		TableViewerColumn kindColumn = new TableViewerColumn(viewer, SWT.LEFT);
		kindColumn.getColumn().setText("Kind");
		kindColumn.getColumn().setWidth(100);
		kindColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object event) {
				return ((UsageDataEvent)event).kind;
			}
		});
		
		TableViewerColumn descriptionColumn = new TableViewerColumn(viewer, SWT.LEFT);
		descriptionColumn.getColumn().setText("Description");
		descriptionColumn.getColumn().setWidth(100);
		descriptionColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object event) {
				return ((UsageDataEvent)event).description;
			}
		});
		
		bundleIdColumn = new TableViewerColumn(viewer, SWT.LEFT);
		bundleIdColumn.getColumn().setText("Bundle Id");
		bundleIdColumn.getColumn().setWidth(100);
		bundleIdColumn.setLabelProvider(new OwnerDrawLabelProvider() {

			@Override
			protected void measure(Event event, Object element) {
				if (element == null) return;
				Point extent = event.gc.textExtent(((UsageDataEvent)element).bundleId);
				event.height = extent.y + 4;
				int width = extent.x + 2;
				
				if (width > bundleIdColumn.getColumn().getWidth()) bundleIdColumn.getColumn().setWidth(width);
			}

			@Override
			protected void paint(Event event, Object element) {
				event.gc.drawText(((UsageDataEvent)element).bundleId, event.x, event.y + 2);
			}
		});
		
		TableViewerColumn bundleVersionColumn = new TableViewerColumn(viewer, SWT.LEFT);
		bundleVersionColumn.getColumn().setText("Version");
		bundleVersionColumn.getColumn().setWidth(100);
		bundleVersionColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object event) {
				return ((UsageDataEvent)event).bundleVersion;
			}
		});

		TableViewerColumn timestampColumn = new TableViewerColumn(viewer, SWT.LEFT);
		timestampColumn.getColumn().setText("When");
		timestampColumn.getColumn().setWidth(100);
		timestampColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object event) {
				return dateFormat.format(new Date(((UsageDataEvent)event).when));
			}
		});
		
		DeferredContentProvider provider = new DeferredContentProvider(sortByTimeStampComparator);
		viewer.setContentProvider(provider);

		viewer.setInput(events);
		setControl(container);
		
		startContentJob();
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
		UsageDataFileReader reader = null;
		try {
			reader = new UsageDataFileReader(file);
			UsageDataEvent event = null;
			while ((event = reader.next()) != null) {
				if (isDisposed()) return;
				if (monitor.isCanceled()) return;
				addEvent(event);
			}
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

	private void addEvent(final UsageDataEvent event) { 
		events.addAll(new Object[] {event});
	}
}
