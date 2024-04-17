package analysis;

import javax.sound.sampled.*;

public class SoundOutput {
    public static void playSound(Sound in) throws LineUnavailableException {
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
        int numSamples = (int) (format.getSampleRate()*in.length());
        double[][] pressureValues = new double[format.getChannels()][numSamples];
        if (format.getChannels()==1) {
            pressureValues[0] = in.pressureValues(0, in.length(), numSamples);
        } else if (in.channels()==1) {
            pressureValues[0] = in.pressureValues(0, in.length(), numSamples);
            pressureValues[1] = in.pressureValues(0, in.length(), numSamples);
        } else {
            pressureValues[0] = in.pressureValuesByChannel(0, in.length(), numSamples,0);
            pressureValues[1] = in.pressureValuesByChannel(0, in.length(), numSamples,1);
        }
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
                    bytes[sampleIndex] = (byte) big;
                    bytes[sampleIndex+1] = (byte) little;
                } else {
                    bytes[sampleIndex] = (byte) little;
                    bytes[sampleIndex+1] = (byte) big;
                }
            }
        }
        output.open(format);
        output.start();
        int written = 0;
        while (written < bytes.length) {
            written += output.write(bytes,written,bytes.length-written);
        }
        output.drain();
        
        output.close();
    }
}
