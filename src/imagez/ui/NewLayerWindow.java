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
import imagez.image.ZLayerType;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * FIXME: this is mostly the same as NewImagewindow, so share code somehow
 * @author notzed
 */
public class NewLayerWindow extends JDialog {

	JTextField layerTitle;
	JComboBox layerOptions;
	JSpinner widthSpinner;
	JSpinner heightSpinner;
	ZImage img;

	public NewLayerWindow(Component parent, ZImage image) {
		super((JFrame)parent);

		this.img = image;

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

		panel.add(new JLabel("Title"), c0);
		panel.add(layerTitle = new JTextField("New Layer"), c1);

		Dimension d = image.getDimension();

		panel.add(new JLabel("Format"), c0);
		panel.add(layerOptions = new JComboBox(ZLayerType.values()), c1);
		panel.add(new JLabel("Width"), c0);
		panel.add(widthSpinner = new JSpinner(new SpinnerNumberModel(d.width, 0, 16384, 1)), c1);
		panel.add(new JLabel("Height"), c0);
		panel.add(heightSpinner = new JSpinner(new SpinnerNumberModel(d.height, 0, 16384, 1)), c1);

		JPanel buttons;
		panel.add(buttons = new JPanel(), c01);
		buttons.add(Box.createHorizontalGlue());
		JButton create;
		buttons.add(create = new JButton("Create Layer"));
		JButton cancel;
		buttons.add(cancel = new JButton("Cancel"));

		create.setDefaultCapable(true);
		getRootPane().setDefaultButton(create);

		layerOptions.setMaximumRowCount(50);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();

		if (parent != null) {
			setLocationRelativeTo(parent);
		} else {
			// erg
			setLocationByPlatform(true);
		}

		create.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
				int width = ((Number) widthSpinner.getValue()).intValue();
				int height = ((Number) heightSpinner.getValue()).intValue();
				Rectangle rect = new Rectangle(width, height);
				ZLayerType type = (ZLayerType) layerOptions.getSelectedItem();

				img.getEditor().addLayer(type, rect, layerTitle.getText());
			}
		});

		cancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
	}

	public static void main(String[] args) {
		new NewLayerWindow(null, new ZImage(640, 480)).setVisible(true);
	}
}
