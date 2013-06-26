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
package imagez;

import imagez.tool.ZToolInfo;
import imagez.ui.ImageWindow;
import imagez.util.EmptyImageCreator;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

class Foo extends JComponent {

	@Override
	protected void paintComponent(Graphics g) {
		System.out.println("painting clip rect " + g.getClipBounds());
		g.drawLine(0, 0, 100, 100);
		super.paintComponent(g);

	}
}

class FixedIcon extends JComponent {

	BufferedImage img;

	public FixedIcon(BufferedImage img) {
		this.img = img;
		setSize(64, 64);
	}

	@Override
	protected void paintComponent(Graphics g) {
		double xs = 64.0 / img.getWidth();
		double ys = 64.0 / img.getHeight();
		Graphics2D gg = (Graphics2D) g.create();
		AffineTransform af = new AffineTransform();

		af.scale(xs, ys);

		gg.drawImage(img, af, null);
		gg.dispose();
	}
}

/**
 *
 * @author notzed
 */
public class Main {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws IOException {
		if (true) {
			try {
                            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception ex) {
			}
		}
                
		ImageWindow win = new ImageWindow(EmptyImageCreator.getEmptyImage());

		win.setVisible(true);
		win.getImageView().setTool(ZToolInfo.createDefaultTool());
	}
}
