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

import imagez.fx.ui.BrightnessContrast;
import imagez.fx.ui.RequesterFrame;
import imagez.ui.ImageWindow;
import java.awt.event.ActionEvent;

/**
 *
 * @author notzed
 */
public class BrightnessContrastAction  extends AbstractWindowAction {
	public BrightnessContrastAction() {
		super("Brightness & Contrast");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ImageWindow win = getWindow(e);
		BrightnessContrast bc = new BrightnessContrast(win.getImageView());

		new RequesterFrame("Brightness & Contrast", win, bc, bc).setVisible(true);
	}
}
