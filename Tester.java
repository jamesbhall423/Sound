import analysis.Converter;
import analysis.DisplayFrame;
import analysis.Sound;
import analysis.SoundInput;
import analysis.SoundOutput;
import analysis.Sounds;
import location.Location3D;
import location.SoundAtLocationEstimator;
import location.SoundRecording;
import analysis.AudioImage;
import analysis.AudioImageConverter;
import analysis.BaseSound;
import analysis.ClippedSound;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
public class Tester {
    private static final double PHONE_DELAY = 5.095;//5.095;
    private static final double START = 1.5;
    private static final double END = 1.5;
    private static final String PHONE_RECORDING = "Location Experiment Phone.wav";
    private static final String LAPTOP_RECORDING = "Localization Experiment Recording Laptop.wav";
    public static void main(String[] args) throws LineUnavailableException, UnsupportedAudioFileException, IOException {
        SoundInput input = new SoundInput("Music.wav");
        Sound sound1 = input.getSound().trimEnd(6);
        AudioImageConverter converter = new AudioImageConverter();
        double[][][] inter = converter.getRawTimeFreqPhaseImage(sound1);
        double[][] cleans = converter.cleanTimeFreqPhaseImage(inter);
        Sound sound2 = converter.getSoundFromCleanedImage(inter, cleans);
        // //Sound sound2 = converter.getSoundFromRawTimeFreqPhaseImage(converter.getRawTimeFreqPhaseImage(sound1));
        sound1 = sound1.trimStart(0.02);
        System.out.println(sound1.energy(1,2));
        System.out.println(sound2.energy(1,2));
        // new DisplayFrame(new AudioImage(sound1),"music1");
        // new DisplayFrame(new AudioImage(sound2),"music2");
        double[] soundThresholds = new double[] {0,200,500,1000,2000,5000,10000,20000};
        testSoundCors(sound1,sound2,0,5,200000,1024,0.05, soundThresholds);
        // sound1.play();
        // sound2.play();
    }
    private static void testSoundCors(Sound sound1, Sound sound2, double start, double end, int samplesP, int samplesF, double fInterval, double[] soundThresholds) {
        for (int i = 0; i+1 < soundThresholds.length; i++) {
            System.out.println(soundThresholds[i]+" "+soundThresholds[i+1]);
            Sound s1 = Sounds.filterFrequencies(sound1, 44100, soundThresholds[i], soundThresholds[i+1]);
            Sound s2 = Sounds.filterFrequencies(sound2, 44100, soundThresholds[i], soundThresholds[i+1]);
            System.out.println(s1.energy(start,end));
            System.out.println(s2.energy(start,end));
            double[][] pressureValues1 = new double[][] {s1.pressureValuesByChannel(start, end, samplesP, 0),s1.pressureValuesByChannel(start, end, samplesP, 1)};
            double[][] pressureValues2 = new double[][] {s2.pressureValuesByChannel(start, end, samplesP, 0),s2.pressureValuesByChannel(start, end, samplesP, 1)};
            System.out.println(testCorrelation(pressureValues1, pressureValues2));
            double sumCor = 0.0;
            int count = 0;
            for (double s = start; s < end-fInterval; s+=fInterval) {
                double[][] fpv1 = s1.freqPhaseValues(s, s+fInterval, samplesF);
                double[][] fpv2 = s2.freqPhaseValues(s, s+fInterval, samplesF);
                count++;
                sumCor+=testCorrelation(fpv1, fpv2);
            }
            System.out.println(sumCor/count);
        }
        
    }
    private static double testCorrelation(double[][] input1, double[][] input2) {
        double sumCor = 0.0;
        double sumSq1 = 0.0;
        double sumSq2 = 0.0;
        for (int i = 0; i < input1.length; i++) for (int j = 0; j < input1[i].length; j++) {// if (!Double.isNaN(input1[i][j])&&!Double.isNaN(input2[i][j])) {
            sumCor += input1[i][j]*input2[i][j];
            sumSq1 += input1[i][j]*input1[i][j];
            sumSq2 += input2[i][j]*input2[i][j];
        }
        return (sumCor*sumCor)/(sumSq1*sumSq2+0.00000001);
    }
    private static void testExperiment() {
        
    }
    private static void testLocalization() {
        Sound phoneSound = new SoundInput(PHONE_RECORDING).getSound().trimStart(PHONE_DELAY+START).trimEnd(END);
        Sound laptopSound = new SoundInput(LAPTOP_RECORDING).getSound().trimStart(START).trimEnd(END).scaleVolume(3);
        System.out.println(phoneSound.energy(0,END));
        System.out.println(laptopSound.energy(0,END));
        SoundRecording<Location3D> phoneRecording = new SoundRecording<>(phoneSound, new Location3D(0, 0, -1));
        SoundRecording<Location3D> laptopRecording = new SoundRecording<>(laptopSound, new Location3D(0, 0, 1));
        @SuppressWarnings("unchecked")
        SoundRecording<Location3D>[] recordings = new SoundRecording[] {laptopRecording,phoneRecording};
        for (double dif = -0.1; dif < 0.1; dif += 0.01) {
            Sound out = SoundAtLocationEstimator.STANDARD_INSTANCE.estimateSoundAtLocation(recordings, new Location3D(0, 0, dif));
            //new DisplayFrame(new AudioImage(out),"Combined Experiment");
            out.play();
            System.out.println(dif);
        }
    }
    private static void testFilePlayBack() throws LineUnavailableException, UnsupportedAudioFileException, IOException {
        SourceDataLine output;
        try {
           output = AudioSystem.getSourceDataLine(new AudioFormat(44100, 16, 2, true,false));
        } catch (Exception e) {
            output = AudioSystem.getSourceDataLine(null);
        }
        AudioFormat format = output.getFormat();
        if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED || format.getSampleSizeInBits()!=16 || format.getChannels() == AudioSystem.NOT_SPECIFIED) {
            throw new UnsupportedOperationException("Requires 16 bit sample PCM_SIGNED (mono/stereo) audio: only could access format: "+format);
        }
        SoundInput input = new SoundInput("Music.wav");
        byte[] bytes = input.samples;
        System.out.println(format);
        System.out.println( AudioSystem.getAudioInputStream(new File("Music.wav")).getFormat());
        output.open(format);
        output.start();
        int written = 0;
        while (written < bytes.length) {
            written += output.write(bytes,written,bytes.length-written);
        }
        output.drain();
        
        output.close();
    }
    private static byte[] outputBytes(AudioFormat format, long[][] pressureValues) {
        byte[] bytes = new byte[2*pressureValues[0].length*format.getChannels()];
        //System.out.println(bytes.length+" "+pressureValues[0].length+" "+format.getChannels());
        for (int frame = 0; frame < pressureValues[0].length; frame++) {
            int frameIndex = frame*format.getFrameSize();
            for (int sample = 0; sample < format.getChannels(); sample++) {
                int sampleIndex = frameIndex + 2*sample;
                short integerPressure = (short) pressureValues[sample][frame];
                int big = (integerPressure>>8)&((1<<8)-1);
                int little = integerPressure&((1<<8)-1);
                if (format.isBigEndian()) {
                    bytes[sampleIndex] = (byte) little;
                    bytes[sampleIndex+1] = (byte) big;
                } else {
                    bytes[sampleIndex] = (byte) big;
                    bytes[sampleIndex+1] = (byte) little;
                }
            }
        }
        return bytes;
    }
}
