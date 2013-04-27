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

import imagez.tool.AffineModel;
import imagez.tool.SuperAffineTool;
import imagez.ui.SmallButton;
import imagez.ui.SmallLabel;
import imagez.ui.SmallSpinner;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Super Affine Tool options.
 * @author notzed
 */
public class AffineOptions extends JPanel implements PropertyChangeListener {

	AffineModel model;
	JSpinner transX;
	JSpinner transY;
	JSpinner sizeX;
	JSpinner sizeY;
	JSpinner shearX, shearY;
	JSlider angleSlider;
	JSpinner angleSpinner;
	JSpinner pivotX, pivotY;
	private final SuperAffineTool tool;

	public AffineOptions(SuperAffineTool itool, AffineModel mmodel) {
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

		options.add(new SmallLabel("Trans X"), c0);
		options.add(transX = new SmallSpinner(new SpinnerNumberModel(0.0, -10240.0, 10240.0, 1.0)), c1);
		options.add(new SmallLabel("Y"), c0);
		options.add(transY = new SmallSpinner(new SpinnerNumberModel(0.0, -10240.0, 10240.0, 1.0)), c1);
		options.add(new SmallLabel("Size"), c0);
		options.add(sizeX = new SmallSpinner(), c1);
		options.add(new SmallLabel("Y"), c0);
		options.add(sizeY = new SmallSpinner(), c1);
		options.add(new SmallLabel("Shear"), c0);
		options.add(shearX = new SmallSpinner(), c1);
		options.add(new SmallLabel("Y"), c0);
		options.add(shearY = new SmallSpinner(), c1);
		options.add(new SmallLabel("Angle"), c0);
		options.add(angleSpinner = new SmallSpinner(), c1);
		options.add(angleSlider = new JSlider(-180, 180, 0), c01);
		options.add(new SmallLabel("Centre"), c0);
		options.add(pivotX = new SmallSpinner(), c1);
		options.add(new SmallLabel("Y"), c0);
		options.add(pivotY = new SmallSpinner(), c1);

		JPanel buttons = new JPanel(new GridLayout(1, 2));
		options.add(buttons, c01);
		
		Action saveMe = new AbstractAction("Save") {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Store affine transform");
				tool.save();
			}
		};
		Action cancelMe = new AbstractAction("Cancel") {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Revert affine transform");
				tool.cancel();
			}
		};
		buttons.add(new SmallButton(saveMe), c0);
		buttons.add(new SmallButton(cancelMe), c1);

		transX.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Number nx = (Number) transX.getValue();
				model.setTranslate(new Point2D.Double(nx.doubleValue(), model.getTranslate().getY()));
			}
		});
		transY.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Number nx = (Number) transY.getValue();
				model.setTranslate(new Point2D.Double(model.getTranslate().getX(), nx.doubleValue()));
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
		shearX.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Number nx = (Number) shearX.getValue();
				model.setShearx(nx.intValue());
			}
		});
		shearY.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Number nx = (Number) shearY.getValue();
				model.setSheary(nx.intValue());
			}
		});
		angleSpinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Number nx = (Number) angleSpinner.getValue();
				model.setAngle(nx.doubleValue());
			}
		});

		pivotX.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Number nx = (Number) pivotX.getValue();
				model.setPivot(new Point2D.Double(nx.doubleValue(), model.getPivot().getY()));
			}
		});

		pivotY.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Number ny = (Number) pivotY.getValue();
				model.setPivot(new Point2D.Double(model.getPivot().getX(), ny.doubleValue()));
			}
		});

		Dimension d = getPreferredSize();
		d.width = 150;
		setMaximumSize(d);
		setPreferredSize(d);

		model.addPropertyChangeListener(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String name = evt.getPropertyName();

		if (name.equals(AffineModel.PROP_ANGLE)) {
			angleSpinner.setValue(Double.valueOf(model.getAngle()));
		} else if (name.equals(AffineModel.PROP_PIVOT)) {
			pivotX.setValue(Double.valueOf(model.getPivot().getX()));
			pivotY.setValue(Double.valueOf(model.getPivot().getY()));
		} else if (name.equals(AffineModel.PROP_SHEARX)) {
			shearX.setValue(Double.valueOf(model.getShearx()));
		} else if (name.equals(AffineModel.PROP_SHEARY)) {
			shearY.setValue(Double.valueOf(model.getSheary()));
		} else if (name.equals(AffineModel.PROP_SIZEX)) {
			sizeX.setValue(model.getSizex());
		} else if (name.equals(AffineModel.PROP_SIZEY)) {
			sizeY.setValue(model.getSizey());
		} else if (name.equals(AffineModel.PROP_TRANSLATE)) {
			transX.setValue(Double.valueOf(model.getTranslate().getX()));
			transY.setValue(Double.valueOf(model.getTranslate().getY()));
		}
	}
}
