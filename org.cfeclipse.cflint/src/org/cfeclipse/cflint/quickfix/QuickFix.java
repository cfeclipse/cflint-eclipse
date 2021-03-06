package org.cfeclipse.cflint.quickfix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IMarkerResolution;

public class QuickFix implements IMarkerResolution {
      String label;
      IMarker marker;
      QuickFix(IMarker mk, String label) {
         this.label = label;
         this.marker = mk;
      }
      public String getLabel() {
         return label;
      }
      public void run(IMarker marker) {
         MessageDialog.openInformation(null, "QuickFix Demo",
            "This quick-fix is not yet implemented");
      }
   }