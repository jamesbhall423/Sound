package location;

import analysis.Sound;

// Location package altervative to localization package
public class SoundRecording<T extends SoundLocation<T>> {
    public final Sound sound;
    public final T location;
    public SoundRecording(Sound sound, T location) {
        this.sound = sound;
        this.location = location;
    }
}
