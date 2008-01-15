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
			return event1.when > event2.when ? 1 : -1;
		}
		
	};

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
		TableViewerColumn whatColumn = new TableViewerColumn(viewer, SWT.LEFT);
		whatColumn.getColumn().setText("What");
		whatColumn.getColumn().setWidth(100);
		whatColumn.setLabelProvider(new UsageDataColumnProvider(whatColumn) {
			@Override
			public String getText(UsageDataEvent event) {
				return event.what;
			}
		});
	}

	private void createKindColumn() {
		TableViewerColumn kindColumn = new TableViewerColumn(viewer, SWT.LEFT);
		kindColumn.getColumn().setText("Kind");
		kindColumn.getColumn().setWidth(100);
		kindColumn.setLabelProvider(new UsageDataColumnProvider(kindColumn) {
			@Override
			public String getText(UsageDataEvent event) {
				return event.kind;
			}
		});
	}

	private void createDescriptionColumn() {
		TableViewerColumn descriptionColumn = new TableViewerColumn(viewer, SWT.LEFT);
		descriptionColumn.getColumn().setText("Description");
		descriptionColumn.getColumn().setWidth(100);
		descriptionColumn.setLabelProvider(new UsageDataColumnProvider(descriptionColumn) {
			@Override
			public String getText(UsageDataEvent event) {
				return event.description;
			}
		});
	}

	private void createBundleIdColumn() {
		TableViewerColumn bundleIdColumn = new TableViewerColumn(viewer, SWT.LEFT);
		bundleIdColumn.getColumn().setText("Bundle Id");
		bundleIdColumn.getColumn().setWidth(100);
		bundleIdColumn.setLabelProvider(new UsageDataColumnProvider(bundleIdColumn) {
			@Override
			public String getText(UsageDataEvent event) {
				return event.bundleId;
			}
			
		});
	}

	private void createBundleVersionColumn() {
		TableViewerColumn bundleVersionColumn = new TableViewerColumn(viewer, SWT.LEFT);
		bundleVersionColumn.getColumn().setText("Version");
		bundleVersionColumn.getColumn().setWidth(100);
		bundleVersionColumn.setLabelProvider(new UsageDataColumnProvider(bundleVersionColumn) {
			@Override
			public String getText(UsageDataEvent event) {
				return event.bundleVersion;
			}
		});
	}

	private void createTimestampColumn() {
		TableViewerColumn timestampColumn = new TableViewerColumn(viewer, SWT.LEFT);
		timestampColumn.getColumn().setText("When");
		timestampColumn.getColumn().setWidth(100);
		timestampColumn.setLabelProvider(new UsageDataColumnProvider(timestampColumn) {
			@Override
			public String getText(UsageDataEvent event) {
				return dateFormat.format(new Date(event.when));
			}
		});
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
//		if (isDisposed()) return;
//		viewer.getTable().getDisplay().syncExec(new Runnable() {
//			public void run() {
//				viewer.refresh();
//			}
//		});
	}
}

abstract class UsageDataColumnProvider extends OwnerDrawLabelProvider {
	private final TableViewerColumn column;

	public UsageDataColumnProvider(TableViewerColumn column) {
		this.column = column;
	}
	
	@Override
	protected void measure(Event event, Object element) {
		if (element == null) return;
		Point extent = event.gc.textExtent(getText((UsageDataEvent)element));
		event.height = extent.y + 4;
		event.width = extent.x + 4;
		
//		int width = extent.x + 4;		
//		if (width > column.getColumn().getWidth()) 
//			column.getColumn().setWidth(width);
	}

	public abstract String getText(UsageDataEvent element);

	@Override
	protected void paint(Event event, Object element) {
		if (element == null) 
			event.gc.drawRectangle(event.x, event.y, event.width, event.height);
		else
			event.gc.drawText(getText((UsageDataEvent)element), event.x, event.y + 2);
	}
}