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
 * An int data-buffer that stores RGBA data in sparse tiles.
 *
 * Storage format is compatible with BufferedImage.TYPE_INT_ARGB(_PRE)
 *
 * The element-by-element access is slow, so avoid it.
 * @author notzed
 */
public class RGBASparseIntDataBuffer extends DataBuffer {

	// don't be fooled, the tilesize is hardcoded @ 64x64
	final int tileSize = 64;
	TileRGBAInt[] tiles;
	final int width;
	final int height;
	final int tilex;

	public class TileRGBAInt {
		final int[] data;

		public TileRGBAInt() {
			data = new int[tileSize * tileSize];
		}
	}

	public RGBASparseIntDataBuffer(int size, int width, int height) {
		super(TYPE_INT, size, 1);

		tilex = (width + (tileSize - 1)) / tileSize * 4;
		int tiley = (height + (tileSize - 1)) / tileSize;

		tiles = new TileRGBAInt[tilex * tiley];

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
		int X = i % width;
		int Y = i / width;
		int tx = X / (tileSize);
		int ty = Y / tileSize;
		int ti = tx + ty * tilex;
		int pos = (X & (tileSize - 1)) + (Y & (tileSize - 1)) * (tileSize);

		TileRGBAInt tf = tiles[ti];
		if (tf != null) {
			return tf.data[pos];
		} else {
			return 0;
		}
	}

	@Override
	public void setElem(int bank, int i, int val) {
		int X = i % width;
		int Y = i / width;
		int tx = X / (tileSize);
		int ty = Y / tileSize;
		int ti = tx + ty * tilex;
		int pos = (X & (tileSize - 1)) + (Y & (tileSize - 1)) * (tileSize);

		TileRGBAInt tf = tiles[ti];
		if (tf != null) {
			tf.data[pos] = val;
		} else {
			tf = new TileRGBAInt();
			tiles[ti] = tf;
			tf.data[pos] = val;
		}
	}

	public void getLineRGBA(float[] dst, int doff, int x, int y, int width) {
		int tx = x / tileSize;
		int ty = (y / tileSize) * tilex;
		int ts = tx + ty;
		int te = (x + width) / tileSize + ty;
		int py = (y & (tileSize - 1)) * tileSize;
		int rx = tx * tileSize;
		float scale = 1.0f / 255.0f;

		for (int j = ts; j <= te; j++) {
			int start = Math.max(rx, x);
			int end = Math.min(rx + tileSize, x + width);
			int soff = (py + (start & (tileSize - 1)));
			TileRGBAInt tf = tiles[j];

			rx = rx + tileSize;
			if (tf != null) {
				for (int i = start; i < end; i += 1) {
					int v = tf.data[soff++];

					dst[doff++] = ((v >> 16) & 0xff) * scale;
					dst[doff++] = ((v >>  8) & 0xff) * scale;
					dst[doff++] = ((v >>  0) & 0xff) * scale;
					dst[doff++] = ((v >> 24) & 0xff) * scale;
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
		float scale = 1.0f / 255.0f;

		for (int j = ts; j <= te; j++) {
			int start = Math.max(rx, x);
			int end = Math.min(rx + tileSize, x + width);
			int soff = (py + (start & (tileSize - 1)));
			TileRGBAInt tf = tiles[j];

			rx = rx + tileSize;
			if (tf != null) {
				for (int i = start; i < end; i += 1) {
					int v = tf.data[soff++];

					a[doff] = ((v >> 24) & 0xff) * scale;
					r[doff] = ((v >> 16) & 0xff) * scale;
					g[doff] = ((v >>  8) & 0xff) * scale;
					b[doff] = ((v >>  0) & 0xff) * scale;
					doff++;
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
		float scale = 255.0f;

		bob:
		for (int j = ts; j <= te; j++) {
			int start = Math.max(rx, x);
			int end = Math.min(rx + tileSize, x + width);
			int doff = (py + (start & (tileSize - 1)));
			TileRGBAInt tf = tiles[j];

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

				tf = tiles[j] = new TileRGBAInt();
				//for (int i=0;i<64*4;i++) tf.data[i] = 1;
			}

			for (int i = start; i < end; i += 1) {
				int R = (int)(src[soff++] * scale + 0.5f);
				int G = (int)(src[soff++] * scale + 0.5f);
				int B = (int)(src[soff++] * scale + 0.5f);
				int A = (int)(src[soff++] * scale + 0.5f);

				A = Math.min(A, 255);
				R = Math.min(R, 255);
				G = Math.min(G, 255);
				B = Math.min(B, 255);

				A = Math.max(A, 0);
				R = Math.max(R, 0);
				G = Math.max(G, 0);
				B = Math.max(B, 0);

				tf.data[doff++] = (A<<24) | (R << 16) | (G << 8) | B;
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
		float scale = 255.0f;

		for (int j = ts; j <= te; j++) {
			int start = Math.max(rx, x);
			int end = Math.min(rx + tileSize, x + width);
			int doff = (py + (start & (tileSize - 1)));
			TileRGBAInt tf = tiles[j];

			rx = rx + tileSize;
			if (tf == null) {
				tf = tiles[j] = new TileRGBAInt();
			}

			for (int i = start; i < end; i += 1) {
				int A = (int)(a[soff] * scale + 0.5f);
				int R = (int)(r[soff] * scale + 0.5f);
				int G = (int)(g[soff] * scale + 0.5f);
				int B = (int)(b[soff] * scale + 0.5f);

				soff += 1;

				A = Math.min(A, 255);
				R = Math.min(R, 255);
				G = Math.min(G, 255);
				B = Math.min(B, 255);

				A = Math.max(A, 0);
				R = Math.max(R, 0);
				G = Math.max(G, 0);
				B = Math.max(B, 0);

				tf.data[doff++] = (A<<24) | (R << 16) | (G << 8) | B;
			}
		}
	}

	public void setDataElements(int x, int y, int[] el) {
		int tx = (x >> 6);
		int ty = (y >> 6);
		int ti = tx + ty * tilex;
		TileRGBAInt tf = tiles[ti];

		if (tf == null) {
			tf = tiles[ti] = new TileRGBAInt();
		}

		int pos = ((x & 63)) + ((y & 63) << 6);
		tf.data[pos] = el[0];
	}

	public int[] getDataElements(int x, int y, int[] el) {
		int tx = (x >> 6);
		int ty = (y >> 6);
		int ti = tx + ty * tilex;
		TileRGBAInt tf = tiles[ti];

		if (tf != null) {
			int pos = ((x & 63)) + ((y & 63) << 6);
			el[0] = tf.data[pos + 0];
		} else {
			el[0] = 0;
		}
		return el;
	}
}
