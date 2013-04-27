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
package imagez.io;

import imagez.image.ZImage;
import imagez.image.ZLayer;
import imagez.image.ZLayerMonoAByte;
import imagez.image.ZLayerMonoAFloat;
import imagez.image.ZLayerMonoByte;
import imagez.image.ZLayerMonoFloat;
import imagez.image.ZLayerMonoShort;
import imagez.image.ZLayerRGBAByte;
import imagez.image.ZLayerRGBAFloat;
import imagez.image.ZLayerRGBAInt;
import imagez.image.ZLayerRGBAShort16;
import imagez.image.ZLayerRGBInt;
import imagez.image.ZLayerRGBShort16;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author notzed
 */
public class ImageLoader {

	int a = BufferedImage.TYPE_3BYTE_BGR;
	static String[] bitypes = new String[]{
		"TYPE_CUSTOM",
		"TYPE_INT_RGB",
		"TYPE_INT_ARGB",
		"TYPE_INT_ARGB_PRE",
		"TYPE_INT_BGR",
		"TYPE_3BYTE_BGR",
		"TYPE_4BYTE_ABGR",
		"TYPE_4BYTE_ABGR_PRE",
		"TYPE_USHORT_565_RGB",
		"TYPE_USHORT_555_RGB",
		"TYPE_BYTE_GRAY",
		"TYPE_USHORT_GRAY",
		"TYPE_BYTE_BINARY",
		"TYPE_BYTE_INDEXED"
	};

	static void showshit(BufferedImage img, String title) {
		JFrame f = new JFrame(title);

		f.setDefaultCloseOperation(f.EXIT_ON_CLOSE);
		f.add(new JLabel(new ImageIcon(img)));
		f.pack();
		f.setVisible(true);
	}

	/**
	 * Tries to load a bitmap directly in to the right type of layer.
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */
	static public ZLayer loadBitmapLayer(Object file) throws IOException {
		ImageInputStream iis = ImageIO.createImageInputStream(file);
		Iterator<ImageReader> itr = ImageIO.getImageReaders(iis);
		ImageReader ir = itr.next();

		ir.setInput(iis);

		int icount = ir.getNumImages(true);

		int imageid = 0;
		System.out.printf("Image file contains %d images\n", icount);

		Iterator<ImageTypeSpecifier> iits = ir.getImageTypes(imageid);
		if (!iits.hasNext()) {
			throw new UnsupportedEncodingException();
		}

		ImageTypeSpecifier its = iits.next();
		System.out.printf("%2d: image type possible = %s bitype = %s\n", 0, its, bitypes[its.getBufferedImageType()]);
		System.out.printf("   sample model: %s\n", its.getSampleModel());
		System.out.printf("   colour model: %s\n", its.getColorModel());
		System.out.printf("   number of components: %d\n", its.getNumComponents());

		int components = its.getNumComponents();
		int type = its.getSampleModel().getTransferType();

		its.getColorModel().getTransparency();
		
		ImageReadParam ip = ir.getDefaultReadParam();

		int width = ir.getWidth(imageid);
		int height = ir.getHeight(imageid);
		ZLayer layer = null;
		
		switch (components) {
			case 1:
				switch (type) {
					case DataBuffer.TYPE_FLOAT:
						layer = new ZLayerMonoFloat(null, new Rectangle(width, height));
						break;
					case DataBuffer.TYPE_BYTE:
					default:
						layer = new ZLayerMonoByte(null, new Rectangle(width, height));
						break;
					case DataBuffer.TYPE_SHORT:
					case DataBuffer.TYPE_USHORT:
						layer = new ZLayerMonoShort(null, new Rectangle(width, height));
						break;
				}
				break;
			case 2:
				switch (type) {
					case DataBuffer.TYPE_FLOAT:
						layer = new ZLayerMonoAFloat(null, new Rectangle(width, height));
						layer = null;
						break;
					case DataBuffer.TYPE_SHORT:
					case DataBuffer.TYPE_USHORT:
						//layer = new ZLayerRGBShort16(null, new Rectangle(width, height));
						layer = null;
						break;
					case DataBuffer.TYPE_BYTE:
					default:
						layer = new ZLayerMonoAByte(null, new Rectangle(width, height));
						break;

				}
				break;
			case 3:
			default:
				switch (type) {
					case DataBuffer.TYPE_FLOAT:
						//layer = new ZLayerRGBFloat(null, new Rectangle(width, height));
						layer = null;
						break;
					case DataBuffer.TYPE_SHORT:
					case DataBuffer.TYPE_USHORT:
						layer = new ZLayerRGBShort16(null, new Rectangle(width, height));
						break;
					case DataBuffer.TYPE_INT:
					default:
						layer = new ZLayerRGBInt(null, new Rectangle(width, height));
						break;

				}
				break;
			case 4:
				switch (type) {
					case DataBuffer.TYPE_FLOAT:
						layer = new ZLayerRGBAFloat(null, new Rectangle(width, height));
						layer = null;
						break;
					case DataBuffer.TYPE_SHORT:
					case DataBuffer.TYPE_USHORT:
						layer = new ZLayerRGBAShort16(null, new Rectangle(width, height));
						break;
					//case DataBuffer.TYPE_BYTE:
					//	layer = new ZLayerRGBAByte(null, new Rectangle(width, height));
					//	break;
					case DataBuffer.TYPE_INT:
					default:
						layer = new ZLayerRGBAInt(null, new Rectangle(width, height));
						break;
				}
				break;
		}

		ip.setDestination(layer.getImage());
		ir.read(0, ip);

		System.out.println("Image loaded into " + layer);

		return layer;
	}

	/**
	 * Try to load an image.
	 * 
	 * Will try OpenRaster format if the file ends in .ora (and/or some other test?)
	 * and then fall-back to ImageIO for single-layer image.
	 * 
	 * @param file
	 * @return
	 * @throws IOException 
	 */
	static public ZImage loadImage(File file) throws IOException {
		String name = file.getName().toLowerCase();

		if (name.endsWith(".ora")) {
			try {
				return OpenRaster.getInstance().loadImage(file);
			} catch (UnsupportedEncodingException x) {
				// it's not an ora file after-all, fall-back to something else
			}
		}

		// if file is a bitmap file ... 
		ZLayer layer = loadBitmapLayer(file);
		Rectangle bounds = layer.getBounds();
		ZImage img = new ZImage(bounds.width, bounds.height);

		img.addLayer(layer);

		return img;
	}

	public static void main(String[] args) throws IOException {
		ImageLoader il = new ImageLoader();
		il.loadBitmapLayer(new File("/home/notzed/cat0.jpg"));
		il.loadBitmapLayer(new File("/home/notzed/aa.png"));
		il.loadBitmapLayer(new File("/home/notzed/aa16.png"));
		il.loadBitmapLayer(new File("/home/notzed/fred-400-16.png"));

		//BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		//System.out.println("bi non pre: " + bitypes[bi.getType()]);
		//bi.coerceData(true);
		//System.out.println("bi pre: " + bitypes[bi.getType()]);
	}
}
