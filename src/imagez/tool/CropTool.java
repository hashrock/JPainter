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
package imagez.tool;

import imagez.image.ZImage;
import imagez.image.ZLayer;
import imagez.tool.ui.CropOptions;
import imagez.ui.ToolOverlay;
import imagez.ui.ImageView;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Crop tool.  Oddly enough.
 *
 * @author notzed
 */
public class CropTool extends ZDragTool {

	CropOverlay overlay;
	TopOverlay top;
	BottomOverlay bottom;
	LeftOverlay left;
	RightOverlay right;
	ZLayer scaledLayer;
	float scaleAmount;
	CropModel model;

	public CropTool() {
		model = new CropModel(new Point(10, 10), new Dimension(100, 100));
	}

	@Override
	public String getName() {
		return "Crop Tool";
	}

	@Override
	public void setImageView(ImageView source) {
		if (this.source != source) {
			if (this.source != null) {
				this.source.removeOverlay(overlay);
				this.source.removeOverlay(top);
				this.source.removeOverlay(bottom);
				this.source.removeOverlay(left);
				this.source.removeOverlay(right);
			}
			if (source != null) {
				model.setImageSize(source.getImage().getDimension());
				overlay = new CropOverlay(source);

				source.addOverlay(overlay);

				source.addOverlay(top = new TopOverlay(source));
				source.addOverlay(bottom = new BottomOverlay(source));
				source.addOverlay(left = new LeftOverlay(source));
				source.addOverlay(right = new RightOverlay(source));
			}
		}
		super.setImageView(source);
	}

	@Override
	public Component getWidget() {
		return new CropOptions(this, model);
	}

	public void cancel() {
		source.previousTool();
	}

	public void save() {
		ZLayer layer = getLayer();
		ZImage img = layer.getZImage();

		img.getEditor().cropImage(model.getBounds());

		this.source.previousTool();
	}

	abstract class HandleOverlay extends ToolOverlay {

