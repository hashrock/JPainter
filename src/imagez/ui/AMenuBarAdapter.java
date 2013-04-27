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

import java.awt.Component;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 * Convenience class for adding an AMenuBar to a component.
 *
 * This needs some more state tracking.
 * e.g. if you are dragging something and hit the rmb, it pops up the menu and loses the drag.
 *
 * Here's a better solution:
 * // http://weblogs.java.net/blog/alexfromsun/archive/2006/09/a_wellbehaved_g.html
 * @author notzed
 */
public class AMenuBarAdapter implements MouseListener, MouseMotionListener {
	Component glassPane;
	Component contentPane;
	AMenuBar menu;
	JFrame frame;
	boolean enabled = false;
	// component receiving drag events
	Component dragTarget;
	// button that caused the drag
	int dragButton;

	AMenuBarAdapter(JFrame frame, AMenuBar menu) {
		this.frame = frame;
		glassPane = frame.getGlassPane();
		contentPane = frame.getContentPane();
		this.menu = menu;
	}

	Component redispatch(MouseEvent e) {
		Point at = SwingUtilities.convertPoint(glassPane, e.getPoint(), contentPane);

		if (at.y < 0) {
			//System.out.println("event out of range?");
			// off window
		} else {
			Component target = SwingUtilities.getDeepestComponentAt(frame.getLayeredPane(), at.x, at.y);

			//		target = null;
			if (target == null) {
				target = SwingUtilities.getDeepestComponentAt(contentPane, at.x, at.y);
			} else {
				//System.out.println(" over layered pane: " + target);
			}

			if (target != null) {
				//System.out.println("over " + target.getClass().getName());
				at = SwingUtilities.convertPoint(glassPane, at, target);
				target.dispatchEvent(new MouseEvent(target, e.getID(), e.getWhen(), e.getModifiers(), at.x, at.y, e.getClickCount(), e.isPopupTrigger()));
				e.consume();
				return target;
			} else {
				System.out.println("nothing under mouse?");
			}
		}
		return null;
	}

	void redispatch(MouseEvent e, Component target) {
		if (target == null) {
			redispatch(e);
		} else {
			Point at = e.getPoint();

			//System.out.println("sending to " + target);

			at = SwingUtilities.convertPoint(glassPane, at, target);
			target.dispatchEvent(new MouseEvent(target, e.getID(), e.getWhen(), e.getModifiers(), at.x, at.y, e.getClickCount(), e.isPopupTrigger()));
			e.consume();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		System.out.println("mouse clicked");
		redispatch(e);
	}
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

	@Override
	public void mousePressed(MouseEvent e) {
		System.out.println("mouse pressed event");
		if (e.getButton() == MouseEvent.BUTTON3) {
			if (dragTarget == null) {
				e.consume();
				// TODO: perhaps - check if we're over a component that has a popup menu, and if so,
				// let the popup menu run?
				// if (target instanceof JComponent && ((JComponent)target).getComponentPopupMenu() != null)

				// FIXME: hack to track image window properly
				if (frame instanceof ImageWindow) {
					//ToolModel.getInstance().setImageWindow((ImageWindow) frame);
				}

				menu.addAncestorListener(al);
				menu.show(frame);
				//		dragTarget = null;  <- does bad stuff
			}
		} else {
			dragTarget = redispatch(e);
			if (dragTarget != null) {
				//System.out.println(" sending to " + dragTarget);
				dragButton = e.getButton();
			} else {
				//System.out.println(" no target found");
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		System.out.println("mouse released ");
		if (dragTarget != null) {
			// FIXME: this doesn't work properly
			redispatch(e, dragTarget);
			if (e.getButton() == dragButton) {
				dragTarget = null;
			}
		} else {
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// track enter target?
		redispatch(e);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		redispatch(e);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		//System.out.println("mouse dragged");
		redispatch(e, dragTarget);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		//System.out.println("mouse moved");
		redispatch(e);
	}
}
