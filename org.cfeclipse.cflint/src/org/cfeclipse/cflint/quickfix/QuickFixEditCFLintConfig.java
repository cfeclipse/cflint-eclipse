package org.cfeclipse.cflint.quickfix;

import java.util.Collections;

import org.cfeclipse.cflint.CFLintPlugin;
import org.cfeclipse.cflint.store.ProjectPropertyPage;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IMarkerResolution;

import com.cflint.config.CFLintConfig;

public class QuickFixEditCFLintConfig extends QuickFix {
	String	label;
	IMarker	marker;
	
	QuickFixEditCFLintConfig(IMarker mk, String label) {
		super(mk, label);
		this.label = label;
		this.marker = mk;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void run(IMarker marker) {
		IProject iProject = CFLintPlugin.getDefault().getCurrentProject();
		org.eclipse.swt.widgets.Shell shell = org.eclipse.swt.widgets.Display.getCurrent().getActiveShell();
		org.eclipse.ui.dialogs.PreferencesUtil.createPropertyDialogOn(shell, iProject, ProjectPropertyPage.PAGE_ID,
				new String[] { ProjectPropertyPage.PAGE_ID }, Collections.EMPTY_MAP).open();
	}
}