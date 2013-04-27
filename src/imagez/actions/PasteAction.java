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
import imagez.image.ZLayer;
import imagez.image.ZLayerRGBAInt;
import imagez.ui.ImageWindow;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

/**
 * Window screen grabbing.
 * @author notzed
 */
public class PasteAction extends AbstractWindowAction {

	public PasteAction() {
		super("Paste", null);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl V"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ImageWindow win = getWindow(e);

		Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();

		//cb.addFlavorListener(new FlavorListener() {
		//	@Override
		//	public void flavorsChanged(FlavorEvent e) {
		//		System.out.println("flavour changed: " + e);
		//	}
		//});

		System.out.println("Available flavours:");
		for (DataFlavor d : cb.getAvailableDataFlavors()) {
			System.out.println(" flavour " + d + " " + d.getHumanPresentableName());
		}

		if (cb.isDataFlavorAvailable(DataFlavor.imageFlavor)) {
			try {
				Image bi;

				// This is a bit weird, if I get an imageFlavour, some copies from the gimp (but not all?)
				// come through as jpeg's - and thus no alpha.
				if (false) {
					DataFlavor fl = new DataFlavor("image/png");
					InputStream is = (InputStream) cb.getData(fl);
					bi = ImageIO.read(is);
				} else {
					bi = (Image) cb.getData(DataFlavor.imageFlavor);
				}

				BufferedImage target;
				ZLayer layer;
				ZImage img;

				// hmm, i think i need to go to bed, this is shitfully messy

				if (win == null) {
					img = new ZImage(bi.getWidth(null), bi.getHeight(null));
					win = new ImageWindow(img);
					win.setVisible(true);
				} else if (win.isEmpty()) {
					img = new ZImage(bi.getWidth(null), bi.getHeight(null));
					win.setImage(img);
				} else {
					img = win.getImageView().getImage();
				}

				layer = new ZLayerRGBAInt(img, new Rectangle(50, 0, bi.getWidth(null), bi.getHeight(null)));
				layer.setTitle("Pasted Image");
				target = layer.getImage();

				Graphics2D gg = target.createGraphics();

				gg.drawImage(bi, 0, 0, null);
				gg.dispose();

				img.addLayer(layer);
				layer.addDamage(layer.getBounds());
			} catch (ClassNotFoundException ex) {
				Logger.getLogger(PasteAction.class.getName()).log(Level.SEVERE, null, ex);
			} catch (UnsupportedFlavorException ex) {
				Logger.getLogger(PasteAction.class.getName()).log(Level.SEVERE, null, ex);
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(win, "Error reading clipboard");
				Logger.getLogger(PasteAction.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
}
