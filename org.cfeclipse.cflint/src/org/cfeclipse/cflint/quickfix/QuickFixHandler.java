package org.cfeclipse.cflint.quickfix;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.views.markers.ExtendedMarkersView;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.views.markers.MarkerViewUtil;

public class QuickFixHandler extends AbstractHandler {

	public TreeViewer getCurrentMarkers(ExtendedMarkersView view) {
		Method method;
		try {
			method = ExtendedMarkersView.class.getDeclaredMethod("getViewer");
			method.setAccessible(true);
		} catch (SecurityException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			return null;
		}
		try {
			return (TreeViewer) method.invoke(view);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public IMarker getSelectedMarker(ExecutionEvent event) {
		// ISelection selection = (ISelection) HandlerUtil.getCurrentSelection(event);

		int lineNumber = 1;
		IMarker targetMarker = null;

		ITextEditor editor = (TextEditor) HandlerUtil.getActivePart(event);
		ISelectionProvider selectionProvider = ((ITextEditor) editor).getSelectionProvider();
		ISelection selection = selectionProvider.getSelection();
		if (selection instanceof ITextSelection) {
			ITextSelection textSelection = (ITextSelection) selection;
			lineNumber = textSelection.getStartLine();
		} else if (selection instanceof StructuredSelection) {
			StructuredSelection sselection = (StructuredSelection) selection;
			IMarker elem = (IMarker) sselection.getFirstElement();
			targetMarker = elem;
			try {
				lineNumber = Integer.parseInt(elem.getAttribute("lineNumber").toString()) - 1;
			} catch (NumberFormatException | CoreException e) {
				lineNumber = 1;
			}
		}
		if(targetMarker == null) {
			IAnnotationModel annotationModel = editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
			IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
			annotationModel.getAnnotationIterator();

			for (Iterator<Annotation> iter = annotationModel.getAnnotationIterator(); iter.hasNext();) {
				Annotation annotation = (Annotation) iter.next();
				if (annotation instanceof MarkerAnnotation) {
					MarkerAnnotation markerAnnotation = (MarkerAnnotation) annotation;
//					markerAnnotation.setQuickFixable(true);
					Position position = annotationModel.getPosition(annotation);
					try {
						int lineOfAnnotation = doc.getLineOfOffset(position.getOffset());
						if (lineNumber == lineOfAnnotation) {
							targetMarker = ((MarkerAnnotation) annotation).getMarker();
						}
					} catch (BadLocationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		if(targetMarker != null) {
			boolean hasResolution = IDE.getMarkerHelpRegistry().hasResolutions(targetMarker);
			if(hasResolution) {
				return targetMarker;			
			}
		}
		return null;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		String commandId = "org.eclipse.jdt.ui.edit.text.java.correction.assist.proposals";
		IMarker targetMarker = getSelectedMarker(event);


		MarkerViewUtil.showMarker(HandlerUtil.getActiveWorkbenchWindow(event).getActivePage(), targetMarker, true);

		IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
		try {
			handlerService.executeCommand(commandId, null);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		// ICommandService commandService = (ICommandService)
		// PlatformUI.getWorkbench().getService(ICommandService.class);
		// Command cmd = commandService.getCommand(commandId);
		// if (!cmd.isDefined()) {
		// return false;
		// }

		// org.eclipse.ui.internal.views.markers.ProblemsView view =
		// (org.eclipse.ui.internal.views.markers.ProblemsView)
		// HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().findView("org.eclipse.ui.views.ProblemView");
		// TreeViewer tree = getCurrentMarkers(view);
		// Object items = tree.getInput();

		// Object els = ((org.eclipse.ui.internal.views.markers.Markers)
		// tree.getInput()).getElements();

		// try {
		// IEvaluationContext ctx = (IEvaluationContext) event.getApplicationContext();
		// ctx.addVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME,
		// new StructuredSelection(new IMarker[] { targetMarker }));
		// ctx.addVariable("quickFix", targetMarker);
		// cmd.executeWithChecks(event);
		// } catch (NotDefinedException | NotEnabledException | NotHandledException e1)
		// {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }

		return null;
	}

}