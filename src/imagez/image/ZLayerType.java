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
import java.lang.reflect.Constructor;

/**
 * Layer type index.
 * 
 * @author notzed
 */
public enum ZLayerType {
	RGBA8("RGBA 8 bit", ZLayerRGBAInt.class),
	RGBA16("RGBA 16 bit", ZLayerRGBAShort16.class),
	RGBAF("RGBA float", ZLayerRGBAFloat.class),
	RGB8("RGB 8 bit", ZLayerRGBInt.class),
	RGB16("RGB 16 bit", ZLayerRGBShort16.class),
	//RGBF("RGB float", ZLayerRGBFloat.class),
	GA8("Greyscale Alpha 8 bit", ZLayerMonoAByte.class),
	//GA16("Greyscale Alpha 16 bit"),
	GAF("Greyscale Alpha float", ZLayerMonoAFloat.class),
	G8("Greyscale 8 bit", ZLayerMonoByte.class),
	//G16("Greyscale 16 bit"),
	GF("Greyscale float", ZLayerMonoFloat.class),
	;

	Class k;
	String label;
	private ZLayerType(String label, Class k) {
		this.label = label;
		this.k = k;
	}

	@Override
	public String toString() {
		return label;
	}

	public ZLayer createLayer(ZImage img, Rectangle bounds) {
		try {
			Constructor<ZLayer> c = k.getConstructor(ZImage.class, Rectangle.class);
			return c.newInstance(img, bounds);
		} catch (Exception ex) {
			throw new RuntimeException();
		}
	}
}
