package bge23.spectrogram;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
import javax.swing.JFrame;
import javax.swing.JPanel;

public class SpectrogramJComponent extends JComponent {

	private static final long serialVersionUID = 1L;
	private static final int width = 1024; //width of spectrogram component in pixels
	private static final int height = 768; //width of spectrogram component in pixels
	private BufferedImage buffer;
	private BufferedImage bi;
	private Graphics2D g2buffer;
	private Graphics2D g2current; //current buffer to display;
	private Spectrogram spec;
	private int windowDuration; //draw a new window in time with the audio file
	private int windowsDrawn = 0; //how many windows have been drawn already
	private int pixelWidth = 4;

	public SpectrogramJComponent(String filepath){
		spec = new Spectrogram(filepath);
		windowDuration = spec.getWindowDuration();
		System.out.println("Number of windows in input: "+spec.getWindowsInFile());
		buffer = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		bi = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		g2buffer = buffer.createGraphics();
		g2current = bi.createGraphics();
		g2buffer.setColor(Color.BLACK);
		g2buffer.clearRect(0,0,width,height);

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() { //timer executes 'step()' method on each window-length time interval, beginning immediately [0 delay]
				try{step();} catch (ArrayIndexOutOfBoundsException e) {cancel();}
			}
		}, 0, windowDuration);
		System.out.println("WINDOW DURATION " +windowDuration);
	}

	public void step() { //'public' to allow access from timer thread



		double[] spectroData = spec.getSpectrogramWindow(windowsDrawn); //remember that this array is only half full, as required by JTransforms
		int elements = spectroData.length/2;
		int pixelHeight = (height/elements > 1) ? height/elements : 1;

		BufferedImage shifted = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		Graphics2D g2dshifted = shifted.createGraphics();
		g2dshifted.drawImage(bi, -pixelWidth, 0, null);
		g2current.drawImage(shifted, 0, 0, width, height, null);
		
		
		for (int i = elements-1; i >= 0; i--) {
			int val = (int)(255-(spectroData[i]/500000d)%255); //TODO: scale this properly!
			g2current.setColor(new Color(val,val,val));
			g2current.fillRect(width-pixelWidth, (elements-1-i)*pixelHeight, pixelWidth, pixelHeight); 
		}


		g2buffer.drawImage(bi, 0, 0, width, height, null);

		System.out.println("Windows drawn: "+windowsDrawn);
		windowsDrawn++;
		repaint();

	}

	public void paintComponent(Graphics g) {
		g.drawImage(buffer, 0, 0, null);
	}

	public static void main(String[] args) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		String filepath = "C:\\Users\\Ben\\Downloads\\cuckoo.wav";

		SpectrogramJComponent s = new SpectrogramJComponent(filepath);  //get data from specified file

		final JPanel panel = new JPanel(new BorderLayout()); //container for SpectrogramJComponent

		s.setPreferredSize(new Dimension(width, height));
		panel.add(s, BorderLayout.CENTER);

		final JFrame frame = new JFrame("Spectrogram"); //create window to hold everything
		frame.getContentPane().add(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack(); //scale window to fit subcomponents
		frame.setLocationRelativeTo(null);

		//play wav file simultaneously with showing spectrogram:
		AudioInputStream a = AudioSystem.getAudioInputStream(new File(filepath));
		Clip c = AudioSystem.getClip();
		c.open(a);


		frame.setVisible(true);
		c.start();
	}

}
