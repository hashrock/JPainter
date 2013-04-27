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
package imagez.fx.dct;

import imagez.image.ZLayer;

/**
 * This code implements "DCT image denoising: a simple and effective image 
 * denoising algorithm".
 * http://www.ipol.im/pub/algo/ys_dct_denoising
 * portions Copyright, Guoshen Yu, Guillermo Sapiro, 2010.
 * @author notzed
 */
public class DCTDenoise {

	ZLayer srcLayer;
	ZLayer dstLayer;
	int cc;
	float[][] tmp;
	float[][] tmpd;

	public DCTDenoise(ZLayer srcLayer, ZLayer dstLayer) {
		this.srcLayer = srcLayer;
		this.dstLayer = dstLayer;

		cc = srcLayer.getChannelCount();

		int size = srcLayer.getBounds().width * srcLayer.getBounds().height;

		tmp = new float[cc][size];
		tmpd = new float[cc][size];
	}

	public void denoise(float sigma) {
		DCT8Denoise den = new DCT8Denoise();

		int width = srcLayer.getBounds().width;
		int height = dstLayer.getBounds().height;

		// TODO: rather than require the whole data flat, could do it in 8x8 tiles

		for (int y = 0; y < height; y++) {
			srcLayer.getLine(tmp, y * width, 0, y, width);
		}

		long now = System.currentTimeMillis();
		den.denoise(tmp, tmpd, cc, width, height, sigma);
		now = System.currentTimeMillis() - now;
		System.out.printf("denoise %dx%d@%d took %d.%03ds\n", width, height, tmp.length, now / 1000, now % 1000);
		for (int y = 0; y < height; y++) {
			dstLayer.setLine(tmpd, y * width, 0, y, width);
		}

	}
}
