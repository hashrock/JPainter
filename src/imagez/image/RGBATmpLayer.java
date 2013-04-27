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

import imagez.blend.BlendMode;
import java.awt.Rectangle;

/**
 * Temporary layer.
 *
 * Wraps an RGBA layer but stops passing damage events to the zimage for
 * settings.
 * @author notzed
 */
public class RGBATmpLayer extends ZLayerRGBAFloat {

	public RGBATmpLayer(ZImage image, Rectangle bounds) {
		super(image, bounds);
	}

	@Override
	public void setOpacity(float opacity) {
		this.opacity = opacity;
	}

	@Override
	public void setMode(BlendMode mode) {
		this.mode = mode;
	}
}
