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

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 * Implements a menu bar which operates somewhat like one from
 * an earlier more golden era.
 *
 * Most easily used with an amenubaradapter.
 * @author notzed
 */
public class AMenuBar extends JMenuBar {

	Popup popup;

	public AMenuBar() {
	}
	static AncestorListener al = new AncestorListener() {

		@Override
		public void ancestorAdded(AncestorEvent event) {
			System.out.println("a added");
		}

		@Override
		public void ancestorRemoved(AncestorEvent event) {
			System.out.println("amreoved " + event.getAncestorParent());
			if (shownMenu != null) {
				shownMenu.removeAncestorListener(this);
			}
		}

		@Override
		public void ancestorMoved(AncestorEvent event) {
			System.out.println("amoved");
		}
	};
	static AMenuBar shownMenu;
	static Component shownOwner;

	public static Component getActiveOwner() {
		return shownOwner;
	}

	public static AMenuBar getActiveMenu() {
		return shownMenu;
	}

	public static void popupMenu(Component invoker, AMenuBar bar) {
		if (shownMenu != null) {
			shownMenu.removeAncestorListener(al);
			System.out.printf("Warning: showing 2 menu's, not possible?");
		}
		shownOwner = invoker;
		shownMenu = bar;
		shownMenu.addAncestorListener(al);
		bar.show(invoker);
	}

	public void show(Component invoker) {
		PopupFactory pf = PopupFactory.getSharedInstance();

		popup = pf.getPopup(invoker, this, Math.max(0, invoker.getX()), Math.max(0, invoker.getY()));
		popup.show();

		MenuElement path[] = new MenuElement[1];
		path[0] = (MenuElement) this;
		MenuSelectionManager.defaultManager().setSelectedPath(path);
	}

	@Override
	public void menuSelectionChanged(boolean isIncluded) {
		super.menuSelectionChanged(isIncluded);

		if (MenuSelectionManager.defaultManager().getSelectedPath().length == 0) {
			if (popup != null) {
				popup.hide();
				popup = null;
			}
		}
	}

	/**
	 * Attach an AMenuBar to a frame.  Obviously only one may be attached to a given window.
	 * @param frame
	 * @param menu
	 */
	static public void attachMenu(JFrame frame, AMenuBar menu) {
		AGlassPane gp = new AGlassPane(frame, menu);

		frame.setGlassPane(gp);

		gp.setVisible(true);
		Toolkit.getDefaultToolkit().addAWTEventListener(gp, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);

		//AMenuBarAdapter ab = new AMenuBarAdapter(frame, menu);
		//ab.glassPane.setVisible(true);
		//ab.glassPane.addMouseListener(ab);
		//ab.glassPane.addMouseMotionListener(ab);
	}
}
