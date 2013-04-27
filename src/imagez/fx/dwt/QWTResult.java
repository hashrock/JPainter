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
 * Wavelet container.
 * @author notzed
 */
public class QWTResult {

	public float[][] lolo;
	public int low;
	public int loh;
	public QWTBand[] wavelets;

	public QWTResult(int w, int h, int depth) {
		wavelets = new QWTBand[depth];
		for (int i = 0; i < depth; i++) {
			w /= 2;
			h /= 2;
			wavelets[i] = new QWTBand(w, h);
		}
		low = w;
		loh = h;
	}

	public int getDepth() {
		return wavelets.length;
	}

	public QWTBand getBand(int l) {
		return wavelets[l];
	}

	public void q2c() {
		for (int i = 0; i < wavelets.length; i++) {
			wavelets[i].q2c();
		}
	}
}
