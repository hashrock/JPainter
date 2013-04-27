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
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferShort;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Arrays;

/**
 * Mono short, with no alpha
 * @author notzed
 */
public class ZLayerMonoShort extends ZLayerMono {

	short[] data;
	ComponentSampleModel csm;

	public ZLayerMonoShort(ZImage zimage, Rectangle bounds) {
		super(zimage, bounds);

		data = new short[bounds.width * bounds.height];

		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
		ColorModel cm = new ComponentColorModel(cs, false, false, ColorModel.OPAQUE, DataBuffer.TYPE_USHORT);
		csm = new ComponentSampleModel(DataBuffer.TYPE_USHORT, bounds.width, bounds.height, 1, bounds.width * 1, new int[]{0});
		DataBufferShort db = new DataBufferShort(data, data.length);
		WritableRaster wr = Raster.createWritableRaster(csm, db, null);
		bimage = new BufferedImage(cm, wr, false, null);
	}

	@Override
	public BufferedImage getImage() {
		return bimage;
	}

	@Override
	public int getStride() {
		return bounds.width;
	}

	@Override
	public void clear() {
		Arrays.fill(data, (short) 0);
	}

	@Override
	public void clear(Rectangle rect) {
		int l = bounds.x - rect.x;
		int r = l + rect.width;
		int t = bounds.y - rect.y;
		int b = t + rect.height;

		for (int y = t; y < b; y++) {
			int off = y * getStride();

			for (int x = l; x < r; x++) {
				data[off++] = 0;
			}
		}
	}

	@Override
	public void getLineMono(float[] dst, int doff, int x, int y, int width) {
		int off = csm.getOffset(x, y);

		for (int i = 0; i < width; i++) {
			dst[doff++] = (data[off++] & 0xffff) * (1.0f / 65535.0f);
		}
	}

	@Override
	public void setLineMono(float[] src, int soff, int x, int y, int width) {
		int off = csm.getOffset(x, y);

		for (int i = 0; i < width; i++) {
			int m = (int)(src[soff++] * 65535.0f + 0.5f);
			
			m = Math.min(m, 65535);
			m = Math.max(m, 0);

			data[off++] = (short)m;
		}
	}

	@Override
	public void getLineRGBA(float[] dst, int doff, int x, int y, int width) {
		int off = csm.getOffset(x, y);

		// what about alpha channel?

		for (int i = 0; i < width; i++) {
			float m = (data[off++] & 0xffff) * (1.0f / 65535.0f);

			dst[doff++] = m;
			dst[doff++] = m;
			dst[doff++] = m;
			dst[doff++] = 1;
		}
	}

	@Override
	public void setLineRGBA(float[] src, int soff, int x, int y, int width) {
		int off = csm.getOffset(x, y);
		float scale = 65535.0f;

		// fixme: background colour

		for (int i = 0; i < width; i++) {
			float r = src[soff++];
			float g = src[soff++];
			float b = src[soff++];
			float a = src[soff++];
			float m = (r + g + b) / 3.0f;

			int M = (int) (m * scale * a + 0.5f);

			M = Math.max(0, M);
			M = Math.min(65535, M);

			// pre-multiply alpha?

			data[off++] = (short) M;
		}
	}

	@Override
	public int getChannelCount() {
		return 1;
	}

	@Override
	public void getLine(float[][] dst, int doff, int x, int y, int width) {
		getLineMono(dst[0], doff, x, y, width);
	}

	@Override
	public void setLine(float[][] src, int soff, int x, int y, int width) {
		setLineMono(src[0], soff, x, y, width);
	}
}
