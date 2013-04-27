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

/**
 * Brush drawing type.
 * 
 * @author notzed
 */
public enum BrushDraw {
	Solid {

		@Override
		public BrushPainter getPainter(BrushContext bc) {
			return new SolidPainter(bc.getFill().getPaint(bc.getForegroundColour(), bc.getBackgroundColour(), bc.getRadiusX(), bc.getRadiusY()),
					bc.getShape().getShape(bc.getRadiusX(), bc.getRadiusY()));
		}
		
	},
	Fade {

		@Override
		public BrushPainter getPainter(BrushContext bc) {
			return new FadePainter(bc.getFill().getPaint(bc.getForegroundColour(), bc.getBackgroundColour(), bc.getRadiusX(), bc.getRadiusY()),
					bc.getShape().getShape(bc.getRadiusX(), bc.getRadiusY()), 50);
		}
		
	};
	
	public abstract BrushPainter getPainter(BrushContext bc);
}
