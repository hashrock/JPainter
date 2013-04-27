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

import imagez.tool.ZToolInfo;
import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.KeyStroke;

/**
 *
 * @author notzed
 */
public class SelectToolAction extends AbstractWindowAction {

	public SelectToolAction(String name, Icon icon, KeyStroke ks) {
		super(name, icon);
		if (ks != null) {
			putValue(ACCELERATOR_KEY, ks);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// FIXME: windows should have their own tool instances always?
		System.out.println("tool action performed!");
		getWindow(e).getImageView().setTool(ZToolInfo.createTool((String) getValue(NAME)));
	}
}
