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
				new QuickFix("Add global ignore for " + problem),
				new QuickFix("Add function level ignore for " + problem),
				new QuickFix("Add line ignore for " + problem),
			};
		} catch (CoreException e) {
			return new IMarkerResolution[0];
		}
	}
}