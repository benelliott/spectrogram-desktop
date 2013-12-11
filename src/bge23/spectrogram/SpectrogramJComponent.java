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
	private static final int width = 1024; //width of spectrogram component in pixels
	private static final int height = 768; //width of spectrogram component in pixels
	private double maxAmplitude = 1;//= 100000000000d; //largest value seen so far, scale colours accordingly
	
	private BufferedImage buffer;
	private BufferedImage bi;
	private Graphics2D g2buffer;
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
		buffer = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		bi = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		g2buffer = buffer.createGraphics();
		g2current = bi.createGraphics();
		g2buffer.setColor(Color.BLACK);
		g2buffer.clearRect(0,0,width,height);
		
		heatMap = new Color[256];
		for (int i = 0; i < 256; i++) {
			heatMap[i] = new Color(
					i, //red
					(int)(2*(127.5f-Math.abs(i-127.5f))), //green is 127.5 - |i-127.5| (draw it - peak at 127.5)
					255-i  //blue
					);
		}
		
		timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() { //timer executes 'step()' method on each window-length time interval, beginning immediately [0 delay]
				try{step();} catch (ArrayIndexOutOfBoundsException e) {cancel();}
			}
		}, 0, windowDuration);
		System.out.println("WINDOW DURATION " +windowDuration);
		
		//play wav file simultaneously with showing spectrogram:
		AudioInputStream a = AudioSystem.getAudioInputStream(new File(filepath));
		c = AudioSystem.getClip();
		c.open(a);
		c.start();
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
			int val = 255-cappedValue(spectroData[i]); //TODO: scale this properly!
			//g2current.setColor(new Color(val,val,val)); //greyscale
			g2current.setColor(heatMap[255-val]); //colour heat map
			g2current.fillRect(width-pixelWidth, (elements-i)*pixelHeight, pixelWidth, pixelHeight); //TODO: do right
		}

		g2buffer.drawImage(bi, 0, 0, width, height, null);

		System.out.println("Windows drawn: "+windowsDrawn);
		windowsDrawn++;
		repaint();
	}
	
	private int cappedValue(double d) {
		//return an integer capped at 255 representing the given double value
		double dAbs = Math.abs(d);
		if (dAbs > maxAmplitude) return 255;
		if (dAbs < 1) return 0;
		double ml = Math.log1p(maxAmplitude);
		double dl = Math.log1p(dAbs);
		return (int)(dl*255/ml); //decibel is a log scale, want something linear
		//return (int) (dAbs*255/maxAmplitude); 
		
		
	}
	
	public void scroll(int offset) {
		try {
			synchronized(timer){
				timer.wait(); //stop the timer from drawing
				c.stop(); //stop the audio -- this does not restart it
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//TODO: reset display properly - this still doesn't work
		int oldWindowsDrawn = windowsDrawn;
		int currentLeftmostWindow = windowsDrawn-(width/pixelWidth);
		final int newLeftmostWindow = currentLeftmostWindow + offset;
		windowsDrawn = newLeftmostWindow;
		Timer timer2 = new Timer();
		timer2.schedule(new TimerTask() {
			public void run() { //timer executes 'step()' method on each window-length time interval, beginning immediately [0 delay]
				try {
					if (windowsDrawn < newLeftmostWindow+(width/pixelWidth)) step();
					else cancel();
				} catch (ArrayIndexOutOfBoundsException e) {
					cancel();
				}
			}
		}, 0, 10); //TODO: choose a good value for '10'
	}
	
	public void resume() {
		synchronized(timer){
			timer.notify(); 
			c.start(); //stop the audio
		}
	}

	public void paintComponent(Graphics g) {
		g.drawImage(buffer, 0, 0, null);
	}


}
