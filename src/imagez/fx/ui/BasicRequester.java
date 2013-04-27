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
package imagez.fx.ui;

import java.awt.GridBagConstraints;
import java.awt.Insets;

/**
 *
 * @author notzed
 */
public interface BasicRequester {

	public void requestCancelled();

	public void requestOk();
	/**
	 * Standard gridbag layout constraint for column 0 'title' area
	 */
	public static GridBagConstraints c0 = new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 0, 0,
			GridBagConstraints.WEST, 0, new Insets(0, 0, 0, 0), 4, 2);
	/**
	 * Standard gridbag layout constraint for column 1 'item' area
	 */
	public static GridBagConstraints c1 = new GridBagConstraints(1, GridBagConstraints.RELATIVE, 1, 1, 1, 0,
			GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 4, 2);
	
	/**
	 * For 3 column data
	 */
	public static GridBagConstraints c2 = new GridBagConstraints(2, GridBagConstraints.RELATIVE, 1, 1, 1, 0,
			GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 4, 2);
}
