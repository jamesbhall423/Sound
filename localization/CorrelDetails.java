package localization;

import analysis.Sound;

public class CorrelDetails implements Cloneable {
    public double correlation;
    public Sound source1;
    public Sound source2;
    public Sound combinedSound;
    public double start1;
    public double start2;
    public double length1;
    public double length2;
    public Sound sound1Partial;
    public Sound sound2Partial;
    public CorrelDetails() {

    }
    public CorrelDetails(double correlation, Sound source1, Sound source2, Sound combinedSound, double start1, double start2, double length1, double length2, Sound sound1Partial, Sound sound2Partial) {
        this.correlation = correlation;
        this.source1=source1;
        this.combinedSound=combinedSound;
        this.start1=start1;
        this.start2=start2;
        this.length1=length1;
        this.length2=length2;
        this.sound1Partial=sound1Partial;
        this.sound2Partial=sound2Partial;
    }
    //make a shallow copy of this instance
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }
}
