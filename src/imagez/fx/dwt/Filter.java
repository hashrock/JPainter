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
 * Base class for wavelet filters
 * @author notzed
 */
public abstract class Filter {

	float[] H0l;
	float[] H0h;
	float[] H1l;
	float[] H1h;
	float[] G0l;
	float[] G0h;
	float[] G1l;
	float[] G1h;

	float[] reverse(float[] a) {
		float[] b = new float[a.length];
		for (int i = 0; i < a.length; i++) {
			b[i] = a[a.length - 1 - i];
		}
		return b;
	}

	void reverse() {
		H0l = reverse(H0l);
		H0h = reverse(H0h);
		H1l = reverse(H1l);
		H1h = reverse(H1h);
	}

	static float[] invert(float[] a) {
		float b[] = new float[a.length];
		for (int i = 0; i < a.length; i++) {
			b[i] = a[a.length - 1 - i];
		}
		return b;
	}
}
