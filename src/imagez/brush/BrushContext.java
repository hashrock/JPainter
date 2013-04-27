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
package imagez.brush;

import imagez.ui.ImageView;
import imagez.ui.ImageWindow;
import imagez.ui.PoxyColourChooser;
import imagez.ui.SmallComboBox;
import imagez.ui.SmallLabel;
import imagez.ui.SmallSpinner;
import imagez.ui.ToolbarSection;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Global brush info, such as colours (and size?)
 * @author notzed
 */
public class BrushContext implements Serializable {
	static final long serialVersionUID = 7603470090810641452L;
	
	public JComponent getWidget(final ImageView view) {
		JPanel jp = new JPanel(new GridBagLayout());
		SpinnerModel radxmodel = new SpinnerNumberModel(radiusX, 0.1, 100, 0.1);
		final JSpinner radxspin = new SmallSpinner(radxmodel);
		final JSlider radxslider = new JSlider(1, 1000, (int) (radiusX * 10));

		//jp.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 8));

		jp.add(new SmallLabel("Radius X"), ToolbarSection.c0);
		jp.add(radxspin, ToolbarSection.c1);
		jp.add(radxslider, ToolbarSection.c01);

		SpinnerModel radymodel = new SpinnerNumberModel(radiusY, 0.1, 100, 0.1);
		final JSpinner radyspin = new SmallSpinner(radymodel);
		final JSlider radyslider = new JSlider(1, 1000, (int) (radiusY * 10));

		jp.add(new SmallLabel("Radius Y"), ToolbarSection.c0);
		jp.add(radyspin, ToolbarSection.c1);
		jp.add(radyslider, ToolbarSection.c01);

		final JComboBox shapelist = new SmallComboBox(new DefaultComboBoxModel(BrushShape.icons()));
		shapelist.setSelectedItem(shape.icon);
		shapelist.setToolTipText("Brush Shape");

		final JComboBox filllist = new SmallComboBox(new DefaultComboBoxModel(BrushFill.icons()));
		filllist.setSelectedItem(fill.icon);
		filllist.setToolTipText("Fill Style");

		JPanel brush = new JPanel();
		brush.setLayout(new BoxLayout(brush, BoxLayout.X_AXIS));

		final ColourIcon coli = new ColourIcon(foregroundColour, backgroundColour);
		final JButton colb = new JButton(coli);

		final PaintIcon fgpaint = new PaintIcon(foregroundColour);
		final PaintIcon bgpaint = new PaintIcon(backgroundColour);
		final JButton fgicon = new JButton(fgpaint);
		final JButton bgicon = new JButton(bgpaint);

		fgicon.setBorder(BorderFactory.createRaisedBevelBorder());
		bgicon.setBorder(BorderFactory.createRaisedBevelBorder());

		colb.setBorder(BorderFactory.createRaisedBevelBorder());
		shapelist.setBorder(BorderFactory.createRaisedBevelBorder());
		filllist.setBorder(BorderFactory.createRaisedBevelBorder());

		ColourButton cb = new ColourButton();
		cb.setBackgroundColour(backgroundColour);

		brush.add(Box.createHorizontalGlue());
		brush.add(shapelist);
		//brush.add(fgicon);
		//brush.add(bgicon);
		brush.add(filllist);
		brush.add(colb);
		brush.add(Box.createHorizontalGlue());

		jp.add(brush, ToolbarSection.c01);

		shapelist.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					JComboBox box = (JComboBox) e.getSource();

					setShape(BrushShape.values()[box.getSelectedIndex()]);
				}
			}
		});

		filllist.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					JComboBox box = (JComboBox) e.getSource();

					setFill(BrushFill.values()[box.getSelectedIndex()]);
				}
			}
		});

		colb.addMouseListener(new MouseAdapter() {

			PoxyColourChooser pcc;

			@Override
			public void mouseClicked(MouseEvent e) {
				Point p = e.getPoint();

				if (e.isConsumed()) {// || !(e.getClickCount() == 2)) {
					return;
				}
				e.consume();

				int which = coli.checkClick(p);
				if (which != ColourIcon.CLICK_NONE) {
					if (pcc == null) {
						pcc = new PoxyColourChooser(BrushContext.this);
					}
					ImageWindow win = (ImageWindow) view.getTopLevelAncestor();
					pcc.setMode(which == ColourIcon.CLICK_FG ? PoxyColourChooser.MODE_FG : PoxyColourChooser.MODE_BG);
					win.setOverlay(pcc);
					pcc.setVisible(true);
				}
			}
		});
		colb.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
			}
		});

		addPropertyChangeListener(new PropertyChangeListener() {

			void updateShape() {
				shapelist.setSelectedItem(shape.icon);
			}

			void updateFill() {
				filllist.setSelectedItem(fill.icon);
			}

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(PROP_RADIUSX)) {
					radxslider.setValue((int) (radiusX * 10));
					radxspin.setValue(radiusX);
					updateShape();
				} else if (evt.getPropertyName().equals(PROP_RADIUSY)) {
					radyslider.setValue((int) (radiusY * 10));
					radyspin.setValue(radiusY);
					updateShape();
				} else if (evt.getPropertyName().equals(PROP_FOREGROUNDCOLOUR)) {
					fgpaint.setPaint(foregroundColour);
					coli.setForeground(foregroundColour);
					colb.repaint();
					fgicon.repaint();
				} else if (evt.getPropertyName().equals(PROP_BACKGROUNDCOLOUR)) {
					bgpaint.setPaint(backgroundColour);
					coli.setBackground(backgroundColour);
					colb.repaint();
					bgicon.repaint();
				} else if (evt.getPropertyName().equals(PROP_SHAPE)) {
					updateShape();
				} else if (evt.getPropertyName().equals(PROP_FILL)) {
					updateFill();
				}
			}
		});
		radxslider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				setRadiusX(radxslider.getValue() / 10.0f);
			}
		});
		radxspin.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				setRadiusX(((Number) radxspin.getValue()).floatValue());
			}
		});
		radyslider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				setRadiusY(radyslider.getValue() / 10.0f);
			}
		});
		radyspin.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				setRadiusY(((Number) radyspin.getValue()).floatValue());
			}
		});

		Dimension d = jp.getPreferredSize();
		d.width = 150;
		jp.setPreferredSize(d);
		jp.setMaximumSize(d);

		return jp;
	}
	
	//
	public Paint getPaint() {
		return getFill().getPaint(foregroundColour, backgroundColour, radiusX, radiusY);
	}
	public Shape getPaintShape() {
		return getShape().getShape(radiusX, radiusY);
	}
	
	//
	protected Color foregroundColour = Color.BLACK;
	public static final String PROP_FOREGROUNDCOLOUR = "foregroundColour";

	public Color getForegroundColour() {
		return foregroundColour;
	}

	public void setForegroundColour(Color foregroundColour) {
		Color oldForegroundColour = this.foregroundColour;
		this.foregroundColour = foregroundColour;
		propertyChangeSupport.firePropertyChange(PROP_FOREGROUNDCOLOUR, oldForegroundColour, foregroundColour);
	}
	protected Color backgroundColour = Color.WHITE;
	public static final String PROP_BACKGROUNDCOLOUR = "backgroundColour";

	public Color getBackgroundColour() {
		return backgroundColour;
	}

	public void setBackgroundColour(Color backgroundColour) {
		Color oldBackgroundColour = this.backgroundColour;
		this.backgroundColour = backgroundColour;
		propertyChangeSupport.firePropertyChange(PROP_BACKGROUNDCOLOUR, oldBackgroundColour, backgroundColour);
	}
	protected float radiusX = 5;
	public static final String PROP_RADIUSX = "radiusX";

	public float getRadiusX() {
		return radiusX;
	}

	public void setRadiusX(float radiusX) {
		float oldRadiusX = this.radiusX;
		this.radiusX = radiusX;
		propertyChangeSupport.firePropertyChange(PROP_RADIUSX, oldRadiusX, radiusX);
	}
	protected float radiusY = 5;
	public static final String PROP_RADIUSY = "radiusY";

	public float getRadiusY() {
		return radiusY;
	}

	public void setRadiusY(float radiusY) {
		float oldRadiusY = this.radiusY;
		this.radiusY = radiusY;
		propertyChangeSupport.firePropertyChange(PROP_RADIUSY, oldRadiusY, radiusY);
	}
	protected BrushShape shape = BrushShape.Ellipse;
	public static final String PROP_SHAPE = "shape";

	public BrushShape getShape() {
		return shape;
	}

	public void setShape(BrushShape shape) {
		BrushShape oldShape = this.shape;
		this.shape = shape;
		propertyChangeSupport.firePropertyChange(PROP_SHAPE, oldShape, shape);
	}
	protected BrushFill fill = BrushFill.Solid;
	public static final String PROP_FILL = "fill";

	public BrushFill getFill() {
		return fill;
	}

	public void setFill(BrushFill fill) {
		BrushFill oldFill = this.fill;
		this.fill = fill;
		propertyChangeSupport.firePropertyChange(PROP_FILL, oldFill, fill);
	}
	
	protected BrushDraw draw = BrushDraw.Solid;
	public static final String PROP_DRAW = "draw";

	public BrushDraw getDraw() {
		return draw;
	}

	public void setDraw(BrushDraw draw) {
		BrushDraw oldDraw = this.draw;
		this.draw = draw;
		propertyChangeSupport.firePropertyChange(PROP_DRAW, oldDraw, draw);
	}
	
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}
}
