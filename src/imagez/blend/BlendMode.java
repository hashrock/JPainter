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
package imagez.blend;

/**
 * Most of the blending functions were taken from this article:
 * 
 * http://www.nathanm.com/photoshop-blending-math/
 *
 * But they still need work since this always applies alpha, and
 * src is pre-multiplied-alpha.
 * @author notzed
 */
public abstract class BlendMode {
	// ??

	/**
	 * Experimenting with whether or not pre-multiplied alpha is a good
	 * idea or not.
	 * It speeds up blending, but destroys data.
	 * NB: non-premultiplied alpha doesn't work properly
	 */
	public final static boolean premultiplyAlpha = false;

	public String getName() {
		return this.getClass().getSimpleName();
	}
	public final static Normal normal = new Normal();
	public final static Multiply multiply = new Multiply();
	final static BlendMode[] kernels = {
		normal,
		multiply,
		new Multiply(),
		//new Divide(),
		new Screen(),
		new Overlay(),
		new Average(),
		new Difference(),
		new Add(),
		new Subtract(),
		new Exclusion(),
		//
		new Lighten(),
		new Darken(),};

	public static BlendMode[] getBlendModes() {
		return kernels;
	}

	public void blendRGBA(float[] dst, int doff, float[] src, int soff, float a, int len) {
		for (int i = 0; i < len; i++) {
			float Rl = src[soff + 0] * a;
			float Gl = src[soff + 1] * a;
			float Bl = src[soff + 2] * a;
			float Al = src[soff + 3] * a;
			float Rb = dst[doff + 0];
			float Gb = dst[doff + 1];
			float Bb = dst[doff + 2];
			float Ab = dst[doff + 3];

			dst[doff + 0] = blend(Rb, Rl, Al) * Ab;
			dst[doff + 1] = blend(Gb, Gl, Al) * Ab;
			dst[doff + 2] = blend(Bb, Bl, Al) * Ab;
			//dst[doff + 3] = Ab;

			doff += 4;
			soff += 4;
		}
	}

	// FIXME: experimental for non-premultiplied alpha support
	public void blendRGBAPre(float[] dst, int doff, float[] src, int soff, float a, int len) {
	}

	public abstract float blend(float d, float s, float a);

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
