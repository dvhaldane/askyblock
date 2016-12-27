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

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

import com.wasteofplastic.askyblock.ASkyBlock;
import com.wasteofplastic.askyblock.commands.Challenges;
import com.wasteofplastic.askyblock.util.Util;
import com.wasteofplastic.askyblock.util.VaultHelper;

/**
 * @author tastybento
 * 
 */
public class MiniShopItem {
    private int slot;
    private double price;
    private double sellPrice;
    private int quantity;
    private ItemType material;
    private String extra;
    private String description;
    private ItemStack item;

    // private ASkyBlock plugin = ASkyBlock.getPlugin();

    /**
     * 
     */
    public MiniShopItem(ItemType material, String extra, int slot, String description, int quantity, Double price, Double sellPrice) {
        this.slot = slot;
        this.material = material;
        if (description.isEmpty()) {
            description = Util.prettifyText(material.getName());
        }
        this.description = description;
        this.price = price;
        this.sellPrice = sellPrice;
        this.quantity = quantity;
        // Make the item(s)
        try {
            item = ItemStack.builder().itemType(material).build();
            if (quantity < 1) {
                quantity = 1;
            }
            item.setQuantity(quantity);
           // Deal with extras
            if (!extra.isEmpty()) {
                // plugin.getLogger().info("DEBUG: extra is not empty");
                // If it not a potion, then the extras should just be durability
                if (!material.getName().contains("POTION")) {
                    if (material.equals(ItemTypes.MONSTER_EGG)) {
                        try {
                            EntityType type = EntityType.valueOf(extra.toUpperCase());
                            item = 
                            if (Bukkit.getServer().getVersion().contains("(MC: 1.8") || Bukkit.getServer().getVersion().contains("(MC: 1.7")) {
                                item = new SpawnEgg(type).toItemStack(quantity);
                            } else {
                                try {
                                    item = new SpawnEgg1_9(type).toItemStack(quantity);
                                } catch (Exception ex) {
                                    item = new ItemStack(material);
                                    Bukkit.getLogger().severe("Monster eggs not supported with this server version.");
                                }
                                ItemMeta meta = item.getItemMeta();
                            }
                        } catch (Exception e) {
                            Bukkit.getLogger().severe("Spawn eggs must be described by name. Try one of these (not all are possible):");                          
                            for (EntityType type : EntityType.values()) {
                                if (type.isSpawnable() && type.isAlive()) {
                                    Bukkit.getLogger().severe(type.toString());
                                }
                            }
                        }
                    } else {
                        item.setDurability(Short.parseShort(extra));
                    }
                } else {
                    // Potion, splash potion or linger potion
                    extra = "POTION:" + extra;
                    String[] extras = extra.split(":");
                    item = Challenges.getPotion(extras, quantity, "minishop.yml");
                }
            }
            // Set the description and price
            ItemMeta meta = item.getItemMeta();
            // Split up the description
            List<String> desc = new ArrayList<String>(Arrays.asList(description.split("\\|")));
            meta.setDisplayName(desc.get(0));
            ArrayList<String> buyAndSell = new ArrayList<String>();
            if (desc.size() > 1) {
                desc.remove(0);// Remove the name
                buyAndSell.addAll(desc); // Add the rest to the description
            }
            // Create prices for buying and selling
            if (price > 0D) {
                buyAndSell.add(ASkyBlock.getPlugin().myLocale().minishopBuy + " " + quantity + " @ " + VaultHelper.econ.format(price));
            }
            if (sellPrice > 0D) {
                buyAndSell.add(ASkyBlock.getPlugin().myLocale().minishopSell + " " + quantity + " @ " + VaultHelper.econ.format(sellPrice));
            }
            if (price < 0D && sellPrice < 0D) {
                buyAndSell.add(ASkyBlock.getPlugin().myLocale().minishopOutOfStock);
            }
            meta.setLore(buyAndSell);
            item.setItemMeta(meta);
 
        } catch (Exception ex) {
            ASkyBlock.getPlugin().getLogger().severe("Problem parsing shop item from minishop.yml so skipping it: " + material);
            ASkyBlock.getPlugin().getLogger().severe("Error is : " + ex.getMessage());
            ex.printStackTrace();
            ASkyBlock.getPlugin().getLogger().info("Potential potion types are: ");
            for (PotionType c : PotionType.values())
                ASkyBlock.getPlugin().getLogger().info(c.name());
            ASkyBlock.getPlugin().getLogger().info("Potions can also be EXTENDED, SPLASH or EXTENDEDSPLASH, example WATER_BREATHING:EXTENDED");
        }
        // If there's no description, then set it.
        if (description == null) {
            this.description = Util.prettifyText(getDataName(item));
        }

    }

