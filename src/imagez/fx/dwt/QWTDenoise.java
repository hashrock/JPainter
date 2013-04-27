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

import imagez.image.ZLayer;

/**
 * Uses a quaternion wavelet transform to perform a dual-tree-complex-wavelet-transform denoise.
 *
 * Related to the thresholding denoising described here: http://eeweb.poly.edu/iselesni/WaveletSoftware/
 *
 * @author notzed
 */
public class QWTDenoise {

	// wavelet depth to use, must be >1
	final int depth = 4;
	ZLayer srcLayer;
	ZLayer dstLayer;
	int cc;
	// wavelet data width/height
	// must be aligned to 2^wavelet depth
	int width;
	int height;
	float[][] tmp;

	public QWTDenoise(ZLayer srcLayer, ZLayer dstLayer) {
		int round = (1 << depth) - 1;

		this.srcLayer = srcLayer;
		this.dstLayer = dstLayer;

		cc = srcLayer.getChannelCount();
		width = (srcLayer.getBounds().width + round) & ~round;
		height = (srcLayer.getBounds().height + round) & ~round;

		tmp = new float[cc][width * height];
	}

	public void denoise(float threshold, float scale, float weight) {
		int swidth = srcLayer.getBounds().width;
		int sheight = dstLayer.getBounds().height;

		long now = System.currentTimeMillis();

		for (int y = 0; y < sheight; y++) {
			srcLayer.getLine(tmp, y * width, 0, y, swidth);
		}

		QWTResult result = new QWTResult(swidth, sheight, depth);
		QWT qwt = new QWT();

		FSFrarras fs = new FSFrarras();
		DualFilt1 df = new DualFilt1();

		for (int c = 0; c < cc; c++) {
			float[] t = tmp[c];

			// normalise factor for qwt (FIXME)
			for (int i = 0; i < t.length; i++) {
				t[i] *= 0.5f;
			}

			qwt.forward(t, width, height, result, fs, df);

			// perform denoise
			for (int k = 0; k < result.getDepth(); k++) {
				// scale factor for this depth
				float s = (float) (Math.exp((result.getDepth() - k) * scale) - 1) * weight + 1;

				QWTBand band = result.getBand(k);

				for (int m = 0; m < 3; m++) {
					softq(band.getW(0, m), band.getW(3, m), threshold, s);
					softq(band.getW(1, m), band.getW(2, m), threshold, s);
				}
			}

			qwt.inverse(t, width, height, result, fs, df);

			// normalise factor for qwt (FIXME)
			for (int i = 0; i < t.length; i++) {
				t[i] *= 0.5f;
			}
		}

		now = System.currentTimeMillis() - now;
		System.out.printf("qwt denoise %dx%d@%d took %d.%03ds\n", swidth, sheight, tmp.length, now / 1000, now % 1000);
		for (int y = 0; y < sheight; y++) {
			dstLayer.setLine(tmp, y * width, 0, y, swidth);
		}

	}

	void softq(float[] re, float[] im, float T, float S) {
		for (int i = 0; i < re.length; i++) {
			float u = re[i];
			float v = im[i];

			// quaternion to complex
			float a = (u + v) / 1.4142135623730951f;
			float b = (u - v) / 1.4142135623730951f;

			// complex threshold denoise
			float y = Math.max((float) Math.sqrt(a * a + b * b) - T, 0);

			y = y / (y + T);

			a *= y * S;
			b *= y * S;

			// complex to quaternion
			u = (a + b) / 1.4142135623730951f;
			v = (a - b) / 1.4142135623730951f;

			re[i] = u;
			im[i] = v;
		}
	}
}
