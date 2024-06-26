package analysis;


import java.io.*;

/**
 * Provides a list of useful methods converting between different types of sound classes.
 */
public class Converter implements Serializable {


    
    /**
     * The Fast Fourier Transform (generic version, with NO optimizations).
     *
     * @param inputReal
     *            an array of length n, the real part
     * @param inputImag
     *            an array of length n, the imaginary part
     * @param DIRECT
     *            TRUE = direct transform (rotate frequencies into point values), FALSE = inverse transform (rotate point values into frequencies)
     * @return a new array of length 2n
     */
    public static double[][] fft(final double[] inputReal, double[] inputImag,
                               boolean DIRECT) {
        // - n is the dimension of the problem
        // - nu is its logarithm in base e
        int n = inputReal.length;

        // If n is a power of 2, then ld is an integer (_without_ decimals)
        double ld = Math.log(n) / Math.log(2.0);

        // Here I check if n is a power of 2. If exist decimals in ld, I quit
        if (((int) ld) - ld != 0) {
            throw new IllegalArgumentException("The number of elements is not a power of 2.");
        }

        // Declaration and initialization of the variables
        // ld should be an integer, actually, so I don't lose any information in
        // the cast
        int nu = (int) ld;
        int n2 = n / 2;
        int nu1 = nu - 1;
        double[] xReal = new double[n];
        double[] xImag = new double[n];
        double tReal, tImag, p, arg, c, s;

        // Here I check if I'm going to do the direct transform or the inverse
        // transform.
        double constant;
        if (DIRECT)
            constant = -2 * Math.PI;
        else
            constant = 2 * Math.PI;

        // I don't want to overwrite the input arrays, so here I copy them. This
        // choice adds \Theta(2n) to the complexity.
        for (int i = 0; i < n; i++) {
            xReal[i] = inputReal[i];
            xImag[i] = inputImag[i];
        }

        // First phase - calculation
        int k = 0;
        for (int l = 1; l <= nu; l++) {
            while (k < n) {
                for (int i = 1; i <= n2; i++) {
                    p = bitreverseReference(k >> nu1, nu);
                    // direct FFT or inverse FFT
                    arg = constant * p / n;
                    c = Math.cos(arg);
                    s = Math.sin(arg);
                    tReal = xReal[k + n2] * c + xImag[k + n2] * s;
                    tImag = xImag[k + n2] * c - xReal[k + n2] * s;
                    xReal[k + n2] = xReal[k] - tReal;
                    xImag[k + n2] = xImag[k] - tImag;
                    xReal[k] += tReal;
                    xImag[k] += tImag;
                    k++;
                }
                k += n2;
            }
            k = 0;
            nu1--;
            n2 /= 2;
        }

        // Second phase - recombination
        k = 0;
        int r;
        while (k < n) {
            r = bitreverseReference(k, nu);
            if (r > k) {
                tReal = xReal[k];
                tImag = xImag[k];
                xReal[k] = xReal[r];
                xImag[k] = xImag[r];
                xReal[r] = tReal;
                xImag[r] = tImag;
            }
            k++;
        }

        double[][] out = new double[xReal.length][2];
        double radice = 1 / Math.sqrt(n);
        for (int i = 0; i < out.length; i++) {
            out[i][0] = xReal[i]*radice;
            out[i][1] = xImag[i]*radice;
        }
        return out;
    }

    /**
     * @author Orlando Selenu
     */
    private static int bitreverseReference(int j, int nu) {
        int j2;
        int j1 = j;
        int k = 0;
        for (int i = 1; i <= nu; i++) {
            j2 = j1 / 2;
            k = 2 * k + j1 - 2 * j2;
            j1 = j2;
        }
        return k;
    }

