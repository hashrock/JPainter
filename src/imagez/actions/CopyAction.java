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

import imagez.ui.ImageWindow;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.KeyStroke;

/**
 * Copying image data.
 * @author notzed
 */
public class CopyAction extends AbstractWindowAction {

	public CopyAction() {
		super("Copy", null);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl C"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ImageWindow win = getWindow(e);

		System.out.println("copy from " + win);

		Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();

		BufferedImage image = win.getImageView().getCurrentLayer().copy();
		ImageTransferable it = new ImageTransferable(image);

		try {
			cb.setContents(it, it);
		} catch (IllegalStateException x) {
			System.out.println("Couldn't set clipboard");
		}
	}

	/**
	 * wraps the image data for a copy operation.
	 *
	 * TODO: This only wraps an 8 bit RGBA image.
	 *
	 * It should also offer floating point representation for use
	 * within ImageZ, or TIFF etc.
	 */
	class ImageTransferable implements Transferable, ClipboardOwner {

		Image image;

		public ImageTransferable(Image image) {
			this.image = image;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[]{DataFlavor.imageFlavor};
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			if (flavor.equals(DataFlavor.imageFlavor)) {
				return true;
			}
			return false;
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			return image;
		}

		@Override
		public void lostOwnership(Clipboard clipboard, Transferable contents) {
			// not sure what I can use this for ... clear some state?
			System.out.println("lost clipboard ownership, do I care?");
		}
	}
}
