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

import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

/**
 *
 * @author notzed
 */
class ZFLoatCompositeContext implements CompositeContext {

	float alpha;

	public ZFLoatCompositeContext(float alpha) {
		this.alpha = alpha;
	}

	public void dispose() {
		//System.out.println("dispose float cc");
	}

	public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
		SampleModel sm = src.getSampleModel();
		ComponentSampleModel csm = (ComponentSampleModel) sm;
		int w = src.getWidth();
		int h = src.getHeight();
		int spos = csm.getBankIndices()[0];
		int smod = csm.getScanlineStride() - w * 4;
		float[] sf = ((DataBufferFloat) src.getDataBuffer()).getData();

		spos = csm.getOffset(0, 0);
		System.out.println("dst offset = " + dstIn.getSampleModelTranslateX());

		ComponentSampleModel ism = (ComponentSampleModel) dstIn.getSampleModel();
		int ipos = ism.getBankIndices()[0];
		int imod = ism.getScanlineStride() - w * 4;
		DataBufferFloat dib = (DataBufferFloat) dstIn.getDataBuffer();

		ComponentSampleModel dsm = (ComponentSampleModel) dstOut.getSampleModel();
		int dpos = dsm.getBankIndices()[0];
		int dmod = dsm.getScanlineStride() - w * 4;
		DataBufferFloat ddb = (DataBufferFloat) dstOut.getDataBuffer();

		spos -= src.getSampleModelTranslateX()*4 + src.getSampleModelTranslateY() * csm.getScanlineStride();
		ipos -= dstIn.getSampleModelTranslateX()*4 + dstIn.getSampleModelTranslateY() * csm.getScanlineStride();
		dpos -= dstOut.getSampleModelTranslateX()*4 + dstOut.getSampleModelTranslateY() * csm.getScanlineStride();

		//(DataBufferFloat) dsm.createDataBuffer();
		float[] df = dib.getData();

		DataBufferFloat sdb = (DataBufferFloat) src.getDataBuffer();
		if (false) {
			System.out.printf(" stride = %d dmod = %d\n", dsm.getScanlineStride(), dmod);
		}

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				float rS = sf[spos++] * alpha;
				float gS = sf[spos++] * alpha;
				float bS = sf[spos++] * alpha;
				float aS = sf[spos++] * alpha;
				float rD = df[ipos++];
				float gD = df[ipos++];
				float bD = df[ipos++];
				float aD = df[ipos++];
				//float rS = sdb.getElemFloat(spos++);
				//float gS = sdb.getElemFloat(spos++);
				//float bS = sdb.getElemFloat(spos++);
				//float aS = sdb.getElemFloat(spos++);
				//float rD = dib.getElemFloat(ipos++);
				//float gD = dib.getElemFloat(ipos++);
				//float bD = dib.getElemFloat(ipos++);
				//float aD = dib.getElemFloat(ipos++);

				//a = 1.0f;
				//r = 0.0f;
				//g = 0.0f;
				//b = 1.0f;

				rD = rS + rD*(1-aS);
				gD = gS + gD*(1-aS);
				bD = bS + bD*(1-aS);
				aD = aS + aD*(1-aS);

				ddb.setElemFloat(dpos++, rD);
				ddb.setElemFloat(dpos++, gD);
				ddb.setElemFloat(dpos++, bD);
				ddb.setElemFloat(dpos++, aD);
				//df[dpos++] = 0.75f;
				//df[dpos++] = 1.0f;
				//df[dpos++] = 0.5f;
				//df[dpos++] = 1.0f;
			}
			dpos += dmod;
			spos += smod;
			ipos += imod;
		}
	}
}

public class ZFloatComposite implements Composite {

	private final float alpha;

	public ZFloatComposite(float alpha) {
		this.alpha = alpha;
	}

	public CompositeContext createContext(ColorModel src, ColorModel dst, RenderingHints hints) {
		int stt = src.getTransferType();
		int dtt = dst.getTransferType();

		//System.out.println("create context for " + src + " to " + dst + " stt " + stt);
		//System.out.println(" wantd dt = " + DataBuffer.TYPE_FLOAT);

		if (stt != dtt
				|| stt != DataBuffer.TYPE_FLOAT
				|| src.getNumComponents() != 4
				|| dst.getNumComponents() != 4) {
			throw new RuntimeException("data types incompatible");
		}

		if (src.getTransferType()
				!= dst.getTransferType()) {
		}

		return new ZFLoatCompositeContext(alpha);
	}
}
