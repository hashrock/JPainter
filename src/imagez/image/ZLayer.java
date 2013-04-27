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

import imagez.blend.BlendMode;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author notzed
 */
public abstract class ZLayer<W extends ZLayer> {

	public static final int TMP_UNSET = 0;
	public static final int TMP_OVERLAY = 1;
	public static final int TMP_REPLACE = 2;
	// for tmp layer cache, do i need this?
	static Map<String, ZLayer> map = new LinkedHashMap<String, ZLayer>();
	// properties
	protected Rectangle bounds;
	ZImage zimage;
	String title;
	boolean visible = true;
	boolean locked = false;
	BlendMode mode = BlendMode.normal;
	float opacity = 1f;
	protected Rectangle damage;
	BufferedImage iconImage;
	protected BufferedImage bimage;
	// tool layer stuff
	ZLayer tmpLayer;
	int tmpLayerMode = TMP_UNSET;

	Dimension fitSize(Dimension size, Dimension target) {
		float dx = (float) size.width / target.width;
		float dy = (float) size.height / target.height;
		Dimension rect;

		if (dx > dy) {
			rect = new Dimension(target.width, target.width * size.height / size.width);
		} else {
			rect = new Dimension(target.height * size.width / size.height, target.height);
		}

		return rect;
	}

