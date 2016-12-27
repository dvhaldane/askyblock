/*******************************************************************************
 * This file is part of ASkyBlock.
 *
 *     ASkyBlock is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ASkyBlock is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ASkyBlock.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/

package com.wasteofplastic.askyblock.schematics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.Banner;
import org.spongepowered.api.data.meta.PatternLayer;
import org.spongepowered.api.data.type.BannerPatternShape;
import org.spongepowered.api.data.type.BannerPatternShapes;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;

import com.wasteofplastic.org.jnbt.CompoundTag;
import com.wasteofplastic.org.jnbt.IntTag;
import com.wasteofplastic.org.jnbt.ListTag;
import com.wasteofplastic.org.jnbt.StringTag;
import com.wasteofplastic.org.jnbt.Tag;

/**
 * This class describes banners and is used in schematic importing
 * 
 * @author tastybento
 * 
 */
public class BannerBlock {
    private DyeColor bannerBaseColor;
    private List<PatternLayer> bannerPattern;

    private static HashMap<String, BannerPatternShape> patternKey;
    // bs, mc, cr, drs, dls, hhb, mr, hh, sc, gru, ss, gra, ts, ms, tt
    // bts, tr, tts, sku, cre, tl, vhr, vh, bo, cbo, bri

    // ss, tt
    static {
        patternKey = new HashMap<String, BannerPatternShape>();
        patternKey.put("", BannerPatternShapes.BASE);
        patternKey.put("bo", BannerPatternShapes.BORDER);
        patternKey.put("bri", BannerPatternShapes.BRICKS);
        patternKey.put("mc", BannerPatternShapes.CIRCLE_MIDDLE);
        patternKey.put("cre", BannerPatternShapes.CREEPER);
        patternKey.put("cr", BannerPatternShapes.CROSS);
        patternKey.put("cbo", BannerPatternShapes.CURLY_BORDER);
        patternKey.put("ld", BannerPatternShapes.DIAGONAL_LEFT);
        patternKey.put("lud", BannerPatternShapes.DIAGONAL_LEFT_MIRROR);
        patternKey.put("rd", BannerPatternShapes.DIAGONAL_RIGHT);
        patternKey.put("rud", BannerPatternShapes.DIAGONAL_RIGHT_MIRROR);
        patternKey.put("flo", BannerPatternShapes.FLOWER);
        patternKey.put("gra", BannerPatternShapes.GRADIENT);
        patternKey.put("gru", BannerPatternShapes.GRADIENT_UP);
        patternKey.put("hh", BannerPatternShapes.HALF_HORIZONTAL);
        patternKey.put("hhb", BannerPatternShapes.HALF_HORIZONTAL_MIRROR);
        patternKey.put("vh", BannerPatternShapes.HALF_VERTICAL);
        patternKey.put("vhr", BannerPatternShapes.HALF_VERTICAL_MIRROR);
        patternKey.put("moj", BannerPatternShapes.MOJANG);
        patternKey.put("mr", BannerPatternShapes.RHOMBUS_MIDDLE);
        patternKey.put("sku", BannerPatternShapes.SKULL);
        patternKey.put("bl", BannerPatternShapes.SQUARE_BOTTOM_LEFT);
        patternKey.put("br", BannerPatternShapes.SQUARE_BOTTOM_RIGHT);
        patternKey.put("tl", BannerPatternShapes.SQUARE_TOP_LEFT);
        patternKey.put("tr", BannerPatternShapes.SQUARE_TOP_RIGHT);
        patternKey.put("sc", BannerPatternShapes.STRAIGHT_CROSS);
        patternKey.put("bs", BannerPatternShapes.STRIPE_BOTTOM);
        patternKey.put("ms", BannerPatternShapes.STRIPE_CENTER);
        patternKey.put("dls", BannerPatternShapes.STRIPE_DOWNLEFT);
        patternKey.put("drs", BannerPatternShapes.STRIPE_DOWNRIGHT);
        patternKey.put("ls", BannerPatternShapes.STRIPE_LEFT);
        patternKey.put("ms", BannerPatternShapes.STRIPE_MIDDLE);
        patternKey.put("rs", BannerPatternShapes.STRIPE_RIGHT);
        patternKey.put("ss", BannerPatternShapes.STRIPE_SMALL);
        patternKey.put("ts", BannerPatternShapes.STRIPE_TOP);
        patternKey.put("bt", BannerPatternShapes.TRIANGLE_BOTTOM);
        patternKey.put("tt", BannerPatternShapes.TRIANGLE_TOP);
        patternKey.put("bts", BannerPatternShapes.TRIANGLES_BOTTOM);
        patternKey.put("tts", BannerPatternShapes.TRIANGLES_TOP);
    }

    public boolean set(BlockState block) {
        Banner banner = (Banner) block.getType();
        
        banner.baseColor().set(bannerBaseColor);
        banner.patternsList().set(bannerPattern);
        return true;
    }

    @SuppressWarnings("deprecation")
    public boolean prep(Map<String, Tag> tileData) {
        // Format for banner is:
        // Patterns = List of patterns
        // id = String "BannerBlock"
        // Base = Int color
        // Then the location
        // z = Int
        // y = Int
        // x = Int
        try {
            // Do the base color
            int baseColor = 15 - ((IntTag) tileData.get("Base")).getValue();
            // //ASkyBlock.getPlugin().getLogger().info("Base value = " +
            // baseColor);
            // baseColor green = 10
            bannerBaseColor = DyeColor.getByDyeData((byte) baseColor);
            // Do the patterns (no idea if this will work or not)
            bannerPattern = new ArrayList<PatternLayer>();
            ListTag patterns = (ListTag) tileData.get("Patterns");
            if (patterns != null) {
                for (Tag pattern : patterns.getValue()) {
                    // ASkyBlock.getPlugin().getLogger().info("pattern = " +
                    // pattern);
                    // Translate pattern to PatternType
                    if (pattern instanceof CompoundTag) {
                        CompoundTag patternColor = (CompoundTag) pattern;
                        // The tag is made up of pattern (String) and color
                        // (int)
                        Map<String, Tag> patternValue = patternColor.getValue();
                        StringTag mark = (StringTag) patternValue.get("Pattern");
                        Integer markColor = 15 - ((IntTag) patternValue.get("Color")).getValue();
                        // ASkyBlock.getPlugin().getLogger().info("mark = " +
                        // mark.getValue());
                        // ASkyBlock.getPlugin().getLogger().info("color = " +
                        // markColor);
                        DyeColor dColor = DyeColor.getByDyeData(markColor.byteValue());
                        // ASkyBlock.getPlugin().getLogger().info(" dye color = "
                        // + dColor.toString());
                        if (patternKey.containsKey(mark.getValue())) {
                            PatternLayer newPattern = PatternLayer.of(patternKey.get(mark.getValue()), dColor);
                            bannerPattern.add(newPattern);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

}
