package analysis;

import java.io.File;

import localization.CorrelDetails;

public class ClippedSound implements Sound {
    private Sound baseSound;
    private double startOffset;
    private double endOffset;
    public ClippedSound(Sound baseSound, double startOffset, double endOffset) {
        this.baseSound = baseSound;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    @Override
    public double[] amplitude(double start, double end, double freq) {
        return baseSound.amplitude(start+startOffset, end+startOffset, freq);
    }

    @Override
    public double[] pressureValues(double start, double end, int samples) {
        return baseSound.pressureValues(start+startOffset, end+startOffset, samples);
    }
    @Override
    public double[] pressureValues(double start, double end, int samples, boolean[] channels) {
        return baseSound.pressureValues(start, end, samples,channels);
    }

    @Override
    public double[][] freqValues(double start, double end, int samples) {
        return baseSound.freqValues(start+startOffset, end+startOffset, samples);
    }

    @Override
    public double energy(double start, double end) {
        return baseSound.energy(start+startOffset, end+startOffset);
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
        return baseSound.length()-startOffset-endOffset;
    }

    @Override
    public Sound trimStart(double start) {
        return new ClippedSound(baseSound, startOffset+start, endOffset);
    }

    @Override
    public Sound trimEnd(double end) {
        if (end < length()) {
            return new ClippedSound(baseSound,startOffset,endOffset+length()-end);
        } else {
            return this;
        }
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

    @Override
    public Sound getSound(double[][] freqValues, double length) {
        return baseSound.getSound(freqValues, length);
    }

    @Override
    public int channels() {
        return baseSound.channels();
    }
    @Override
    public Sound getShiftedByPhase(double freq, double paramOffset) {
       return new ClippedSound(baseSound.getShiftedByPhase(freq,startOffset+paramOffset), startOffset, endOffset);
    }

}
