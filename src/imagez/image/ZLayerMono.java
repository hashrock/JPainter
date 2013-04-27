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
 *
 * @author notzed
 */
public abstract class ZLayerMono extends ZLayer {
	public ZLayerMono(ZImage zimage, Rectangle bounds) {
		super(zimage, bounds);
	}

	@Override
	public ZLayer createTmpLayerOverlay() {
		return new ZLayerMonoAByte(zimage, bounds);
	}

	// TODO: these are no longer needed, getLine() returns a single channel already
	abstract public void getLineMono(float[] dst, int doff, int x, int y, int width);
	abstract public void setLineMono(float[] src, int soff, int x, int y, int width);
}
