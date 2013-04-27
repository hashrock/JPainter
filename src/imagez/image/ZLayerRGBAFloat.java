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
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 *FIXME: the get/setline functions don't take bounds.x/y into account (for most others too apart from rgbaInt class)
 * @author notzed
 */
public class ZLayerRGBAFloat extends ZLayerRGBA {

	float data[];
	ComponentSampleModel csm;

	public ZLayerRGBAFloat(ZImage zimage, Rectangle bounds) {
		super(zimage, bounds);

		data = new float[bounds.width * bounds.height * 4];

		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		ColorModel cm = new ComponentColorModel(cs, true, BlendMode.premultiplyAlpha, ColorModel.TRANSLUCENT, DataBuffer.TYPE_FLOAT);
		ComponentSampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_FLOAT, bounds.width, bounds.height, 4, bounds.width * 4, new int[]{0, 1, 2, 3});
		DataBufferFloat db = new DataBufferFloat(data, data.length);

		WritableRaster raster = Raster.createWritableRaster(sm, db, null);
		csm = sm;
		bimage = new BufferedImage(cm, raster, BlendMode.premultiplyAlpha, null);
	}

	@Override
	public ZLayer createTmpLayer() {
		return new ZLayerRGBASparseFloat(zimage, bounds);
	}

	@Override
	public int getStride() {
		return bounds.width*4;
	}

	public float[] getData() {
		return data;
	}

	@Override
	public void clear(Rectangle rect) {
		int height = rect.height;
		int width = rect.width;
		int off = csm.getOffset(rect.x, rect.y);
		int mod = csm.getScanlineStride() - width*4;

		for(int y=0;y<height;y++) {
			for (int x=0;x<width;x++) {
				data[off++] = 0;
				data[off++] = 0;
				data[off++] = 0;
				data[off++] = 0;
			}
			off += mod;
		}
		addDamage(rect);
	}

	@Override
	public void getLineRGBA(float[] dst, int doff, int x, int y, int width) {
		int off = csm.getOffset(x - bounds.x, y - bounds.y);

		System.arraycopy(data, off, dst, doff, width * 4);
	}

	@Override
	public void setLineRGBA(float[] src, int soff, int x, int y, int width) {
		int off = csm.getOffset(x - bounds.x, y - bounds.y);

		System.arraycopy(src, soff, data, off, width * 4);
	}

	@Override
	public int getChannelCount() {
		return 4;
	}

	@Override
	public void getLine(float[][] dst, int doff, int x, int y, int width) {
		int off = csm.getOffset(x - bounds.x, y - bounds.y);

		for (int i=0;i<width;i++) {
			dst[0][doff] = data[off++];
			dst[1][doff] = data[off++];
			dst[2][doff] = data[off++];
			dst[3][doff] = data[off++];
			doff += 1;
		}
	}

	@Override
	public void setLine(float[][] src, int soff, int x, int y, int width) {
		int off = csm.getOffset(x - bounds.x, y - bounds.y);

		for (int i=0;i<width;i++) {
			data[off++] = src[0][soff];
			data[off++] = src[1][soff];
			data[off++] = src[2][soff];
			data[off++] = src[3][soff];
			soff += 1;
		}
	}
}
