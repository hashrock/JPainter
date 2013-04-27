/*
 * Copyright (c) 2010, Guoshen Yu <yu@cmap.polytechnique.fr>,
 *                     Guillermo Sapiro <guille@umn.edu>
 * Copyright (C) 2011, Michael Zucchi <notzed@gmail.com>
 * All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package imagez.fx.dct;

import java.util.Arrays;

/**
 * DCT Denoising function.
 * 
 * Original code has been optimised somewhat for memory use and
 * single-threaded performance.
 * 
 * This code implements "DCT image denoising: a simple and effective image 
 * denoising algorithm".
 * http://www.ipol.im/pub/algo/ys_dct_denoising
 * portions Copyright, Guoshen Yu, Guillermo Sapiro, 2010.
 *
 * @author notzed
 */
public class DCT8Denoise {

	public void denoise(float[][] src, float[][] dst, int channels, int width, int height, float sigma) {
		int width_p = 8;
		int height_p = 8;
		float Th = sigma * 3.0f;
		DCT8x8 dct = new DCT8x8();
		DCT3 dct3 = new DCT3();
		float[] patches = new float[channels * width_p * height_p];

		for (int i = 0; i < dst.length; i++) {
			Arrays.fill(dst[i], 0.0f);
		}

		// 2D DCT forward and thresholding
		for (int j = 0; j < height - height_p + 1; j += 1) {
			for (int i = 0; i < width - width_p + 1; i += 1) {
				image2Patch(src, patches, width, height, channels, width_p, height_p, i, j);

				// for RGB: colour forward
				if (channels == 3) {
					dct3.forward(patches, patches, width_p, height_p);
				}

				for (int k = 0; k < channels; k++) {
					dct.forward(patches, k * width_p * height_p);
				}

				threshold(patches, width_p * height_p * channels, Th);

				for (int k = 0; k < channels; k++) {
					dct.inverse(patches, k * width_p * height_p);
				}

				// for RGB: colour inverse
				if (channels == 3) {
					dct3.inverse(patches, patches, width_p, height_p);
				}

				patch2Image(dst, patches, width, height, channels, width_p, height_p, i, j);
			}
		}

		// Normalise by weight
		normalise(dst, width, height, channels);
	}

	private void threshold(float[] patch, int length, float Th) {
		for (int i = 0; i < length; i++) {
			if (true) {
				if (Math.abs(patch[i]) < Th) {
					patch[i] = 0;
				}
			} else {
				float m = Math.max(Math.abs(patch[i]) - Th, 0);

				patch[i] = (m / (m + Th)) * patch[i];
			}
		}
	}

	private void image2Patch(float[][] src, float[] patches, int width, int height, int channels, int width_p, int height_p, int offx, int offy) {
		int size1 = width * height;
		int size2 = width_p * height_p;

		// loop over the pixels in the patch
		for (int kp = 0; kp < channels; kp++) {
			for (int jp = 0; jp < height_p; jp++) {
				for (int ip = 0; ip < width_p; ip++) {
					patches[kp * size2 + jp * width_p + ip] =
							src[kp][(offy + jp) * width + offx + ip];
				}
			}
		}
	}

	private void patch2Image(float[][] dst, float[] patches, int width, int height, int channels, int width_p, int height_p, int offx, int offy) {
		int size1 = width * height;
		int size2 = width_p * height_p;

		// loop over the pixels in the patch
		for (int kp = 0; kp < channels; kp++) {
			for (int jp = 0; jp < height_p; jp++) {
				for (int ip = 0; ip < width_p; ip++) {
					dst[kp][(offy + jp) * width + offx + ip] +=
							patches[kp * size2 + jp * width_p + ip];
				}
			}
		}
	}

	/**
	 * Normalise by patch weights.  i.e. how many tiles overlap a given pixel.
	 * 
	 * Since we build the destination using a regular pattern, we can calculate
	 * the weights here.
	 * 
	 * NB: not very good calculation.
	 * @param dst
	 * @param width
	 * @param height 
	 */
	private void normalise(float[][] dst, int width, int height, int channels) {
		for (int kp = 0; kp < channels; kp++) {
			for (int y = 0; y < height; y++) {
				float yc = y < 8 ? y + 1 : y >= height - 8 ? height - y : 8;
				for (int x = 0; x < width; x++) {
					float xc = x < 8 ? x + 1 : x >= width - 8 ? width - x : 8;
					int i = y * width + x;

					dst[kp][i] *= 1.0f / (xc * yc);
				}
			}
		}
	}
}
