package analysis;

import javax.swing.JFrame;

import java.awt.BorderLayout;

public class DisplayFrame extends JFrame {
    public DisplayFrame(double[][] marking) {
        initialize(new DisplayPanel(marking));
    }
    public DisplayFrame(AudioImage imageProducer, String title) {
        initialize(new DisplayPanel(imageProducer));
        setTitle(title);
    }
    private void initialize(DisplayPanel panel) {
        setLayout(new BorderLayout());
        add(new TimeGradientPanel(1.0/panel.timeResolution(), panel.getPreferredSize().width*panel.timeResolution(), 0.0, 0.2,50),BorderLayout.NORTH);
        add(new FrequencyGradientPanel(1.0/panel.freqResolution(),panel.getPreferredSize().height*panel.freqResolution()), BorderLayout.WEST);
        add(panel);
        add(new FrequencyGradientPanel(1.0/panel.freqResolution(),panel.getPreferredSize().height*panel.freqResolution()), BorderLayout.EAST);
        add(new TimeGradientPanel(1.0/panel.timeResolution(), panel.getPreferredSize().width*panel.timeResolution(), 0.0, 0.2,50),BorderLayout.SOUTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }
    
}
