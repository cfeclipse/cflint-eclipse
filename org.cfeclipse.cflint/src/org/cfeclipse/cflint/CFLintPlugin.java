package org.cfeclipse.cflint;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.cfeclipse.cflint.config.CFLintConfigUI;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.cflint.BugInfo;
import com.cflint.BugList;
import com.cflint.CFLint;
import com.cflint.config.CFLintConfig;
import com.cflint.config.ConfigUtils;
import com.cflint.plugins.CFLintScanner;
import com.cflint.tools.CFLintFilter;

/**
 * The activator class controls the plug-in life cycle
 */
public class CFLintPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.cfeclipse.cflint"; //$NON-NLS-1$

	// The shared instance
	private static CFLintPlugin plugin;

	private CFLint cflint;
	private CFLint scannerLinter;
	private HashMap<String, CFLintConfig> projectCFLintConfigs = new HashMap<String, CFLintConfig>();
	
	
	public HashMap<String, CFLintConfig> getProjectCFLintConfigs() {
		return projectCFLintConfigs;
	}

	public void setProjectCFLintConfigs(HashMap<String, CFLintConfig> projectCFLintConfigs) {
		this.projectCFLintConfigs = projectCFLintConfigs;
	}

	/**
	 * The constructor
	 */
	public CFLintPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static CFLintPlugin getDefault() {
		return plugin;
	}

	public void addMarkers(IResource res) {
		if (!res.isAccessible() || !res.exists())
			return;
		if (res.getRawLocation() == null || res.getRawLocation().toFile().isDirectory())
			return;
		if (!res.getRawLocation().toFile().getAbsolutePath().endsWith(".cfm")
				&& !res.getRawLocation().toFile().getAbsolutePath().endsWith(".cfc"))
			return;
		try {
			res.deleteMarkers(CFLintBuilder.MARKER_TYPE, true, IResource.DEPTH_ONE);
//			res.deleteMarkers(null, true, IResource.DEPTH_ONE);
			String projectName = res.getProject().getName();
			CFLintConfig config = projectCFLintConfigs.get(projectName);
			boolean reload = false;
			if(config == null) {
				config = CFLintConfigUI.getProjectCFLintConfig(res.getProject());
				projectCFLintConfigs.put(projectName, config);
				reload = true;
			}
			if (cflint == null || reload) {
				cflint = new CFLint(config);
				cflint.setVerbose(true);
				cflint.setLogError(true);
				cflint.setQuiet(false);
				cflint.setShowProgress(false);
				cflint.setProgressUsesThread(true);
				CFLintFilter filter = CFLintFilter.createFilter(true);
				cflint.getBugs().setFilter(filter);
			}
			cflint.getBugs().getBugList().clear();
			File sourceFile = res.getRawLocation().makeAbsolute().toFile();
			System.out.println("Scanning " + sourceFile.getAbsolutePath());
			cflint.scan(sourceFile);
			BugList bugList = cflint.getBugs();
			for (BugInfo bugInfo : bugList) {
				addMarker(res, bugInfo);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void scanResource(IResource res, CFLintScanner scanner) {
		if (!res.isAccessible() || !res.exists())
			return;
		if (res.getRawLocation() == null || res.getRawLocation().toFile().isDirectory())
			return;
		if (!res.getRawLocation().toFile().getAbsolutePath().endsWith(".cfm")
				&& !res.getRawLocation().toFile().getAbsolutePath().endsWith(".cfc"))
			return;
		try {
			CFLintConfig config = new CFLintConfig();
//			config.setRules(ConfigUtils.loadDefaultPluginInfo().getRules());
//			res.deleteMarkers(CFLintBuilder.MARKER_TYPE, true, IResource.DEPTH_ONE);
			if (scannerLinter == null) {
				scannerLinter = new CFLint(config);
				scannerLinter.setVerbose(true);
				scannerLinter.setLogError(true);
				scannerLinter.setQuiet(false);
				scannerLinter.setShowProgress(false);
				scannerLinter.setProgressUsesThread(true);
				CFLintFilter filter = CFLintFilter.createFilter(true);
				scannerLinter.getBugs().setFilter(filter);
				scannerLinter.addScanner(scanner);
			}
			scannerLinter.getBugs().getBugList().clear();
			File sourceFile = res.getRawLocation().makeAbsolute().toFile();
			System.out.println("Scanning Resource" + sourceFile.getAbsolutePath());
			scannerLinter.scan(sourceFile);
//			BugList bugList = cflintResourceScanner.getBugs();
//			for (BugInfo bugInfo : bugList) {
//				addMarker(res, bugInfo);
//			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void addMarker(IResource res, BugInfo bug) {
		try {
			IMarker marker = res.createMarker(CFLintBuilder.MARKER_TYPE);
			int lineNumber = bug.getLine();
			marker.setAttribute(IMarker.MESSAGE, bug.getSeverity() + ": " + bug.getMessage() + " (" + bug.getMessageCode() + ")");
			if (bug.getSeverity().startsWith("WARN")) {
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
			}
			else if (bug.getSeverity().equals("INFO")) {
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
			} else {
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			}
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			marker.setAttribute(IMarker.MARKER, "org.eclipse.core.resources.problemmarker");
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
}
