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
import imagez.image.ZLayerRGBAFloat;
import imagez.io.OpenEXRInputStream.ChannelAttr;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.InflaterInputStream;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

class OpenEXRInputStream extends ImageInputStreamImpl {

	InputStream is;

	public OpenEXRInputStream(InputStream is) {
		this.is = is;
		this.setByteOrder(ByteOrder.LITTLE_ENDIAN);
	}

	@Override
	public int read() throws IOException {
		int v = is.read();

		this.bitOffset = 0;

		if (v != -1) {
			streamPos++;
		}

		return v;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int rlen = is.read(b, off, len);

		if (rlen > 0) {
			streamPos += rlen;
		}
		this.bitOffset = 0;

		return rlen;
	}

	public Rectangle readbox2i() throws IOException {
		Rectangle r = new Rectangle();

		r.x = readInt();
		r.y = readInt();
		r.width = readInt() - r.x + 1;
		r.height = readInt() - r.y + 1;
		return r;
	}

	public Rectangle2D.Float readbox2f() throws IOException {
		Rectangle2D.Float r = new Rectangle2D.Float();

		r.x = readFloat();
		r.y = readFloat();
		r.width = readFloat() - r.x + 1;
		r.height = readFloat() - r.y + 1;

		return r;
	}

	public String readStringZ() throws IOException {
		StringBuilder sb = new StringBuilder();
		int c;

		// if len > 32 ... error
		c = readByte() & 0xff;
		while (c != 0) {
			sb.append((char) c);
			c = readByte() & 0xff;
		}

		return sb.toString();
	}

	class ChannelAttr {

		String name;
		int pixType;
		byte linear;
		int xSampling;
		int ySampling;

		@Override
		public String toString() {
			return String.format("[name=%s, type=%d, linear=%d, xsamp=%d, ysamp=%d]",
					name, pixType, linear, xSampling, ySampling);
		}
	}

	public List<ChannelAttr> readChannels() throws IOException {
		ArrayList<ChannelAttr> chanList = new ArrayList<ChannelAttr>();

		StringBuilder name = new StringBuilder();
		int c;

		c = readByte() & 0xff;
		while (c != 0) {
			ChannelAttr attr = new ChannelAttr();
			name.setLength(0);
			do {
				name.append((char) c);
				c = readByte() & 0xff;
			} while (c != 0);
			attr.name = name.toString();
			System.out.println(" channel name " + name);
			attr.pixType = readInt();
			System.out.println("  type = " + attr.pixType);
			attr.linear = readByte();
			System.out.println("  linear = " + attr.linear);
			readByte();
			readByte();
			readByte();
			attr.xSampling = readInt();
			System.out.println("  xSampling= " + attr.xSampling);
			attr.ySampling = readInt();
			System.out.println("  ySampling= " + attr.ySampling);
			chanList.add(attr);
			c = readByte() & 0xff;
		}

		return chanList;
	}
}

/**
 * OpenEXR file format reader/writer?  Yeah ... right.
 * @author notzed
 */
public class OpenEXR {

	final public static byte NO_COMPRESSION = 0;
	final static public byte RLE_COMPRESSION = 1;
	final static public byte ZIPS_COMPRESSION = 2;
	final static public byte ZIP_COMPRESSION = 3;
	final static public byte PIZ_COMPRESSION = 4;
	final static public byte PXR24_COMPRESSION = 5;
	final static public byte B44_COMPRESSION = 6;
	final static public byte B44A_COMPRESSION = 7;
	// Maps compressiion type to # of scanlines per block
	int[] compressionToSPB = new int[]{1, 1, 1, 16, 32, 16, 32, 32};

	public void readAttribute(FileImageInputStream fis, String name, String type, int length) {
	}

