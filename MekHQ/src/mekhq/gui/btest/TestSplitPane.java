package mekhq.gui.btest;

import mekhq.gui.baseComponents.AbstractSplitPane;

import javax.swing.*;
import java.awt.*;

public class TestSplitPane extends AbstractSplitPane {
    public TestSplitPane(JFrame frame) {
        super(frame, "TestSplitPane");
        initialize();
    }

    @Override
    protected Component createLeftComponent() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setBorder(BorderFactory.createTitledBorder("Left"));
        panel.add(new JLabel("Hi"));
        panel.add(new JLabel("Bye"));
        panel.add(new JLabel("Si"));
        panel.add(new JLabel("Fi"));
        panel.add(new JLabel("Di"));
        panel.add(new JLabel("Mi"));
        panel.add(new JLabel("Bi"));
        return panel;
    }

    @Override
    protected Component createRightComponent() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setBorder(BorderFactory.createTitledBorder("Right"));
        panel.add(new JLabel("Hi"));
        panel.add(new JLabel("Bye"));
        panel.add(new JLabel("Si"));
        panel.add(new JLabel("Fi"));
        panel.add(new JLabel("Di"));
        panel.add(new JLabel("Mi"));
        panel.add(new JLabel("Bi"));
        return panel;
    }
}
