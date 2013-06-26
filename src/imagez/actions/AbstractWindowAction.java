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
package imagez.actions;

import imagez.ui.ImageView;
import imagez.ui.ImageWindow;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

/**
 * Adds some helpers so actions can find their targets.
 * @author notzed
 */
abstract public class AbstractWindowAction extends AbstractAction {

	public AbstractWindowAction(String name, Icon icon) {
		super(name, icon);
	}

	public AbstractWindowAction(String name) {
		super(name);
	}

	static ImageWindow getWindow(ActionEvent e) {
		ImageWindow win;
                

		if (e.getSource() instanceof ImageView) {
                    win = (ImageWindow) ((ImageView) e.getSource()).getTopLevelAncestor();
		} else if (e.getSource() instanceof JMenuItem) {
                    //メニューの親を返却
                    JMenuItem menuItem = (JMenuItem) e.getSource();
                    JPopupMenu popupMenu = (JPopupMenu) menuItem.getParent();
                    Component invoker = popupMenu.getInvoker(); //this is the JMenu
                    JComponent invokerAsJComponent = (JComponent) invoker;
                    Container topLevel = invokerAsJComponent.getTopLevelAncestor();
                    win = (ImageWindow)topLevel;
		} else
			win = null;

		return win;
	}
}
