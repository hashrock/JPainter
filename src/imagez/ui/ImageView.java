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

import imagez.actions.ZoomAction;
import imagez.brush.BrushContext;
import imagez.image.ZSelectionModel;
import imagez.image.ZLayer;
import imagez.image.ZImage;
import imagez.image.ZImageListener;
import imagez.image.ZSelectionMask;
import imagez.tool.ZTool;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 *
 * @author notzed
 */
public class ImageView extends JComponent implements ZImageListener, KeyListener, MouseMotionListener, MouseListener {

	PropertyChangeListener selectionListener = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			String name = evt.getPropertyName();

			if (showSelection | name.equals(ZSelectionModel.PROP_SHAPE)) {
				repaint();
			}
		}
	};
	//
	protected ZImage image;
	protected ZLayer currentLayer;
	ImageWindow win;
	public final BrushContext brushContext;
	/**
	 * Display the layer edges
	 */
	boolean showLayerEdges = true;
	/**
	 * Show the selection using a fill overlay thing
	 */
	boolean showSelection = false;
	// New tool overlay interface
	AffineTransform screenToTool;
	ArrayList<ToolOverlay> overlays = new ArrayList<ToolOverlay>();

	public ImageView(ImageWindow win) {
		this.win = win;
		this.setDoubleBuffered(false);
		this.setOpaque(false);

		brushContext = new BrushContext();

		InputMap imap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
		ActionMap amap = getActionMap();

		// TODO: i presume the actions should be singletons
		for (int i = 1; i <= 8; i++) {
			char key = Character.forDigit(i, 10);
			String name = "zoom" + key;

			imap.put(KeyStroke.getKeyStroke(key), name);
			amap.put(name, new ZoomAction(i));

			imap.put(KeyStroke.getKeyStroke("shift " + key), "-" + name);
			amap.put("-" + name, new ZoomAction(1.0f / i));
		}

		this.setPreferredSize(new Dimension(512, 256));
		this.addMouseMotionListener(this);

		screenToTool = new AffineTransform();
		screenToTool.setToScale(zoom, zoom);

		// For overlay event routing
		this.addMouseListener(this);

		foregroundColour = Color.BLACK;
		backgroundColour = Color.white;
	}

	public void setImage(ZImage image) {
		if (this.image != null) {
			this.image.removeImageListener(this);
			this.image.getSelectionModel().removePropertyChangeListener(selectionListener);
		}
		this.image = image;
		if (image != null) {
			image.addImageListener(this);
			image.getSelectionModel().addPropertyChangeListener(selectionListener);

			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			float f = 1;
			zoom = 1.0f;
			while (image.getDimension().width * zoom > d.width
					|| image.getDimension().height * zoom > d.height) {
				f += 1;
				zoom = 1 / f;
			}

			currentLayer = image.getLayer(0);

			this.setPreferredSize(imageToWin(new Rectangle(image.getDimension())).getSize());
		}
		this.repaint();
	}
	protected ZTool tool;
	protected ZTool lastTool;
	public static final String PROP_TOOL = "tool";

	public ZTool getTool() {
		return tool;
	}

	public void setTool(ZTool tool) {

		lastTool = this.tool;
		this.tool = tool;

		if (lastTool != null) {
			lastTool.setImageView(null);
		}

		tool.setImageView(this);
		firePropertyChange(PROP_TOOL, lastTool, tool);
	}

	/**
	 * Revert to previous tool
	 */
	public void previousTool() {
		setTool(lastTool);
	}
	protected Color foregroundColour;
	public static final String PROP_FOREGROUNDCOLOUR = "foregroundColour";

	public Color getForegroundColour() {
		return foregroundColour;
	}

	public void setForegroundColour(Color foregroundColour) {
		Color oldForegroundColour = this.foregroundColour;
		this.foregroundColour = foregroundColour;
		firePropertyChange(PROP_FOREGROUNDCOLOUR, oldForegroundColour, foregroundColour);
	}
	protected Color backgroundColour;
	public static final String PROP_BACKGROUNDCOLOUR = "backgroundColour";

	public Color getBackgroundColour() {
		return backgroundColour;
	}

	public void setBackgroundColour(Color backgroundColour) {
		Color oldBackgroundColour = this.backgroundColour;
		this.backgroundColour = backgroundColour;
		firePropertyChange(PROP_BACKGROUNDCOLOUR, oldBackgroundColour, backgroundColour);
	}

	public ZImage getImage() {
		return image;
	}

	public ZLayer getCurrentLayer() {
		return currentLayer;
	}

	public void setCurrentLayer(ZLayer currentLayer) {
		this.currentLayer = currentLayer;
	}

	public ZSelectionModel getCurrentSelection() {
		return image.getSelectionModel();
	}
	protected float zoom = 1.0f;
	public static final String PROP_ZOOM = "zoom";

	public float getZoom() {
		return zoom;
	}

	public void setZoom(float zoom) {
		float oldZoom = this.zoom;
		this.zoom = zoom;
		firePropertyChange(PROP_ZOOM, oldZoom, zoom);

		// Update overlay zoom(s)
		screenToTool.setToScale(zoom, zoom);
		for (ToolOverlay ti : overlays) {
			ti.updateTransform(screenToTool);
		}

		Dimension d = image != null ? image.getDimension().getSize() : new Dimension(512, 256);
		d.width *= zoom;
		d.height *= zoom;
		setPreferredSize(d);
		System.out.println("set preferred size " + d + " zoom " + zoom);
		revalidate();
		repaint();
	}

	public void setShowLayerEdges(boolean showLayerEdges) {
		if (this.showLayerEdges != showLayerEdges) {
			this.showLayerEdges = showLayerEdges;
			repaint();
		}
	}

	public boolean isShowLayerEdges() {
		return showLayerEdges;
	}
	
	/**
	 * Convert point from window coordinates to pixel coordinates
	 * @param p
	 * @return
	 */
	public Point2D.Double convertPoint(Point p) {
		return new Point2D.Double(p.x / zoom, p.y / zoom);
	}

	// convert window coordinates to rectangle coordinates
	public Rectangle winToImage(Rectangle rect) {
		return new Rectangle(
				(int) (rect.x / zoom),
				(int) (rect.y / zoom),
				(int) (rect.width / zoom),
				(int) (rect.height / zoom));
	}

	public Rectangle imageToWin(Rectangle rect) {
		return new Rectangle(
				(int) (rect.x * zoom),
				(int) (rect.y * zoom),
				(int) (rect.width * zoom),
				(int) (rect.height * zoom));
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D gg = (Graphics2D) g.create();
		if (image != null) {
			Rectangle rect = g.getClipBounds();
			Rectangle irect = winToImage(rect);

			if (!image.threadrefresh) {
				image.refresh(irect);
			}
			BufferedImage bi = image.getImage();

			gg.scale(zoom, zoom);
			gg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

			gg.drawImage(bi, null, null);

			Shape selection = getCurrentSelection().getShape();
			if (!selection.getBounds().isEmpty()) {
				gg.setStroke(new BasicStroke(1.0f / zoom, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 2, new float[]{5 / zoom, 5 / zoom}, 0));
				gg.draw(selection);
				gg.setColor(Color.white);
				gg.setStroke(new BasicStroke(1.0f / zoom, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 2, new float[]{5 / zoom, 5 / zoom}, 5 / zoom));
				gg.draw(selection);

				selection = getCurrentSelection().getEditingShape();
				if (selection != null) {
					gg.setStroke(new BasicStroke(1.0f / zoom, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 2, new float[]{5 / zoom, 5 / zoom}, 0));
					gg.setColor(Color.blue);
					gg.draw(selection);
					gg.setStroke(new BasicStroke(1.0f / zoom, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 2, new float[]{5 / zoom, 5 / zoom}, 5 / zoom));
					gg.setColor(Color.yellow);
					gg.draw(selection);
				}
				
				// A filled overlay showing selection feathering as well
				if (showSelection) {
					ZSelectionMask mask = getCurrentSelection().getMask();
					Rectangle bounds = mask.getBounds();
					
					gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
					gg.drawImage(mask.getImage(), bounds.x, bounds.y, null);
					gg.setPaintMode();
				}
			}

			if (tool != null) {
				tool.paint(gg);
			}

			gg.scale(1 / zoom, 1 / zoom);

			// show layer edges if on
			if (showLayerEdges) {
				Graphics2D g2 = (Graphics2D) g.create();
				int count = image.getLayerCount();
				AffineTransform at = new AffineTransform();

				at.setToScale(zoom, zoom);

				g2.setXORMode(Color.yellow);
				g2.setColor(Color.blue);
				g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10, new float[]{3, 2}, 0));

				for (int i = 0; i < count; i++) {
					ZLayer l = image.getLayer(i);

					g2.draw(at.createTransformedShape(l.getBounds()));
				}
				g2.dispose();
			}

			{
				// new overlays
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				int count = overlays.size();
				for (int i = 0; i < count; i++) {
					ToolOverlay ti = overlays.get(i);

					ti.paint(g2);
				}
				g2.dispose();
			}

		} else {
			gg.setFont(new Font("Dialog", Font.BOLD, 12));
			gg.drawString("No Image Loaded.", 64, 64);
			gg.drawString("Access the menu using the right mouse button.", 64, 64 + 20);
		}
		gg.dispose();
	}

	@Override
	public void imageChanged() {
		if (win != null) {
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

			this.setPreferredSize(imageToWin(new Rectangle(image.getDimension())).getSize());
			win.imageChanged();
		}
	}

	@Override
	public void imageChanged(Rectangle rect) {
		repaint(imageToWin(rect));
	}

	@Override
	public void layerAdded(int index, ZLayer layer) {
	}

	@Override
	public void layerRemoved(int index, ZLayer layer) {
	}

	@Override
	public void layerChanged(int index, ZLayer olayer, ZLayer nlayer) {
		if (currentLayer == olayer) {
			setCurrentLayer(nlayer);
		}
	}

	@Override
	public void layerPropertyChanged(PropertyChangeEvent evt) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
		System.out.println("key typed " + e);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		System.out.println("key pressed " + e);
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	// Proxy mouse events
	@Override
	public void mouseDragged(MouseEvent e) {
		overlayMouseDragged(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		overlayMouseMoved(e);
	}
	boolean[] buttonState = new boolean[4];

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		int b = e.getButton();
		if (buttonState[b]) {
			System.out.println("mouse button press out of sync " + b);
		}
		buttonState[b] = true;

		overlayMousePressed(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		int b = e.getButton();
		if (!buttonState[b]) {
			System.out.println("mouse button up out of sync " + b);
		}
		buttonState[b] = false;

		overlayMouseReleased(e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * New tool interface
	 */
	public AffineTransform getScreenToTool() {
		return screenToTool;
	}

	public void repaintImage(Rectangle r) {
		// convert rectangle to unscaled coordinates then repaint
		//repaint(r.x / zoom, r.y/zoom, WIDTH, WIDTH);
		repaint(imageToWin(r));
	}

	public void addOverlay(ToolOverlay overlay) {
		overlay.updateTransform(screenToTool);
		overlays.add(overlay);
		overlay.repaint();
	}

	public void removeOverlay(ToolOverlay overlay) {
		overlays.remove(overlay);
		overlay.repaint();
	}

	ToolOverlay addItem(Shape shape, Paint paint, Stroke stroke) {
		ToolOverlay item = new ToolOverlay(this, shape, paint, stroke);
		item.updateTransform(screenToTool);
		overlays.add(item);

		return item;
	}
	ToolOverlay mouseOver;

	/**
	 * Route mouse moved events to overlay objects
	 */
	void overlayMouseMoved(MouseEvent e) {
		Point2D p = convertPoint(e.getPoint());

		//System.out.println("overlay mouse moved \n" + p);

		int count = overlays.size();
		boolean hadhit = false;
		for (int i = 0; !e.isConsumed() && i < count; i++) {
			ToolOverlay ti = overlays.get(i);

			if (ti.isMouseOver(p)) {
				hadhit = true;
				if (ti != mouseOver) {
					if (mouseOver != null) {
						mouseOver.mouseExited(e);
					}
					mouseOver = ti;
					mouseOver.mouseEntered(e);
				}
				ti.mouseMoved(e);
			}
		}
		if (!hadhit && mouseOver != null) {
			mouseOver.mouseExited(e);
			mouseOver = null;
		}
	}

	void overlayMouseDragged(MouseEvent e) {
		if (mouseOver != null) {
			mouseOver.mouseDragged(e);
		}
	}

	void overlayMousePressed(MouseEvent e) {
		Point2D p = convertPoint(e.getPoint());

		// button state?

		int count = overlays.size();
		for (int i = count - 1; !e.isConsumed() && i >= 0; i--) {
			ToolOverlay ti = overlays.get(i);

			if (ti.isMouseOver(p)) {
				if (ti != mouseOver) {
					if (mouseOver != null) {
						mouseOver.mouseExited(e);
					}
					mouseOver = ti;
					mouseOver.mouseEntered(e);
				}
				ti.mousePressed(e);
			}
		}
	}

	void overlayMouseReleased(MouseEvent e) {
		if (mouseOver != null) {
			mouseOver.mouseReleased(e);
		}

		// button state?
	}

	public void removeOverlays(List<ToolOverlay> handles) {
		for (ToolOverlay t : handles) {
			removeOverlay(t);
		}
	}

	public void addOverlays(LinkedList<ToolOverlay> handles) {
		for (ToolOverlay t : handles) {
			addOverlay(t);
		}
	}

	void setShowSelection(boolean selected) {
		if (this.showSelection != selected) {
			this.showSelection = selected;
			repaint();
		}
	}

	public boolean isShowSelection() {
		return showSelection;
	}
}
