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
 *
 * @author notzed
 */
public class Normal extends BlendMode {

	@Override
	public String getName() {
		return "Normal";
	}

	@Override
	public float blend(float d, float s, float a) {
		return s + d * (1 - a);
	}

	@Override
	public void blendRGBA(float[] dst, int doff, float[] src, int soff, float a, int len) {
		if (premultiplyAlpha) {
			for (int i = 0; i < len; i++) {
				float Rl = src[soff + 0] * a;
				float Gl = src[soff + 1] * a;
				float Bl = src[soff + 2] * a;
				float Al = src[soff + 3] * a;
				float Rb = dst[doff + 0];
				float Gb = dst[doff + 1];
				float Bb = dst[doff + 2];
				float Ab = dst[doff + 3];

				dst[doff + 0] = Rl + Rb * (1 - Al);
				dst[doff + 1] = Gl + Gb * (1 - Al);
				dst[doff + 2] = Bl + Bb * (1 - Al);
				dst[doff + 3] = Al + Ab * (1 - Al);

				doff += 4;
				soff += 4;
			}
		} else {
			// As = Asr * Aac
			// Cs = Csr * Asr * Aac
			// Fs = 1 and Fd = (1-As), thus:
			// Ar = As + Ad*(1-As)
			// Cr = Cs + Cd*(1-As)

			for (int i = 0; i < len; i++) {
				float Rsr = src[soff + 0];
				float Gsr = src[soff + 1];
				float Bsr = src[soff + 2];
				float As = src[soff + 3] * a;
				float Rb = dst[doff + 0];
				float Gb = dst[doff + 1];
				float Bb = dst[doff + 2];
				float Ab = dst[doff + 3];

				dst[doff + 0] = Rsr * As + Rb * (1 - As);
				dst[doff + 1] = Gsr * As + Gb * (1 - As);
				dst[doff + 2] = Bsr * As + Bb * (1 - As);
				dst[doff + 3] = As + Ab * (1 - As);

				doff += 4;
				soff += 4;
			}
		}
	}

	public void blendRGBAPre(float[] dst, int doff, float[] src, int soff, float a, int len) {
		for (int i = 0; i < len; i++) {
			float Rl = src[soff + 0] * a;
			float Gl = src[soff + 1] * a;
			float Bl = src[soff + 2] * a;
			float Al = src[soff + 3] * a;
			float Rb = dst[doff + 0];
			float Gb = dst[doff + 1];
			float Bb = dst[doff + 2];
			float Ab = dst[doff + 3];

			dst[doff + 0] = Rl + Rb * (1 - Al);
			dst[doff + 1] = Gl + Gb * (1 - Al);
			dst[doff + 2] = Bl + Bb * (1 - Al);
			dst[doff + 3] = Al + Ab * (1 - Al);

			doff += 4;
			soff += 4;
		}
	}
}
