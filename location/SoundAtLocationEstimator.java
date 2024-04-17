package location;

import java.util.ArrayList;
import java.util.List;

import analysis.Sound;

public class SoundAtLocationEstimator <T extends SoundLocation<T>> {
    public static final SoundAtLocationEstimator<Location3D> STANDARD_INSTANCE = new SoundAtLocationEstimator<>(0.05, 0.01, 2048);
    public SoundAtLocationEstimator<T> newInstance() {
        return new SoundAtLocationEstimator<>(samplingInterval, freqFracSmear, samplesPerInterval);
    }
    private double samplingInterval;
    private double freqFracSmear;
    private int samplesPerInterval;

    // For use during calculation
    private SoundRecording<T> mainRecording;
    private List<SoundRecording<T>> otherRecordings;
    private T testLocation;
    public SoundAtLocationEstimator(double samplingInverval, double freqFracSmear, int samplesPerInterval) {
        this.samplingInterval = samplingInverval;
        this.freqFracSmear = freqFracSmear;
        this.samplesPerInterval = samplesPerInterval;
    }
    // Requires user to sync recording timings and amplitudes prior to use.
    public Sound estimateSoundAtLocation(SoundRecording<T>[] recordings, T testLocation) {
        this.testLocation = testLocation;
        mainRecording = getMainSoundRecording(recordings);
        otherRecordings = getOtherSoundRecordings(mainRecording, recordings);
        int intervals = (int)(mainRecording.sound.length() / samplingInterval);
        double[][][] mainTimeFreqPhaseImage = getTimeFreqPhaseImage(mainRecording,intervals);
        double[][] otherTimeFreqImage = new double[intervals][samplesPerInterval];
        double weightSum = getWeightSum(mainRecording, otherRecordings);
        for (int i = 0; i < otherRecordings.size(); i++) smear(getTimeFreqPhaseImage(otherRecordings.get(i),intervals),otherTimeFreqImage,weightSum,otherRecordings.get(i));
        for (int i = 0; i < mainTimeFreqPhaseImage.length; i++) for (int j = 0; j < mainTimeFreqPhaseImage[0].length; j++) {
            double magnitudeRatio = magnitude(mainTimeFreqPhaseImage[i][j], 1) / otherTimeFreqImage[i][j];
            if (magnitudeRatio>1) {
                mainTimeFreqPhaseImage[i][j][0] /= magnitudeRatio;
                mainTimeFreqPhaseImage[i][j][1] /= magnitudeRatio;
            }
        }
        return mainRecording.sound.getSound(mainTimeFreqPhaseImage, mainRecording.sound.length());
    }
    private double getWeight(SoundRecording<T> testRecording) {
        double distanceToLocation = testRecording.location.soundTraversalTime(testLocation);
        return distanceToLocation*distanceToLocation;
    }
    private double getWeightSum(SoundRecording<T> mainRecording, List<SoundRecording<T>> otherRecordings) {
        double sum = 0.0;
        for (SoundRecording<T> testRecording: otherRecordings) sum += getWeight(testRecording);
        return sum;
    }
    private void smear(double[][][] timeFreqPhaseImage, double[][] combinedOtherImage, double weightSum, SoundRecording<T> recording) {
        for (int interval = 0; interval < combinedOtherImage.length; interval++) for (int freqLevel = 0; freqLevel < combinedOtherImage[0].length; freqLevel++) {
            combinedOtherImage[interval][freqLevel] = getWeight(recording)*getSmearedValue(timeFreqPhaseImage[interval],freqLevel);
        }
    }
    private double getSmearedValue(double[][] freqPhaseValues, int freqLevel) {
        int absFreq = Math.min(freqLevel,freqPhaseValues.length-freqLevel)+1;
        double freqVar = freqFracSmear*freqLevel;
        int freqVarFloor = (int) freqVar;
        double freqVarExcess = freqVar - freqVarFloor;
        double max = 0.0;
        for (int i = absFreq-freqVarFloor; i < absFreq+freqVarFloor; i++) {
            max = getMagnitudeIfGreater(i,freqPhaseValues,max,1);
        }
        max = getMagnitudeIfGreater(absFreq-freqVarFloor-1, freqPhaseValues, max,freqVarExcess);
        max = getMagnitudeIfGreater(absFreq+freqVarFloor+1, freqPhaseValues, max,freqVarExcess);
        return max;
    }
    private double getMagnitudeIfGreater(int index, double[][] aray, double max, double scale) {
        if (magnitudeGreater(index, aray, max, scale)) return magnitude(aray[index],scale);
        else return max;
    }
    private boolean magnitudeGreater(int index, double[][] aray, double compare, double scale) {
        return valueInArray(index, aray) && magnitude(aray[index],scale)>compare;
    }
    private double magnitude(double[] aray, double scale) {
        return scale*Math.hypot(aray[0],aray[1]);
    }
    private boolean valueInArray(int index, double[][] aray) {
        return index>0 && index<aray.length;
    }
    public double[][][] getTimeFreqPhaseImage(SoundRecording<T> recording, int intervals) {
        double[][][] out = new double[intervals][][];
        for (int i = 0; i < out.length; i++) {
            double intervalStart = samplingInterval*i+recording.location.soundTraversalTime(testLocation);
            out[i] = recording.sound.freqPhaseValues(intervalStart, intervalStart+samplingInterval, samplesPerInterval);
        }
        return out;
    }
    private void normalizeVolumeBasedOnDistance(SoundRecording<T> mainRecording, List<SoundRecording<T>> otherRecordings) {
        for (SoundRecording<T> recording: otherRecordings) recording.sound = recording.sound.scaleVolume(recording.location.soundTraversalTime(testLocation)/mainRecording.location.soundTraversalTime(testLocation));
    }

    private List<SoundRecording<T>> getOtherSoundRecordings(SoundRecording<T> mainRecording, SoundRecording<T>[] recordings) {
        List<SoundRecording<T>> out = new ArrayList<>(recordings.length-1);
        for (SoundRecording<T> recording: recordings) {
            if (recording!=mainRecording) out.add(recording);
        }
        normalizeVolumeBasedOnDistance(mainRecording, out);
        return out;
    }

    private SoundRecording<T> getMainSoundRecording(SoundRecording<T>[] recordings) {
        double minimumDistance = getMinimumDistance(recordings, testLocation);
        for (SoundRecording<T> recording: recordings) {
            if (recording.location.soundTraversalTime(testLocation) == minimumDistance) return recording;
        }
        assert false;
        return null;
    }

    private double getMinimumDistance(SoundRecording<T>[] recordings, T testLocation) {
        double min = recordings[0].location.soundTraversalTime(testLocation);
        for (SoundRecording<T> recording: recordings) {
            if (recording.location.soundTraversalTime(testLocation)<min) min = recording.location.soundTraversalTime(testLocation);
        }
        return min;
    }
    
}
