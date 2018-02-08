package org.cfeclipse.cflint;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.cfeclipse.cflint.console.ConsoleUtil;
import org.cfeclipse.cflint.store.CFLintPropertyManager;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.IProgressService;
import org.osgi.framework.BundleContext;

import com.cflint.BugInfo;
import com.cflint.BugList;
import com.cflint.CFLint;
import com.cflint.Levels;
import com.cflint.config.CFLintConfig;
import com.cflint.config.CFLintPluginInfo;
import com.cflint.config.ConfigUtils;
import com.cflint.plugins.CFLintScanner;
import com.cflint.tools.CFLintFilter;

/**
 * The activator class controls the plug-in life cycle
 */
public class CFLintPlugin extends AbstractUIPlugin {
	
	// The plug-in ID
	public static final String PLUGIN_ID	= "org.cfeclipse.cflint"; //$NON-NLS-1$
	public static final String CONSOLE_NAME	= "CFLint";
	
	// The shared instance
	private static CFLintPlugin plugin;
	
	private CFLint						  cflint;
	private CFLint						  scannerLinter;
	private HashMap<String, CFLintConfig> projectCFLintConfigs = new HashMap<String, CFLintConfig>();
	
	private CFLintPropertyManager propertyManager;
	
	private CFLintPartListener			  cflintPartListener;
	private static final CFLintPluginInfo pluginInfo = ConfigUtils.loadDefaultPluginInfo();
	
	public HashMap<String, CFLintConfig> getProjectCFLintConfigs() {
		return projectCFLintConfigs;
	}
	
	public synchronized void setProjectCFLintConfigs(HashMap<String, CFLintConfig> projectCFLintConfigs) {
		this.projectCFLintConfigs = projectCFLintConfigs;
	}
	
