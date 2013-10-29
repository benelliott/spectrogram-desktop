package bge23.spectrogram;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

/* From documentation:
 * 
 * 
public void realForward(double[] a)
Computes 1D forward DFT of real data leaving the result in a . The physical layout of the output data is as follows:
if n is even then
 a[2*k] = Re[k], 0<=k<n/2
 a[2*k+1] = Im[k], 0<k<n/2
 a[1] = Re[n/2]

if n is odd then
 a[2*k] = Re[k], 0<=k<(n+1)/2
 a[2*k+1] = Im[k], 0<k<(n-1)/2
 a[1] = Im[(n-1)/2]

This method computes only half of the elements of the real transform.
 The other half satisfies the symmetry condition. 
 If you want the full real forward transform, use realForwardFull.
  To get back the original data, use realInverse on the output of this method.

 */

public class FFTExperiments {
	public static void main(String[] args) {
		FFTExperiments f = new FFTExperiments();
		f.testWAV(args[0]);
	}
	
	private void testWAV(String filepath) {
		WAVExplorer w = new WAVExplorer(filepath);
		if (w.isMono()) {
			double[] unspacedSamples = w.getFirstChannelData();
			double[] spacedSamples = lengthenSampleArray(unspacedSamples);
			System.out.println("Time domain: ");
			printChart(spacedSamples, 30);
			
			DoubleFFT_1D d = new DoubleFFT_1D(unspacedSamples.length); //initialise with n, where n = data size
			d.realForward(spacedSamples);

			System.out.println();
			System.out.println("Frequency domain: ");
			printChart(spacedSamples, 30);
		}
	}
	
	private double[] lengthenSampleArray(double[] unlengthenedArray) {
		//returns an array of double the length of unlengthenedArray, with the second half zero-padded to
		//use in the FFT
		double[] toReturn = new double[2*unlengthenedArray.length];
		for (int i = 0; i < unlengthenedArray.length; i++) {
			toReturn[i] = unlengthenedArray[i]; //Java arrays initialised to 0 by default
		}
		return toReturn;
	}
	
	@SuppressWarnings("unused")
	private void testManualSamples() {
		double[] samples = new double[20]; //input to FFT must be twice data size

		//see DSP slide 79
		// (sin-like wave)
		samples[0] = 1;
		samples[1] = 0.66;
		samples[2] = 0;
		samples[3] = -0.66;
		samples[4] = -1;
		samples[5] = -0.66;
		samples[6] = 0;
		samples[7] = 0.66;
		samples[8] = 1;
		samples[9] = 0.66;
		
		
		/* Dirac comb
		samples[0] = 1000000;
		samples[2] = 1000000;
		samples[4] = 1000000;
		samples[6] = 1000000;
		samples[8] = 1000000;
		*/

		System.out.println("Time domain:");
		for (int i = 0; i < samples.length/2; i++) {
			System.out.print("  "+samples[i]);
		}
		
		printChart(samples, samples.length/2);

		DoubleFFT_1D d = new DoubleFFT_1D(10); //initialise with n, where n = data size
		d.realForward(samples);

		System.out.println();
		System.out.println("Frequency domain:");
		for (int i = 0; i < samples.length/2; i++) {
			System.out.print("  "+samples[i]);
		}
		
		printChart(samples, samples.length/2);
	}

	private void printChart(double[] samples, int limit) {
		//Attempt at a rough ASCII chart
		
		//find min and max values in array
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (int i = 0; i < limit; i++) {
			if (samples[i] > max) max = samples[i];
			if (samples[i] < min) min = samples[i];
		}
		
		//use min and max to get acceptable 'quartile values'
		double firstQuartile = min+((max-min)/4);
		double secondQuartile = min+((max-min)/2);
		double thirdQuartile = min+3*((max-min)/4);
		
		System.out.println(); System.out.println();
		System.out.println("Minimum: "+min+", first quartile: "+firstQuartile+", second quartile: "+secondQuartile+", third quartile: "+thirdQuartile+", Maximum: "+max);
		
		System.out.println();
		for (int i = 0; i < limit; i++) {
			if (samples[i] >= thirdQuartile) System.out.print(" |");
			else System.out.print("  ");
		}
		
		System.out.println();

		for (int i = 0; i < limit; i++) {
			if (samples[i] >= secondQuartile) System.out.print(" |");
			else System.out.print("  ");
		}
		
		System.out.println();
		
		for (int i = 0; i < limit; i++) {
			if (samples[i] <= secondQuartile) System.out.print(" |");
			else System.out.print("  ");
		}
		
		System.out.println();


		for (int i = 0; i < limit; i++) {
			if (samples[i] <= firstQuartile) System.out.print(" |");
			else System.out.print("  ");
		}
		
		System.out.println();

	}

}

