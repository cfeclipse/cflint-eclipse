package org.cfeclipse.cflint.preferences;

import org.cfeclipse.cflint.store.CFLintPreferenceConstants;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class ShowPreferencePropertyTester extends PropertyTester {
	public static final String PROPERTY_NAMESPACE = "org.cfeclipse.cflint.enablement";
	public static final String PROPERTY_CAN_SHOW = "canShow";
	public static final String CFE_NATURE = "org.cfeclipse.cfml.CFENature";
	public static final String CFBUILDER_NATURE = "com.adobe.ide.coldfusion.projectNature";

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		CFLintPreferenceManager preferenceManager = new CFLintPreferenceManager();
		if (PROPERTY_CAN_SHOW.equals(property)) {
			if (receiver instanceof IProject) {
				IProject project = (IProject) receiver;
				try {
					if (project.hasNature(CFE_NATURE)) {
						return true;
					} else if (project.hasNature(CFBUILDER_NATURE)) {
						return true;
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
			if (preferenceManager.getBooleanPref(CFLintPreferenceConstants.P_CFLINT_ENABLED)) {
				return true;
			}
			return false;
		}
		return false;
	}
}