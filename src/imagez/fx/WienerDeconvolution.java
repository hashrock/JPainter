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
import imagez.image.ZLayer;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple complex number class.
 *
 * Mutable class is more efficient than immutable for calculations.
 *
 * This is only used as a place-holder to simplify calculations.  Never
 * store an array of these!
 * @author notzed
 */
class ZComplexF {

	float r;
	float i;

	public ZComplexF() {
	}

	public ZComplexF(float r, float i) {
		this.r = r;
		this.i = i;
	}

	public void get(float[] data, int index) {
		r = data[index * 2];
		i = data[index * 2 + 1];
	}

	public void set(float[] data, int index) {
		data[index * 2] = r;
		data[index * 2 + 1] = i;
	}

	public void csubf(ZComplexF b) {
		r -= b.r;
		i -= b.i;
	}

	public void caddf(ZComplexF b) {
		r += b.r;
		i += b.i;
	}

	public static void conj(ZComplexF a, ZComplexF c) {
		c.r = a.r;
		c.i = -a.i;
	}

	public static void cdivf(ZComplexF a, ZComplexF b, ZComplexF c) {
		float y, p, q, w;
		float ar = a.r;
		float ai = a.i;
		float br = b.r;
		float bi = b.i;

		y = ar * ar + ai * ai;
		p = br * ar + bi * ai;
		q = bi * ar - br * ai;

		if (y < 1.0f) {
			w = Float.MAX_VALUE * y;
			if ((Math.abs(p) > w) || (Math.abs(q) > w) || (y == 0.0f)) {
				c.r = Float.MAX_VALUE;
				c.i = Float.MAX_VALUE;
				//mtherr( "cdivf", OVERFLOW );
				return;
			}
		}
		c.r = p / y;
		c.i = q / y;
	}

	public static void cmulf(ZComplexF a, ZComplexF b, ZComplexF c) {
		float y;
		float ar = a.r;
		float ai = a.i;
		float br = b.r;
		float bi = b.i;

		y = br * ar - bi * ai;
		c.i = br * ai + bi * ar;
		c.r = y;
	}

	public static void caddf(ZComplexF a, ZComplexF b, ZComplexF c) {
		c.r = b.r + a.r;
		c.i = b.i + a.i;
	}

	public static void csubf(ZComplexF a, ZComplexF b, ZComplexF c) {
		c.r = b.r - a.r;
		c.i = b.i - a.i;
	}
}

/**
 * Performs a convolution in the frequency domain.
 *
 * @author notzed
 */
public class WienerDeconvolution {

	final ZLayer layer;
	final ZLayer toLayer;
	final int width;
	final int height;
	final FloatFFT_2D fft;
	final float[] filt;
	final float[] R, G, B, A;
	final float[][] data;
	// TODO: use one pool for all effects
	ExecutorService pool;

	public WienerDeconvolution(ZLayer layer, ZLayer toLayer) {
		this.layer = layer;
		this.toLayer = toLayer;

		width = layer.getBounds().width;
		height = layer.getBounds().height;

		fft = new FloatFFT_2D(height, width);

		R = new float[width * height * 2];
		G = new float[width * height * 2];
		B = new float[width * height * 2];
		A = new float[width * height * 2];
		data = new float[4][];
		data[0] = R;
		data[1] = G;
		data[2] = B;
		data[3] = A;

		filt = new float[width * height * 2];

		int nthreads = Runtime.getRuntime().availableProcessors();
		pool = Executors.newFixedThreadPool(nthreads);
	}

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

	// Wiener deconvolution is:
	// X =  Y * conj(H) / (abs(H)^2 + K)
	// H = fft of psf (filt)
	//
	void deconvolve(float[] dst, float[] filt, float K) {
		int len = dst.length / 2;

		ZComplexF y = new ZComplexF();
		ZComplexF h = new ZComplexF();
		ZComplexF fi = new ZComplexF();
		ZComplexF x = new ZComplexF();

		for (int i = 0; i < len; i += 1) {
			y.get(dst, i);
			h.get(filt, i);

			// D = abs(H)^2 + K
			float d = h.r * h.r + h.i * h.i + K;

			// FI = conj(H) / D
			fi.r = h.r / d;
			fi.i = -h.i / d;

			// X = Y * FI
			ZComplexF.cmulf(fi, y, x);

			x.set(dst, i);
		}
	}

