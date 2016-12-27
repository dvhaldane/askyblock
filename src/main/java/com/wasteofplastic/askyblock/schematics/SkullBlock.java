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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Skull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.SkullType;
import org.spongepowered.api.data.type.SkullTypes;
import org.spongepowered.api.util.Direction;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.wasteofplastic.org.jnbt.ByteTag;
import com.wasteofplastic.org.jnbt.CompoundTag;
import com.wasteofplastic.org.jnbt.IntTag;
import com.wasteofplastic.org.jnbt.ListTag;
import com.wasteofplastic.org.jnbt.StringTag;
import com.wasteofplastic.org.jnbt.Tag;

/**
 * This class describes skulls and is used in schematic importing
 * 
 * @author SpyL1nk
 * 
 */
public class SkullBlock {

    private static final Random random = new Random();
    private static final String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private SkullType skullType;
    private String skullOwnerName;
    private String skullOwnerUUID;
    private Direction skullRotation;
    private int skullStanding;
    private String skullTextureValue = null;
    private String skullTextureSignature = null;

    private static HashMap<Integer, SkullType> skullTypeList;
    private static HashMap<Integer, Direction> skullRotationList;

    static {
        skullTypeList = new HashMap<Integer, SkullType>();
        skullTypeList.put(0, SkullTypes.SKELETON);
        skullTypeList.put(1, SkullTypes.WITHER_SKELETON);
        skullTypeList.put(2, SkullTypes.ZOMBIE);
        skullTypeList.put(3, SkullTypes.PLAYER);
        skullTypeList.put(4, SkullTypes.CREEPER);
    }

    static {
        skullRotationList = new HashMap<Integer, Direction>();
        skullRotationList.put(0, Direction.NORTH);
        skullRotationList.put(1, Direction.NORTH_NORTHEAST);
        skullRotationList.put(2, Direction.NORTHEAST);
        skullRotationList.put(3, Direction.EAST_NORTHEAST);
        skullRotationList.put(4, Direction.EAST);
        skullRotationList.put(5, Direction.EAST_SOUTHEAST);
        skullRotationList.put(6, Direction.SOUTHEAST);
        skullRotationList.put(7, Direction.SOUTH_SOUTHEAST);
        skullRotationList.put(8, Direction.SOUTH);
        skullRotationList.put(9, Direction.SOUTH_SOUTHWEST);
        skullRotationList.put(10, Direction.SOUTHWEST);
        skullRotationList.put(11, Direction.WEST_SOUTHWEST);
        skullRotationList.put(12, Direction.WEST);
        skullRotationList.put(13, Direction.WEST_NORTHWEST);
        skullRotationList.put(14, Direction.NORTHWEST);
        skullRotationList.put(15, Direction.NORTH_NORTHWEST);
    }

    @SuppressWarnings("deprecation")
    public boolean set(BlockState block) {
        Skull skull = (Skull) block.getState();
        if(skullOwnerName != null){
            skull.setOwner(skullOwnerName);
        }
        skull.getSkullData().set(Keys.SKULL_TYPE, skullType);
        skull.setRotation(skullRotation);
        skull.getSkullData()..setRawData((byte) skullStanding);
        // Texture update
        if(skullTextureValue != null){
            setSkullWithNonPlayerProfile(skullTextureValue, skullTextureSignature, skullOwnerUUID, skullOwnerName, skull);
        }
        return true;
    }

