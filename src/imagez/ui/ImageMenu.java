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
package imagez.ui;

import imagez.actions.*;
import imagez.image.ZLayerType;
import imagez.tool.ZToolInfo;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

/**
 *
 * @author notzed
 */
public class ImageMenu {

    static AMenuBar imageMenu;

    public static AMenuBar getJMenu() {
        AMenuBar mb = new AMenuBar();
        JMenu menu = new JMenu("File");
        mb.add(menu);
        menu.add(new JMenuItem(new NewAction()));
        menu.add(new JMenuItem(new SnapWindowAction()));
        menu.add(new JMenuItem(new SnapClipboardAction()));
        menu.add(new JSeparator());
        menu.add(new JMenuItem(new OpenAction()));
        menu.add(new JMenuItem(new SaveAction()));
        menu.add(new JSeparator());
        menu.add(new JMenuItem(new CloseAction()));
        menu.add(new JMenuItem(new QuitAction()));

        menu = new JMenu("Edit");
        mb.add(menu);
        menu.add(new JMenuItem(new UndoAction()));
        menu.add(new JMenuItem(new RedoAction()));
        menu.add(new JSeparator());
        menu.add(new JMenuItem(new CopyAction()));
        menu.add(new JMenuItem(new PasteAction()));

        menu = new JMenu("Select");
        mb.add(menu);
        menu.add(new JMenuItem(new SelectAllAction()));
        menu.add(new JMenuItem(new SelectNoneAction()));
        menu.add(new JMenuItem(new SelectInvertAction()));

        menu = new JMenu("View");
        mb.add(menu);
        JCheckBoxMenuItem cb = new JCheckBoxMenuItem();
        cb.setSelected(true);
        cb.setAction(new ShowToolbarAction(cb));
        menu.add(cb);
        menu.add(new JMenuItem(ShowLastPoxAction.getInstance()));
        menu.add(new JMenuItem(ShowPaintPoxAction.getInstance()));
        menu.add(new JMenuItem(ShowGrabPoxAction.getInstance()));

        mb.add(menu = new JMenu("Image"));
        menu.add(new JMenuItem(new ResizeImageAction()));

        mb.add(menu = new JMenu("Layers"));
        menu.add(new JMenuItem(new ResizeLayerAction()));
        JMenu sub;
        menu.add(sub = new JMenu("Layer Type"));
        for (ZLayerType t : ZLayerType.values()) {
            sub.add(t.toString());
        }

        menu = new JMenu("Tools");
        mb.add(menu);
        addTools(menu, ZToolInfo.getToolInfo("paint"));
        //addTools(menu, ToolModel.getTools("paint"));
        menu.add(new JSeparator());
        //addTools(menu, ToolModel.getTools("structured"));
        menu.add(new JSeparator());
        addTools(menu, ZToolInfo.getToolInfo("selection"));
        //addTools(menu, ToolModel.getTools("selection"));
        menu.add(new JSeparator());
        addTools(menu, ZToolInfo.getToolInfo("editing"));

        menu = new JMenu("Filters");
        mb.add(menu);
        menu.add(new JMenuItem(new BrightnessContrastAction()));
        menu.add(new JMenuItem(new GaussianBlurAction()));
        menu.add(new JMenuItem(new WienerDeconvolutionAction()));
        menu.add(new JMenuItem(new UnsharpMaskAction()));
        menu.add(new JMenuItem(new DCTDenoiseAction()));
        menu.add(new JMenuItem(new QWTDenoiseAction()));

        return mb;
    }

    private static AMenuBar getMenu() {
        AMenuBar mb = new AMenuBar();
        JMenu menu = new JMenu("File");
        mb.add(menu);
        menu.add(new JMenuItem(new NewAction()));
        menu.add(new JMenuItem(new SnapWindowAction()));
        menu.add(new JMenuItem(new SnapClipboardAction()));
        menu.add(new JSeparator());
        menu.add(new JMenuItem(new OpenAction()));
        menu.add(new JMenuItem(new SaveAction()));
        menu.add(new JSeparator());
        menu.add(new JMenuItem(new CloseAction()));
        menu.add(new JMenuItem(new QuitAction()));

        menu = new JMenu("Edit");
        mb.add(menu);
        menu.add(new JMenuItem(new UndoAction()));
        menu.add(new JMenuItem(new RedoAction()));
        menu.add(new JSeparator());
        menu.add(new JMenuItem(new CopyAction()));
        menu.add(new JMenuItem(new PasteAction()));

        menu = new JMenu("Select");
        mb.add(menu);
        menu.add(new JMenuItem(new SelectAllAction()));
        menu.add(new JMenuItem(new SelectNoneAction()));
        menu.add(new JMenuItem(new SelectInvertAction()));

        menu = new JMenu("View");
        mb.add(menu);
        JCheckBoxMenuItem cb = new JCheckBoxMenuItem();
        cb.setSelected(true);
        cb.setAction(new ShowToolbarAction(cb));
        menu.add(cb);
        menu.add(new JMenuItem(ShowLastPoxAction.getInstance()));
        menu.add(new JMenuItem(ShowPaintPoxAction.getInstance()));
        menu.add(new JMenuItem(ShowGrabPoxAction.getInstance()));

        mb.add(menu = new JMenu("Image"));
        menu.add(new JMenuItem(new ResizeImageAction()));

        mb.add(menu = new JMenu("Layers"));
        menu.add(new JMenuItem(new ResizeLayerAction()));
        JMenu sub;
        menu.add(sub = new JMenu("Layer Type"));
        for (ZLayerType t : ZLayerType.values()) {
            sub.add(t.toString());
        }

        menu = new JMenu("Tools");
        mb.add(menu);
        addTools(menu, ZToolInfo.getToolInfo("paint"));
        //addTools(menu, ToolModel.getTools("paint"));
        menu.add(new JSeparator());
        //addTools(menu, ToolModel.getTools("structured"));
        menu.add(new JSeparator());
        addTools(menu, ZToolInfo.getToolInfo("selection"));
        //addTools(menu, ToolModel.getTools("selection"));
        menu.add(new JSeparator());
        addTools(menu, ZToolInfo.getToolInfo("editing"));

        menu = new JMenu("Filters");
        mb.add(menu);
        menu.add(new JMenuItem(new BrightnessContrastAction()));
        menu.add(new JMenuItem(new GaussianBlurAction()));
        menu.add(new JMenuItem(new WienerDeconvolutionAction()));
        menu.add(new JMenuItem(new UnsharpMaskAction()));
        menu.add(new JMenuItem(new DCTDenoiseAction()));
        menu.add(new JMenuItem(new QWTDenoiseAction()));

        return mb;
    }

    private static void addTools(JMenu menu, ZToolInfo[] tools) {
        for (ZToolInfo t : tools) {
            menu.add(new JMenuItem(new SelectToolAction(t.name, t.icon, t.accel)));
        }
    }

    public static AMenuBar getImageMenu() {
        if (imageMenu == null) {
            imageMenu = getMenu();
        }

        return imageMenu;
    }
}
