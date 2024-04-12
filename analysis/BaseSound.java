package analysis;

import java.io.File;
import java.util.Arrays;

import localization.CorrelDetails;

public class BaseSound implements Sound {
    private final double[][] values;
    private final double samplesPerSecond;
    public BaseSound(long[][] values, double samplesPerSecond) {
        this.values = Converter.fromLong(values);
        this.samplesPerSecond = samplesPerSecond;
    }
    public BaseSound(double[][] values, double samplesPerSecond) {
        this.values = values;
        this.samplesPerSecond = samplesPerSecond;
    }
    @Override
    public double[] amplitude(double start, double end, double freq) {
        int startN = (int)(start*samplesPerSecond);
        int endN = (int)(end*samplesPerSecond);
        double angularFreq = 2*Math.PI*freq/samplesPerSecond;
        double[] out = {0.0,0.0};
        for (int i = startN; i < endN && i < values.length; i++) for (int j = 0; j < values[i].length; j++) {
            out[0] += Math.sin(angularFreq*(i-startN))*values[i][j];
            out[1] += Math.cos(angularFreq*(i-startN))*values[i][j];
        }
        return out;
    }

    @Override
    public double[] pressureValues(double start, double end, int samples) {
        boolean[] channels = new boolean[values[0].length];
        Arrays.fill(channels, true);
        return pressureValues(start, end, samples,channels);
    }

    public double[] pressureValues(double start, double end, int samples, boolean[] channels) {
        double valuesPerSample = (end-start)*samplesPerSecond / samples;
        double[] out = new double[samples];
        for (int i = 0; i < samples; i++) {
            int numRef = 0;
            for (double ref = start*samplesPerSecond+i*valuesPerSample;ref<start*samplesPerSecond+(i+1)*valuesPerSample+1 && Math.floor(ref)+1<values.length;ref++) {
                int index = (int)(Math.floor(ref));
                double frac = ref - index;
                for (int j = 0; j < values[index].length; j++) if (channels[j]) {
                    out[i]+=frac*values[index][j]+(1-frac)*values[index+1][j];
                    numRef++;
                }
            }
            if (numRef==0) out[i]=out[i-1];
            else out[i]/=numRef;
        }
        return out;
    }
    @Override
    public double[][] freqPhaseValues(double start, double end, int samples) {
        boolean[] realChannels = new boolean[values[0].length];
        boolean[] imagChannels = new boolean[values[0].length];
        for (int i = 0; i < values[0].length; i++) {
            realChannels[i] = ((i % 2)==0);
            imagChannels[i] = ((i % 2)==1);
        }
        int size = 1;
        while (size<samples) size*=2;
        double[] real = pressureValues(start, end, samples, realChannels);
        double[] imag = pressureValues(start, end, samples, imagChannels);
        double[][] fft = Converter.fft(real,imag,false);
        return fft;
    }

    @Override
    public double energy(double start, double end) {
        double total = 0;
        double totalSquare = 0;
        int startSample = (int)(start*samplesPerSecond);
        int endSample = Math.min(values.length,(int)(end*samplesPerSecond));
        int sampleDif = endSample-startSample;
        for (int index = startSample; index < endSample; index++) {
            for (int channel = 0; channel < values[index].length; channel++) {
                total += values[index][channel];
                totalSquare += values[index][channel]*values[index][channel];
            }
        }
        double average = total / sampleDif;
        double averageSquare = totalSquare / sampleDif;
        return (averageSquare-average*average)*sampleDif / values[0].length;
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
        return values.length/samplesPerSecond;
    }

    @Override
    public Sound trimStart(double start) {
        return new ClippedSound(this,start,0);
    }

    @Override
    public Sound trimEnd(double end) {
        if (end<length())  return new ClippedSound(this,0,length()-end);
        else return this;
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
        double[][] flipFreq = Converter.flipArrayDimensions(freqValues);
        double[][] soundValues = Converter.fft(flipFreq[0],flipFreq[1],true);
        return new BaseSound(soundValues,soundValues.length/length);
    }
    @Override
    public int channels() {
        return values[0].length;
    }
    @Override
    public Sound getShiftedByPhase(double freq, double startOffset) {
        if (values[0].length!=2) throw new UnsupportedOperationException("Unimplemented method 'getShiftedByPhase' for "+values[0].length+" channels");
        double[][] next = new double[values.length][2];
        for (int i = 0; i < values.length; i++) {
            next[i] = Converter.adjustPointBack(values[i], freq, i/samplesPerSecond-startOffset);
        }
        return new BaseSound(next,samplesPerSecond);
    }
    @Override
    public Sound scaleVolume(double scale) {
        double[][] out = new double[values.length][values[0].length];
        for (int i = 0; i < out.length; i++) for (int j = 0; j < out.length; j++) {
            out[i][j] = scale * values[i][j];
        }
        return new BaseSound(out,samplesPerSecond);
    }
    @Override
    public Sound getSound(double[][][] timeFreqPhaseValues, double length) {
        double[][][] flipFreq = new double[timeFreqPhaseValues.length][][];
        for (int i = 0; i < timeFreqPhaseValues.length; i++) flipFreq[i] = Converter.flipArrayDimensions(timeFreqPhaseValues[i]);
        double[][][] soundValueSections = new double[flipFreq.length][][];
        for (int i = 0; i < timeFreqPhaseValues.length; i++) soundValueSections[i] = Converter.fft(flipFreq[i][0],flipFreq[i][1],true);
        double[][] soundValues = new double[soundValueSections.length*soundValueSections[0].length][];
        for (int i = 0; i < soundValues.length; i++) soundValues[i] = soundValueSections[i/soundValueSections[0].length][i%soundValueSections[0].length];
        return new BaseSound(soundValues,soundValues.length/length);
    }
}
