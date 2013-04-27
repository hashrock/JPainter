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

import imagez.tool.ui.PaintOptions;
import imagez.ui.ImageView;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Basic painting tool.
 *
 * Paint is applied at regular intervals based on travelled distance.
 * 
 * FIXME: this is probably to be removed at some point
 * @author notzed
 */
abstract public class PaintTool extends PenTool {

	Paint paint;
	Shape brush;
	// For paint application
	// last paint location
	double oldx, oldy;
	// total distance travelled
	double distance;
	// total distance drawn
	double drawn;
	// how often paint is applied
	double step = 3.0;//3.0;
	// fading out mode
	boolean fadeOut = false;
	float fadeMax = 100;
	// jitter mode
	boolean jitter;
	float jitterAmount;
	// incremental mode
	// in this mode the opacity is set on the alphacomposite directly, not on the pen layer
	boolean incremental;

	public PaintTool() {
	}
	public static final String PROP_BRUSH = "brush";

	public void setBrush(Shape brush) {
		Shape old = this.brush;

		if (!old.equals(brush)) {
			this.brush = brush;
			this.propertyChangeSupport.firePropertyChange(PROP_BRUSH, old, brush);
		}
	}

	public Shape getBrush() {
		return brush;
	}

	public Paint getPaint() {
		return source.getForegroundColour();
	}

	@Override
	public Color getColour() {
		return Color.yellow;
	}

	Point2D.Double previewMouseLocation;
	Rectangle previewDamage;
	//PPath dot;

	@Override
	public void setImageView(ImageView source) {
		if (source != this.source) {
			if (this.source != null) {
				//this.source.getXORLayer().removeAllChildren();
			}
		}
		super.setImageView(source);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (source == null)
			return;

		Point2D.Double at = getImageReal(e);
		/*PLayer pl = source.getXORLayer();

		if (dot == null || pl.indexOfChild(dot) == -1) {
			dot = new PPath(brush);
			dot.setPickable(false);
			pl.addChild(dot);
		}

		dot.setOffset(at.x, at.y);
		 * 
		 */

		if (true) {
			return;
		}

		// This is a bit shitty, it's tracking damage itself.
		// ideally want a 'display layer' this shit can be drawn
		// onto using a scene graph type thing, i guess
		Rectangle damaged = brush.getBounds();

		damaged.x += at.x - 4;
		damaged.y += at.y - 4;
		damaged.width += 8;
		damaged.height += 8;

		if (previewDamage != null)
			repaint(previewDamage);

		repaint(damaged);
		previewDamage = damaged;
		previewMouseLocation = at;
	}
/*
	@Override
	public void paint(Graphics2D g) {
		Point2D.Double at = previewMouseLocation;

		if (at == null)
			return;

		g.setStroke(new BasicStroke(1.0f));
		g.setXORMode(Color.red);
		g.setColor(Color.green);
		g.translate(at.x, at.y);
		g.draw(brush);
	}
*/
	@Override
	public Component getWidget() {
		return new PaintOptions(this);
	}

	/**
	 * Paint to a new location.  This will split the path into
	 * regularly spaced interfaces and call paintAt() to apply
	 * paint.
	 * @param x
	 * @param y
	 */
	public void paintTo(double x, double y) {
		double dx = x - oldx;
		double dy = y - oldy;
		double len = Math.sqrt(dx * dx + dy * dy);

		distance += len;
		while (drawn <= distance) {
			double t = (distance - drawn) / len;

			doPaintAt(x - dx * t, y - dy * t);
			drawn += step;
		}

		oldx = x;
		oldy = y;
	}

	/**
	 * Implementing classes apply one spot of paint here.
	 * @param x
	 * @param y
	 */
	//public void paintAt(double x, double y) {
	public Rectangle paintAt(double x, double y) {
		Graphics2D g = (Graphics2D) penGraphics.create();

		g.translate(x, y);
		g.fill(brush);
		g.dispose();

		Rectangle damaged = brush.getBounds();

		damaged.x += x - 4;
		damaged.y += y - 4;
		damaged.width += 8;
		damaged.height += 8;

		//if (!threadpaint) {
		penTouched(damaged);
		//}

		return damaged;
	}

	// TODO: migth want a 'set graphics for step' function
	void doPaintAt(double x, double y) {
		if (fadeOut) {
			if (distance >= fadeMax) {
				return;
			}
			penGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (fadeMax - distance) / fadeMax));
		}
		if (threadpaint) {
			// HACK: testing thread ideas
			// FIXME: this doesn't handle fade
			if (paintThread == null) {
				paintThread = new PaintThread();
				paintThread.start();
			}
			paintThread.paint.add(new Point2D.Double(x, y));
		} else {
			paintAt(x, y);
		}
	}
	boolean threadpaint = false;
	PaintThread paintThread;

	// testing threaded rendering
	class PaintThread extends Thread {

		LinkedBlockingQueue<Point2D> paint = new LinkedBlockingQueue<Point2D>();

		@Override
		public void run() {
			try {
				if (true) {
					while (true) {
						Point2D at = paint.take();
						Rectangle rect = new Rectangle();

						if (Double.isNaN(at.getY())) {
							break;
						}

						paintAt(at.getX(), at.getY());
					}
				} else {
					out:
					while (true) {
						Point2D at = paint.take();
						Rectangle rect = new Rectangle();

						while (at != null) {
							if (Double.isNaN(at.getY())) {
								break out;
							}

							rect = rect.union(paintAt(at.getX(), at.getY()));
							at = paint.poll();
						}
						penTouched(rect);
					}
				}
			} catch (InterruptedException ex) {
				Logger.getLogger(PaintTool.class.getName()).log(Level.SEVERE, null, ex);
			} catch (Exception x) {
				System.out.println("damnit");
			}
		}
	}

	@Override
	public void penDown(MouseEvent e) {
		super.penDown(e);

		penGraphics.setPaint(getPaint());

		Point2D.Double at = getImageReal(e);

		oldx = at.x;
		oldy = at.y;
		distance = 0;
		drawn = step;

		doPaintAt(oldx, oldy);
	}

	@Override
	public void penUp(MouseEvent e) {
		if (threadpaint) {
			paintThread.paint.add(new Point2D.Double(Double.NaN, Double.NaN));
			try {
				paintThread.join();
			} catch (InterruptedException ex) {
				Logger.getLogger(PaintTool.class.getName()).log(Level.SEVERE, null, ex);
			}
			paintThread = null;
		}
		super.penUp(e);
	}

	@Override
	public void penMoved(MouseEvent e) {
		// HACK: for drawing layer info
		mouseMoved(e);

		Point2D.Double at = getImageReal(e);

		paintTo(at.x, at.y);
	}
}
