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
package imagez.ui;

import imagez.image.ZImage;
import imagez.image.ZLayer;
import imagez.image.ZLayerType;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * @author notzed
 */
public class NewImageWindow extends JDialog {

	JComboBox layerOptions;
	JSpinner widthSpinner;
	JSpinner heightSpinner;
	ImageWindow win;

	public NewImageWindow(ImageWindow wwin) {
		super(wwin);

		this.win = wwin;

		JPanel panel = new JPanel();
		setContentPane(panel);

		// size
		// depth
		panel.setLayout(new GridBagLayout());

		GridBagConstraints c0 = new GridBagConstraints();
		c0.gridx = 0;
		c0.ipadx = 4;
		c0.ipady = 2;
		c0.anchor = c0.WEST;
		GridBagConstraints c1 = (GridBagConstraints) c0.clone();
		c1.weightx = 1;
		c1.gridx = 1;
		c1.fill = c1.HORIZONTAL;
		GridBagConstraints c01 = (GridBagConstraints) c1.clone();
		c01.gridwidth = 2;

		panel.add(new JLabel("Format"), c0);
		panel.add(layerOptions = new JComboBox(ZLayerType.values()), c1);
		panel.add(new JLabel("Width"), c0);
		panel.add(widthSpinner = new JSpinner(new SpinnerNumberModel(640, 0, 16384, 1)), c1);
		panel.add(new JLabel("Height"), c0);
		panel.add(heightSpinner = new JSpinner(new SpinnerNumberModel(480, 0, 16384, 1)), c1);

		JPanel buttons;
		panel.add(buttons = new JPanel(), c01);
		buttons.add(Box.createHorizontalGlue());
		JButton create;
		buttons.add(create = new JButton("Create Image"));
		JButton cancel;
		buttons.add(cancel = new JButton("Cancel"));

		create.setDefaultCapable(true);
		getRootPane().setDefaultButton(create);

		layerOptions.setMaximumRowCount(50);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();

		// must be done after pack so window size is known
		if (wwin != null) {
			setLocationRelativeTo(win);
		}

		create.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
				int width = ((Number)widthSpinner.getValue()).intValue();
				int height = ((Number)heightSpinner.getValue()).intValue();

				Rectangle rect = new Rectangle(width, height);
				ZLayerType type = (ZLayerType) layerOptions.getSelectedItem();
				ZImage img = new ZImage(width, height);
				ZLayer layer = type.createLayer(img, rect);

				layer.setTitle("Background");
				img.addLayer(layer);

				if (win == null || !win.isEmpty()) {
					win = new ImageWindow(img);
					win.setVisible(true);
				} else {
					win.setImage(img);
				}
			}
		});

		cancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
	}

	public static void main(String[]args) {
		new NewImageWindow(null).setVisible(true);
	}
}
