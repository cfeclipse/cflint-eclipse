package org.cfeclipse.cflint.preferences;

import org.cfeclipse.cflint.CFLintBuilder;
import org.cfeclipse.cflint.store.CFLintPreferenceConstants;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.texteditor.ITextEditor;

public class ShowQuickFixPropertyTester extends PropertyTester {
	public static final String PROPERTY_NAMESPACE = "org.cfeclipse.cflint.enablement";
	public static final String PROPERTY_CAN_SHOW = "canShow";
	public static final String PROPERTY_IS_MARKER = "isMarkerSelected";
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
		} else if (PROPERTY_IS_MARKER.equals(property)) {
			System.out.println("WHOLY FUCK!");
			// for some reason the receiver selection doesn't always update? grab it fresh:
			IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
			if (editor instanceof ITextEditor) {

				ISelectionProvider selectionProvider = ((ITextEditor) editor).getSelectionProvider();
				ISelection selection = selectionProvider.getSelection();

				if (selection instanceof ITextSelection) {
					TextSelection textSelection = (TextSelection) receiver;
					int lineNumber = textSelection.getStartLine()+1;
					try {
						IMarker[] markers = ResourceUtil.getResource(editor.getEditorInput())
								.findMarkers(CFLintBuilder.MARKER_TYPE.PROBLEM.toString(), true, IResource.DEPTH_ZERO);
						for (IMarker marker : markers) {
							Object lineNumberAttribute = marker.getAttribute("lineNumber");
							if (lineNumberAttribute != null) {
								int markerLineNumber = Integer.parseInt(lineNumberAttribute.toString());
								System.out.println("Mak:" + markerLineNumber + " line:" + lineNumber);
								if (markerLineNumber == lineNumber) {
									System.out.println("FOUND");
									return true;
								}
							}
						}
					} catch (CoreException e) {
						e.printStackTrace();
					}

				}
			}

		}
		return false;
	}
}