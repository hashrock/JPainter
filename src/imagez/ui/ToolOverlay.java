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

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

public class ToolOverlay {

	protected float x, y;
	protected Shape shape;
	protected Paint fill;
	protected Stroke stroke;
	protected Paint paint;
	protected Path2D.Float scaledPath;
	protected Point2D.Float scaledLocation;
	protected final ImageView layer;
	protected float alpha = 1.0f;

	public ToolOverlay(ImageView layer) {
		this.layer = layer;
	}

	public ToolOverlay(ImageView layer, Shape shape, Paint paint, Stroke stroke) {
		this.layer = layer;
		this.shape = shape;
		this.fill = paint;
		this.paint = paint;
		this.stroke = stroke;
		init();
	}

	protected void init() {
		scaledPath = new Path2D.Float(shape);
	}

	public void setLocation(float x, float y) {
		Rectangle oldbounds = shape.getBounds();
		oldbounds.translate((int) this.x, (int) this.y);
		oldbounds.grow(1, 1);

		this.x = x;
		this.y = y;

		updateTransform(layer.getScreenToTool());

		Rectangle newbounds = shape.getBounds();
		newbounds.translate((int) x, (int) y);
		newbounds.grow(1, 1);

		//repaint(oldbounds);
		//repaint(newbounds);
		layer.repaintImage(oldbounds);
		layer.repaintImage(newbounds);
	}

	protected void updateTransform() {
		updateTransform(layer.getScreenToTool());
	}

	public void updateTransform(AffineTransform at) {
		scaledPath = new Path2D.Float(shape);
		scaledPath.transform(at);
		scaledLocation = new Point2D.Float(x, y);
		at.transform(scaledLocation, scaledLocation);
	}

	public void setAlpha(float alpha) {
		if (alpha != this.alpha) {
			this.alpha = alpha;

			repaint();
		}
	}

	public void setFill(Paint fill) {
		if (!fill.equals(this.fill)) {
			this.fill = fill;
			repaint();
		}
	}

	/**
	 * Repaint the current region
	 */
	public void repaint() {
		Rectangle b = shape.getBounds();
		b.grow(1, 1);
		b.translate((int) x, (int) y);
		layer.repaintImage(b);
	}

	public boolean isMouseOver(Point2D p) {
		//Point2D t = new Point2D.Double(p.getX() - scaledLocation.getX(), p.getY() - scaledLocation.getY());
		Point2D t = new Point2D.Double(p.getX() - x, p.getY() - y);

		return shape.contains(t);
	}

	/**
	 * Paints object, using global screen coordinates.
	 * @param gg 
	 */
	public void paint(Graphics2D gg) {
		if (alpha == 0) {
			return;
		}

		gg = (Graphics2D) gg.create();
		gg.translate(scaledLocation.x, scaledLocation.y);

		if (fill != null) {
			if (alpha != 1.0f) {
				gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
			}
			gg.setPaint(fill);
			gg.fill(scaledPath);
			if (alpha != 1.0f) {
				gg.setPaintMode();
			}
		}
		if (stroke != null) {
			gg.setPaint(paint);
			gg.setStroke(stroke);
			gg.draw(scaledPath);
		}

		gg.dispose();
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
	}
}