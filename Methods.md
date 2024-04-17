# The following are useful methods.

## Determining the correlations for two sources at different times.
* For harmonic sounds and explosive sounds phase information is likely useless, and the timing of the amplitude determines better the timing of the sound. For noise-like sounds it is probably useful, but these sounds tend to blend into each other.

* The following was an idea to calculate phase correlation. It failed to determine the timing on single-frequency sounds. It has not been tested on explosive or noise-like sounds.
* Use the frequency bandwith 20-1280 (64 samples over 1/20 second)(sample possibly larger, but use same frequency distribution)
* Wtih 64 samples, this method is probably overkill.
* Take the fourier transforms of sources A and B. Let the amplitude of the frequencies of C equal the smaller of the two amplitudes, and let the phase angles of C be equal to the difference of the phase angles of A and B.
* Perform an inverse fourier transform of C. The loudness at each point in time of C is a measure of the correlation at that time difference (a-b) and (a-b-1/20/sec).


## Breaking sounds into sound parts
* Three types of sound parts, pitch, bump, and diffuse.
* Pitch represents a frequency for a period of time.
* Bump represents a swing in pressure
* Diffuse represents non-repetitive sound in a specific frequency band
* Listen for Pitches, bumps, and diffuse sounds above a specific loudness level.

## Pitch Definition Idea
* A Pitch has the following properties
* Fundamental frequency (use start frequency and then use numbers representing the change of frequency)
* A start and length
* Amplitudes at different points along the length
* A phase
* Overtone multiples and phases (Set of numbers)
* The overtones themselves may modify over the course of the sound. So, 2D array?
* Unsure how to calculate at this point.

## Audio Color Image Manufacturing
* Proposed color scheme
* Hue = Phase (From start of picture)
* Luminosity = Amplitude
* Saturation = Signal Clarity (Interference with nearby frequencies) (plus or minus one inverse sampling interval)
* Available space: height 600, width 1200
* Use freq under 5000 hz for picture
* Time resolution 0.005s (200 pixels / second)
* Sampling Interval: 0.02s
* Frequency Interval: (5x10hz)
### Efficiently adding inbetween time and frequency intervals
* Data for in-betwwen time interval is straightforward. Use FFT for in-between time interval.
* Data for in-between frequency: shift values by the given frequency shift (10hz). This will provide values for 10hz, 60hz, ect.
* All phases will have to be adjusted to fit the start of the image, not the start of the sampling interval.

## Sound localization without sound identification

* Identifying sounds is a difficult matter. Not only would seperating the sounds be dificult on the computer, it would be difficult for the user to specify the sounds that they are looking for.
* Therefore, it is better for the user to search by location, rather than by sound.
* The function, then, would take in the estimated synchronized sounds, and the distance from the point in question to each one.
* Each sound should have a main freq-time-phase image, and a smeared one.
* The smeared image should be smeared by frequency 1-2%, the new value being the maximum absolute amplitude in that range.
* To assemble the result, take two images. One is the main image of the closest source, the other is are is the average of the images of the other recordings, adjusted for magnitude (inverse of distance), and time.
* If the value on the main recording is larger than that predicted by the other recordings, the value is reduced to the predicted value. 