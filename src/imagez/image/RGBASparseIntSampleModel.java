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

import java.awt.image.DataBuffer;
import java.awt.image.SinglePixelPackedSampleModel;

/**
 *
 * @author notzed
 */
public class RGBASparseIntSampleModel  extends SinglePixelPackedSampleModel {
	public RGBASparseIntSampleModel(int dataType, int width, int height, int scanlineStride, int[] bitMasks) {
		super(dataType, width, height, scanlineStride, bitMasks);
	}

	@Override
	public DataBuffer createDataBuffer() {
		System.out.println("creating compatible data buffer?");
		return super.createDataBuffer();
	}

	@Override
	public void setPixel(int x, int y, float[] fArray, DataBuffer data) {
		System.out.println("setpixel 1");
		super.setPixel(x, y, fArray, data);
	}

	@Override
	public void setPixels(int x, int y, int w, int h, float[] fArray, DataBuffer data) {
		System.out.println("setpixels");
		super.setPixels(x, y, w, h, fArray, data);
	}

	@Override
	public void setPixel(int x, int y, int[] iArray, DataBuffer data) {
		System.out.println("setpixel int");
		super.setPixel(x, y, iArray, data);
	}

	@Override
	public void setSample(int x, int y, int b, float s, DataBuffer data) {
		System.out.println("set sample");
		super.setSample(x, y, b, s, data);
	}

	@Override
	public void setDataElements(int x, int y, Object obj, DataBuffer data) {
		RGBASparseIntDataBuffer db = (RGBASparseIntDataBuffer) data;
		int[] f = (int[]) obj;

		db.setDataElements(x, y, f);
	}

	@Override
	public void setDataElements(int x, int y, int w, int h, Object obj, DataBuffer data) {
		System.out.println("set data elements 2");
		super.setDataElements(x, y, w, h, obj, data);
	}

	@Override
	public Object getDataElements(int x, int y, Object obj, DataBuffer data) {
		RGBASparseIntDataBuffer db = (RGBASparseIntDataBuffer) data;
		int[] f = (int[]) obj;
		if (f == null) {
			f = new int[1];
		}

		return db.getDataElements(x, y, f);
	}
}