    public boolean prep(Map<String, Tag> tileData, int dataValue) {

        try {
            // Take skull type
            if(tileData.containsKey("SkullType")){
                int skullTypeId = (int) ((ByteTag) tileData.get("SkullType")).getValue();
                //Bukkit.getLogger().info("DEBUG: skull type = " + skullTypeId);
                if(skullTypeList.containsKey(skullTypeId)){
                    skullType = skullTypeList.get(skullTypeId);
                }
                else {
                    // Prevent hacks, set to default skull type
                    skullType = skullTypeList.get(0);
                }
            }
            else{
                // Prevent hacks, set to defaut skull type
                skullType = skullTypeList.get(0);
            }

            //Bukkit.getLogger().info("DEBUG: skull's data value = " + dataValue);
            // Data value 0 is actually unused for skulls, set to 2 to prevent hacks
            if(dataValue > 0 && dataValue < 6){

                skullStanding = dataValue;

                if(tileData.containsKey("Rot")){
                    int skullRotId = (int) ((ByteTag) tileData.get("Rot")).getValue();
                    //Bukkit.getLogger().info("DEBUG: skull's rotation byte = " + skullRotId);
                    // Useful for skulls on the floor to insert rotation data
                    if(skullRotationList.containsKey(skullStanding)){
                        skullRotation = skullRotationList.get(skullRotId);
                    }
                    else{
                        // Prevents hacks
                        skullRotation = skullRotationList.get(0);
                    }
                }
                else{
                    skullRotation = skullRotationList.get(0);
                }
            }
            else{

                skullStanding = 2;

                if(tileData.containsKey("Rot")){
                    int skullRotId = ((IntTag) tileData.get("Rot")).getValue();
                    //Bukkit.getLogger().info("DEBUG: skull's rotation byte = " + skullRotId);
                    // Useful for skulls on the floor to insert rotation data
                    if(skullRotationList.containsKey(skullStanding)){
                        skullRotation = skullRotationList.get(skullRotId);
                    }
                    // Prevents hacks
                    else{
                        skullRotation = skullRotationList.get(0);
                    }
                }
                else{
                    skullRotation = skullRotationList.get(0);
                }
            }

            // Check for Player Heads (skin, texture etc.)
            if(skullType == SkullTypes.PLAYER && tileData.containsKey("Owner")){

                Map<String, Tag> skullOwner = ((CompoundTag) tileData.get("Owner")).getValue();

                if(skullOwner.containsKey("Name")){
                    skullOwnerName = ((StringTag) skullOwner.get("Name")).getValue();

                    //Bukkit.getLogger().info("DEBUG: skull owner's name = " + skullOwnerName);
                }

                if(skullOwner.containsKey("Id")){
                    skullOwnerUUID = ((StringTag) skullOwner.get("Id")).getValue();

                    //Bukkit.getLogger().info("DEBUG: skull owner's UUID = " + skullOwnerUUID);
                }

                if(skullOwner.containsKey("Properties")){
                    Map<String, Tag> skullOwnerProperties = ((CompoundTag) skullOwner.get("Properties")).getValue();

                    if(skullOwnerProperties.containsKey("textures")){

                        ListTag listTagTextures = (ListTag) skullOwnerProperties.get("textures");

                        //Bukkit.getLogger().info("DEBUG: skull texture's list = " + listTagTextures);

                        if(listTagTextures != null){

                            // Logicaly, textures should have only one entry ...
                            Map<String, Tag> skullOwnerTextures = ((CompoundTag) listTagTextures.getValue().get(0)).getValue();

                            if(skullOwnerTextures.containsKey("Value")){
                                skullTextureValue = ((StringTag) skullOwnerTextures.get("Value")).getValue();

                                //Bukkit.getLogger().info("DEBUG: skull texture's value = " + skullTextureValue);
                            }
                            if(skullOwnerTextures.containsKey("Signature")){
                                skullTextureSignature = ((StringTag) skullOwnerTextures.get("Signature")).getValue();

                                //Bukkit.getLogger().info("DEBUG: skull's texture signature = " + skullTextureSignature);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    // Credits: GermanCoding
    @SuppressWarnings("deprecation")
    public static void setSkullWithNonPlayerProfile(String textureValue, String textureSignature, String ownerUUID, String ownerName, Skull skull) {
        if (skull.getType() != BlockTypes.SKULL)
            throw new IllegalArgumentException("Block must be a skull.");

        skull.getWorld().loadChunk(skull.getLocation().getChunkPosition(), true);


        // Difference beetween NonPlayerSkin and PlayerSkin
        if(textureSignature != null){
            try {
                setSkullProfile(skull, getPlayerProfile(textureValue, textureSignature, ownerUUID, ownerName));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                setSkullProfile(skull, getNonPlayerProfile(textureValue, ownerUUID, ownerName));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        //skull.getWorld().refreshChunk(skull.getChunk().getX(), skull.getChunk().getZ());
    }

    // Credits: val59000 (THANK YOU VERY MUCH VAL59000 !)
    private static void setSkullProfile(Skull skull, GameProfile gameProfile) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException { 	

        Field profileField = null;
        try {
            profileField = skull.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(skull, gameProfile);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    // Credits: dori99xd
    public static GameProfile getNonPlayerProfile(String textureValue, String ownerUUID, String ownerName) {
        // Create a new GameProfile with .schematic informations or with fake informations
        GameProfile newSkinProfile = new GameProfile(ownerUUID == null ? UUID.randomUUID() : UUID.fromString(ownerUUID),
                ownerName == null ? getRandomString(16) : null);

        // Insert textures properties
        newSkinProfile.getProperties().put("textures", new Property("textures", textureValue));
        return newSkinProfile;
    }

    // Credits: dori99xd
    public static GameProfile getPlayerProfile(String textureValue, String textureSignature, String ownerUUID, String ownerName) {
        // Create a new GameProfile with .schematic informations or with fake informations
        GameProfile newSkinProfile = new GameProfile( ownerUUID == null ? UUID.randomUUID() : UUID.fromString(ownerUUID),
                ownerName == null ? getRandomString(16) : null);

        // Insert textures properties
        newSkinProfile.getProperties().put("textures", new Property("textures", textureValue, textureSignature));
        return newSkinProfile;
    }

    // Credits: dori99xd
    public static String getRandomString(int length) {
        StringBuilder b = new StringBuilder(length);
        for(int j = 0; j < length; j++){
            b.append(chars.charAt(random.nextInt(chars.length())));
        }
        return b.toString();
    }
}