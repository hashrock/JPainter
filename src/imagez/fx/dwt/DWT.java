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
package imagez.fx.dwt;

/**
 * Discrete wavelet transform base class.
 * 
 * This mirrors the structure of the Polytechnic University
 * wavelet toolkit, but has some small optimisations
 * such as performing both high/low convolution together.
 * http://taco.poly.edu/WaveletSoftware/
 * @author notzed
 */
public class DWT {

	/**
	 * Mirror coordinates about the boundaries.
	 * The edge point is not repeated.
	 * @param v
	 * @param m
	 * @return 
	 */
	static int mirror(int v, int m) {
		if (v < 0) {
			v = -v - 1;
		} else if (v >= m) {
			v = m + m - 1 - v;
		}
		return v;
	}

	// filter up 1 down 2 in y, both for low and high-pass filter.
	protected void filterY(float[] src, int w, int h, float[] lp, float[] hp, float[] lo, float[] hi) {
		// lp and hp are same length!
		int n2 = hp.length / 2;
		int n = hp.length;

		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y += 2) {
				float H = 0;
				float L = 0;
				for (int k = 0; k < n; k++) {
					int yi = mirror(y - n2 + k + 1, h);
					float v = src[x + yi * w];

					H += v * hp[n - 1 - k];
					L += v * lp[n - 1 - k];
				}
				lo[x + (y / 2) * w] = L;
				hi[x + (y / 2) * w] = H;
			}
		}
	}

	protected void filterX(float[] src, int w, int h, float[] lp, float[] hp, float[] lo, float[] hi) {
		int n2 = hp.length / 2;
		int n = hp.length;

		for (int y = 0; y < h; y += 1) {
			for (int x = 0; x < w; x += 2) {
				float H = 0;
				float L = 0;
				for (int k = 0; k < n; k++) {
					int xi = mirror(x - n2 + k + 1, w);
					float v = src[xi + y * w];

					H += v * hp[n - 1 - k];
					L += v * lp[n - 1 - k];
				}
				lo[x / 2 + y * w / 2] = L;
				hi[x / 2 + y * w / 2] = H;
			}
		}
	}
	int fb = 0;

	protected void analysisBank(float[] src, int w, int h, float[] l0, float[] h0, float[] l1, float[] h1, float[] ll, float[] lh, float[] hl, float[] hh) {
		float[] L = new float[w * (h / 2)];
		float[] H = new float[w * (h / 2)];

		filterY(src, w, h, l0, h0, L, H);

		filterX(L, w, h / 2, l1, h1, ll, lh);
		filterX(H, w, h / 2, l1, h1, hl, hh);
	}
	int off = 0;

	// inverse transform
	// filter up by 2, down by 1 in y, both lo/hi pass
	public void iFilterY(float[] dst, int w, int h, float[] lp, float[] hp, float[] lo, float[] hi) {
		// lp and hp are same length!
		int n2 = hp.length / 2;
		int n = hp.length;

		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y += 1) {
				float v0 = 0;
				float v1 = 0;
				for (int k = 0; k < n; k += 2) {
					int yi = mirror(y - n2 / 2 + k / 2 + off, h);

					v0 += lp[n - 2 - k] * lo[x + yi * w];
					v0 += hp[n - 2 - k] * hi[x + yi * w];

					v1 += lp[n - 2 - k + 1] * lo[x + yi * w];
					v1 += hp[n - 2 - k + 1] * hi[x + yi * w];
				}
				dst[x + (y * 2 + 0) * w] = v0;
				dst[x + (y * 2 + 1) * w] = v1;
			}
		}
	}

	// filter up by 2, down by 1 in x, both lo/hi pass
	public void iFilterX(float[] dst, int w, int h, float[] lp, float[] hp, float[] lo, float[] hi) {
		int n2 = hp.length / 2;
		int n = hp.length;

		for (int y = 0; y < h; y += 1) {
			for (int x = 0; x < w; x += 1) {
				float v0 = 0;
				float v1 = 0;

				for (int k = 0; k < n; k += 2) {
					int xi = mirror(x - n2 / 2 + k / 2, w);

					v0 += lp[n - 2 - k] * lo[xi + y * w];
					v0 += hp[n - 2 - k] * hi[xi + y * w];

					v1 += lp[n - 2 - k + 1] * lo[xi + y * w];
					v1 += hp[n - 2 - k + 1] * hi[xi + y * w];
				}
				dst[x * 2 + 0 + y * w * 2] = v0;
				dst[x * 2 + 1 + y * w * 2] = v1;
			}
		}
	}

	public void synthesisBank(float[] dst, int w, int h, float[] l0, float[] h0, float[] l1, float[] h1, float[] ll, float[] lh, float[] hl, float[] hh) {
		float[] L = new float[w * 2 * h];
		float[] H = new float[w * 2 * h];

		iFilterX(L, w, h, l1, h1, ll, lh);
		iFilterX(H, w, h, l1, h1, hl, hh);

		iFilterY(dst, w * 2, h, l0, h0, L, H);
	}

	public void iFilterYAdd(float[] dst, int w, int h, float[] lp, float[] hp, float[] lo, float[] hi) {
		// lp and hp are same length!
		int n2 = hp.length / 2;
		int n = hp.length;

		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y += 1) {
				float v0 = 0;
				float v1 = 0;
				for (int k = 0; k < n; k += 2) {
					int yi = mirror(y - n2 / 2 + k / 2 + off, h);

					v0 += lp[n - 2 - k] * lo[x + yi * w];
					v0 += hp[n - 2 - k] * hi[x + yi * w];

					v1 += lp[n - 2 - k + 1] * lo[x + yi * w];
					v1 += hp[n - 2 - k + 1] * hi[x + yi * w];
				}
				dst[x + (y * 2 + 0) * w] += v0;
				dst[x + (y * 2 + 1) * w] += v1;
			}
		}
	}

	public void synthesisBankAdd(float[] dst, int w, int h, float[] l0, float[] h0, float[] l1, float[] h1, float[] ll, float[] lh, float[] hl, float[] hh) {
		float[] L = new float[w * 2 * h];
		float[] H = new float[w * 2 * h];

		iFilterX(L, w, h, l1, h1, ll, lh);
		iFilterX(H, w, h, l1, h1, hl, hh);

		iFilterYAdd(dst, w * 2, h, l0, h0, L, H);
	}
}
