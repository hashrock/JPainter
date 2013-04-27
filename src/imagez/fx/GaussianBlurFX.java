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

import imagez.image.ZLayer;
import java.awt.Rectangle;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gaussian blur engine.
 *
 * Uses multiple threads automatically.
 *
 * TODO: should have some sort of asynchronous 'done' interface.
 * @author notzed
 */
public class GaussianBlurFX {

	final int FMT_RGBA = 0;
	final int FMT_I = 1;
	// maximum length of a blur kernel == padding for images
	final int maxLength = 128;
	//
	//RGBALayer layer;
	//RGBALayer tmpLayer;
	ZLayer srcLayer;
	ZLayer dstLayer;
	final int format;
	int firstRow;
	int lastRow;
	CyclicBarrier barrier;
	Rectangle bounds;
	//
	float[] kernel;
	//
	//int rgbastride;
	//float[] rgba;
//
	int planestride;
	int planeOffset;
	float[] dest;
//
	//float[] r;
	//float[] g;
	//float[] b;
	//float[] a;
	//
	float[][] channels;

	// TODO: use as singleton and pass stuff to threads directly
	public GaussianBlurFX(ZLayer srcLayer, ZLayer dstLayer, Rectangle bounds) {
		// FIXME: I'm padding this too much, only need maxlength/2 padding
		int size = (bounds.width + maxLength * 2) * (bounds.height + maxLength * 2);

		this.bounds = bounds;

		format = FMT_I;
		int count = srcLayer.getChannelCount();
		channels = new float[count][];
		for (int i = 0; i < count; i++) {
			channels[i] = new float[size];
		}

		planestride = bounds.width + maxLength * 2;
		planeOffset = planestride * maxLength + maxLength;

		this.srcLayer = srcLayer;
		this.dstLayer = dstLayer;

		threadCount = Runtime.getRuntime().availableProcessors();
		threads = new Thread[threadCount];
		barrier = new CyclicBarrier(threadCount);
	}
//
	int threadCount;
	Thread[] threads;
	CountDownLatch latch;

	/**
	 * Start a blur operation.
	 *
	 * Use waitBlur() to wait for it to finish.
	 * TODO: async interface or soemthing
	 * @param sigma
	 */
	public void startBlur(float sigma) {
		int step = bounds.height / threadCount + 1;
		int start = 0;

		kernel = getKernel(sigma);

		// quick version just use a latch and run this whole thing synchronously
		latch = new CountDownLatch(threadCount);
		barrier.reset();

		for (int i = 0; i < threadCount; i++) {
			int finish = Math.min(start + step, bounds.height);
			threads[i] = new Thread(new Worker(start, finish));

			start = finish;

			threads[i].start();
		}
	}

