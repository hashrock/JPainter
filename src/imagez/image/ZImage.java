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

import imagez.undo.ZUndoManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;

/**
 * An zimage is a collection of layers, backed by a buffered zimage.
 * @author notzed
 */
public class ZImage implements PropertyChangeListener {

	boolean usefloat = false;
	Dimension dimension;
	// 'native format' image
	BufferedImage image;
	ZSelectionModel selectionModel;
	//
	private ArrayList<ZLayer> layers = new ArrayList<ZLayer>();
	LinkedList<ZImageListener> listeners = new LinkedList<ZImageListener>();
	//
	boolean undoEnabled = true;
	imagez.undo.ZUndoManager undoManager;

	/**
	 * Create a new empty image with a backing composition image.
	 * @param w
	 * @param h
	 */
	public ZImage(int w, int h) {
		this.dimension = new Dimension(w, h);
		image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

		this.damaged = new Rectangle(dimension);
		this.selectionModel = new ZSelectionModel(this);

		undoManager = new ZUndoManager();
	}

	/*
	public ZImage(File name) throws IOException {
	ImageLoader il = new ImageLoader();
	
	BufferedImage bi = ImageIO.read(name);
	
	if (bi == null) {
	throw new UnsupportedEncodingException();
	}
	
	System.out.println("Loaded buffered image type = " + bi.getType());
	
	int w = bi.getWidth();
	int h = bi.getHeight();
	this.dimension = new Dimension(w, h);
	
	this.image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
	
	// create single layer for this zimage
	ZLayer layer = layerFromImage(bi);
	layer.setTitle("background");
	addLayer(layer);
	
	this.damaged = new Rectangle(dimension);
	this.selectionModel = new ZSelectionModel(this);
	
	undoManager = new ZUndoManager();
	}
	 *
	 */
	public boolean isUndoEnabled() {
		return undoEnabled;
	}

	public void setUndoEnabled(boolean undoEnabled) {
		this.undoEnabled = undoEnabled;
	}

	public ZSelectionModel getSelectionModel() {
		return selectionModel;
	}

	public void addImageListener(ZImageListener l) {
		listeners.add(l);
	}

	public void removeImageListener(ZImageListener l) {
		listeners.remove(l);
	}

	protected void fireImageChanged(Rectangle damaged) {
		for (ZImageListener l : listeners) {
			l.imageChanged(damaged);
		}
	}

	protected void fireImageChanged() {
		for (ZImageListener l : listeners) {
			l.imageChanged();
		}
	}

	protected void fireLayerAdded(int index, ZLayer layer) {
		for (ZImageListener l : listeners) {
			l.layerAdded(index, layer);
		}
	}

	protected void fireLayerRemoved(int index, ZLayer layer) {
		for (ZImageListener l : listeners) {
			l.layerRemoved(index, layer);
		}
	}

	protected void fireLayerChanged(int index, ZLayer olayer, ZLayer nlayer) {
		for (ZImageListener l : listeners) {
			l.layerChanged(index, olayer, nlayer);
		}
	}

	protected void fireLayerPropertyChanged(PropertyChangeEvent evt) {
		for (ZImageListener l : listeners) {
			l.layerPropertyChanged(evt);
		}
	}

	public ZLayer createLayer() {
		ZLayer layer = new ZLayerRGBAFloat(this, new Rectangle(dimension));
		//ZLayer layer = new ZLayerRGBAInt(this, new Rectangle(dimension));

		layer.setTitle("Layer 0");

		return layer;
	}

	public int addLayer(ZLayer layer) {
		int index = layers.size();

		layers.add(layer);
		layer.zimage = this;
		layer.addPropertyChangeListener(this);
		addDamage(layer.getBounds());
		fireLayerAdded(index, layer);
		
		return index;
	}

	public void addLayer(int index, ZLayer layer) {
		layers.add(index, layer);
		layer.zimage = this;
		layer.addPropertyChangeListener(this);
		addDamage(layer.getBounds());
		fireLayerAdded(index, layer);
	}

	public void removeLayer(ZLayer layer) {
		int index = layers.indexOf(layer);

		assert index != -1;

		layers.remove(layer);
		layer.removePropertyChangeListener(this);
		addDamage(layer.getBounds());
		fireLayerRemoved(index, layer);
	}

