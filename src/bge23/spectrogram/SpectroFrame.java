package bge23.spectrogram;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class SpectroFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private final JButton libButton;
	private final JButton settingsButton;
	private final DragPanel dp;

	public SpectroFrame(String filepath) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		setTitle("Spectrogram");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		libButton = new JButton("Library");
		gbc.anchor = GridBagConstraints.NORTHWEST;
		panel.add(libButton, gbc);
		
		settingsButton = new JButton("Settings");
		gbc.anchor = GridBagConstraints.NORTHEAST;
		panel.add(settingsButton, gbc);

		dp = new DragPanel(filepath);
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.SOUTH;
		panel.add(dp, gbc);
		
		getContentPane().add(panel);
		
		pack();
	}

	public static void main(String[] args) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		String filepath = "C:\\Users\\Ben\\lapwing.wav";
		SpectroFrame s = new SpectroFrame(filepath);
		s.setVisible(true);
	}
}
