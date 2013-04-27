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
import java.util.Arrays;

/**
 * Mono float + alpha
 * @author notzed
 */
public class ZLayerMonoAFloat extends ZLayerMono {

	float[] data;
	ComponentSampleModel csm;

	public ZLayerMonoAFloat(ZImage zimage, Rectangle bounds) {
		super(zimage, bounds);

		data = new float[bounds.width * bounds.height * 2];

		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
		ColorModel cm = new ComponentColorModel(cs, true, BlendMode.premultiplyAlpha, ColorModel.TRANSLUCENT, DataBuffer.TYPE_FLOAT);
		csm = new ComponentSampleModel(DataBuffer.TYPE_FLOAT, bounds.width, bounds.height, 2, bounds.width * 2, new int[]{0, 1});
		DataBufferFloat db = new DataBufferFloat(data, data.length);
		WritableRaster wr = Raster.createWritableRaster(csm, db, null);

		bimage = new BufferedImage(cm, wr, BlendMode.premultiplyAlpha, null);
	}

	@Override
	public BufferedImage getImage() {
		return bimage;
	}

	@Override
	public int getStride() {
		return bounds.width * 2;
	}

	@Override
	public void clear() {
		Arrays.fill(data, 0);
	}

	@Override
	public void clear(Rectangle rect) {
		int l = bounds.x - rect.x;
		int r = l + rect.width;
		int t = bounds.y - rect.y;
		int b = t + rect.height;
		
		for (int y=t;y<b;y++) {
			int off = y*getStride();
			
			for (int x=l;x<r;x++) {
				data[off++] = 0;
				data[off++] = 0;
			}
		}
	}

	@Override
	public void getLineMono(float[] dst, int doff, int x, int y, int width) {
		int off = csm.getOffset(x, y);

		System.arraycopy(data, off, dst, doff, width);
	}

	@Override
	public void setLineMono(float[] src, int soff, int x, int y, int width) {
		int off = csm.getOffset(x, y);

		System.arraycopy(src, soff, data, off, width);
	}

	@Override
	public void getLineRGBA(float[] dst, int doff, int x, int y, int width) {
		int off = csm.getOffset(x, y);

		for (int i=0;i<width;i++) {
			float m = data[off++];
			float a = data[off++];
			
			dst[doff++] = m;
			dst[doff++] = m;
			dst[doff++] = m;
			dst[doff++] = a;
		}
	}

	@Override
	public void setLineRGBA(float[] src, int soff, int x, int y, int width) {
		int off = csm.getOffset(x, y);

		// how do i merge the colours?

		for (int i=0;i<width;i++) {
			float r = src[soff++];
			float g = src[soff++];
			float b = src[soff++];
			float a = src[soff++];

			data[off++] = (r+g+b) * (1.0f/3.0f);
			data[off++] = a;
		}
	}

	@Override
	public int getChannelCount() {
		return 2;
	}

	@Override
	public void getLine(float[][] dst, int doff, int x, int y, int width) {
		int off = csm.getOffset(x, y);

		for (int i=0;i<width;i++) {
			float m = data[off++];
			float a = data[off++];

			dst[0][doff] = m;
			dst[1][doff] = a;
			doff += 1;
		}
	}

	@Override
	public void setLine(float[][] src, int soff, int x, int y, int width) {
		int off = csm.getOffset(x, y);

		for (int i=0;i<width;i++) {
			float m = src[0][soff];
			float a = src[1][soff];

			soff ++;

			data[off++] = m;
			data[off++] = a;
		}
	}
}
