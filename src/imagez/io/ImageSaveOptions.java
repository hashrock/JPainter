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
import imagez.image.ZImageCompositor;
import imagez.image.ZLayer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author notzed
 */
public abstract class ImageSaveOptions extends JPanel {

	File file;
	ZImage img;
	JRadioButton mergeAlpha;
	JRadioButton mergeSolid;
	JCheckBox dither;
	ButtonGroup mergeGroup;
	JPanel layerOptions;
	JPanel options;
	JPanel commentOptions;
	JTextArea comment;
	//
	public final JTextField nameBox;
	JLabel nameInfo;
	//
	public final JButton saveVersion;
	public final JButton save;
	public final JButton cancel;

	public ImageSaveOptions(File ffile, ZImage img, boolean supportsLayers, boolean canAlpha, boolean canComment) {
		this.file = ffile;
		this.img = img;

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		options = new JPanel();
		options.setAlignmentX(0);
		add(options);

		if (!supportsLayers) {
			layerOptions = new JPanel();
			layerOptions.setLayout(new BoxLayout(layerOptions, BoxLayout.PAGE_AXIS));
			layerOptions.setBorder(new TitledBorder("Layer Composition"));
			layerOptions.setAlignmentX(0);
			layerOptions.setMaximumSize(new Dimension(1000, 2000));
			add(layerOptions, BorderLayout.PAGE_START);

			mergeGroup = new ButtonGroup();
			//layerOptions.add(new JLabel("Layer Options"));

			if (canAlpha) {
				mergeAlpha = new JRadioButton("Merge and include alpha");
				layerOptions.add(mergeAlpha);
				mergeGroup.add(mergeAlpha);
			}
			mergeSolid = new JRadioButton("Flatten to solid colour");
			layerOptions.add(mergeSolid);
			mergeGroup.add(mergeSolid);

			dither = new JCheckBox("Dither");
			layerOptions.add(dither);
			dither.setEnabled(false);
		}

		if (canComment) {
			commentOptions = new JPanel();
			commentOptions.setLayout(new BoxLayout(commentOptions, BoxLayout.PAGE_AXIS));
			commentOptions.setBorder(new TitledBorder("Comment"));
			commentOptions.setAlignmentX(0);
			add(commentOptions);

			//JLabel lab = new JLabel("Comment");
			//lab.setAlignmentX(0);
			//commentOptions.add(lab);

			comment = new JTextArea();
			JScrollPane sp = new JScrollPane(comment);

			sp.setAlignmentX(0);
			commentOptions.add(sp, BorderLayout.CENTER);

			comment.setPreferredSize(new Dimension(400, 100));
		}

		// What a friggan mess!  All for a simple name box.
		JPanel namePanel = new JPanel();
		namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.LINE_AXIS));
		namePanel.setAlignmentX(0);
		add(namePanel);
		namePanel.add(new JLabel("Name"));
		nameBox = new JTextField(file.getName());

		// wtf?
		Dimension d = nameBox.getPreferredSize();
		d.width = Integer.MAX_VALUE;
		nameBox.setMaximumSize(d);

		namePanel.add(nameBox);
		nameBox.getDocument().addDocumentListener(new DocumentListener() {

			void resetFile() {
				file = new File(file.getParentFile(), nameBox.getText());
				setAbles();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				resetFile();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				resetFile();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				resetFile();
			}
		});
		nameInfo = new JLabel();
		namePanel.add(nameInfo);

		JPanel buttonBox = new JPanel();
		buttonBox.setLayout(new BoxLayout(buttonBox, BoxLayout.LINE_AXIS));
		buttonBox.setAlignmentX(0);

		//add(Box.createVerticalStrut(8));
		//add(new JSeparator());
		//add(Box.createVerticalStrut(4));
		add(buttonBox);

		buttonBox.add(Box.createHorizontalGlue());
		saveVersion = new JButton("Save Version");
		save = new JButton("Save");
		cancel = new JButton("Cancel");
		buttonBox.add(saveVersion);
		buttonBox.add(save);
		buttonBox.add(cancel);

		saveVersion.setDefaultCapable(true);
		save.setDefaultCapable(true);
		cancel.setDefaultCapable(true);

		//setPreferredSize(new Dimension(500, 400));
	}

	void setAbles() {
		if (file.exists()) {
			saveVersion.setEnabled(true);
			nameInfo.setText("file exists");
			//too big nameInfo.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
			nameInfo.setForeground(Color.red);
			save.setText("Overwrite");
		} else {
			saveVersion.setEnabled(false);
			save.setText("Save");
			nameInfo.setText(null);
			nameInfo.setIcon(null);
		}
	}

	public abstract String getTitle();

	public abstract String getFormat();

	public abstract void save() throws IOException;

	// how useful is this?  not convinced it's worth it ...
	public void saveVersion() {
		// FIXME: finish or discard
		if (true) {
			return;
		}
		// find a name that doesn't exist yet
		File dir = file.getParentFile();

		// try to parse name looking for version?
		String name = file.getName();
		int dot = name.lastIndexOf('.');
		int id = 1;
		String ext = "";
		StringBuilder test = new StringBuilder();

		if (dot != -1) {
			Pattern p = Pattern.compile("\\d+$");
			Matcher m = p.matcher(name.substring(0, dot));

			ext = name.substring(dot);

			if (m.matches()) {
				int count = m.start();

				id = Integer.parseInt(name.substring(count, dot));

				id++;
				test.append(name.substring(0, count));
			} else {
				test.append(name.substring(0, dot));
				test.append("-");
			}
		} else {
			test.append(name);
			test.append("-");
		}

		int baselen = test.length();

		File vfile;
		do {
			test.setLength(baselen);
			test.append(id);
			test.append(ext);
			vfile = new File(test.toString());
		} while (vfile.exists());
	}

	public void saveToLayer(ZLayer writeLayer) {
		ZImageCompositor ic = new ZImageCompositor();

		// DEPENDING ON OPTIONS?
		// Or is solid/alpha based on target image type?
		ic.flattenImage(writeLayer.getBounds(), img.getVisibleLayers(), null, writeLayer);
	}

	ImageWriter getWriter() throws IOException {
		// what about options?  Sigh.
		Iterator<ImageWriter> it = ImageIO.getImageWritersByFormatName(getFormat());
		if (!it.hasNext()) {
			throw new UnsupportedEncodingException();
		}

		ImageWriter iw = it.next();

		ImageOutputStream ios;

		iw.setOutput(new FileImageOutputStream(file));
		return iw;
	}

	public static ImageSaveOptions getSaveOptions(ZImage image, String fmt, File file) {
		fmt = fmt.toLowerCase();

		if (fmt.equals("png")) {
			return new ImageSavePNG(file, image);
		} else if (fmt.equals("jpeg") || fmt.equals("jpg")) {
			return new ImageSaveJPEG(file, image);
		} else {
			return new ImageSaveOpenRaster(file, image);
		}
	}
}
