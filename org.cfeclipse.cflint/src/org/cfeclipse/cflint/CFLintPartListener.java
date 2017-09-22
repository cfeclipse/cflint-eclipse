package org.cfeclipse.cflint;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class CFLintPartListener implements IPartListener, IWindowListener {
	
	private IEditorPart lastActiveEditor;

	public IEditorPart getLastActiveEditor() {
		if(lastActiveEditor == null && PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null)
			lastActiveEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		return lastActiveEditor;
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			lastActiveEditor = (IEditorPart) part;
		}
	}
	
	@Override
	public void partDeactivated(IWorkbenchPart part) {
		if (part instanceof IEditorPart && part == lastActiveEditor) {
			lastActiveEditor = null;
		}
	}
	
	@Override
	public void partClosed(IWorkbenchPart part) {
		if (part instanceof IEditorPart && part == lastActiveEditor) {
			lastActiveEditor = null;
		}
	}
	
	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			lastActiveEditor = (IEditorPart) part;
		}
	}
	
	@Override
	public void partActivated(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			lastActiveEditor = (IEditorPart) part;
		}
	}

	@Override
	public void windowActivated(IWorkbenchWindow window) {
		window.getPartService().addPartListener(this);
	}

	@Override
	public void windowDeactivated(IWorkbenchWindow window) {
		window.getPartService().removePartListener(this);
	}

	@Override
	public void windowOpened(IWorkbenchWindow window) {
		windowActivated(window);
	}

	@Override
	public void windowClosed(IWorkbenchWindow window) {
		windowDeactivated(window);
	}
}
