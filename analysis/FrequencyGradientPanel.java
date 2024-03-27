package analysis;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

public class FrequencyGradientPanel extends JPanel {
    private double resolution;
    private double maxFrequency;
    public FrequencyGradientPanel(double resolution, double maxFrequency) {
        this.resolution = resolution;
        this.maxFrequency = maxFrequency;
    }
    @Override
    public void paint(Graphics g) {
        for (int hzMarker = 0; hzMarker < maxFrequency; hzMarker+=500) {
            g.drawString(hzMarker+"hz", 0, (int)(getPreferredSize().height-hzMarker*resolution)-2);
            g.drawLine( 0, (int)(getPreferredSize().height-hzMarker*resolution)-1, 50, (int)(getPreferredSize().height-hzMarker*resolution)-1);
        }
    }
    @Override
    public Dimension getMinimumSize() {
        return new Dimension(50,(int)(maxFrequency*resolution));
    }
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(50,(int)(maxFrequency*resolution));
    }
}
