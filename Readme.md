# README

## Description
* The purpose of this project was to compare the timing of a sound on multiple recordings to figure out where the sound is coming from.
* Timing the difference in time between two sounds is a relatively easy task.
* Identifying sounds to be compared, however, is extremely difficult. In real scenarios there will be many sounds, and the user may be interested in any one of them.
* This project is still in progress.

## Sound Graph
* This project includes SoundGraph.bat, a (Windows) program to graph the properties of a sound (wav) file.
* The program will ask for the name of the sound file, and the time, in seconds, from which the graph is to be displayed.
* The sound file can be either in this directory, or via  an absolute path.
* The graph will display a 5 second interval from the time specified.
* The graph represent the freq-time domain of the sound, frequency being on the y axis and time on the x axis, and the brightness of each pixel representing the loudness of the sound at each frequency and time.
* A sound with a well-defined frequency will have a well defined color.
* The color of each pixel is determined by the phase of the sound. That is, a sound slightly higher in frequency than the one given by the line will progress from red to green to blue to red while a sound slightly lower in frequency will progress from red to blue to green to red, and a sound at the same frequency will maintain the same color.