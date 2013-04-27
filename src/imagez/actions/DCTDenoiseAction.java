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

import imagez.fx.ui.DCTDenoiseTool;
import imagez.fx.ui.RequesterFrame;
import imagez.ui.ImageWindow;
import java.awt.event.ActionEvent;

/**
 *
 * @author notzed
 */
public class DCTDenoiseAction  extends AbstractWindowAction {
	public DCTDenoiseAction() {
		super("DCT Denoise");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ImageWindow win = getWindow(e);
		DCTDenoiseTool bt = new DCTDenoiseTool(win.getImageView());

		new RequesterFrame("DCT Denoise", win, bt, bt).setVisible(true);
	}
}
