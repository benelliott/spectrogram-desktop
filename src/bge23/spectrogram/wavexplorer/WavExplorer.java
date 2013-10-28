package bge23.spectrogram.wavexplorer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/*
 *  A class for extracting information from a .wav file based on
 *  information in its header. It also provides a means for extracting
 *  the samples as an array.
 * 
 */

public class WavExplorer {
	private RandomAccessFile wavFile; //file to open
	private long fileLength; //length of whole file in bytes
	private int dataLength; //number of bytes in data section
	private short numChannels; //number of channels in the file
	private short sampleRate; //sampling frequency Fs used to encode the file
	private int bitsPerSample; //number of bits used to hold each sample
	private int numSamples; //number of samples in the file
	private int[] firstChannelArray; //array of samples from first (left) channel
	private int[] secondChannelArray; //array of samples from second (right) channel, if it exists
	private boolean isMono; // true if there is only one channel, i.e. signal is mono, not stereo
	
	public WavExplorer(String filepath) {
		try {
			wavFile = new RandomAccessFile(filepath, "r");
			
			fileLength = wavFile.length();
			
			wavFile.seek(22);
			numChannels = wavFile.readShort();
			numChannels = Short.reverseBytes(numChannels); //must reverse bits since little-endian
			
			sampleRate = wavFile.readShort(); //at offset 24 so no seeking necessary
			sampleRate = Short.reverseBytes(sampleRate); //little-endian
			
			wavFile.seek(34);
			bitsPerSample = wavFile.readByte();
			if (bitsPerSample > 32) {
				System.err.println("Bitrate of "+bitsPerSample+" not supported.");
				throw new IOException();
			}
			
			wavFile.seek(40);
			dataLength = wavFile.readInt();
			dataLength = Integer.reverseBytes(dataLength); //little-endian
			
			numSamples = (8*dataLength)/(numChannels*bitsPerSample);
			firstChannelArray = new int[numSamples];

			if (numChannels == 1) { //mono, only one channel of samples
				isMono = true;

				for (int i = 0; i < numSamples; i++) {
					//wavFile already at offset 44, no need to seek
					if (bitsPerSample == 8) firstChannelArray[i] = wavFile.readByte();
					if (bitsPerSample == 16) firstChannelArray[i] = Short.reverseBytes(wavFile.readShort());
					if (bitsPerSample == 32) firstChannelArray[i] = Integer.reverseBytes(wavFile.readInt());
				}
				
			}
			else {
				if (numChannels == 2) { //stereo, two channels of samples
					isMono = false;
					secondChannelArray = new int [numSamples];
					for (int i = 0; i < numSamples; i++) {
						//wavFile already at offset 44, no need to seek
						if (bitsPerSample == 8) {
							firstChannelArray[i] = wavFile.readByte();
							secondChannelArray[i] = wavFile.readByte();
						}
						if (bitsPerSample == 16) {
							firstChannelArray[i] = Short.reverseBytes(wavFile.readShort());
							secondChannelArray[i] = Short.reverseBytes(wavFile.readShort());
						}
						if (bitsPerSample == 32) {
							firstChannelArray[i] = Integer.reverseBytes(wavFile.readInt());
							secondChannelArray[i] = Integer.reverseBytes(wavFile.readInt());
						}
					}
				}
				else throw new IOException();
			}
			wavFile.close();
		} catch (FileNotFoundException e) {
			System.err.println("Couldn't find file!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public int[] getFirstChannelData() { //get a copy of the first channel data
		int length = firstChannelArray.length;
		int toReturn[] = new int[length];
		for (int i = 0; i < length; i++) {
			toReturn[i] = firstChannelArray[i];
		}
		return toReturn;
	}
	
	public int[] getSecondChannelData() { //get a copy of the second channel data
		if (!isMono) {
			int length = secondChannelArray.length;
			int toReturn[] = new int[length];
			for (int i = 0; i < length; i++) {
				toReturn[i] = secondChannelArray[i];
			}
			return toReturn;
		}
		else {
			System.err.println("File is not stereo; no second channel available.");
			return new int[0]; //TODO is this bad?
		}
	}
	
	public long getFileLength() {
		return fileLength;
	}	
	
	public int getDataLength() {
		return dataLength;
	}
	
	public int getSampleRate() {
		return sampleRate;
	}
	
	public int getNumSamples() {
		return numSamples;
	}
	
	public int getNumChannels() {
		return numChannels;
	}
	
	public int getBitsPerSample() {
		return bitsPerSample;
	}
	
	public static void main(String[] args) {
		if (args.length != 1 || !(args[0].endsWith(".wav"))) {
			System.err.println("Please provide path to .wav file.");
			return;
		}
		WavExplorer we = new WavExplorer(args[0]);
		System.out.println(we.getSampleRate());
		System.out.println(we.getNumSamples());
		System.out.println(we.getNumChannels());
		System.out.println(we.getBitsPerSample());
		int[] dataArray = we.getFirstChannelData();
		for (int i = 1000; i < 1005; i++) {
			System.out.println(dataArray[i]);
		}

	}

}
