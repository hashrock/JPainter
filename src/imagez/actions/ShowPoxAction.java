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

import imagez.ui.ImageWindow;
import imagez.ui.PoxyBoxOld;
import java.awt.Point;
import java.awt.event.ActionEvent;
import javax.swing.SwingUtilities;

/**
 *
 * @author notzed
 */
abstract public class ShowPoxAction extends AbstractWindowAction {

	static protected PoxyBoxOld lastPox;

	/**
	 * Switch to another poxybox or toggle the current one.
	 *
	 * TODO: perhaps if a poxybox is already visible the new
	 * one should pop up in the same location.
	 * @param pox
	 */
	static protected void switchPox(ActionEvent e, PoxyBoxOld pox) {
		Point p;

		if (lastPox != null && lastPox.isVisible()) {
			p = lastPox.getLocationOnScreen();
			lastPox.setVisible(false);
			if (lastPox == pox) {
				return;
			}
		} else {
			ImageWindow win = getWindow(e);
			p = win.getMousePosition(true);
			if (p != null) {
				SwingUtilities.convertPointToScreen(p, win);

				//p.translate(-128, -128 - 64);
				p.translate(-pox.getWidth()/2, -pox.getHeight()/2);
			} else {
				p = win.getLocationOnScreen();
			}
			if (p.x < 0) {
				p.x = 0;
			}
			if (p.y < 0) {
				p.y = 0;
			}
		}
		lastPox = pox;

		pox.setLocation(p);
		pox.validate();
		pox.setVisible(!pox.isVisible());
	}

	public ShowPoxAction(String title) {
		super(title);
	}
}
