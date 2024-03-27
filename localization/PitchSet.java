package localization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import analysis.ClippedSound;
import analysis.Converter;
import analysis.Sound;
import analysis.Sounds;

public class PitchSet implements Comparable<PitchSet> {
    private double minFreq;
    private double maxFreq;
    private double start;
    private double end;
    private double samplePeriod;
    public PitchSet(double minFreq, double maxFreq, double start, double end, double samplePeriod) {
        this.maxFreq=maxFreq;
        this.minFreq=minFreq;
        this.start=start;
        this.end=end;
        this.samplePeriod = samplePeriod;
    }
    public PitchSet(Pitch pitch, double samplePeriod) {
        this.maxFreq = pitch.frequency();
        this.minFreq = pitch.frequency();
        this.start = pitch.start();
        this.end = pitch.end();
        this.samplePeriod = samplePeriod;
    }
    public PitchSet(PitchSet set1, PitchSet set2) {
        this.minFreq = Math.min(set1.minFreq,set2.minFreq);
        this.maxFreq = Math.max(set1.maxFreq,set2.maxFreq);
        this.start = Math.min(set1.start,set2.start);
        this.end = Math.max(set1.end,set2.end);
        this.samplePeriod = Math.max(set1.samplePeriod,set2.samplePeriod);
    }

