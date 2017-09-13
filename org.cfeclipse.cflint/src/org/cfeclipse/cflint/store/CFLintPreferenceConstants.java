/*
 * Created on Nov 12, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.cfeclipse.cflint.store;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author Denny Valliant
 * 
 */
public class CFLintPreferenceConstants {
	
	/** enable cflint builder **/
	public static final String P_CFLINT_ENABLED = "__cflint_enabled";
	public static final String P_CFLINT_STOREINPROJECT = "__cflint_storeinproject";
	public static final String CFLINT_PROPERTIES_ENABLED = "__cflint_properties_enabled";
	

	/**
	 * Sets up the default values for preferences managed by {@link FoldingPreferencePage} .
	 */
	
	public static void setDefaults(IPreferenceStore store) { 
		store.setDefault(P_CFLINT_ENABLED, true);
		store.setDefault(P_CFLINT_STOREINPROJECT, false);
		store.setDefault(CFLINT_PROPERTIES_ENABLED, false);
	}

}