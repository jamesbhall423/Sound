package analysis;

public class AudioImageConverter {
    private double freqResolution;
    private double freqInterval;
    private double timeResolution;
    private int samplingResolution;
    private double samplingInterval;
    private int numKeep;
    private int numFreqDivisions;
    private int numTimeDivisions;
    private int residualPasses;
    private double samplingRate;
    public AudioImageConverter() {
        initialize(0.02, 1024,1024,10,5,4,44100);
    }
    public AudioImageConverter(int numKeep) {
        initialize(0.02, 1024,numKeep,10,5,4,44100);
    }
    public AudioImageConverter(double samplingInterval, int samplingResolution, int numKeep, int residualPasses, int numFreqDivisions, int numTimeDivisions, double samplingRate) {
        initialize(samplingInterval, samplingResolution, numKeep, residualPasses, numFreqDivisions, numTimeDivisions, samplingRate);
    }
    private void initialize(double samplingInterval, int samplingResolution, int numKeep, int residualPasses, int numFreqDivisions, int numTimeDivisions, double samplingRate) {
        this.timeResolution = samplingInterval/numTimeDivisions;
        this.numKeep = numKeep;
        this.samplingInterval = samplingInterval;
        this.samplingResolution = samplingResolution;
        this.numFreqDivisions = numFreqDivisions;
        this.residualPasses = residualPasses;
        this.numTimeDivisions = numTimeDivisions;
        this.samplingRate = samplingRate;
        this.freqInterval = 1/samplingInterval;
        this.freqResolution = freqInterval/numFreqDivisions;
    }
    public int numFreqDivisions() {
        return numFreqDivisions;
    }
    public double freqResolution() {
        return freqResolution;
    }
    public double timeResolution() {
        return timeResolution;
    }
    public int samplingResolution() {
        return samplingResolution;
    }
    public double samplingInterval() {
        return samplingInterval;
    }
    public double[][][] getRawTimeFreqPhaseImage(Sound sound) {
        Sound[] sounds = getPhaseShiftedSounds(sound);
        double[][][][] freqTimeFreqPhaseValues = getFreqTimeFreqPhaseValues(sounds);
        double[][][] timeFreqPhaseValues = new double[freqTimeFreqPhaseValues[0].length][freqTimeFreqPhaseValues.length*freqTimeFreqPhaseValues[0][0].length][2];
        for (int i = 0; i < timeFreqPhaseValues.length; i++) for (int j = 0; j < timeFreqPhaseValues[i].length; j++) {
            timeFreqPhaseValues[i][j] = freqTimeFreqPhaseValues[j%numFreqDivisions][i][j/numFreqDivisions];
        }
        return timeFreqPhaseValues;
    }
    private double[][][][] getFreqTimeFreqPhaseValues(Sound[] sounds) {
        double[][][][] freqTimeFreqPhaseValues = new double[sounds.length][(int)(sounds[0].length()/timeResolution)][][];
        for (int freqShiftIndex = 0; freqShiftIndex < freqTimeFreqPhaseValues.length; freqShiftIndex++) for (int timeIndex = 0; timeIndex < freqTimeFreqPhaseValues[freqShiftIndex].length; timeIndex++) {
            double time = timeResolution*timeIndex;
            freqTimeFreqPhaseValues[freqShiftIndex][timeIndex] = chopArray(sounds[freqShiftIndex].freqPhaseValues(time, time+samplingInterval, samplingResolution),time);
        }
        return freqTimeFreqPhaseValues;
    }
    private double[][] chopArray(double[][] freqValues, double time) {
        double[][] out = new double[numKeep][2];
        for (int i = 0; i < out.length; i++) {
            out[i] = Converter.polarPoint(Converter.adjustPointBack(freqValues[i],i/samplingInterval,time));
        }
        return out;
    }
    private Sound[] getPhaseShiftedSounds(Sound sound) {
        Sound[] out = new Sound[numFreqDivisions];
        for (int i = 0; i < numFreqDivisions; i++) {
            out[i] = sound.getShiftedByPhase(i*freqResolution, 0);
        }
        return out;
    }
    public Sound getSoundFromRawTimeFreqPhaseImage(double[][][] rawTimeFreqPhaseImage) {
        double[][][][][] timeFreqTimeFreqPhaseValues = new double[numTimeDivisions][numFreqDivisions][rawTimeFreqPhaseImage.length/numTimeDivisions][rawTimeFreqPhaseImage[0].length/numFreqDivisions][2];
        for (int timeIndex = 0; timeIndex<rawTimeFreqPhaseImage.length; timeIndex++) for (int freqIndex = 0; freqIndex < rawTimeFreqPhaseImage[timeIndex].length; freqIndex++) {
            try {
                timeFreqTimeFreqPhaseValues[timeIndex%numTimeDivisions][freqIndex%numFreqDivisions][timeIndex/numTimeDivisions][freqIndex/numFreqDivisions] = rawTimeFreqPhaseImage[timeIndex][freqIndex];
            } catch (ArrayIndexOutOfBoundsException e) {
                // time or freq not perfectly divisible
            }
        }
        Sound[][] sounds = new Sound[numTimeDivisions][numFreqDivisions];
        for (int i = 0; i < numTimeDivisions; i++) for (int j = 0; j < numFreqDivisions; j++) {
            sounds[i][j] = reverseRotateSound(createSoundFromTimeFreqPhaseValues(timeFreqTimeFreqPhaseValues[i][j],i,j),i,j);
        }
        Sound combined = new BaseSound(new double[(int)(samplingRate*sounds[0][0].length())][2],samplingRate);
        for (int i = 0; i < numTimeDivisions; i++) for (int j = 0; j < numFreqDivisions; j++) {
            combined = combined.combine(sounds[i][j],0,0,combined.length(),combined.length(),1,1);
        }
        return combined.scaleVolume(1.0/(numFreqDivisions*numTimeDivisions));
    }
    public BaseSound createSoundFromTimeFreqPhaseValues(double[][][] timeFreqPhaseValues,int timePart, int freqPart) {

        double[][][] localTimeFreqPhaseValues = new double[timeFreqPhaseValues.length][timeFreqPhaseValues[0].length][2];
        for (int timeIndex = 0; timeIndex < localTimeFreqPhaseValues.length; timeIndex++) for (int freqIndex = 0; freqIndex < localTimeFreqPhaseValues[timeIndex].length; freqIndex++) {
            localTimeFreqPhaseValues[timeIndex][freqIndex] = Converter.adjustPointForward(Converter.euclidianPoint(timeFreqPhaseValues[timeIndex][freqIndex]), freqIndex*freqInterval, timePart*timeResolution);
        }
        return new BaseSound(new double[0][0],samplingRate).getSound(localTimeFreqPhaseValues,localTimeFreqPhaseValues.length*samplingInterval);
    }
    public Sound reverseRotateSound(Sound sound, int timePart, int freqPart) {
        return sound.trimStart(samplingInterval-timePart*timeResolution).getShiftedByPhase(-freqPart*freqResolution, -samplingInterval);
    }
    // Returns a record (in order) of the frequencies that were used to clean the record.
    public double[][] cleanTimeFreqPhaseImage(double[][][] timeFreqPhaseValues) {
        convertToLocalPhase(timeFreqPhaseValues);
        boolean[][] isSelected = new boolean[timeFreqPhaseValues.length][timeFreqPhaseValues[0].length];
        double[][] record = new double[timeFreqPhaseValues.length][residualPasses];
        for (int i = 0; i < timeFreqPhaseValues.length; i++) for (int j = 0; j < residualPasses; j++) {
            double freqIndex1 = selectGreatestIndex(timeFreqPhaseValues[i],isSelected[i]);
            record[i][j] = freqIndex1;
            double[] phaseValues1 = estimatePhaseValues(freqIndex1,timeFreqPhaseValues[i]);
            subtractResidualFrequencies(timeFreqPhaseValues[i], freqIndex1, phaseValues1, isSelected[i]);
        }
        convertToGlobalPhase(timeFreqPhaseValues);
        return record; 
    }
    public void undoCleanTimeFreqPhaseImage(double[][][] cleanedTimeFreqPhaseImage, double[][] cleanRecord) {
        convertToLocalPhase(cleanedTimeFreqPhaseImage);
        int[][] isSelected = new int[cleanedTimeFreqPhaseImage.length][cleanedTimeFreqPhaseImage[0].length];
        for (int i = 0; i < cleanedTimeFreqPhaseImage.length; i++) for (int j = residualPasses-1; j >= 0; j--) {
            double freqIndex1 = cleanRecord[i][j];
            modifySelectedCount(isSelected[i], freqIndex1, cleanedTimeFreqPhaseImage[i].length, 1);
        }
        for (int i = 0; i < cleanedTimeFreqPhaseImage.length; i++) for (int j = residualPasses-1; j >= 0; j--) {
            double freqIndex1 = cleanRecord[i][j];
            double[] phaseValues1 = estimatePhaseValues(freqIndex1,cleanedTimeFreqPhaseImage[i]);
            addResidualFrequencies(cleanedTimeFreqPhaseImage[i], freqIndex1, phaseValues1, isSelected[i],i);
            modifySelectedCount(isSelected[i], freqIndex1, cleanedTimeFreqPhaseImage[i].length, -1);
        }
        convertToGlobalPhase(cleanedTimeFreqPhaseImage);
    }
    public Sound getSoundFromCleanedImage(double[][][] cleanedTimeFreqPhaseImage, double[][] cleanRecord) {
        undoCleanTimeFreqPhaseImage(cleanedTimeFreqPhaseImage, cleanRecord);
        return getSoundFromRawTimeFreqPhaseImage(cleanedTimeFreqPhaseImage);
    }
    private void modifySelectedCount(int[] isSelected, double freqIndex, int length, int modifier) {
        int selectedIndex = (int)Math.round(freqIndex);
        isSelected[selectedIndex]+=modifier;
        if (selectedIndex>0) isSelected[selectedIndex-1]+=modifier;
        if (selectedIndex>1) isSelected[selectedIndex-2]+=modifier;
        if (selectedIndex+1<length) isSelected[selectedIndex+1]+=modifier;
        if (selectedIndex+2<length) isSelected[selectedIndex+2]+=modifier;
    }
    private void convertToLocalPhase(double[][][] timeFreqPhaseValues) {
        for (int i = 0; i < timeFreqPhaseValues.length; i++) for (int j = 0; j < timeFreqPhaseValues[i].length; j++) {
            timeFreqPhaseValues[i][j] = Converter.adjustPolarPointForward(timeFreqPhaseValues[i][j], j*freqResolution, i*timeResolution);
        }
    }
    private void convertToGlobalPhase(double[][][] timeFreqPhaseValues) {
        for (int i = 0; i < timeFreqPhaseValues.length; i++) for (int j = 0; j < timeFreqPhaseValues[i].length; j++) {
            timeFreqPhaseValues[i][j] = Converter.adjustPolarPointBack(timeFreqPhaseValues[i][j], j*freqResolution, i*timeResolution);
        }
    }
    private double selectGreatestIndex(double[][] freqPhaseValues, boolean[] allSelected) {
        int maxIndex = 0;
        for (int i = 0; i < freqPhaseValues.length; i++) if (!allSelected[i]&&(freqPhaseValues[i][0]>freqPhaseValues[maxIndex][0]||allSelected[maxIndex])&&(i==0||freqPhaseValues[i][0]>freqPhaseValues[i-1][0])&&(i+1==freqPhaseValues.length||freqPhaseValues[i][0]>freqPhaseValues[i+1][0])) {
            maxIndex = i;
        }
        allSelected[maxIndex] =  true;
        if (maxIndex>0) allSelected[maxIndex-1] = true;
        if (maxIndex>1) allSelected[maxIndex-2] = true;
        if (maxIndex+1<freqPhaseValues.length) allSelected[maxIndex+1] = true;
        if (maxIndex+2<freqPhaseValues.length) allSelected[maxIndex+2] = true;
        if (maxIndex>0 && maxIndex+1<freqPhaseValues.length) {
            double marDif = (freqPhaseValues[maxIndex+1][0]-freqPhaseValues[maxIndex-1][0]) / (freqPhaseValues[maxIndex][0]+1);
            // Going halfway to another point produces one difference 1.5 times the freq resolution and one difference 0.5 times the freq resolution.
            // The expected magnitude overlap (for small values) is sin(pi*x)/(pi*x)
            double halfwayExpectedDiference = Math.sin(Math.PI*0.5/numFreqDivisions)/(Math.PI*0.5/numFreqDivisions) - Math.sin(Math.PI*1.5/numFreqDivisions)/(Math.PI*1.5/numFreqDivisions);
            double freqDif = marDif / (2*halfwayExpectedDiference);
            // If it was closer to the other point the other point would be selected.
            if (freqDif>=0.5) freqDif = 0.4999;
            if (freqDif<=-0.5) freqDif = -0.4999;
            return maxIndex+freqDif;
        } else return maxIndex;
    }
    private double[] estimatePhaseValues(double freqIndex1, double[][] freqPhaseValues) {
        int baseIndex = (int)Math.floor(freqIndex1);
        double fracHigher = freqIndex1-baseIndex;
        double fracLower = 1-fracHigher;
        double[] lower = Converter.euclidianPoint(freqPhaseValues[baseIndex]);
        double[] higher;
        if (baseIndex+1<freqPhaseValues.length) higher = Converter.euclidianPoint(freqPhaseValues[baseIndex+1]);
        else higher=lower;
        double[] ecOut = new double[] {fracLower*lower[0]+fracHigher*higher[0], fracLower*lower[1]+fracHigher*higher[1]};
        return Converter.polarPoint(ecOut);
    }
    private void subtractResidualFrequencies(double[][] freqPhaseValues, double freqIndex1, double[] phaseValue1, boolean[] allSelected) {
        for (int freqIndex2 = 0; freqIndex2 < freqPhaseValues.length; freqIndex2++) if (!allSelected[freqIndex2]||Math.abs(freqIndex2-freqIndex1)<2.5&&Math.abs(freqIndex2-freqIndex1)>1) {
            Converter.polarSubtractResidualFrequency(freqIndex1, freqIndex2, numFreqDivisions, phaseValue1, freqPhaseValues[freqIndex2]);
        }
    }
    private void addResidualFrequencies(double[][] freqPhaseValues, double freqIndex1, double[] phaseValue1, int[] allSelected, int index) {
        double[] reversePhaseValue1 = new double[] {phaseValue1[0],phaseValue1[1]+Math.PI};
        for (int freqIndex2 = 0; freqIndex2 < freqPhaseValues.length; freqIndex2++) if (allSelected[freqIndex2]==0||Math.abs(freqIndex2-freqIndex1)<2.5&&Math.abs(freqIndex2-freqIndex1)>1) Converter.polarSubtractResidualFrequency(freqIndex1, freqIndex2, numFreqDivisions, reversePhaseValue1, freqPhaseValues[freqIndex2]);
    }
    public double[][] timeFreqAmpValues(double[][][] timeFreqPhaseValues) {
        double[][] out = new double[timeFreqPhaseValues.length][timeFreqPhaseValues[0].length];
        for (int i = 0; i < out.length; i++) for (int j = 0; j < out[i].length; j++) {
            out[i][j] = timeFreqPhaseValues[i][j][0];
        }
        return out;
    }
    public double[][] smear(double[][] timeFreqValues, double percentDifference) {
        double[][] out = new double[timeFreqValues.length][timeFreqValues[0].length];
        for (int i = 0; i < out.length; i++) for (int j = 0; j < out[i].length; j++) for (int k = (int)Math.floor((1-percentDifference)*j); k <= Math.ceil((1+percentDifference)*j)&&k<out[i].length; k++) {
            double absDifference = (j-k)/((j+1)*percentDifference);
            double ratio = Math.exp(-absDifference*absDifference);
            if (timeFreqValues[i][k]*ratio>out[i][j]) out[i][j]=timeFreqValues[i][k]*ratio;
        }
        //Marge positive and negative frequencies
        for (int i = 0; i < out.length; i++) for (int j = 1; j < out[i].length; j++) {
            if (out[i][out[i].length-j]>out[i][j]) out[i][j] = out[i][out[i].length-j];
        }
        return out;
    }
    public double[][][] getConsistentTimeFreqPhaseImage(double[][][] timeFreqPhaseValues, double[][] other, double otherAmp) {
        double[][][] out = new double[Math.min(timeFreqPhaseValues.length,other.length)][timeFreqPhaseValues[0].length][2];
        for (int i = 0; i < out.length; i++) for (int j = 0; j < out[i].length; j++) {
            out[i][j][1] = timeFreqPhaseValues[i][j][1];
            if (other[i][j]*otherAmp>=timeFreqPhaseValues[i][j][0]) out[i][j][0] = timeFreqPhaseValues[i][j][0]*timeFreqPhaseValues[i][j][0]/(other[i][j]*otherAmp);
            else out[i][j][0] = other[i][j]*otherAmp;
        }
        return out;
    }
    public double energy(double[][][] timeFreqPhaseValues) {
        double out = 0.0;
        for (int i = 0; i < timeFreqPhaseValues.length; i++) for (int j = 0; j < timeFreqPhaseValues[i].length; j++) {
            out += timeFreqPhaseValues[i][j][0]*timeFreqPhaseValues[i][j][0];
        }
        return out;
    }
    public double energy(double[][] timeFreqValues) {
        double out = 0.0;
        for (int i = 0; i < timeFreqValues.length; i++) for (int j = 0; j < timeFreqValues[i].length; j++) {
            out += timeFreqValues[i][j]*timeFreqValues[i][j];
        }
        return out;
    }
}
