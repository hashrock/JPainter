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
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.WritableRaster;

/**
 *
 * @author notzed
 */
public class ZLayerRGBAByte extends ZLayerRGBA {

	byte[] data;
	PixelInterleavedSampleModel psm;

	public ZLayerRGBAByte(ZImage zimage, Rectangle bounds) {
		super(zimage, bounds);

		this.bimage = new BufferedImage(bounds.width, bounds.height, BlendMode.premultiplyAlpha ? BufferedImage.TYPE_4BYTE_ABGR_PRE : BufferedImage.TYPE_4BYTE_ABGR);

		this.psm = (PixelInterleavedSampleModel) bimage.getSampleModel();
		WritableRaster raster = bimage.getRaster();
		this.data = ((DataBufferByte) raster.getDataBuffer()).getData();
	}

	public ZLayerRGBAByte(ZImage zimage, BufferedImage image) {
		super(zimage, image.getRaster().getBounds());

		this.bimage = image;
		this.psm = (PixelInterleavedSampleModel) image.getSampleModel();
		WritableRaster raster = image.getRaster();
		this.data = ((DataBufferByte) raster.getDataBuffer()).getData();

		assert psm.getNumDataElements() == 4;
	}

	@Override
	public int getStride() {
		return bounds.width*4;
	}

	@Override
	public void clear(Rectangle rect) {
		int doff = psm.getOffset(rect.x, rect.y) - 3;
		int width = rect.width;
		int height = rect.height;
		int dmod = psm.getScanlineStride() - width*4;

		for (int y=0;y<height;y++) {
			for (int x =0;x<width;x++) {
				data[doff++] = 0;
				data[doff++] = 0;
				data[doff++] = 0;
				data[doff++] = 0;
			}
			doff += dmod;
		}
	}

	@Override
	public void getLineRGBA(float[] dst, int doff, int x, int y, int width) {
		int soff = psm.getOffset(x, y) - 3;
		float scale = 1.0f / 255.0f;

		// probably doesn't compile well?
		// could really change everything to ARGB anyway
		for (int i = 0; i < width; i++) {
			dst[doff + 3] = (((int) data[soff++]) & 0xff) * scale;
			dst[doff + 2] = (((int) data[soff++]) & 0xff) * scale;
			dst[doff + 1] = (((int) data[soff++]) & 0xff) * scale;
			dst[doff + 0] = (((int) data[soff++]) & 0xff) * scale;
			doff += 4;
		}
	}

	@Override
	public void setLineRGBA(float[] src, int soff, int x, int y, int width) {
		int doff = psm.getOffset(x, y) - 3;
		float scale = 255.0f;

		// probably doesn't compile well?
		// could really change everything to ARGB anyway
		for (int i = 0; i < width; i++) {
			int R = (int) (src[soff++] * scale);
			int G = (int) (src[soff++] * scale);
			int B = (int) (src[soff++] * scale);
			int A = (int) (src[soff++] * scale);

			R = Math.max(R, 0);
			G = Math.max(G, 0);
			B = Math.max(B, 0);
			A = Math.max(A, 0);
			R = Math.min(R, 255);
			G = Math.min(G, 255);
			B = Math.min(B, 255);
			A = Math.min(A, 255);

			data[doff++] = (byte) A;
			data[doff++] = (byte) B;
			data[doff++] = (byte) G;
			data[doff++] = (byte) R;
		}
	}

	@Override
	public int getChannelCount() {
		return 4;
	}

	@Override
	public void getLine(float[][] dst, int doff, int x, int y, int width) {
		int soff = psm.getOffset(x, y) - 3;
		float scale = 1.0f / 255.0f;

		// probably doesn't compile well?
		// could really change everything to ARGB anyway
		for (int i = 0; i < width; i++) {
			dst[3][doff] = (((int) data[soff++]) & 0xff) * scale;
			dst[2][doff] = (((int) data[soff++]) & 0xff) * scale;
			dst[1][doff] = (((int) data[soff++]) & 0xff) * scale;
			dst[0][doff] = (((int) data[soff++]) & 0xff) * scale;
			doff ++;
		}
	}

	@Override
	public void setLine(float[][] src, int soff, int x, int y, int width) {
		int doff = psm.getOffset(x, y) - 3;
		float scale = 255.0f;

		for (int i = 0; i < width; i++) {
			int R = (int) (src[0][soff] * scale);
			int G = (int) (src[1][soff] * scale);
			int B = (int) (src[2][soff] * scale);
			int A = (int) (src[3][soff] * scale);

			soff += 1;

			R = Math.max(R, 0);
			G = Math.max(G, 0);
			B = Math.max(B, 0);
			A = Math.max(A, 0);
			R = Math.min(R, 255);
			G = Math.min(G, 255);
			B = Math.min(B, 255);
			A = Math.min(A, 255);

			data[doff++] = (byte) A;
			data[doff++] = (byte) B;
			data[doff++] = (byte) G;
			data[doff++] = (byte) R;
		}
	}
}
