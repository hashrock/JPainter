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
import java.awt.geom.Point2D;
import javax.swing.Icon;

/**
 *
 * @author notzed
 */
public enum BrushFill {

	/**
	 * Solid colour fill.
	 */
	Solid {

		@Override
		public Paint getPaint(Color fg, Color bg, float radx, float rady) {
			return fg;
		}
		
	},
	/**
	 * Radial gradient fill
	 */
	Radial {

		@Override
		public Paint getPaint(Color fg, Color bg, float radx, float rady) {
			float rgba[] = new float[4];

			fg.getComponents(rgba);
			Color edge = new Color(rgba[0], rgba[1], rgba[2], 0f);
			return new RadialGradientPaint(new Point2D.Float(0, 0), radx,
					new float[]{0.0f, 1.0f}, new Color[]{fg, edge});
		}
	};

	public abstract Paint getPaint(Color fg, Color bg, float radx, float rady);
	
	final Icon icon;

	private BrushFill() {
		icon = new FillIcon(getPaint(Color.black, Color.white, 12, 12));
	}
	private static Icon[] icons;

	static public Icon[] icons() {
		if (icons == null) {
			BrushFill[] bss = values();
			icons = new Icon[bss.length];
			for (int i=0;i<bss.length;i++) {
				icons[i] = bss[i].icon;
			}
		}
		return icons;
	}

}