	public void waitBlur() {
		try {
			latch.await();
		} catch (InterruptedException ex) {
			Logger.getLogger(GaussianBlurFX.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	float[] getKernel(float sigma) {
		int length = (int) (Math.ceil(sigma * 6 + 1));

		length = Math.min(maxLength, length);

		float[] kk = new float[length];

		double sig = -1.0 / (2 * sigma * sigma);
		//double fact = 1.0 / Math.sqrt(2 * Math.PI * sigma * sigma);
		float energy = 0;
		for (int j = 0; j < length; j++) {
			int y = (j - length / 2);
			float v = (float) Math.exp(sig * (y * y));

			kk[j] = v;
			energy += v;
		}
		for (int j = 0; j < length; j++) {
			kk[j] /= energy;
		}

		return kk;
	}

	class Worker implements Runnable {

		private final int start;
		private final int end;

		public Worker(int start, int end) {
			this.start = start;
			this.end = end;
		}

		@Override
		public void run() {
			try {
				blurLayers(srcLayer, dstLayer, bounds, kernel, kernel.length, channels);
			} catch (InterruptedException ex) {
				System.out.println("thread interrupted");
			} catch (BrokenBarrierException ex) {
				System.out.println("broken barrier");
			} catch (TimeoutException ex) {
				System.out.println("timeout");
			} catch (Exception ex) {
				System.out.println("shit  waiting  " + barrier.getNumberWaiting());
				if (!barrier.isBroken()) {
					System.out.println("reset barrier");
					barrier.reset();
					System.out.println("reset barrier done");
				}
				ex.printStackTrace();
			} finally {
				latch.countDown();
			}
		}

		// I think the best thing here is to do a bunch of rows
		// at once, keeping a running tally in tmp of the partial results
		// this blurs a single unpacked plane.  not currently used.
		void blurmt(float[] src, float[] dst, Rectangle bounds, float kernel[], int length)
				throws InterruptedException, BrokenBarrierException {
			int pos = start * planestride + planeOffset;
			int width = bounds.width;
			int pmod = planestride - (width);
			int height = bounds.height;

			// Perform horizontal

			pos -= length / 2;
			for (int j = start - length / 2; j < end; j++) {
				for (int i = 0; i < width; i++) {
					float v = src[pos] * kernel[0];
					for (int k = 1; k < length; k++) {
						v += src[pos + k] * kernel[k];
					}
					dst[pos + length / 2] = v;
					pos++;
				}
				pos += pmod;
			}

			// Need all horizontal done before doing vertical
			barrier.await();

			// err this is a bit shit
			for (int i = 0; i < width; i += 64) {
				pos = i + (start - length / 2) * planestride + planeOffset;
				for (int j = start - length / 2; j < end; j++) {
					for (int l = 0; l < 64; l++) {
						float v = 0;
						for (int k = 0; k < length; k++) {
							v += dst[pos + l + k * planestride] * kernel[k];
						}
						src[pos + l + (length / 2) * planestride] = v;
					}
					pos += planestride;
				}
			}

			// make sure everyone's finished with tmp
			barrier.await();
		}

		// This does all channels at once and writes straight back to the layer
		void blurLayers(ZLayer srcLayer, ZLayer dstLayer, Rectangle bounds, float kernel[], int length, float[][] workmem)
				throws InterruptedException, BrokenBarrierException, TimeoutException {
			int pos = start * planestride + planeOffset;
			int width = bounds.width;
			int pmod = planestride - (width);
			int height = bounds.height;
			int ccount = srcLayer.getChannelCount();
			int left = bounds.x;
			int xoff = maxLength - length / 2;
			float[][] tmp = new float[ccount][];

			for (int c = 0; c < ccount; c++) {
				tmp[c] = new float[planestride];
			}

			// Perform horizontal
			//pos -= length / 2;
			Rectangle srect = srcLayer.getBounds();
			for (int j = start; j < end; j++) {
				// unpack channels
				srcLayer.getLine(tmp, maxLength, srect.x, j+srect.y, width);
				for (int c = 0; c < ccount; c++) {
					float[] src = tmp[c];
					float[] dst = workmem[c];
					int dp = pos;

					// extend edges
					float es = src[maxLength];
					float ee = src[maxLength + width - 1];
					for (int k = 0; k < maxLength; k++) {
						src[k] = es;
						src[maxLength + width + k] = ee;
					}

					// do 1d convolution
					for (int i = 0; i < width; i++) {
						int s = i + xoff;
						float v = src[s] * kernel[0];
						for (int k = 1; k < length; k++) {
							v += src[s + k] * kernel[k];
						}
						dst[dp++] = v;
						try {
							assert !Float.isNaN(v);
						} catch (AssertionError e) {
							System.out.println("no shit sherlock");
						}
					}
				}
				pos += planestride;
			}

			// duplicate start/end rows
			// quick hack, threads should probably be assigned ranges & run after barrier
			// or give start/end thread less rows to do so it evens out
			if (threadCount == 1 || start == 0) {
				int l0pos = start * planestride + planeOffset;
				int linepos = l0pos - planestride;
				for (int j = 0; j < length / 2; j++) {
					for (int c = 0; c < ccount; c++) {
						float[] dst = workmem[c];
						for (int i = 0; i < width; i++) {
							dst[linepos + i] = dst[l0pos + i];
						}
					}
					linepos -= planestride;
				}
			}
			if (threadCount == 1 || end == bounds.height) {
				int l0pos = (end - 1) * planestride + planeOffset;
				int linepos = l0pos + planestride;
				for (int j = 0; j < length / 2; j++) {
					for (int c = 0; c < ccount; c++) {
						float[] dst = workmem[c];
						for (int i = 0; i < width; i++) {
							dst[linepos + i] = dst[l0pos + i];
						}
					}
					linepos += planestride;
				}
			}

			// Need all horizontal done before doing vertical
			barrier.await(5, TimeUnit.SECONDS);

			// Do vertical
			// this one does rows at a time which is is cache friendly (enough), at least for for small images
			Rectangle drect = dstLayer.getBounds();
			pos = (start - length / 2) * planestride + planeOffset;
			for (int j = start; j < end; j++) {
				for (int c = 0; c < ccount; c++) {
					float[] src = tmp[c];
					float[] dst = workmem[c];

					for (int l = 0; l < width; l++) {
						float v = 0;
						for (int k = 0; k < length; k++) {
							v += dst[pos + l + k * planestride] * kernel[k];
						}
						src[l] = v;
						assert !Float.isNaN(v);
					}
				}
				dstLayer.setLine(tmp, 0, drect.x, drect.y+j, width);
				pos += planestride;
			}

			// make sure everyone's finished
			barrier.await(5, TimeUnit.SECONDS);
		}
	}
}