    /**
     * @return the item
     */
    public ItemStack getItem() {
        return item;
    }

    /**
     * Returns a clean version of this item with no meta data
     * 
     * @return Clean item stack
     */
    public ItemStack getItemClean() {
        ItemStack temp = this.item.clone();
        ItemMeta meta = temp.getItemMeta();
        meta.setDisplayName(null);
        meta.setLore(null);
        temp.setItemMeta(meta);
        return temp;
    }

    /**
     * @return the slot
     */
    public int getSlot() {
        return slot;
    }

    /**
     * @return the price
     */
    public double getPrice() {
        return price;
    }

    /**
     * @return the sellPrice
     */
    public double getSellPrice() {
        return sellPrice;
    }

    /**
     * @return the quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * @return the material
     */
    public ItemType getMaterial() {
        return material;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param slot
     *            the slot to set
     */
    public void setSlot(int slot) {
        this.slot = slot;
        
    }

    /**
     * @param price
     *            the price to set
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * @param quantity
     *            the quantity to set
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * @param material
     *            the material to set
     */
    public void setItemType(ItemType material) {
        this.material = material;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the extra
     */
    public String getExtra() {
        return extra;
    }

    /**
     * @param extra
     *            the extra to set
     */
    public void setExtra(String extra) {
        this.extra = extra;
    }

    private static boolean isTool(ItemType mat) {
        switch (mat) {
        case BOW:
        case SHEARS:
        case FISHING_ROD:
        case FLINT_AND_STEEL:

        case CHAINMAIL_BOOTS:
        case CHAINMAIL_CHESTPLATE:
        case CHAINMAIL_HELMET:
        case CHAINMAIL_LEGGINGS:

        case WOOD_AXE:
        case WOOD_HOE:
        case WOOD_PICKAXE:
        case WOOD_SPADE:
        case WOOD_SWORD:

        case LEATHER_BOOTS:
        case LEATHER_CHESTPLATE:
        case LEATHER_HELMET:
        case LEATHER_LEGGINGS:

        case DIAMOND_AXE:
        case DIAMOND_HOE:
        case DIAMOND_PICKAXE:
        case DIAMOND_SPADE:
        case DIAMOND_SWORD:

        case DIAMOND_BOOTS:
        case DIAMOND_CHESTPLATE:
        case DIAMOND_HELMET:
        case DIAMOND_LEGGINGS:
        case STONE_AXE:
        case STONE_HOE:
        case STONE_PICKAXE:
        case STONE_SPADE:
        case STONE_SWORD:

        case GOLD_AXE:
        case GOLD_HOE:
        case GOLD_PICKAXE:
        case GOLD_SPADE:
        case GOLD_SWORD:

        case GOLD_BOOTS:
        case GOLD_CHESTPLATE:
        case GOLD_HELMET:
        case GOLD_LEGGINGS:
        case IRON_AXE:
        case IRON_HOE:
        case IRON_PICKAXE:
        case IRON_SPADE:
        case IRON_SWORD:

        case IRON_BOOTS:
        case IRON_CHESTPLATE:
        case IRON_HELMET:
        case IRON_LEGGINGS:
            return true;
        default:
            return false;
        }

    }

}