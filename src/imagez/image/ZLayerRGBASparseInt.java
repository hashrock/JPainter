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
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.WritableRaster;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 * A layer with a sparse storage using integers to store the values.
 * 
 * This isn't terribly efficient since it falls-back to the generic
 * unoptimised code-paths for java2d.  But it takes a lot less space
 * than the float one.
 *
 * @author notzed
 */
public class ZLayerRGBASparseInt extends ZLayerRGBA {

	RGBASparseIntSampleModel csm;
	RGBASparseIntDataBuffer db;

	public ZLayerRGBASparseInt(ZImage zimage, Rectangle bounds) {
		super(zimage, bounds);
		int size = bounds.width * bounds.height;

		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		ColorModel cm = new DirectColorModel(cs, 32, 0x00ff0000, 0x0000ff00, 0x000000ff, 0xff000000, BlendMode.premultiplyAlpha, DataBuffer.TYPE_INT);
		csm = new RGBASparseIntSampleModel(DataBuffer.TYPE_INT, bounds.width, bounds.height, bounds.width, new int[]{0x00ff0000, 0x0000ff00, 0x000000ff, 0xff000000});
		//ComponentSampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_FLOAT, bounds.width, bounds.height, 4, bounds.width * 4, new int[]{0, 1, 2, 3});
		db = new RGBASparseIntDataBuffer(size, bounds.width, bounds.height);

		WritableRaster raster = new SparseWritableRaster(csm, db, new Point());
		bimage = new BufferedImage(cm, raster, BlendMode.premultiplyAlpha, null);
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
	@Override
	public String toString() {
		int used = 0;
		for (int i = 0; i < db.tiles.length; i++) {
			if (db.tiles[i] != null) {
				used++;
			}
		}
		return "ZLayerRGBASParseInt[" + db.width + "," + db.height + ", tiles=" + db.tiles.length + ", active=" + used + "]";
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				ZLayerRGBASparseInt img = new ZLayerRGBASparseInt(null, new Rectangle(1024, 768));

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
