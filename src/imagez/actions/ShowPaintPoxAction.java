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

import imagez.ui.PoxyPaintBox;
import java.awt.event.ActionEvent;
import javax.swing.KeyStroke;

/**
 *
 * @author notzed
 */
public class ShowPaintPoxAction extends ShowPoxAction {

	static PoxyPaintBox paint;
	static ShowPaintPoxAction self;
	public static ShowPaintPoxAction getInstance() {
		if (self == null)
			self = new ShowPaintPoxAction();
		return self;
	}


	private ShowPaintPoxAction() {
		super("PaintPox");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F1"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (paint == null)
			paint = new PoxyPaintBox();
		switchPox(e, paint);
	}
}
