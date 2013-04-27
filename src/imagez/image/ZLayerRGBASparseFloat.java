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
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

class SparseWritableRaster extends WritableRaster {

	public SparseWritableRaster(SampleModel sm, Point origin) {
		super(sm, origin);
	}

	public SparseWritableRaster(SampleModel sm, DataBuffer db, Point origin) {
		super(sm, db, origin);
	}

	@Override
	public WritableRaster createCompatibleWritableRaster() {
		System.out.println("create compatible writable raster not going to work");
		return super.createCompatibleWritableRaster();
	}

	@Override
	public void setPixel(int x, int y, int[] iArray) {
		//System.out.printf("writable raster set pixel %d,%d,%d", iArray[0], iArray[1], iArray[2]);
		super.setPixel(x, y, iArray);
	}
}

/**
 * A layer with a sparse storage.
 * @author notzed
 */
public class ZLayerRGBASparseFloat extends ZLayerRGBA {

	ComponentSampleModel csm;
	RGBASparseFloatDataBuffer db;

	public ZLayerRGBASparseFloat(ZImage zimage, Rectangle bounds) {
		super(zimage, bounds);
		int size = bounds.width * bounds.height * 4;

		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		ColorModel cm = new ComponentColorModel(cs, true, BlendMode.premultiplyAlpha, ColorModel.TRANSLUCENT, DataBuffer.TYPE_FLOAT);
		ComponentSampleModel sm = new RGBASparseFloatSampleModel(DataBuffer.TYPE_FLOAT, bounds.width, bounds.height, 4, bounds.width * 4, new int[]{0, 1, 2, 3});
		//ComponentSampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_FLOAT, bounds.width, bounds.height, 4, bounds.width * 4, new int[]{0, 1, 2, 3});
		db = new RGBASparseFloatDataBuffer(size, bounds.width, bounds.height);

		//WritableRaster raster = Raster.createWritableRaster(sm, db, null);
		WritableRaster raster = new SparseWritableRaster(sm, db, new Point());
		csm = sm;
		bimage = new BufferedImage(cm, raster, BlendMode.premultiplyAlpha, null);

		//int[]s = sm.getSampleSize();
		//for (int i=0;i<s.length;i++) {
		//	System.out.printf("sample size %d = %d\n", i, s[i]);
		//}
	}

	@Override
	public int getStride() {
		return bounds.width * 4;
	}

	@Override
	public void clear(Rectangle rect) {
		// FIXME: only clear rect!
		db.clear();
		addDamage(rect);
	}

	@Override
	public void getLineRGBA(float[] dst, int doff, int x, int y, int width) {
		x -= bounds.x;
		y -= bounds.y;
		db.getLineRGBA(dst, doff, x, y, width);
	}

	@Override
	public void setLineRGBA(float[] src, int soff, int x, int y, int width) {
		x -= bounds.x;
		y -= bounds.y;
		db.setLineRGBA(src, soff, x, y, width);
	}

	@Override
	public int getChannelCount() {
		return 4;
	}

	@Override
	public void getLine(float[][] dst, int doff, int x, int y, int width) {
		x -= bounds.x;
		y -= bounds.y;
		db.getLineRGBA(dst, doff, x, y, width);
	}

	@Override
	public void setLine(float[][] src, int soff, int x, int y, int width) {
		x -= bounds.x;
		y -= bounds.y;
		db.setLineRGBA(src, soff, x, y, width);
	}

	/*
	public void compress() {
	// should it just be serialisable?

	long now = System.currentTimeMillis();

	Deflater df = new Deflater(Deflater.DEFAULT_COMPRESSION);
	ByteArrayOutputStream os = new ByteArrayOutputStream();
	DeflaterOutputStream dos = new DeflaterOutputStream(os, df);

	ByteBuffer bb = ByteBuffer.allocate(64 * 64 * 4 * 4);
	FloatBuffer fb = bb.asFloatBuffer();
	try {

	// TODO: write header?
	int raw = 0;
	for (int i = 0; i < db.tiles.length; i++) {
	RGBASparseFloatDataBuffer.TileRGBAFloat tf = db.tiles[i];

	if (tf != null) {
	// TODO: write position
	fb.position(0);
	fb.put(tf.data);
	bb.position(0);
	dos.write(bb.array());
	raw += 64 * 64 * 4 * 4;
	}
	}

	dos.close();
	byte [] compressed = os.toByteArray();

	now = System.currentTimeMillis() - now;

	System.out.printf("compression took %03d.%03ds from %d to %d bytes %f%%\n", now/1000, now % 1000, raw, compressed.length, (float)compressed.length / raw);
	} catch (IOException ex) {
	Logger.getLogger(ZLayerRGBASparseFloat.class.getName()).log(Level.SEVERE, null, ex);
	}
	}
	 */
	@Override
	public String toString() {
		int used = 0;
		for (int i = 0; i < db.tiles.length; i++) {
			if (db.tiles[i] != null) {
				used++;
			}
		}
		return "ZLayerRGBASParseFloat[" + db.width + "," + db.height + ", tiles=" + db.tiles.length + ", active=" + used + "]";
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				ZLayerRGBASparseFloat img = new ZLayerRGBASparseFloat(null, new Rectangle(1024, 768));

				Graphics2D gg = img.getImage().createGraphics();

				gg.setColor(Color.red);
				gg.drawLine(0, 0, 1024, 768);

				gg.dispose();

				JFrame f = new JFrame("sparse image test");

				ImageIcon ic = new ImageIcon(img.getImage());

				f.add(new JLabel(ic));
				f.pack();
				f.setVisible(true);
			}
		});
	}
}
