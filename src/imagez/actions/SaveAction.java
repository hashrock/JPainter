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
import imagez.image.ZImageCompositor;
import imagez.image.ZLayer;
import imagez.image.ZLayerMonoFloat;
import imagez.image.ZLayerRGBAByte;
import imagez.image.ZLayerRGBAFloat;
import imagez.image.ZLayerRGBAInt;
import imagez.image.ZLayerRGBAShort16;
import imagez.io.ImageSaveJPEG;
import imagez.io.ImageSaveOptions;
import imagez.io.ImageSavePNG;
import imagez.io.SaveImageWindow;
import imagez.ui.ImageFileChooser;
import imagez.ui.ImageWindow;
import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

/**
 *
 * @author notzed
 */
public class SaveAction extends AbstractWindowAction {

	public SaveAction() {
		super("Save", null);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl S"));
	}

	@Override
	public Object getValue(String key) {
		return super.getValue(key);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ImageWindow win = getWindow(e);

		if (win == null || win.isEmpty()) {
			return;
		}

		ZImage img = win.getImageView().getImage();
/*
		// hack hack, get from layer or from image?
		int type = -1;
		int depth = -1;
		int layerCount = img.getLayerCount();
		int layerVisible = 0;
		for (int i = 0; i < layerCount; i++) {
			ZLayer l = img.getLayer(i);
			int stype;
			int sdepth;

			if (!l.isVisible()) {
				continue;
			}

			layerVisible++;

			if (l instanceof ZLayerRGBAInt || l instanceof ZLayerRGBAByte) {
				stype = 0;
				sdepth = 4;
			} else if (l instanceof ZLayerRGBAShort16) {
				stype = 1;
				sdepth = 4;
			} else if (l instanceof ZLayerRGBAFloat) {
				stype = 2;
				sdepth = 4;
			} else if (l instanceof ZLayerMonoFloat) {
				stype = 2;
				sdepth = 1;
			} else {
				return;
			}
			type = Math.max(stype, type);
			depth = Math.max(depth, sdepth);
		}
*/

		//JFileChooser fc = new JFileChooser();
		JFileChooser fc = new ImageFileChooser();

		// TODO: make non-modal.  Pack into a new frame, handle events, etc.
/*
		// HACK: change mode to detail mode to make it nicer to use
		try {
		JPanel rootPanel = (JPanel) fc.getComponent(0);
		JPanel buttonPanel = (JPanel) rootPanel.getComponent(0);
		int len = buttonPanel.getComponentCount();
		int seen = 0;

		for (int i = 0; i < len; i++) {
		Component c = buttonPanel.getComponent(i);

		if (c instanceof JToggleButton) {
		seen++;
		if (seen == 2) {
		//((JToggleButton)c).setSelected(true);
		((JToggleButton) c).doClick();
		}
		}
		}
		} catch (Exception x) {
		System.out.println("filechooser hacks are dangerous: " + x.getMessage());
		}
		fc.setPreferredSize(new Dimension(500, 600));
		 */

		Preferences prefs = Preferences.userNodeForPackage(SaveAction.class);


		String dir = prefs.get("save.drawer", null);

		if (dir != null) {
			fc.setCurrentDirectory(new File(dir));
		}

		if (fc.showSaveDialog(win) == JFileChooser.APPROVE_OPTION) {
			try {
				File file = fc.getSelectedFile();
				/*
				if (file.exists()) {
					String[] labels = new String[]{"Cancel", "Overwrite"};
					int o;
					if ((o = JOptionPane.showOptionDialog(win, "<html>File `" + file + "' exists.<br><br>Overwrite?", "File Exists",
							JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, labels, labels[0])) != 1) {
						System.out.println("option chosen = " + o);
						return;
					}
				}
				 *
				 */
				prefs.put("save.drawer", fc.getCurrentDirectory().getPath());
				String name = file.getName();
				int dot = name.lastIndexOf('.');
				String fmt = dot == -1 || (name.length() - dot < 2) ? "png" : name.substring(dot + 1);

				{
					ImageSaveOptions so = ImageSaveOptions.getSaveOptions(img, fmt, file);

					if (so != null) {
						SaveImageWindow siw = new SaveImageWindow(win, so);

						siw.setVisible(true);
						/*
						JFrame jf = new JFrame();
						jf.setLocationByPlatform(true);
						jf.setContentPane(so);
						jf.pack();
						jf.setDefaultCloseOperation(jf.DISPOSE_ON_CLOSE);
						jf.setVisible(true);
						jf.setTitle("Save " + so.getTitle());
						jf.getRootPane().setDefaultButton(so.save);
						so.nameBox.requestFocusInWindow();
						 *
						 */
						return;
					}
				}


				/*


				// Now work out intermediate image format
				ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
				ImageWriter iw;
				looking:
				while (true) {
					// It really depends on the backend ...
					ImageTypeSpecifier its;
					System.out.printf("try saving possible as image type %d channels %d\n", type, depth);
					// TODO: depth
					switch (type) {
						case 0:
						default:
							// bytes
							its = ImageTypeSpecifier.createPacked(cs, 0x00ff0000, 0x0000ff00, 0x000000ff, 0xff000000, DataBuffer.TYPE_INT, false);
							break;
						case 1:
							// shorts
							its = ImageTypeSpecifier.createInterleaved(cs, new int[]{0, 1, 2, 3}, DataBuffer.TYPE_USHORT, true, false);
							break;
						case 2:
							// floats
							its = ImageTypeSpecifier.createInterleaved(cs, new int[]{0, 1, 2, 3}, DataBuffer.TYPE_FLOAT, true, false);
					}
					System.out.println("Trying its " + its + " for format " + fmt);
					Iterator<ImageWriter> it = ImageIO.getImageWriters(its, fmt);
					if (!it.hasNext()) {
						System.out.println("No matching writer found");
						if (type == 0) {
							System.out.println(" but we're out of shit to try?  drop alpha?");
							return;
						}
						type--;
						continue looking;
					}
					iw = it.next();
					break;
				}

				ImageWriteParam ip;
				ip = iw.getDefaultWriteParam();

				System.out.println("writer = " + iw);
				System.out.println("default writeparam = " + ip);
				System.out.println("can compressed = " + ip.canWriteCompressed());

				ZLayer writeLayer;
				switch (type) {
					case 0:
					default:
						writeLayer = new ZLayerRGBAInt(null, new Rectangle(img.getDimension()));
						break;
					case 1:
						writeLayer = new ZLayerRGBAShort16(null, new Rectangle(img.getDimension()));
						break;
					case 2:
						writeLayer = new ZLayerRGBAFloat(null, new Rectangle(img.getDimension()));
						break;
				}
				ZLayer[] visible = new ZLayer[layerVisible];
				int j = 0;
				for (int i = 0; i < layerCount; i++) {
					ZLayer l = img.getLayer(i);
					if (l.isVisible()) {
						visible[j++] = l;
					}
				}
				ZImageCompositor ic = new ZImageCompositor();
				ic.flattenImage(writeLayer.getBounds(), visible, null, writeLayer);
				if (!ImageIO.write(writeLayer.getImage(), fmt, file)) {
					System.out.println("image write failed?");
				}
				System.out.println(" saving" + file + " using image writer " + iw);
				 *
				 */
			} catch (Exception ex) {
				Logger.getLogger(SaveAction.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
}
