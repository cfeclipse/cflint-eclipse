package org.cfeclipse.cflint.preferences;

import org.cfeclipse.cflint.CFLintPlugin;
import org.cfeclipse.cflint.store.CFLintPreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.RGB;

public class CFLintPreferenceManager {

	private IPreferenceStore store;
	
	
	public CFLintPreferenceManager() {
		store = CFLintPlugin.getDefault().getPreferenceStore();
	}

	public boolean getBooleanPref(String prefKey) {
		return store.getBoolean(prefKey);
	}

	public String getStringPref(String prefKey) {
		return store.getString(prefKey);
	}
	
	/* private String getColorString(RGB color) {
	    return color.red + "," + color.green + "," + color.blue;
	} */
	
	public void initializeDefaultValues() {
		
		CFLintPreferenceConstants.setDefaults(store);
	}
	
	/**
	 * Gets an RGB from the preference store using key as the key. If the key
	 * does not exist, it returns 0,0,0
	 * @param key
	 * @return
	 */
	@SuppressWarnings("null")
	public RGB getColor(String key)
	{
		//try to get the color as a string from the store
		String rgbString = store.getString(key);
		//System.err.println(key + " :: " + rgbString);
		
		//if we didnt get anything back...
		if(rgbString.length() <= 0)
		{
			//try to get it from the default settings
			rgbString = store.getDefaultString(key);
			
			//if we still didnt get anything use black
			if(rgbString.length() <= 0)
			{
				// Force a stack trace to see what called this.
				try {
					rgbString = null;
					System.out.println(rgbString.length());
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				System.err.println("Color key: " + key + " is a no show using black");
				rgbString = "0,0,0";
			}
		}
		
		//make sure we get an ok string
		rgbString = deParen(rgbString);
		
		RGB newcolor = null;
		try
		{
			newcolor = StringConverter.asRGB(deParen(rgbString));
		}
		catch(Exception e)
		{
			System.err.println("Woah... got an odd color passed: " + key);
			e.printStackTrace(System.err);
		}
		
		return newcolor;
	}
	
	/**
	 * for some reason the color can get stored as  {RGB 12, 1, 1} and the rbg maker
	 * thingy expects them in 12,1,1, format so this cleans up the string a bit
	 * @param item
	 * @return
	 */
	private String deParen(String item)
	{
		String d = item.replace('{',' ').replace('}',' '); 
		d = d.replaceAll("[RGB ]","").trim();
		return d;
	}


	public Boolean CFLintEnabled() {
		return store.getBoolean(CFLintPreferenceConstants.P_CFLINT_ENABLED);
	}
		
	
}
