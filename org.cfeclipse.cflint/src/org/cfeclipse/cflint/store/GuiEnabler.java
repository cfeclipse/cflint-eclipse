package org.cfeclipse.cflint.store;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class GuiEnabler {
    /**
     * Used to set the enable state of a tree of controls.
     */
    public enum EnableState {
        /**
         * The control is disabled, for when there is no information to show in
         * it. All controls, including labels, are disabled.
         */
        DISABLED, 
        /**
         * For when there is information to show in the control, but it should
         * be read-only. Controls are disabled, except Text which is
         * non-editable, and Lables, which are enabeled.
         */
        READ_ONLY, 
        /**
         * All controls are enabled and editable.
         */
        EDITABLE
    }

    private static final String ENABLED_KEY = GuiEnabler.class.getName() + ".disabled";
    private static final String EDITABLE_KEY = GuiEnabler.class.getName() + ".read_only";

    /**
     * Disables or makes read-only {@code control} and all its child controls (recursively). 
     * Also restores the state of controls previously disabled by this method. The action
     * performed on the controls is determined by {@link EnableState enableState}. 
     * 
     * @param excluded These controls (and their children) are not modified by
     * the method.
     */
    public static void recursiveUpdateEnableState(Control control, EnableState enableState, Control... excluded) {
        updateEnabledState(control, enableState, new HashSet<>(Arrays.asList(excluded)));
    }

    /**
     * See {@link GuiEnabler#recursiveUpdateEnableState(Control, EnableState, Control...)}. 
     */
    public static void updateEnabledState(Control control, EnableState enableState, Set<Control> excluded) {
        if (excluded.contains(control)) {
            return;
        } else if (control instanceof Composite && !(control instanceof Combo)) {
            for (Control child : ((Composite) control).getChildren()) {
                updateEnabledState(child, enableState, excluded);
            }
        } else {
            updateControl(control, enableState);
        }
    }

    /**
     * Updates a single control to have its proper state for enableState.
     */
    private static void updateControl(Control control, EnableState enableState) {
        if (enableState == EnableState.DISABLED) {
            makeDisabled(control);
        } else if (enableState == EnableState.READ_ONLY) {
            if (control instanceof Text) {
                makeNonEditable((Text) control);
                makeEnabled(control);
            } if (control instanceof Label) {
                makeEnabled(control);
            } else {
                makeDisabled(control);
            }
        } else if (enableState == EnableState.EDITABLE) {
            makeEnabled(control);
            if (control instanceof Text) makeEditable((Text) control);
        }
    }


    private static void makeEnabled(Control control) {
        if (control.getData(ENABLED_KEY) != null) {
            control.setData(ENABLED_KEY, null);
            control.setEnabled(true);
        }
    }

    private static void makeDisabled(Control control) {
        if (control.getEnabled()) {
            control.setData(ENABLED_KEY, "marked");
            control.setEnabled(false);
        }
    }

    private static void makeEditable(Text text) {
        if (text.getData(EDITABLE_KEY) != null) {
            text.setData(EDITABLE_KEY, null);
            text.setEditable(true);
        }
    }

    private static void makeNonEditable(Text text) {
        if (text.getEditable()) {
            text.setData(EDITABLE_KEY, "marked");
            text.setEditable(false);
        }
    }
}