package localization;

import java.io.File;
import java.util.Arrays;

import analysis.Sound;

public abstract class Pitch implements SoundPart {
    protected double startFreq;
    protected double endFreq;
    protected double frequency;
    protected double[] amplitude;
    @Override
    public double[] amplitude(double start, double end, double freq) {
        double interval = Math.min(endFreq,end)-Math.max(startFreq,start);
        double range = 1/interval;
        if (interval>0 && Math.abs(freq-frequency)<range) {
            return (double[]) amplitude.clone();
        } else {
            return new double[2];
        }
    }
    public double start() {
        return startFreq;
    }
    public double end() {
        return endFreq;
    }
    public double frequency() {
        return frequency;
    }

    @Override
    public double[] pressureValues(double start, double end, int samples) {
        double interval = (end-start)/samples;
        double[] out = new double[samples];
        double ampVal = Math.hypot(amplitude[0],amplitude[1]);
        int sampleFreqStart = (int)Math.round((startFreq-start)/interval);
        int sampleFreqEnd = (int)Math.round((endFreq-start)/interval);
        if (sampleFreqStart<0) sampleFreqStart=0;
        if (sampleFreqEnd>samples) sampleFreqEnd=samples;
        Arrays.fill(out,sampleFreqStart,sampleFreqEnd,ampVal);
        return out;
    }

    @Override
    public double[][] freqPhaseValues(double start, double end, int samples) {
        int freqIndex = (int) (frequency*(end-start)/samples);
        double[][] out = new double[samples][2];
        out[freqIndex] = (double[]) amplitude.clone();
        return out;
    }

    @Override
    public double energy(double start, double end) {
        if (start<startFreq) start = startFreq;
        if (end<endFreq) end = endFreq;
        if (end<start) return 0;
        else return (end-start)*(amplitude[0]*amplitude[0]+amplitude[1]*amplitude[1])/2;
    }

    @Override
    public void save(File soundFile) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    public void play() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'play'");
    }

    @Override
    public double length() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'length'");
    }

    @Override
    public Sound trimStart(double start) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'trimStart'");
    }

    @Override
    public Sound trimEnd(double end) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'trimEnd'");
    }

    @Override
    public Sound retime(double shiftStart, double scale) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'retime'");
    }

    @Override
    public Sound removeCorrelation(CorrelDetails details, boolean latter) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeCorrelation'");
    }

    @Override
    public Sound negate() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'negate'");
    }

    @Override
    public Sound combine(Sound other, double startThis, double startOther, double endThis, double endOther,
            double ampThis, double ampOther) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'combine'");
    }

    // @Override
    // public CorrelDetails startingCorrelDetails() {
    //     // TODO Auto-generated method stub
    //     throw new UnsupportedOperationException("Unimplemented method 'startingCorrelDetails'");
    // }

    @Override
    public CorrelDetails getCorrelation(Sound other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCorrelation'");
    }

    @Override
    public Sound getSound(double[][] freqValues, double length) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSound'");
    }
    
}
