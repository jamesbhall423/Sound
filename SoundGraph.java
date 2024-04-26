import java.util.Scanner;

import analysis.AudioImage;
import analysis.DisplayFrame;
import analysis.Sound;
import analysis.SoundInput;
import analysis.DisplayPanel;
import analysis.FrequencyGradientPanel;
import analysis.ClippedSound;
import analysis.BaseSound;
import analysis.Converter;

public class SoundGraph {
    public static void main(String[] args) {
        Scanner keyboard = new Scanner(System.in);
        System.out.print("Enter the name of the sound file: ");
        String filename = keyboard.nextLine();
        System.out.print("Enter the start of the interval: ");
        double start = keyboard.nextDouble();
        keyboard.nextLine();
        SoundInput input = new SoundInput(filename);
        keyboard.close();
        new DisplayFrame(new AudioImage(input.getSound().trimStart(start).trimEnd(5),25,true),filename);
    }
}
