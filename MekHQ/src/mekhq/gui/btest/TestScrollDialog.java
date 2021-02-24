package mekhq.gui.btest;

import mekhq.gui.baseComponents.AbstractButtonDialog;

import javax.swing.*;
import java.awt.*;

public class TestScrollDialog extends AbstractButtonDialog {
    public TestScrollDialog(JFrame frame) {
        super(frame, "TestScrollDialog", "TestScrollDialog");
        initialize();
    }

    @Override
    protected Container createCenterPane() {
        return new TestScrollPane(getFrame());
    }
}
