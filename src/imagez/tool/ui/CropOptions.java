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
package imagez.tool.ui;

import imagez.tool.CropModel;
import imagez.tool.CropTool;
import imagez.ui.SmallButton;
import imagez.ui.SmallLabel;
import imagez.ui.SmallSpinner;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Super Affine Tool options.
 * @author notzed
 */
public class CropOptions extends JPanel implements PropertyChangeListener {

	CropModel model;
	JSpinner transX;
	JSpinner transY;
	JSpinner sizeX;
	JSpinner sizeY;
	private final CropTool tool;

	public CropOptions(CropTool itool, CropModel mmodel) {
		this.tool = itool;
		this.model = mmodel;
		JPanel options = this;

		options.setLayout(new GridBagLayout());

		GridBagConstraints c0 = new GridBagConstraints();
		c0.gridx = 0;
		c0.anchor = GridBagConstraints.BASELINE_TRAILING;
		c0.ipadx = 0;
		c0.insets = new Insets(1, 2, 1, 2);

		GridBagConstraints c1 = new GridBagConstraints();
		c1.gridx = 1;
		c1.insets = new Insets(1, 2, 1, 2);
		c1.weightx = 1;
		c1.anchor = GridBagConstraints.BASELINE_LEADING;
		c1.fill = GridBagConstraints.HORIZONTAL;

		GridBagConstraints c2 = new GridBagConstraints();
		c2.gridx = 2;
		c2.insets = new Insets(1, 2, 1, 2);
		c2.weightx = 1;
		c2.anchor = GridBagConstraints.BASELINE_LEADING;
		c2.fill = GridBagConstraints.HORIZONTAL;

		GridBagConstraints c01 = new GridBagConstraints();
		c01.gridx = 0;
		c01.gridwidth = 2;
		c01.anchor = GridBagConstraints.BASELINE_LEADING;
		c01.fill = GridBagConstraints.HORIZONTAL;

		GridBagConstraints c02 = new GridBagConstraints();
		c02.gridx = 0;
		c02.gridwidth = 3;
		c02.anchor = GridBagConstraints.BASELINE_LEADING;
		c02.fill = GridBagConstraints.HORIZONTAL;

		GridBagConstraints c23 = new GridBagConstraints();
		c23.gridx = 1;
		c23.gridwidth = 2;
		c23.anchor = GridBagConstraints.BASELINE_LEADING;
		c23.fill = GridBagConstraints.HORIZONTAL;

		options.add(new SmallLabel("Off X"), c0);
		options.add(transX = new SmallSpinner(), c1);
		options.add(new SmallLabel("Y"), c0);
		options.add(transY = new SmallSpinner(), c1);
		options.add(new SmallLabel("Size"), c0);
		options.add(sizeX = new SmallSpinner(), c1);
		options.add(new SmallLabel("Y"), c0);
		options.add(sizeY = new SmallSpinner(), c1);

		JPanel buttons = new JPanel(new GridLayout(1, 2));
		options.add(buttons, c01);

		Action saveMe = new AbstractAction("Save") {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Store crop");
				tool.save();
			}
		};
		Action cancelMe = new AbstractAction("Cancel") {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Revert crop");
				tool.cancel();
			}
		};
		buttons.add(new SmallButton(saveMe), c0);
		buttons.add(new SmallButton(cancelMe), c1);

		copyModel();
		
		transX.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Number nx = (Number) transX.getValue();
				model.setOffx(nx.intValue());
			}
		});
		transY.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Number nx = (Number) transY.getValue();
				model.setOffy(nx.intValue());
			}
		});

		sizeX.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Number nx = (Number) sizeX.getValue();
				model.setSizex(nx.intValue());
			}
		});
		sizeY.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Number nx = (Number) sizeY.getValue();
				model.setSizey(nx.intValue());
			}
		});

		Dimension d = getPreferredSize();
		d.width = 150;
		setMaximumSize(d);
		setPreferredSize(d);

		model.addPropertyChangeListener(this);
	}

	void copyModel() {
		sizeX.setValue(model.getSizex());
		sizeY.setValue(model.getSizey());
		transX.setValue(model.getOffx());
		transY.setValue(model.getOffy());
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String name = evt.getPropertyName();

		if (name.equals(CropModel.PROP_SIZEX)) {
			sizeX.setValue(model.getSizex());
		} else if (name.equals(CropModel.PROP_SIZEY)) {
			sizeY.setValue(model.getSizey());
		} else if (name.equals(CropModel.PROP_OFFX)) {
			transX.setValue(model.getOffx());
		} else if (name.equals(CropModel.PROP_OFFY)) {
			transY.setValue(model.getOffy());
		}
	}
}
