/*
 *  Copyright (c) 2011 Michael Zucchi
 *
 *  This file is part of ImageZ, a bitmap image editing appliction.
 *
 *  ImageZ is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  ImageZ is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with ImageZ.  If not, see <http://www.gnu.org/licenses/>.
 */
package imagez.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.colorchooser.DefaultColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class HSLBox extends JComponent implements MouseListener, MouseMotionListener, ChangeListener {

	final int size = 128;
	ColorSelectionModel selectionModel;
	BufferedImage img;
	static public final int MODE_HUE = 0;
	static public final int MODE_SATURATION = 1;
	static public final int MODE_LIGHTNESS = 2;
	int mode;
	float value = 1.0f;
	//
	float H;
	float S;
	float L;

	public HSLBox() {
		regenImage();

		setPreferredSize(new Dimension(size, size));
		setMinimumSize(new Dimension(size, size));

		selectionModel = new DefaultColorSelectionModel();

		selectionModel.addChangeListener(this);

		addMouseListener(this);
	}

	public ColorSelectionModel getSelectionModel() {
		return selectionModel;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// convert rgb to hsl
		Color c = selectionModel.getSelectedColor();
		float [] rgb = c.getColorComponents(null);

		float min = rgb[0];
		float max = min;

		min = Math.min(min, rgb[1]);
		max = Math.max(max, rgb[1]);
		min = Math.min(min, rgb[2]);
		max = Math.max(max, rgb[2]);

		float l = (max + min)/2;
		float h, s;

		if (min == max) {
			s = h = 0;
		} else {
			if (l < 0.5) {
				s = (max-min)/(max+min);
			} else {
				s = (max-min)/(2-max-min);
			}
			if (rgb[0] == max)
				h = (rgb[1] - rgb[2]) / ( max - min);
			else if (rgb[1] == max)
				h = 2.0f + (rgb[2] - rgb[0]) / (max - min);
			else
				h = 4.0f + (rgb[0] - rgb[1]) / (max - min);
		}

		setH(h);
		setS(s);
		setL(l);
	}

	public void setH(float H) {
		this.H = H;
		if (mode == MODE_HUE) {
			value = H;
			regenImage();
		} else
			repaint();
	}

	public void setL(float L) {
		this.L = L;
		if (mode == MODE_LIGHTNESS) {
			value = L;
			regenImage();
		} else
			repaint();
	}

	public void setS(float S) {
		this.S = S;
		if (mode == MODE_SATURATION) {
			value = S;
			regenImage();
		} else
			repaint();
	}

	void setXY(float x, float y) {
		float h, s, l;

		switch (mode) {
			case MODE_HUE:
				h = value;
				s = 1 - y;
				l = x;
				break;
			case MODE_SATURATION:
				h = x;
				s = value;
				l = y;
				break;
			case MODE_LIGHTNESS:
			default:
				h = y;
				s = x;
				l = value;
				break;
		}

		H = h;
		S = s;
		L = l;

		float[] rgb = new float[3];
		hsl2rgb(h, s, l, rgb);
		System.out.printf("hsl = %f,%f,%f  rgb = %f,%f,%f\n", h, s, l, rgb[0], rgb[1], rgb[2]);
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.drawImage(img, 0, 0, null);

		int x,y;
		switch (mode) {
			case MODE_HUE:
				y = (int) ((1 - S) * size);
				x = (int) (L * size);
				break;
			case MODE_SATURATION:
				x = (int) (H * size);
				y = (int) (L * size);
				break;
			case MODE_LIGHTNESS:
			default:
				y = (int) (H * size);
				x = (int) (S * size);
				break;
		}
		g.setXORMode(Color.white);
		g.drawLine(x, 0, x, size);
		g.drawLine(0, y, size, y);
	}
	float sat = 1f;

	float hue2rgb(float m1, float m2, float h) {
		if (h < 0) {
			h += 1;
		}
		if (h > 1) {
			h -= 1;
		}
		if (h * 6 < 1) {
			return m1 + (m2 - m1) * h * 6;
		}
		if (h * 2 < 1) {
			return m2;
		}
		if (h * 3 < 2) {
			return m1 + (m2 - m1) * (2 / 3f - h) * 6;
		}
		return m1;
	}

	void hsl2rgb(float h, float s, float l, float[] rgb) {
		float m2 = (l <= 0.5f) ? l * (s + 1) : (l + s - l * s);
		float m1 = l * 2 - m2;

		rgb[0] = hue2rgb(m1, m2, h + 1 / 3f);
		rgb[1] = hue2rgb(m1, m2, h);
		rgb[2] = hue2rgb(m1, m2, h - 1 / 3f);
	}

	void regenImage() {
		float[] rgbf = new float[3];

		img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);

		for (int j = 0; j < size; j++) {
			for (int i = 0; i < size; i++) {
				float h, l, s;

				switch (mode) {
					case MODE_HUE:
						h = value;
						s = 1 - j / (float) size;
						l = i / (float) size;
						break;
					case MODE_SATURATION:
						h = i / (float) size;
						s = value;
						l = j / (float) size;
						break;
					case MODE_LIGHTNESS:
					default:
						h = j / (float) size;
						s = i / (float) size;
						l = value;
						break;
				}

				hsl2rgb(h, s, l, rgbf);

				float r = rgbf[0];
				float g = rgbf[1];
				float b = rgbf[2];

				int rgb = ((int) (r * 255.0) << 16)
						| ((int) (g * 255.0) << 8)
						| ((int) (b * 255.0));

				img.setRGB(i, j, rgb);
			}
		}
		repaint();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		Point p = e.getPoint();
		float x = e.getX() / (float) size;
		float y = e.getY() / (float) size;

		setXY(x, y);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			mouseMoved(e);
			addMouseMotionListener(this);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			removeMouseMotionListener(this);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

}

/**
 *
 * @author notzed
 */
public class HSLChooser extends JPanel {

	HSLBox box;
	JSlider hslider;
	JSlider sslider;
	JSlider lslider;

	public HSLChooser() {
		box = new HSLBox();
		add(box);

		hslider = new JSlider(0, 1000, 0);
		add(hslider);
		sslider = new JSlider(0, 1000, 1000);
		add(sslider);
		lslider = new JSlider(0, 1000, 1000);
		add(lslider);

		hslider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				box.setH(((JSlider) e.getSource()).getValue() / 1000.0f);
			}
		});
		sslider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				box.setS(((JSlider) e.getSource()).getValue() / 1000.0f);
			}
		});
		lslider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				box.setL(((JSlider) e.getSource()).getValue() / 1000.0f);
			}
		});

	}

	public static void main(String[] args) {

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				JFrame jf = new JFrame("colour thing");

				jf.add(new HSLChooser());
				jf.pack();
				jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				jf.setVisible(true);
			}
		});
	}
}
