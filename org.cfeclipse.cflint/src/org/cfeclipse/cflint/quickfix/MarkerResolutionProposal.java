package org.cfeclipse.cflint.quickfix;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import org.eclipse.core.runtime.CoreException;
import org.cfeclipse.cflint.CFLintPlugin;
import org.eclipse.core.resources.IMarker;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;

import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;

public class MarkerResolutionProposal implements ICompletionProposal {

	private IMarkerResolution fResolution;
	private IMarker fMarker;

	/**
	 * Constructor for MarkerResolutionProposal.
	 * 
	 * @param resolution
	 *            the marker resolution
	 * @param marker
	 *            the marker
	 */
	public MarkerResolutionProposal(IMarkerResolution resolution, IMarker marker) {
		fResolution = resolution;
		fMarker = marker;
	}

	public void apply(IDocument document) {
		fResolution.run(fMarker);
	}

	public String getAdditionalProposalInfo() {
		if (fResolution instanceof IMarkerResolution2) {
			return ((IMarkerResolution2) fResolution).getDescription();
		}
		if (fResolution instanceof ICompletionProposal) {
			return ((ICompletionProposal) fResolution).getAdditionalProposalInfo();
		}
		try {
			String problemDesc = (String) fMarker.getAttribute(IMarker.MESSAGE);
			return problemDesc;
		} catch (CoreException e) {
			e.printStackTrace();
			// CFLintPlugin.log(e);
		}
		return null;
	}

	public IContextInformation getContextInformation() {
		return null;
	}

	public String getDisplayString() {
		return fResolution.getLabel();
	}

	public Image getImage() {
		if (fResolution instanceof IMarkerResolution2) {
			return ((IMarkerResolution2) fResolution).getImage();
		}
		if (fResolution instanceof ICompletionProposal) {
			return ((ICompletionProposal) fResolution).getImage();
		}
		return null;
	}

	public int getRelevance() {
		if (fResolution instanceof ICompletionProposal) {
			// return ((ICompletionProposal) fResolution).getRelevance();
		}
		return 10;
	}

	public Point getSelection(IDocument document) {
		if (fResolution instanceof ICompletionProposal) {
			return ((ICompletionProposal) fResolution).getSelection(document);
		}
		return null;
	}

}