package localization;

import analysis.Converter;
import analysis.Sound;

public class SoundTimeDifference {
    private static final double INTERVAL = 0.05;
    private static final int SAMPLES = 64;
    public static Sound compareTiming(Sound soundA, Sound soundB) {
        double[][] polarA = polarFreq(soundA);
        double[][] polarB = polarFreq(soundB);
        double[][] polarC = new double[polarA.length][2];
        for (int i = 0; i < polarC.length; i++) {
            polarC[i][0] = Math.min(polarA[i][0],polarB[i][0]);
            polarC[i][1] = polarA[i][1]-polarB[i][1];
        }
        return soundA.getSound(Converter.euclidianCoordinates(polarC), INTERVAL);
    }
    private static double[][] polarFreq(Sound sound) {
        double[][] freq = sound.freqPhaseValues(0, INTERVAL, SAMPLES);
        return Converter.polarCoordinates(freq);
    }
}
