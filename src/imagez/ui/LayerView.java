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

import imagez.TestLayers;
import imagez.blend.BlendMode;
import imagez.image.ZImage;
import imagez.image.ZLayer;
import imagez.image.ZLayerTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

abstract class ZAction extends AbstractAction {

    String iconKey;

    public ZAction(String iconKey) {
        this.iconKey = iconKey;
    }

    @Override
    public Object getValue(String key) {
        if (key.equals(SMALL_ICON)) {
            try {
                InputStream is = this.getClass().getResourceAsStream("/imagez/icons/" + iconKey);
                return new ImageIcon(ImageIO.read(is));
            } catch (IOException ex) {
                Logger.getLogger(TestLayers.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return super.getValue(key);
    }
}

/**
 * Layer editor thing
 *
 * @author notzed
 */
public class LayerView extends JPanel implements ListSelectionListener, PropertyChangeListener {

    ZImage image;
    JTable layers;
    //
    JComboBox layerMode;
    JSpinner opacitySpinner;
    JSlider opacitySlider;
    //
    LayerAddAction addAction = new LayerAddAction();
    LayerRemoveAction removeAction = new LayerRemoveAction();
    LayerUpAction upAction = new LayerUpAction();
    LayerDownAction downAction = new LayerDownAction();
    LayerCopyAction copyAction = new LayerCopyAction();
    LayerPasteAction pasteAction = new LayerPasteAction();

    public LayerView() {
        JPanel panel = this;
        panel.setLayout(new BorderLayout());

        panel.setPreferredSize(new Dimension(150, 300));
        panel.setMaximumSize(new Dimension(150, 300));

        JPanel options = new JPanel();
        options.setOpaque(false);
        panel.add(options, BorderLayout.NORTH);
        options.setLayout(new GridBagLayout());

        GridBagConstraints c0 = new GridBagConstraints();
        c0.gridx = 0;
        c0.anchor = GridBagConstraints.BASELINE_LEADING;
        c0.ipadx = 3;
        c0.insets = new Insets(1, 2, 1, 2);
        options.add(new SmallLabel("Mode"), c0);
        options.add(new SmallLabel("Opacity"), c0);
        GridBagConstraints c1 = new GridBagConstraints();
        c1.gridx = 1;
        c1.insets = new Insets(1, 2, 1, 2);
        c1.weightx = 1;
        c1.anchor = GridBagConstraints.BASELINE_LEADING;
        c1.fill = GridBagConstraints.HORIZONTAL;

        layerMode = new SmallComboBox(new DefaultComboBoxModel(BlendMode.getBlendModes()));
        if (false) {
            // found a better way to do it, see AGlassPane
            // HACK: force full heavy weight for internal comboboxes so glasspane event handling works
            // see http://forums.sun.com/thread.jspa?forumID=57&threadID=5315492
            // see http://forums.sun.com/thread.jspa?threadID=5407999
            layerMode.setLightWeightPopupEnabled(false);
            try {
                Class cls = Class.forName("javax.swing.PopupFactory");
                Field field = cls.getDeclaredField("forceHeavyWeightPopupKey");
                field.setAccessible(true);
                layerMode.putClientProperty(field.get(null), Boolean.TRUE);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        layerMode.setMaximumRowCount(50);
        options.add(layerMode, c1);
        SpinnerNumberModel nm = new SpinnerNumberModel(Float.valueOf(1.0f), Float.valueOf(0.0f), Float.valueOf(1.0f), Float.valueOf(0.01f));
        opacitySpinner = new JSpinner(nm);
        options.add(opacitySpinner, c1);

        GridBagConstraints c01 = new GridBagConstraints();
        c01.gridx = 0;
        c01.gridwidth = 2;
        c01.anchor = GridBagConstraints.BASELINE_LEADING;
        c01.fill = GridBagConstraints.HORIZONTAL;

        opacitySlider = new JSlider(0, 100, 100);
        options.add(opacitySlider, c01);

        JPanel buttons = new JPanel();
        panel.add(buttons, BorderLayout.SOUTH);
        buttons.setLayout(new GridLayout(1, 0));

        buttons.add(makeButton(addAction, Color.blue));
        buttons.add(makeButton(upAction, Color.green));
        buttons.add(makeButton(downAction, Color.green));

        buttons.add(makeButton(copyAction, Color.yellow));
        buttons.add(makeButton(pasteAction, Color.yellow));

        buttons.add(makeButton(removeAction, Color.green));

        JScrollPane scroll = new JScrollPane();
        panel.add(scroll, BorderLayout.CENTER);

        JTable jt = new JTable();
        layers = jt;
        jt.setFont(SmallLabel.fontPlain);
        jt.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jt.setAutoCreateColumnsFromModel(false);
        jt.setTableHeader(null);
        jt.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        jt.setRowHeight(48);
        jt.setShowGrid(false);
        jt.setIntercellSpacing(new Dimension());

        TableColumn tc;
        tc = new TableColumn(0, 16, null, null);
        tc.setMaxWidth(16);
        tc.setMinWidth(16);
        jt.addColumn(tc);
        tc = new TableColumn(1, 16, null, null);
        tc.setMaxWidth(16);
        tc.setMinWidth(16);
        jt.addColumn(tc);
        tc = new TableColumn(2, 48, null, null);
        tc.setMaxWidth(48);
        tc.setMinWidth(48);
        jt.addColumn(tc);
        jt.addColumn(new TableColumn(3, 128, null, null));

        jt.getSelectionModel().addListSelectionListener(this);

        scroll.setViewportView(jt);

        opacitySpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Number n = (Number) opacitySpinner.getValue();
                if (currentLayer != null) {
                    currentLayer.setOpacity(n.floatValue());
                }
            }
        });

        opacitySlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                float a = opacitySlider.getValue() / 100.0f;
                if (currentLayer != null) {
                    currentLayer.setOpacity(a);
                }
            }
        });

        layerMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentLayer != null) {
                    currentLayer.setMode((BlendMode) layerMode.getSelectedItem());
                }
            }
        });

        // remove jtable cut/paste ... so that's where my keypresses kept going
        ActionMap map = jt.getActionMap();
        while (map != null) {
            map.remove(TransferHandler.getCopyAction().getValue(Action.NAME));
            map.remove(TransferHandler.getCutAction().getValue(Action.NAME));
            map.remove(TransferHandler.getPasteAction().getValue(Action.NAME));
            map = map.getParent();
        }

        setAble();
    }

    private JButton makeButton(Action a, Color colour) {
        JButton b = new JButton(a);
        b.setMargin(new Insets(0, 0, 0, 0));
        //b.setBackground(colour);
        return b;
    }

    public void setImage(ZImage image) {
        if (this.image != image) {
            this.image = image;
            if (image != null) {
                ZLayerTableModel model = new ZLayerTableModel(image);
                model.addTableModelListener(new TableModelListener() {
                    @Override
                    public void tableChanged(TableModelEvent e) {
                    }
                });
                layers.setModel(model);
            } else {
                layers.setModel(new DefaultTableModel());
            }
            setAble();
        }
    }

    public ZImage getImage() {
        return image;
    }
    protected ZLayer currentLayer;
    public static final String PROP_CURRENTLAYER = "currentLayer";

    public ZLayer getCurrentLayer() {
        return currentLayer;
    }

    public void setCurrentLayer(ZLayer currentLayer) {
        ZLayer oldCurrentLayer = this.currentLayer;
        int row = image != null ? Math.max(0, image.indexOf(currentLayer)) : -1;

        this.currentLayer = currentLayer;

        if (oldCurrentLayer != null) {
            oldCurrentLayer.removePropertyChangeListener(this);
        }
        if (currentLayer != null) {
            currentLayer.addPropertyChangeListener(this);
            opacitySpinner.setValue(currentLayer.getOpacity());
            opacitySlider.setValue((int) (currentLayer.getOpacity() * 100.0f));
        }

        layers.getSelectionModel().setSelectionInterval(row, row);
        firePropertyChange(PROP_CURRENTLAYER, oldCurrentLayer, currentLayer);

        setAble();
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            int row = layers.getSelectedRow();

            if (row >= 0) {
                setCurrentLayer(image.getLayerAt(row));
            }
        }
    }

    // Property on current layer
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();

        if (name.equals(ZLayer.PROP_OPACITY)) {
            Number n = (Number) evt.getNewValue();

            opacitySpinner.setValue(n);
            opacitySlider.setValue((int) (n.floatValue() * 100));
        } else if (name.equals(ZLayer.PROP_MODE)) {
            layerMode.setSelectedItem(evt.getNewValue());
        }
    }

    void setAble() {
        ZImage image = getImage();
        if (image != null) {
            int index = image.indexOf(getCurrentLayer());

            upAction.setEnabled(index > 0);
            downAction.setEnabled(index >= 0 && index < image.getLayerCount() - 1);
            addAction.setEnabled(true);
            removeAction.setEnabled(getCurrentLayer() != null);
        } else {
            upAction.setEnabled(false);
            downAction.setEnabled(false);
            addAction.setEnabled(false);
            removeAction.setEnabled(false);
        }
        copyAction.setEnabled(false);
        pasteAction.setEnabled(false);
    }

    class LayerUpAction extends ZAction {

        public LayerUpAction() {
            super("layer-up.png");
            this.putValue(TOOL_TIP_TEXT_KEY, "Raise Layer");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ZImage image = getImage();
            int index = image.indexOf(getCurrentLayer());

            if (index > 0) {
                image.getEditor().moveLayer(index, index - 1);
                // FIXME: find this out from a listener (otherwise undo doesn't work)
                layers.getSelectionModel().setSelectionInterval(index - 1, index - 1);
            }
        }
    }

    class LayerDownAction extends ZAction {

        public LayerDownAction() {
            super("layer-down.png");
            this.putValue(TOOL_TIP_TEXT_KEY, "Lower Layer");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ZImage image = getImage();
            int index = image.indexOf(getCurrentLayer());

            if (index + 1 < image.getLayerCount()) {
                image.getEditor().moveLayer(index, index + 1);
                // FIXME: find this out from a listener (otherwise undo doesn't work)
                layers.getSelectionModel().setSelectionInterval(index + 1, index + 1);
            }
        }
    }

    class LayerAddAction extends ZAction {

        public LayerAddAction() {
            super("layer-add.png");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new NewLayerWindow(getTopLevelAncestor(), getImage()).setVisible(true);
        }
    }

    class LayerRemoveAction extends ZAction {

        public LayerRemoveAction() {
            super("layer-delete.png");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ZImage image = getImage();
            ZLayer layer = getCurrentLayer();

            image.getEditor().removeLayer(layer);
        }
    }

    class LayerCopyAction extends ZAction {

        public LayerCopyAction() {
            super("layer-copy.png");
            this.putValue(TOOL_TIP_TEXT_KEY, "Copy Layer");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // FIXME: implement layer copy
        }
    }

    class LayerPasteAction extends ZAction {

        public LayerPasteAction() {
            super("layer-paste.png");
            this.putValue(TOOL_TIP_TEXT_KEY, "Paste Into Layer");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // FIXME: implement paste as layer
        }
    }
}
