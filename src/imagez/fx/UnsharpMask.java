/*
 * Copyright (C) 2011 notzed
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
package imagez.fx;

import imagez.image.ZLayer;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements unsharp mask.
 * 
 * Rather than use the existing gaussian filter, this is a newer/simpler implementation
 * @author notzed
 */
public class UnsharpMask {

	ZLayer srcLayer;
	ZLayer dstLayer;
	final int maxLength = 128;
	float[][] tmpbuffer;
	// TODO: use one pool for all effects
	ExecutorService pool;

	public UnsharpMask(ZLayer srcLayer, ZLayer dstLayer) {
		this.srcLayer = srcLayer;
		this.dstLayer = dstLayer;

		int cc = srcLayer.getChannelCount();
		int size = srcLayer.getBounds().width * srcLayer.getBounds().height;
		tmpbuffer = new float[cc][size];

		int nthreads = Runtime.getRuntime().availableProcessors();
		pool = Executors.newFixedThreadPool(nthreads);

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

	public void unsharpMask(float sigma, float w) {
		try {
			long now = System.currentTimeMillis();
			int nthreads = Runtime.getRuntime().availableProcessors();

			int height = srcLayer.getBounds().height;
			int width = srcLayer.getBounds().height;
			int nrows = Math.max(1, height / nthreads);

			CyclicBarrier barrier = new CyclicBarrier(nthreads);
			CountDownLatch latch = new CountDownLatch(nthreads);

			float[] xkernel = getKernel(sigma);

			for (int i = 0; i < nthreads; i++) {
				int first = Math.min(height, i*nrows);
				int last = i==nthreads -1 ? height : Math.min(height, first+nrows);
				pool.execute(new UnsharpWorker(first, last, barrier, latch, w, xkernel, xkernel));
			}
			latch.await();
		} catch (InterruptedException ex) {
			Logger.getLogger(UnsharpMask.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private class UnsharpWorker implements Runnable {

		int start;
		int finish;
		CyclicBarrier barrier;
		CountDownLatch latch;
		private final float weight;
		float[] xkernel;
		float[] ykernel;

		public UnsharpWorker(int start, int finish, CyclicBarrier barrier, CountDownLatch latch, float w, float[] xkernel, float[] ykernel) {
			this.start = start;
			this.finish = finish;
			this.barrier = barrier;
			this.latch = latch;
			this.weight = w;
			this.xkernel = xkernel;
			this.ykernel = ykernel;
		}

		// mirror edges
		private int edge(int v, int m) {
			return v < 0 ? -v
					: (v < m ? v
					: (m + m - 1 - v));
		}

		@Override
		public void run() {
			int width = srcLayer.getBounds().width;
			int height = srcLayer.getBounds().height;

			try {
				int ccount = srcLayer.getChannelCount();
				float[][] tmp = new float[ccount][];

				for (int c = 0; c < ccount; c++) {
					tmp[c] = new float[width];
				}
				
				// for unsharp mask: local copy of source again
				float[][] img = new float[ccount][];
				for (int c = 0; c < ccount; c++) {
					img[c] = new float[width];
				}

				int kh;

				kh = xkernel.length / 2;
				// First filter in X, for our band of data
				for (int y = start; y < finish; y++) {
					srcLayer.getLine(tmp, 0, 0, y, width);

					for (int x = 0; x < width; x++) {
						for (int c = 0; c < ccount; c++) {
							float v = 0;
							float[] src = tmp[c];

							for (int k = 0; k < xkernel.length; k++) {
								int j = edge(x + k - kh, width);
								v += xkernel[k] * src[j];
							}
							tmpbuffer[c][x + y * width] = v;
						}
					}
				}

				// Wait for all X filters to be done
				barrier.await();

				float w = Math.max(0, Math.min(weight, 0.95f));
				float scale = (1.0f / (1.0f - w));
				// Now filter in y, we do rows at a time (moderately cache friendly)
				kh = ykernel.length / 2;
				for (int y = start; y < finish; y++) {
					srcLayer.getLine(img, 0, 0, y, width);
					
					for (int x = 0; x < width; x++) {
						for (int c = 0; c < ccount; c++) {
							float v = 0;
							float[] src = tmpbuffer[c];

							for (int k = 0; k < ykernel.length; k++) {
								int j = edge(y + k - kh, height);

								v += xkernel[k] * src[j * width + x];
							}
							
							// this does the unsharp mask
							v = (img[c][x] - w * v) * scale;
							
							tmp[c][x] = v;
						}
					}
					dstLayer.setLine(tmp, 0, 0, y, width);
				}

			} catch (InterruptedException ex) {
				Logger.getLogger(UnsharpMask.class.getName()).log(Level.SEVERE, null, ex);
			} catch (BrokenBarrierException ex) {
				Logger.getLogger(UnsharpMask.class.getName()).log(Level.SEVERE, null, ex);
			} finally {
				latch.countDown();
			}
		}
	}
}
