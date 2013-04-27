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
package imagez;

import java.awt.Rectangle;
import java.awt.image.DataBufferByte;

/**
 *
 * @author notzed
 */
public class TestGauss {

	float[] blur1(float[] src, float[] dst, Rectangle bounds, float kernel[], int length) {
		int pos = 0;
		int pmod = length;
		int w = bounds.width;
		int h = bounds.height;

		// Perform horizontal
		for (int j = 0; j < h; j++) {
			for (int i = 0; i < w - length; i++) {
				float v = src[pos] * kernel[0];
				for (int k = 1; k < length; k++) {
					v += src[pos + k] * kernel[k];
				}
				dst[pos + length / 2 - 1] = v;
				pos++;
			}
			pos += pmod;
		}

		// perform vertical
		for (int i = 0; i < w; i++) {
			pos = i;
			for (int j = 0; j < h - length; j++) {
				float v = dst[pos] * kernel[0];
				for (int k = 1; k < length; k++) {
					v += dst[pos + k * w] * kernel[k];
				}
				src[pos + (length / 2 - 1) * w] = v;
				pos += w;
			}
		}

		return dst;
	}

	float[] blur16(float[] src, float[] dst, Rectangle bounds, float kernel[], int length) {
		int pos = 0;
		int pmod = length;
		int w = bounds.width;
		int h = bounds.height;

		// Perform horizontal
		for (int j = 0; j < h; j++) {
			for (int i = 0; i < w - length; i++) {
				float v = src[pos] * kernel[0];
				for (int k = 1; k < length; k++) {
					v += src[pos + k] * kernel[k];
				}
				dst[pos + length / 2 - 1] = v;
				pos++;
			}
			pos += pmod;
		}

		// perform vertical - 16 blurs at once
		float[] vs = new float[16];

		for (int i = 0; i < w; i += 16) {
			pos = i;
			for (int j = 0; j < h - length; j++) {
				for (int l = 0; l < 16; l++) {
					vs[l] = 0;
				}
				for (int k = 0; k < length; k++) {
					for (int l = 0; l < 16; l++) {
						vs[l] += dst[pos + l + k * w] * kernel[k];
					}
				}
				for (int l = 0; l < 16; l++) {
					src[pos + l + (length / 2 - 1) * w] = vs[l];
				}
				pos += w;
			}
		}

		return dst;
	}

	float[] blur16b(float[] src, float[] dst, Rectangle bounds, float kernel[], int length) {
		int pos = 0;
		int pmod = length;
		int w = bounds.width;
		int h = bounds.height;

		// Perform horizontal
		for (int j = 0; j < h; j++) {
			for (int i = 0; i < w - length; i++) {
				float v = src[pos] * kernel[0];
				for (int k = 1; k < length; k++) {
					v += src[pos + k] * kernel[k];
				}
				dst[pos + length / 2 - 1] = v;
				pos++;
			}
			pos += pmod;
		}

		for (int i = 0; i < w; i += 64) {
			pos = i;
			for (int j = 0; j < h - length; j++) {
				for (int l = 0; l < 64; l++) {
					float v = 0;
					for (int k = 0; k < length; k++) {
						v += dst[pos + l + k * w] * kernel[k];
					}
					src[pos + l + (length / 2 - 1) * w] = v;
				}
				pos += w;
			}
		}

		return dst;
	}

	float[] getKernel(float sigma) {
		int length = (int) (Math.ceil(sigma* 3));
		float[] kernel = new float[length];

		double sig = -1.0 / (2 * sigma * sigma);
		//double fact = 1.0 / Math.sqrt(2 * Math.PI * sigma * sigma);
		float energy = 0;
		for (int j = 0; j < length; j++) {
			int y = (j - length / 2);
			float v = (float) Math.exp(sig * (y * y));

			kernel[j] = v;
			energy += v;
		}
		for (int j = 0; j < length; j++) {
			kernel[j] /= energy;

			//System.out.printf("%2d: %f\n", j, kernel[j]);
		}

		return kernel;
	}

	//run 1 took 005.221s	1
	//run 16 took 006.378s	1.22
	//run 16b took 004.161s	0.80
	//run 64b took 004.060s

	public static void main(String[] args) {
		TestGauss tg = new TestGauss();
		int width = 1024;
		int height = 1024;

		float[] kernel = tg.getKernel(5.0f);
		System.out.printf("kernel size = " + kernel.length);

		float[] src = new float[width * height];
		float[] dst = new float[width * height];

		Rectangle rect = new Rectangle(0, 0, width, height);

		System.out.println("warming up");
		// warm them up
		for (int i = 0; i < 100; i++) {
			tg.blur1(src, dst, rect, kernel, kernel.length);
		}
		for (int i = 0; i < 100; i++) {
			tg.blur16(src, dst, rect, kernel, kernel.length);
		}
		for (int i = 0; i < 100; i++) {
			tg.blur16b(src, dst, rect, kernel, kernel.length);
		}

		long now;

		now = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			tg.blur1(src, dst, rect, kernel, kernel.length);
		}
		now = System.currentTimeMillis() - now;
		System.out.printf("run 1 took %03d.%03ds\n", now / 1000, now % 1000);

		now = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			tg.blur16(src, dst, rect, kernel, kernel.length);
		}
		now = System.currentTimeMillis() - now;
		System.out.printf("run 16 took %03d.%03ds\n", now / 1000, now % 1000);

		now = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			tg.blur16b(src, dst, rect, kernel, kernel.length);
		}
		now = System.currentTimeMillis() - now;
		System.out.printf("run 16b took %03d.%03ds\n", now / 1000, now % 1000);

	}
}
