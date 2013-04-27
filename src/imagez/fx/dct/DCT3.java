/*
 * Copyright (c) 2010, Guoshen Yu <yu@cmap.polytechnique.fr>,
 *                     Guillermo Sapiro <guille@umn.edu>
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

/**
 * 3 way colour transform.
 * @author notzed
 */
public class DCT3 {

	static float DCTbasis3[] = {
		0.5773502588272094726562500000000000000000f,
		0.5773502588272094726562500000000000000000f,
		0.5773502588272094726562500000000000000000f,
		//
		0.7071067690849304199218750000000000000000f,
		0.0000000000000000000000000000000000000000f,
		-0.7071067690849304199218750000000000000000f,
		//
		0.4082483053207397460937500000000000000000f,
		-0.8164966106414794921875000000000000000000f,
		0.4082483053207397460937500000000000000000f
	};

	/**
	 * Forward 3x3x3 DCT transform
	 * @param in data is stored in 3xplanes offset by width*height
	 * @param out
	 * @param width
	 * @param height 
	 */
	public void forward(float[] in, float[] out, int width, int height) {
		int size1 = width * height;

		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {
				int idx_pixel0 = j * width + i;
				int idx_pixel1 = 1 * size1 + j * width + i;
				int idx_pixel2 = 2 * size1 + j * width + i;

				// this is only so that in may == out
				float in0 = in[idx_pixel0];
				float in1 = in[idx_pixel1];
				float in2 = in[idx_pixel2];

				out[idx_pixel0] =
						(in0 * DCTbasis3[0 * 3 + 0]
						+ in1 * DCTbasis3[0 * 3 + 1]
						+ in2 * DCTbasis3[0 * 3 + 2]);

				out[idx_pixel1] =
						(in0 * DCTbasis3[1 * 3 + 0]
						+ in1 * DCTbasis3[1 * 3 + 1]
						+ in2 * DCTbasis3[1 * 3 + 2]);

				out[idx_pixel2] =
						(in0 * DCTbasis3[2 * 3 + 0]
						+ in1 * DCTbasis3[2 * 3 + 1]
						+ in2 * DCTbasis3[2 * 3 + 2]);
			}
		}
	}

	public void inverse(float[] in, float[] out, int width, int height) {
		int size1 = width * height;
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {
				int idx_pixel0 = j * width + i;
				int idx_pixel1 = 1 * size1 + j * width + i;
				int idx_pixel2 = 2 * size1 + j * width + i;

				// this is only so that in may == out
				float in0 = in[idx_pixel0];
				float in1 = in[idx_pixel1];
				float in2 = in[idx_pixel2];

				out[idx_pixel0] =
						(in0 * DCTbasis3[0 * 3 + 0]
						+ in1 * DCTbasis3[1 * 3 + 0]
						+ in2 * DCTbasis3[2 * 3 + 0]);

				out[idx_pixel1] =
						(in0 * DCTbasis3[0 * 3 + 1]
						+ in1 * DCTbasis3[1 * 3 + 1]
						+ in2 * DCTbasis3[2 * 3 + 1]);

				out[idx_pixel2] =
						(in0 * DCTbasis3[0 * 3 + 2]
						+ in1 * DCTbasis3[1 * 3 + 2]
						+ in2 * DCTbasis3[2 * 3 + 2]);
			}
		}
	}
}
