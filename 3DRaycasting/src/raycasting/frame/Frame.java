package raycasting.frame;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * @author Fabian
 */

@SuppressWarnings("serial")
public class Frame extends JPanel implements ActionListener {

	JFrame frame;

	private int[] tempwall;
	private ArrayList<int[]> walls;

	private double posX = 10, posY = 10;
	private double rotation = 90;
	private int FOV = 70;
	private int rays = 600;
	private int maxRayLength = 1000;
	private double[] rayLength;

	public int posMotion = 0;
	public int rotMotion = 0;

	public Frame() {
		frame = new JFrame("3DRaycasting");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setSize(1237, 650);
		frame.setVisible(true);
		frame.add(this);

		init();
	}

	Timer timer;

	private void init() {
		walls = new ArrayList<int[]>();
		walls.add(new int[] { -1, -1, 601, -1 });
		walls.add(new int[] { -1, 601, 601, 601 });
		walls.add(new int[] { -1, -1, -1, 601 });
		walls.add(new int[] { 601, -1, 601, 601 });

		rayLength = new double[rays];
		for (int i = 0; i < rays; i++) {
			rayLength[i] = maxRayLength;
		}

		mouseListener ml = new mouseListener();
		frame.addMouseListener(ml);
		frame.addMouseMotionListener(ml);
		frame.addKeyListener(new keyListener());

		timer = new Timer(1000 / 60, this);
		timer.start();
	}

