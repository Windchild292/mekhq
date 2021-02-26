package mekhq.gui.btest;

import mekhq.gui.baseComponents.AbstractTabbedPane;

import javax.swing.*;

public class TestTabbedPane extends AbstractTabbedPane {
    public TestTabbedPane(JFrame frame) {
        super(frame, "TestTabbedPane");
        initialize();
    }

    @Override
    protected void initialize() {
        add("One", new JPanel());
        add("Two", new JPanel());
        add("Three", new JPanel());
        add("Four", new JPanel());
        add("Five", new JPanel());
        setPreferences();
    }
}
