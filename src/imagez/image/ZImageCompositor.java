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
package imagez.image;

import imagez.blend.BlendMode;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Compositor that works with different backing formats and
 * generates an RGBA image.
 *
 * Multithreaded version should probably run
 * on a singleton, perhaps ZImage can manage that.
 * @author notzed
 */
public class ZImageCompositor {

	//
	class Worker implements Runnable {

		Rectangle bounds;
		BufferedImage result;
		ZLayer[] layers;
		ZLayerMono mask;

		public Worker(Rectangle bounds, ZLayer[] layers, ZLayerMono mask, BufferedImage result) {
			this.bounds = bounds;
			this.layers = layers;
			this.mask = mask;
			this.result = result;
		}

		@Override
		public void run() {
			try {
				compose(bounds, layers, mask, result);
			} catch (Exception x) {
				x.printStackTrace();
			}
			latch.countDown();
		}
	}
	CountDownLatch latch;
	ExecutorService pool;

	void composeMT(Rectangle bounds, ZLayer[] layers, ZLayerMono mask, BufferedImage result) {
		int nthreads = Runtime.getRuntime().availableProcessors();

		nthreads = Math.min(nthreads, bounds.height);

		int step = Math.max(1, (bounds.height / nthreads) + 1);
		int start = 0;

		if (pool == null) {
			pool = Executors.newCachedThreadPool();
		}

		//System.out.printf("using %d threads\n", nthreads);

		latch = new CountDownLatch(nthreads);
		for (int i = 0; i < nthreads; i++) {
			int finish = Math.min(start + step, bounds.height);
			pool.execute(new Worker(new Rectangle(bounds.x, bounds.y + start, bounds.width, finish - start), layers, mask, result));
			start = finish;
		}
		try {
			latch.await();
			//System.out.println("threads in pool " + ((ThreadPoolExecutor)pool).getPoolSize());
		} catch (InterruptedException ex) {
			Logger.getLogger(ZImageCompositor.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Compose the bounded contents of tmpLayer to the target layer using
	 * temporary layer mode mode.
	 * @param bounds
	 * @param layer
	 * @param tmpLayer
	 * @param mode
	 */
	public void composeDown(Rectangle bounds, ZLayer layer, ZLayer tmpLayer, ZLayerMono mask, int mode) {
		int height = bounds.height;
		int width = bounds.width;
		int left = bounds.x;
		int right = bounds.x + width;
		int top = bounds.y;
		int bottom = bounds.y + height;

		int imwidth = layer.getBounds().width;

		float[] mbuffer = new float[imwidth];
		float[] sbuffer = new float[imwidth * 4];
		float[] tbuffer = new float[imwidth * 4];
		float[] dbuffer = new float[imwidth * 4];

		Rectangle rect = layer.getBounds();
		int xstart = Math.max(rect.x, bounds.x);
		int xend = Math.min(rect.x + rect.width, bounds.x + bounds.width);
		int xwidth = xend - xstart;
		int xstart4 = xstart * 4;

		System.out.println("compose down " + bounds);

		for (int y = top; y < bottom; y++) {
			if (mask == null) {
				if (mode == ZLayer.TMP_REPLACE) {
					tmpLayer.getLineRGBA(dbuffer, xstart4, xstart, y, xwidth);
				} else {
					tmpLayer.getLineRGBA(sbuffer, xstart4, xstart, y, xwidth);
					layer.getLineRGBA(dbuffer, xstart4, xstart, y, xwidth);
					tmpLayer.mode.blendRGBA(dbuffer, xstart4, sbuffer, xstart4, tmpLayer.opacity, xwidth);
				}
			} else {
				if (mode == ZLayer.TMP_REPLACE) {
					tmpLayer.getLineRGBA(tbuffer, xstart4, xstart, y, xwidth);
					layer.getLineRGBA(sbuffer, xstart4, xstart, y, xwidth);
					mask.getLineMono(mbuffer, xstart, xstart, y, xwidth);
					alphamix(dbuffer, xstart4, tbuffer, xstart4, sbuffer, xstart4, mbuffer, xstart4, xwidth);
				} else {
					tmpLayer.getLineRGBA(tbuffer, xstart4, xstart, y, xwidth);
					layer.getLineRGBA(dbuffer, xstart4, xstart, y, xwidth);
					mask.getLineMono(mbuffer, xstart, xstart, y, xwidth);
					alphascale(sbuffer, xstart4, tbuffer, xstart4, mbuffer, xstart, xwidth);
					tmpLayer.mode.blendRGBA(dbuffer, xstart4, sbuffer, xstart4, tmpLayer.opacity, xwidth);
				}
			}
			layer.setLineRGBA(dbuffer, xstart4, xstart, y, xwidth);
		}
	}

	public void composeDownDelta(Rectangle bounds, ZLayer layer, ZLayer tmpLayer, ZLayerMono mask, int mode, ZLayer diff) {
		int height = bounds.height;
		int width = bounds.width;
		int left = bounds.x;
		int right = bounds.x + width;
		int top = bounds.y;
		int bottom = bounds.y + height;

		int imwidth = layer.getBounds().width + layer.getBounds().x;

		float[] mbuffer = new float[imwidth];
		float[] sbuffer = new float[imwidth * 4];
		float[] tbuffer = new float[imwidth * 4];
		float[] dbuffer = new float[imwidth * 4];
		float[] ybuffer = new float[imwidth * 4];
		float[] zbuffer = new float[imwidth * 4];

		Rectangle rect = layer.getBounds();
		int xstart = Math.max(rect.x, bounds.x);
		int xend = Math.min(rect.x + rect.width, bounds.x + bounds.width);
		int xwidth = xend - xstart;
		int xstart4 = xstart * 4;
		int xwidth4 = xwidth * 4;

		//System.out.println("compose down delta" + bounds);

		for (int y = top; y < bottom; y++) {
			boolean ispre = false;

			layer.getLineRGBA(zbuffer, xstart4, xstart, y, xwidth);

			// have to convert target to premultplied first for blending
			if (!BlendMode.premultiplyAlpha) {
				multiplyAlpha(ybuffer, zbuffer, xstart4, xwidth);
				ispre = true;
			}

			if (mask == null) {
				if (mode == ZLayer.TMP_REPLACE) {
					tmpLayer.getLineRGBA(dbuffer, xstart4, xstart, y, xwidth);
					ispre = false;
				} else {
					tmpLayer.getLineRGBA(sbuffer, xstart4, xstart, y, xwidth);
					//layer.getLine(dbuffer, xstart4, xstart, y, xwidth);
					System.arraycopy(ybuffer, xstart4, dbuffer, xstart4, xwidth4);
					tmpLayer.mode.blendRGBA(dbuffer, xstart4, sbuffer, xstart4, tmpLayer.opacity, xwidth);
				}
			} else {
				if (mode == ZLayer.TMP_REPLACE) {
					tmpLayer.getLineRGBA(tbuffer, xstart4, xstart, y, xwidth);
					//layer.getLine(sbuffer, xstart4, xstart, y, xwidth);
					mask.getLineMono(mbuffer, xstart, xstart, y, xwidth);
					alphamix(dbuffer, xstart4, tbuffer, xstart4, ybuffer, xstart4, mbuffer, xstart4, xwidth);
				} else {
					tmpLayer.getLineRGBA(tbuffer, xstart4, xstart, y, xwidth);
					//layer.getLine(dbuffer, xstart4, xstart, y, xwidth);
					mask.getLineMono(mbuffer, xstart, xstart, y, xwidth);
					alphascale(sbuffer, xstart4, tbuffer, xstart4, mbuffer, xstart, xwidth);
					System.arraycopy(ybuffer, xstart4, dbuffer, xstart4, xwidth4);
					tmpLayer.mode.blendRGBA(dbuffer, xstart4, sbuffer, xstart4, tmpLayer.opacity, xwidth);
				}
			}

			// convert back to non-premultiplied alpha since blending makes it that
			// hmm, well not exactly.  for non-touched pixels this fucks it up.  or does it?
			if (ispre) {
				divideAlpha(dbuffer, xstart4, xwidth);
			}

			// generate diff
			for (int i = xstart4; i < xstart4 + xwidth4; i += 4) {
				tbuffer[i + 0] = dbuffer[i + 0] - zbuffer[i + 0];
				tbuffer[i + 1] = dbuffer[i + 1] - zbuffer[i + 1];
				tbuffer[i + 2] = dbuffer[i + 2] - zbuffer[i + 2];
				tbuffer[i + 3] = dbuffer[i + 3] - zbuffer[i + 3];
			}
			diff.setLineRGBA(tbuffer, xstart4, xstart, y, xwidth);

			layer.setLineRGBA(dbuffer, xstart4, xstart, y, xwidth);
		}
	}

	/**
	 * Compose all layers within bounds and store result in result.
	 * @param bounds
	 * @param result
	 */
	public void compose(Rectangle bounds, ZLayer[] layers, ZLayerMono mask, BufferedImage result) {
		int height = bounds.height;
		int width = bounds.width;
		int left = bounds.x;
		int right = bounds.x + width;
		int top = bounds.y;
		int bottom = bounds.y + height;

		int imwidth = result.getWidth();

		int length = layers.length;

		Rectangle[] rbounds = new Rectangle[length];

		for (int i = 0; i < length; i++) {
			rbounds[i] = layers[i].getBounds();
			ZLayer l = layers[i];
			//System.out.println("layer " + l + " premul alpha = " + l.getImage().isAlphaPremultiplied());
			//if (l.tmpLayerMode != ZLayer.TMP_UNSET) {
			//	System.out.println("  tmplayer " + l.tmpLayer + " premul alpha = " + l.tmpLayer.getImage().isAlphaPremultiplied());
			//}
		}

		float[] mbuffer = new float[imwidth];
		float[] compose = new float[imwidth * 4];
		float[] sbuffer = new float[imwidth * 4];
		float[] tbuffer = new float[imwidth * 4];
		float[] xbuffer = new float[imwidth * 4];
		float[] ybuffer = new float[imwidth * 4];

		int[] target = ((DataBufferInt) result.getRaster().getDataBuffer()).getData();
		// ugh, convert this to real location using the right thing.
		// probably samplemodel.getoffset() will do
		int drow = imwidth * top + left;

		for (int y = top; y < bottom; y++) {
			Arrays.fill(compose, 0);

			for (int l = 0; l < length; l++) {
				Rectangle rect = rbounds[l];
				int xstart = Math.max(rect.x, bounds.x);
				int xend = Math.min(rect.x + rect.width, bounds.x + bounds.width);
				int xwidth = xend - xstart;

				if ((y >= rect.y) & (y < rect.y + rect.height) & (xwidth > 0)) {
					ZLayer layer = layers[l];
					int xstart4 = xstart * 4;
					int xwidth4 = xwidth * 4;

					// if type == float could optimise all of these to avoid an extra memcpy

					if (layer.tmpLayerMode == ZLayer.TMP_UNSET) {
						layer.getLineRGBA(sbuffer, xstart4, xstart, y, xwidth);
						layer.mode.blendRGBA(compose, xstart4, sbuffer, xstart4, layer.opacity, xwidth);
					} else {
						ZLayer tmpLayer = layer.tmpLayer;

						if (mask == null) {
							if (layer.tmpLayerMode == ZLayer.TMP_REPLACE) {
								// blend tmp layer data to target instead
								tmpLayer.getLineRGBA(sbuffer, xstart4, xstart, y, xwidth);
								tmpLayer.mode.blendRGBA(compose, xstart4, sbuffer, xstart4, tmpLayer.opacity, xwidth);
							} else {
								if (true) {
									// blend tmp layer to src, then result to target
									// For non-premultiplied mode, unfortunately since the blending assumes
									// the source is premultiplied and the dest isn't
									// it needs to change modes on the fly.
									// TODO: can another blend function handle this instead?
									layer.getLineRGBA(sbuffer, xstart4, xstart, y, xwidth);
									if (!BlendMode.premultiplyAlpha) {
										multiplyAlpha(sbuffer, xstart4, xwidth);
									}
									tmpLayer.getLineRGBA(tbuffer, xstart4, xstart, y, xwidth);
									tmpLayer.mode.blendRGBA(sbuffer, xstart4, tbuffer, xstart4, tmpLayer.opacity, xwidth);
									if (!BlendMode.premultiplyAlpha) {
										divideAlpha(sbuffer, xstart4, xwidth);
									}
									layer.mode.blendRGBA(compose, xstart4, sbuffer, xstart4, layer.opacity, xwidth);
								} else {
									// THIS IS BROKEN
									// ** Need to do it the other way ... but need to convert back from
									// ** pre-multiplied as well.
									// blend src to target
									// blend tmp to target
									layer.getLineRGBA(sbuffer, xstart4, xstart, y, xwidth);
									tmpLayer.getLineRGBA(tbuffer, xstart4, xstart, y, xwidth);
									layer.mode.blendRGBA(compose, xstart4, sbuffer, xstart4, layer.opacity, xwidth);
									tmpLayer.mode.blendRGBA(compose, xstart4, tbuffer, xstart4, tmpLayer.opacity, xwidth);
								}
							}
						} else {
							if (layer.tmpLayerMode == ZLayer.TMP_REPLACE) {
								// blend tmp layer to target tmp
								// blend src layer to target tmp
								// alpha-mix/replace the two to target
								layer.getLineRGBA(sbuffer, xstart4, xstart, y, xwidth);
								System.arraycopy(compose, xstart4, xbuffer, xstart4, xwidth4);
								layer.mode.blendRGBA(xbuffer, xstart4, sbuffer, xstart4, layer.opacity, xwidth);

								tmpLayer.getLineRGBA(tbuffer, xstart4, xstart, y, xwidth);
								System.arraycopy(compose, xstart4, ybuffer, xstart4, xwidth4);
								tmpLayer.mode.blendRGBA(ybuffer, xstart4, tbuffer, xstart4, tmpLayer.opacity, xwidth);

								mask.getLineMono(mbuffer, xstart, xstart, y, xwidth);

								alphamix(compose, xstart4, ybuffer, xstart4, xbuffer, xstart4, mbuffer, xstart, xwidth);
							} else {
								// apply mask to tmp
								// blend masked tmp to src
								// blend result to dest
								// is this the right order?
								tmpLayer.getLineRGBA(tbuffer, xstart4, xstart, y, xwidth);
								mask.getLineMono(mbuffer, xstart, xstart, y, xwidth);
								alphascale(xbuffer, xstart4, tbuffer, xstart4, mbuffer, xstart, xwidth);
								layer.getLineRGBA(sbuffer, xstart4, xstart, y, xwidth);
								if (!BlendMode.premultiplyAlpha) {
									multiplyAlpha(sbuffer, xstart4, xwidth);
								}
								tmpLayer.mode.blendRGBA(sbuffer, xstart4, xbuffer, xstart4, tmpLayer.opacity, xwidth);
								if (!BlendMode.premultiplyAlpha) {
									divideAlpha(sbuffer, xstart4, xwidth);
								}
								layer.mode.blendRGBA(compose, xstart4, sbuffer, xstart4, layer.opacity, xwidth);
							}
						}
					}
				}
			}

			// Blend with checkerboard (do in convertrow?)
			if (BlendMode.premultiplyAlpha) {
				for (int x = left; x < right; x++) {
					float I = (((y ^ x) >> 4) & 1) * 0.3f + 0.35f;
					float A = compose[x * 4 + 3];

					compose[x * 4 + 0] = compose[x * 4 + 0] + I * (1 - A);
					compose[x * 4 + 1] = compose[x * 4 + 1] + I * (1 - A);
					compose[x * 4 + 2] = compose[x * 4 + 2] + I * (1 - A);
					//compose[x * 4 + 0] = compose[x * 4 + 0] * A + I * (1 - A);
					//compose[x * 4 + 1] = compose[x * 4 + 1] * A + I * (1 - A);
					//compose[x * 4 + 2] = compose[x * 4 + 2] * A + I * (1 - A);
				}
			} else {
				for (int x = left; x < right; x++) {
					float I = (((y ^ x) >> 4) & 1) * 0.3f + 0.35f;
					float A = compose[x * 4 + 3];

					compose[x * 4 + 0] = compose[x * 4 + 0] + I * (1 - A);
					compose[x * 4 + 1] = compose[x * 4 + 1] + I * (1 - A);
					compose[x * 4 + 2] = compose[x * 4 + 2] + I * (1 - A);
					//compose[x * 4 + 0] = compose[x * 4 + 0] * A + I * (1 - A);
					//compose[x * 4 + 1] = compose[x * 4 + 1] * A + I * (1 - A);
					//compose[x * 4 + 2] = compose[x * 4 + 2] * A + I * (1 - A);
				}
			}

			// now convert the composed result into displayable/output format
			convertRow(target, drow, compose, left * 4, left, right);
			drow += imwidth;
		}
	}

	private void convertRow(int[] target, int dp, float[] compose, int cp, int left, int right) {
		for (int x = left; x < right; x++) {
			float fr = compose[cp++];
			float fg = compose[cp++];
			float fb = compose[cp++];
			float fa = compose[cp++];
			fa = 1;
			int R = (int) (fr * fa * 255.0f);
			int G = (int) (fg * fa * 255.0f);
			int B = (int) (fb * fa * 255.0f);
			int A = (int) (fa * 255.0f);

			R = Math.max(R, 0);
			G = Math.max(G, 0);
			B = Math.max(B, 0);
			A = Math.max(A, 0);
			R = Math.min(R, 255);
			G = Math.min(G, 255);
			B = Math.min(B, 255);
			A = Math.min(A, 255);
			A = 255;
			target[dp++] = (A << 24) | (R << 16) | (G << 8) | B;
		}
	}

	private void convertRow(byte[] target, int dp, float[] compose, int cp, int left, int right) {
		for (int x = left; x < right; x++) {
			float fr = compose[cp++];
			float fg = compose[cp++];
			float fb = compose[cp++];
			float fa = compose[cp++];
			int R = (int) (fr * fa * 255.0f);
			int G = (int) (fg * fa * 255.0f);
			int B = (int) (fb * fa * 255.0f);
			int A = (int) (fa * 255.0f);

			R = Math.max(R, 0);
			G = Math.max(G, 0);
			B = Math.max(B, 0);
			A = Math.max(A, 0);
			R = Math.min(R, 255);
			G = Math.min(G, 255);
			B = Math.min(B, 255);
			A = Math.min(A, 255);

			target[dp++] = (byte) A;
			target[dp++] = (byte) B;
			target[dp++] = (byte) G;
			target[dp++] = (byte) R;
		}
	}

	private void alphascale(float[] dbuffer, int dp, float[] tbuffer, int tp, float[] mbuffer, int mp, int width) {
		for (int x = 0; x < width; x++) {
			float m = mbuffer[mp++];
			float m1 = (1 - m);

			dbuffer[dp++] = tbuffer[tp++] * m;
			dbuffer[dp++] = tbuffer[tp++] * m;
			dbuffer[dp++] = tbuffer[tp++] * m;
			dbuffer[dp++] = tbuffer[tp++] * m; // ??
		}
	}

	private void alphamix(float[] dbuffer, int dp, float[] tbuffer, int tp, float[] sbuffer, int sp, float[] mbuffer, int mp, int width) {
		for (int x = 0; x < width; x++) {
			float m = mbuffer[mp + x];
			float m1 = (1 - m);

			dbuffer[dp++] = sbuffer[sp++] * m1 + tbuffer[tp++] * m;
			dbuffer[dp++] = sbuffer[sp++] * m1 + tbuffer[tp++] * m;
			dbuffer[dp++] = sbuffer[sp++] * m1 + tbuffer[tp++] * m;
			dbuffer[dp++] = sbuffer[sp++] * m1 + tbuffer[tp++] * m; // ??
		}
	}

	private void divideAlpha(float[] dbuffer, int doff, int width) {
		for (int i = 0; i < width; i++) {
			float a = dbuffer[doff + 3];

			a = (Math.abs(a) < 1E-5) ? 0 : 1.0f / a;

			dbuffer[doff + 0] *= a;
			dbuffer[doff + 1] *= a;
			dbuffer[doff + 2] *= a;
			doff += 4;
		}
	}

	private void multiplyAlpha(float[] dbuffer, float[] sbuffer, int doff, int width) {
		for (int i = 0; i < width; i++) {
			float a = sbuffer[doff + 3];
			dbuffer[doff + 0] = sbuffer[doff + 0] * a;
			dbuffer[doff + 1] = sbuffer[doff + 1] * a;
			dbuffer[doff + 2] = sbuffer[doff + 2] * a;
			dbuffer[doff + 3] = a;
			doff += 4;
		}
	}

	private void multiplyAlpha(float[] dbuffer, int doff, int width) {
		for (int i = 0; i < width; i++) {
			float a = dbuffer[doff + 3];
			dbuffer[doff + 0] *= a;
			dbuffer[doff + 1] *= a;
			dbuffer[doff + 2] *= a;
			doff += 4;
		}
	}

	public enum ComposeBackground {

		Transparent,
		CheckerBoard,
		SolidColour,
	}

	/**
	 * Another version of compose above, but stores to a `layer' and doesn't
	 * pre-multiply alpha.  Also the background is options.
	 * 
	 * The layer is just used as a holder/abstraction for different backing
	 * formats.
	 * 
	 * The only difference is the final stage ... subclass?
	 * @param bounds
	 * @param layers
	 * @param mask
	 * @param result
	 */
	public void flattenImage(Rectangle bounds, ZLayer[] layers, ZLayerMono mask, ZLayer result) {
		int height = bounds.height;
		int width = bounds.width;
		int left = bounds.x;
		int right = bounds.x + width;
		int top = bounds.y;
		int bottom = bounds.y + height;

		int imwidth = result.getBounds().width;

		int length = layers.length;

		Rectangle[] rbounds = new Rectangle[length];

		for (int i = 0; i < length; i++) {
			rbounds[i] = layers[i].getBounds();
		}

		float[] mbuffer = new float[imwidth];
		float[] compose = new float[imwidth * 4];
		float[] sbuffer = new float[imwidth * 4];
		float[] tbuffer = new float[imwidth * 4];
		float[] xbuffer = new float[imwidth * 4];
		float[] ybuffer = new float[imwidth * 4];

		// ugh, convert this to real location using the right thing.
		// probably samplemodel.getoffset() will do
		int drow = imwidth * top + left;

		for (int y = top; y < bottom; y++) {
			Arrays.fill(compose, 0);

			for (int l = 0; l < length; l++) {
				Rectangle rect = rbounds[l];
				int xstart = Math.max(rect.x, bounds.x);
				int xend = Math.min(rect.x + rect.width, bounds.x + bounds.width);
				int xwidth = xend - xstart;

				if ((y >= rect.y) & (y < rect.y + rect.height) & (xwidth > 0)) {
					ZLayer layer = layers[l];
					int xstart4 = xstart * 4;
					int xwidth4 = xwidth * 4;

					// if type == float could optimise all of these to avoid an extra memcpy

					if (layer.tmpLayerMode == ZLayer.TMP_UNSET) {
						layer.getLineRGBA(sbuffer, xstart4, xstart, y, xwidth);
						layer.mode.blendRGBA(compose, xstart4, sbuffer, xstart4, layer.opacity, xwidth);
					} else {
						ZLayer tmpLayer = layer.tmpLayer;

						if (mask == null) {
							if (layer.tmpLayerMode == ZLayer.TMP_REPLACE) {
								// blend tmp layer data to target instead
								tmpLayer.getLineRGBA(sbuffer, xstart4, xstart, y, xwidth);
								tmpLayer.mode.blendRGBA(compose, xstart4, sbuffer, xstart4, tmpLayer.opacity, xwidth);
							} else {
								// blend tmp layer to src, then result to target
								// is this order right?  does it matter?
								layer.getLineRGBA(sbuffer, xstart4, xstart, y, xwidth);
								tmpLayer.getLineRGBA(tbuffer, xstart4, xstart, y, xwidth);
								tmpLayer.mode.blendRGBA(sbuffer, xstart4, tbuffer, xstart4, tmpLayer.opacity, xwidth);
								layer.mode.blendRGBA(compose, xstart4, sbuffer, xstart4, layer.opacity, xwidth);
							}
						} else {
							if (layer.tmpLayerMode == ZLayer.TMP_REPLACE) {
								// blend tmp layer to target tmp
								// blend src layer to target tmp
								// alpha-mix/replace the two to target
								layer.getLineRGBA(sbuffer, xstart4, xstart, y, xwidth);
								System.arraycopy(compose, xstart4, xbuffer, xstart4, xwidth4);
								layer.mode.blendRGBA(xbuffer, xstart4, sbuffer, xstart4, layer.opacity, xwidth);

								tmpLayer.getLineRGBA(tbuffer, xstart4, xstart, y, xwidth);
								System.arraycopy(compose, xstart4, ybuffer, xstart4, xwidth4);
								tmpLayer.mode.blendRGBA(ybuffer, xstart4, tbuffer, xstart4, tmpLayer.opacity, xwidth);

								mask.getLineMono(mbuffer, xstart, xstart, y, xwidth);

								alphamix(compose, xstart4, ybuffer, xstart4, xbuffer, xstart4, mbuffer, xstart, xwidth);
							} else {
								// apply mask to tmp
								// blend masked tmp to src
								// blend result to dest
								// is this the right order?
								tmpLayer.getLineRGBA(tbuffer, xstart4, xstart, y, xwidth);
								mask.getLineMono(mbuffer, xstart, xstart, y, xwidth);
								alphascale(xbuffer, xstart4, tbuffer, xstart4, mbuffer, xstart, xwidth);
								layer.getLineRGBA(sbuffer, xstart4, xstart, y, xwidth);
								tmpLayer.mode.blendRGBA(sbuffer, xstart4, xbuffer, xstart4, tmpLayer.opacity, xwidth);
								layer.mode.blendRGBA(compose, xstart4, sbuffer, xstart4, layer.opacity, xwidth);
							}
						}
					}
				}
			}

			if (false) {
				// Blend with checkerboard
				for (int x = left; x < right; x++) {
					float I = (((y ^ x) >> 4) & 1) * 0.3f + 0.35f;
					float A = compose[x * 4 + 3];

					compose[x * 4 + 0] = compose[x * 4 + 0] * A + I * (1 - A);
					compose[x * 4 + 1] = compose[x * 4 + 1] * A + I * (1 - A);
					compose[x * 4 + 2] = compose[x * 4 + 2] * A + I * (1 - A);
				}
			}

			// conver to non-premultiplied alpha
			if (!BlendMode.premultiplyAlpha) {
				for (int x = left; x < right; x++) {
					float I = (((y ^ x) >> 4) & 1) * 0.3f + 0.35f;
					float A = compose[x * 4 + 3];

					if (Math.abs(A) > 1E-5) {
						compose[x * 4 + 0] /= A;
						compose[x * 4 + 1] /= A;
						compose[x * 4 + 2] /= A;
					} else {
						compose[x * 4 + 0] = 0;
						compose[x * 4 + 1] = 0;
						compose[x * 4 + 2] = 0;
					}
				}
			}
			result.setLineRGBA(compose, left * 4, left, y, right - left);
			drow += imwidth;
		}
	}
}
