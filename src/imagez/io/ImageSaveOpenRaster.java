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
import imagez.image.ZLayerRGBAByte;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Open Raster save format
 * @author notzed
 */
public class ImageSaveOpenRaster extends ImageSaveOptions {

	final static Integer[] depths = new Integer[]{16, 8, 5 /*, 4*/};
	JComboBox depth;
	JCheckBox savethumb;
	JCheckBox savepreview;

	public ImageSaveOpenRaster(File file, ZImage image) {
		super(file, image, true, true, false);

		//FIXME: scan layers to find minimum layer required
		ZLayer[] layers = image.getVisibleLayers();
		int defaultDepth = 8;
		for (int i = 0; i < layers.length; i++) {
			// get what info from layer ???
		}

		//mergeAlpha.setSelected(true);

		Dimension d;

		options.setLayout(new GridBagLayout());
		options.setBorder(new TitledBorder("OpenRaster Options"));

		GridBagConstraints c0 = new GridBagConstraints();
		c0.gridx = 0;
		c0.ipadx = 4;
		c0.ipady = 2;
		c0.anchor = GridBagConstraints.WEST;
		GridBagConstraints c1 = (GridBagConstraints) c0.clone();
		c1.weightx = 1;
		c1.gridx = 1;
		c1.fill = GridBagConstraints.HORIZONTAL;

		GridBagConstraints c01 = (GridBagConstraints) c0.clone();
		c01.gridwidth = 2;

		savethumb = new JCheckBox("Save Thumbnail", true);
		options.add(savethumb, c0);
		savepreview = new JCheckBox("Save Preview");
		options.add(savepreview, c0);

		// dummy for alignment
		options.add(new JLabel(""), c1);

		d = options.getPreferredSize();
		d.width = 10000;
		options.setMaximumSize(d);

		setAbles();
	}

	@Override
	public String getTitle() {
		return "ORA";
	}

	@Override
	public String getFormat() {
		return "png";
	}

	static class Layer {

		@XmlAttribute
		public String name;
		@XmlAttribute
		public float opacity;
		@XmlAttribute
		public int x;
		@XmlAttribute
		public int y;
		@XmlAttribute
		public String src;

		public Layer() {
		}

		public Layer(String name, float opacity, int x, int y, String src) {
			this.name = name;
			this.opacity = opacity;
			this.x = x;
			this.y = y;
			this.src = src;
		}
	}

	static class Stack {

		@XmlElement(name="layer")
		public List<Layer> layers = new LinkedList<Layer>();
	}

	@XmlRootElement(name = "image")
	static class Image {

		@XmlElement
		public Stack stack;
		@XmlAttribute
		public int w;
		@XmlAttribute
		public int h;
	}

