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

/**
 * A float data-buffer that stores RGBA data in sparse tiles.
 *
 * The element-by-element access is slow, so avoid it.
 * @author notzed
 */
public class RGBASparseFloatDataBuffer extends DataBuffer {

	// don't be fooled, the tilesize is hardcoded @ 64x64
	final int tileSize = 64;
	TileRGBAFloat[] tiles;
	final int width;
	final int height;
	final int tilex;

	public class TileRGBAFloat {
		final float[] data;

		public TileRGBAFloat() {
			data = new float[tileSize * tileSize * 4];
		}
	}

	public RGBASparseFloatDataBuffer(int size, int width, int height) {
		super(TYPE_FLOAT, size, 1);

		tilex = (width + (tileSize - 1)) / tileSize * 4;
		int tiley = (height + (tileSize - 1)) / tileSize;

		tiles = new TileRGBAFloat[tilex * tiley];

		this.width = tilex * tileSize;
		this.height = tiley * tileSize;
	}

	public void clear() {
		for (int i = 0; i < tiles.length; i++) {
			tiles[i] = null;
		}
	}

	@Override
	public int getElem(int bank, int i) {
		return (int) getElemFloat(bank, i);
	}

	@Override
	public void setElem(int bank, int i, int val) {
		//System.out.println("set element " + val);
		setElemFloat(bank, i, val);
	}

	@Override
	public void setElemFloat(int bank, int i, float val) {
		int X = i % width;
		int Y = i / width;
		int tx = X / (tileSize * 4);
		int ty = Y / tileSize;
		int ti = tx + ty * tilex;
		int pos = (X & (tileSize * 4 - 1)) + (Y & (tileSize - 1)) * (tileSize * 4);

		TileRGBAFloat tf = tiles[ti];
		if (tf != null) {
			tf.data[pos] = val;
		} else {
			tf = new TileRGBAFloat();
			tiles[ti] = tf;
			tf.data[pos] = val;
		}
	}

	@Override
	public float getElemFloat(int bank, int i) {
		int X = i % width;
		int Y = i / width;
		int tx = X / (tileSize * 4);
		int ty = Y / tileSize;
		int ti = tx + ty * tilex;
		int pos = (X & (tileSize * 4 - 1)) + (Y & (tileSize - 1)) * (tileSize * 4);

		TileRGBAFloat tf = tiles[ti];
		if (tf != null) {
			return tf.data[pos];
		} else {
			return 0;
		}
	}

	@Override
	public void setElemDouble(int bank, int i, double val) {
		setElemFloat(bank, i, (float) val);
	}

	@Override
	public double getElemDouble(int bank, int i) {
		return getElemFloat(bank, i);
	}

	public void getLineRGBA(float[] dst, int doff, int x, int y, int width) {
		int tx = x / tileSize;
		int ty = (y / tileSize) * tilex;
		int ts = tx + ty;
		int te = (x + width) / tileSize + ty;
		int py = (y & (tileSize - 1)) * tileSize;
		int rx = tx * tileSize;

		for (int j = ts; j <= te; j++) {
			int start = Math.max(rx, x);
			int end = Math.min(rx + tileSize, x + width);
			int soff = (py + (start & (tileSize - 1))) * 4;
			TileRGBAFloat tf = tiles[j];

			rx = rx + tileSize;
			if (tf != null) {
				for (int i = start; i < end; i += 1) {
					dst[doff++] = tf.data[soff++];
					dst[doff++] = tf.data[soff++];
					dst[doff++] = tf.data[soff++];
					dst[doff++] = tf.data[soff++];
				}
			} else {
				for (int i = start; i < end; i += 1) {
					dst[doff++] = 0;
					dst[doff++] = 0;
					dst[doff++] = 0;
					dst[doff++] = 0;
				}
			}
		}
	}

