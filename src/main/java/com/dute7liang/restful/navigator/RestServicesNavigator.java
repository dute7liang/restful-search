package com.dute7liang.restful.navigator;

import com.intellij.ide.util.treeView.TreeState;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.treeStructure.SimpleTree;
import com.dute7liang.restful.common.ToolkitIcons;
import com.dute7liang.utils.RestfulToolkitBundle;
import com.dute7liang.utils.ToolkitUtil;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.net.URL;

@State(name = "RestServicesNavigator", storages = { @Storage(StoragePathMacros.WORKSPACE_FILE) })
public class RestServicesNavigator implements PersistentStateComponent<RestServicesNavigatorState> {

	public static final Logger LOG = Logger.getInstance(RestServicesNavigator.class);

	public static final String TOOL_WINDOW_ID = "RestServices";

	private static final URL SYNC_ICON_URL = RestServicesNavigator.class.getResource("/actions/refresh.png");

	protected final Project myProject;

	// private JTree myTree;
	protected RestServiceStructure myStructure;

	RestServicesNavigatorState myState = new RestServicesNavigatorState();

	private SimpleTree myTree;

	private ToolWindow myToolWindow;
	private RestServiceProjectsManager myProjectsManager;
	private RestServiceDetail myRestServiceDetail;

	public RestServicesNavigator(Project myProject) {
		this.myProject = myProject;
	}

	public static RestServicesNavigator getInstance(Project p) {
		return p.getService(RestServicesNavigator.class);
	}

	private void initTree() {
		myTree = new SimpleTree() {

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				final JLabel myLabel = new JLabel(RestfulToolkitBundle.message("toolkit.navigator.nothing.to.display",
						ToolkitUtil.formatHtmlImage(SYNC_ICON_URL)));

				if (myProject.isInitialized()) {
					return;
				}
				myLabel.setFont(getFont());
				myLabel.setBackground(getBackground());
				myLabel.setForeground(getForeground());
				Rectangle bounds = getBounds();
				Dimension size = myLabel.getPreferredSize();
				myLabel.setBounds(0, 0, size.width, size.height);

				int x = (bounds.width - size.width) / 2;
				Graphics g2 = g.create(bounds.x + x, bounds.y + 20, bounds.width, bounds.height);
				try {
					myLabel.paint(g2);
				}
				finally {
					g2.dispose();
				}
			}
		};
		myTree.getEmptyText().clear();

		myTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
	}

	public void createToolWindowContent(ToolWindow toolWindow) {
		if (myToolWindow != null) {
			return;
		}

		myToolWindow = toolWindow;
		initTree();
		myProjectsManager = RestServiceProjectsManager.getInstance(myProject);
		myRestServiceDetail = new RestServiceDetail();

		myToolWindow.setIcon(ToolkitIcons.SERVICE);

		JPanel panel = new RestServicesNavigatorPanel(myProject, myTree, myRestServiceDetail);
		final ContentFactory contentFactory = ContentFactory.getInstance();
		final Content content = contentFactory.createContent(panel, "", false);
		ContentManager contentManager = myToolWindow.getContentManager();
		contentManager.addContent(content);
		contentManager.setSelectedContent(content, false);
	}

	boolean wasVisible = false;

	public void stateChanged() {
		if (myToolWindow == null) {
			return;
		}
		if (myToolWindow.isDisposed()) {
			return;
		}
		boolean visible = myToolWindow.isVisible();
		if (!visible || wasVisible) {
			return;
		}
		scheduleStructureUpdate();
		wasVisible = true;
	}

	public void scheduleStructureUpdate() {
		scheduleStructureRequest(this::refreshStructureInBackground);

	}

	private void scheduleStructureRequest(final Runnable r) {
		if (myToolWindow == null) {
			return;
		}
		ToolkitUtil.runWhenProjectIsReady(myProject, () -> {
			if (!myToolWindow.isVisible()) {
				return;
			}

			boolean shouldCreate = myStructure == null;
			if (shouldCreate) {
				initStructure();
			}

			r.run();
			// fixme: compat
			// if (shouldCreate) {
			// TreeState.createFrom(myState.treeState).applyTo(myTree);
			// }
		});

	}

	private void initStructure() {
		myStructure = new RestServiceStructure(myProject, myProjectsManager, myRestServiceDetail, myTree);

	}

	private void refreshStructureInBackground() {
		ApplicationManager.getApplication().executeOnPooledThread(() -> {
			if (myProject.isDisposed()) {
				return;
			}

			java.util.List<RestServiceProject> projects = myProjectsManager.getServiceProjects();
			ApplicationManager.getApplication().invokeLater(() -> {
				if (myProject.isDisposed() || myToolWindow == null || myToolWindow.isDisposed() || !myToolWindow.isVisible() || myStructure == null) {
					return;
				}
				myStructure.updateProjects(projects);
			}, myProject.getDisposed());
		});
	}

	private void listenForProjectsChanges() {
		// todo :
	}

	@Nullable
	@Override
	public RestServicesNavigatorState getState() {
		ApplicationManager.getApplication().assertIsDispatchThread();
		if (myStructure != null) {
			try {
				myState.treeState = new Element("root");
				TreeState.createOn(myTree).writeExternal(myState.treeState);
			}
			catch (WriteExternalException e) {
				LOG.warn(e);
			}
		}
		return myState;
	}

	@Override
	public void loadState(RestServicesNavigatorState state) {
		myState = state;
		scheduleStructureUpdate();
	}

}
