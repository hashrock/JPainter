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
import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author notzed
 */
public class ZLayerTableModel extends AbstractTableModel implements ZImageListener {

	static int COL_VISIBLE = 0;
	static int COL_LOCKED = 1;
	static int COL_ICON = 2;
	static int COL_TITLE = 3;
	final ZImage image;

	public ZLayerTableModel(ZImage image) {
		this.image = image;
		image.addImageListener(this);
	}

	public ZImage getImage() {
		return image;
	}

	/*
	public void add(ZLayer layer) {
	int row = layers.size();

	layer.addPropertyChangeListener(this);
	layers.add(layer);
	this.fireTableRowsInserted(row, row);
	}

	public void remove(int index) {
	ZLayer layer = layers.remove(index);

	layer.removePropertyChangeListener(this);
	this.fireTableRowsDeleted(index, index);
	}

	public void move(int oindex, int nindex) {
	ZLayer layer = layers.remove(oindex);

	if (nindex > oindex)
	nindex--;

	layers.add(nindex, layer);
	this.fireTableRowsDeleted(oindex, oindex);
	this.fireTableRowsInserted(nindex, nindex);
	}
	 */

	@Override
	public int getRowCount() {
		return image.getLayerCount();
	}

	@Override
	public int getColumnCount() {
		return 4;
	}
	Class[] classes = new Class[]{Boolean.class, Boolean.class, ImageIcon.class, String.class};

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return classes[columnIndex];
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 0 || columnIndex == 1 || columnIndex == 3;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		ZLayer layer = image.getLayerAt(rowIndex);

		switch (columnIndex) {
			case 0:
				layer.setVisible((Boolean) aValue);
				break;
			case 1:
				layer.setLocked((Boolean) aValue);
				break;
			case 3:
				layer.setTitle((String) aValue);
				break;
			default:
				return;
		}
		fireTableCellUpdated(rowIndex, columnIndex);
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		ZLayer layer = image.getLayerAt(rowIndex);

		switch (columnIndex) {
			case 0:
				return layer.isVisible();
			case 1:
				return layer.isLocked();
			case 2:
				// FIXME: don't create a new imageicon every time
				return new ImageIcon(layer.getIconImage());
			case 3:
				return layer.getTitle();
		}

		return "";
	}

	@Override
	public void imageChanged(Rectangle rect) {
	}

	@Override
	public void imageChanged() {
	}

	@Override
	public void layerAdded(int index, ZLayer layer) {
		fireTableRowsInserted(index, index);
	}

	@Override
	public void layerRemoved(int index, ZLayer layer) {
		fireTableRowsDeleted(index, index);
	}

	@Override
	public void layerChanged(int index, ZLayer olayer, ZLayer nlayer) {
		fireTableRowsUpdated(index, index);
	}

	@Override
	public void layerPropertyChanged(PropertyChangeEvent evt) {
		ZLayer layer = (ZLayer) evt.getSource();
		int row = layer.getZImage().indexOf(layer);

		if (row < 0) {
			return;
		}

		if (evt.getPropertyName().equals(ZLayer.PROP_TITLE)) {
			fireTableCellUpdated(row, COL_TITLE);
		} else if (evt.getPropertyName().equals(ZLayer.PROP_VISIBLE)) {
			fireTableCellUpdated(row, COL_VISIBLE);
		} else if (evt.getPropertyName().equals(ZLayer.PROP_LOCKED)) {
			fireTableCellUpdated(row, COL_LOCKED);
		} else if (evt.getPropertyName().equals(ZLayer.PROP_ICON)) {
			fireTableCellUpdated(row, COL_ICON);
		}
		// etc and the rest
	}
}