		public HandleOverlay(ImageView layer) {
			super(layer);
			fill = Color.blue;
			alpha = 0f;
			model.addPropertyChangeListener(new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					rebuildPath();
				}
			});
			rebuildPath();
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			setAlpha(0.25f);
		}

		@Override
		public void mouseExited(MouseEvent e) {
			setAlpha(0);
		}
		int startx;
		int endx;
		int starty;
		int endy;
		Point2D start;

		@Override
		public void mousePressed(MouseEvent e) {
			start = layer.convertPoint(e.getPoint());
			startx = model.getOffx();
			starty = model.getOffy();
			endx = model.getSizex() + model.getOffx();
			endy = model.getSizey() + model.getOffy();
			e.consume();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			Point2D p = layer.convertPoint(e.getPoint());

			move((int) (p.getX() - start.getX()), (int) (p.getY() - start.getY()));
			e.consume();
		}

		abstract void move(int dx, int dy);

		protected void rebuildPath() {
			updateTransform();
		}
	}

	public class TopOverlay extends HandleOverlay {

		public TopOverlay(ImageView layer) {
			super(layer);
		}

		@Override
		void move(int dx, int dy) {
			model.setRange(model.getOffx(), starty + dy, model.getSizex() + model.getOffx(), endy);
		}

		@Override
		public void rebuildPath() {
			shape = new Rectangle2D.Float(model.offx, model.offy, model.sizex, 10);
			super.rebuildPath();
		}
	}

	public class BottomOverlay extends HandleOverlay {

		public BottomOverlay(ImageView layer) {
			super(layer);
		}

		@Override
		void move(int dx, int dy) {
			model.setRange(startx, starty, endx, endy + dy);
		}

		@Override
		public void rebuildPath() {
			shape = new Rectangle2D.Float(model.offx, model.offy + model.sizey - 10, model.sizex, 10);
			super.rebuildPath();
		}
	}

	public class LeftOverlay extends HandleOverlay {

		public LeftOverlay(ImageView layer) {
			super(layer);
		}

		@Override
		void move(int dx, int dy) {
			model.setRange(startx + dx, starty, endx, endy);
		}

		@Override
		public void rebuildPath() {
			shape = new Rectangle2D.Float(model.offx, model.offy, 10, model.sizey);
			super.rebuildPath();
		}
	}

	public class RightOverlay extends HandleOverlay {

		public RightOverlay(ImageView layer) {
			super(layer);
		}

		@Override
		void move(int dx, int dy) {
			model.setRange(startx, starty, endx + dx, endy);
		}

		@Override
		public void rebuildPath() {
			shape = new Rectangle2D.Float(model.offx + model.sizex - 10, model.offy, 10, model.sizey);
			super.rebuildPath();
		}
	}

	public class CropOverlay extends ToolOverlay {

		public CropOverlay(ImageView layer) {
			super(layer);
			shape = new Path2D.Float(Path2D.WIND_EVEN_ODD);
			fill = Color.red;
			stroke = new BasicStroke(1);
			paint = Color.black;
			alpha = 0.25f;
			rebuildPath();
			super.init();
			model.addPropertyChangeListener(cropListener);
		}

		@Override
		public boolean isMouseOver(Point2D p) {
			return true;
			//return model.getBounds().contains(p);
		}

		private void rebuildPath() {
			Path2D.Float s = (Path2D.Float) this.shape;

			s.reset();

			Dimension d = model.getImageSize();

			s.moveTo(0, 0);
			s.lineTo(d.width, 0);
			s.lineTo(d.width, d.height);
			s.lineTo(0, d.height);
			s.closePath();

			int l = model.getOffx();
			int r = l + model.getSizex();
			int t = model.getOffy();
			int b = t + model.getSizey();

			s.moveTo(l, t);
			s.lineTo(l, b);
			s.lineTo(r, b);
			s.lineTo(r, t);
			s.closePath();

			updateTransform();
			//scaledPath = new Path2D.Float(shape);
		}

		// FIXME: this should optimise the repaint based on the events recieved
		@Override
		public void repaint() {
			super.repaint();
			rebuildPath();
			super.repaint();
			//layer.repaint();
		}
		PropertyChangeListener cropListener = new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				String n = evt.getPropertyName();
				if (n.equals(CropModel.PROP_OFFX)) {
					repaint();
				} else if (n.equals(CropModel.PROP_OFFY)) {
					repaint();
				} else if (n.equals(CropModel.PROP_SIZEX)) {
					repaint();
				} else if (n.equals(CropModel.PROP_SIZEY)) {
					repaint();
				}
			}
		};
		Point2D start, finish;
		int startx, starty, sizex, sizey;
		int mode = 0;
		static final int CREATE = 0;
		static final int MOVE = 1;

		void updateModelDrag() {
			int x1 = (int) start.getX();
			int x2 = (int) finish.getX();
			int y1 = (int) start.getY();
			int y2 = (int) finish.getY();

			model.setRange(x1, y1, x2, y2);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			Point2D p = source.convertPoint(e.getPoint());

			// clicked inside - then move it
			if (model.getBounds().contains(p)) {
				start = source.convertPoint(e.getPoint());
				startx = model.getOffx();
				starty = model.getOffy();
				sizex = model.getSizex();
				sizey = model.getSizey();
				mode = MOVE;
			} else {
				finish = start = p;
				updateModelDrag();
				mode = CREATE;
			}
		}

		void domove(Point2D p) {
			int dx = startx + (int) (p.getX() - start.getX());
			int dy = starty + (int) (p.getY() - start.getY());

			model.setRange(dx, dy, dx + sizex, dy + sizey);
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			Point2D p = source.convertPoint(e.getPoint());

			switch (mode) {
				case MOVE:
					domove(p);
					break;
				case CREATE:
					finish = source.convertPoint(e.getPoint());
					updateModelDrag();
					break;
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			mouseDragged(e);
		}
	}
}
