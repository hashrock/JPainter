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
import imagez.image.ZLayerRGBAInt;
import imagez.image.ZLayerRGBInt;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;

/**
 *
 * @author notzed
 */
public class ImageSaveJPEG extends ImageSaveOptions {

	JSlider qualitySlider;
	JCheckBox progressive;
	JCheckBox thumbnail;

	public ImageSaveJPEG(File file, ZImage image) {
		super(file, image, false, true, true);

		// by default we just want RGB, since RGBA isn't widely supported
		mergeSolid.setSelected(true);

		options.setLayout(new GridBagLayout());
		options.setBorder(new TitledBorder("JPEG Options"));
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
		c01.fill = c01.HORIZONTAL;

		options.add(new JLabel("Quality"), c0);
		qualitySlider = new JSlider(0, 100, 85);
		options.add(qualitySlider, c1);
		options.add(Box.createVerticalStrut(4));
		options.add(new JSeparator());
		options.add(Box.createVerticalStrut(4));

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0, 2));

		progressive = new JCheckBox("Progressive");
		panel.add(progressive);
		thumbnail = new JCheckBox("Save Thumbnail");
		panel.add(thumbnail);
		options.add(panel, c01);

		thumbnail.setEnabled(false);

		Dimension d = options.getPreferredSize();
		d.width = 10000;
		options.setMaximumSize(d);

		setAbles();
		//setPreferredSize(new Dimension(500, 400));
	}

	@Override
	public String getTitle() {
		return "JPEG";
	}

	@Override
	public String getFormat() {
		return "jpeg";
	}

	@Override
	public void save() throws IOException {
		ZLayer writeLayer;

		// FIXME: get background colour from .. somewhere

		if (mergeSolid.isSelected()) {
			writeLayer = new ZLayerRGBInt(null, new Rectangle(img.getDimension()));
			((ZLayerRGBInt)writeLayer).setBackground(1, 1, 1);
		} else {
			writeLayer = new ZLayerRGBAInt(null, new Rectangle(img.getDimension()));
		}

		saveToLayer(writeLayer);

		// TODO: common code?
		ImageWriter iw = getWriter();
		ImageWriteParam ip = iw.getDefaultWriteParam();

		IIOImage ioimg = new IIOImage(writeLayer.getImage(), null, null);

		ip.setCompressionMode(ip.MODE_EXPLICIT);
		ip.setCompressionQuality(qualitySlider.getValue() / 100.0f);
		ip.setProgressiveMode(progressive.isSelected() ? ip.MODE_DEFAULT : ip.MODE_DISABLED);

		iw.write(null, ioimg, ip);
	}
}
