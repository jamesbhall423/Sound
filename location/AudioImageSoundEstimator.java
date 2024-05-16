package location;

import analysis.AudioImageConverter;
import analysis.Sound;

public class AudioImageSoundEstimator<T extends SoundLocation<T>> extends SoundAtLocationEstimator<T> {
    private AudioImageConverter converter;
    public AudioImageSoundEstimator(AudioImageConverter converter, double freqFracSmear) {
        super(converter.samplingInterval(), freqFracSmear, converter.samplingResolution());
        this.converter = converter;
    }
    @Override
    public Sound estimateSoundAtLocation(SoundRecording<T>[] recordings, T testLocation) {
        this.testLocation = testLocation;
        mainRecording = getMainSoundRecording(recordings);
        otherRecordings = getOtherSoundRecordings(mainRecording, recordings);
        double[][][] mainTimeFreqPhaseImage = converter.getRawTimeFreqPhaseImage(mainRecording.sound.trimStart(mainRecording.location.soundTraversalTime(testLocation)));
        double[][] mainMarks = converter.cleanTimeFreqPhaseImage(mainTimeFreqPhaseImage);

        SoundRecording<T> otherRecording = getMainSoundRecording(otherRecordings);
        double[][][] otherTimeFreqPhaseImage = converter.getRawTimeFreqPhaseImage(otherRecording.sound.trimStart(otherRecording.location.soundTraversalTime(testLocation)));
        converter.cleanTimeFreqPhaseImage(otherTimeFreqPhaseImage);
        double[][] otherTimeFreqImage = converter.smear(converter.timeFreqAmpValues(otherTimeFreqPhaseImage), freqFracSmear);
        double distanceRatio = otherRecording.location.soundTraversalTime(testLocation) / mainRecording.location.soundTraversalTime(testLocation);
        double[][][] outTimeFreqPhaseImage = converter.getConsistentTimeFreqPhaseImage(mainTimeFreqPhaseImage, otherTimeFreqImage, distanceRatio);
        return converter.getSoundFromCleanedImage(outTimeFreqPhaseImage, mainMarks);
        
    }
}
