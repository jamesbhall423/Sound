package analysis;

import java.awt.Dimension;
import java.awt.Graphics;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JPanel;

public class TimeGradientPanel extends JPanel {
    private double resolution;
    private double length;
    private double start;
    private double interval;
    private int sidePanelWidth;
    public TimeGradientPanel(double resolution, double length, double start, double interval, int sidePanelWidth) {
        this.resolution = resolution;
        this.length = length;
        this.start = start;
        this.interval = interval;
        this.sidePanelWidth = sidePanelWidth;
    }
    @Override
    public void paint(Graphics g) {
        NumberFormat formatter = new DecimalFormat("#0.0");
        for (double timeMarker = Converter.floor(start,interval); timeMarker < start+length-0.01; timeMarker+=interval) {
            g.drawString(formatter.format(timeMarker)+"s", (int)(timeMarker*resolution)+5+sidePanelWidth, 15);
            g.drawLine((int)(timeMarker*resolution)+sidePanelWidth, 0, (int)(timeMarker*resolution)+sidePanelWidth,20);
        }
    }
    @Override
    public Dimension getMinimumSize() {
        return new Dimension((int)(length*resolution)+2*sidePanelWidth,20);
    }
    @Override
    public Dimension getPreferredSize() {
        return new Dimension((int)(length*resolution)+2*sidePanelWidth,20);
    }
}
