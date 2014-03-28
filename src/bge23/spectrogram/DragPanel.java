package bge23.spectrogram;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JPanel;

public class DragPanel extends JPanel implements MouseMotionListener {

	private static final long serialVersionUID = 1L;
	private static int width = 800;
	private static int height = 704;
	private static int pixelsPerWindow = 2;
	private static SpectrogramJComponent sjc;

	
	public DragPanel(String filepath) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		sjc = new SpectrogramJComponent(filepath, pixelsPerWindow);  //get data from specified file
		sjc.setPreferredSize(new Dimension(width, height));
		add(sjc, BorderLayout.CENTER);
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		
	}

}
