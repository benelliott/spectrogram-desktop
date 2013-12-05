package bge23.spectrogram;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class DragPanel extends JPanel implements MouseMotionListener {

	private static final long serialVersionUID = 1L;
	private static int width = 1024;
	private static int height = 768;
	private static int pixelsPerWindow = 4;

	
	private static SpectrogramJComponent sjc;
	private Point dragStartPt;
	private int windowChange;
	
	public DragPanel(String filepath) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		sjc = new SpectrogramJComponent(filepath, pixelsPerWindow);  //get data from specified file
		sjc.setPreferredSize(new Dimension(width, height));
		add(sjc, BorderLayout.CENTER);
		sjc.addMouseListener(new MouseAdapter(){
			@Override
			public void mousePressed(MouseEvent e) {
				System.out.println("pressed");
				dragStartPt = e.getPoint();
				repaint();
			}
			
			public void mouseReleased(MouseEvent e) {
				System.out.println("released");
				Point dragEndPt = e.getPoint();
				int horizontalMov = e.getX() - dragStartPt.x;
				windowChange = horizontalMov/pixelsPerWindow;
				sjc.scroll(windowChange);
			}
		});
		sjc.addMouseMotionListener(this);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		System.out.println("dragged");
		int horizontalMov = e.getX() - dragStartPt.x;
		windowChange = horizontalMov/pixelsPerWindow;
		sjc.scroll(windowChange);
	}
	


	@Override
	public void mouseMoved(MouseEvent e) {
		// Auto-generated method stub

	}

}
