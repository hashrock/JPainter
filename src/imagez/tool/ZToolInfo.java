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

import imagez.TestLayers;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

/**
 *
 * @author notzed
 */
public class ZToolInfo {

	public final String name;
	public final String type;
	public final Class klass;
	public final Icon icon;
	public final KeyStroke accel;

	public ZToolInfo(String name, String type, Class klass, String icon, String accel) {
		this.name = name;
		this.type = type;
		this.klass = klass;
		this.icon = icon == null ? null : getIcon(icon);
		this.accel = accel == null ? null : KeyStroke.getKeyStroke(accel);
	}

	static Icon getIcon(String key) {
		try {
			InputStream is = ZToolInfo.class.getResourceAsStream("/imagez/icons/" + key);
			return new ImageIcon(ImageIO.read(is));
		} catch (IOException ex) {
			Logger.getLogger(TestLayers.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}

	}
	static final HashMap<String, ZToolInfo> tools;

	static {
		tools = new HashMap<String, ZToolInfo>();
		tools.put("Draw", new ZToolInfo("Draw", "paint", DrawTool.class, "tool-brush.png", "typed d"));
	//	tools.put("Brush", new ZToolInfo("Brush", "paint", BrushTool.class, "tool-brush.png", "typed p"));
	//	tools.put("Felt", new ZToolInfo("Felt", "paint", FeltTool.class, "tool-felt-pen.png", "typed n"));

		tools.put("Rectangle", new ZToolInfo("Rectangle", "selection", RectangleSelectionTool.class, "tool-select-rectangle.png", "typed r"));
		tools.put("Ellipse", new ZToolInfo("Ellipse", "selection", EllipseSelectionTool.class, "tool-select-rectangle.png", "typed e"));
		
		tools.put("Affine", new ZToolInfo("Affine", "editing", SuperAffineTool.class, "tool-select-rectangle.png", "typed a"));
		tools.put("Crop", new ZToolInfo("Crop", "editing", CropTool.class, "tool-select-rectangle.png", "typed C"));
		
		tools.put("Text", new ZToolInfo("Text", "editing", TextTool.class, "tool-select-rectangle.png", "typed T"));
	}

	public static ZToolInfo [] getToolInfo(String type) {
		ArrayList<ZToolInfo> tis = new ArrayList<ZToolInfo>();

		for (ZToolInfo t : tools.values()) {
			if (t.type.equals(type))
				tis.add(t);
		}
		
		return tis.toArray(new ZToolInfo[0]);
	}
	
	public static ZTool createDefaultTool() {
		return createTool("Draw");
	}
	
	public static ZTool createTool(String name) {
		ZTool tool = null;

		try {
			ZToolInfo c = tools.get(name);
			if (c == null) {
				throw new RuntimeException("No such tool: " + name);
			}

			tool = (ZTool) c.klass.getConstructor().newInstance();
		} catch (InstantiationException ex) {
			Logger.getLogger(ZToolInfo.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			Logger.getLogger(ZToolInfo.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IllegalArgumentException ex) {
			Logger.getLogger(ZToolInfo.class.getName()).log(Level.SEVERE, null, ex);
		} catch (InvocationTargetException ex) {
			Logger.getLogger(ZToolInfo.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NoSuchMethodException ex) {
			Logger.getLogger(ZToolInfo.class.getName()).log(Level.SEVERE, null, ex);
		} catch (SecurityException ex) {
			Logger.getLogger(ZToolInfo.class.getName()).log(Level.SEVERE, null, ex);
		}

		return tool;
	}
}
