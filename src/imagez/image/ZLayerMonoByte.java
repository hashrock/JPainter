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
import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.util.Arrays;

/**
 * Mono byte, with no alpha
 * @author notzed
 */
public class ZLayerMonoByte extends ZLayerMono {

	byte[] data;
	ComponentSampleModel csm;

	public ZLayerMonoByte(ZImage zimage, Rectangle bounds) {
		super(zimage, bounds);

		this.bimage = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_BYTE_GRAY);
		this.csm = (ComponentSampleModel) bimage.getSampleModel();
		WritableRaster raster = bimage.getRaster();
		this.data = ((DataBufferByte) raster.getDataBuffer()).getData();
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
		Arrays.fill(data, (byte)0);
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
			}
		}
	}

	@Override
	public void getLineMono(float[] dst, int doff, int x, int y, int width) {
		int off = csm.getOffset(x, y);

		for (int i = 0; i < width; i++) {
			dst[doff++] = (data[off++] & 0xff) * (1.0f / 255.0f);
		}
	}

	@Override
	public void setLineMono(float[] src, int soff, int x, int y, int width) {
		int off = csm.getOffset(x, y);

		for (int i = 0; i < width; i++) {
			int m = (int)(src[soff++] * 255.0f + 0.5f);

			m = Math.min(m, 255);
			m = Math.max(m, 0);

			data[off++] = (byte)m;
		}
	}

	@Override
	public void getLineRGBA(float[] dst, int doff, int x, int y, int width) {
		int off = csm.getOffset(x, y);

		// what about alpha channel?

		for (int i=0;i<width;i++) {
			float m = (data[off++] & 0xff) * (1.0f / 255.0f);
			
			dst[doff++] = m;
			dst[doff++] = m;
			dst[doff++] = m;
			dst[doff++] = 1;
		}
	}

	@Override
	public void setLineRGBA(float[] src, int soff, int x, int y, int width) {
		int off = csm.getOffset(x, y);
		float scale = 255.0f;
		
		// fixme: background colour

		for (int i=0;i<width;i++) {
			float r = src[soff++];
			float g = src[soff++];
			float b = src[soff++];
			float a = src[soff++];
			float m = (r + g + b) / 3.0f;

			int M = (int) (m * scale * a + 0.5f);

			M = Math.max(0, M);
			M = Math.min(255, M);

			data[off++] = (byte)M;
		}
	}

	@Override
	public int getChannelCount() {
		return 1;
	}

	@Override
	public void getLine(float[][] dst, int doff, int x, int y, int width) {
		int off = csm.getOffset(x, y);

		// what about alpha channel?

		for (int i=0;i<width;i++) {
			float m = (data[off++] & 0xff) * (1.0f / 255.0f);

			dst[0][doff++] = m;
		}
	}

	@Override
	public void setLine(float[][] src, int soff, int x, int y, int width) {
		int off = csm.getOffset(x, y);

		for (int i = 0; i < width; i++) {
			int m = (int)(src[0][soff++] * 255.0f + 0.5f);

			m = Math.min(m, 255);
			m = Math.max(m, 0);

			data[off++] = (byte)m;
		}
	}

	/**
	 * Replace a section of 'this' with src
	 * Bounds describe where to put it.
	 * @param src
	 */
	void replace(ZLayerMonoByte src) {
		Rectangle overlap = bounds.intersection(src.bounds);
		int sstride = src.getStride();
		int dstride = getStride();
		int sp = sstride * (overlap.y - src.bounds.y) + (overlap.x - src.bounds.x);
		int dp = dstride * (overlap.y - bounds.y) + (overlap.x - bounds.x);
		int height = overlap.height;
		int width = overlap.width;
		int smod = sstride - width;
		int dmod = dstride - width;
		byte[] sdata = src.data;
		byte[] ddata = data;

		System.out.printf("replace %s  src %s pos %d dst %s pos %d\n", overlap, src.bounds, sp, bounds, dp);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				ddata[dp++] = sdata[sp++];
			}
			dp += dmod;
			sp += smod;
		}
	}
}
