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
import java.awt.event.ActionEvent;
import javax.swing.KeyStroke;

/**
 *
 * @author notzed
 */
public class CloseAction extends AbstractWindowAction {

	public CloseAction() {
		super("Close", null);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl W"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ImageWindow win = getWindow(e);

		if (win == null) {
			System.out.println("close got no window, which is of course impossible");
			return;
		}

		win.close();
	}
}
