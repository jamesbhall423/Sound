package analysis;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;
public class SoundInput {
    private AudioFormat format;
    public byte[] samples;
    /**
     * 
     * @param filename le lien vers le fichier song (URL ou absolute path)
     */
    public SoundInput(String filename) {
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(new File(filename));
            format = stream.getFormat();
            samples = getSamples(stream);
        }
        catch (UnsupportedAudioFileException e){e.printStackTrace();}
        catch (IOException e){e.printStackTrace();}
    }
    
    public byte[] getSamples(AudioInputStream stream) {
        int length = (int)(stream.getFrameLength() * format.getFrameSize());
        byte[] samples = new byte[length];
        DataInputStream in = new DataInputStream(stream);
        try {
            in.readFully(samples);
        }
        catch (IOException e){e.printStackTrace();}
        return samples;
    }
    public void printFormat() {
        System.out.println(format);
    }
    public Sound getSound() {
        if (!(format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)||format.getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED))) {
            throw new UnsupportedOperationException(format.getEncoding()+" is not supported.");
        }
        long[][] out = getLongValues();
        return new BaseSound(out,format.getFrameRate());
    }
    public long[][] getLongValues() {
        int sampleByteSize = format.getSampleSizeInBits()/8;
        long[][] out = new long[samples.length/format.getFrameSize()][format.getChannels()];
        for (int sample = 0; sample < out.length; sample++) {
            for (int channel = 0; channel < format.getChannels(); channel++) {
                if (format.isBigEndian()) for (int bite = 0; bite < sampleByteSize; bite++) {
                    out[sample][channel] += getByte(sample,channel,sampleByteSize-bite-1) << (8l*bite);
                } else for (int bite = 0; bite < sampleByteSize; bite++) {
                    out[sample][channel] += getByte(sample,channel,bite) << (8l*bite);
                }
                if (format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)&&out[sample][channel]>=(1l<<(format.getSampleSizeInBits()-1))) {
                    out[sample][channel] -= 1l<<(format.getSampleSizeInBits());
                }
            }
        }
        return out;
    }
    private int getByte(int sample,int channel,int bite) {
        int out = samples[sample*format.getFrameSize()+channel*format.getChannels()+bite];
        if (out<0) out+=256;
        return out;
    }
    public void printByteSample(int numsamples) {
        for (int sample = 0; sample < numsamples; sample++) {
            int basenum = sample*4;
            System.out.println(samples[basenum]+" "+samples[basenum+1]+" "+samples[basenum+2]+" "+samples[basenum+3]);
        }
    }
}
