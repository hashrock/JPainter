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
package imagez.image;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * A layer that contains structured content.
 *
 * It is still backed by an RGBA layer.
 *
 * blah blah should probably be svg blah blah.  But xml really sucks.
 * @author notzed
 */
public class ZLayerRGBAStructured extends ZLayerRGBAInt {

	public ZLayerRGBAStructured(ZImage zimage, Rectangle bounds) {
		super(zimage, bounds);
	}

	public void addText(int x, int y, String text) {
		BufferedImage img = getImage();
		Graphics2D gg = img.createGraphics();

		Font f = new Font("Sans", Font.PLAIN, 36);

		gg.setColor(Color.black);
		gg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		FontRenderContext frc = gg.getFontRenderContext();

		TextLayout tl = new TextLayout(text, f, frc);

		tl.draw(gg, x, y);

		gg.dispose();

		Rectangle2D tbounds = tl.getBounds();
		Rectangle rect = tbounds.getBounds();

		rect.translate(x, y);
		addDamage(rect);

		addText2(x, y, text);
	}

	public void addText2(int x, int y, String text) {
		BufferedImage img = getImage();
		Graphics2D gg = img.createGraphics();

		y += 128;

		Font f = new Font("SansSerif", Font.PLAIN, 36);

		System.out.println("font = " + f);

		gg.setFont(f);
		gg.setColor(Color.black);
		gg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		FontRenderContext frc = gg.getFontRenderContext();

		AttributedString as = new AttributedString(text);
		as.addAttribute(TextAttribute.KERNING, TextAttribute.KERNING_ON);
		as.addAttribute(TextAttribute.FONT, f);
		//as.addAttribute(TextAttribute.SIZE, 36);
		//as.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD, 9, 15);

		LineBreakMeasurer lbm = new LineBreakMeasurer(as.getIterator(), frc);

		float maxWidth = 800;
		Rectangle rect = new Rectangle();
		Point2D.Float pen = new java.awt.geom.Point2D.Float(x, y);
		AttributedCharacterIterator it = as.getIterator();

		while (lbm.getPosition() < text.length()) {
			int pos = lbm.getPosition();
			int nl = text.indexOf(10, pos);
			TextLayout tl;
			//nl = -1;
			if (nl == -1) {
				tl = lbm.nextLayout(maxWidth);
			} else if (nl == pos) {
				pen.y += f.getSize();
				lbm.setPosition(nl + 1);
				continue;
			} else {
				tl = lbm.nextLayout(maxWidth, nl, false);
				if (lbm.getPosition() == nl) {
					lbm.setPosition(nl + 1);
				}
			}

			pen.y += tl.getAscent();
			float dx = tl.isLeftToRight() ? 0 : maxWidth - tl.getAdvance();
			tl.draw(gg, pen.x, pen.y);
			pen.y += tl.getDescent() + tl.getLeading();

			rect = rect.union(tl.getBounds().getBounds());
		}


		gg.dispose();

		rect.translate(x, y);
		addDamage(rect);

	}

	public static void main(String[] args) {
		GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();

		System.out.println(ge);

		GraphicsDevice[] gd = ge.getScreenDevices();

		for (GraphicsDevice g : gd) {
			System.out.println("gd " + g);

			GraphicsConfiguration dc = g.getDefaultConfiguration();
			System.out.println("default: " + dc.getBounds());

			GraphicsConfiguration[] gc = g.getConfigurations();

			for (GraphicsConfiguration c : gc) {
				System.out.println("  gc " + c + " " + c.getBounds());
			}
		}

		//	Runtime rt = Runtime.getRuntime();
		Rectangle rect = new Rectangle();
		try {
			ProcessBuilder pb = new ProcessBuilder("xwininfo", "-frame");
			Process p = pb.start();
			InputStream is = p.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			//BufferedInputStream bis = new BufferedInputStream(is);
			String line;

			while ((line = br.readLine()) != null) {
				if (line.startsWith("  Absolute upper-left X:")) {
					rect.x = Integer.parseInt(line.substring(26));
				} else if (line.startsWith("  Absolute upper-left Y:")) {
					rect.y = Integer.parseInt(line.substring(26));
				} else if (line.startsWith("  Width:")) {
					rect.width = Integer.parseInt(line.substring(9));
				} else if (line.startsWith("  Height:")) {
					rect.height = Integer.parseInt(line.substring(10));
				}
			}
			br.close();
		} catch (IOException ex) {
			Logger.getLogger(ZLayerRGBAStructured.class.getName()).log(Level.SEVERE, null, ex);
		}

		try {
			Robot r = new Robot();
			//BufferedImage bi = r.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
			BufferedImage bi = r.createScreenCapture(rect);

			JFrame jf = new JFrame();
			//BufferedImage tmp = new BufferedImage(1024 * 2, 768, BufferedImage.TYPE_INT_ARGB);
			//tmp.createGraphics().drawImage(bi, 0, 0, 1024 * 2, 768, null);

			jf.add(new JLabel(new ImageIcon(bi)));
			jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			jf.pack();
			jf.setVisible(true);
		} catch (AWTException ex) {
			Logger.getLogger(ZLayerRGBAStructured.class.getName()).log(Level.SEVERE, null, ex);
		}


	}
}
