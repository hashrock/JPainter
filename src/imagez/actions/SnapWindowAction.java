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
package imagez.actions;

import imagez.image.ZImage;
import imagez.image.ZLayerRGBAInt;
import imagez.ui.ImageWindow;
import java.awt.AWTException;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.swing.JOptionPane;

/**
 * Window screen grabbing.
 * @author notzed
 */
public class SnapWindowAction extends AbstractWindowAction {

	public SnapWindowAction() {
		super("New From Window", null);
		//putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl N"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ImageWindow win = getWindow(e);
		
		// TODO: pop up a window asking for details.
		//  e.g. include frame or not
		//       full-screen or window  (if supported?)
		//       timeout

		Rectangle rect = new Rectangle();
		try {
			ProcessBuilder pb = new ProcessBuilder("xwininfo" ,"-frame");
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
			p.destroy();
		} catch (IOException ex) {
			//Logger.getLogger(ZLayerRGBAStructured.class.getName()).log(Level.SEVERE, null, ex);
			rect.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		}

		try {
			Robot r = new Robot();
			BufferedImage bi = r.createScreenCapture(rect);

			ZImage img = new ZImage(rect.width, rect.height);
			img.addLayer(new ZLayerRGBAInt(img, new Rectangle(img.getDimension())));
			Graphics2D gg = img.getLayer(0).getImage().createGraphics();

			gg.drawImage(bi, 0, 0, null);
			gg.dispose();

			if (win != null && win.isEmpty()) {
				win.setImage(img);
			} else {
				//img.createLayer().setTitle("Background");
				win = new ImageWindow(img);
				win.setVisible(true);
			}

		} catch (AWTException ex) {
			//Logger.getLogger(ZLayerRGBAStructured.class.getName()).log(Level.SEVERE, null, ex);
			JOptionPane.showMessageDialog(win, "Unable to capture window or screen");
		}
	}
}
