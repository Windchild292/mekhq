package mekhq.gui.btest;

import mekhq.gui.baseComponents.AbstractDialog;

import javax.swing.*;
import java.awt.*;

public class TestDialog extends AbstractDialog {
    public TestDialog(JFrame frame) {
        super(frame, "TestDialog", "TestDialog");
        initialize();
    }

    @Override
    protected Container createCenterPane() {
        return new JPanel();
    }
}
