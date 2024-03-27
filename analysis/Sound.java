package analysis;


import java.io.File;

import localization.CorrelDetails;

public interface Sound {
    public double[] pressureValues(double start, double end, int samples, boolean[] channels);
    public double[] amplitude(double start, double end, double freq);
    public int channels();
    public double[] pressureValues(double start, double end, int samples);
    public double[][] freqValues(double start, double end, int samples);
    public Sound getSound(double[][] freqValues, double length);
    public Sound getShiftedByPhase(double freq,double startOffset);
    public double energy(double start, double end);
    public void save(File soundFile);
    public void play();
    public double length();
    public Sound trimStart(double start);
    public Sound trimEnd(double end);
    public Sound retime(double shiftStart, double scale);
    public Sound removeCorrelation(CorrelDetails details, boolean latter);
    public Sound negate();
    public Sound combine(Sound other, double startThis, double startOther, double endThis, double endOther, double ampThis, double ampOther);
}