    public static double[][] flipArrayDimensions(double[][] in) {
        double[][] out = new double[in[0].length][in.length];
        for (int x = 0; x < in.length; x++) for (int y = 0; y < in[x].length; y++) {
            out[y][x] = in[x][y];
        }
        return out;
    }
    public static long[][] flipArrayDimensions(long[][] in) {
        long[][] out = new long[in[0].length][in.length];
        for (int x = 0; x < in.length; x++) for (int y = 0; y < in[x].length; y++) {
            out[y][x] = in[x][y];
        }
        return out;
    }
    public static double[][] polarCoordinates(double[][] euclidian) {
        double[][] polar = new double[euclidian.length][2];
        for (int i = 0; i < polar.length; i++) {
            polar[i] = polarPoint(euclidian[i]);
        }
        return polar;
    }
    public static final double[] polarPoint(double[] euclidian) {
        double[] polar = new double[2];
        polar[0] = Math.hypot(euclidian[0], euclidian[1]);
        polar[1] = Math.atan2(euclidian[1],euclidian[0]);
        return polar;
    }
    public static double[][] euclidianCoordinates(double[][] polar) {
        double[][] euclidian = new double[polar.length][2];
        for (int i = 0; i < polar.length; i++) {
            euclidian[i][0] = polar[i][0]*Math.cos(polar[i][1]);
            euclidian[i][1] = polar[i][0]*Math.sin(polar[i][1]);
        }
        return euclidian;
    }
    public static double[] euclidianPoint(double[] polar) {
        return new double[] {polar[0]*Math.cos(polar[1]),polar[0]*Math.sin(polar[1])};
    }
    public static double[][] fromLong(long[][] in) {
        double[][] out = new double[in.length][in[0].length];
        for (int y = 0; y < in.length; y++) for (int x = 0; x < in[y].length; x++) {
            out[y][x] = in[y][x];
        }
        return out;
    }
    public static int power2Higher(double numSamples) {
        int out = 1;
        while (out<numSamples) out<<=1;
        return out;
    }
    public static double[] adjustPointBack(double[] in, double freq, double time) {
        return multiplyPoint(in,rotationAtTime(freq,-time));
    }
    public static double[] adjustPointForward(double[] in, double freq, double time) {
        return multiplyPoint(in, rotationAtTime(freq, time));
    }
    public static double[] adjustPolarPointBack(double[] in, double freq, double time) {
        return new double[] {in[0],in[1]-2*Math.PI*freq*time};
    }
    public static double[] adjustPolarPointForward(double[] in, double freq, double time) {
        return new double[] {in[0],in[1]+2*Math.PI*freq*time};
    }
    public static double floor(double number, double divisor) {
        return Math.floor(number/divisor)*divisor;
    }
    private static final double ONE_POINT_FIVE_CUBED = Math.pow(1.5,3); 
    public static double cubicInterpolation(double n1, double n2, double n3, double n4, double x) {
        double yddd = ydd(n4,n3,n2)-ydd(n3,n2,n1);
        double ydd = (ydd(n4,n3,n2)+ydd(n3,n2,n1))/2;
        double x3 = yddd / 6;
        double x2 = ydd / 2;
        double x1 = (n4-n1-2*ONE_POINT_FIVE_CUBED*x3)/3;
        double x0 = (n1+n2+n3+n4-5*x2)/4;
        return x3*x*x*x + x2*x*x + x1*x + x0;
    }
    private static double ydd(double n1, double n2, double n3) {
        return (n3-n2)-(n2-n1);
    }
    public static double[] freqCorrelation(double freqIndex1, double freqIndex2, double freqRatio) {
        //Residual: given freq num a -> e^(2PIiax) -> residual at b = e^(-2PIibx)*e^(2PIiax) = e^(2PIi(a-b)x)
        //Integral (0,1) e^(2PIi(a-b)x) = i(1-e^(2PIi(b-a)))/2PI(b-a)
        // = 
        double[] out = new double[2];
        double bMinusA = (freqIndex2-freqIndex1)/freqRatio;
        out[1] = (Math.cos(2*Math.PI*bMinusA)-1)/(2*Math.PI*bMinusA);
        out[0] = Math.sin(2*Math.PI*bMinusA)/(2*Math.PI*bMinusA);
        return out;
    }
    public static double[] multiplyPoint(double[] point1, double[] point2) {
        return new double[] {point1[0]*point2[0]-point1[1]*point2[1], point1[0]*point2[1]+point1[1]*point2[0]};
    }
    public static double[] rotationAtTime(double freq, double time) {
        return new double[]{Math.cos(2*Math.PI*freq*time), Math.sin(2*Math.PI*freq*time)};
    }
    public static double[] residualFrequency(double freqIndex1, double freqIndex2, double freqRatio, double[] in) {
        return multiplyPoint(in, freqCorrelation(freqIndex1,freqIndex2,freqRatio));
    }
    public static double[] subtractResidualFrequency(double freqIndex1, double freqIndex2, double freqRatio, double[] in, double[] out) {
        double[] residual = residualFrequency(freqIndex1, freqIndex2, freqRatio, in);
        out[0] -= residual[0];
        out[1] -= residual[1];
        return out;
    }
    public static double[] polarSubtractResidualFrequency(double freqIndex1, double freqIndex2, double freqRatio, double[] in, double[] out) {
        double[] ecOut = subtractResidualFrequency(freqIndex1, freqIndex2, freqRatio, euclidianPoint(in),euclidianPoint(out));
        double[] plOut = polarPoint(ecOut);
        out[0] = plOut[0];
        out[1] = plOut[1];
        return out;
    }
}