	public void readLayer(ZImage image, File file) throws IOException {
		OpenEXRInputStream fis = new OpenEXRInputStream(new FileInputStream(file));


		// high level layout
		// magic no
		// version
		// header
		// line offset table
		// scan line blocks

		Rectangle dataSize = null;
		int compression = NO_COMPRESSION;
		List<ChannelAttr> channels = null;

		int magic = fis.readInt();
		int version = fis.readInt();

		System.out.printf("Magic: %08x\n", magic);
		System.out.printf("Version: %02x flags %06x (%s)\n", version & 0xff, version >> 8, (version & 0x200) == 0 ? "scanlines" : "tiles");

		// read header
		// (attribute name
		// attribute type
		// attribute size
		// attribute value)*
		// 0x00
		int c = fis.readByte() & 0xff;
		StringBuilder attName = new StringBuilder();
		StringBuilder attType = new StringBuilder();
		byte[] blah = new byte[1024];
		while (c != 0) {
			attName.setLength(0);
			do {
				attName.append((char) c);
				c = fis.readByte();
			} while (c != 0);
			String atype = fis.readStringZ();
			String aname = attName.toString();

			int attSize = fis.readInt();
			//fis.skipBytes(attSize)

			System.out.println("header " + attName + " type '" + atype + "' size " + attSize);
			if (atype.equals("chlist")) {
				channels = fis.readChannels();

				//for (ChannelAttr ch : channels) {
				//	System.out.println("channel: " + ch.toString());
				//}
			} else if (atype.equals("compression")) {
				compression = fis.readByte() & 0xff;

				System.out.printf("compression = %d\n", compression);
			} else if (atype.equals("box2i")) {
				Rectangle box = fis.readbox2i();

				if (aname.equals("dataWindow")) {
					dataSize = box;
				}

				System.out.println(" box = " + box);
			} else if (atype.equals("preview")) {
				int width = fis.readInt();
				int height = fis.readInt();
				BufferedImage preview = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
				byte[] data = ((DataBufferByte) preview.getRaster().getDataBuffer()).getData();

				fis.read(data);

				JFrame frame = new JFrame();
				JLabel label = new JLabel(new ImageIcon(preview));

				frame.add(label);
				frame.pack();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setTitle("OpenEXR preview");
				frame.setVisible(true);
			} else {
				while (attSize > 0) {
					int read = Math.min(attSize, 1024);
					attSize -= fis.read(blah, 0, read);
				}
			}
			c = fis.readByte() & 0xff;
		}

		// next: line offset table

		// a bit silly, need to know the compression to know how many scanlines/block
		int scanPerBlock = compressionToSPB[compression];
		int offsetCount = (dataSize.height + (scanPerBlock - 1)) / scanPerBlock;

		System.out.printf("Have %d scanline blocks\n", offsetCount);

		System.out.printf("skiping offsets ...\n");
		for (int i = 0; i < offsetCount; i++) {
			long offset = fis.readLong();
			// do nothing with them since we're loading the whole file
		}

		// find out how big each scanline is (for each channel?)
		int channelCount = channels.size();
		int[] channelOffset = new int[channelCount];
		int[] channelStride = new int[channelCount];
		int scanlineSize = 0;

		for (int i = 0; i < channelCount; i++) {
			ChannelAttr ch = channels.get(i);
			int perPixel = ch.pixType == 1 ? 2 : 4;
			int stride = dataSize.width * perPixel;

			channelStride[i] = stride;
			channelOffset[i] = scanlineSize;
			scanlineSize += stride;
		}


		//RGBALayer layer = new RGBALayer(image, dataSize);
		ZLayerRGBAFloat layer = (ZLayerRGBAFloat) image.getLayer(0);
		float[] dst = null; // FIXME: write rows at a time   layer.getData();
		int dstride = layer.getBounds().width * 4;

		System.out.printf("scan line blocks");
		for (int i = 0; i < offsetCount; i++) {
			//for (int i = 0; i < 0; i++) {
			int ycoord = fis.readInt();
			int pixelSize = fis.readInt();

			System.out.printf("%d: block coord %d size %d\n", i, ycoord, pixelSize);
			// TODO: is there a way to 'sub-stream' this so it just reads from the raw stream
			//does it matter ... probably not
			byte[] packed = new byte[pixelSize];
			fis.read(packed);

			InputStream is = new ByteArrayInputStream(packed);
			InflaterInputStream zip = new InflaterInputStream(is);

			for (int j = 0; j < scanPerBlock; j++) {

				for (int k = 0; k < channelCount; k++) {
					ChannelAttr ch = channels.get(k);
					int offset;

					if (ch.name.equals("G")) {
						offset = 0;
						/*} else if (ch.name.equals("G")) {
						offset = 1;
						} else if (ch.name.equals("B")) {
						offset = 2;
						} else if (ch.name.equals("A")) {
						offset = 3;
						 */					} else {
						// unknown channel, ignore it
						int skip = channelStride[k];

						for (int x = 0; x < skip; x++) {
							zip.read();
						}
						continue;
					}
					int dstOffset = (ycoord + j) * dstride + offset;

					switch (ch.pixType) {
						case 0: // uint 32 bit - not used for raster data, since it isn't normalised?
							for (int x = 0; x < dataSize.width; x++) {
								int full = zip.read() | (zip.read() << 8) | (zip.read() << 16) | (zip.read() << 24);
								// noop
							}
							break;
						case 1: // half type
							for (int x = 0; x < dataSize.width; x++) {
								int half = (zip.read() << 8) | zip.read();

								// HACK: gotta work out how the data size relates to the y coordinate
								if (dstOffset + x * 4 > 0) {
									//float v = (half) / 65535.0f;
									float v = Half.toFloat((short) half);
									//System.out.printf("%f\n", v);
									dst[dstOffset + x * 4] = v;
									dst[dstOffset + x * 4 + 1] = v;
									dst[dstOffset + x * 4 + 2] = v;
									//dst[dstOffset + x * 4 + 3] = 1f;
								}
							}
							break;
						case 2: // float type
							for (int x = 0; x < dataSize.width; x++) {
								int full = zip.read() | (zip.read() << 8) | (zip.read() << 16) | (zip.read() << 24);

								dst[dstOffset + x * 4] = Float.intBitsToFloat(full);
							}
							break;
					}
				}

			}
			{
				int xx = 0;
				while (zip.read() != -1) {
					xx++;
				}
				System.out.printf("left in zip = %d", xx);

			}

		}
		//image.addLayer(layer);
		//image.addDamage(layer.bounds);
		//image.refresh(layer.bounds);
	}

	static public void main(String[] args) {


		//	if (true)
		//		return;

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				File f = new File("/home/notzed/Pictures/exr/Blobbies.exr");
				ZImage image = new ZImage(1040, 1040);
				// FIXME: add layer
				//File f = new File("/home/notzed/Pictures/exr/Tree.exr");
				//ZImage image = new ZImage(1214, 732);
				OpenEXR exr = new OpenEXR();

				/*
				float[] d = ((ZLayerRGBAFloat) image.getLayer(0)).getData();
				for (int i = 0; i < d.length; i += 4) {
					d[i + 3] = 1;
				}*/

				if (true) {
					try {
						exr.readLayer(image, f);
					} catch (Exception x) {
						System.out.println("ex " + x);
					}
				}
				JFrame frame = new JFrame();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				JLabel label = new JLabel(new ImageIcon(image.getLayer(0).getImage()));
				frame.setBackground(Color.black);
				frame.add(label);
				frame.pack();
				frame.setVisible(true);
				label.repaint();
			}
		});

	}
}
