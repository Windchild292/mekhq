package mekhq.gui.btest;

import mekhq.gui.baseComponents.AbstractButtonDialog;

import javax.swing.*;
import java.awt.*;

public class TestSplitDialog extends AbstractButtonDialog {
    public TestSplitDialog(JFrame frame) {
        super(frame, "TestSplitDialog", "TestSplitDialog");
        initialize();
    }

    @Override
    protected Container createCenterPane() {
        return new TestSplitPane(getFrame());
    }
}