	public void moveLayer(int oindex, int nindex) {
		ZLayer olayer = layers.get(oindex);
		ZLayer nlayer = layers.get(nindex);

		layers.set(oindex, nlayer);
		layers.set(nindex, olayer);

		addDamage(olayer.getBounds().union(nlayer.getBounds()));

		fireLayerChanged(oindex, olayer, nlayer);
		fireLayerChanged(nindex, nlayer, olayer);
	}

	public void replaceLayer(ZLayer olayer, ZLayer nlayer) {
		int i = indexOf(olayer);

		assert (i >= 0);

		layers.set(i, nlayer);

		nlayer.addPropertyChangeListener(this);
		olayer.removePropertyChangeListener(this);

		addDamage(olayer.getBounds().union(nlayer.getBounds()));
		fireLayerChanged(i, olayer, nlayer);
	}

	public int getLayerCount() {
		return layers.size();
	}

	public ZLayer getLayerAt(int index) {
		return layers.get(index);
	}

	public int indexOf(ZLayer layer) {
		return layers.indexOf(layer);
	}

	/**
	 * Proxy layer events
	 * @param evt
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		//System.out.println("got layer change " + evt.getPropertyName());
		fireLayerPropertyChanged(evt);

		if (evt.getPropertyName().equals(ZLayer.PROP_MODE)) {
			addDamage(new Rectangle(dimension));
		} else if (evt.getPropertyName().equals(ZLayer.PROP_OPACITY)) {
			addDamage(new Rectangle(dimension));
		} else if (evt.getPropertyName().equals(ZLayer.PROP_VISIBLE)) {
			addDamage(new Rectangle(dimension));
		} else if (evt.getPropertyName().equals(ZLayer.PROP_DAMAGE)) {
			Rectangle rect = (Rectangle) evt.getNewValue();

			if (rect != null) {
				addDamage(rect);
			}
		}
		// TODO: what else?  put damaged bounds in property too
	}

	ZLayer layerFromImage(BufferedImage bi) {
		long now = System.currentTimeMillis();

		if (false) {
			ZLayerRGBAInt rgba = new ZLayerRGBAInt(this, new Rectangle(0, 0, bi.getWidth(), bi.getHeight()));
			BufferedImage target = rgba.getImage();
			Graphics2D gg = (Graphics2D) target.getGraphics();

			gg.drawImage(bi, 0, 0, null);
			gg.dispose();
			return rgba;
		}

		ZLayerRGBAFloat rgba = new ZLayerRGBAFloat(this, new Rectangle(0, 0, bi.getWidth(), bi.getHeight()));

		if (usefloat) {
			// This is 'good on paper' but runs like a pig dog. 10x slower at least.
			ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
			ColorModel cm = new ComponentColorModel(cs, true, true, ColorModel.TRANSLUCENT, DataBuffer.TYPE_FLOAT);
			SampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_FLOAT, bi.getWidth(), bi.getHeight(), 4, bi.getWidth() * 4, new int[]{0, 1, 2, 3});
			DataBufferFloat db = new DataBufferFloat(rgba.data, rgba.data.length);
			WritableRaster wr = Raster.createWritableRaster(sm, db, null);
			BufferedImage bb = new BufferedImage(cm, wr, true, null);

			Graphics2D gg = (Graphics2D) bb.getGraphics();

			Graphics2D g2 = (Graphics2D) bi.getGraphics();

			gg.drawImage(bi, 0, 0, null);

			gg.setPaint(Color.RED);
			gg.setStroke(new BasicStroke(5));
			gg.drawLine(0, 0, 100, 100);

			now = System.currentTimeMillis() - now;
			System.out.printf("took %03d.%03ds\n", now / 1000, now % 1000);

			if (true) {
				return rgba;
			}
		}

		// These are about 10x faster than going through the unoptimised
		// java2d float path.
		switch (bi.getType()) {
			case BufferedImage.TYPE_INT_ARGB: {
				int[] bitmap = ((DataBufferInt) bi.getRaster().getDataBuffer()).getData();

				System.out.println("Lloading byte type argb, could optimise");

				// FIXME: premultiply alpha
				int len = Math.min(bitmap.length, rgba.data.length / 4);
				float[] data = rgba.data;
				int dp = 0;
				float scale = 1.0f / 255.0f;
				for (int i = 0; i < len; i++) {
					int p = bitmap[i];
					float a = ((p >> 24) & 0xff) * scale;
					float r = ((p >> 16) & 0xff) * scale;
					float g = ((p >> 8) & 0xff) * scale;
					float b = ((p >> 0) & 0xff) * scale;
					data[dp++] = r;
					data[dp++] = g;
					data[dp++] = b;
					data[dp++] = a;
				}
				break;
			}
			case BufferedImage.TYPE_4BYTE_ABGR: {
				byte[] bitmap = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();

				System.out.println("4byte abgr");

				int len = Math.min(bitmap.length / 3, rgba.data.length / 4);
				float[] data = rgba.data;
				int dp = 0;
				int bp = 0;
				float scale = 1.0f / 255.0f;
				for (int i = 0; i < len; i++) {
					int A = bitmap[bp++];
					int B = bitmap[bp++];
					int G = bitmap[bp++];
					int R = bitmap[bp++];
					float a = (A & 0xff) * scale;
					float r = (R & 0xff) * scale;
					float g = (G & 0xff) * scale;
					float b = (B & 0xff) * scale;

					// FIXME: pre-multiply alpha!
					data[dp++] = r;
					data[dp++] = g;
					data[dp++] = b;
					data[dp++] = a;
				}
				break;
			}
			case BufferedImage.TYPE_3BYTE_BGR: {
				byte[] bitmap = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();

				int len = Math.min(bitmap.length / 3, rgba.data.length / 4);
				float[] data = rgba.data;
				int dp = 0;
				int bp = 0;
				float scale = 1.0f / 255.0f;
				for (int i = 0; i < len; i++) {
					int B = bitmap[bp++];
					int G = bitmap[bp++];
					int R = bitmap[bp++];
					float r = (R & 0xff) * scale;
					float g = (G & 0xff) * scale;
					float b = (B & 0xff) * scale;

					data[dp++] = r;
					data[dp++] = g;
					data[dp++] = b;
					data[dp++] = 1.0f;
				}
				break;
			}
			case BufferedImage.TYPE_BYTE_GRAY: {
				byte[] bitmap = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();

				System.out.println("Lloading byte type gray, could optimise");

				int len = Math.min(bitmap.length, rgba.data.length / 4);
				float[] data = rgba.data;
				int dp = 0;
				float scale = 1.0f / 255.0f;
				for (int i = 0; i < len; i++) {
					int p = bitmap[i];
					float v = (p & 0xff) * scale;

					data[dp++] = v;
					data[dp++] = v;
					data[dp++] = v;
					data[dp++] = 1.0f;
				}
				break;
			}
			default:
				System.out.println("fallback loader");
				// fall-back to java2d for the rest
				BufferedImage target = rgba.getImage();
				Graphics2D gg = (Graphics2D) target.getGraphics();

				gg.drawImage(bi, 0, 0, null);
				gg.dispose();
				break;
		}

		now = System.currentTimeMillis() - now;
		System.out.printf("load image took %03d.%03ds\n", now / 1000, now % 1000);

		return rgba;
	}

	public BufferedImage getImage() {
		return image;
	}

	public Dimension getDimension() {
		return dimension;
	}

	public ZLayer getLayer(int id) {
		return layers.get(id);
	}

	public ZLayer[] getVisibleLayers() {
		int vcount = 0;
		int len = layers.size();
		for (int i = 0; i < len; i++) {
			ZLayer l = layers.get(i);
			if (l.isVisible()) {
				vcount++;
			}
		}

		ZLayer[] res = new ZLayer[vcount];
		vcount = 0;
		for (int i = 0; i < len; i++) {
			ZLayer l = layers.get(i);
			if (l.isVisible()) {
				res[vcount++] = l;
			}
		}
		return res;
	}

	public ZLayerListModel getLayerModel() {
//		return layers;
		return null;
	}
	ZImageCompositor compositor;

	void composeLayers(Rectangle bounds, ZLayer[] layers, ZLayerMono mask, BufferedImage result) {
		if (compositor == null) {
			compositor = new ZImageCompositor();
		}

		//System.out.println("composing " + bounds);
		long now = System.currentTimeMillis();

		compositor.composeMT(bounds, layers, mask, result);
		//compositor.compose(bounds, layers, mask, result);

		if (bounds.width == dimension.width & bounds.height == dimension.height) {
			now = System.currentTimeMillis() - now;
			System.out.printf("Full refresh of %d layers took %03d.%03ds\n", layers.length, now / 1000, now % 1000);
		}
	}

	public void refresh(Rectangle rect) {
		// re-form backing zimage over damaged rectangle.

		// TODO: only refresh area being asked for?

		if (!threadrefresh) {
			if (damaged == null || damaged.isEmpty()) {
				return;
			}

			rect = damaged;
		}

		ZSelectionMask mask = selectionModel.getMask();

		// what about layer damage?
		// just reset here for now.

		int count = 0;
		for (int i = 0; i < layers.size(); i++) {
			ZLayer l = layers.get(i);
			if (l.visible) {
				count++;
			}
			l.setDamage(null);
		}

		ZLayer[] layerArray = new ZLayer[count];
		count = 0;
		for (int i = 0; i < layers.size(); i++) {
			ZLayer l = layers.get(i);
			if (l.visible) {
				layerArray[count++] = l;
			}
		}

		composeLayers(rect, layerArray, mask, image);

		damaged = null;
	}
	Rectangle damaged = null;//new Rectangle();

	// TODO: better interface
	protected void addDamage(Rectangle bounds) {
		bounds = bounds.intersection(new Rectangle(dimension));
		if (!bounds.isEmpty()) {
			if (threadrefresh) {
				if (refreshThread == null) {
					refreshThread = new ZImageThread();
					refreshThread.start();
				}
				refreshThread.updates.add(bounds.getBounds());
				return;
			}
			if (damaged == null) {
				damaged = bounds;
			} else {
				damaged = damaged.union(bounds);
			}
			fireImageChanged(bounds);
		}
	}

	// multi-threaded damage stuff
	class ZImageThread extends Thread {

		LinkedBlockingQueue<Rectangle> updates = new LinkedBlockingQueue<Rectangle>();

		@Override
		public void run() {
			try {
				while (true) {
					Rectangle rect;
					Rectangle more;
					int count = 1;

					rect = updates.take();
					while ((more = updates.poll()) != null) {
						count++;
						rect = rect.union(more);
					}

					if (count > 1) {
						System.out.printf("had %d events\n", count);
					}

					refresh(rect);
					fireImageChanged(rect);
				}
			} catch (InterruptedException ex) {
				Logger.getLogger(ZImage.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
	public boolean threadrefresh = false;
	ZImageThread refreshThread;

	public void saveImage(File path) throws IOException {
		FileOutputStream fos = new FileOutputStream(path);
		ZipOutputStream jos = new ZipOutputStream(fos);

		System.out.println("Saving " + layers.size() + " to " + path);

		// TODO: save meta-data

		int len = layers.size();
		for (int i = 0; i < len; i++) {
			ZipEntry jarEnt = new ZipEntry(String.format("layer-%04d.png", i));

			jos.putNextEntry(jarEnt);
			ZLayer layer = layers.get(i);
			// FIXME: layers coudl be different types
			ZLayerRGBAFloat l = (ZLayerRGBAFloat) layer;
			BufferedImage bi = l.getImage();

			// need to conver to 16 bit?  float?
			if (false) {
				BufferedImage outimg = new BufferedImage(layer.bounds.width, layer.bounds.height, BufferedImage.TYPE_4BYTE_ABGR);
				outimg.getGraphics().drawImage(bi, 0, 0, null);
				ImageIO.write(outimg, "png", jos);
			} else {
				if (!ImageIO.write(bi, "TIFF", jos)) {
					System.out.println("couldn't find a write for image/format");
				}
			}
		}
		jos.close();
		fos.close();
	}

	ZImageEditor editor;
	public ZImageEditor getEditor() {
		if (editor == null) {
			editor = new ZImageEditor(this);
		}
		return editor;
	}
	
	public imagez.undo.ZUndoManager getUndoManager() {
		return undoManager;
	}

	public void resizeImage(Dimension nsize) {
		// hmmm ...
	}

	/**
	 * Changes the image size
	 * @param size 
	 */
	public void setSize(Dimension size) {
		Dimension osize = dimension;

		if (size.equals(osize))
			return;
		
		dimension = size;
		image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);

		addDamage(new Rectangle(0, 0, Math.max(osize.width, size.width), Math.max(osize.height, size.height)));
		fireImageChanged();
	}
}
