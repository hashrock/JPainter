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
package imagez.ui;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Model used for editing rectangular regions
 * @author notzed
 */
public class RectangleModel {

	Dimension imageSize;

	public RectangleModel() {
		imageSize = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	public void setMaximumBounds(Dimension sourceSize) {
		this.imageSize = sourceSize;
	}

	public Dimension getImageSize() {
		return imageSize;
	}
	
	public Rectangle getBounds() {
		return new Rectangle(x1, y1, x2-x1, y2-y1);
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


		if (y1 > y2) {
			int t = y2;
			y2 = y1;
			y1 = t;
		}
		
		setX1(x1);
		setY1(y1);
		setX2(x2);
		setY2(y2);
	}
	
	protected int x1;
	public static final String PROP_X1 = "x1";

	public int getX1() {
		return x1;
	}

	public void setX1(int x1) {
		int oldX1 = this.x1;
		this.x1 = x1;
		propertyChangeSupport.firePropertyChange(PROP_X1, oldX1, x1);
	}

	protected int y1;
	public static final String PROP_Y1 = "y1";

	public int getY1() {
		return y1;
	}

	public void setY1(int y1) {
		int oldY1 = this.y1;
		this.y1 = y1;
		propertyChangeSupport.firePropertyChange(PROP_Y1, oldY1, y1);
	}
	protected int x2;
	public static final String PROP_X2 = "x2";

	public int getX2() {
		return x2;
	}

	public void setX2(int x2) {
		int oldX2 = this.x2;
		this.x2 = x2;
		propertyChangeSupport.firePropertyChange(PROP_X2, oldX2, x2);
	}
	protected int y2;
	public static final String PROP_Y2 = "y2";

	public int getY2() {
		return y2;
	}

	public void setY2(int y2) {
		int oldY2 = this.y2;
		this.y2 = y2;
		propertyChangeSupport.firePropertyChange(PROP_Y2, oldY2, y2);
	}

	
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}
}
