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
import imagez.tool.ui.AffineOptions;
import imagez.ui.ImageView;
import imagez.ui.ToolOverlay;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Affine transformation tool.
 *
 * Combined rotate + translate + shear + scale.
 * 
 * @author notzed
 */
public class SuperAffineTool extends ZDragTool {

	//
	//SuperAffineThing thing;
	// old stuff to be deleted
	float angle = 0;
	Point pos;
	Point centre;
	// test idea - low quality high speed version
	boolean downscale = true;
	Image scaledImage;
	ZLayer scaledLayer;
	float scaleAmount;
	AffineModel amodel;
	// new stuff
	Centre cent;
	Pivot pivot;
	Rotate rotate;
	ScaleX scalex;
	ScaleY scaley;
	ShearX shearx;
	ShearY sheary;

	public SuperAffineTool() {
		amodel = new AffineModel(new Dimension());

		amodel.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (source != null) {
					source.repaint();
				}
			}
		});
	}

	@Override
	public String getName() {
		return "Super Affine Tool";
	}

	@Override
	public void setImageView(ImageView source) {
		if (this.source != source) {
			if (scaledImage != null) {
				scaledImage.flush();
				scaledImage = null;
			}
			pos = null;
			if (this.source != null) {
				this.source.removeOverlay(cent);
				this.source.removeOverlay(rotate);
				this.source.removeOverlay(pivot);
				this.source.removeOverlay(scalex);
				this.source.removeOverlay(scaley);
				this.source.removeOverlay(shearx);
				this.source.removeOverlay(sheary);
			}
			if (source != null) {
				Dimension d = source.getImage().getDimension();
				//Rectangle bounds = source.getCurrentLayer().getBounds();

				amodel.setSize(d);
				//amodel.setTranslate(bounds.getLocation());

				source.addOverlay(scalex = new ScaleX(source, amodel));
				source.addOverlay(scaley = new ScaleY(source, amodel));
				source.addOverlay(cent = new Centre(source, amodel));
				source.addOverlay(rotate = new Rotate(source, amodel));
				source.addOverlay(pivot = new Pivot(source, amodel));
				source.addOverlay(shearx = new ShearX(source, amodel));
				source.addOverlay(sheary = new ShearY(source, amodel));
			}
		}
		super.setImageView(source);
	}

	@Override
	public Component getWidget() {
		return new AffineOptions(this, amodel);
	}

	@Override
	public void paint(Graphics2D g) {
		BufferedImage img = getLayer().getImage();
		AffineTransform at = amodel.getCombinedTransform();

		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		//g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		//g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		//		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		//		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
		g.drawImage(img, at, null);
	}

	public void cancel() {
		source.previousTool();
		source.repaint();
	}

	public void save() {
		AffineTransform at = amodel.getCombinedTransform();
		ZLayer layer = getLayer();
		ZImage img = layer.getZImage();

		img.getEditor().affineLayer(layer, at, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

		amodel.reset();
		this.source.previousTool();
	}

	class HandleOverlay extends ToolOverlay {

		Point2D start;
		Point2D finish;
		final AffineModel model;
		static final float s = 3;

		public HandleOverlay(ImageView layer, AffineModel model) {
			super(layer);
			this.model = model;
			fill = Color.white;
			alpha = 0.25f;
			stroke = new BasicStroke(0.5f);
			paint = Color.black;
			model.addPropertyChangeListener(new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					update();
				}
			});
		}

		/**
		 * Overridden, these handles are screen-resolution so never updated.
		 * @param at 
		 */
		//@Override
		//public void updateTransform(AffineTransform at) {
		//	scaledPath = new Path2D.Float(shape);
		//	scaledLocation = new Point2D.Float(x, y);
		//}
		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
				start = layer.convertPoint(e.getPoint());
				e.consume();
			} else {
				start = null;
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (start != null) {
				finish = layer.convertPoint(e.getPoint());

				move((finish.getX() - start.getX()), (finish.getY() - start.getY()));
				e.consume();
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			mouseDragged(e);
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			setAlpha(1f);
		}

		@Override
		public void mouseExited(MouseEvent e) {
			setAlpha(0.25f);
		}

		void move(double dx, double dy) {
		}

		void update() {
		}
	}

	class Centre extends HandleOverlay {

		double startx, starty;

		public Centre(ImageView layer, AffineModel model) {
			super(layer, model);
			shape = new Ellipse2D.Float(-4 * s, -4 * s, 8 * s, 8 * s);
			update();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				startx = model.getTranslate().getX();
				starty = model.getTranslate().getY();
			}
			super.mousePressed(e);
		}

		@Override
		void move(double dx, double dy) {
			Point2D.Double dn = new Point2D.Double(startx + dx, starty + dy);

			model.setTranslate(dn);
		}

		@Override
		void update() {
			Point2D dd = model.getTranslate();
			Dimension d = layer.getCurrentLayer().getBounds().getSize();
			setLocation((float) dd.getX() + d.width / 2, (float) dd.getY() + d.height / 2);
		}
	}

	class Pivot extends HandleOverlay {

		double startx, starty;

		public Pivot(ImageView layer, AffineModel model) {
			super(layer, model);

			Area a = new Area(new Ellipse2D.Float(-8 * s, -8 * s, 8 * 2 * s, 8 * 2 * s));
			Area b = new Area(new Ellipse2D.Float(-6 * s, -6 * s, 6 * 2 * s, 6 * 2 * s));
			a.subtract(b);
			shape = new Path2D.Float(a);

			update();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				startx = model.getPivot().getX();
				starty = model.getPivot().getY();
			}
			super.mousePressed(e);
		}

		@Override
		void move(double dx, double dy) {
			System.out.println("pivot move");
			Point2D.Double dn = new Point2D.Double(startx + dx, starty + dy);

			model.setPivot(dn);
		}

		@Override
		void update() {
			Point2D dd = model.getPivot();
			Dimension d = layer.getCurrentLayer().getBounds().getSize();
			setLocation((float) dd.getX() + d.width / 2, (float) dd.getY() + d.height / 2);
		}
	}

	class ScaleX extends HandleOverlay {

		double sizex;

		public ScaleX(ImageView layer, AffineModel model) {
			super(layer, model);
			shape = new Rectangle2D.Float(-3 * s, 10 * s, 6 * s, 4 * s);
			Dimension d = layer.getCurrentLayer().getBounds().getSize();
			setLocation((float) (d.getWidth() / 2), (float) (d.getHeight() / 2));
			update();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				if (e.getClickCount() == 2) {
					model.setSizex(model.getSize().width);
					e.consume();
				}
				sizex = model.getSizex();
			}
			super.mousePressed(e);
		}

		@Override
		void move(double dx, double dy) {
			System.out.println("sizex move");
			model.setSizex((int) (sizex + dx));
		}

		@Override
		void update() {
			Rectangle2D.Float r = (Rectangle2D.Float) shape;
			float scale = (float) model.getSizex() / model.getSize().width;

			scale = Math.max(scale, 0.5f);

			repaint();
			r.setRect(-3 * s * scale, 10 * s, 6 * s * scale, 4 * s);
			updateTransform();
			repaint();
		}
	}

	class ScaleY extends HandleOverlay {

		double sizey;

		public ScaleY(ImageView layer, AffineModel model) {
			super(layer, model);
			shape = new Rectangle2D.Float();

			Dimension d = layer.getCurrentLayer().getBounds().getSize();
			setLocation((float) (d.getWidth() / 2), (float) (d.getHeight() / 2));

			update();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				if (e.getClickCount() == 2) {
					model.setSizey(model.getSize().height);
					e.consume();
				}
				sizey = model.getSizey();
			}
			super.mousePressed(e);
		}

		@Override
		void move(double dx, double dy) {
			model.setSizey((int) (sizey + dy));
		}

		@Override
		void update() {
			Rectangle2D.Float r = (Rectangle2D.Float) shape;
			float scale = (float) model.getSizey() / model.getSize().height;

			scale = Math.max(scale, 0.5f);

			repaint();
			r.setRect(-10 * s - 4 * s, -3 * s * scale, 4 * s, 6 * s * scale);
			updateTransform();
			repaint();
		}
	}

	class ShearX extends HandleOverlay {

		double shearx;

		public ShearX(ImageView layer, AffineModel model) {
			super(layer, model);

			Path2D.Float shearX = new Path2D.Float();

			shearX.moveTo(-4 * s, -10 * s);
			shearX.lineTo(2 * s, -10 * s);
			shearX.lineTo(4 * s, -14 * s);
			shearX.lineTo(-2 * s, -14 * s);
			shearX.closePath();

			shape = shearX;

			update();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				if (e.getClickCount() == 2) {
					model.setShearx(0);
					e.consume();
				}
				shearx = model.getShearx();
			}
			super.mousePressed(e);
		}

		@Override
		void move(double dx, double dy) {
			model.setShearx((float) (shearx + dx));
		}

		@Override
		void update() {
			Dimension d = layer.getCurrentLayer().getBounds().getSize();

			setLocation((float) (d.getWidth() / 2) + model.getShearx() / model.getSize().width * 64, (float) (d.getHeight() / 2));
		}
	}

	class ShearY extends HandleOverlay {

		double sheary;

		public ShearY(ImageView layer, AffineModel model) {
			super(layer, model);

			Path2D.Float shearY = new Path2D.Float();

			shearY.moveTo(10 * s, -4 * s);
			shearY.lineTo(10 * s, 2 * s);
			shearY.lineTo(14 * s, 4 * s);
			shearY.lineTo(14 * s, -2 * s);
			shearY.closePath();

			shape = shearY;

			update();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				if (e.getClickCount() == 2) {
					model.setSheary(0);
					e.consume();
				}
				sheary = model.getSheary();
			}
			super.mousePressed(e);
		}

		@Override
		void move(double dx, double dy) {
			model.setSheary((float) (sheary + dy));
		}

		@Override
		void update() {
			Dimension d = layer.getCurrentLayer().getBounds().getSize();

			setLocation((float) (d.getWidth() / 2), (float) (d.getHeight() / 2 + model.getSheary() / model.getSize().height * 64));
		}
	}

	class Rotate extends HandleOverlay {

		public Rotate(ImageView layer, AffineModel model) {
			super(layer, model);
			Area a = new Area(new Ellipse2D.Float(-21 * s, -21 * s, 21 * 2 * s, 21 * 2 * s));
			Area b = new Area(new Ellipse2D.Float(-18 * s, -18 * s, 18 * 2 * s, 18 * 2 * s));
			a.subtract(b);
			shape = new Path2D.Float(a);

			Dimension size = layer.getCurrentLayer().getBounds().getSize();
			setLocation(size.width / 2, size.height / 2);
		}
		Point2D pos;

		@Override
		public void mousePressed(MouseEvent event) {
			if (event.getClickCount() == 2) {
				model.setAngle(0);
			} else {
				super.mousePressed(event);
				pos = layer.convertPoint(event.getPoint());
			}
			event.consume();
		}

		@Override
		public void mouseDragged(MouseEvent event) {
			Point2D m = layer.convertPoint(event.getPoint());
			Point2D cc = new Point2D.Double(x, y);//rotate.getGlobalTranslation();

			double dx1 = cc.getX() - pos.getX();
			double dy1 = cc.getY() - pos.getY();
			double dx2 = cc.getX() - m.getX();
			double dy2 = cc.getY() - m.getY();

			double a = model.getAngle();

			a += -(Math.atan2(dx2, dy2) - Math.atan2(dx1, dy1)) * 180 / Math.PI;
			pos = m;

			System.out.println("mouse drag angle " + a);

			event.consume();
			model.setAngle(a);
		}
	}
}
