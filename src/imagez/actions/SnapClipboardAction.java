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
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * Window screen grabbing.
 * @author notzed
 */
public class SnapClipboardAction extends AbstractWindowAction {

	public SnapClipboardAction() {
		super("New From Clipboard", null);
		//putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl N"));
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

		if (cb.isDataFlavorAvailable(DataFlavor.imageFlavor)) {
			try {
				Image bi = (Image) cb.getData(DataFlavor.imageFlavor);

				ZImage img = new ZImage(bi.getWidth(null), bi.getHeight(null));
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
			} catch (UnsupportedFlavorException ex) {
				Logger.getLogger(SnapClipboardAction.class.getName()).log(Level.SEVERE, null, ex);
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(win, "Error reading clipboard");
				Logger.getLogger(SnapClipboardAction.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		//for (DataFlavor d : cb.getAvailableDataFlavors()) {
		//	System.out.println(" flavour " + d);
		//}
	}
}
