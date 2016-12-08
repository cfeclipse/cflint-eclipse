package org.cfeclipse.cflint.store;


import org.cfeclipse.cflint.CFLintPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;

public class CFLintPropertyManager {
	
	private IPreferenceStore preferenceManager;

	public CFLintPropertyManager() 
	{
		super();
		this.preferenceManager = CFLintPlugin.getDefault().getPreferenceStore();
	}
	
	public IPreferenceStore getStore(IProject project)
	{
		return new ProjectPropertyStore(project);
	}
	
	public void initializeDefaultValues(IProject project) {
		IPreferenceStore store = new ProjectPropertyStore(project);
		store.setDefault(CFLintPreferenceConstants.P_CFLINT_ENABLED, preferenceManager.getDefaultBoolean(CFLintPreferenceConstants.P_CFLINT_ENABLED));
	}
	
	public void setCFLintEnabledProject(Boolean enabled, IProject project){
		IPreferenceStore store = new ProjectPropertyStore(project);
		store.setValue(CFLintPreferenceConstants.P_CFLINT_ENABLED, enabled);
		
	}
	
	public boolean getCFLintEnabledProject(IProject project){
		IPreferenceStore store = new ProjectPropertyStore(project);
		return store.getBoolean(CFLintPreferenceConstants.P_CFLINT_ENABLED);
	}
	
	public boolean getCFLintStoreConfigInProject(IProject project){
		IPreferenceStore store = new ProjectPropertyStore(project);
		return store.getBoolean(CFLintPreferenceConstants.P_CFLINT_STOREINPROJECT);
	}
	
	public void setCFLintStoreConfigInProject(Boolean enabled, IProject project){
		IPreferenceStore store = new ProjectPropertyStore(project);
		store.setValue(CFLintPreferenceConstants.P_CFLINT_STOREINPROJECT, enabled);
		
	}
	
}
