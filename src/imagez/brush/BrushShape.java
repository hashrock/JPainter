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

import java.awt.Shape;
import javax.swing.Icon;

/**
 *
 * @author notzed
 */
public enum BrushShape {

	Ellipse {
		@Override
		public Shape getShape(float sx, float sy) {
			return new java.awt.geom.Ellipse2D.Float(-sx, -sy, sx * 2, sy * 2);
		}
	},
	Rectangle {

		@Override
		public Shape getShape(float sx, float sy) {
			return new java.awt.geom.Rectangle2D.Float(-sx, -sy, sx * 2, sy * 2);
		}
	};
	// star?

	final Icon icon;

	private BrushShape() {
		icon = new ShapeIcon(getShape(12, 12));
	}
	
	public abstract Shape getShape(float sx, float sy);
	private static Icon[] icons;

	static public Icon[] icons() {
		if (icons == null) {
			BrushShape[] bss = values();
			icons = new Icon[bss.length];
			for (int i=0;i<bss.length;i++) {
				icons[i] = bss[i].icon;
			}
		}
		return icons;
	}
}
