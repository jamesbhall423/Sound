package localization;

import analysis.Sound;

public interface SoundPart extends Sound {
    CorrelDetails startingCorrelDetails();
    CorrelDetails getCorrelation(Sound other);
}
