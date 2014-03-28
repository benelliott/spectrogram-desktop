package bge23.spectrogram;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JComponent;

public class SpectrogramJComponent extends JComponent {

	private static final long serialVersionUID = 1L;
	private static final int width = 800; //width of spectrogram component in pixels
	private static final int height = 704; //width of spectrogram component in pixels
	private double maxAmplitude = 1;//= 100000000000d; //largest value seen so far, scale colours accordingly
	private int contrast = 3;
	private BufferedImage bi;
	private Graphics2D g2current; //current buffer to display;
	private Spectrogram spec;
	private int windowDuration; //draw a new window in time with the audio file
	private int windowsDrawn = 0; //how many windows have been drawn already
	private int pixelWidth;
	private Timer timer;
	private Clip c;
	private Color[] heatMap;

	public SpectrogramJComponent(String filepath, int pixelWidth) throws UnsupportedAudioFileException, IOException, LineUnavailableException{
		this.pixelWidth = pixelWidth;
		spec = new Spectrogram(filepath);
		windowDuration = spec.getWindowDuration();
		System.out.println("Number of windows in input: "+spec.getWindowsInFile());
		bi = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		g2current = bi.createGraphics();
		
		heatMap = new Color[256];
		for (int i = 0; i < 256; i++) {
			heatMap[i] = new Color(
					i, //red
					(int)(2*(127.5f-Math.abs(i-127.5f))), //green is 127.5 - |i-127.5| (draw it - peak at 127.5)
					255-i  //blue
				);
		}
		
//		for (int i = 0; i < 256; i++) {
//			if (0 <= i && i <= 31) heatMap[i] = new Color(255,255,204);
//			if (32 <= i && i <= 63) heatMap[i] = new Color(255,237,160);
//			if (64 <= i && i <= 95) heatMap[i] = new Color(254,217,118);
//			if (96 <= i && i <= 127) heatMap[i] = new Color(254,178,76);
//			if (128 <= i && i <= 159) heatMap[i] = new Color(253,141,60);
//			if (160 <= i && i <= 191) heatMap[i] = new Color(252,78,42);
//			if (192 <= i && i <= 223) heatMap[i] = new Color(227,26,28);
//			if (224 <= i && i <= 255) heatMap[i] = new Color(177,0,38);
//
//		}
		
		timer = new Timer();

		System.out.println("WINDOW DURATION " +windowDuration);
		
		//play wav file simultaneously with showing spectrogram:
		AudioInputStream a = AudioSystem.getAudioInputStream(new File(filepath));
		c = AudioSystem.getClip();
		c.open(a);
		c.start();
		
		timer.schedule(new TimerTask() {
			public void run() { //timer executes 'step()' method on each window-length time interval, beginning immediately [0 delay]
				try{step();} catch (ArrayIndexOutOfBoundsException e) {
					cancel();
					}
			}
		}, 0, windowDuration);
	}
	
	public void step() { //'public' to allow access from timer thread
		double[] spectroData = spec.getCompositeWindow(windowsDrawn); //remember that this array is only half full, as required by JTransforms
		int elements = spectroData.length/2;
		int pixelHeight = (height/elements > 1) ? height/elements : 1;

		BufferedImage shifted = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		Graphics2D g2dshifted = shifted.createGraphics();
		g2dshifted.drawImage(bi, -pixelWidth, 0, null);
		g2current.drawImage(shifted, 0, 0, width, height, null);
		
		for (int i = elements-1; i >= 0; i--) {
			if (maxAmplitude < spectroData[i]) maxAmplitude = spectroData[i];
			int val = 255-cappedValue(spectroData[i]); 
			//g2current.setColor(new Color(val,val,val)); //greyscale
			g2current.setColor(heatMap[255-val]); //colour heat map
			g2current.fillRect((width-pixelWidth), (elements-i-1)*pixelHeight, pixelWidth, pixelHeight);
		}

		repaint();
		System.out.println("Windows drawn: "+windowsDrawn);
		windowsDrawn++;
	}
	
	private int cappedValue(double d) {
		/*
		 * Returns an integer capped at 255 representing the magnitude of the
		 * given double value, d, relative to the highest amplitude seen so far. The amplitude values
		 * provided use a logarithmic scale but this method converts these back to a linear scale, 
		 * more appropriate for pixel colouring.
		 */
		if (d < 0) return 0;
		if (d > maxAmplitude) {
			maxAmplitude = d;
			return 255;
		}
		return (int)(255*Math.pow((Math.log1p(d)/Math.log1p(maxAmplitude)),contrast));
	}

	public void paintComponent(Graphics g) {
		g.drawImage(bi, 0, 0, null);
	}


}
