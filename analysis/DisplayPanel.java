package analysis;

import javax.swing.JPanel;
import java.awt.Image;
import java.awt.Dimension;
import java.awt.Graphics;
public class DisplayPanel extends JPanel {
    private Image image;
    private AudioImage audioImage;
    private Dimension realSize;
    public DisplayPanel(double[][] marking) {
        audioImage = new AudioImage(marking);
        image = createImage(audioImage);
        realSize =  new Dimension(marking[0].length, marking.length);
    }
    public DisplayPanel(AudioImage imageProducer) {
        audioImage = imageProducer;
        image = createImage(imageProducer);
        realSize =  new Dimension(image.getWidth(null),image.getHeight(null));
    }
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.drawImage(image,0,0,null);
    }
    @Override
    public Dimension getMinimumSize() {
        return new Dimension(realSize);
    }
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(realSize);
    }
    public double freqResolution() {
        return audioImage.freqResolution();
    }
    public double timeResolution() {
        return audioImage.timeResolution();
    }
}
