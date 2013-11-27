package bge23.spectrogram;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class OtherPersonsCode extends JComponent {

    // private static final int W = 200;
    // private static final int H = 150;
    private static final int W = 640;
    private static final int H = 480;
    private static final int REFRESH_INTERVAL = 50; // ms
    private static final int STEP = 1;
    private int position = 0;
    private BufferedImage buffer;
    private BufferedImage img1;
    private BufferedImage img2;
    private Graphics2D g2buffer;
    private Graphics2D g2img1;
    private Graphics2D g2img2;
    private Graphics2D g2current;
    private Random random;
    private Color[] colors;

    public OtherPersonsCode() {
        random = new Random();
        buffer = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        img1 = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        img2 = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        g2buffer = buffer.createGraphics();
        g2img1 = img1.createGraphics();
        g2img2 = img2.createGraphics();
        g2current = g2img1;
        g2buffer.setColor(Color.BLACK);
        g2buffer.clearRect(0, 0, W, H);
        colors = new Color[256];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = new Color((i*i)%150, (i*i)%25,(i*i)%25); // grayscale
        }
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                step();
            }
        }, 0, REFRESH_INTERVAL);
    }

    public void paintComponent(Graphics g) {
        g.drawImage(buffer, 0, 0, null);
    }

    // executes on the timer thread
    public void step() {
        position = (position + STEP) % W;
        if (position == 0) {
            System.out.println("swap");
            g2current = (g2current == g2img1) ? g2img2 : g2img1;
        }

        // no need to clear since everything is covered with opaque color
        for (int i = 0; i < W / STEP; i++) {
            g2current.setColor(colors[random.nextInt(256)]);
            g2current.fillRect(position, i * STEP, STEP, STEP);
        }

        if (g2current == g2img1) {
            g2buffer.drawImage(img1, W - position, 0, null);
            g2buffer.drawImage(img2, -position, 0, null);
        } else {
            g2buffer.drawImage(img2, W - position, 0, null);
            g2buffer.drawImage(img1, -position, 0, null);
        }

        // paintComponent will be called on the EDT (Event Dispatch Thread)
        repaint();
    }

    public static void dmain(String[] args) {
        final JPanel panel = new JPanel(new BorderLayout());
        OtherPersonsCode spectogram = new OtherPersonsCode();
        spectogram.setPreferredSize(new Dimension(W, H));
        panel.add(spectogram, BorderLayout.CENTER);
        final JFrame frame = new JFrame("Scroller");
        frame.getContentPane().add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
