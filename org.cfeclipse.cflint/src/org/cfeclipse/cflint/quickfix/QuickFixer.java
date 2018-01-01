package org.cfeclipse.cflint.quickfix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;
import org.eclipse.ui.IMarkerResolutionGenerator2;

public class QuickFixer implements IMarkerResolutionGenerator {
	public IMarkerResolution[] getResolutions(IMarker mk) {
		try {
			Object problem = mk.getAttribute("messageCode");
			return new IMarkerResolution[] { 
				new QuickFixAddGlobalIgnore(mk, "Ignore " + problem + " globally"),
				new QuickFixAddMultiLineIgnore(mk, "Multi-line ignore of " + problem),
				new QuickFixAddLineIgnore(mk, "Ignore line " + problem),
				new QuickFixAddLineIgnore(mk, "Ignore entire line"),
				new QuickFixEditCFLintConfig(mk, "Edit Configuration"),
			};
		} catch (CoreException e) {
			return new IMarkerResolution[0];
		}
	}
}