	public ZLayer(ZImage zimage, Rectangle bounds) {
		this.zimage = zimage;
		this.bounds = bounds;

		Dimension d = fitSize(bounds.getSize(), new Dimension(48, 32));
		iconImage = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB_PRE);
	}

	abstract public void getLineRGBA(float[] dst, int doff, int x, int y, int width);

	abstract public void setLineRGBA(float[] src, int soff, int x, int y, int width);

	abstract public int getStride();

	abstract public int getChannelCount();

	/**
	 * Retrieve the channels of the image in float format.
	 * @param dst
	 * @param doff
	 * @param x
	 * @param y
	 * @param width
	 */
	abstract public void getLine(float[][] dst, int doff, int x, int y, int width);

	abstract public void setLine(float[][] src, int soff, int x, int y, int width);
	//??abstract public void replace(W src);

	abstract public void clear(Rectangle rect);

	public void clear() {
		clear(bounds);
	}

	/**
	 * @param nbounds
	 * @param cloneData clone title, opacity and other meta-data.
	 * @return
	 */
	public ZLayer createCompatibleLayer(Rectangle nbounds, boolean cloneData) {
		try {
			Constructor c = getClass().getConstructor(ZImage.class, Rectangle.class);
			ZLayer layer = (ZLayer) c.newInstance(zimage, nbounds);
			
			if (cloneData) {
				layer.title = title;
				layer.opacity = opacity;
				layer.visible = visible;
				layer.mode = mode;
			}
			return layer;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Create a tmp layer compatible with this one.
	 *
	 * Default implementations creates a layer of the same size and type.
	 * @return
	 */
	public ZLayer createTmpLayer() {
		return createCompatibleLayer(bounds, false);
	}

	/**
	 * Create a temp layer for drawing into.  This should have an alpha channel
	 * and needn't be compatible with the target image.
	 * @return
	 */
	public ZLayer createTmpLayerOverlay() {
		//return new ZLayerRGBASparseInt(zimage, bounds);
		return new ZLayerRGBAInt(zimage, bounds);
	}

	// <editor-fold defaultstate="collapsed" desc="Tool Layer & Cache">
	// Tool layer cache
	static synchronized ZLayer getTmpLayer(ZLayer prototype) {
		String key = prototype.getClass().getName() + prototype.bounds.width + "x" + prototype.bounds.height;
		ZLayer layer = map.remove(key);

		if (layer == null) {
			// should layer have a gettmplayer to suit?  possibly.
			//layer = new RGBATmpLayer(prototype.zimage, prototype.bounds);
//			layer = new ZLayerRGBASparseFloat(prototype.zimage, prototype.bounds);
			layer = prototype.createTmpLayerOverlay();

			System.out.println("get tmplayer from " + prototype + " is " + layer);

//			layer = new ZLayerRGBAInt(prototype.zimage, prototype.bounds);
//			layer = new ZLayerRGBASparseInt(prototype.zimage, prototype.bounds);
			// FIXME: remove older entries if the size is too big
			map.put(key, layer);
		}

		return layer;
	}

	static synchronized void dropTmpLayer(ZLayer prototype, ZLayer layer) {
		String key = prototype.getClass().getName() + prototype.bounds.width + "x" + prototype.bounds.height;

		// good time to clear it, when user drops it
		layer.clear();

		map.put(key, layer);
	}

	public int getTmpLayerMode() {
		return tmpLayerMode;
	}

	public ZLayer getTmpLayer() {
		return tmpLayer;
	}

	/**
	 * Acquire a temporary tool layer.  Once finished with releaseTmpLayer() must be
	 * invoked.  Only one temp layer may be active at any one time.
	 *
	 * See also ZImageCompositor.
	 *
	 * @param mode TMP_OVERLAY or TMP_REPLACE.  TMP_OVERLAY means the layer edits will be
	 * merged into the layer based on the layer blending modes and selection mask.
	 * TMP_REPLACE means the temp layer replaces the content in the original layer
	 * based on the selection mask.
	 * @return A layer that can be used for editing.
	 */
	public ZLayer aquireTmpLayer(int mode) {
		ZLayer layer;

		assert tmpLayer == null;

		if (mode == TMP_OVERLAY) {
			layer = (ZLayer) getTmpLayer(this);
		} else {
			layer = createTmpLayer();
		}

		layer.zimage = zimage;
		layer.title = "tmp:" + title;
		layer.bounds.x = bounds.x;
		layer.bounds.y = bounds.y;

		layer.opacity = opacity;
		layer.mode = this.mode;
		layer.visible = true;

		tmpLayer = layer;
		tmpLayerMode = mode;

		return layer;
	}
	ZImageCompositor comp;

	/**
	 * Complete use of the temporary tool layer.
	 *
	 * @param layer Layer to be released.
	 * @param apply If true, then apply the changes represented by the layer and layer mode.
	 * @param rect Region changed, if known.  Maybe null for whole image.
	 */
	public void releaseTmpLayer(ZLayer layer, boolean apply, Rectangle rect) {
		assert tmpLayer == layer;

		if (rect == null) {
			rect = getBounds().getBounds();
		} else {
			rect = rect.intersection(getBounds());
		}

		if (apply) {
			if (comp == null) {
				comp = new ZImageCompositor();
			}

			if (zimage != null && zimage.isUndoEnabled()) {
				ZLayerRGBASparseFloat delta = new ZLayerRGBASparseFloat(null, rect);
				comp.composeDownDelta(rect, this, layer, zimage.getSelectionModel().getMask(), tmpLayerMode, delta);

				zimage.getUndoManager().addLayerChange(this, rect, delta);
			} else {
				comp.composeDown(rect, this, layer, zimage.getSelectionModel().getMask(), tmpLayerMode);
			}
		}
		addDamage(rect);
		scheduleUpdateIcon();

		if (tmpLayerMode == TMP_OVERLAY) {
			dropTmpLayer(this, layer);
		}

		tmpLayer = null;
		tmpLayerMode = TMP_UNSET;
	}
	// </editor-fold>
	// <editor-fold defaultstate="collapsed" desc="Properties">
	public static final String PROP_TITLE = "title";
	public static final String PROP_LOCKED = "locked";
	public static final String PROP_VISIBLE = "visible";
	public static final String PROP_ICON = "icon";
	public static final String PROP_OPACITY = "opacity";
	public static final String PROP_MODE = "mode";
	public static final String PROP_DAMAGE = "damage";
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		String oldTitle = this.title;
		this.title = title;
		propertyChangeSupport.firePropertyChange(PROP_TITLE, oldTitle, title);
	}

	public void setOpacity(float opacity) {
		if (opacity != this.opacity) {
			float old = this.opacity;
			this.opacity = opacity;
			propertyChangeSupport.firePropertyChange(PROP_OPACITY, old, opacity);
		}
	}

	public float getOpacity() {
		return opacity;
	}

	public BlendMode getMode() {
		return mode;
	}

	public void setMode(BlendMode value) {
		if (!this.mode.equals(value)) {
			BlendMode old = this.mode;
			this.mode = value;
			propertyChangeSupport.firePropertyChange(PROP_MODE, old, value);
		}
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		if (visible != this.visible) {
			boolean old = this.visible;
			this.visible = visible;
			propertyChangeSupport.firePropertyChange(PROP_VISIBLE, old, visible);
		}
	}

	public void setLocked(boolean locked) {
		boolean old = this.locked;
		this.locked = locked;
		propertyChangeSupport.firePropertyChange(PROP_LOCKED, old, locked);
	}

	public boolean isLocked() {
		return locked;
	}

	public Rectangle getDamage() {
		return damage;
	}

	public void setDamage(Rectangle damage) {
		Rectangle oldDamage = this.damage;
		this.damage = damage;
		propertyChangeSupport.firePropertyChange(PROP_DAMAGE, oldDamage, damage);
	}
	// </editor-fold>
	// <editor-fold defaultstate="collapsed" desc="Icon Generation/access">
	Timer timeout;
	UpdateIconTask updateIcon;

	class UpdateIconTask extends TimerTask {

		@Override
		public void run() {
			updateIcon = null;

			Graphics2D gg = (Graphics2D) iconImage.getGraphics();

			gg.setBackground(Color.gray);
			gg.clearRect(0, 0, iconImage.getWidth(), iconImage.getHeight());
			gg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			//gg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			gg.drawImage(getImage(), 0, 0, iconImage.getWidth(), iconImage.getHeight(), null);
			gg.dispose();

			propertyChangeSupport.firePropertyChange(PROP_ICON, null, iconImage);
		}
	}

	// synchronized?
	void scheduleUpdateIcon() {
		if (timeout == null) {
			timeout = new Timer("icon updater", true);
		}

		if (updateIcon == null) {
			updateIcon = new UpdateIconTask();
			timeout.schedule(updateIcon, 500);
		}
	}

	public BufferedImage getIconImage() {
		return iconImage;
	}
	// </editor-fold>

	public Rectangle getBounds() {
		return bounds;
	}

	public BufferedImage getImage() {
		return bimage;
	}

	/**
	 * Copy the layer + current selection
	 * @return
	 */
	public BufferedImage copy() {
		// TODO: the clipboard handles the format conversion
		// So want to save in format we're in, not convert to INT_ARGB
		//if (true) {
		//	System.out.println(" copy from " + this);
		//	return bimage;
		//}

		ZSelectionMask mask = zimage.getSelectionModel().getMask();
		if (mask == null) {
			return bimage;
		}

		Rectangle rect = zimage.getSelectionModel().getBounds().intersection(bounds);

		/*
		 * Manually convert to a simple 8 bit copy/paste format.
		 *
		 * Since we store everything in pre-mult alpha need to divide by alpha first.
		 */

		BufferedImage cimage = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_ARGB);

		int[] cdata = ((DataBufferInt) cimage.getRaster().getDataBuffer()).getData();
		float[] mrow = new float[rect.width];
		float[] srow = new float[rect.width * 4];
		int dpos = 0;
		for (int y = 0; y < rect.height; y++) {
			mask.getLineMono(mrow, 0, rect.x, y + rect.y, rect.width);
			this.getLineRGBA(srow, 0, rect.x, y + rect.y, rect.width);
			for (int x = 0; x < rect.width; x++) {
				float m = mrow[x];
				float A = srow[x * 4 + 3];

				if (Math.abs(A) > 1E-5) {
					int r = (int) (srow[x * 4 + 0] / A * 255.0f + 0.5f);
					int g = (int) (srow[x * 4 + 1] / A * 255.0f + 0.5f);
					int b = (int) (srow[x * 4 + 2] / A * 255.0f + 0.5f);
					int a = (int) (A * m * 255.0f + 0.5f);

					cdata[dpos++] = (a << 24) | (r << 16) | (g << 8) | b;
				} else {
					cdata[dpos++] = 0;
				}
			}
		}

		return cimage;
	}

	public ZImage getZImage() {
		return zimage;
	}

	public void addDamage(Rectangle rect) {

		rect = bounds.intersection(rect);

		if (rect.isEmpty()) {
			return;
		}

		Rectangle d = getDamage();

		if (d == null) {
			d = rect;
		} else {
			d = d.union(rect);
		}
		setDamage(d);
		// TODO: not entirley happy with this - temp layers aren't listened to by the image,
		// so tools pass damage through the base layer ... but that isn't updated until the
		// tool is finished.  so redundant icon creation.
		//scheduleUpdateIcon();
	}
}
