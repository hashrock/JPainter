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

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * This is hightly modified and used to add custom widget behaviour:
 * 
 *  - amiga-style menus
 *  - (sort of amiga-style) embedded dialogs
 * 
 * Original documentation follows:
 * 
 * GlassPane tutorial
 * "A well-behaved GlassPane"
 * http://weblogs.java.net/blog/alexfromsun/
 * http://weblogs.java.net/blog/alexfromsun/archive/2006/09/a_wellbehaved_g.html
 * <p/>
 * This is the final version of the GlassPane
 * it is transparent for MouseEvents,
 * and respects underneath component's cursors by default,
 * it is also friendly for other users,
 * if someone adds a mouseListener to this GlassPane
 * or set a new cursor it will respect them
 *
 * @author Alexander Potochkin
 */
public class AGlassPane extends JPanel implements AWTEventListener {

	private final JFrame frame;
	private Point point = new Point();
	AMenuBar menu;

	public AGlassPane(JFrame frame, AMenuBar menu) {
		super(null);
		this.frame = frame;
		this.menu = menu;
		setOpaque(false);
	}
	/*
	AncestorListener al = new AncestorListener() {
	
	@Override
	public void ancestorAdded(AncestorEvent event) {
	System.out.println("a added");
	}
	
	@Override
	public void ancestorRemoved(AncestorEvent event) {
	System.out.println("amreoved " + event.getAncestorParent());
	menu.removeAncestorListener(this);
	}
	
	@Override
	public void ancestorMoved(AncestorEvent event) {
	System.out.println("amoved");
	}
	};
	 * 
	 */

	@Override
	public void eventDispatched(AWTEvent event) {
		if (event instanceof MouseEvent) {
			MouseEvent me = (MouseEvent) event;
			if (!SwingUtilities.isDescendingFrom(me.getComponent(), frame)
					|| me.isConsumed()) {
				return;
			}
			if (me.getButton() == me.BUTTON3) {
				// HACK: currently just consume all button-3 events.
				me.consume();
				if (me.getClickCount() == 1
						&& me.getModifiersEx() == (me.BUTTON3_DOWN_MASK)) {
					// TODO: perhaps - check if we're over a component that has a popup menu, and if so,
					// let the popup menu run?
					// if (target instanceof JComponent && ((JComponent)target).getComponentPopupMenu() != null)

					AMenuBar.popupMenu(frame, menu);
					//	menu.addAncestorListener(al);
					//	menu.show(frame);
					//		dragTarget = null;  <- does bad stuff
				}
			}
		} else if (event instanceof KeyEvent) {
			KeyEvent ke = (KeyEvent) event;
			if (!SwingUtilities.isDescendingFrom(ke.getComponent(), frame)
					|| ke.isConsumed()) {
				return;
			}
			if (frame instanceof ImageWindow) {
				ImageWindow win = (ImageWindow) frame;
				if (win.getOverlay() != null) {
					if (ke.getKeyCode() == KeyEvent.VK_SPACE
							|| ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
						ke.consume();
						win.setOverlay(null);
					}
				}
			}
		}
	}

	/**
	 * If someone adds a mouseListener to the GlassPane or set a new cursor
	 * we expect that he knows what he is doing
	 * and return the super.contains(x, y)
	 * otherwise we return false to respect the cursors
	 * for the underneath components
	 */
	@Override
	public boolean contains(int x, int y) {
		if (getMouseListeners().length == 0
				&& getMouseMotionListeners().length == 0
				&& getMouseWheelListeners().length == 0
				&& getCursor() == Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) {
			return false;
		}
		return super.contains(x, y);
	}
}
