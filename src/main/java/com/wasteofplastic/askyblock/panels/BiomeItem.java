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

package com.wasteofplastic.askyblock.panels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.item.BlockItemData;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.biome.BiomeType;

import com.wasteofplastic.askyblock.Settings;
import com.wasteofplastic.askyblock.util.Util;
import com.wasteofplastic.askyblock.util.VaultHelper;

public class BiomeItem {
    private ItemStack item;
    private int slot;
    private double price;
    // private String description;
    private String name;
    private boolean confirm;
    private BiomeType biome;

    /**
     * @param slot
     * @param cost
     * @param description
     * @param name
     */
    public BiomeItem(ItemType type, int slot, double cost, String description, String name, boolean confirm, BiomeType biome) {
        this.slot = slot;
        this.price = cost;
        // this.description = description;
        this.name = name;
        this.confirm = confirm;
        this.biome = biome;
        // Make the item(s)
        item = ItemStack.builder().itemType(type)..build();
        // Set the description and price
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + name);
        List<String> lore = new ArrayList<String>();
        if (description.contains("|") || description.length() <= 20) {
            // Split pip character requires escaping it
            String[] split = description.split("\\|");
            lore = new ArrayList<String>(Arrays.asList(split));
        } else {
            lore = Util.chop(TextColors.YELLOW, description, 20);
        }
        // Create price
        if (Settings.useEconomy && cost > 0D) {
            lore.add(VaultHelper.econ.format(cost));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    /**
     * @return the item
     */
    public ItemStack getItem() {
        return item;
    }

    /**
     * @return the slot
     */
    public int getSlot() {
        return slot;
    }

    /**
     * @return the confirm
     */
    public boolean isConfirm() {
        return confirm;
    }

    /**
     * @return the biome
     */
    public BiomeType getBiome() {
        return biome;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the price
     */
    public double getPrice() {
        return price;
    }

    /**
     * @param biome
     *            the biome to set
     */
    public void setBiome(BiomeType biome) {
        this.biome = biome;
    }

}