	/**
	 * The constructor
	 */
	public CFLintPlugin() {
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		IContextService contextService = (IContextService) PlatformUI.getWorkbench().getService(IContextService.class);
		contextService.activateContext("org.cfeclipse.cflint.cflintContext");
		plugin = this;
		propertyManager = new CFLintPropertyManager();
		cflintPartListener = new CFLintPartListener();
		
		this.getWorkbench().addWindowListener(cflintPartListener);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		this.getWorkbench().removeWindowListener(cflintPartListener);
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
	
	public IEditorPart getLastActiveEditor() {
		return cflintPartListener.getLastActiveEditor();
	}
	
	public CFLintConfig getProjectCFLintConfig(IProject iProject) {
		String projectName = iProject.getName();
		CFLintConfig config = projectCFLintConfigs.get(projectName);
		if (config == null || config.getRules() == null) {
			config = _getProjectCFLintConfig(iProject);
			projectCFLintConfigs.put(projectName, config);
		}
		return config;
	}
	
	public CFLintConfig _getProjectCFLintConfig(IProject iProject) {
		CFLintConfig currentConfig = null;
		File configFile = getConfigFile(iProject);
		if (configFile.exists()) {
			try {
				currentConfig = new CFLintConfig();
				currentConfig = ConfigUtils.unmarshalJson(new FileInputStream(configFile), CFLintConfig.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			currentConfig = new CFLintConfig();
			currentConfig.setRules(pluginInfo.getRules());
		}
		return currentConfig;
	}
	
	public CFLintConfig resetConfig(IProject iProject) {
		String projectName = iProject.getName();
		projectCFLintConfigs.remove(projectName);
		File configFile = getConfigFile(iProject);
		configFile.delete();
		return getProjectCFLintConfig(iProject);
	}
	
	public File getConfigFile(IProject iProject) {
		File configFile;
		if (!propertyManager.getCFLintStoreConfigInProject(iProject)) {
			configFile = iProject.getWorkingLocation(CFLintPlugin.PLUGIN_ID).append("cflint.definition.json").toFile();
		} else {
			configFile = iProject.getProject().getLocation().append("cflint.definition.json").toFile();
		}
		return configFile;
	}
	
	public void saveProjectCFLintConfig(final IProject iProject, CFLintConfig cflintConfig) {
		try {
			PrintWriter writer;
			File configFile = getConfigFile(iProject);
			writer = new PrintWriter(new BufferedWriter(new FileWriter(configFile)));
			writer.write(ConfigUtils.marshalJson(cflintConfig));
			writer.close();
			projectCFLintConfigs.put(iProject.getName(), cflintConfig);
		} catch (Exception e) {
			e.printStackTrace();
		}
		class BuildJob extends Job {
			public BuildJob() {
				super("CFLint - cleaning and rebuilding: " + iProject.getName());
			}
			
			public IStatus run(IProgressMonitor monitor) {
				try {
					iProject.build(IncrementalProjectBuilder.CLEAN_BUILD, monitor);
					iProject.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
				} catch (CoreException e) {
					e.printStackTrace();
				}
				return Status.OK_STATUS;
			}
		}
		new BuildJob().schedule();
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
			CFLintBuilder.clearMarkers(res);
			CFLintConfig config = getProjectCFLintConfig(res.getProject());
			if (cflint == null) {
				cflint = new CFLint(config);
				cflint.setVerbose(true);
				cflint.setLogError(true);
				cflint.setQuiet(false);
				cflint.setShowProgress(false);
				cflint.setProgressUsesThread(true);
			} else {
				cflint.setConfiguration(config);
			}
			File sourceFile = res.getRawLocation().makeAbsolute().toFile();
			log("Scanning " + sourceFile.getAbsolutePath());
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
			// config.setRules(ConfigUtils.loadDefaultPluginInfo().getRules());
			// res.deleteMarkers(CFLintBuilder.MARKER_TYPE, true, IResource.DEPTH_ONE);
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
			log("Scanning Resource" + sourceFile.getAbsolutePath());
			scannerLinter.scan(sourceFile);
			BugList bugList = scannerLinter.getBugs();
			for (BugInfo bugInfo : bugList) {
				addMarker(res, bugInfo);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void addMarker(IResource res, BugInfo bug) {
		try {
			// System.out.println(bug.toString());
			IMarker marker;
			if (bug.getSeverity() == Levels.WARNING) {
				marker = res.createMarker(CFLintBuilder.MARKER_TYPE.WARNING.toString());
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
			} else if (bug.getSeverity() == Levels.INFO) {
				marker = res.createMarker(CFLintBuilder.MARKER_TYPE.INFO.toString());
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
			} else {
				marker = res.createMarker(CFLintBuilder.MARKER_TYPE.PROBLEM.toString());
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			}
			int lineNumber = bug.getLine();
			String messageText = bug.getSeverity() + ": " + bug.getMessage() + " (" + bug.getMessageCode() + ")";
			// messageText += " offset:" + bug.getOffset() + " col:" + bug.getColumn() + " line:" + bug.getLine() + "
			// len:"
			// + bug.getLength();
			marker.setAttribute(IMarker.MESSAGE, messageText);
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			// System.out.println("col" + bug.getColumn());
			// System.out.println("start" + bug.getStartChar());
			// System.out.println("end" + bug.getEndChar());
			// System.out.println(bug.getLine());
			// System.out.println(lineNumber);
			// System.out.println(bug.getExpression());
			// System.out.println();
			
			marker.setAttribute("messageCode", bug.getMessageCode());
			// marker.setAttribute(IMarker.CHAR_START, bug.getColumn());
			// marker.setAttribute(IMarker.CHAR_END, bug.getExpression().length());
			
			marker.setAttribute(IMarker.CHAR_START, bug.getOffset());
			
			marker.setAttribute(IMarker.CHAR_END, bug.getOffset() + bug.getLength());
			
			marker.setAttribute(IMarker.MARKER, "org.eclipse.core.resources.problemmarker");
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			marker.setAttribute("CFLINT_MESSAGE_CODE", bug.getMessageCode());
			
			// if(marker != null){
			// Annotation a = new MarkerAnnotation(marker);
			// annotations.add(a);
			// annotationModel.addAnnotation(a, new Position(startIndex, stopIndex - startIndex));
			// }
			
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public CFLintConfig getCurrentProjectCFLintConfig() {
		return getProjectCFLintConfig(getCurrentProject());
	}
	
	public IProject getCurrentProject() {
		IProject project = null;
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage activePage = window.getActivePage();
			
			IEditorPart activeEditor = activePage.getActiveEditor();
			
			if (activeEditor != null) {
				IEditorInput input = activeEditor.getEditorInput();
				
				project = input.getAdapter(IProject.class);
				if (project == null) {
					IResource resource = input.getAdapter(IResource.class);
					if (resource != null) {
						project = resource.getProject();
					}
				}
			}
		}
		return project;
	}
	
	public static void log(String msg) {
		ConsoleUtil.printInfo(CONSOLE_NAME, msg);
	}
	
	public static void logError(String msg) {
		ConsoleUtil.printError(CONSOLE_NAME, msg);
	}
	
}
