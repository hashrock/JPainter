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
import java.util.LinkedList;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 *
 * @author notzed
 */
public class ZLayerListModel implements ListModel, ZImageListener {
	LinkedList<ListDataListener> listDataListeners = new LinkedList<ListDataListener>();
	final ZImage image;

	public ZLayerListModel(ZImage image) {
		image.addImageListener(this);
		this.image = image;
	}

	public ZImage getImage() {
		return image;
	}

	public boolean add(ZLayer e) {
		image.addLayer(e);
		return true;
	}

	public void remove(ZLayer e) {
		image.removeLayer(e);
	}

	@Override
	public int getSize() {
		return image.getLayerCount();
	}

	@Override
	public Object getElementAt(int index) {
		return image.getLayerAt(index);
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		listDataListeners.add(l);
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		listDataListeners.remove(l);
	}

	protected void fireListContentsChanged(int pos0, int pos1) {
		ListDataEvent evt = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, pos0, pos1);

		for (int i=0;i<listDataListeners.size();i++) {
			listDataListeners.get(i).contentsChanged(evt);
		}
	}
	protected void fireListIntervalAdded(int pos0, int pos1) {
		ListDataEvent evt = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, pos0, pos1);

		for (int i=0;i<listDataListeners.size();i++) {
			listDataListeners.get(i).contentsChanged(evt);
		}
	}
	protected void fireListIntervalRemoved(int pos0, int pos1) {
		ListDataEvent evt = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, pos0, pos1);

		for (int i=0;i<listDataListeners.size();i++) {
			listDataListeners.get(i).contentsChanged(evt);
		}
	}

	@Override
	public void imageChanged(Rectangle rect) {
		// dont care
	}

	@Override
	public void imageChanged() {
		// dont care
	}

	@Override
	public void layerAdded(int index, ZLayer layer) {
		fireListIntervalAdded(index, index);
	}

	@Override
	public void layerRemoved(int index, ZLayer layer) {
		fireListIntervalRemoved(index, index);
	}

	@Override
	public void layerChanged(int index, ZLayer olayer, ZLayer nlayer) {
		fireListContentsChanged(index, index);
	}

	@Override
	public void layerPropertyChanged(PropertyChangeEvent evt) {
		// dont care
	}
}