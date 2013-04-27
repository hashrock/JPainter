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
package imagez.io;

import imagez.image.ZImage;
import imagez.image.ZLayer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Support for the OpenRaster format
 * 
 * TODO: move the saving stuff here too, currently it is in ImageSaveOpenRaster
 * 
 * @author notzed
 */
public class OpenRaster {

	static class Layer {

		@XmlAttribute
		public String name = "";
		@XmlAttribute
		public float opacity = 1;
		@XmlAttribute
		public int x;
		@XmlAttribute
		public int y;
		@XmlAttribute
		public String src;
		//
		transient ZLayer zlayer;

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

		@XmlElement(name = "layer")
		public LinkedList<Layer> layers = new LinkedList<Layer>();

		public Layer findLayerByPath(String path) {
			for (Layer l : layers) {
				if (l.src.equals(path)) {
					return l;
				}
			}
			return null;
		}
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
	JAXBContext jc;
	Marshaller ms;
	Unmarshaller um;

	public OpenRaster() {
		try {
			jc = JAXBContext.newInstance(Image.class);
			ms = jc.createMarshaller();
			um = jc.createUnmarshaller();
		} catch (JAXBException ex) {
			Logger.getLogger(OpenRaster.class.getName()).log(Level.SEVERE, null, ex);
			throw new RuntimeException("Unable to initialise XML stuff");
		}
	}
	static OpenRaster ora;

	public static OpenRaster getInstance() {
		if (ora == null) {
			ora = new OpenRaster();
		}
		return ora;
	}

	public ZImage loadImage(File name) throws FileNotFoundException, IOException {

		ZipInputStream zis = new ZipInputStream(new FileInputStream(name));

		Image image = null;

		// first need to find the setup file
		ZipEntry entry;
		while ((entry = zis.getNextEntry()) != null) {
			System.out.println("scanning zip: " + entry.getName());
			if (entry.getName().equals("stack.xml")) {
				try {
					System.out.println("reading stack.xml");
					// this appears to close the stream also
					image = (Image) um.unmarshal(zis);
					break;
				} catch (JAXBException ex) {
					Logger.getLogger(OpenRaster.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			zis.closeEntry();
		}
		zis.close();

		if (image == null) {
			// FIXME: custom exception
			throw new UnsupportedEncodingException();
		}

		// now scan the image layers
		zis = new ZipInputStream(new FileInputStream(name));
		while ((entry = zis.getNextEntry()) != null) {
			// See if entry is a layer image - load it if so
			Layer l = image.stack.findLayerByPath(entry.getName());
			if (l != null) {
				l.zlayer = ImageLoader.loadBitmapLayer(zis);
				l.zlayer.setTitle(l.name);
				l.zlayer.setOpacity(l.opacity);
				l.zlayer.getBounds().setLocation(l.x, l.y);
				// FIXME: set layer offset
			}
			zis.closeEntry();
		}
		zis.close();

		// finally, build up the image
		ZImage zimage = new ZImage(image.w, image.h);
		//for (Layer l : image.stack.layers) {
		//	zimage.addLayer(l.zlayer);
		//}
		for (Iterator<Layer> it = image.stack.layers.descendingIterator();
				it.hasNext();) {
			Layer l = it.next();
			zimage.addLayer(l.zlayer);
		}

		return zimage;
	}
}
