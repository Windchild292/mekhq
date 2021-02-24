package mekhq.gui.btest;

import mekhq.gui.baseComponents.AbstractScrollPane;

import javax.swing.*;
import java.awt.*;

public class TestScrollPane extends AbstractScrollPane {
    public TestScrollPane(JFrame frame) {
        super(frame, "TestScrollPane");
        initialize();
    }

    @Override
    protected void initialize() {
        setLayout(new ScrollPaneLayout());
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setBorder(BorderFactory.createTitledBorder("Me"));
        panel.add(new JLabel("Hi"));
        panel.add(new JLabel("Bye"));
        panel.add(new JLabel("Si"));
        panel.add(new JLabel("Fi"));
        panel.add(new JLabel("Di"));
        panel.add(new JLabel("Mi"));
        panel.add(new JLabel("Bi"));
        panel.add(new JLabel("Hi"));
        panel.add(new JLabel("Bye"));
        panel.add(new JLabel("Si"));
        panel.add(new JLabel("Fi"));
        panel.add(new JLabel("Di"));
        panel.add(new JLabel("Mi"));
        panel.add(new JLabel("Bi"));
        panel.add(new JLabel("Hi"));
        panel.add(new JLabel("Bye"));
        panel.add(new JLabel("Si"));
        panel.add(new JLabel("Fi"));
        panel.add(new JLabel("Di"));
        panel.add(new JLabel("Mi"));
        panel.add(new JLabel("Bi"));
        panel.add(new JLabel("Hi"));
        panel.add(new JLabel("Bye"));
        panel.add(new JLabel("Si"));
        panel.add(new JLabel("Fi"));
        panel.add(new JLabel("Di"));
        panel.add(new JLabel("Mi"));
        panel.add(new JLabel("Bi"));
        panel.add(new JLabel("Hi"));
        panel.add(new JLabel("Bye"));
        panel.add(new JLabel("Si"));
        panel.add(new JLabel("Fi"));
        panel.add(new JLabel("Di"));
        panel.add(new JLabel("Mi"));
        panel.add(new JLabel("Bi"));
        panel.add(new JLabel("Hi"));
        panel.add(new JLabel("Bye"));
        panel.add(new JLabel("Si"));
        panel.add(new JLabel("Fi"));
        panel.add(new JLabel("Di"));
        panel.add(new JLabel("Mi"));
        panel.add(new JLabel("Bi"));
        add(panel);
        setPreferences();
    }
}