	public void getLineRGBA(float[][] dst, int doff, int x, int y, int width) {
		int tx = x / tileSize;
		int ty = (y / tileSize) * tilex;
		int ts = tx + ty;
		int te = (x + width) / tileSize + ty;
		int py = (y & (tileSize - 1)) * tileSize;
		int rx = tx * tileSize;
		float[] r = dst[0], g = dst[1], b = dst[2], a = dst[3];

		for (int j = ts; j <= te; j++) {
			int start = Math.max(rx, x);
			int end = Math.min(rx + tileSize, x + width);
			int soff = (py + (start & (tileSize - 1))) * 4;
			TileRGBAFloat tf = tiles[j];

			rx = rx + tileSize;
			if (tf != null) {
				for (int i = start; i < end; i += 1) {
					r[doff] = tf.data[soff++];
					g[doff] = tf.data[soff++];
					b[doff] = tf.data[soff++];
					a[doff++] = tf.data[soff++];
				}
			} else {
				for (int i = start; i < end; i += 1) {
					r[doff] = 0;
					g[doff] = 0;
					b[doff] = 0;
					a[doff++] = 0;
				}
			}
		}
	}

	public void setLineRGBA(float[] src, int soff, int x, int y, int width) {
		int tx = x / tileSize;
		int ty = (y / tileSize) * tilex;
		int ts = tx + ty;
		int te = (x + width) / tileSize + ty;
		int py = (y & (tileSize - 1)) * tileSize;
		int rx = tx * tileSize;

		bob:
		for (int j = ts; j <= te; j++) {
			int start = Math.max(rx, x);
			int end = Math.min(rx + tileSize, x + width);
			int doff = (py + (start & (tileSize - 1))) * 4;
			TileRGBAFloat tf = tiles[j];

			rx = rx + tileSize;
			if (tf == null) {
				// Quick hack: see if we're writing nothing to an nonexistant tile - leave sparse
				// NB: This works so long as you cant have negative pixel values.
				int so = soff;
				float v = 0;
				for (int i = start; i < end; i += 1) {
					v += src[so++];
					v += src[so++];
					v += src[so++];
					v += src[so++];
				}
				if (v == 0) {
					soff += (end - start) * 4;
					continue bob;
				}

				tf = tiles[j] = new TileRGBAFloat();
				//for (int i=0;i<64*4;i++) tf.data[i] = 1;
			}

			for (int i = start; i < end; i += 1) {
				tf.data[doff++] = src[soff++];
				tf.data[doff++] = src[soff++];
				tf.data[doff++] = src[soff++];
				tf.data[doff++] = src[soff++];
			}
		}
	}

	public void setLineRGBA(float[][] src, int soff, int x, int y, int width) {
		int tx = x / tileSize;
		int ty = (y / tileSize) * tilex;
		int ts = tx + ty;
		int te = (x + width) / tileSize + ty;
		int py = (y & (tileSize - 1)) * tileSize;
		int rx = tx * tileSize;
		float[] r = src[0], g = src[1], b = src[2], a = src[3];

		for (int j = ts; j <= te; j++) {
			int start = Math.max(rx, x);
			int end = Math.min(rx + tileSize, x + width);
			int doff = (py + (start & (tileSize - 1))) * 4;
			TileRGBAFloat tf = tiles[j];

			rx = rx + tileSize;
			if (tf == null) {
				tf = tiles[j] = new TileRGBAFloat();
			}

			for (int i = start; i < end; i += 1) {
				tf.data[doff++] = r[soff];
				tf.data[doff++] = g[soff];
				tf.data[doff++] = b[soff];
				tf.data[doff++] = a[soff++];
			}
		}
	}

	public void setDataElements(int x, int y, float[] el) {
		int tx = (x >> 6);
		int ty = (y >> 6);
		int ti = tx + ty * tilex;
		TileRGBAFloat tf = tiles[ti];

		if (tf == null) {
			tf = tiles[ti] = new TileRGBAFloat();
		}

		int pos = ((x & 63) << 2) + ((y & 63) << 8);
		tf.data[pos + 0] = el[0];
		tf.data[pos + 1] = el[1];
		tf.data[pos + 2] = el[2];
		tf.data[pos + 3] = el[3];
	}

	public float[] getDataElements(int x, int y, float[] el) {
		int tx = (x >> 6);
		int ty = (y >> 6);
		int ti = tx + ty * tilex;
		TileRGBAFloat tf = tiles[ti];

		if (tf != null) {
			int pos = ((x & 63) << 2) + ((y & 63) << 8);
			el[0] = tf.data[pos + 0];
			el[1] = tf.data[pos + 1];
			el[2] = tf.data[pos + 2];
			el[3] = tf.data[pos + 3];
		} else {
			el[0] = 0;
			el[1] = 0;
			el[2] = 0;
			el[3] = 0;
		}
		return el;
	}
}