	public Color rainbow(float offset, float f) {
		float hue = ((float) (System.nanoTime()) - offset) / (1.0E10F) % 8.0F;
		long color = Long.parseLong(Integer.toHexString(Integer.valueOf(Color.HSBtoRGB(hue, 1.0F, 1.001F)).intValue()),
				16);
		Color c = new Color((int) color);
		return new Color((c.getRed() / 255.0F) * f, (c.getGreen() / 255.0F) * f, (c.getBlue() / 255.0F) * f,
				c.getAlpha() / 255.0F);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2D = (Graphics2D) g;

		g2D.setColor(Color.BLACK);

		g2D.fillRect(10, 10, 601, 601);
		g2D.fillRect(620, 10, 600, 600);

		Color rainbow = rainbow(1111, 1);
		g2D.setColor(rainbow);
		g2D.setStroke(new BasicStroke(2));
		if (tempwall != null) {
			g2D.drawLine(10 + tempwall[0], 10 + tempwall[1], 10 + tempwall[2], 10 + tempwall[3]);
		}
		for (int[] wall : walls) {
			g2D.drawLine(10 + wall[0], 10 + wall[1], 10 + wall[2], 10 + wall[3]);
		}

		g2D.setColor(new Color(255, 255, 255, 50));
		for (int i = 0; i < rays; i++) {
			int x = (int) (Math.sin((rotation - FOV / 2 + FOV / (double) rays * i) / 180d * Math.PI) * rayLength[i]);
			int y = (int) (-Math.cos((rotation - FOV / 2 + FOV / (double) rays * i) / 180 * Math.PI) * rayLength[i]);
			g2D.drawLine(10 + (int) posX, 10 + (int) posY, 10 + (int) posX + x, 10 + (int) posY + y);
		}

		for (int i = 0; i < rays; i++) {
			double length = rayLength[i];
			length *= Math.cos((-FOV / 2 + FOV / (double) rays * i) / 180d * Math.PI);
			int height = 600;
			if (length != 0) {
				height = (int) (10000 / length);
			}
			if (height > 600) {
				height = 600;
			}
			double col = ((3 * maxRayLength / (double) length) + 32) / 255;
			if (col > 1) {
				col = 1;
			}
			if (col < 0) {
				col = 0;
			}
			g2D.setColor(new Color((int) (rainbow.getRed() * col), (int) (rainbow.getGreen() * col),
					(int) (rainbow.getBlue() * col)));
			g2D.fillRect(620 + 600 / rays * i, 310 - height / 2, 600 / rays, height);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		rotation = (rotation + rotMotion) % 360;
		posX += Math.sin(rotation / 180d * Math.PI) * posMotion;
		posY -= Math.cos(rotation / 180d * Math.PI) * posMotion;
		if (posX > 600) {
			posX = 600;
		}
		if (posY > 600) {
			posY = 600;
		}
		if (posX < 0) {
			posX = 0;
		}
		if (posY < 0) {
			posY = 0;
		}
		updateRays();
		repaint();
	}

	public void updateRays() {
		for (int i = 0; i < rays; i++) {
			double x1 = posX;
			double y1 = posY;
			double x2 = x1 + (Math.sin((rotation - FOV / 2 + FOV / (double) rays * i) / 180d * Math.PI) * maxRayLength);
			double y2 = y1 + (-Math.cos((rotation - FOV / 2 + FOV / (double) rays * i) / 180 * Math.PI) * maxRayLength);
			double length = maxRayLength;
			if (tempwall != null) {
				double[] lcp = lineCollisionPoint(tempwall[0], tempwall[1], tempwall[2], tempwall[3], x1, y1, x2, y2);
				if (lcp != null) {
					double l = distance(new double[] { x1, y1 }, lcp);
					if (l < length) {
						length = l;
					}
				}
			}
			for (int[] wall : walls) {
				double[] lcp = lineCollisionPoint(wall[0], wall[1], wall[2], wall[3], x1, y1, x2, y2);
				if (lcp != null) {
					double l = distance(new double[] { x1, y1 }, lcp);
					if (l < length) {
						length = l;
					}
				}
			}
			rayLength[i] = length;
		}
	}

	private double[] lineCollisionPoint(double x1, double y1, double x2, double y2, double x3, double y3, double x4,
			double y4) {
		if (((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1)) != 0
				&& ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1)) != 0) {
			double uA = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3))
					/ ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1));
			double uB = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3))
					/ ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1));
			if (uA >= 0 && uA <= 1 && uB >= 0 && uB <= 1) {
				return new double[] { x1 + (uA * (x2 - x1)), y1 + (uA * (y2 - y1)) };
			}
		}
		return null;
	}

	public double[] lineCollisionPoint(double[] line1, double[] line2) {
		return lineCollisionPoint(line1[0], line1[1], line1[2], line1[3], line2[0], line2[1], line2[2], line2[3]);
	}

	public double distance(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
	}

	public double distance(double[] point1, double[] point2) {
		return distance(point1[0], point1[1], point2[0], point2[1]);
	}

	private class keyListener implements KeyListener {
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_UP) {
				posMotion = 2;
			}
			if (e.getKeyCode() == KeyEvent.VK_DOWN) {
				posMotion = -2;
			}
			if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				rotMotion = 2;
			}
			if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				rotMotion = -2;
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_UP) {
				posMotion = 0;
			}
			if (e.getKeyCode() == KeyEvent.VK_DOWN) {
				posMotion = 0;
			}
			if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				rotMotion = 0;
			}
			if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				rotMotion = 0;
			}
		}

		@Override
		public void keyTyped(KeyEvent e) {
		}
	}

	private class mouseListener implements MouseListener, MouseMotionListener {
		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			int posX = e.getX() - 12;
			int posY = e.getY() - 36;
			if (posX >= 0 && posY >= 0 && posX <= 600 && posY <= 600) {
				tempwall = new int[] { posX, posY, posX, posY };
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (tempwall != null) {
				walls.add(tempwall);
				tempwall = null;
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (tempwall != null) {
				int posX = e.getX() - 12;
				int posY = e.getY() - 36;
				if (posX >= 0 && posY >= 0 && posX <= 600 && posY <= 600) {
					tempwall[2] = posX;
					tempwall[3] = posY;
				}
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
		}
	}
}
