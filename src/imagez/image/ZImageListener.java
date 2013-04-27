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

import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;

/**
 * Listener for an image.
 *
 * Could have multiple views of the same image?
 * @author notzed
 */
public interface ZImageListener {
	// image data changed
	void imageChanged(Rectangle rect);
	// whole image changed, including resize/layers
	void imageChanged();
	// Layers are not a high-volume list, so don't need bulk indicators
	void layerAdded(int index, ZLayer layer);
	void layerRemoved(int index, ZLayer layer);
	void layerChanged(int index, ZLayer olayer, ZLayer nlayer);
	void layerPropertyChanged(PropertyChangeEvent evt);
}
