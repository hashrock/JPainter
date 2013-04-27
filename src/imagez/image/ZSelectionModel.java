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

import imagez.fx.GaussianBlurFX;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * Manages a selection
 *
 * A selection is formed from a number of shapes which can
 * be composed of multiple components with boolean operators
 * applied.
 * @author notzed
 */
public class ZSelectionModel {

	public static final int MODE_REPLACE = 0;
	public static final int MODE_UNION = 1;
	public static final int MODE_SUBTRACT = 2;
	public static final int MODE_INTERSECT = 3;
	public static final int MODE_XOR = 4;
	//
	Area currentArea = new Area();
	Area workArea = new Area();
	Area editingArea;
	//
	boolean resetMask = true;
	ZSelectionMask mask;
	ZImage image;

	public ZSelectionModel(ZImage image) {
		this.image = image;
		mask = new ZSelectionMask(new Rectangle(image.dimension));
		shape = workArea;
	}

	/**
	 * Merge the last edit into the base object.
	 */
	public void mergeEdit() {
		currentArea = workArea;
	}

	/**
	 * Add another selection shape.
	 *
	 * If this is just an edit, then the last added is
	 * replaced with the edited version.
	 * @param mode
	 * @param s
	 * @param isedit
	 */
	public void updateSelection(int mode, Shape s, boolean isedit) {
		Area a = new Area(s);

		editingArea = a;

		if (!isedit) {
			currentArea = workArea;
		}
		workArea = currentArea.createTransformedArea(new AffineTransform());

		switch (mode) {
			case MODE_REPLACE:
				currentArea = workArea = a;
				break;
			case MODE_UNION:
				workArea.add(a);
				break;
			case MODE_SUBTRACT:
				workArea.subtract(a);
				break;
			case MODE_INTERSECT:
				workArea.intersect(a);
				break;
			case MODE_XOR:
				workArea.exclusiveOr(a);
				break;
		}

		// FIXME: recalc mask using background thread.
		resetMask = true;

		setShape(workArea);
	}
	//public static final String PROP_BOUNDS = "bounds";

	/**
	 * Retrieve the bounds which can contain this selection.
	 *
	 * If feathering is enabled then the bounds are adjusted appropriately.
	 * @return
	 */
	public Rectangle getBounds() {
		Rectangle bounds = workArea.getBounds();

		if (featherEnabled && !bounds.isEmpty()) {
			int d = (int) Math.ceil(featherAmount) * 3;

			bounds.x -= d;
			bounds.y -= d;
			bounds.width += d * 2;
			bounds.height += d * 2;
		}

		return bounds;
	}

	/**
	 * The last area being edited.
	 * @return
	 */
	public Shape getEditingShape() {
		return editingArea;
	}

	public void clearSelection() {
		workArea = new Area();
		currentArea = new Area();
		editingArea = null;
		setShape(workArea);
	}
	
	/**
	 * more cache friendly version, but could do better and use less tmp space
	 *
	 * FIXME: this doesn't handle the edges properly
	 */
	/*
	float[] blurrect(float[] src, int sposin, int sstride, float[] dst, int dposin, int dstride, int w, int h, float kernel[], int length) {
		int spos = sposin;
		int dpos = dposin;

		// Perform horizontal
		for (int j = 0; j < h; j++) {
			for (int i = 0; i < w - length; i++) {
				float v = src[spos] * kernel[0];
				for (int k = 1; k < length; k++) {
					v += src[spos + k] * kernel[k];
				}
				dst[dpos + j + length / 2 - 1] = v;
			}
			dpos += dstride;
			spos += sstride;
		}

		for (int i = 0; i < w; i += 16) {
			spos = sposin + i;
			dpos = dposin + i;
			for (int j = 0; j < h - length; j++) {
				for (int l = 0; l < 16; l++) {
					float v = 0;
					for (int k = 0; k < length; k++) {
						v += dst[dpos + l + k * w] * kernel[k];
					}
					src[spos + l + (length / 2 - 1) * w] = v;
				}
				spos += sstride;
				dpos += dstride;
			}
		}

		return dst;
	}

	// This version of the blur is more cache friently
	float[] blur16b(float[] src, float[] dst, Rectangle bounds, float kernel[], int length) {
		int pos = 0;
		int pmod = length;
		int w = bounds.width;
		int h = bounds.height;

		// Perform horizontal
		for (int j = 0; j < h; j++) {
			for (int i = 0; i < w - length; i++) {
				float v = src[pos] * kernel[0];
				for (int k = 1; k < length; k++) {
					v += src[pos + k] * kernel[k];
				}
				dst[pos + length / 2 - 1] = v;
				pos++;
			}
			pos += pmod;
		}

		for (int i = 0; i < w; i += 16) {
			pos = i;
			for (int j = 0; j < h - length; j++) {
				for (int l = 0; l < 16; l++) {
					float v = 0;
					for (int k = 0; k < length; k++) {
						v += dst[pos + l + k * w] * kernel[k];
					}
					src[pos + l + (length / 2 - 1) * w] = v;
				}
				pos += w;
			}
		}

		return dst;
	}
	 *
	 */
	
