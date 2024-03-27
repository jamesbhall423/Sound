package localization;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

public class PitchCompare {
    public Pitch pitch1;
    public Pitch pitch2;
    public double frequency;
    public double timeDiff;
    public PitchCompare(Pitch pitch1, Pitch pitch2) {
        this.pitch1 = pitch1;
        this.pitch2 = pitch2;
        this.frequency = (pitch1.frequency+pitch2.frequency)/2;
        this.timeDiff = (pitch2.startFreq+pitch2.endFreq-pitch1.startFreq-pitch1.endFreq)/2;
    }
    @Override
    public String toString() {
        return pitch1+""+pitch2+" dif: "+timeDiff+" at "+frequency+"hz";
    }
    public static boolean matches(Pitch pitch1, Pitch pitch2, double maxSeperation) {
        double length1 = pitch1.endFreq-pitch1.startFreq;
        double length2 = pitch2.endFreq-pitch2.startFreq;
        double freqDif = Math.abs(pitch2.frequency-pitch1.frequency);
        double intervalMultiple = Math.abs(Math.log(length1/length2));
        double seperation = Math.abs(pitch2.startFreq+pitch2.endFreq-pitch1.startFreq-pitch1.endFreq)/2;
        System.out.println("Comparing "+pitch1+" "+pitch2);
        System.out.println(seperation+" "+intervalMultiple+" "+freqDif+" "+(1/Math.min(length1,length2)));
        if (seperation>maxSeperation) return false;
        if (intervalMultiple>0.5) return false;
        if (freqDif>1/Math.min(length1,length2)) return false;
        return true;
    }
    public static List<PitchCompare> compare(SortedSet<Pitch> list1, SortedSet<Pitch> list2, double maxSeperation) {
        Iterator<Pitch> itter1 = list1.iterator();
        Iterator<Pitch> itter2 = list2.iterator();
        Pitch last1 = itter1.next();
        Pitch last2 = null;
        boolean last1GreaterThanLast2 = true;
        List<PitchCompare> out = new ArrayList<>();
        while (last1GreaterThanLast2 ? itter2.hasNext() : itter1.hasNext()) {
            if (last1GreaterThanLast2) last2 = itter2.next();
            else last1 = itter1.next();
            if (matches(last1, last2, maxSeperation)) {
                out.add(new PitchCompare(last1, last2));
            }
            last1GreaterThanLast2 = last1.frequency>last2.frequency;
        }
        return out;
    }
}
