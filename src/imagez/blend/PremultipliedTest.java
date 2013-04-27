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
package imagez.blend;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author notzed
 */
public class PremultipliedTest {
	static void doshit(BufferedImage img, Paint p) {
		Graphics2D gg = img.createGraphics();

		gg.setPaint(p);
		gg.translate(128, 128);
		gg.fillOval(-100, -100, 200, 200);

		gg.dispose();

		int[] data = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();

		for (int i=0;i<data.length;i++) {
			data[i] |= 0xff000000;
		}
	}
	static void showshit(BufferedImage img, String title) {
		JFrame f = new JFrame(title);

		f.setDefaultCloseOperation(f.EXIT_ON_CLOSE);
		f.add(new JLabel(new ImageIcon(img)));
		f.pack();
		f.setVisible(true);
	}
	public static void main(String[]args) {
		Color fg = new Color(1f, 0f, 0f, 1f);
		Color edge = new Color(1f, 0f, 0f, 0f);

		BufferedImage bipre = new BufferedImage(245, 256, BufferedImage.TYPE_INT_ARGB_PRE);
		BufferedImage bi = new BufferedImage(245, 256, BufferedImage.TYPE_INT_ARGB);

		Paint paint = new RadialGradientPaint(new Point2D.Float(0, 0), 100,
						new float[]{0.0f, 1.0f}, new Color[]{fg, edge});

		doshit(bipre, paint);
		doshit(bi, paint);

		showshit(bipre, "pre");
		showshit(bi, "nopre");

	}
}
