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
package imagez.tool;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Model used for crop tool to map to ui
 * @author notzed
 */
public class CropModel {

	Point position;
	Dimension size;
	Dimension imageSize;

	public CropModel(Point pos, Dimension size) {
		this.position = pos;
		this.size = size;
		this.imageSize = size;

		reset();
	}

	public void setImageSize(Dimension sourceSize) {
		this.imageSize = sourceSize;
	}

	public Dimension getImageSize() {
		return imageSize;
	}
	
	public void reset() {
		setSizex(size.width);
		setSizey(size.height);
		setOffx(position.x);
		setOffy(position.y);
	}

	public Rectangle getBounds() {
		return new Rectangle(offx, offy, sizex, sizey);
	}
	
	public void setRange(int x1, int y1, int x2, int y2) {

		x1 = Math.max(x1, 0);
		x2 = Math.max(x2, 0);
		y1 = Math.max(y1, 0);
		y2 = Math.max(y2, 0);

		x1 = Math.min(x1, imageSize.width);
		x2 = Math.min(x2, imageSize.width);
		y1 = Math.min(y1, imageSize.height);
		y2 = Math.min(y2, imageSize.height);

		if (x1 > x2) {
			int t = x2;
			x2 = x1;
			x1 = t;
		}

		setOffx(x1);
		setSizex(x2 - x1);

		if (y1 > y2) {
			int t = y2;
			y2 = y1;
			y1 = t;
		}
		setOffy(y1);
		setSizey(y2 - y1);
	}
	protected int sizex;
	public static final String PROP_SIZEX = "sizex";

	public int getSizex() {
		return sizex;
	}

	public void setSizex(int sizex) {
		int oldSizex = this.sizex;
		this.sizex = sizex;
		propertyChangeSupport.firePropertyChange(PROP_SIZEX, oldSizex, sizex);
	}
	protected int sizey;
	public static final String PROP_SIZEY = "sizey";

	public int getSizey() {
		return sizey;
	}

	public void setSizey(int sizey) {
		int oldSizey = this.sizey;
		this.sizey = sizey;
		propertyChangeSupport.firePropertyChange(PROP_SIZEY, oldSizey, sizey);
	}
	protected int offx;
	public static final String PROP_OFFX = "offx";

	public int getOffx() {
		return offx;
	}

	public void setOffx(int offx) {
		int oldOffx = this.offx;
		this.offx = offx;
		propertyChangeSupport.firePropertyChange(PROP_OFFX, oldOffx, offx);
	}
	protected int offy;
	public static final String PROP_OFFY = "offy";

	public int getOffy() {
		return offy;
	}

	public void setOffy(int offy) {
		int oldOffy = this.offy;
		this.offy = offy;
		propertyChangeSupport.firePropertyChange(PROP_OFFY, oldOffy, offy);
	}
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}
}
