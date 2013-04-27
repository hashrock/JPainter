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
 * Level result for QWT wavelet (4 sub-bands)
 * @author notzed
 */
public class QWTBand {

	/**
	 * Wavelet sub-bands 4 x (lohi, hilo, hihi)
	 */
	public final float[][] wavelets = new float[4 * 3][];
	public final int w;
	public final int h;

	public QWTBand(int w, int h) {
		this.w = w;
		this.h = h;

		for (int i = 0; i < wavelets.length; i++) {
			wavelets[i] = new float[w * h * 2];
		}
	}

	/**
	 * 
	 * @param orientation 0-3 one of 4 orientations.
	 * @param subband 0-2 one of the 3 subbands.
	 * @return 
	 */
	public float[] getW(int orientation, int subband) {
		return wavelets[orientation * 3 + subband];
	}
	final static float sqrt2 = (float) Math.sqrt(2);

	public void q2c(float[] a, float[] b) {
		for (int i = 0; i < a.length; i++) {
			float u = a[i];
			float v = b[i];

			a[i] = (u + v) / sqrt2;
			b[i] = (u - v) / sqrt2;
		}
	}

	public void q2c() {
		for (int m=0;m<3;m++) {
			q2c(getW(0, m), getW(3, m));
			q2c(getW(1, m), getW(2, m));
		}
	}
}
