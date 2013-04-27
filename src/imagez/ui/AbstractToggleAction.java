/*
 * Copyright (C) 2011 notzed
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package imagez.ui;

import javax.swing.AbstractAction;

/**
 * Actions that can link to 'toggle' buttons.
 * @author notzed
 */
public abstract class AbstractToggleAction extends AbstractAction {

	boolean selected = true;

	public AbstractToggleAction(String name) {
		super(name);
	}

	@Override
	public Object getValue(String key) {
		if (key.equals(SELECTED_KEY)) {
			return selected;
		}
		return super.getValue(key);
	}

	@Override
	public void putValue(String key, Object newValue) {
		if (key.equals(SELECTED_KEY)) {
			selected = (Boolean) newValue;
		}
		super.putValue(key, newValue);
	}
}
