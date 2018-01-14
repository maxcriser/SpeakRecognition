/*
*      _______                       _____   _____ _____  
*     |__   __|                     |  __ \ / ____|  __ \ 
*        | | __ _ _ __ ___  ___  ___| |  | | (___ | |__) |
*        | |/ _` | '__/ __|/ _ \/ __| |  | |\___ \|  ___/ 
*        | | (_| | |  \__ \ (_) \__ \ |__| |____) | |     
*        |_|\__,_|_|  |___/\___/|___/_____/|_____/|_|     
*                                                         
* -------------------------------------------------------------
*
* TarsosDSP is developed by Joren Six at IPEM, University Ghent
*  
* -------------------------------------------------------------
*
*  Info: http://0110.be/tag/TarsosDSP
*  Github: https://github.com/JorenSix/TarsosDSP
*  Releases: http://0110.be/releases/TarsosDSP/
*  
*  TarsosDSP includes modified source code by various authors,
*  for credits and info, see README.
* 
*/


package com.example.mvmax.speakrecognition.speakrecognition;

public class GainProcessor implements AudioProcessor {
	private double gain;
	
	public GainProcessor(double newGain) {
		setGain(newGain);
	}

	public void setGain(double newGain) {
		this.gain = newGain;
	}

	@Override
	public boolean process(AudioEvent audioEvent) {
		float[] audioFloatBuffer = audioEvent.getFloatBuffer();
		for (int i = audioEvent.getOverlap(); i < audioFloatBuffer.length ; i++) {
			float newValue = (float) (audioFloatBuffer[i] * gain);
			if(newValue > 1.0f) {
				newValue = 1.0f;
			} else if(newValue < -1.0f) {
				newValue = -1.0f;
			}
			audioFloatBuffer[i] = newValue;
		}
		return true;
	}
	
	@Override
	public void processingFinished() {
		// NOOP
	}
}
