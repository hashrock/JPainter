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
package imagez.fx;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_2D;
import edu.emory.mathcs.utils.ConcurrencyUtils;
import imagez.image.ZLayerRGBAFloat;

/**
 * Performs a convolution in the frequency domain.
 *
 * @author notzed
 */
public class FrequencyConvolution {

	// convolve in frequency domain.
	// convolve two complex arrays.
	// i.e. complex multiply them
	void convolve(float[] dst, float[] filt) {
		int len = dst.length;

		for (int i = 0; i < len; i += 2) {
			float a = dst[i];
			float b = dst[i + 1];
			float c = filt[i];
			float d = filt[i + 1];

			dst[i] = a * c - b * d;
			dst[i + 1] = a * d + b * c;
		}
	}
	static FloatFFT_2D fft;

	public void GaussianBlur(ZLayerRGBAFloat layer, float sigma) {
		int width, height;

		float[] src = layer.getData();

		float[] filt;

		long now = System.currentTimeMillis();

		// apply FFT to all layers of the rgba thing.

		// TODO: only target roi!

		// first, extract channels
		width = layer.getBounds().width;
		height = layer.getBounds().height;

		int len = width * height;

		if (fft == null) {
			fft = new FloatFFT_2D(height, width);
		}
		System.out.printf("threads threshold = %d\n", ConcurrencyUtils.getThreadsBeginN_2D());
		System.out.printf(" threads = %d\n", ConcurrencyUtils.getNumberOfThreads());

		//ConcurrencyUtils.setThreadsBeginN_1D_FFT_4Threads(512);
		//ConcurrencyUtils.setNumberOfThreads(1);

		// create filter
		filt = new float[width * height * 2];
		if (true) {
			int p = 0;
			double sig = -1.0 / (2 * sigma * sigma);
			double fact = 1.0 / (2 * Math.PI * sigma * sigma);
			for (int j = 0; j < height; j++) {
				int y = j - height / 2;
				for (int i = 0; i < width; i++, p++) {
					int x = i - width / 2;
					filt[p] = (float) (fact * Math.exp(sig * (x * x + y * y)));
				}
			}
		} else {
			int xx = (int)(sigma*10);
			int off = width*height/2+width/2-(width*xx/2)-xx/2;
			for (int j=0;j<xx;j++) {
				filt[off+j+j*width] = 1.0f/xx;
			}
		}
		fft.realForwardFull(filt); // <- could be cached i suppose

		long diff = System.currentTimeMillis() - now;
		System.out.printf("Setup blur %f took %03d.%03d\n", sigma, diff / 1000, diff % 1000);

		// split channels
		float[] R = new float[width * height * 2];
		float[] G = new float[width * height * 2];
		float[] B = new float[width * height * 2];
		float[] A = new float[width * height * 2];
		for (int i = 0; i < len; i++) {
			R[i] = src[i * 4 + 0];
			G[i] = src[i * 4 + 1];
			B[i] = src[i * 4 + 2];
			A[i] = src[i * 4 + 3];
		}

		fft.realForwardFull(R);
		convolve(R, filt);
		fft.complexInverse(R, true);

		fft.realForwardFull(G);
		convolve(G, filt);
		fft.complexInverse(G, true);

		fft.realForwardFull(B);
		convolve(B, filt);
		fft.complexInverse(B, true);

		fft.realForwardFull(A);
		convolve(A, filt);
		fft.complexInverse(A, true);

		// We need to 'fftshift' the data - swap corner-opposing quadrants.
		// Do that in one step and form the result
		int si = 0;
		int di = width * height + width;

		copyQuad(width, height, src, 0, width * 2, R, G, B, A, width * height + width, width);
		copyQuad(width, height, src, width * 2, width * 2, R, G, B, A, width * height, width);
		copyQuad(width, height, src, width * 4 * height / 2, width * 2, R, G, B, A, width, width);
		copyQuad(width, height, src, width * 4 * height / 2 + width * 2, width * 2, R, G, B, A, 0, width);


		now = System.currentTimeMillis() - now;
		System.out.printf("Gaussian blur %f took %03d.%03d\n", sigma, now / 1000, now % 1000);
		/*
		for (int i = 0; i < len; i++) {
		src[i * 4 + 0] = R[i * 2];
		src[i * 4 + 1] = G[i * 2];
		src[i * 4 + 2] = B[i * 2];
		src[i * 4 + 3] = A[i * 2];
		}
		 * */
	}

	private void copyQuad(int width, int height, float[] src, int si, int smod, float[] R, float[] G, float[] B, float[] A, int di, int dmod) {
		for (int j = 0; j < height / 2; j++) {
			for (int i = 0; i < width / 2; i++) {
				src[si + 0] = R[di];
				src[si + 1] = G[di];
				src[si + 2] = B[di];
				src[si + 3] = 1;//Math.abs(A[di]);
				si += 4;
				di += 2;
			}
			si += smod;
			di += dmod;
		}
	}
}
