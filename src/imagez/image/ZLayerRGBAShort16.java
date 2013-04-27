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
import java.awt.image.DataBufferShort;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * Layer backed by short array with short elements.
*
 * @author notzed
 */
public class ZLayerRGBAShort16 extends ZLayerRGBA {

	short data[];
	ComponentSampleModel csm;

	public ZLayerRGBAShort16(ZImage zimage, Rectangle bounds) {
		super(zimage, bounds);

		data = new short[bounds.width * bounds.height * 4];

		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		ColorModel cm = new ComponentColorModel(cs, true, BlendMode.premultiplyAlpha, ColorModel.TRANSLUCENT, DataBuffer.TYPE_USHORT);
		ComponentSampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_USHORT, bounds.width, bounds.height, 4, bounds.width * 4, new int[]{0, 1, 2, 3});
		DataBufferShort db = new DataBufferShort(data, data.length);

		WritableRaster raster = Raster.createWritableRaster(sm, db, null);
		csm = sm;
		bimage = new BufferedImage(cm, raster, BlendMode.premultiplyAlpha, null);
	}

	@Override
	public int getStride() {
		return bounds.width*4;
	}

	public short[] getData() {
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
		float scale = 1.0f / 65535f;

		for (int i=0;i<width;i++) {
			int R = data[off++] & 0xffff;
			int G = data[off++] & 0xffff;
			int B = data[off++] & 0xffff;
			int A = data[off++] & 0xffff;

			dst[doff++] = R * scale;
			dst[doff++] = G * scale;
			dst[doff++] = B * scale;
			dst[doff++] = A * scale;
		}
	}

	@Override
	public void setLineRGBA(float[] src, int soff, int x, int y, int width) {
		int off = csm.getOffset(x - bounds.x, y - bounds.y);
		float scale = 65535.0f;

		for (int i=0;i<width;i++) {
			int R = (int) (src[soff++] * scale + 0.5f);
			int G = (int) (src[soff++] * scale + 0.5f);
			int B = (int) (src[soff++] * scale + 0.5f);
			int A = (int) (src[soff++] * scale + 0.5f);

			R = Math.max(R, 0);
			G = Math.max(G, 0);
			B = Math.max(B, 0);
			A = Math.max(A, 0);
			R = Math.min(R, 65535);
			G = Math.min(G, 65535);
			B = Math.min(B, 65535);
			A = Math.min(A, 65535);

			data[off++] = (short)R;
			data[off++] = (short)G;
			data[off++] = (short)B;
			data[off++] = (short)A;
		}
	}

	@Override
	public int getChannelCount() {
		return 4;
	}

	@Override
	public void getLine(float[][] dst, int doff, int x, int y, int width) {
		int off = csm.getOffset(x - bounds.x, y - bounds.y);
		float scale = 1.0f / 65535f;
		float[] dstr = dst[0];
		float[] dstg = dst[1];
		float[] dstb = dst[2];
		float[] dsta = dst[3];

		for (int i=0;i<width;i++) {
			int R = data[off++] & 0xffff;
			int G = data[off++] & 0xffff;
			int B = data[off++] & 0xffff;
			int A = data[off++] & 0xffff;

			dstr[doff] = R * scale;
			dstg[doff] = G * scale;
			dstb[doff] = B * scale;
			dsta[doff] = A * scale;
			doff += 1;
		}
	}

	@Override
	public void setLine(float[][] src, int soff, int x, int y, int width) {
		int off = csm.getOffset(x - bounds.x, y - bounds.y);
		float scale = 65535.0f;
		float[] srcr = src[0];
		float[] srcg = src[1];
		float[] srcb = src[2];
		float[] srca = src[3];

		for (int i=0;i<width;i++) {
			int R = (int) (srcr[soff] * scale + 0.5f);
			int G = (int) (srcg[soff] * scale + 0.5f);
			int B = (int) (srcb[soff] * scale + 0.5f);
			int A = (int) (srca[soff] * scale + 0.5f);

			R = Math.max(R, 0);
			G = Math.max(G, 0);
			B = Math.max(B, 0);
			A = Math.max(A, 0);
			R = Math.min(R, 65535);
			G = Math.min(G, 65535);
			B = Math.min(B, 65535);
			A = Math.min(A, 65535);

			data[off++] = (short)R;
			data[off++] = (short)G;
			data[off++] = (short)B;
			data[off++] = (short)A;
			soff += 1;
		}
	}
}
