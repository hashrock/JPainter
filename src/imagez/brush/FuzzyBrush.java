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

import java.awt.Color;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.Point2D;

/**
 * A fuzzy brush of single colour
 * @author notzed
 */
public class FuzzyBrush extends Brush {

	Shape shape;
	Paint paint;
	Color oldfg;
	float oldrad;

	public FuzzyBrush(BrushContext bc) {
		super(bc);
	}

	public Shape getShape() {
		float rad = bc.getRadiusX();
		if (rad != oldrad) {
			shape = new java.awt.geom.Ellipse2D.Float(-rad, -rad, rad*2, rad*2);
		}
		return shape;
	}
	
	@Override
	public Paint getPaint() {
		Color fg = bc.getForegroundColour();
		float rad = bc.getRadiusX();

		if (rad != oldrad || !fg.equals(oldfg)) {
			float rgba[] = new float[4];

			fg.getComponents(rgba);
			Color edge = new Color(rgba[0], rgba[1], rgba[2], 0f);

			oldfg = fg;
			paint = new RadialGradientPaint(new Point2D.Float(0, 0), bc.getRadiusX(),
					new float[]{0.0f, 1.0f}, new Color[]{fg, edge});
		}
		return paint;
	}
}
