package bge23.spectrogram;

import java.util.ArrayList;

//TODO: find good values for sliceDurationLimit, windowSize, overlap

public class Spectrogram{
	private ArrayList<double[][]> audioSlices; //List of 2D arrays of input data with syntax slice[window number][window element]
	private boolean finalSliceIncomplete = false;
	private boolean finalWindowIncomplete = false;
	private int finalSliceLength;
	private int finalWindowLength;
	private int sampleRate;
	private int bitsPerSample;
	private int windowSize = 20; //window size in miliseconds. Window size will decide the index range for the arrays. TODO: decide an appropriate size
	private ArrayList<double[][]> spectroSlices; //List of 2D arrays of output data, e.g. temp = slices.getFirst(); temp[time][freq]
	private int sliceDurationLimit = 6000; //limit of slice duration in miliseconds  -- TODO: decide an appropriate size
	private int audioSliceSizeLimit = (sliceDurationLimit/1000)*sampleRate*bitsPerSample; //limit to audio slice size in 
	private SpectrogramJComponent sjc;

	
	public Spectrogram() {
	
	}
	
	public Spectrogram(String filepath) {
		getDataFromWAV(filepath);
	}
	
	public static void main(String[] args) {
		Spectrogram s = new Spectrogram();
		
	}
	
	private void getDataFromWAV(String filepath) { //fills audioSlices list with 
		finalSliceIncomplete = false;
		finalWindowIncomplete = false;
		//TODO: work with stereo input
		WAVExplorer w = new WAVExplorer(filepath);
		double[] firstChannelData = w.getFirstChannelData();
		int duration = w.getDuration();
		sampleRate = w.getSampleRate();
		bitsPerSample = w.getBitsPerSample();
		System.out.println("Audio slice size limit: "+audioSliceSizeLimit); //TODO: remove eventually
		int windowsPerSlice = sliceDurationLimit/windowSize; //no. windows in slice = slice duration / window duration
		int elementsPerWindow = audioSliceSizeLimit/(sliceDurationLimit/windowSize); //no. elements in window = slice size (bytes) / no. windows
		int i = 0;
		while (i*audioSliceSizeLimit < firstChannelData.length) { //takes care of all full slices
			//want to add an array to ArrayList each time we fill one minute's worth
			double[][] singleSlice = new double[windowsPerSlice][elementsPerWindow]; 
			//TODO: allow for overlaps - what is a good overlap size?
			for (int j = 0; j < singleSlice.length; j++) { //all of this is really inefficient!
				for (int k = 0; k < singleSlice[0].length; k++) {
					singleSlice[j][k] = firstChannelData[i*audioSliceSizeLimit+j*elementsPerWindow+k];
				}
			}
			audioSlices.add(singleSlice);
			i++;
		}
		
		//Now deal with the remaining data that is not enough to fill a slice
		int capturedData = (i-1)*audioSliceSizeLimit;
		finalSliceLength = firstChannelData.length - capturedData;
		if  (finalSliceLength != 0) {
			finalSliceIncomplete = true;
			int remainingFullWindows = (int) Math.floor(finalSliceLength/windowSize);
			int finalWindowLength = finalSliceLength-remainingFullWindows;
			double[][] finalSlice = new double[remainingFullWindows+1][elementsPerWindow];
			if (finalWindowLength == 0) finalSlice = new double[remainingFullWindows][elementsPerWindow]; //don't add room for unfinished window if there aren't any
			for (int j = 0; j < remainingFullWindows; j++) {
				for (int k = 0; k < elementsPerWindow; k++) {
				finalSlice[j][k] = firstChannelData[capturedData+j*elementsPerWindow+k];
				}
			}
				
			//Now deal with the remaining data that is not enough to fill a window
				
			if (finalWindowLength != 0) {
				for (int k = 0; k < finalWindowLength; k++) {
					finalSlice[remainingFullWindows][k] = firstChannelData[capturedData+remainingFullWindows*elementsPerWindow+k];
				}
				finalWindowIncomplete = true;
			}
			
			audioSlices.add(finalSlice);
		}
		
	}
	
	//TODO: remember that last 'slice' in list may only be part-full
	
	private void fillSpectro() {
		for (double[][] slice : audioSlices) {
			double[][] spectroSlice = new double[slice.length][slice[0].length]; //JTransforms requires that input arrays be padded with as many zeros as there are samples. TODO: check that there are always the same number of samples
			for (int window = 0; window < slice.length; window++) {
				for (int i = 0; i < slice[window].length; i++) {
					spectroSlice[window][i] = slice[window][i];
				}
				spectroTransform(spectroSlice[window]); //store the STFT of the window in the same array once the samples have been populated 
			}
			spectroSlices.add(spectroSlice); //add the transformed slice to the list
		}
	}
 	
 	
 	private void spectroTransform(double[] paddedSamples) { //calculate the squared STFT of the provided time-domain samples
 		
 	}
 	
	private void getNextDrawableChunk() {
		//TODO -- allows SpectrogramComponent to ask for next data to draw
	}
	
	private void getDrawableChunk(int time) {
		//TODO
	}
	

}
