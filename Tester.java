import analysis.Converter;

public class Tester {
    public static void main(String[] args) {
        double[] xReal = new double[16];
        double[] xImag = new double[16];
        double freq = 0.25;
        for (int i = 0; i < xReal.length; i++) {
            xReal[i] = Math.cos(2*Math.PI*freq*i);
            xImag[i] = Math.sin(2*Math.PI*freq*i);
        }
        double[][] fft = Converter.fft(xReal,xImag,true);
        for (int i = 0; i < fft.length; i++) {
            System.out.println(fft[i][0]+" "+fft[i][1]);
        }
    }
}
