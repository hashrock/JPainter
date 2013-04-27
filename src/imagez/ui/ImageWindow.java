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

import imagez.image.ZImage;
import imagez.image.ZLayer;
import imagez.tool.ZTool;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;

/**
 * @author notzed
 */
public class ImageWindow extends javax.swing.JFrame {

	JPanel mainPanel;
	private imagez.ui.ImageView imageView;
	private javax.swing.JScrollPane jScrollPane1;
	JPanel toolbar;
	LayerView layers;
	// tool area
	ToolbarSection toolSection;
	// temp hack to test idea
	JButton paletteButton;

	JScrollBar stackID;
	ArrayList<ZImage> stack = new ArrayList<ZImage>();
	
	/**
	 * In addition to the tool tracking for the drawing
	 * surface, track the current window.
	 */
	class WindowToolAdapter extends ToolAdapter {

		ImageWindow win;

		public WindowToolAdapter(ImageWindow win) {
			super(win.imageView);
			this.win = win;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			//ToolModel.getInstance().setImageWindow(win);
			super.mousePressed(e);
		}
	}

	public ImageWindow(ZImage image) {
		this();

		setImage(image);
	}

	public ImageWindow() {
		//setUndecorated(true);
		//getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
		//setDefaultLookAndFeelDecorated(false);
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		jScrollPane1 = new javax.swing.JScrollPane();
		imageView = new imagez.ui.ImageView(this);

		setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
		setTitle("ImageZ");

		jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		jScrollPane1.setViewportView(imageView);
		mainPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

		toolbar = new JPanel();
		toolbar.setBackground(Color.white);
		toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.PAGE_AXIS));
		mainPanel.add(toolbar, BorderLayout.WEST);

		stackID = new JScrollBar(JScrollBar.HORIZONTAL, 0, 1, 0, 2);
		stackID.setVisible(false);
		mainPanel.add(stackID, BorderLayout.SOUTH);
		
		getContentPane().add(mainPanel);

		/*
		toolbar.add(new ToolbarLabel("Layers"));
		layers = new LayerView();
		toolbar.add(layers);
		layers.setAlignmentX(0);
		 *
		 */
		layers = new LayerView();

		ToolbarSection sec;
		
		
		sec = new ToolbarSection("Layers", layers);
		//sec.content.setBackground(Color.CYAN);
		toolbar.add(sec);

		toolSection = new ToolbarSection("Tool", null);
		toolbar.add(toolSection);

		sec = new ToolbarSection("Brush", imageView.brushContext.getWidget(imageView));
		//sec.content.setBackground(Color.green);
		toolbar.add(sec);

		toolbar.add(Box.createVerticalGlue());
		toolbar.add(Box.createHorizontalStrut(150));

		/*
		JPanel testStuff = new JPanel();
		paletteButton = new JButton("click me");
		paletteButton.addMouseListener(new MouseAdapter() {
		
		@Override
		public void mousePressed(MouseEvent e) {
		if (e.getButton() == e.BUTTON1) {
		popupPalette();
		}
		}
		});
		testStuff.add(paletteButton);
		
		sec = new ToolbarSection("Test", testStuff);
		toolbar.add(sec);
		 *
		 */
		ImageData imageData = new ImageData(imageView);
		sec = new ToolbarSection("Info", imageData);
		sec.content.setBackground(Color.WHITE);
		toolbar.add(sec);

		pack();

		InputMap imap = imageView.getInputMap(imageView.WHEN_IN_FOCUSED_WINDOW);
		ActionMap amap = imageView.getActionMap();

		/*
		imap.put(KeyStroke.getKeyStroke(' '), "show-pox");
		amap.put("show-pox", ShowLastPoxAction.getInstance());
		imap.put(KeyStroke.getKeyStroke("F1"), "show-paint");
		amap.put("show-paint", ShowPaintPoxAction.getInstance());
		imap.put(KeyStroke.getKeyStroke("F2"), "show-selection");
		amap.put("show-selection", ShowGrabPoxAction.getInstance());
		 */

		// this needs work, quite a lot
		AMenuBar amenu = ImageMenu.getImageMenu();
		AMenuBar.attachMenu(this, amenu);

		// hack to link up accelerators
		addAccelerators(amenu);

		WindowToolAdapter ta = new WindowToolAdapter(this);

		layers.addPropertyChangeListener(LayerView.PROP_CURRENTLAYER, new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				imageView.setCurrentLayer((ZLayer) evt.getNewValue());
			}
		});

		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				close();
			}
		});

		/*
		ToolModel.getInstance().addPropertyChangeListener(new PropertyChangeListener() {
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
		String name = evt.getPropertyName();
		
		if (name.equals(ToolModel.PROP_TOOL)) {
		System.out.println("tool changed");
		ZTool tool = (ZTool) evt.getNewValue();
		
		toolSection.setContent((JComponent) tool.getWidget());
		toolSection.tb.setText(tool.getName());
		}
		}
		});
		 * 
		 */

		imageView.addPropertyChangeListener(ImageView.PROP_TOOL, new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				System.out.println("tool changed");
				ZTool tool = (ZTool) evt.getNewValue();

				toolSection.setContent((JComponent) tool.getWidget());
				toolSection.tb.setText(tool.getName());
			}
		});
	}

	void popupPaletteX() {
		System.out.println("popup ?");
		Point pos = paletteButton.getLocation();
		JPanel jp = new JPanel();
		final JColorChooser jc = new JColorChooser();

		jc.setPreviewPanel(new JPanel());

		pos = SwingUtilities.convertPoint(paletteButton, pos, this);

		Dimension d = jc.getPreferredSize();

		jp.setLayout(new BorderLayout());
		jp.add(jc);

		int y = Math.min(pos.y, getHeight() - d.height);

		jp.setBounds(pos.x, y, d.width, d.height);
		jp.setVisible(true);
		getLayeredPane().add(jp);

		jp.addMouseListener(new MouseListener() {

			@Override
			public void mouseEntered(MouseEvent e) {
				System.out.println("mouse enter " + e.getSource());
			}

			@Override
			public void mouseExited(MouseEvent e) {
				System.out.println("mouse exited " + e.getSource());
				//p.hide();
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				throw new UnsupportedOperationException("Not supported yet.");
			}

			@Override
			public void mousePressed(MouseEvent e) {
				throw new UnsupportedOperationException("Not supported yet.");
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		});
	}

	void popupPalette() {
		Point pos = paletteButton.getLocation();
		pos = SwingUtilities.convertPoint(paletteButton, pos, this);
		final JColorChooser jc = new JColorChooser();
		jc.setPreviewPanel(new JPanel());

		JPopupMenu jm = new JPopupMenu();
		jm.add(jc);
		jm.show(this, pos.x, pos.y);


		//final Popup p = PopupFactory.getSharedInstance().getPopup(this, jc, pos.x+paletteButton.getWidth()/2, pos.y);
		//p.show();

		jm.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent e) {
				System.out.println("mouse enter " + e.getSource());
			}

			@Override
			public void mouseExited(MouseEvent e) {
				System.out.println("mouse exited " + e.getSource());
				//p.hide();
			}
		});

		//p.show();
	}

	private void addAccelerators(MenuElement amenu) {

		if (amenu instanceof JMenuItem) {
			JMenuItem ji = (JMenuItem) amenu;
			Action a = ji.getAction();
			if (a != null) {
				KeyStroke ks = (KeyStroke) a.getValue(Action.ACCELERATOR_KEY);
				if (ks != null) {
					InputMap imap = imageView.getInputMap(imageView.WHEN_IN_FOCUSED_WINDOW);
					ActionMap amap = imageView.getActionMap();

					imap.put(ks, a);
					amap.put(a, a);
				}
			}
		}
		for (MenuElement me : amenu.getSubElements()) {
			addAccelerators(me);
		}
	}

	public void setImage(ZImage image) {
		imageView.setImage(image);
		imageView.invalidate();
		pack();

		layers.setImage(image);
		if (image != null) {
			layers.setCurrentLayer(image.getLayer(0));
		} else {
			layers.setCurrentLayer(null);
		}
	}

	// HACK: called by imageview, should listen directly?
	// FIXME: doesn't work properly anyway.
	void imageChanged() {
		imageView.invalidate();
		pack();
	}

	public boolean isEmpty() {
		return imageView.getImage() == null;
	}

	public ImageView getImageView() {
		return imageView;
	}

	public void setShowToolbar(boolean show) {
		toolbar.setVisible(show);
	}
	JComponent overlay;

	/**
	 * Set the overlay tool.
	 * TODO: this should use the 'poxycontroller' stuff at some point.
	 * @param overlay 
	 */
	public void setOverlay(JComponent overlay) {
		if (this.overlay != null) {
			getLayeredPane().remove(this.overlay);
		}
		this.overlay = overlay;
		if (overlay != null) {
		//	Rectangle r = overlay.getBounds();
		//	r.x = 200 + 8;
		//	r.y = 8;
			Point pos = overlay.getLocation();
			Dimension d = overlay.getPreferredSize();
			
			pos.x = Math.min(pos.x, getWidth() - d.width);
			pos.y = Math.min(pos.y, getHeight() - d.height);
			pos.x = Math.max(158, pos.x);
			pos.y = Math.max(0, pos.y);
			System.out.println("set overlay pos " + pos);
			overlay.setLocation(pos);
	//		overlay.setLocation(toolbar.isVisible() ? 150+8 : 8, 8);
		//	overlay.setBounds(r);
			getLayeredPane().add(overlay, 1);
		}
		getLayeredPane().repaint();
	}

	public JComponent getOverlay() {
		return overlay;
	}

	public void close() {

		System.out.println("Closing window " + this);
		// TODO: add an 'are you sure' if it was modified

		int opened = 0;
		Window[] wins = ImageWindow.getOwnerlessWindows();
		for (Window w : wins) {
			if (w instanceof ImageWindow && w.isDisplayable()) {
				opened++;
			}
		}

		// If last window to close, first zero all content, and then quit
		if (opened == 1) {
			if (isEmpty()) {
				System.exit(0);
			} else {
				setImage(null);
			}
		} else {
			dispose();
		}
	}
}