    public double minFreq() {
        return minFreq;
    }
    public double maxFreq() {
        return maxFreq;
    }
    public double start() {
        return start;
    }
    public double end() {
        return end;
    }
    public static void addPitchset(PitchSet toAdd, Set<PitchSet> sets, double seperation) {
        Iterator<PitchSet> itter = sets.iterator();
        while (itter.hasNext()) {
            PitchSet next = itter.next();
            if (next.matches(toAdd, seperation)) {
                itter.remove();
                addPitchset(new PitchSet(toAdd, next),sets, seperation);
                return;
            }
        }
        sets.add(toAdd);
    }
    public static Set<PitchSet> sortIntoSets(List<Pitch> pitches, double freqSeperation,double timeSeperation) {
        Set<PitchSet> out = new TreeSet<>();
        for (Pitch next: pitches) {
            addPitchset(new PitchSet(next, timeSeperation), out, freqSeperation);
        }
        return out;
    }
    public String toString() {
        return String.format("%,.2f", start)+"s-"+String.format("%,.2f", end)+"s "+String.format("%,.0f", minFreq)+"-"+String.format("%,.0f", maxFreq)+"hz";
    }
    public boolean matches(PitchSet other, double seperation) {
        return minFreq<=other.maxFreq()+seperation&&maxFreq+seperation>=other.minFreq()&&start<=other.end()&&end>=other.start();
    }
    private double averageFreqByAcceleration(Sound filtered, int samplesPerSecond) {
        double[] pressureValues = filtered.pressureValues(0.0, filtered.length(), (int)(samplesPerSecond*filtered.length()));
        double[] accelValues = new double[pressureValues.length-2];
        for (int i = 0; i < accelValues.length; i++) accelValues[i] = pressureValues[i+1]*(2*pressureValues[i+1]-pressureValues[i]-pressureValues[i+2]);
        double freq = samplesPerSecond*Math.sqrt(Arrays.stream(accelValues).sum()/filtered.energy(0, filtered.length()))/Math.PI;
        return freq;
    }
    private double averageFreqByEnergy(Sound filtered, int samplesPerSecond) {
        double rotationalContributions = 0.0;
        double totalEnergy = 0.0;
        int samples = Converter.power2Higher(filtered.length()*samplesPerSecond);
        int indexStart = (int) Math.max(0,minFreq*filtered.length()-1);
        int indexEnd = (int) Math.min(samples,Math.ceil(maxFreq*filtered.length())+1);
        double[][] freqValues = filtered.freqValues(0, filtered.length(), samples);
        double[] freqAmplitudes = Converter.flipArrayDimensions(Converter.polarCoordinates(freqValues))[0];
        for (int i = indexStart; i < indexEnd; i++) {
            rotationalContributions+=freqAmplitudes[i]*i/filtered.length();
            totalEnergy+=freqAmplitudes[i];
        }
        return rotationalContributions/totalEnergy;
    }
    public PitchSet narrowFreq(Sound sound, int samplesPerSecond) {
        Sound soundTrim = sound.trimEnd(end).trimStart(start);
        Sound filtered = Sounds.filterFrequencies(soundTrim, samplesPerSecond, minFreq, maxFreq);
        double freq = averageFreqByEnergy(filtered, samplesPerSecond);
        return new PitchSet(freq,freq,start,end, samplePeriod);
    }
    private static double[][] freqValuesPrint(Sound sound, double start, double end, int samples) {
        int channels = sound.channels();
        boolean[] realChannels = new boolean[channels];
        boolean[] imagChannels = new boolean[channels];
        for (int i = 0; i < channels; i++) {
            realChannels[i] = ((i % 2)==0);
            imagChannels[i] = ((i % 2)==1);
        }
        int size = 1;
        while (size<samples) size*=2;
        double[] real = sound.pressureValues(start, end, samples, realChannels);
        double[] imag = sound.pressureValues(start, end, samples, imagChannels);
        for (int i = 0; i < real.length; i++) {
            System.out.println(real[i]+" "+imag[i]);
        }
        System.exit(1);
        double[][] fft = Converter.fft(real,imag,true);
        return fft;
    }
    public List<Pitch> getSubPitchList(Sound sound) {
        double threshold = Math.sqrt(sound.energy(0,sound.length()));
        int samples = (int)Math.ceil(2*Math.PI*trueMaxFreq()*(trueEnd()-trueStart()));
        double[] pressureValues = sound.pressureValues(trueStart(), trueEnd(), samples);
        int freqSamples = (int) trueMaxFreq();
        int startFreqSample = (int) trueMinFreq();
        int trueFreqSamples = freqSamples - startFreqSample;
        double[][] cosValues = new double[trueFreqSamples][pressureValues.length];
        double[][] sinValues = new double[trueFreqSamples][pressureValues.length];
        double timeBetweenSamples = (trueEnd()-trueStart())/samples;
        for (int i = startFreqSample; i < freqSamples; i++) for (int j = 0; j < pressureValues.length; j++) {
            double angularFreq = 2*Math.PI*trueMaxFreq()*i/freqSamples;
            cosValues[i-startFreqSample][j] += Math.cos(j*angularFreq*timeBetweenSamples)*pressureValues[j];
            sinValues[i-startFreqSample][j] += Math.sin(j*angularFreq*timeBetweenSamples)*pressureValues[j];
        }
        List<Pitch> out = new ArrayList<Pitch>();
        double amplitude;
        double freq;
        do {
            freq = findFreqMode(sinValues, cosValues, startFreqSample);
            double modeIndex = modeIndex(freq, sinValues, cosValues)-startFreqSample;
            int index = (int)(Math.round(modeIndex));
            amplitude = amplitude(modeIndex, sinValues, cosValues);
            double[] interval = interval(0.9, sinValues[index], cosValues[index]);
            if (Math.sqrt(freq)*amplitude>threshold) out.add(new PitchFactory.freqTime(interval[0], interval[1], freq, amplitude));
            clearFreq(freq, interval, sinValues, cosValues,startFreqSample);
        } while (Math.sqrt(freq)*amplitude>threshold);
        return out;
    }
    private double findFreqMode(double[][] sinValues, double[][] cosValues, int startFreqSample) {
        double[] sumSinValues = new double[sinValues.length];
        double[] sumCosValues = new double[cosValues.length];
        double[] energyValues = new double[sinValues.length];
        for (int i = 0; i < energyValues.length; i++) for (int j = 0; j < sinValues[i].length; j++) {
            sumSinValues[i] += sinValues[i][j];
            sumCosValues[i] += cosValues[i][j];
        }
        for (int i = 0; i < energyValues.length; i++){
            energyValues[i] += sumSinValues[i]*sumSinValues[i]+sumCosValues[i]*sumCosValues[i];
        }
        int maxIndex = 0;
        for (int i = 0; i < energyValues.length; i++) {
            if (energyValues[i]>energyValues[maxIndex]) maxIndex = i;
        }
        return freqFromMode(maxIndex+startFreqSample,energyValues.length);
    }
    private double[] interval(double fracThreshold, double[] sinValues, double[] cosValues) {
        double sumSin = Arrays.stream(sinValues).sum();
        double sumCos = Arrays.stream(cosValues).sum();
        double sinEnergy = sumSin*sumSin;
        double cosEnergy = sumCos*sumCos;
        double totalEnergy = sinEnergy+cosEnergy;
        double fracSin = sinEnergy/totalEnergy;
        double fracCos = cosEnergy/totalEnergy;
        double startThreshold = (1-fracThreshold)/2;
        double endThreshold = 1-startThreshold;
        double runningContribution = 0.0;
        double startInterval = trueStart();
        double endInterval = trueStart();
        for (int i = 0; i < sinValues.length; i++) {
            if (fracSin>0) runningContribution+=fracSin*sinValues[i]/sumSin;
            if (fracCos>0) runningContribution+=fracCos*cosValues[i]/sumCos;
            if (runningContribution<startThreshold) startInterval = trueStart() + i*(trueEnd()-trueStart())/sinValues.length;
            if (runningContribution<endThreshold) endInterval = trueStart() + i*(trueEnd()-trueStart())/sinValues.length;
        }
        return new double[]{startInterval,endInterval};
    }
    private void clearFreq(double freq, double[] interval, double[][] sinValues, double[][] cosValues, int startFreqSample) {
        double modeIndex = modeIndex(freq,sinValues,cosValues)-startFreqSample;
        double amplitude = amplitude(modeIndex,sinValues,cosValues);
        // Todo 
        // Screening from pitch falls off inversely proportional to the distance to the pitch
        // = sin(pi*dif) / pi*dif
        // Where dif is the distance between the mode and the tested freq,in terms of multiples of the inverse of the elapsed time.
        for (int i = 0; i < sinValues.length; i++) {
            double freqDif = Math.abs(freqFromMode(i+startFreqSample,sinValues.length)-freq);
            double invMultipleDif = (interval[1]-interval[0])*freqDif;
            double ampMask = 1.5*amplitude/invMultipleDif;
            double hypot = Math.hypot(Arrays.stream(sinValues[i]).sum(),Arrays.stream(cosValues[i]).sum());
            for (int j = 0; j < sinValues[i].length; j++) {
                double difStart = (j * (trueEnd()-trueStart()) / sinValues[i].length) + trueStart() - interval[0];
                double difEnd = (j * (trueEnd()-trueStart()) / sinValues[i].length) + trueStart() - interval[1];
                double difFromInterval = Math.max(0,Math.max(-difStart, difEnd));
                double fracDifFromInterval = difFromInterval / (interval[1]-interval[0]);
                double ampTimeMask = ampMask / (1+fracDifFromInterval);
                if (hypot<=ampTimeMask) {
                    sinValues[i][j] = 0;
                    cosValues[i][j] = 0;
                } else {
                    sinValues[i][j] *= (hypot-ampMask)/hypot;
                    cosValues[i][j] *= (hypot-ampMask)/hypot;
                }
            }
        }
    }
    private double freqFromMode(double modeIndex, int length) {
        return ((trueMaxFreq()-trueMinFreq())*modeIndex)/length;
    }
    private double amplitude(double modeIndex, double[][] sinValues, double[][] cosValues) {
        int floor = Math.max((int) modeIndex,0);
        int ceil = Math.min(1+((int) modeIndex),sinValues.length-1);
        double sinFloorSum = Arrays.stream(sinValues[floor]).sum();
        double sinCeilSum = Arrays.stream(sinValues[ceil]).sum();
        double cosFloorSum = Arrays.stream(cosValues[floor]).sum();
        double cosCeilSum = Arrays.stream(cosValues[ceil]).sum();
        return Math.sqrt(sumSquare(sinFloorSum,sinCeilSum,cosFloorSum,cosCeilSum));
    }
    private double sumSquare(double a, double b, double c, double d) {
        return a*a+b*b+c*c+d*d;
    }
    private double modeIndex(double freq, double[][] sinValues, double[][] cosValues) {
        return cosValues.length*freq/(trueMaxFreq()-trueMinFreq());
    }
    private double trueMaxFreq() {
        return maxFreq+10;
    }
    private double trueMinFreq() {
        double out = (minFreq-10);
        if (out<0) out=0;
        return out;
    }
    private double trueStart() {
        return Math.max(start - samplePeriod,0);
    }
    private double trueEnd() {
        return end+samplePeriod;
    }
    @Override
    public int compareTo(PitchSet o) {
       if (start < o.start) return -1;
       else if (start > o.start) return 1;
       else if (end < o.end) return -1;
       else if (end > o.end) return 1;
       else if (minFreq < o.minFreq) return -1;
       else if (minFreq > o.minFreq) return 1;
       else if (maxFreq < o.maxFreq) return -1;
       else if (maxFreq > o.maxFreq) return 1;
       else return 0;
    }
}