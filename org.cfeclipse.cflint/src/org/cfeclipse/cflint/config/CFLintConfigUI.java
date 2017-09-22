package org.cfeclipse.cflint.config;

import org.eclipse.swt.graphics.Rectangle;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.cfeclipse.cflint.CFLintPlugin;
import org.cfeclipse.cflint.store.CFLintPropertyManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import com.cflint.config.CFLintConfig;
import com.cflint.config.CFLintPluginInfo;
import com.cflint.config.CFLintPluginInfo.PluginInfoRule;
import com.cflint.config.CFLintPluginInfo.PluginInfoRule.PluginMessage;
import com.cflint.config.ConfigUtils;

public class CFLintConfigUI {
	
	private ArrayList<RuleEditor>		  ruleEditors;
	private CFLintConfig				  cflintConfig;
	private static String				  cflintVersion	= com.cflint.Version.getVersion();
	private static final CFLintPluginInfo pluginInfo	= ConfigUtils.loadDefaultPluginInfo();
	private Composite					  composite;
	private IProject					  iProject;
	
	public void buildGUI(Composite composite, IProject iProject) {
		this.composite = composite;
		this.iProject = iProject;
		setConfig(CFLintPlugin.getDefault().getProjectCFLintConfig(iProject));
		buildRulesGUI(composite, iProject);
	}
	
	public void buildRulesGUI(final Composite composite, IProject iProject) {
		ruleEditors = new ArrayList<RuleEditor>();
		
		final Composite searchComposite = new Composite(composite, SWT.NULL);
		GridData gdata = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gdata.heightHint = 30;
		searchComposite.setLayoutData(gdata);
		searchComposite.setLayout(new GridLayout(4, false));
		
		Label searchLabel = new Label(searchComposite, SWT.NONE);
		searchLabel.setText("Search:");
		final Text searchText = new Text(searchComposite, SWT.BORDER);
		searchText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		searchLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		
		List<PluginInfoRule> enabledRules = getConfig().getRules();
		List<PluginMessage> excludeMessages = getConfig().getExcludes();
		Button enabledCheckbox = new Button(searchComposite, SWT.CHECK);
		enabledCheckbox.setText("Enable/Disable All Rules for CFLint " + cflintVersion);
		enabledCheckbox.setSelection(false);
		enabledCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				Button btn = (Button) event.getSource();
				boolean enableAll = btn.getSelection();
				for (RuleEditor ruleEditor : ruleEditors) {
					ruleEditor.setRuleEnabled(enableAll);
				}
			}
		});
		
		Button disabledOnlyCheckbox = new Button(searchComposite, SWT.CHECK);
		disabledOnlyCheckbox.setText("List Disabled Only");
		disabledOnlyCheckbox.setSelection(false);
		
		final ScrolledComposite rulesParentComposite = new ScrolledComposite(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData sgdata = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		sgdata.heightHint = 500;
		rulesParentComposite.setLayoutData(sgdata);
		rulesParentComposite.setLayout(new FillLayout(SWT.VERTICAL));
		
		final Composite rulesComposite = new Composite(rulesParentComposite, SWT.NONE);
		
		GridData rulesCompositeGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		rulesCompositeGridData.minimumHeight = 100;
		rulesCompositeGridData.grabExcessVerticalSpace = true;
		rulesComposite.setLayoutData(rulesCompositeGridData);
		rulesComposite.setLayout(new GridLayout(1, false));
		
		searchText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent event) {
				Iterator<RuleEditor> ruleIterator = ruleEditors.iterator();
				while (ruleIterator.hasNext()) {
					RuleEditor editor = ruleIterator.next();
					if (editor.getRule().getName().toLowerCase().contains(searchText.getText().toLowerCase())) {
						editor.setVisible(true);
						((GridData) editor.getLayoutData()).exclude = false;
					} else {
						editor.setVisible(false);
						((GridData) editor.getLayoutData()).exclude = true;
					}
					rulesComposite.layout(true, true);
					editor.getParent().pack();
					rulesParentComposite.pack();
				}
			}
		});
		
		disabledOnlyCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				Button btn = (Button) event.getSource();
				boolean onlyDisabled = btn.getSelection();
				for (RuleEditor editor : ruleEditors) {
					if (!onlyDisabled) {
						editor.setVisible(true);
						((GridData) editor.getLayoutData()).exclude = false;
					} else {
						boolean hasDisabledMessage = false;
						for (MessageEditor messageEditor : editor.getMessagesEditors()) {
							if (!messageEditor.getMessageEnabled()) {
								hasDisabledMessage = true;
							}
						}
						
						if (!editor.isEnabled() || hasDisabledMessage) {
							editor.setVisible(true);
							((GridData) editor.getLayoutData()).exclude = false;
						} else {
							editor.setVisible(false);
							((GridData) editor.getLayoutData()).exclude = true;
						}
					}
					rulesParentComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
					rulesComposite.layout(true, true);
					editor.getParent().pack();
					rulesParentComposite.pack();
				}
			}
		});
		
		HashMap<String, String> descriptions = (HashMap<String, String>) ConfigUtils.loadDescriptions();
		for (PluginInfoRule rule : pluginInfo.getRules()) {
			RuleEditor ruleEdit;
			if (enabledRules.contains(rule)) {
				rule = enabledRules.get(enabledRules.indexOf(rule));
				ruleEdit = new RuleEditor(rulesComposite, SWT.NONE, rule, true, descriptions, excludeMessages);
			} else {
				ruleEdit = new RuleEditor(rulesComposite, SWT.NONE, rule, true, descriptions, excludeMessages);
			}
			ruleEdit.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
			ruleEdit.setLayout(new GridLayout(1, false));
			ruleEditors.add(ruleEdit);
		}
		rulesParentComposite.setContent(rulesComposite);
		rulesParentComposite.setExpandVertical(true);
		rulesParentComposite.setExpandHorizontal(true);
		rulesParentComposite.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(final ControlEvent e) {
				final Rectangle r = rulesComposite.getClientArea();
				// TODO: totally incorrect but gets close enough for now
				rulesParentComposite.setMinSize(composite.computeSize(r.width, ruleEditors.size() * 200));
			}
		});
	}
	
	public void setConfig(CFLintConfig projectCFLintConfig) {
		this.cflintConfig = projectCFLintConfig;
	}
	
	public CFLintConfig getConfig() {
		List<PluginInfoRule> rules = cflintConfig.getRules();
		List<PluginMessage> excludes = cflintConfig.getExcludes();
		for (RuleEditor ruleEditor : ruleEditors) {
			PluginInfoRule rule = ruleEditor.getRule();
			if (rule != null) {
				if (rules.indexOf(rule) != -1) {
					rules.set(rules.indexOf(rule), rule);
				} else {
					rules.add(rule);
				}
				for (PluginMessage exclude : ruleEditor.getExcludes()) {
					if (excludes.indexOf(exclude) != -1) {
						excludes.set(excludes.indexOf(exclude), exclude);
					} else {
						excludes.add(exclude);
					}
				}
			}
		}
		return cflintConfig;
	}
	
	public ArrayList<RuleEditor> getRuleEditors() {
		return ruleEditors;
	}
	
	public void resetRules() {
		CFLintConfig config = new CFLintConfig();
		config.setRules(pluginInfo.getRules());
		setConfig(config);
	}
	
}
