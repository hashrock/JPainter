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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/**
 * Quick and dirty information box.  Mouse location and zoom at present.
 *
 * @author notzed
 */
public class ImageData extends JPanel {

	JComboBox zoomLevel;
	JLabel mouseLocation;
	JToggleButton showLayers;
	JToggleButton showSelection;
	private final ImageView imageView;

	public ImageData(ImageView iv) {
		super();
		this.imageView = iv;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel options = new JPanel();
		options.setOpaque(false);
		add(options);
		options.setLayout(new GridBagLayout());

		// without this it expands to fill it's container.  with it it shrinks to fit it's contents.  well fuck that for a joke
		options.setMaximumSize(new Dimension(200, 90));

		GridBagConstraints c0 = new GridBagConstraints();
		c0.gridx = 0;
		c0.anchor = GridBagConstraints.BASELINE_LEADING;
		c0.ipadx = 3;
		c0.insets = new Insets(1, 2, 1, 2);
		c0.fill = GridBagConstraints.HORIZONTAL;
		options.add(new SmallLabel("Mouse"), c0);
		options.add(new SmallLabel("Zoom"), c0);
		GridBagConstraints c1 = new GridBagConstraints();
		c1.gridx = 1;
		c1.insets = new Insets(1, 2, 1, 2);
		c1.weightx = 1;
		c1.anchor = GridBagConstraints.BASELINE_LEADING;
		c1.fill = GridBagConstraints.HORIZONTAL;

		mouseLocation = new SmallLabel("x, y");
		options.add(mouseLocation, c1);

		DefaultComboBoxModel zoomModel = new DefaultComboBoxModel(new Float[]{1 / 8f, 1 / 7f, 1 / 6f, 1 / 5f, 1 / 4f, 1 / 3f, 1 / 2f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f});

		zoomLevel = new SmallComboBox(zoomModel);
		zoomLevel.setSelectedItem(Float.valueOf(1f));
		zoomLevel.setMaximumRowCount(50);
		options.add(zoomLevel, c1);

		GridBagConstraints c2 = new GridBagConstraints();
		c2.gridwidth = 2;
		c2.gridx = 0;
		c2.fill = GridBagConstraints.HORIZONTAL;

		JPanel rest = new JPanel();
		options.add(rest, c2);
		rest.setLayout(new GridLayout(1, 3));

		rest.add(showLayers = new SmallToggleButton(showLayersAction));
		rest.add(showSelection = new SmallToggleButton(showSelectionAction));

		zoomLevel.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				Number n = (Number) zoomLevel.getSelectedItem();

				imageView.setZoom(n.floatValue());
			}
		});

		iv.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				String name = evt.getPropertyName();

				if (name.equals(ImageView.PROP_ZOOM)) {
					zoomLevel.getModel().setSelectedItem(evt.getNewValue());
				}
			}
		});

		iv.addMouseMotionListener(new MouseMotionAdapter() {

			void showMouse(MouseEvent e) {
				Point2D p = imageView.convertPoint(e.getPoint());
				mouseLocation.setText(String.format("%d, %d", (int) p.getX(), (int) p.getY()));
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				showMouse(e);
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				showMouse(e);
			}
		});
	}
	ShowLayersAction showLayersAction = new ShowLayersAction();
	ShowSelectionAction showSelectionAction = new ShowSelectionAction();

	class ShowLayersAction extends AbstractToggleAction {

		public ShowLayersAction() {
			super("lay");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			imageView.setShowLayerEdges(selected);
		}
	}

	class ShowSelectionAction extends AbstractToggleAction {

		public ShowSelectionAction() {
			super("sel");
			selected = false;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			imageView.setShowSelection(selected);
		}
	}
}
