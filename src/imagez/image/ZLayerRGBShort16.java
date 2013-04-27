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

/**
 * Layer backed by short array with short elements.
*
 * @author notzed
 */
public class ZLayerRGBShort16 extends ZLayerRGB {

	short data[];
	ComponentSampleModel csm;

	public ZLayerRGBShort16(ZImage zimage, Rectangle bounds) {
		super(zimage, bounds);

		data = new short[bounds.width * bounds.height * 3];

		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		ColorModel cm = new ComponentColorModel(cs, false, false, ColorModel.OPAQUE, DataBuffer.TYPE_USHORT);
		ComponentSampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_USHORT, bounds.width, bounds.height, 3, bounds.width * 3, new int[]{0, 1, 2});
		DataBufferShort db = new DataBufferShort(data, data.length);

		WritableRaster raster = Raster.createWritableRaster(sm, db, null);
		csm = sm;
		bimage = new BufferedImage(cm, raster, true, null);
	}

	@Override
	public int getStride() {
		return bounds.width*3;
	}

	public short[] getData() {
		return data;
	}

	@Override
	public void clear(Rectangle rect) {
		int height = rect.height;
		int width = rect.width;
		int off = csm.getOffset(rect.x, rect.y);
		int mod = csm.getScanlineStride() - width*3;

		for(int y=0;y<height;y++) {
			for (int x=0;x<width;x++) {
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
		float scale = 1.0f / 65535f;

		for (int i=0;i<width;i++) {
			int R = data[off++] & 0xffff;
			int G = data[off++] & 0xffff;
			int B = data[off++] & 0xffff;

			dst[doff++] = R * scale;
			dst[doff++] = G * scale;
			dst[doff++] = B * scale;
			dst[doff++] = 1;
		}
	}

	@Override
	public void setLineRGBA(float[] src, int soff, int x, int y, int width) {
		int off = csm.getOffset(x - bounds.x, y - bounds.y);
		float scale = 65535.0f;
		float br = bgRed;
		float bg = bgGreen;
		float bb = bgBlue;;

		for (int i=0;i<width;i++) {
			float A = src[soff+3];
			float a1 = 1-A;
			int R = (int) ((br * a1 + src[soff+0]) * scale + 0.5f);
			int G = (int) ((bg * a1 + src[soff+1]) * scale + 0.5f);
			int B = (int) ((bb * a1 + src[soff+2]) * scale + 0.5f);

			soff += 4;

			R = Math.max(R, 0);
			G = Math.max(G, 0);
			B = Math.max(B, 0);

			R = Math.min(R, 65535);
			G = Math.min(G, 65535);
			B = Math.min(B, 65535);

			data[off++] = (short)R;
			data[off++] = (short)G;
			data[off++] = (short)B;
		}
	}

	@Override
	public int getChannelCount() {
		return 3;
	}

	@Override
	public void getLine(float[][] dst, int doff, int x, int y, int width) {
		if (true)
		throw new RuntimeException();
		int off = csm.getOffset(x - bounds.x, y - bounds.y);
		float scale = 1.0f / 65535f;
		float[] dstr = dst[0];
		float[] dstg = dst[1];
		float[] dstb = dst[2];

		for (int i=0;i<width;i++) {
			int R = data[off++] & 0xffff;
			int G = data[off++] & 0xffff;
			int B = data[off++] & 0xffff;

			dstr[doff] = R * scale;
			dstg[doff] = G * scale;
			dstb[doff] = B * scale;
			doff += 1;
		}
	}

	@Override
	public void setLine(float[][] src, int soff, int x, int y, int width) {
		if (true)
		throw new RuntimeException();
		int off = csm.getOffset(x - bounds.x, y - bounds.y);
		float scale = 65535.0f;
		float[] srcr = src[0];
		float[] srcg = src[1];
		float[] srcb = src[2];

		for (int i=0;i<width;i++) {
			int R = (int) (srcr[soff] * scale + 0.5f);
			int G = (int) (srcg[soff] * scale + 0.5f);
			int B = (int) (srcb[soff] * scale + 0.5f);

			R = Math.max(R, 0);
			G = Math.max(G, 0);
			B = Math.max(B, 0);

			R = Math.min(R, 65535);
			G = Math.min(G, 65535);
			B = Math.min(B, 65535);

			data[off++] = (short)R;
			data[off++] = (short)G;
			data[off++] = (short)B;
			soff += 1;
		}
	}
}
