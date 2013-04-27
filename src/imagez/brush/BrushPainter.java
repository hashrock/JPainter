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

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.LinkedList;

/**
 * Base class for painters.
 * 
 * Painters apply one dab of paint.
 * @author notzed
 */
abstract public class BrushPainter {

	Graphics2D gg;
	// For paint application
	// last paint location
	private double oldx, oldy;
	// total distance travelled
	private double distance;
	// total distance drawn
	private double drawn;
	// how often paint is applied
	private double step = 3.0;//3.0;

	public double getStep() {
		return step;
	}
	
	public void setStep(double step) {
		this.step = step;
	}
	
	/**
	 * Get ready to paint
	 * @param gg graphics context to draw to
	 */
	public void begin(Graphics2D gg, double px, double py) {
		this.gg = (Graphics2D) gg.create();

		oldx = px;
		oldy = py;
		distance = 0;
		drawn = 0;
	}

	/**
	 * Apply one dab of paint at the location
	 * @param px
	 * @param py 
	 * @param length how far along the drawing line we've drawn
	 */
	abstract public void paint(double px, double py, double length);

	/**
	 * Paint a line to the new location.
	 * @param x
	 * @param y 
	 */
	public void paintTo(double x, double y) {
		double dx = x - oldx;
		double dy = y - oldy;
		double len = Math.sqrt(dx * dx + dy * dy);

		if (drawn == 0) {
			paint(oldx, oldy, 0);
		}

		distance += len;
		while (drawn <= distance) {
			double t = (distance - drawn) / len;

			System.out.printf("painting at %f,%f %f\n", x - dx * t, y - dy * t, drawn);
			
			paint(x - dx * t, y - dy * t, drawn);
			drawn += step;
		}

		oldx = x;
		oldy = y;
	}

	/**
	 * Finish painting/cleanup
	 */
	public void end() {
		if (drawn == 0)
			paint(oldx, oldy, 0);
		gg.dispose();
	}
	LinkedList<BrushPainterListener> listeners = new LinkedList<BrushPainterListener>();

	public void addBrushPainterListener(BrushPainterListener l) {
		listeners.add(l);
	}

	public void removeBrushPainterListener(BrushPainterListener l) {
		listeners.remove(l);
	}

	protected void fireBrushDamaged(Rectangle damaged) {
		for (BrushPainterListener l : listeners) {
			l.brushDamaged(damaged);
		}
	}
}
