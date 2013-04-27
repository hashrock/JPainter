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
		ImageWindow win = new ImageWindow();

		win.setVisible(true);
		win.getImageView().setTool(ZToolInfo.createDefaultTool());
		if (false) {
			final BufferedImage bi = ImageIO.read(new File("/home/notzed/Pictures/lena_std.png"));
			JFrame frame = new JFrame("Image resize");
			JTabbedPane tab = new JTabbedPane();

			ImageIcon icf = new ImageIcon(bi);
			ImageIcon ich = new ImageIcon(bi);

			FixedIcon lf = new FixedIcon(bi);
			//JLabel lf = new JLabel(icf);
			JComponent lh = new JComponent() {

				@Override
				protected void paintComponent(Graphics g) {
					Graphics2D gg = (Graphics2D) g.create();

					gg.scale(0.25, 0.25);
					gg.drawImage(bi, 32, 32, null);
					//this.getIcon().paintIcon(this, gg, 0, 0);

					gg.dispose();
					//super.paintComponent(g);
				}
			};

			lh.setSize(32, 32);


			tab.add("Full", lf);
			tab.add("Small", lh);

			frame.add(tab);
			frame.pack();
			frame.setVisible(true);
		}


		if (true) {
			return;
		}
		JFrame frame = new JFrame("Window");
		Foo f = new Foo();

		frame.setPreferredSize(new Dimension(720, 580));
		frame.add(f);
		frame.pack();

		frame.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					System.exit(0);
				}
				super.keyPressed(e);
			}
		});

		frame.setVisible(true);
		try {
			Thread.sleep(1000);

		} catch (InterruptedException ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
		}
		f.repaint(0, 0, 10, 10);
		try {
			Thread.sleep(1000);

		} catch (InterruptedException ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
		}
		f.repaint();
	}
}
