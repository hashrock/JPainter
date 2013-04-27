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
package imagez.image;

import java.awt.Color;
import java.awt.Rectangle;

/**
 * A layer with no alpha.
 *
 * Really only used for loading/saving stuff?  Although perhaps it would still work.
 * @author notzed
 */
public abstract class ZLayerRGB extends ZLayer<ZLayerRGB> {

	float bgRed;
	float bgGreen;
	float bgBlue;
	
	public ZLayerRGB(ZImage zimage, Rectangle bounds) {
		super(zimage, bounds);
	}

	/**
	 * Sets the background colour to use when storing alpha adjusted values where alpha != 1
	 * @param r
	 * @param g
	 * @param b
	 */
	 public void setBackground(float r, float g, float b) {
		 bgRed = r;
		 bgGreen = g;
		 bgBlue = b;
	 }
}
