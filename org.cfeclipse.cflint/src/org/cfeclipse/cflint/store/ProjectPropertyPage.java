package org.cfeclipse.cflint.store;

import java.io.File;

import org.cfeclipse.cflint.CFLintBuilder;
import org.cfeclipse.cflint.CFLintPlugin;
import org.cfeclipse.cflint.config.CFLintConfigUI;
import org.cfeclipse.cflint.config.RuleEditor;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.dialogs.PropertyPage;

public class ProjectPropertyPage extends PropertyPage {

	private CFLintPropertyManager propertyManager;
	private ProjectPropertyStore propStore;
	private BooleanFieldEditor cflintEnabledField;
	private BooleanFieldEditor cflintStoreConfigInProjectField;
	private CFLintConfigUI cflintConfigUI;
	public static final String PAGE_ID = "org.cfeclipse.cflint.store.cfbuilder.ProjectPropertyPage";

	
	/**
	 * Constructor for SamplePropertyPage.
	 */
	public ProjectPropertyPage() {
		super();
		propertyManager = new CFLintPropertyManager();
		this.propStore = new ProjectPropertyStore();
	}

	public void setElement(IAdaptable element) {
		super.setElement(element);
		IProject project = (IProject) getElement();
		this.propStore.setProject(project);
	}


	/**
	 * The project CFLint properties
	 * 
	 * @param parent
	 */
	private void addCFLintSection(Composite parent) {
		Composite composite = createDefaultComposite(parent);
		Label cflintLabel = new Label(composite, SWT.BOLD);
		cflintLabel.setText("CFLint Configuration (" + com.cflint.Version.getVersion() + ")");
		Group group = new Group(composite, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd.horizontalSpan = 2;
		group.setLayoutData(gd);
		group.setLayout(new GridLayout(3, false));
		Composite myC1= new Composite(group,SWT.NONE);
		this.cflintEnabledField = new BooleanFieldEditor(CFLintPreferenceConstants.P_CFLINT_ENABLED,
				"Enable CFLint for this project", myC1);
		this.cflintEnabledField.setPreferenceStore(propertyManager.getStore((IProject) getElement()));
		this.cflintEnabledField.load();

		Composite myC2= new Composite(group,SWT.NONE);
		this.cflintStoreConfigInProjectField = new BooleanFieldEditor(CFLintPreferenceConstants.P_CFLINT_STOREINPROJECT,
				"Store CFLint config in project", myC2);
		final Button resetButton = new Button(group, SWT.BORDER);
		resetButton.setText("Reset Config");
		resetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				Button button = (Button) e.widget;
			    MessageBox confirm = new MessageBox(Display.getCurrent().getActiveShell(),SWT.ICON_QUESTION | SWT.YES | SWT.NO);
			    File config = CFLintPlugin.getDefault().getConfigFile(propStore.getProject());
			    confirm.setMessage("Are you sure you want to delete/reset " + config.getPath() + "?");
			    if (confirm.open() == SWT.YES) {
					CFLintPlugin.getDefault().resetConfig(propStore.getProject());
			    }
			}
		});
		this.cflintStoreConfigInProjectField.setPreferenceStore(propertyManager.getStore((IProject) getElement()));
		this.cflintStoreConfigInProjectField.load();
		final Group rulesGroup = new Group(composite, SWT.NONE);
		this.cflintEnabledField.setPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent arg0) {
				if(Boolean.valueOf(arg0.getNewValue().toString()) == false) {
					GuiEnabler.recursiveUpdateEnableState(rulesGroup, GuiEnabler.EnableState.DISABLED);
				} else {
					GuiEnabler.recursiveUpdateEnableState(rulesGroup, GuiEnabler.EnableState.EDITABLE);
				}
			}
		});
		GridData gd2= new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd2.horizontalSpan = 2;
		rulesGroup.setLayoutData(gd2);
		rulesGroup.setLayout(new GridLayout(1, false));
		cflintConfigUI = new CFLintConfigUI();
		cflintConfigUI.buildGUI(rulesGroup, (IProject) getElement());
		if(!this.cflintEnabledField.getBooleanValue()) {
			GuiEnabler.recursiveUpdateEnableState(rulesGroup, GuiEnabler.EnableState.DISABLED);			
		}
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);
		addCFLintSection(composite);
		return composite;
	}

	private Composite createDefaultComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(2, true));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return composite;
	}

	protected void performDefaults() {
		cflintEnabledField.loadDefault();
		cflintStoreConfigInProjectField.loadDefault();
		cflintConfigUI.resetRules();
	}

	public boolean performOk() {
		IProject project = (IProject) getElement();
		propertyManager.setCFLintEnabledProject(cflintEnabledField.getBooleanValue(), project);
		propertyManager.setCFLintStoreConfigInProject(cflintStoreConfigInProjectField.getBooleanValue(), project);
		CFLintPlugin.getDefault().saveProjectCFLintConfig((IProject) getElement(), cflintConfigUI.getConfig());
		try {
			final String BUILDER_ID = CFLintBuilder.BUILDER_ID;
			IProjectDescription desc;
			desc = project.getDescription();
			ICommand[] commands = desc.getBuildSpec();
			boolean found = false;
			for (int i = 0; i < commands.length; ++i) {
				if (commands[i].getBuilderName().equals(BUILDER_ID)) {
					found = true;
					break;
				}
			}
			if (!found && cflintEnabledField.getBooleanValue()) {
				ICommand command = desc.newCommand();
				command.setBuilderName(BUILDER_ID);
				ICommand[] newCommands = new ICommand[commands.length + 1];
				// Add it before other builders.
				System.arraycopy(commands, 0, newCommands, 1, commands.length);
				newCommands[0] = command;
				desc.setBuildSpec(newCommands);
				project.setDescription(desc, null);
			} else if (found && !cflintEnabledField.getBooleanValue()) {
				ICommand[] newCommands = new ICommand[commands.length - 1];
				for (int i = 0; i < commands.length; ++i) {
					if (!commands[i].getBuilderName().equals(BUILDER_ID)) {
						newCommands[i] = commands[i];
					}
				}				
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

}