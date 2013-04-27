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
package imagez.io;

import imagez.image.ZImage;
import imagez.image.ZLayer;
import imagez.image.ZLayerMonoAByte;
import imagez.image.ZLayerMonoByte;
import imagez.image.ZLayerRGBAInt;
import imagez.image.ZLayerRGBAShort16;
import imagez.image.ZLayerRGBInt;
import imagez.image.ZLayerRGBShort16;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.TitledBorder;

/**
 *
 * @author notzed
 */
public class ImageSavePNG extends ImageSaveOptions {

	final static Integer[] depths = new Integer[]{16, 8, 5 /*, 4*/};
	JComboBox depth;
	JCheckBox progressive;
	JCheckBox saveBackground;

	public ImageSavePNG(File file, ZImage image) {
		super(file, image, false, true, true);

		//FIXME: scan layers to find minimum layer required
		ZLayer[] layers = image.getVisibleLayers();
		int defaultDepth = 8;
		for (int i = 0; i < layers.length; i++) {
			// get what info from layer ???
		}

		mergeAlpha.setSelected(true);

		Dimension d;

		options.setLayout(new GridBagLayout());
		options.setBorder(new TitledBorder("PNG Options"));

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.add(new JLabel("Bits Per Channel"));
		depth = new JComboBox(depths);
		depth.setSelectedItem(Integer.valueOf(defaultDepth));
		panel.add(depth);
		panel.setAlignmentX(0);

		layerOptions.add(Box.createVerticalStrut(4));
		layerOptions.add(new JSeparator());
		layerOptions.add(Box.createVerticalStrut(4));
		layerOptions.add(panel);

		GridBagConstraints c0 = new GridBagConstraints();
		c0.gridx = 0;
		c0.ipadx = 4;
		c0.ipady = 2;
		c0.anchor = c0.WEST;
		GridBagConstraints c1 = (GridBagConstraints) c0.clone();
		c1.weightx = 1;
		c1.gridx = 1;
		c1.fill = c1.HORIZONTAL;

		GridBagConstraints c01 = (GridBagConstraints) c0.clone();
		c01.gridwidth = 2;

		progressive = new JCheckBox("Progressive");
		options.add(progressive, c0);
		saveBackground = new JCheckBox("Save Background Colour");
		options.add(saveBackground, c0);

		saveBackground.setEnabled(false);

		// dummy for alignment
		options.add(new JLabel(""), c1);

		d = options.getPreferredSize();
		d.width = 10000;
		options.setMaximumSize(d);

		d = layerOptions.getPreferredSize();
		d.width = 10000;
		layerOptions.setMaximumSize(d);

		setAbles();
	}

	@Override
	public String getTitle() {
		return "PNG";
	}

	@Override
	public String getFormat() {
		return "png";
	}

	@Override
	public void save() throws IOException {
		int d = ((Number) depth.getSelectedItem()).intValue();
		ZLayer writeLayer;

		// 4 channels is rgba
		// 2 channels is ma
		//  but we output alpha based on use option not layer data
		int channels = 1;
		for (ZLayer l : img.getVisibleLayers()) {
			int c = l.getChannelCount();

			c = c == 2 ? 1 : c;
			c = c == 4 ? 3 : c;
			channels = Math.max(channels, c);
		}

		// gotta be a better way than this messs ...
		if (channels == 1) {
			switch (d) {
				// FIXME: 16 bit mono
				case 8:
				default:
					if (mergeSolid.isSelected()) {
						writeLayer = new ZLayerMonoByte(null, new Rectangle(img.getDimension()));
						//((ZLayerRGBInt) writeLayer).setBackground(1, 1, 1);
					} else {
						writeLayer = new ZLayerMonoAByte(null, new Rectangle(img.getDimension()));
					}
					break;
			}
		} else {
			switch (d) {
				case 16:
					if (mergeSolid.isSelected()) {
						writeLayer = new ZLayerRGBShort16(null, new Rectangle(img.getDimension()));
						((ZLayerRGBShort16) writeLayer).setBackground(1, 1, 1);
					} else {
						writeLayer = new ZLayerRGBAShort16(null, new Rectangle(img.getDimension()));
					}
					break;
				case 8:
				default:
					if (mergeSolid.isSelected()) {
						writeLayer = new ZLayerRGBInt(null, new Rectangle(img.getDimension()));
						((ZLayerRGBInt) writeLayer).setBackground(1, 1, 1);
					} else {
						writeLayer = new ZLayerRGBAInt(null, new Rectangle(img.getDimension()));
					}
					break;
				//case 5:
				// pngwriter just saves it as 8 bit anyway, afaict
				//	if (mergeSolid.isSelected()) {
				//		writeLayer = new ZLayerRGBShort565(null, new Rectangle(img.getDimension()));
				//		((ZLayerRGBShort565) writeLayer).setBackground(1, 1, 1);
				//	} else {
				//		writeLayer = new ZLayerRGBAInt(null, new Rectangle(img.getDimension()));
				//	}
				//	break;
				//case 4:
				//	break;
			}
		}

		saveToLayer(writeLayer);

		// TODO: common code?
		ImageWriter iw = getWriter();
		ImageWriteParam ip = iw.getDefaultWriteParam();

		// How to set meta data stuff?

		IIOImage ioimg = new IIOImage(writeLayer.getImage(), null, null);

		ip.setProgressiveMode(progressive.isSelected() ? ip.MODE_DEFAULT : ip.MODE_DISABLED);

		iw.write(null, ioimg, ip);
	}
}