	static public void showImage(BufferedImage bi) {
		JFrame jf = new JFrame();
		
		jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		jf.add(new JLabel(new ImageIcon(bi)));		
		jf.pack();
		jf.setVisible(true);
	}

	/**
	 * Retrieve a mask describing the current selection.
	 * This is a one-channel zimage object which can be applied as a mask.
	 * @return null if no selection is set
	 */
	public ZSelectionMask getMask() {
		Rectangle bounds = getBounds();

		if (bounds.isEmpty()) {
			return null;
		}

		if (resetMask) {
			if (featherEnabled) {
				ZLayerMonoFloat tmpMask = new ZLayerMonoFloat(null, bounds);
				Graphics2D gg = tmpMask.getImage().createGraphics();

				System.out.println("feathering amount " + featherAmount);

				// Woeful.  Just woeful.

				// create a temporary layer just big enough for mask
				// render to it
				// blur it
				// copy it to the target full-size mask


				gg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				if (antiAliasingEnabled) {
					gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				}

				gg.setPaint(Color.white);
				gg.translate(-bounds.x, -bounds.y);
				gg.fill(workArea);

				gg.dispose();

				System.out.println("tmp mask " + tmpMask.bounds + " bounds " + bounds);

				GaussianBlurFX blur = new GaussianBlurFX(tmpMask, tmpMask, bounds);

				blur.startBlur(featherAmount);
				mask.clear();
				blur.waitBlur();

				// copy result over
				mask.replace(tmpMask);
			} else {
				BufferedImage bi = mask.getImage();
				Graphics2D gg = bi.createGraphics();

				System.out.println("not feathering");

				mask.clear();

				gg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				if (antiAliasingEnabled) {
					gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				}

				gg.setPaint(Color.white);
				gg.translate(-mask.bounds.x, -mask.bounds.y);
				gg.fill(workArea);
				gg.dispose();
			}

			resetMask = false;
		}

		return mask;
	}
	protected Shape shape;
	public static final String PROP_SHAPE = "shape";

	public Shape getShape() {
		return shape;
	}

	public void setShape(Shape shape) {
		Shape oldShape = this.shape;
		this.shape = shape;
		propertyChangeSupport.firePropertyChange(PROP_SHAPE, oldShape, shape);
	}

	protected boolean antiAliasingEnabled = true;
	public static final String PROP_ANTIALIASINGENABLED = "antiAliasingEnabled";

	public boolean isAntiAliasingEnabled() {
		return antiAliasingEnabled;
	}

	public void setAntiAliasingEnabled(boolean antiAliasingEnabled) {
		boolean oldAntiAliasingEnabled = this.antiAliasingEnabled;
		this.antiAliasingEnabled = antiAliasingEnabled;
		
		resetMask |= (oldAntiAliasingEnabled != antiAliasingEnabled);

		propertyChangeSupport.firePropertyChange(PROP_ANTIALIASINGENABLED, oldAntiAliasingEnabled, antiAliasingEnabled);
	}
	protected boolean featherEnabled = true;
	public static final String PROP_FEATHERENABLED = "featherEnabled";

	public boolean isFeatherEnabled() {
		return featherEnabled;
	}

	public void setFeatherEnabled(boolean featherEnabled) {
		boolean oldFeatherEnabled = this.featherEnabled;
		this.featherEnabled = featherEnabled;
		
		resetMask |= (oldFeatherEnabled != featherEnabled);

		propertyChangeSupport.firePropertyChange(PROP_FEATHERENABLED, oldFeatherEnabled, featherEnabled);
	}
	protected float featherAmount = 10.0f;
	public static final String PROP_FEATHERAMOUNT = "featherAmount";

	public float getFeatherAmount() {
		return featherAmount;
	}

	public void setFeatherAmount(float featherAmount) {
		float oldFeatherAmount = this.featherAmount;

		featherAmount = Math.max(0.1f, featherAmount);
		this.featherAmount = featherAmount;
				
		resetMask |= (oldFeatherAmount != featherAmount);
		
		propertyChangeSupport.firePropertyChange(PROP_FEATHERAMOUNT, oldFeatherAmount, featherAmount);
	}
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}
}
