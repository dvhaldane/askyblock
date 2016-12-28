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

package com.wasteofplastic.askyblock;

import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.equipment.EquipmentInventory;

/**
 * Where the inventory data is stored
 * 
 * @author tastybento
 */
public class InventoryStore {
	
    private PlayerInventory inventory;
    private EquipmentInventory armor;

    /**
     * @param inventory
     * @param armor
     */
    public InventoryStore(PlayerInventory inventory, EquipmentInventory armor) {
        this.inventory = inventory;
        this.armor = armor;
    }

    /**
     * @return the inventory
     */
    public PlayerInventory getInventory() {
        return inventory;
    }

    /**
     * @param inventory
     *            the inventory to set
     */
    public void setInventory(PlayerInventory inventory) {
        this.inventory = inventory;
    }

    /**
     * @return the armor
     */
    public EquipmentInventory getArmor() {
        return armor;
    }

    /**
     * @param armor
     *            the armor to set
     */
    public void setArmor(EquipmentInventory armor) {
        this.armor = armor;
    }
}
