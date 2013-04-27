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

import java.util.Arrays;

/**
 * Quaternion wavelet transform.
 * @author notzed
 */
public class QWT extends DWT {

	/**
	 * Forward transform
	 * @param src source real data
	 * @param w width of data (and stride)
	 * @param G1h height of data
	 * @param result pre-initialised result holder, implies the depth of the transform
	 * @param l0 level 0 filter coefficients
	 * @param ln level 1-n filter coefficients
	 */
	public void forward(float[] src, int w, int h, QWTResult result, Filter l0, Filter ln) {
		// Work space for lo lo result for all levels
		float[][] ll = new float[4][w / 2 * h / 2];

		// TODO: normalisation

		// level 0
		QWTBand r = result.getBand(0);

		analysisBank(src, w, h, l0.H0l, l0.H0h, l0.H0l, l0.H0h, ll[0], r.getW(0, 0), r.getW(0, 1), r.getW(0, 2));
		analysisBank(src, w, h, l0.H0l, l0.H0h, l0.H1l, l0.H1h, ll[1], r.getW(1, 0), r.getW(1, 1), r.getW(1, 2));
		analysisBank(src, w, h, l0.H1l, l0.H1h, l0.H0l, l0.H0h, ll[2], r.getW(2, 0), r.getW(2, 1), r.getW(2, 2));
		analysisBank(src, w, h, l0.H1l, l0.H1h, l0.H1l, l0.H1h, ll[3], r.getW(3, 0), r.getW(3, 1), r.getW(3, 2));

		// other levels ... pretty much exactly the same, just use filters ln instead ...
		// and feed in ll[x] instead of src
		for (int j = 1; j < result.getDepth(); j++) {
			r = result.getBand(j);

			w /= 2;
			h /= 2;

			analysisBank(ll[0], w, h, ln.H0l, ln.H0h, ln.H0l, ln.H0h, ll[0], r.getW(0, 0), r.getW(0, 1), r.getW(0, 2));
			analysisBank(ll[1], w, h, ln.H0l, ln.H0h, ln.H1l, ln.H1h, ll[1], r.getW(1, 0), r.getW(1, 1), r.getW(1, 2));
			analysisBank(ll[2], w, h, ln.H1l, ln.H1h, ln.H0l, ln.H0h, ll[2], r.getW(2, 0), r.getW(2, 1), r.getW(2, 2));
			analysisBank(ll[3], w, h, ln.H1l, ln.H1h, ln.H1l, ln.H1h, ll[3], r.getW(3, 0), r.getW(3, 1), r.getW(3, 2));
		}

		// narrow lolo result to actual size needed
		result.lolo = new float[4][];
		for (int i = 0; i < 4; i++) {
			result.lolo[i] = Arrays.copyOfRange(ll[i], 0, (w / 2) * (h / 2));
		}
	}

	/**
	 * Inverse QWT transform
	 * @param dst output real data
	 * @param w width (and stride)
	 * @param G1h height
	 * @param result output from forward()
	 * @param l0 level 0 filters
	 * @param ln level n filters
	 */
	public void inverse(float[] dst, int w, int h, QWTResult result, Filter l0, Filter ln) {
		float[][] ll = new float[4][w / 2 * h / 2];

		QWTBand r;
		float[][] lo = result.lolo;

		// other levels ... pretty much exactly the same, just use filters ln instead ...
		// and feed in ll[x] instead of src
		for (int j = result.getDepth() - 1; j > 0; j--) {
			r = result.getBand(j);

			w = r.w;
			h = r.h;

			System.out.printf("inverse %d %d,%d\n", j, w, h);

			synthesisBank(ll[0], w, h, ln.G0l, ln.G0h, ln.G0l, ln.G0h, lo[0], r.getW(0, 0), r.getW(0, 1), r.getW(0, 2));
			synthesisBank(ll[1], w, h, ln.G0l, ln.G0h, ln.G1l, ln.G1h, lo[1], r.getW(1, 0), r.getW(1, 1), r.getW(1, 2));
			synthesisBank(ll[2], w, h, ln.G1l, ln.G1h, ln.G0l, ln.G0h, lo[2], r.getW(2, 0), r.getW(2, 1), r.getW(2, 2));
			synthesisBank(ll[3], w, h, ln.G1l, ln.G1h, ln.G1l, ln.G1h, lo[3], r.getW(3, 0), r.getW(3, 1), r.getW(3, 2));
			lo = ll;
		}

		r = result.getBand(0);
		w = r.w;
		h = r.h;

		// level 0
		synthesisBank(dst, w, h, l0.G0l, l0.G0h, l0.G0l, l0.G0h, lo[0], r.getW(0, 0), r.getW(0, 1), r.getW(0, 2));
		synthesisBankAdd(dst, w, h, l0.G0l, l0.G0h, l0.G1l, l0.G1h, lo[1], r.getW(1, 0), r.getW(1, 1), r.getW(1, 2));
		synthesisBankAdd(dst, w, h, l0.G1l, l0.G1h, l0.G0l, l0.G0h, lo[2], r.getW(2, 0), r.getW(2, 1), r.getW(2, 2));
		synthesisBankAdd(dst, w, h, l0.G1l, l0.G1h, l0.G1l, l0.G1h, lo[3], r.getW(3, 0), r.getW(3, 1), r.getW(3, 2));
	}
}
