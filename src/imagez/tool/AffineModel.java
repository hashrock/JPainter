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
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Model used for affine tool to map to ui
 * @author notzed
 */
public class AffineModel {

	protected Dimension size;

	public AffineModel(Dimension ssize) {
		this.size = ssize;
		sizex = ssize.width;
		sizey = ssize.height;
	}

	public Dimension getSize() {
		return size;
	}

	public void reset() {
		setAngle(0);
		setPivot(new Point2D.Double());
		setTranslate(new Point2D.Double());
		setSizex(size.width);
		setSizey(size.height);
		setShearx(0);
		setSheary(0);
	}

	public void setSize(Dimension s) {
		// reset everything if size changes?
		size = s.getSize();
		reset();
	}

	public AffineTransform getCombinedTransform() {
		AffineTransform at = new AffineTransform();

		// FIXME: need to rotate after others not before.
		at.translate(translate.getX(), translate.getY());
		at.translate(size.width / 2, size.height / 2);
		at.scale((double) sizex / size.width, (double) sizey / size.height);
		at.shear(shearx / size.width, sheary / size.height);
		at.translate(-size.width / 2, -size.height / 2);
		at.translate(size.width / 2 + pivot.getX(), size.height / 2 + pivot.getY());
		at.rotate(angle * Math.PI / 180);
		at.translate(-size.width / 2 - pivot.getX(), -size.height / 2 - pivot.getY());

		return at;
	}
	protected float shearx;
	public static final String PROP_SHEARX = "shearx";

	public float getShearx() {
		return shearx;
	}

	public void setShearx(float shearx) {
		float oldShearx = this.shearx;
		this.shearx = shearx;
		propertyChangeSupport.firePropertyChange(PROP_SHEARX, oldShearx, shearx);
	}
	protected float sheary;
	public static final String PROP_SHEARY = "sheary";

	public float getSheary() {
		return sheary;
	}

	public void setSheary(float sheary) {
		float oldSheary = this.sheary;
		this.sheary = sheary;
		propertyChangeSupport.firePropertyChange(PROP_SHEARY, oldSheary, sheary);
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
	protected double angle;
	public static final String PROP_ANGLE = "angle";

	public double getAngle() {
		return angle;
	}

	/**
	 * Set affine angle in degrees
	 * @param angle
	 */
	public void setAngle(double angle) {
		double oldAngle = this.angle;
		this.angle = angle;
		propertyChangeSupport.firePropertyChange(PROP_ANGLE, oldAngle, angle);
	}
	protected Point2D pivot = new Point2D.Double();
	public static final String PROP_PIVOT = "pivot";

	public Point2D getPivot() {
		return pivot;
	}

	public void setPivot(Point2D pivot) {
		Point2D oldPivot = this.pivot;
		this.pivot = pivot;
		propertyChangeSupport.firePropertyChange(PROP_PIVOT, oldPivot, pivot);
	}
	protected Point2D translate = new Point2D.Double();
	public static final String PROP_TRANSLATE = "translate";

	public Point2D getTranslate() {
		return translate;
	}

	public void setTranslate(Point2D translate) {
		Point2D oldTranslate = this.translate;
		this.translate = translate;
		propertyChangeSupport.firePropertyChange(PROP_TRANSLATE, oldTranslate, translate);
	}
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}
}