	public void deconvolve(float sigma, float K) {
		long now = System.currentTimeMillis();

		// apply FFT to all layers of the rgba thing.

		// TODO: only target roi for optimisation

		// update/create filter
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
			int xx = (int) (sigma * 10);
			int off = width * height / 2 + width / 2 - (width * xx / 2) - xx / 2;
			for (int j = 0; j < xx; j++) {
				filt[off + j + j * width] = 1.0f / xx;
			}
		}
		fft.realForwardFull(filt);

		long diff = System.currentTimeMillis() - now;
		System.out.printf("Setup blur %f took %03d.%03d\n", sigma, diff / 1000, diff % 1000);

		// FIXME: only workes with non-moved layers

		// split channels
		for (int y = 0; y < height; y++) {
			layer.getLine(data, y * width, 0, y, width);
		}

		fft.realForwardFull(R);
		deconvolve(R, filt, K);
		fft.complexInverse(R, true);

		fft.realForwardFull(G);
		deconvolve(G, filt, K);
		fft.complexInverse(G, true);

		fft.realForwardFull(B);
		deconvolve(B, filt, K);
		fft.complexInverse(B, true);

		fft.realForwardFull(A);
		deconvolve(A, filt, K);
		fft.complexInverse(A, true);

		// We need to 'fftshift' the data - swap corner-opposing quadrants.
		// Do that in one step and form the result
		int si = 0;
		int di = width * height + width;

		float[][] tmp = new float[4][];
		for (int i = 0; i < 4; i++) {
			tmp[i] = new float[width];
		}

		int hw = width / 2;
		int ww = width - hw;
		int hh = height / 2;
		int wh = height - hh;

		System.out.printf("image size %d,%d\n", width, height);

		// a b
		// c d
		//  set a <- d
		copyQuad(ww, wh, toLayer, 0, 0, data, hw, hh, width, tmp);
		//  set d <- a
		copyQuad(hw, hh, toLayer, ww, wh, data, 0, 0, width, tmp);
		//  set b <- c
		copyQuad(hw, wh, toLayer, ww, 0, data, 0, hh, width, tmp);
		//  set c <- b
		copyQuad(ww, hh, toLayer, 0, wh, data, hw, 0, width, tmp);


