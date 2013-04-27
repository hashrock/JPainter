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

import imagez.image.ZSelectionModel;
import imagez.tool.SelectionTool;
import imagez.ui.ImageView;
import imagez.ui.SmallButton;
import imagez.ui.SmallCheckBox;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author notzed
 */
public class SelectionOptions extends JPanel implements ChangeListener, PropertyChangeListener {

	SelectionTool tool;
	ZSelectionModel model;
//
	JToggleButton antialias;
	JToggleButton feather;
	JSpinner featherAmount;

	public SelectionOptions(SelectionTool tool) {
		this.setLayout(new GridBagLayout());
		this.tool = tool;

		GridBagConstraints c0 = new GridBagConstraints();
		c0.gridx = 0;
		GridBagConstraints c1 = new GridBagConstraints();
		c1.gridx = 1;
		c1.weightx = 1;
		c1.fill = GridBagConstraints.HORIZONTAL;
		GridBagConstraints c01 = new GridBagConstraints();
		c01.gridx = 0;
		c01.gridwidth = 2;
		c01.fill = GridBagConstraints.HORIZONTAL;

		add(antialias = new SmallCheckBox("Anti Alias"), c01);
		add(feather = new SmallCheckBox("Feather"), c0);
		add(featherAmount = new JSpinner(new SpinnerNumberModel(10.0, 0, 100, 1.0)), c1);

		add(new SmallButton(new ClearSelectionAction()), c01);

		feather.addChangeListener(this);
		featherAmount.addChangeListener(this);

		add(Box.createVerticalGlue());

		//setModel(ToolModel.getInstance().getImageView().getCurrentSelection());
		setModel(tool.getSource().getCurrentSelection());
	}

	public void setModel(ZSelectionModel model) {
		if (this.model != model) {
			if (this.model != null) {
				this.model.removePropertyChangeListener(this);
			}

			this.model = model;
			if (model != null) {
				model.addPropertyChangeListener(this);

				antialias.setSelected(model.isAntiAliasingEnabled());
				feather.setSelected(model.isFeatherEnabled());
				featherAmount.getModel().setValue(model.getFeatherAmount());
			}
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (model != null) {
			System.out.println("updating feather stuff");
			model.setFeatherEnabled(feather.isSelected());
			Number n = (Number) featherAmount.getModel().getValue();
			model.setFeatherAmount(n.floatValue());
		} else {
			System.out.println("hmm, no model on selection options");
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();

		if (prop.equals(ZSelectionModel.PROP_FEATHERAMOUNT)) {
			Number n = (Number) evt.getNewValue();
			featherAmount.getModel().setValue(n.floatValue());
		} else if (prop.equals(ZSelectionModel.PROP_ANTIALIASINGENABLED)) {
			antialias.setSelected((Boolean) evt.getNewValue());
		} else if (prop.equals(ZSelectionModel.PROP_FEATHERENABLED)) {
			feather.setSelected((Boolean) evt.getNewValue());
		}
	}

	class ClearSelectionAction extends AbstractAction {
		public ClearSelectionAction() {
			super("Clear Selection");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (model != null) {
				model.clearSelection();
				tool.clearSelection();
			}
		}
	}
}
