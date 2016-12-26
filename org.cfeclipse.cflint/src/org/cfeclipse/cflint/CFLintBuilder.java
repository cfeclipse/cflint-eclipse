package org.cfeclipse.cflint;

import java.util.HashMap;
import java.util.Map;

import org.cfeclipse.cflint.CFLintPlugin;
import org.cfeclipse.cflint.store.CFLintPropertyManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import com.cflint.config.CFLintConfig;

/**
 * 
 * CFLint project builder
 * 
 * @author Denny Valliant
 */
public class CFLintBuilder extends IncrementalProjectBuilder {

	private CFLintPropertyManager propertyManager = new CFLintPropertyManager();
	public static final String MARKER_TYPE = "org.cfeclipse.cflint.cflintmarker";

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		super.clean(monitor);
		CFLintPlugin.getDefault().setProjectCFLintConfigs(new HashMap<String, CFLintConfig>());
	}

	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) {
		if (!propertyManager.getCFLintEnabledProject(getProject())) {
			try {
				IResourceDelta delta = getDelta(getProject());
				if(delta != null) {
					delta.getResource().deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_ONE);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		if (kind == IncrementalProjectBuilder.FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	private void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) {
		System.out.println("incremental build on " + delta);
		try {
			delta.accept(new IResourceDeltaVisitor() {
				public boolean visit(IResourceDelta delta) {
					getResourceBuildMarkers(delta.getResource());
					return true; // visit children too
				}
			});
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private void fullBuild(IProgressMonitor monitor) {
		try {
			getProject().accept(new CFMLBuildVisitor());
		} catch (CoreException e) {
		}
	}

	private void getResourceBuildMarkers(IResource res) {
		CFLintPlugin.getDefault().addMarkers(res);
	}

	class CFMLBuildVisitor implements IResourceVisitor {
		public boolean visit(IResource res) {
			getResourceBuildMarkers(res);
			// build the specified resource.
			// return true to continue visiting children.
			return true;
		}
	}

}
