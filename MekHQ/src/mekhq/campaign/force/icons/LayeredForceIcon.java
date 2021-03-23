/*
 * Copyright (c) 2020-2021 - The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MekHQ.
 *
 * MekHQ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MekHQ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MekHQ. If not, see <http://www.gnu.org/licenses/>.
 */
package mekhq.campaign.force.icons;

import megamek.common.annotations.Nullable;
import megamek.common.icons.AbstractIcon;
import megamek.common.util.StringUtil;
import mekhq.MHQStaticDirectoryManager;
import mekhq.MekHQ;
import mekhq.MekHqXmlUtil;
import mekhq.campaign.force.enums.LayeredForceIconEnum;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LayeredForceIcon extends StandardForceIcon {
    //region Variable Declarations
    private static final long serialVersionUID = -2366003293807482568L;

    public static final String LAYERED_CATEGORY = "Layered";
    public static final String XML_TAG = "layeredForceIcon";

    private LinkedHashMap<LayeredForceIconEnum, List<String>> iconMap = new LinkedHashMap<>();
    //endregion Variable Declarations

    //region Constructors
    public LayeredForceIcon() {
        this(LAYERED_CATEGORY, DEFAULT_ICON_FILENAME);
    }

    public LayeredForceIcon(final AbstractIcon icon) {
        this(icon.getCategory(), icon.getFilename());
    }

    public LayeredForceIcon(final @Nullable String category, final @Nullable String filename) {
        super(category, filename);
        createDefaultIconMap();
    }

    public LayeredForceIcon(final @Nullable String category, final @Nullable String filename,
                            final LinkedHashMap<LayeredForceIconEnum, List<String>> iconMap) {
        super(category, filename);
        setIconMap(iconMap);
    }
    //endregion Constructors

    //region Getters/Setters
    @Override
    public void setCategory(final @Nullable String category) {

    }

    @Override
    public void setFilename(final @Nullable String filename) {
        this.filename = (filename == null) ? DEFAULT_FORCE_ICON_FILENAME : filename;
    }

    public LinkedHashMap<LayeredForceIconEnum, List<String>> getIconMap() {
        return iconMap;
    }

    public void setIconMap(final LinkedHashMap<LayeredForceIconEnum, List<String>> iconMap) {
        this.iconMap = iconMap;
    }
    //endregion Getters/Setters

    private void createDefaultIconMap() {
        iconMap.put(LayeredForceIconEnum.FRAME, Collections.singletonList("Frame.png"));
    }

    @Override
    public @Nullable Image getBaseImage() {
        return LAYERED_CATEGORY.equals(getCategory()) ? createLayeredForceIcon() : super.getBaseImage();
    }

    private @Nullable Image createLayeredForceIcon() {
        // If we can't create the force icon directory, return null
        if (MHQStaticDirectoryManager.getForceIcons() == null) {
            return null;
        }

        try {
            int width = 0;
            int height = 0;

            // Gather height/width
            for (final LayeredForceIconEnum layer : LayeredForceIconEnum.getInDrawOrder()) {
                if (getIconMap().containsKey(layer)) {
                    for (final String value : getIconMap().get(layer)) {
                        // Load up the image piece
                        final BufferedImage currentImage = (BufferedImage) MHQStaticDirectoryManager
                                .getForceIcons().getItem(layer.getLayerPath(), value);
                        if (currentImage != null) {
                            width = Math.max(currentImage.getWidth(), width);
                            height = Math.max(currentImage.getHeight(), height);
                        }
                    }
                }
            }

            final BufferedImage image = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
                    .getDefaultConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
            final Graphics2D g2d = image.createGraphics();
            for (final LayeredForceIconEnum layer : LayeredForceIconEnum.getInDrawOrder()) {
                if (getIconMap().containsKey(layer)) {
                    for (String value : getIconMap().get(layer)) {
                        final BufferedImage currentImage = (BufferedImage) MHQStaticDirectoryManager
                                .getForceIcons().getItem(layer.getLayerPath(), value);
                        if (currentImage != null) {
                            // Draw the current buffered image onto the base, aligning bottom and right side
                            g2d.drawImage(currentImage, width - currentImage.getWidth() + 1,
                                    height - currentImage.getHeight() + 1, null);
                        }
                    }
                }
            }

            return image;
        } catch (Exception e) {
            MekHQ.getLogger().error(e);
        }

        try {
            return (BufferedImage) MHQStaticDirectoryManager.getForceIcons().getItem("",
                    DEFAULT_FORCE_ICON_FILENAME);
        } catch (Exception e) {
            MekHQ.getLogger().error("Failed to build default layered force icon", e);
        }

        return null;
    }

    //region FileIO
    @Override
    public void writeToXML(final PrintWriter pw, final int indent) {
        writeToXML(pw, indent, XML_TAG);
    }

    @Override
    public void writeBodyToXML(final PrintWriter pw, int indent) {
        super.writeBodyToXML(pw, indent);
        if (LayeredForceIcon.LAYERED_CATEGORY.equals(getCategory())) {
            MekHqXmlUtil.writeSimpleXMLOpenIndentedLine(pw, indent++, "iconMap");
            for (Map.Entry<LayeredForceIconEnum, List<String>> entry : getIconMap().entrySet()) {
                MekHqXmlUtil.writeSimpleXMLOpenIndentedLine(pw, indent++, "entry");
                MekHqXmlUtil.writeSimpleXMLTag(pw, indent, "key", entry.getKey().name());
                for (final String value : entry.getValue()) {
                    MekHqXmlUtil.writeSimpleXMLTag(pw, indent, "value", value);
                }
                MekHqXmlUtil.writeSimpleXMLCloseIndentedLine(pw, --indent, "entry");
            }
            MekHqXmlUtil.writeSimpleXMLCloseIndentedLine(pw, --indent, "iconMap");
        }
    }

    public static LayeredForceIcon parseFromXML(final Node wn) {
        final LayeredForceIcon icon = new LayeredForceIcon();
        try {
            icon.parseNodes(wn.getChildNodes());
        } catch (Exception e) {
            MekHQ.getLogger().error(e);
            return new LayeredForceIcon();
        }
        return icon;
    }

    @Deprecated
    public void parseIconMapSubNodes(final Node wn) {
    }

    @Override
    protected void parseNode(final Node wn) {
        super.parseNode(wn);
        if ("iconMap".equals(wn.getNodeName())) {
            parseIconMapSubNodes(wn.getChildNodes());
        }
    }

    private void parseIconMapSubNodes(final NodeList nl) {
        final List<String> values = new ArrayList<>();
        String key = null;
        for (int y = 0; y < nl.getLength(); y++) {
            final Node wn = nl.item(y);

            // If it's not an element node, we ignore it.
            if (wn.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            switch (wn.getNodeName()) {
                case "key":
                    key = MekHqXmlUtil.unEscape(wn.getTextContent().trim());
                    break;
                case "value":
                    values.add(MekHqXmlUtil.unEscape(wn.getTextContent().trim()));
                    break;
            }
        }

        if (!StringUtil.isNullOrEmpty(key) && !values.isEmpty()) {
            getIconMap().put(LayeredForceIconEnum.valueOf(key), values);
        }
    }
    //endregion FileIO

    @Override
    public LayeredForceIcon clone() {
        final LinkedHashMap<LayeredForceIconEnum, List<String>> iconMap = new LinkedHashMap<>();
        for (final Map.Entry<LayeredForceIconEnum, List<String>> entry : getIconMap().entrySet()) {
            iconMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return new LayeredForceIcon(getCategory(), getFilename(), iconMap);
    }

    @Override
    public boolean equals(final @Nullable Object other) {
        if (other == null) {
            return false;
        } else if (this == other) {
            return true;
        } else if (other instanceof LayeredForceIcon) {
            final LayeredForceIcon dOther = (LayeredForceIcon) other;
            return dOther.getCategory().equals(getCategory())
                    && dOther.getFilename().equals(getFilename())
                    && getIconMap().equals(dOther.getIconMap());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (getCategory() + getFilename() + getIconMap().hashCode()).hashCode();
    }
}
