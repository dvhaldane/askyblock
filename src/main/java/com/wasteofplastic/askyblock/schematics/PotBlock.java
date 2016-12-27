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

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.item.inventory.ItemStack;

import com.wasteofplastic.askyblock.nms.NMSAbstraction;
import com.wasteofplastic.org.jnbt.IntTag;
import com.wasteofplastic.org.jnbt.StringTag;
import com.wasteofplastic.org.jnbt.Tag;

/**
 * This class describes pots and is used in schematic importing
 * 
 * @author SpyL1nk
 * 
 */
public class PotBlock {
    private BlockType potItem;
    @SuppressWarnings("unused")
	private int potItemData;

    private static HashMap<String, BlockType> potItemList;

    static {
        potItemList = new HashMap<String, BlockType>();
        potItemList.put("", BlockTypes.AIR);
        potItemList.put("minecraft:red_flower", BlockTypes.RED_FLOWER);
        potItemList.put("minecraft:yellow_flower", BlockTypes.YELLOW_FLOWER);
        potItemList.put("minecraft:sapling", BlockTypes.SAPLING);
        potItemList.put("minecraft:red_mushroom", BlockTypes.RED_MUSHROOM);
        potItemList.put("minecraft:brown_mushroom", BlockTypes.BROWN_MUSHROOM);
        potItemList.put("minecraft:cactus", BlockTypes.CACTUS);
        potItemList.put("minecraft:deadbush", BlockTypes.DEADBUSH);
        potItemList.put("minecraft:tallgrass", BlockTypes.TALLGRASS);
    }

    public boolean set(NMSAbstraction nms, BlockType block) {
        if(potItem != BlockTypes.AIR){
            nms.setFlowerPotBlock(block, ItemStack.builder().fromBlockState(potItem.getDefaultState()).build());
        }
        return true;
    }

    public boolean prep(Map<String, Tag> tileData) {
        // Initialize as default
        potItem = BlockTypes.AIR;
        potItemData = 0;
        try {
            if(tileData.containsKey("Item")){

                // Get the item in the pot
                if (tileData.get("Item") instanceof IntTag) {
                    // Item is a number, not a material
                    int id = ((IntTag) tileData.get("Item")).getValue();
                    Sponge.getRegistry().getType(BlockType.class, String.valueOf(id)).ifPresent(bt -> potItem = bt);
                    // Check it's a viable pot item
                    if (!potItemList.containsValue(potItem)) {
                        // No, so reset to AIR
                        potItem = BlockTypes.AIR;
                    }
                } else if (tileData.get("Item") instanceof StringTag) {
                    // Item is a material
                    String itemName = ((StringTag) tileData.get("Item")).getValue();
                    if (potItemList.containsKey(itemName)){
                        // Check it's a viable pot item
                        if (potItemList.containsKey(itemName)) {
                            potItem = potItemList.get(itemName);
                        }
                    }
                }

                if(tileData.containsKey("Data")){
                    int dataTag = ((IntTag) tileData.get("Data")).getValue();
                    // We should check data for each type of potItem 
                    if(potItem == BlockTypes.RED_FLOWER){
                        if(dataTag >= 0 && dataTag <= 8){
                            potItemData = dataTag;
                        } else {
                            // Prevent hacks
                            potItemData = 0;
                        }
                    } else if(potItem == BlockTypes.YELLOW_FLOWER ||
                            potItem == BlockTypes.RED_MUSHROOM ||
                            potItem == BlockTypes.BROWN_MUSHROOM ||
                            potItem == BlockTypes.CACTUS){
                        // Set to 0 anyway
                        potItemData = 0;
                    } else if(potItem == BlockTypes.SAPLING){
                        if(dataTag >= 0 && dataTag <= 4){
                            potItemData = dataTag;
                        } else {
                            // Prevent hacks
                            potItemData = 0;
                        }
                    } else if(potItem == BlockTypes.TALLGRASS){
                        // Only 0 or 2
                        if(dataTag == 0 || dataTag == 2){
                            potItemData = dataTag;
                        } else {
                            potItemData = 0;
                        }
                    } else {
                        // ERROR ?
                        potItemData = 0;
                    }
                }
                else {
                    potItemData = 0;
                }
            }
            //Bukkit.getLogger().info("Debug: flowerpot item = " + potItem.toString());
            //Bukkit.getLogger().info("Debug: flowerpot item data = " + potItemData);
            //Bukkit.getLogger().info("Debug: flowerpot materialdata = " + new MaterialData(potItem,(byte) potItemData).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}