package localization;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import analysis.Converter;
import analysis.Sound;

public class PitchFactory implements SoundPartFactory  {
    public static final double STANDARD_INVERVAL = 0.05;
    public static final double STANDARD_MAX_FREQ = 20480;
    
    public static class freqTime extends Pitch implements Comparable<Pitch> {
        public freqTime(double startFreq, double endFreq, double frequency, double amplitude) {
            this.startFreq=startFreq;
            this.endFreq=endFreq;
            this.frequency=frequency;
            this.amplitude=new double[]{amplitude,0};
        }
        
        @Override
        public Sound getShiftedByPhase(double freq, double startOffset) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getShiftedByPhase'");
        }
        @Override
        public String toString() {
            return String.format("%,.2f", startFreq)+"s-"+String.format("%,.2f", endFreq)+"s "+String.format("%,.2f", amplitude[0])+" x "+String.format("%,.0f", frequency)+"hz";
        }

        @Override
        public CorrelDetails startingCorrelDetails() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'startingCorrelDetails'");
        }
        @Override
        public int channels() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'channels'");
        }
        @Override
        public double[] pressureValues(double start, double end, int samples, boolean[] channels) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'pressureValues'");
        }
        @Override
        public int compareTo(Pitch o) {
            if (frequency < o.frequency) return -1;
            else if (frequency > o.frequency) return 1;
            else if (startFreq < o.startFreq) return -1;
            else if (startFreq > o.startFreq) return 1;
            else if (endFreq < o.endFreq) return -1;
            else if (endFreq > o.endFreq) return 1;
            else return 0;
        }

        @Override
        public Sound scaleVolume(double scale) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'scaleVolume'");
        }

        @Override
        public Sound getSound(double[][][] timeFreqPhaseValues, double length) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getSound'");
        }
    }
    @Override
    public SoundPart CreateSoundPart(Sound sound) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'CreateSoundPart'");
    }
    public static List<Pitch> soundBreakdown(Sound sound, double interval, int numFrequencies) {
        double threshold = 1*Math.sqrt(sound.energy(0,sound.length()));
        double[][][] freqTimeValues = new double[(int)(Math.floor(sound.length()/interval))][2*numFrequencies][2];
        int index = 0;
        List<Pitch> out = new ArrayList<Pitch>();
        for (double start = 0; start+interval < sound.length(); start+=interval) {
            double[][] freqValues = sound.freqPhaseValues(start, start+interval, 2*numFrequencies);
            freqTimeValues[index]=freqValues;
            index++;
        }
        for (int i = 0; i < freqTimeValues.length; i++) for (int j = 0; j < numFrequencies; j++) {
            double start = i*interval;
            double freq = j/interval;
            double ampSum = 0.0;
            int length;
            for (length=0; i+length<freqTimeValues.length&&Math.hypot(freqTimeValues[i+length][j][0], freqTimeValues[i+length][j][1])*Math.sqrt(freq)>threshold;length++) {
                ampSum += Math.hypot(freqTimeValues[i+length][j][0], freqTimeValues[i+length][j][1]);
                freqTimeValues[i+length][j][0]=0;
                freqTimeValues[i+length][j][1]=0;
            }
            double end = (i+length)*interval;
            double amplitude = ampSum/length;
            if (length>0) out.add(new freqTime(start, end, freq, amplitude));
        }
        return out;
    }
    public static SortedSet<Pitch> preciseSoundBreakdown(Sound in, double start, double time, double interval) {
        Sound sound = in.trimStart(start).trimEnd(time);
        List<Pitch> firstPass = soundBreakdown(sound,interval,Converter.power2Higher(interval*STANDARD_MAX_FREQ));
        Set<PitchSet> sets = PitchSet.sortIntoSets(firstPass, 1.2/interval, interval);
        SortedSet<Pitch> out = new TreeSet<>();
        for (PitchSet set: sets) {
            List<Pitch> subPitches = set.getSubPitchList(sound);
            for (Pitch next: subPitches) {
                out.add(next);
            }
        }
        return out;
    }
}