	static void loadXML() {
		try {
			JAXBContext jc = JAXBContext.newInstance(Image.class);

			Image image = new Image();
			image.stack = new Stack();
			image.stack.layers.add(new Layer("layer name", 0.5f, 10, 10, "data/foo.png"));

			Marshaller ms = jc.createMarshaller();
			ms.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			ms.marshal(image, System.out);

			Unmarshaller um = jc.createUnmarshaller();
			Image im = (Image) um.unmarshal(new File("/home/notzed/bob/stack.xml"));

			System.out.println("image size " + im.w + "," + im.h);
			System.out.println("loaded layers:");
			for (Layer l : im.stack.layers) {
				System.out.println(l.name);
			}


		} catch (JAXBException ex) {
			Logger.getLogger(ImageSaveOpenRaster.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void main(String[] args) {
		loadXML();
	}

	public void saveLayer(ZipOutputStream zos, Document doc, Element stack, ZLayer l, int index) throws IOException {
		// FIXME: if float layer, need to convert to 16 bit first ...
		// Or rather, save it in openexr, should that ever get written
		
		ZipEntry entry = new ZipEntry(String.format("data/layer%03d.png", index));

		// should store it i guess ... if doing this need to manually set size/crc/method
		// see: http://blogs.oracle.com/CoreJavaTechTips/entry/creating_zip_and_jar_files
		//entry.setMethod(ZipEntry.STORED);

		zos.putNextEntry(entry);
		ImageIO.write(l.getImage(), "PNG", zos);
		zos.closeEntry();

		// FIXME: use the OpenRaster / JAXB stuff for the XML encodings?

		Element xlayer = doc.createElement("layer");
		xlayer.setAttribute("opacity", Float.toString(l.getOpacity()));
		xlayer.setAttribute("name", l.getTitle());
		xlayer.setAttribute("x", Integer.toString(l.getBounds().x));
		xlayer.setAttribute("y", Integer.toString(l.getBounds().y));
		xlayer.setAttribute("src", entry.getName());

		// FIXME: xlayer.setAttribute("composite-op", ...);

		stack.appendChild(xlayer);

	}

	@Override
	public void save() throws IOException {
		try {
			int count = img.getLayerCount();

			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = db.newDocument();
			Element ximage = doc.createElement("image");
			Element xstack = doc.createElement("stack");
			ZipEntry entry;

			ximage.setAttribute("w", Integer.toString(img.getDimension().width));
			ximage.setAttribute("h", Integer.toString(img.getDimension().height));

			doc.appendChild(ximage);
			ximage.appendChild(xstack);

			FileOutputStream fos = new FileOutputStream(file);
			ZipOutputStream zos = new ZipOutputStream(fos);

			// Layers are inverted in imagez (for now)
			for (int i = count - 1; i >= 0; i--) {
				ZLayer l = img.getLayerAt(i);

				saveLayer(zos, doc, xstack, l, count - 1 - i);
			}

			// output mime/type file
			entry = new ZipEntry("mimetype");
			zos.putNextEntry(entry);
			zos.write("image/openraster".getBytes("US-ASCII"));
			zos.closeEntry();

			boolean preview = savepreview.isSelected();
			boolean thumb = savethumb.isSelected();
			ZLayer previewLayer = null;

			if (preview | thumb) {
				previewLayer = new ZLayerRGBAByte(null, new Rectangle(img.getDimension()));
				saveToLayer(previewLayer);
			}

			// full-sized image preview
			if (preview) {
				entry = new ZipEntry("Thumbnails/preview.png");
				zos.putNextEntry(entry);
				ImageIO.write(previewLayer.getImage(), "PNG", zos);
				zos.closeEntry();
			}
			// thumbnail preview
			if (thumb) {
				Dimension imgd = img.getDimension();
				int twidth = 128;
				int theight = 128;

				if (imgd.height > imgd.width) {
					twidth = twidth * imgd.width / imgd.height;
				} else {
					theight = theight * imgd.height / imgd.width;
				}

				BufferedImage bi = new BufferedImage(twidth, theight, BufferedImage.TYPE_4BYTE_ABGR);
				Graphics2D gg = bi.createGraphics();
				gg.drawImage(previewLayer.getImage(), 0, 0, twidth, theight, null);
				gg.dispose();

				entry = new ZipEntry("Thumbnails/thumbnail.png");
				zos.putNextEntry(entry);
				ImageIO.write(bi, "PNG", zos);
				zos.closeEntry();
			}

			// output stack.xml
			entry = new ZipEntry("stack.xml");
			zos.putNextEntry(entry);

			TransformerFactory tfac = TransformerFactory.newInstance();
			Transformer mtmti = tfac.newTransformer();
			mtmti.setOutputProperty(OutputKeys.INDENT, "yes");
			StreamResult result = new StreamResult(zos);

			mtmti.transform(new DOMSource(doc), result);
			zos.closeEntry();

			zos.close();
		} catch (TransformerConfigurationException ex) {
			Logger.getLogger(ImageSaveOpenRaster.class.getName()).log(Level.SEVERE, null, ex);
		} catch (TransformerException ex) {
			Logger.getLogger(ImageSaveOpenRaster.class.getName()).log(Level.SEVERE, null, ex);
		} catch (ParserConfigurationException ex) {
			Logger.getLogger(ImageSaveOpenRaster.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
