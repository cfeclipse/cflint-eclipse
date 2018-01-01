package org.cfeclipse.cflint.quickfix;

import org.cfeclipse.cflint.CFLintPlugin;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IMarkerResolution;

import com.cflint.config.CFLintConfig;

public class QuickFixAddGlobalIgnore extends QuickFix {
	String	label;
	IMarker	marker;
	
	QuickFixAddGlobalIgnore(IMarker mk, String label) {
		super(mk, label);
		this.label = label;
		this.marker = mk;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void run(IMarker marker) {
		try {
			IProject iProject = CFLintPlugin.getDefault().getCurrentProject();
			CFLintConfig cflintConfig = CFLintPlugin.getDefault().getProjectCFLintConfig(iProject);
			String code = marker.getAttribute("CFLINT_MESSAGE_CODE").toString();
			cflintConfig.addExclude(code);
			CFLintPlugin.getDefault().saveProjectCFLintConfig(iProject, cflintConfig);
//			MessageDialog.openInformation(null, "Add global ignore", "Added global ignore for " + code);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
}