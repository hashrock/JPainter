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
package imagez.tool;

import imagez.image.ZSelectionModel;
import imagez.tool.ui.SelectionOptions;
import java.awt.Component;
import java.awt.event.MouseEvent;

/**
 * Base class for selection tools.
 *
 * Handles mouse drag (maybe this should be in a 'range selection tool'.
 *
 * TODO: add control handles for
 * @author notzed
 */
abstract public class SelectionTool extends ZDragTool {


	public SelectionTool() {
	}
	protected int mode = ZSelectionModel.MODE_REPLACE;
	public static final String PROP_MODE = "mode";

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		int oldMode = this.mode;
		this.mode = mode;
		propertyChangeSupport.firePropertyChange(PROP_MODE, oldMode, mode);
	}

	@Override
	public Component getWidget() {
		return new SelectionOptions(this);
	}

	abstract public void clearSelection();
	
	@Override
	public void mousePressed(MouseEvent e) {
		if (e.isShiftDown() && e.isControlDown()) {
			mode = ZSelectionModel.MODE_XOR;
		} else if (e.isShiftDown()) {
			mode = ZSelectionModel.MODE_UNION;
		} else if (e.isAltDown()) {
			mode = ZSelectionModel.MODE_INTERSECT;
		} else if (e.isControlDown()) {
			mode = ZSelectionModel.MODE_SUBTRACT;
		} else {
			mode = ZSelectionModel.MODE_REPLACE;
		}
	}
}
