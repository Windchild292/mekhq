package mekhq.gui.btest;

import mekhq.gui.baseComponents.AbstractButtonDialog;
import mekhq.gui.baseComponents.AbstractScrollPane;

import javax.swing.*;
import java.awt.*;

public class TestScrollDialog extends AbstractButtonDialog {
    AbstractScrollPane sp;
    public TestScrollDialog(JFrame frame) {
        super(frame, "TestScrollDialog", "TestScrollDialog");
        initialize();
    }

    @Override
    protected Container createCenterPane() {
        sp = new TestScrollPane(getFrame());
        return sp;
    }

    @Override
    public void setVisible(boolean visible) {
        sp.setPreferences();
        super.setVisible(visible);
    }
}
