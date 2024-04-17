import analysis.Converter;
import analysis.DisplayFrame;
import analysis.Sound;
import analysis.SoundInput;
import analysis.SoundOutput;
import location.Location3D;
import location.SoundAtLocationEstimator;
import location.SoundRecording;
import analysis.AudioImage;
import analysis.BaseSound;

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
