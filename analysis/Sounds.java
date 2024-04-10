package analysis;

public class Sounds {
    public static final Sound filterFrequencies(Sound in, int samplesPerSecond, double minFreq, double maxFreq) {
        minFreq-=2.5/in.length();
        maxFreq+=2.5/in.length();
        int samples = Converter.power2Higher(in.length()*samplesPerSecond);
        double[][] freqValues = in.freqPhaseValues(0, in.length(), samples);
        for (int i = 0; i < freqValues.length; i++) if (i/in.length()<minFreq||i/in.length()>maxFreq) {
            freqValues[i][0] = 0;
            freqValues[i][1] = 0;
        }
        return in.getSound(freqValues, in.length());
    }
}
