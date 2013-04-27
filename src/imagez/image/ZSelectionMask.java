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

import java.awt.Rectangle;

/**
 * Simple 1 channel 'zimage' for selection masks (or other masks?)
 * @author notzed
 */
public class ZSelectionMask extends ZLayerMonoFloat {
	// extends ZMask?
/*
	Rectangle bounds;
	// do i really need floats?  probably not ... but ...
	float[] data;

	BufferedImage bimage;
*/
	public ZSelectionMask(Rectangle bounds) {
		super(null, bounds);
	}
}
