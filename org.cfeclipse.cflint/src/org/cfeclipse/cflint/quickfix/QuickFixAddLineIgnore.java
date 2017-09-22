package org.cfeclipse.cflint.quickfix;

import org.cfeclipse.cflint.CFLintPlugin;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.ITextEditor;

public class QuickFixAddLineIgnore extends QuickFix {
	String	label;
	IMarker	marker;
	
	QuickFixAddLineIgnore(IMarker mk, String label) {
		super(mk, label);
		this.label = label;
		this.marker = mk;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void run(IMarker marker) {
		String code;
		try {
			code = marker.getAttribute("CFLINT_MESSAGE_CODE").toString();
			int line = Integer.parseInt(marker.getAttribute(IMarker.LINE_NUMBER).toString()) -1;
			String col = marker.getAttribute(IMarker.CHAR_START).toString();
			String pasteText = " // cflint ignore:line";
			
			IEditorPart part = CFLintPlugin.getDefault().getLastActiveEditor();
			if (!(part instanceof AbstractTextEditor)) {
				return;
			}
			ITextEditor editor = (ITextEditor) part;
			if (editor != null && editor.isEditable()) {
				IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
				int offset = doc.getLineOffset(line) + doc.getLineLength(line) - 1;
				doc.replace(offset, 0, pasteText);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}