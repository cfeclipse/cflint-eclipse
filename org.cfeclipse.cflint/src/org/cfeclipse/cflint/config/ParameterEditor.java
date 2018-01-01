package org.cfeclipse.cflint.config;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;

import com.cflint.config.CFLintPluginInfo.PluginInfoRule.PluginParameter;

public class ParameterEditor extends Group {
	private Text parameterText;
	private PluginParameter parameter;
	private ListViewer listViewer;

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 * @param parameter
	 */
	public ParameterEditor(Composite parent, int style, PluginParameter parameter) {
		super(parent, style);
		this.parameter = parameter;
		this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		this.setLayout(new GridLayout(3, false));

		Label parameterLabel = new Label(this, SWT.NONE);
		parameterLabel.setText("Parameter");

		Label parameterNameLabel = new Label(this, SWT.NONE);
		parameterNameLabel.setText(parameter.getName());
		Object valueObject = parameter.getValue();

		if (valueObject instanceof String) {

			parameterText = new Text(this, SWT.BORDER);
			parameterText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			parameterText.setText(parameter.getValue().toString());
			parameterLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			parameterNameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		} else {

			// handle a list
			parameterLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
			parameterNameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
			final Composite valuesEditorComposite = new Composite(this, SWT.NULL);
			GridData gdata = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
			gdata.heightHint = 125;
			valuesEditorComposite.setLayoutData(gdata);
			valuesEditorComposite.setLayout(new GridLayout(3, false));

			final ArrayList<String> valueList = (ArrayList<String>) valueObject;
			listViewer = new ListViewer(valuesEditorComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
			listViewer.setContentProvider(new IStructuredContentProvider() {
				public Object[] getElements(Object inputElement) {
					ArrayList<String> v = (ArrayList<String>) inputElement;
					return v.toArray();
				}

				public void dispose() {
					System.out.println("Disposing ...");
				}

				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
					System.out.println("Input changed: old=" + oldInput + ", new=" + newInput);
				}
			});

			listViewer.setInput(valueList);

			Button buttonAdd;
			Button buttonRemove;
			Button buttonModify;

			final Composite buttonGroup = new Composite(valuesEditorComposite, SWT.NULL);
			FillLayout fillLayout = new FillLayout(SWT.VERTICAL);
			fillLayout.spacing = 2;

			buttonGroup.setLayout(fillLayout);
			buttonGroup.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));

			buttonAdd = new Button(buttonGroup, SWT.PUSH);
			buttonAdd.setText("Add");
			buttonAdd.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					String text = JOptionPane.showInputDialog(null, "New: ");
					if (text != null) {
						valueList.add(text);
					}
					listViewer.refresh(false);
				}
			});

			buttonModify = new Button(buttonGroup, SWT.PUSH);
			buttonModify.setText("Modify");
			buttonModify.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					IStructuredSelection selection = (IStructuredSelection) listViewer.getSelection();
					String element = (String) selection.getFirstElement();
					if (element == null) {
						return;
					}
					String text = JOptionPane.showInputDialog(null, "Rename: ", element);
					if (text != null) {
						for (int x = 0; x < valueList.size(); x++) {
							if (valueList.get(x) == element) {
								valueList.set(x, text);
							}
						}
						listViewer.refresh(false);
					}
				}
			});

			buttonRemove = new Button(buttonGroup, SWT.PUSH);
			buttonRemove.setText("Remove");
			buttonRemove.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					IStructuredSelection selection = (IStructuredSelection) listViewer.getSelection();
					String element = (String) selection.getFirstElement();
					if (element == null) {
						return;
					}
					valueList.remove(element);
					listViewer.refresh(false);
				}
			});

		}

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public PluginParameter getParameter() {
		if (listViewer != null) {
			parameter.setValue(listViewer.getInput());
		} else {
			parameter.setValue(parameterText.getText());
		}
		return parameter;
	}

}
