package analysis;

import java.awt.Color;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;
import java.util.DoubleSummaryStatistics;

public class AudioImage implements ImageProducer {
    private int[] image;
    private ColorModel colorModel;
    private int width;
    private int height;
    private double freqResolution;
    private double timeResolution;
    private Set<ImageConsumer> consumers = new HashSet<>();
    public AudioImage(double[][] freqTimeValues) {
        loadImage(freqTimeValues, 8);
    }
    public AudioImage(double[] pressureValues, int height) {
        DoubleSummaryStatistics stat = Arrays.stream(pressureValues).summaryStatistics();
        double min = stat.getMin();
        double max = stat.getMax();
        double interval = (max-min)/(height-1);
        double[][] displayField = new double[pressureValues.length][height];
        for (int i = 0; i < pressureValues.length; i++) {
            displayField[i][(int)((pressureValues[i]-min)/interval)] = 1.0;
        }
        loadImage(displayField,8);
    }
    public AudioImage(Sound sound, double interval, int numFrequencies) {
        System.out.println(((int)(Math.floor(sound.length()/interval)))+" "+numFrequencies);
        double[][] freqTimeValues = new double[(int)(Math.floor(sound.length()/interval))][numFrequencies];
        int index = 0;
        timeResolution = interval;
        freqResolution = 1/interval;
        for (double start = 0; start+interval < sound.length(); start+=interval) {
            double[][] freqValues = sound.freqPhaseValues(start, start+interval, 2*numFrequencies);
            for (int i = 0; i < numFrequencies; i++) {
                freqTimeValues[index][i] = Math.sqrt(i)*Math.sqrt(freqValues[i][0]*freqValues[i][0]+freqValues[i][1]*freqValues[i][1]);
            }
            index++;
        }
        loadImage(freqTimeValues, 8);
    }
    private void loadImage(double[][] freqTimeValues, int bits) {
        double[][] image2D = normalize(freqTimeValues);
        // Fine Through here
        int mask = (1<<bits)-1;
        colorModel = new DirectColorModel(bits, mask, mask, mask);
        width = image2D.length;
        height = image2D[0].length;
        image = stretch(image2D,bits);
    }
    private static double[][] normalize(double[][] inputImage) {
        double max = 0;
        double[][] out = new double[inputImage.length][inputImage[0].length];
        for (int x = 0; x < inputImage.length; x++) for (int y = 0; y < inputImage[x].length; y++) {
            if (inputImage[x][y]>max)  {
                max = inputImage[x][y];
            }
        }
        for (int x = 0; x < inputImage.length; x++) for (int y = 0; y < inputImage[x].length; y++) {
            out[x][y] = Math.sqrt(inputImage[x][y]/max);
        }
        return out;
    }
    private int[] stretch(double[][] inputImage, int bits) {
        int[] out = new int[width*height];
        for (int x = 0; x < width; x++) for (int y = 0; y < height; y++) {
            out[y*width+x] = ((1<<(bits))-1)-(int) (inputImage[x][y]*((1<<(bits))-1));
        }
        return out;
    }
    @Override
    public void addConsumer(ImageConsumer ic) {
        consumers.add(ic);
    }
    @Override
    public boolean isConsumer(ImageConsumer ic) {
        return consumers.contains(ic);
    }
    @Override
    public void removeConsumer(ImageConsumer ic) {
        consumers.remove(ic);
    }
    @Override
    public void startProduction(ImageConsumer ic) {
        addConsumer(ic);
        // clone the vector containing consumers
        Set<ImageConsumer> copy = new HashSet<>(consumers);
        // loop over all consumers
        for (ImageConsumer e: copy) {
            ic.setColorModel(colorModel);
            ic.setDimensions(width, height);
            ic.setHints(ImageConsumer.RANDOMPIXELORDER);
            ic.setPixels(0, 0, width, height, colorModel, image, 0, width);
            ic.imageComplete(ImageConsumer.STATICIMAGEDONE);
        }
    }
    @Override
    public void requestTopDownLeftRightResend(ImageConsumer ic) {
        // Do nothing
    }
    public AudioImage(Sound sound) {
        AudioImageConverter converter = new AudioImageConverter(0.02, 1024, 100, 10, 5, 4, 44100);
        double[][][] timeFreqPhaseValues = converter.getRawTimeFreqPhaseImage(sound);
        converter.cleanTimeFreqPhaseImage(timeFreqPhaseValues);
        this.freqResolution = converter.freqResolution();
        this.timeResolution = converter.timeResolution();
        loadImage(timeFreqPhaseValues, converter.numFreqDivisions());
    }
    private boolean useShift = false;
    public AudioImage(Sound sound, int numClean, boolean useShifted) {
        residualPasses = numClean;
        this.useShift = useShifted;
        timeResolution = 0.005;
        double samplingInterval = 0.02;
        int samplingResolution = 1024;
        Sound[] sounds = getPhaseShiftedSounds(sound);
        double[][][][] freqTimeFreqPhaseValues = new double[sounds.length][(int)(sound.length()/timeResolution)][][];
        for (int freqShiftIndex = 0; freqShiftIndex < freqTimeFreqPhaseValues.length; freqShiftIndex++) for (int timeIndex = 0; timeIndex < freqTimeFreqPhaseValues[freqShiftIndex].length; timeIndex++) {
            double time = timeResolution*timeIndex;
            freqTimeFreqPhaseValues[freqShiftIndex][timeIndex] = chopArray(sounds[freqShiftIndex].freqPhaseValues(time, time+samplingInterval, samplingResolution),100,time,samplingInterval);
        }
        double[][][] timeFreqPhaseValues = new double[freqTimeFreqPhaseValues[0].length][freqTimeFreqPhaseValues.length*freqTimeFreqPhaseValues[0][0].length][2];
        for (int i = 0; i < timeFreqPhaseValues.length; i++) for (int j = 0; j < timeFreqPhaseValues[i].length; j++) {
            timeFreqPhaseValues[i][j] = freqTimeFreqPhaseValues[j%5][i][j/5];
        }
        cleanResiduals(timeFreqPhaseValues, freqTimeFreqPhaseValues.length, samplingInterval, timeResolution);
        loadImage(timeFreqPhaseValues, freqTimeFreqPhaseValues.length);
    }
    private Sound[] getPhaseShiftedSounds(Sound sound) {
        freqResolution = 10;
        int numParts = 5;
        Sound[] out = new Sound[numParts];
        for (int i = 0; i < numParts; i++) {
            out[i] = sound.getShiftedByPhase(i*freqResolution, 0);
        }
        return out;
    }
    private double[][] chopArray(double[][] freqValues,int numKeep, double time, double samplingInterval) {
        double[][] out = new double[numKeep][2];
        for (int i = 0; i < out.length; i++) {
            out[i] = Converter.polarPoint(Converter.adjustPointBack(freqValues[i],i/samplingInterval,time));
        }
        return out;
    }
    private void loadImage(double[][][] timeFreqPhaseValues, int numFreqDivisions) {
        double[][][] hsb = new double[timeFreqPhaseValues.length][timeFreqPhaseValues[0].length][3];
        for (int i = 0; i < hsb.length; i++) for (int j = 0; j < hsb[0].length; j++) {
            hsb[i][j][0] = timeFreqPhaseValues[i][j][1];
            hsb[i][j][1] = getSaturation(timeFreqPhaseValues[i],j,numFreqDivisions);
            hsb[i][j][2] = timeFreqPhaseValues[i][j][0];
        }
        float[][][] normalHSB = normalizeHSB(hsb);
        int[][] picture = convertToRGB(normalHSB);
        image = new int[picture.length*picture[0].length];
        colorModel = ColorModel.getRGBdefault();
        width = picture.length;
        height = picture[0].length;
        for (int i = 0; i < width; i++) for (int j = 0; j < height; j++) {
            image[i+width*j] = picture[i][(height-j-1)];
        }
    }
    private double getSaturation(double[][] freqValues,int index, int numFreqDivisions) {
        return freqValues[index][0] / (1+freqValues[index][0]+valueWithinBounds(freqValues, index-numFreqDivisions)+valueWithinBounds(freqValues, index+numFreqDivisions));
    }
    private double valueWithinBounds(double[][] aray, int index) {
        if (index >= 0 && index < aray.length) return aray[index][0];
        else return 0;
    }
    private float[][][] normalizeHSB(double[][][] raw) {
        double maxBrightness = 0.0;
        for (int i = 0; i < raw.length; i++) for (int j = 0; j < raw[i].length; j++) {
            if (raw[i][j][2]>maxBrightness) maxBrightness = raw[i][j][2];
        }
        float[][][] out = new float[raw.length][raw[0].length][3];
        for (int i = 0; i < raw.length; i++) for (int j = 0; j < raw[0].length; j++) {
            out[i][j][0] = (float) (raw[i][j][0]/(2*Math.PI)+0.5);
            out[i][j][1] = (float) raw[i][j][1];
            out[i][j][2] = (float) Math.pow(raw[i][j][2]/maxBrightness,0.33);
        }
        return out;
    }
    private int[][] convertToRGB(float[][][] hsb) {
        int[][] out = new int[hsb.length][hsb[0].length];
        for (int i = 0; i < out.length; i++) for (int j = 0; j < out[i].length; j++) {
            out[i][j] = Color.HSBtoRGB(hsb[i][j][0],hsb[i][j][1],hsb[i][j][2]);
        }
        return out;
    }
    
