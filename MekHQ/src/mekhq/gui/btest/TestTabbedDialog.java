package mekhq.gui.btest;

import mekhq.gui.baseComponents.AbstractButtonDialog;

import javax.swing.*;
import java.awt.*;

public class TestTabbedDialog extends AbstractButtonDialog {
    public TestTabbedDialog(JFrame frame) {
        super(frame, "TestTabbedDialog", "TestTabbedDialog");
        initialize();
    }

    @Override
    protected Container createCenterPane() {
        return new TestTabbedPane(getFrame());
    }
}
