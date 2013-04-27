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
package imagez.image;

import imagez.undo.ImageResizeRecord;
import imagez.undo.LayerReplaceRecord;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

/**
 * Front-end for various operations on images.
 * 
 * This handles updating the undo-stack and so on, which are not performed
 * by the image interfaces.
 * @author notzed
 */
public class ZImageEditor {

	final ZImage image;

	public ZImageEditor(ZImage image) {
		this.image = image;
	}

	/**
	 * Create a new layer and append it to the layer list.
	 * @param type
	 * @param bounds
	 * @param title
	 * @return 
	 */
	public ZLayer addLayer(ZLayerType type, Rectangle bounds, String title) {
		ZLayer layer = type.createLayer(image, bounds);

		if (title != null) {
			layer.setTitle(title);
		}
		int index = image.addLayer(layer);

		image.getUndoManager().addLayerCreate(layer, index);
		return layer;
	}

	/**
	 * Move a layer at a given index to another index.
	 * @param oindex
	 * @param nindex 
	 */
	public void moveLayer(int oindex, int nindex) {
		assert (oindex >= 0 && oindex < image.getLayerCount());
		assert (nindex >= 0 && nindex < image.getLayerCount());

		image.moveLayer(oindex, nindex);
		image.getUndoManager().addLayerReorder(image, oindex, nindex);
	}

	/**
	 * Delete the specified layer.
	 * @param layer 
	 */
	public void removeLayer(ZLayer layer) {
		int index = image.indexOf(layer);

		image.removeLayer(layer);
		image.getUndoManager().addLayerDelete(layer, index);
	}

	/**
	 * Replace a layer with completely new content.
	 * 
	 * Use this if every pixel has changed, or the image was resized.
	 * @param layer
	 * @param nlayer 
	 */
	public void replaceLayer(ZLayer layer, ZLayer nlayer) {
		image.replaceLayer(layer, nlayer);
		image.getUndoManager().addLayerReplace(layer, nlayer);
	}

	/**
	 * Resize an individual layer.
	 * @param layer
	 * @param newsize
	 * @param hint RenderingHints.VALUE_INTERPOLATION* for resize operation.
	 */
	public void resizeLayer(ZLayer layer, Dimension newsize, Object hint) {
		Rectangle bounds = layer.getBounds();
		Rectangle nbounds = new Rectangle(bounds.x, bounds.y, newsize.width, newsize.height);
		ZLayer nlayer = layer.createCompatibleLayer(nbounds, true);

		System.out.println("resizing " + newsize + " using " + hint);

		Graphics2D gg = nlayer.getImage().createGraphics();
		if (hint != null) {
			gg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
		}
		gg.drawImage(layer.getImage(), 0, 0, nlayer.getBounds().width, nlayer.getBounds().height, null);
		gg.dispose();

		replaceLayer(layer, nlayer);
	}

	/**
	 * Replace layer with an affine transform thereof.
	 * 
	 * The layer bounds are adjusted to take into account the transformed
	 * bounds - it should include the whole image and offset appropriately.
	 * @param layer
	 * @param at
	 * @param hint 
	 */
	public void affineLayer(ZLayer layer, AffineTransform at, Object hint) {
		Path2D.Float f = new Path2D.Float(layer.getBounds(), at);
		Rectangle nbounds = f.getBounds();

		// noop if nothing changed?

		AffineTransform recentre = new AffineTransform();
		recentre.translate(-nbounds.x, -nbounds.y);

		recentre.concatenate(at);

		ZLayer nlayer = layer.createCompatibleLayer(nbounds, true);

		System.out.println("new bounds = " + nbounds + "layer type " + layer);

		Graphics2D gg = nlayer.getImage().createGraphics();
		if (hint != null) {
			gg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
		}
		gg.drawImage(layer.getImage(), recentre, null);
		gg.dispose();

		replaceLayer(layer, nlayer);
	}

	/**
	 * Resize an image.  Layers are scaled proproptionally.
	 * @param size
	 * @param hint 
	 */
	public void resizeImage(Dimension size, Object hint) {
		int count = image.getLayerCount();
		ImageResizeRecord cr = new ImageResizeRecord(image, image.getDimension(), size);
		Dimension osize = image.getDimension();

		System.out.println("resizing image " + osize + " to " + size);
		image.setSize(size);

		for (int l = 0; l < count; l++) {
			ZLayer layer = image.getLayer(l);
			Rectangle bounds = layer.getBounds();
			Rectangle nbounds = new Rectangle(bounds.x * size.width / osize.width, bounds.y * size.height / osize.height,
					bounds.width * size.width / osize.width, bounds.height * size.height / osize.height);
			ZLayer nlayer = layer.createCompatibleLayer(nbounds, true);

			Graphics2D gg = nlayer.getImage().createGraphics();
			gg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
			gg.drawImage(layer.getImage(), 0, 0, nlayer.getBounds().width, nlayer.getBounds().height, null);
			gg.dispose();

			image.replaceLayer(layer, nlayer);
			cr.addRecord(new LayerReplaceRecord(layer, nlayer));
		}

		image.getUndoManager().addCompound(cr);
	}
	
	/**
	 * Crop the whole image to a new rectangle.
	 * @param rect 
	 */
	public void cropImage(Rectangle rect) {
		int count = image.getLayerCount();
		Dimension size = rect.getSize();
		ImageResizeRecord cr = new ImageResizeRecord(image, image.getDimension(), size);

		System.out.println("Cropping image " + rect);
		image.setSize(size);

		for (int l = 0; l < count; l++) {
			ZLayer layer = image.getLayer(l);
			Rectangle bounds = layer.getBounds();
			Rectangle nbounds = bounds.intersection(rect);
			Rectangle lbounds = nbounds.getBounds();

			lbounds.x -= rect.x;
			lbounds.y -= rect.y;
			
			ZLayer nlayer = layer.createCompatibleLayer(lbounds, true);

			System.out.println(" layer " + bounds + " new bounds " + lbounds);
			System.out.println(" copy " + nbounds);
			
			Graphics2D gg = nlayer.getImage().createGraphics();
			gg.drawImage(layer.getImage(), 0, 0, nbounds.width, nbounds.height,
					nbounds.x, nbounds.y, nbounds.x + nbounds.width, nbounds.y + nbounds.height, null);
			gg.dispose();

			image.replaceLayer(layer, nlayer);
			cr.addRecord(new LayerReplaceRecord(layer, nlayer));
		}

		image.getUndoManager().addCompound(cr);
	}

}