    public double freqResolution() {
        return freqResolution;
    }
    public double timeResolution() {
        return timeResolution;
    }
    private int residualPasses = 0;
    private void cleanResiduals(double[][][] timeFreqPhaseValues, int freqResolution, double samplingInterval, double timeResolution) {
        convertToLocalPhase(timeFreqPhaseValues, freqResolution, samplingInterval, timeResolution);
        boolean[][] isSelected = new boolean[timeFreqPhaseValues.length][timeFreqPhaseValues[0].length];
        for (int i = 0; i < timeFreqPhaseValues.length; i++) for (int j = 0; j < residualPasses; j++) {
            double freqIndex1 = selectGreatestIndex(timeFreqPhaseValues[i],isSelected[i],freqResolution);
            double[] phaseValues1 = estimatePhaseValues(freqIndex1,timeFreqPhaseValues[i]);
            subtractResidualFrequencies(timeFreqPhaseValues[i], freqResolution, freqIndex1, phaseValues1, isSelected[i]);
        }
        convertToGlobalPhase(timeFreqPhaseValues, freqResolution, samplingInterval, timeResolution);
    }
    private static int testcount = 0;
    private double selectGreatestIndex(double[][] freqPhaseValues, boolean[] allSelected, double freqResolution) {
        int maxIndex = 0;
        for (int i = 0; i < freqPhaseValues.length; i++) if (!allSelected[i]&&(freqPhaseValues[i][0]>freqPhaseValues[maxIndex][0]||allSelected[maxIndex])&&(i==0||freqPhaseValues[i][0]>freqPhaseValues[i-1][0])&&(i+1==freqPhaseValues.length||freqPhaseValues[i][0]>freqPhaseValues[i+1][0])) {
            maxIndex = i;
        }
        allSelected[maxIndex] =  true;
        if (maxIndex>0) allSelected[maxIndex-1] = true;
        if (maxIndex>1) allSelected[maxIndex-2] = true;
        if (maxIndex+1<freqPhaseValues.length) allSelected[maxIndex+1] = true;
        if (maxIndex+2<freqPhaseValues.length) allSelected[maxIndex+2] = true;
        if (useShift&&maxIndex>0 && maxIndex+1<freqPhaseValues.length) {
            double marDif = (freqPhaseValues[maxIndex+1][0]-freqPhaseValues[maxIndex-1][0]) / (freqPhaseValues[maxIndex][0]+1);
            // Going halfway to another point produces one difference 1.5 times the freq resolution and one difference 0.5 times the freq resolution.
            // The expected magnitude overlap (for small values) is sin(pi*x)/(pi*x)
            double halfwayExpectedDiference = Math.sin(Math.PI*0.5/freqResolution)/(Math.PI*0.5/freqResolution) - Math.sin(Math.PI*1.5/freqResolution)/(Math.PI*1.5/freqResolution);
            double freqDif = marDif / (2*halfwayExpectedDiference);
            // If it was closer to the other point the other point would be selected.
            if (freqDif>0.5) freqDif = 0.5;
            if (freqDif<-0.5) freqDif = -0.5;
            return maxIndex+freqDif;
        } else return maxIndex;
    }
    private double[] estimatePhaseValues(double freqIndex1, double[][] freqPhaseValues) {
        int baseIndex = (int)Math.floor(freqIndex1);
        double fracHigher = freqIndex1-baseIndex;
        double fracLower = 1-fracHigher;
        double[] lower = Converter.euclidianPoint(freqPhaseValues[baseIndex]);
        double[] higher = Converter.euclidianPoint(freqPhaseValues[baseIndex+1]);
        double[] ecOut = new double[] {fracLower*lower[0]+fracHigher*higher[0], fracLower*lower[1]+fracHigher*higher[1]};
        return Converter.polarPoint(ecOut);
    }
    private void subtractResidualFrequencies(double[][] freqPhaseValues, int freqResolution, double freqIndex1, double[] phaseValue1, boolean[] allSelected) {
        for (int freqIndex2 = 0; freqIndex2 < freqPhaseValues.length; freqIndex2++) if (!allSelected[freqIndex2]) Converter.polarSubtractResidualFrequency(freqIndex1, freqIndex2, freqResolution, phaseValue1, freqPhaseValues[freqIndex2]);
    }
    private void convertToLocalPhase(double[][][] timeFreqPhaseValues, int numFreqDivisions, double samplingInterval, double timeResolution) {
        for (int i = 0; i < timeFreqPhaseValues.length; i++) for (int j = 0; j < timeFreqPhaseValues[i].length; j++) {
            timeFreqPhaseValues[i][j] = Converter.adjustPolarPointForward(timeFreqPhaseValues[i][j], j/samplingInterval/numFreqDivisions, i*timeResolution);
        }
    }
    private void convertToGlobalPhase(double[][][] timeFreqPhaseValues, int numFreqDivisions, double samplingInterval, double timeResolution) {
        for (int i = 0; i < timeFreqPhaseValues.length; i++) for (int j = 0; j < timeFreqPhaseValues[i].length; j++) {
            timeFreqPhaseValues[i][j] = Converter.adjustPolarPointBack(timeFreqPhaseValues[i][j], j/samplingInterval/numFreqDivisions, i*timeResolution);
        }
    }
}
