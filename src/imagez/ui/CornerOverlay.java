/*
 * Copyright (C) 2011 notzed
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package imagez.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Handles that track a rectangular model and allow the corners to be manipulated.
 * @author notzed
 */
public class CornerOverlay extends ToolOverlay {

	RectangleModel model;

	protected CornerOverlay(ImageView layer, RectangleModel model) {
		super(layer);
		shape = new Ellipse2D.Float();
		this.model = model;
		this.fill = Color.red;
		this.alpha = 0.25f;
		this.paint = Color.black;
		this.stroke = new BasicStroke(0.5f);

		//Ellipse2D.Float s = new Ellipse2D.Float();
		model.addPropertyChangeListener(modelChanged);

		update();
	}

	PropertyChangeListener modelChanged = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			modelChanged(evt);
		}
	};
	
	protected void update() {
	}

	protected void modelChanged(PropertyChangeEvent evt) {
	}
	protected Point2D start;
	protected int x1, y1;
	protected int x2, y2;

	@Override
	public void mouseEntered(MouseEvent e) {
		setAlpha(0.5f);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		setAlpha(0.25f);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			start = layer.convertPoint(e.getPoint());
			x1 = model.x1;
			y1 = model.y1;
			x2 = model.x2;
			y2 = model.y2;
			e.consume();
		} else {
			start = null;
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (start != null) {
			Point2D finish = layer.convertPoint(e.getPoint());

			move((int) (finish.getX() - start.getX()), (int) (finish.getY() - start.getY()));
			e.consume();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mouseDragged(e);
	}

	void move(int dx, int dy) {
	}

	void setCentre(int cx, int cy) {
		Ellipse2D.Float s = (Ellipse2D.Float) shape;

		repaint();

		s.setFrame(cx - 8, cy - 8, 17, 17);
		updateTransform();

		repaint();
	}

	public static class TLHandle extends CornerOverlay {

		public TLHandle(ImageView layer, RectangleModel model) {
			super(layer, model);
		}

		@Override
		void move(int dx, int dy) {
			dx += x1;
			dy += y1;

			model.setRange(dx, dy, x2, y2);
		}

		@Override
		protected void update() {
			setCentre(model.x1, model.y1);
		}

		@Override
		protected void modelChanged(PropertyChangeEvent evt) {
			String n = evt.getPropertyName();
			if (n.equals(RectangleModel.PROP_X1)
					|| n.equals(RectangleModel.PROP_Y1)) {
				update();
			}
		}
	}

	public static class TRHandle extends CornerOverlay {

		public TRHandle(ImageView layer, RectangleModel model) {
			super(layer, model);
		}

		

		@Override
		void move(int dx, int dy) {
			dx += x2;
			dy += y1;

			model.setRange(x1, dy, dx, y2);
		}

		@Override
		protected void update() {
			setCentre(model.x2, model.y1);
		}

		@Override
		protected void modelChanged(PropertyChangeEvent evt) {
			String n = evt.getPropertyName();
			if (n.equals(RectangleModel.PROP_X2)
					|| n.equals(RectangleModel.PROP_Y1)) {
				update();
			}
		}
	}

	public static class BLHandle extends CornerOverlay {

		public BLHandle(ImageView layer, RectangleModel model) {
			super(layer, model);
		}

		

		@Override
		void move(int dx, int dy) {
			dx += x1;
			dy += y2;

			model.setRange(dx, y1, x2, dy);
		}

		@Override
		protected void update() {
			setCentre(model.x1, model.y2);
		}

		@Override
		protected void modelChanged(PropertyChangeEvent evt) {
			String n = evt.getPropertyName();
			if (n.equals(RectangleModel.PROP_X1)
					|| n.equals(RectangleModel.PROP_Y2)) {
				update();
			}
		}
	}

	public static class BRHandle extends CornerOverlay {

		public BRHandle(ImageView layer, RectangleModel model) {
			super(layer, model);
		}

		@Override
		void move(int dx, int dy) {
			dx += x2;
			dy += y2;

			model.setRange(x1, y1, dx, dy);
		}

		@Override
		protected void update() {
			setCentre(model.x2, model.y2);
		}

		@Override
		protected void modelChanged(PropertyChangeEvent evt) {
			String n = evt.getPropertyName();
			if (n.equals(RectangleModel.PROP_X2)
					|| n.equals(RectangleModel.PROP_Y2)) {
				update();
			}
		}
	}
}