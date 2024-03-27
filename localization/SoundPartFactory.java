package localization;

import analysis.Sound;

public interface SoundPartFactory {
    SoundPart CreateSoundPart(Sound sound);
}