		now = System.currentTimeMillis() - now;
		System.out.printf("Wiener Deconvolution %f took %03d.%03d\n", sigma, now / 1000, now % 1000);
		/*
		for (int i = 0; i < len; i++) {
		src[i * 4 + 0] = R[i * 2];
		src[i * 4 + 1] = G[i * 2];
		src[i * 4 + 2] = B[i * 2];
		src[i * 4 + 3] = A[i * 2];
		}
		 * */
	}

	// multi-threaded version
	// This isn't as big a win as i'd hoped since jtransforms is already multi-threading the
	// fft - which is the biggest cost here.

	public void deconvolveMT(float sigma, float K) {
		CyclicBarrier barrier = new CyclicBarrier(4);

		try {
			long now = System.currentTimeMillis();
			int nthreads = Runtime.getRuntime().availableProcessors();

			// apply FFT to all layers of the rgba thing.

			// TODO: only target roi for optimisation

			// Concurrently create the wiener filter, and copy the data from the image

			CountDownLatch latchFilter;
			CountDownLatch latchCopy;
			latchFilter = new CountDownLatch(nthreads);
			latchCopy = new CountDownLatch(nthreads);

			int trows = Math.max(1, height / nthreads);

			// update/create filter first
			for (int y = 0; y < height; y += trows) {
				pool.execute(new FilterJob(latchFilter, sigma, y, Math.min(y + trows, height)));
			}

			// then copy the data to our thingy
			for (int y = 0; y < height; y += trows) {
				pool.execute(new ExtractJob(latchCopy, y, Math.min(y + trows, height)));
			}

			// Wait for filter setup to be done, and then do the fft here
			latchFilter.await();
			fft.realForwardFull(filt);

			{
				long diff = System.currentTimeMillis() - now;
				System.out.printf("Setup blur %f took %03d.%03d\n", sigma, diff / 1000, diff % 1000);
			}

			// Now wait for the copy to be done (which it should be already)
			latchCopy.await();

			// Now launch the 4 separate tasks which convolve each channel and restore the results
			CountDownLatch latchConvolve = new CountDownLatch(4);

			for (int i = 0; i < 4; i++) {
				pool.execute(new ConvolveJob(barrier, latchConvolve, data[i], K, i));
			}

			latchConvolve.await();

			now = System.currentTimeMillis() - now;
			System.out.printf("Wiener Deconvolution %f took %03d.%03d\n", sigma, now / 1000, now % 1000);

		} catch (InterruptedException ex) {
			Logger.getLogger(WienerDeconvolution.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			// can i reset the barrier incase something gets broken?
		}
	}

	private class FilterJob implements Runnable {

		private final CountDownLatch latch;
		double sigma;
		int first;
		int last;

		public FilterJob(CountDownLatch latch, double sigma, int first, int last) {
			this.latch = latch;
			this.sigma = sigma;
			this.first = first;
			this.last = last;
		}

		@Override
		public void run() {
			// update/create filter
			try {
				int p = first * width;
				double sig = -1.0 / (2 * sigma * sigma);
				double fact = 1.0 / (2 * Math.PI * sigma * sigma);
				for (int j = first; j < last; j++) {
					int y = j - height / 2;
					for (int i = 0; i < width; i++, p++) {
						int x = i - width / 2;
						filt[p] = (float) (fact * Math.exp(sig * (x * x + y * y)));
					}
				}
			} finally {
				latch.countDown();
			}
		}
	}

	private class ExtractJob implements Runnable {

		private final CountDownLatch latch;
		int first;
		int last;

		public ExtractJob(CountDownLatch latch, int first, int last) {
			this.latch = latch;
			this.first = first;
			this.last = last;
		}

		@Override
		public void run() {
			try {
				for (int y = first; y < last; y++) {
					layer.getLine(data, y * width, 0, y, width);
				}
			} finally {
				latch.countDown();
			}
		}
	}

	private class ConvolveJob implements Runnable {

		private final CountDownLatch latch;
		float[] D;
		CyclicBarrier barrier;
		private final float K;
		int quadrant;

		public ConvolveJob(CyclicBarrier barrier, CountDownLatch latch, float[] D, float K, int quadrant) {
			this.latch = latch;
			this.D = D;
			this.barrier = barrier;
			this.K = K;
			this.quadrant = quadrant;
		}

		@Override
		public void run() {
			try {
				fft.realForwardFull(D);
				deconvolve(D, filt, K);
				fft.complexInverse(D, true);

				barrier.await();

				float[][] tmp = new float[4][];
				for (int i = 0; i < 4; i++) {
					tmp[i] = new float[width];
				}

				int hw = width / 2;
				int ww = width - hw;
				int hh = height / 2;
				int wh = height - hh;

				// a b
				// c d
				switch (quadrant) {
					case 0:
						//  set a <- d
						copyQuad(ww, wh, toLayer, 0, 0, data, hw, hh, width, tmp);
						break;
					case 1:
						//  set d <- a
						copyQuad(hw, hh, toLayer, ww, wh, data, 0, 0, width, tmp);
						break;
					case 2:
						//  set b <- c
						copyQuad(hw, wh, toLayer, ww, 0, data, 0, hh, width, tmp);
						break;
					case 3:
						//  set c <- b
						copyQuad(ww, hh, toLayer, 0, wh, data, hw, 0, width, tmp);
						break;
				}
			} catch (InterruptedException ex) {
				Logger.getLogger(WienerDeconvolution.class.getName()).log(Level.SEVERE, null, ex);
			} catch (BrokenBarrierException ex) {
				Logger.getLogger(WienerDeconvolution.class.getName()).log(Level.SEVERE, null, ex);
			} finally {
				latch.countDown();
			}
		}
	}

	private void copyQuad(int width, int height, ZLayer layer, int x, int y, float[][] src, int sx, int sy, int dstride, float[][] tmp) {
		// result = abs()
		System.out.printf("copy quad from %d,%d to %d,%d wqidth=%d height=%d\n", sx, sy, x, y, width, height);
		for (int j = 0; j < height; j++) {
			int di = ((j + sy) * dstride + sx) * 2;
			for (int i = 0; i < width; i++) {
				for (int k = 0; k < 3; k++) {
					tmp[k][i] = (float) Math.sqrt(src[k][di] * src[k][di] + src[k][di + 1] * src[k][di + 1]);
				}
				// force alpha (testing)
				tmp[3][i] = 1;
				di += 2;
			}
			// store
			layer.setLine(tmp, 0, x, j + y, width);
		}
	}
}
