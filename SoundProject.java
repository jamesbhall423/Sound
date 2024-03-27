import analysis.SoundInput;
import localization.Pitch;
import localization.PitchCompare;
import localization.PitchFactory;
import localization.PitchSet;
import localization.SoundTimeDifference;
import analysis.Sound;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import analysis.AudioImage;
import analysis.BaseSound;
import analysis.ClippedSound;
import analysis.Converter;
import analysis.DisplayFrame;
import analysis.Sounds;
import analysis.FrequencyGradientPanel;
import analysis.TimeGradientPanel;

public class SoundProject {
    // base phone start: 5.09, phone thresh 4000, lap thresh 100000
    private static final double phone2290s_540hz_bump_start = Double.NaN;
    private static final double phone2310s_540hz_bump_end = Double.NaN;
    private static final double lap2290s_540hz_bump_start = 0.02;
    private static final double lap2310s_540hz_bump_end = 0.02;

    // base phone start: 5.09, phone thresh 10000, lap thresh 10000
    private static final double phone2s_540hz_bump_start = Double.NaN;
    private static final double phone2s_540hz_bump_end = Double.NaN;
    private static final double lap2s_540hz_bump_start = Double.NaN;
    private static final double lap2s_540hz_bump_end = Double.NaN;


    private static final int samplesPerSecond = 41000;
    private static final String PHONE_RECORDING = "C:\\Users\\james\\Sound\\Location Experiment Phone.wav";
    private static final String LAPTOP_RECORDING = "C:\\Users\\james\\Sound\\Localization Experiment Recording Laptop.wav";
    public static void main(String[] args) {
        double bump = 2;
        double end = 22;
        Sound sound1 = displayFreq("C:\\Users\\james\\Sound\\Piano A440.wav",0,10);
        // SortedSet<Pitch> list1 = PitchFactory.preciseSoundBreakdown(sound1, 3,3,0.05);
        // for (Pitch next: list1) {
        //   System.out.println(next);
        // }
        // System.out.println();
        // List<Pitch> pitchlist1 = PitchFactory.soundBreakdown(sound1.trimStart(3).trimEnd(3), 0.05, 1024);
        // for (Pitch next: pitchlist1) System.out.println(next);
        // System.out.println("next");
        // SortedSet<Pitch> list2 = PitchFactory.preciseSoundBreakdown(sound1, 3.0,0.5,0.01);
        // for (Pitch next: list2) {
        //   System.out.println(next);
        // }
        // System.out.println("next");
        // SortedSet<Pitch> list3 = PitchFactory.preciseSoundBreakdown(sound1, 5.4,0.5,0.01);
        // for (Pitch next: list3) {
        //   System.out.println(next);
        // }
        // System.out.println("next");
        // SortedSet<Pitch> list4 = PitchFactory.preciseSoundBreakdown(sound2, 0.6,0.5,0.01);
        // for (Pitch next: list4) {
        //   System.out.println(next);
        // }
        // System.out.println("next");
        // SortedSet<Pitch> list5 = PitchFactory.preciseSoundBreakdown(sound1, 4.1,0.5,0.01);
        // for (Pitch next: list5) {
        //   System.out.println(next);
        // }
        //Sound sound1 = displayFreq(PHONE_RECORDING,5.095+bump,end);
        // Sound sound2 = displayFreq(LAPTOP_RECORDING,bump,end);
        // double[] startThresholds = new double[]{0.0,10.0,20.0};
        // double start1 = 18.0;
        // double end1 = 19.0;
        // double sound1Offset = 0.03;
        // for (int i = 0; i < startThresholds.length; i++) {
        //     System.out.println();
        //     System.out.println("Time = "+startThresholds[i]);
        //     System.out.println();
        //     System.out.println("Phone");
        //     SortedSet<Pitch> list1 = PitchFactory.preciseSoundBreakdown(sound1, startThresholds[i], 2);
        //     SortedSet<Pitch> list2 = PitchFactory.preciseSoundBreakdown(sound2, startThresholds[i], 2);
        //     for (Pitch next: list1) {
        //         System.out.println(next);
        //     }
        //     System.out.println("Laptop");
        //     for (Pitch next: list2) {
        //         System.out.println(next);
        //     }
        //     System.out.println("Compare");
        //     for (PitchCompare next: PitchCompare.compare(list1, list2, 0.1)) System.out.println(next);

        //     // List<Pitch> pitchlist1 = PitchFactory.soundBreakdown(sound1.trimStart(startThresholds[i]).trimEnd(2), 0.05, 1024);
        //     // //for (Pitch next: pitchlist1) System.out.println(next);
        //     // Set<PitchSet> sets1 = PitchSet.sortIntoSets(pitchlist1,25,0.05);
        //     // for (PitchSet set: sets1) {
        //     //     System.out.println(set+" "+set.narrowFreq(sound1.trimStart(startThresholds[i]).trimEnd(2),samplesPerSecond));
        //     //     List<Pitch> subPitches = set.getSubPitchList(sound1.trimStart(startThresholds[i]).trimEnd(2));
        //     //     for (Pitch pitch: subPitches) {
        //     //         System.out.println(pitch);
        //     //     }
        //     //     System.out.println();
        //     // }
        //     // System.out.println();
        //     // System.out.println("Laptop");
        //     // System.out.println(Math.sqrt(sound2.trimStart(startThresholds[i]).energy(0,2)));
        //     // List<Pitch> pitchlist2 = PitchFactory.soundBreakdown(sound2.trimStart(startThresholds[i]).trimEnd(2), 0.05, 1024);
        //     // //for (Pitch next: pitchlist2) System.out.println(next);
        //     // Set<PitchSet> sets2 = PitchSet.sortIntoSets(pitchlist2,25,0.05);
        //     // for (PitchSet set: sets2) {
        //     //     System.out.println(set+" "+set.narrowFreq(sound2.trimStart(startThresholds[i]).trimEnd(2),samplesPerSecond));
        //     //     List<Pitch> subPitches = set.getSubPitchList(sound2.trimStart(startThresholds[i]).trimEnd(2));
        //     //     for (Pitch pitch: subPitches) {
        //     //         System.out.println(pitch);
        //     //     }
        //     //     System.out.println();
        //     // }
        // }
        
        //  double[][] timeDiff = timeDiffArray(sound1, sound2);
         // new DisplayFrame(new AudioImage(Sounds.filterFrequencies(sound1, 2048, 100, 300).pressureValues(start1+sound1Offset, end1+sound1Offset, 1000),250));
         // new DisplayFrame(new AudioImage(sound2.pressureValues(start1, end1, 1000),250));
        //  new DisplayFrame(new AudioImage(timeDiff));
        // int freq = 363;
        // for (int i = -20; i < 20; i++) System.out.println(i+" "+Converter.polarPoint(sound1.amplitude(start1, end1, freq+i))[0]);
        // System.out.println();
        // for (int i = -20; i < 20; i++) System.out.println(i+" "+Converter.polarPoint(sound2.amplitude(start1, end1, freq+i))[0]);
        // System.out.println();
        // for (int i = 0; i < 20; i++) System.out.println(i+" "+Converter.polarPoint(sound1.amplitude(start1+i*0.01, end1+i*0.01, freq))[0]);
        // System.out.println();
        // for (int i = 0; i < 20; i++) System.out.println(i+" "+Converter.polarPoint(sound2.amplitude(start1+i*0.01, end1+i*0.01, freq))[0]);
    }
    private static Sound displayFreq(String filename, double start, double end) {
        SoundInput input = new SoundInput(filename);
        Sound sound = input.getSound().trimEnd(end).trimStart(start);
        new DisplayFrame(new AudioImage(sound),filename);
        //new DisplayFrame(new AudioImage(sound,0.05,512));
        return sound;
    }
    public static double[][] timeDiffArray(Sound sound1, Sound sound2) {
        double[][] out = new double[(int) Math.min(Math.round(20*sound1.length()),Math.round(20*sound2.length()))][];
        for (int i = 0; i < out.length; i++) {
            out[i] = SoundTimeDifference.compareTiming(sound1.trimStart(0.05*i), sound2.trimStart(0.05*i)).pressureValues(0, 0.05, 128);
            int maxIndex = 0;
            for (int j = 0; j < out[i].length; j++) {
                if (out[i][j] > out[i][maxIndex]) {
                    maxIndex = j;
                }
            }
            try {
                if (out[i][maxIndex]>1000) {

                    out[i][maxIndex-1] += 400000;
                    out[i][maxIndex] += 800000;
                    out[i][maxIndex+1] += 400000;
                }
            } catch (ArrayIndexOutOfBoundsException e) {

            }
        }
        return out;
    }
}
