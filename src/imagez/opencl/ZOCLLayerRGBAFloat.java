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
package imagez.opencl;

import imagez.image.*;
import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

/**
 * Hmmm, how can this be handled with opencl.
 *
 * Tricky.
 *
 * 1. should i/can i provide an image interface?  Perhaps not.
 *    it might be doable, but it's a pain, it would require
 *    perhaps a new samplemodel.
 * 2. get/set data interfaces will be slow (ish), but
 *    it's probably still useful to have them so cpu filters
 *    can still work.
 *    can they be cached transparently/easily?  -- gpu data
 *    will get stale.
 *
 * Layers need a damaged thing too, not just the image ...
 * that might make it possible to do.
 * @author notzed
 */
public class ZOCLLayerRGBAFloat extends ZOCLLayerRGBA {

	float data[];
	ComponentSampleModel csm;

	public ZOCLLayerRGBAFloat(ZImage zimage, Rectangle bounds) {
		super(zimage, bounds);

		data = new float[bounds.width * bounds.height * 4];

		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		ColorModel cm = new ComponentColorModel(cs, true, true, ColorModel.TRANSLUCENT, DataBuffer.TYPE_FLOAT);
		ComponentSampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_FLOAT, bounds.width, bounds.height, 4, bounds.width * 4, new int[]{0, 1, 2, 3});
		DataBufferFloat db = new DataBufferFloat(data, data.length);

		WritableRaster raster = Raster.createWritableRaster(sm, db, null);
		csm = sm;
		bimage = new BufferedImage(cm, raster, true, null);
	}

	@Override
	public int getStride() {
		return bounds.width*4;
	}

	public float[] getData() {
		return data;
	}

	@Override
	public void clear(Rectangle rect) {
	}

	@Override
	public void getLineRGBA(float[] dst, int doff, int x, int y, int width) {
	}

	@Override
	public void setLineRGBA(float[] src, int soff, int x, int y, int width) {
	}

	@Override
	public int getChannelCount() {
		return 4;
	}

	@Override
	public void getLine(float[][] dst, int doff, int x, int y, int width) {
	}

	@Override
	public void setLine(float[][] src, int soff, int x, int y, int width) {
	}
}

class CLDataBufferFloat extends DataBuffer {

	//CLImage<FloatBuffer> ...
	public CLDataBufferFloat() {
		super(1, 1);
	}

	
	@Override
	public int getElem(int bank, int i) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setElem(int bank, int i, int val) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
}

class Foo extends SampleModel {

	public Foo() {
		super(DataBuffer.TYPE_FLOAT, 1024, 768, 4);
	}

	@Override
	public int getNumDataElements() {
		return 4;
	}

	@Override
	public Object getDataElements(int x, int y, Object obj, DataBuffer data) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setDataElements(int x, int y, Object obj, DataBuffer data) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getSample(int x, int y, int b, DataBuffer data) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setSample(int x, int y, int b, int s, DataBuffer data) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public SampleModel createCompatibleSampleModel(int w, int h) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public SampleModel createSubsetSampleModel(int[] bands) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public DataBuffer createDataBuffer() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int[] getSampleSize() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getSampleSize(int band) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}