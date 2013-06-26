/*
 * Copyright (C) 2013 181
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
package imagez.util;

import imagez.image.ZImage;
import imagez.image.ZLayer;
import imagez.image.ZLayerType;
import java.awt.Rectangle;

/**
 *
 * @author 181
 */
public class EmptyImageCreator {

    public static ZImage getEmptyImage() {
        int width = 640;
        int height = 480;

        Rectangle rect = new Rectangle(width, height);
        ZLayerType type = ZLayerType.RGBA8;
        ZImage img = new ZImage(width, height);
        ZLayer layer = type.createLayer(img, rect);

        layer.setTitle("Background");
        img.addLayer(layer);
        
        return img;
    }
}
