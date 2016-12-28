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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.carrier.Chest;
import org.spongepowered.api.data.type.Art;
import org.spongepowered.api.data.type.Arts;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.data.type.TreeType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.hanging.ItemFrame;
import org.spongepowered.api.entity.hanging.Painting;
import org.spongepowered.api.entity.living.Villager;
import org.spongepowered.api.entity.living.animal.Horse;
import org.spongepowered.api.entity.living.animal.Ocelot;
import org.spongepowered.api.entity.living.animal.Rabbit;
import org.spongepowered.api.entity.living.animal.Sheep;
import org.spongepowered.api.entity.living.animal.Wolf;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.api.util.rotation.Rotations;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.biome.BiomeType;

import com.flowpowered.math.vector.Vector3i;
import com.wasteofplastic.askyblock.ASkyBlock;
import com.wasteofplastic.askyblock.Settings;
import com.wasteofplastic.askyblock.Settings.GameType;
import com.wasteofplastic.askyblock.util.Util;
import com.wasteofplastic.org.jnbt.ByteArrayTag;
import com.wasteofplastic.org.jnbt.ByteTag;
import com.wasteofplastic.org.jnbt.CompoundTag;
import com.wasteofplastic.org.jnbt.FloatTag;
import com.wasteofplastic.org.jnbt.IntTag;
import com.wasteofplastic.org.jnbt.ListTag;
import com.wasteofplastic.org.jnbt.NBTInputStream;
import com.wasteofplastic.org.jnbt.ShortTag;
import com.wasteofplastic.org.jnbt.StringTag;
import com.wasteofplastic.org.jnbt.Tag;

public class Schematic {
    private ASkyBlock plugin;
    //private short[] blocks;
    //private byte[] data;
    private short width;
    private short length;
    private short height;
    private Map<BlockVector, Map<String, Tag>> tileEntitiesMap = new HashMap<BlockVector, Map<String, Tag>>();
    //private HashMap<BlockVector, EntityType> entitiesMap = new HashMap<BlockVector, EntityType>();
    private List<EntityObject> entitiesList = new ArrayList<EntityObject>();
    private File file;
    private String heading;
    private String name;
    private String perm;
    private String description;
    private int rating;
    private boolean useDefaultChest;
    private ItemType icon;    
    private BiomeType biome;
    private boolean usePhysics;
    private boolean pasteEntities;
    private boolean visible;
    private int order;
    // These hashmaps enable translation between WorldEdit strings and Bukkit names
    //private HashMap<String, EntityType> WEtoME = new HashMap<String, EntityType>();
    private List<EntityType> islandCompanion;
    private List<String> companionNames;
    private ItemStack[] defaultChestItems;
    // Name of a schematic this one is paired with
    private String partnerName = "";
    // Key blocks
    private Vector bedrock;
    private Vector chest;
    private Vector welcomeSign;
    private Vector topGrass;
    private Vector playerSpawn;
    //private ItemType playerSpawnBlock;
    private Set<String> attachable = new HashSet<String>();
    private Map<String, Art> paintingList = new HashMap<String, Art>();
    private Map<Byte, Direction> facingList = new HashMap<Byte, Direction>();
    private Map<Byte, Rotation> rotationList = new HashMap<Byte, Rotation>();
    private List<IslandBlock> islandBlocks;
    //private boolean pasteAir;
    private int durability;
    private int levelHandicap;
    private double cost;

    public Schematic(ASkyBlock plugin) {
        this.plugin = plugin;
        // Initialize 
        name = "";
        heading = "";
        description = "Default Island";
        perm = "";
        icon = ItemTypes.MAP;
        rating = 50;
        useDefaultChest = true;	
        biome = Settings.defaultBiome;
        usePhysics = Settings.usePhysics;
        file = null;
        islandCompanion = new ArrayList<EntityType>();
        islandCompanion.add(Settings.islandCompanion);
        companionNames = Settings.companionNames;
        defaultChestItems = Settings.chestItems;
        visible = true;
        order = 0;
        bedrock = null;
        chest = null;
        welcomeSign = null;
        topGrass = null;
        playerSpawn = null;
        //playerSpawnBlock = null;
        partnerName = "";
    }

    @SuppressWarnings("deprecation")
    public Schematic(ASkyBlock plugin, File file) throws IOException {
        this.plugin = plugin;
        // Initialize
        short[] blocks;
        byte[] data;
        name = file.getName();
        heading = "";
        description = "";
        perm = "";
        icon = ItemTypes.MAP;
        rating = 50;
        useDefaultChest = true;
        biome = Settings.defaultBiome;
        usePhysics = Settings.usePhysics;
        islandCompanion = new ArrayList<EntityType>();
        islandCompanion.add(Settings.islandCompanion);
        companionNames = Settings.companionNames;
        defaultChestItems = Settings.chestItems;
        pasteEntities = false;
        visible = true;
        order = 0;
        bedrock = null;
        chest = null;
        welcomeSign = null;
        topGrass = null;
        playerSpawn = null;
        //playerSpawnBlock = null;
        partnerName = "";

        attachable.add(BlockTypes.STONE_BUTTON.getId());
        attachable.add(BlockTypes.WOODEN_BUTTON.getId());
        attachable.add(BlockTypes.COCOA.getId());
        attachable.add(BlockTypes.LADDER.getId());
        attachable.add(BlockTypes.LEVER.getId());
        attachable.add(BlockTypes.PISTON_EXTENSION.getId());
        attachable.add(BlockTypes.UNLIT_REDSTONE_TORCH.getId());
        attachable.add(BlockTypes.REDSTONE_TORCH.getId());
        attachable.add(BlockTypes.WALL_SIGN.getId());
        attachable.add(BlockTypes.TORCH.getId());
        attachable.add(BlockTypes.TRAPDOOR.getId());
        attachable.add(BlockTypes.TRIPWIRE_HOOK.getId());
        attachable.add(BlockTypes.VINE.getId());
        attachable.add(BlockTypes.WOODEN_DOOR.getId());
        attachable.add(BlockTypes.IRON_DOOR.getId());
        attachable.add(BlockTypes.RED_MUSHROOM.getId());
        attachable.add(BlockTypes.BROWN_MUSHROOM.getId());
        attachable.add(BlockTypes.PORTAL.getId());

        // Painting list, useful to check if painting exsits or nor
        paintingList.put("Kebab", Arts.KEBAB);
        paintingList.put("Aztec", Arts.AZTEC);
        paintingList.put("Alban", Arts.ALBAN);
        paintingList.put("Aztec2", Arts.AZTEC_2);
        paintingList.put("Bomb", Arts.BOMB);
        paintingList.put("Plant", Arts.PLANT);
        paintingList.put("Wasteland", Arts.WASTELAND);
        paintingList.put("Wanderer", Arts.WANDERER);
        paintingList.put("Graham", Arts.GRAHAM);
        paintingList.put("Pool", Arts.POOL);
        paintingList.put("Courbet", Arts.COURBET);
        paintingList.put("Sunset", Arts.SUNSET);
        paintingList.put("Sea", Arts.SEA);
        paintingList.put("Creebet", Arts.CREEBET);
        paintingList.put("Match", Arts.MATCH);
        paintingList.put("Bust", Arts.BUST);
        paintingList.put("Stage", Arts.STAGE);
        paintingList.put("Void", Arts.VOID);
        paintingList.put("SkullAndRoses", Arts.SKULL_AND_ROSES);
        paintingList.put("Wither", Arts.WITHER);
        paintingList.put("Fighters", Arts.FIGHTERS);
        paintingList.put("Skeleton", Arts.SKELETON);
        paintingList.put("DonkeyKong", Arts.DONKEY_KONG);
        paintingList.put("Pointer", Arts.POINTER);
        paintingList.put("Pigscene", Arts.PIGSCENE);
        paintingList.put("BurningSkull", Arts.BURNING_SKULL);

        facingList.put((byte) 0, Direction.SOUTH);
        facingList.put((byte) 1, Direction.WEST);
        facingList.put((byte) 2, Direction.NORTH);
        facingList.put((byte) 3, Direction.EAST);

        rotationList.put((byte) 0, Rotations.TOP);
        rotationList.put((byte) 2, Rotations.LEFT);
        rotationList.put((byte) 4, Rotations.BOTTOM);
        rotationList.put((byte) 6, Rotations.RIGHT);

        // Entities
        /*
	WEtoME.put("LAVASLIME", EntityType.MAGMA_CUBE);
	WEtoME.put("ENTITYHORSE", EntityType.HORSE);
	WEtoME.put("OZELOT", EntityType.OCELOT);
	WEtoME.put("MUSHROOMCOW", EntityType.MUSHROOM_COW);
	WEtoME.put("PIGZOMBIE", EntityType.PIG_ZOMBIE);
         */
        this.file = file;
        // Try to load the file
        try {
            FileInputStream stream = new FileInputStream(file);
            // InputStream is = new DataInputStream(new
            // GZIPInputStream(stream));
            NBTInputStream nbtStream = new NBTInputStream(stream);

            CompoundTag schematicTag = (CompoundTag) nbtStream.readTag();
            nbtStream.close();
            stream.close();
            if (!schematicTag.getName().equals("Schematic")) {
                throw new IllegalArgumentException("Tag \"Schematic\" does not exist or is not first");
            }

            Map<String, Tag> schematic = schematicTag.getValue();

            Vector3i origin = null;
            try {
                int originX = getChildTag(schematic, "WEOriginX", IntTag.class).getValue();
                int originY = getChildTag(schematic, "WEOriginY", IntTag.class).getValue();
                int originZ = getChildTag(schematic, "WEOriginZ", IntTag.class).getValue();
                Vector3i min = new Vector3i(originX, originY, originZ);

                //int offsetX = getChildTag(schematic, "WEOffsetX", IntTag.class).getValue();
                //int offsetY = getChildTag(schematic, "WEOffsetY", IntTag.class).getValue();
                //int offsetZ = getChildTag(schematic, "WEOffsetZ", IntTag.class).getValue();
                //Vector offset = new Vector(offsetX, offsetY, offsetZ);

                //origin = min.subtract(offset);
                origin = min.clone();
            } catch (Exception ignored) {}
            //Bukkit.getLogger().info("Origin = " + origin);


            if (!schematic.containsKey("Blocks")) {
                throw new IllegalArgumentException("Schematic file is missing a \"Blocks\" tag");
            }

            width = getChildTag(schematic, "Width", ShortTag.class).getValue();
            length = getChildTag(schematic, "Length", ShortTag.class).getValue();
            height = getChildTag(schematic, "Height", ShortTag.class).getValue();

            String ItemTypes = getChildTag(schematic, "ItemTypes", StringTag.class).getValue();
            if (!ItemTypes.equals("Alpha")) {
                throw new IllegalArgumentException("Schematic file is not an Alpha schematic");
            }

            byte[] blockId = getChildTag(schematic, "Blocks", ByteArrayTag.class).getValue();
            data = getChildTag(schematic, "Data", ByteArrayTag.class).getValue();
            byte[] addId = new byte[0];
            blocks = new short[blockId.length]; // Have to later combine IDs
            // We support 4096 block IDs using the same method as vanilla
            // Minecraft, where
            // the highest 4 bits are stored in a separate byte array.
            if (schematic.containsKey("AddBlocks")) {
                addId = getChildTag(schematic, "AddBlocks", ByteArrayTag.class).getValue();
            }

            // Combine the AddBlocks data with the first 8-bit block ID
            for (int index = 0; index < blockId.length; index++) {
                if ((index >> 1) >= addId.length) { // No corresponding
                    // AddBlocks index
                    blocks[index] = (short) (blockId[index] & 0xFF);
                } else {
                    if ((index & 1) == 0) {
                        blocks[index] = (short) (((addId[index >> 1] & 0x0F) << 8) + (blockId[index] & 0xFF));
                    } else {
                        blocks[index] = (short) (((addId[index >> 1] & 0xF0) << 4) + (blockId[index] & 0xFF));
                    }
                }
            }
            // Entities
            List<Tag> entities = getChildTag(schematic, "Entities", ListTag.class).getValue();
            for (Tag tag : entities) {
                if (!(tag instanceof CompoundTag))
                    continue;

                CompoundTag t = (CompoundTag) tag;
                //Bukkit.getLogger().info("**************************************");
                EntityObject ent = new EntityObject();
                for (Map.Entry<String, Tag> entry : t.getValue().entrySet()) {
                    //Bukkit.getLogger().info("DEBUG " + entry.getKey() + ">>>>" + entry.getValue());
                    //Bukkit.getLogger().info("++++++++++++++++++++++++++++++++++++++++++++++++++");
                    if (entry.getKey().equals("id")) {
                        String id = ((StringTag)entry.getValue()).getValue().toUpperCase();
                        //Bukkit.getLogger().info("DEBUG: ID is '" + id + "'");
                        if (IslandBlock.WEtoME.containsKey(id)) {
                            //Bukkit.getLogger().info("DEBUG: id found");
                            ent.setType(IslandBlock.WEtoME.get(id));
                        } else if (!id.equalsIgnoreCase("ITEM")){
                            //Bukkit.getLogger().info("DEBUG: id not found");
                            try {
                                ent.setType(EntityType.valueOf(id));
                            } catch (Exception ex) {
                                plugin.getLogger().warning("MobType " + id + " unknown, skipping");
                            }
                        }
                    }

                    if (entry.getKey().equals("Pos")) {
                        //Bukkit.getLogger().info("DEBUG Pos fond");
                        if (entry.getValue() instanceof ListTag) {
                            //Bukkit.getLogger().info("DEBUG coord found");
                            List<Tag> pos = new ArrayList<Tag>();
                            pos = ((ListTag) entry.getValue()).getValue();
                            //Bukkit.getLogger().info("DEBUG pos: " + pos);
                            double x = (double)pos.get(0).getValue() - origin.getX();
                            double y = (double)pos.get(1).getValue() - origin.getY();
                            double z = (double)pos.get(2).getValue() - origin.getZ();
                            ent.setLocation(new BlockVector(x,y,z));
                        }
                    } else if (entry.getKey().equals("Motion")) {
                        //Bukkit.getLogger().info("DEBUG Pos fond");
                        if (entry.getValue() instanceof ListTag) {
                            //Bukkit.getLogger().info("DEBUG coord found");
                            List<Tag> pos = new ArrayList<Tag>();
                            pos = ((ListTag) entry.getValue()).getValue();
                            //Bukkit.getLogger().info("DEBUG pos: " + pos);
                            ent.setMotion(new Vector((double)pos.get(0).getValue(), (double)pos.get(1).getValue()
                                    ,(double)pos.get(2).getValue()));
                        }
                    } else if (entry.getKey().equals("Rotation")) {
                        //Bukkit.getLogger().info("DEBUG Pos fond");
                        if (entry.getValue() instanceof ListTag) {
                            //Bukkit.getLogger().info("DEBUG coord found");
                            List<Tag> pos = new ArrayList<Tag>();
                            pos = ((ListTag) entry.getValue()).getValue();
                            //Bukkit.getLogger().info("DEBUG pos: " + pos);
                            ent.setYaw((float)pos.get(0).getValue());
                            ent.setPitch((float)pos.get(1).getValue());
                        }
                    } else if (entry.getKey().equals("Color")) {
                        if (entry.getValue() instanceof ByteTag) {
                            ent.setColor(((ByteTag) entry.getValue()).getValue());
                        }
                    } else if (entry.getKey().equals("Sheared")) {
                        if (entry.getValue() instanceof ByteTag) {
                            if (((ByteTag) entry.getValue()).getValue() != (byte)0) {
                                ent.setSheared(true);
                            } else {
                                ent.setSheared(false);
                            }
                        }
                    } else if (entry.getKey().equals("RabbitType")) {
                        if (entry.getValue() instanceof IntTag) {
                            ent.setRabbitType(((IntTag)entry.getValue()).getValue());
                        }
                    } else if (entry.getKey().equals("Profession")) {
                        if (entry.getValue() instanceof IntTag) {
                            ent.setProfession(((IntTag)entry.getValue()).getValue());
                        }
                    } else if (entry.getKey().equals("CarryingChest")) {
                        if (entry.getValue() instanceof ByteTag) {
                            ent.setCarryingChest(((ByteTag) entry.getValue()).getValue());
                        }
                    } else if (entry.getKey().equals("OwnerUUID")) {
                        ent.setOwned(true);
                    } else if (entry.getKey().equals("CollarColor")) {
                        if (entry.getValue() instanceof ByteTag) {
                            ent.setCollarColor(((ByteTag) entry.getValue()).getValue());
                        }
                    } else if (entry.getKey().equals("Facing")) {
                        if (entry.getValue() instanceof ByteTag) {
                            ent.setFacing(((ByteTag) entry.getValue()).getValue());
                        }
                    } else if (entry.getKey().equals("Motive")) {
                        if (entry.getValue() instanceof StringTag) {
                            ent.setMotive(((StringTag) entry.getValue()).getValue());
                        }
                    } else if (entry.getKey().equals("ItemDropChance")) {
                        if (entry.getValue() instanceof FloatTag) {
                            ent.setItemDropChance(((FloatTag) entry.getValue()).getValue());
                        }
                    } else if (entry.getKey().equals("ItemRotation")) {
                        if (entry.getValue() instanceof ByteTag){
                            ent.setItemRotation(((ByteTag) entry.getValue()).getValue());
                        }
                    } else if (entry.getKey().equals("Item")) {
                        if (entry.getValue() instanceof CompoundTag) {
                            CompoundTag itemTag = (CompoundTag) entry.getValue();
                            for (Map.Entry<String, Tag> itemEntry : itemTag.getValue().entrySet()) {
                                if (itemEntry.getKey().equals("Count")){
                                    if (itemEntry.getValue() instanceof ByteTag){
                                        ent.setCount(((ByteTag) itemEntry.getValue()).getValue());
                                    }
                                } else if (itemEntry.getKey().equals("Damage")){
                                    if (itemEntry.getValue() instanceof ShortTag){
                                        ent.setDamage(((ShortTag) itemEntry.getValue()).getValue());
                                    }
                                } else if (itemEntry.getKey().equals("id")){
                                    if (itemEntry.getValue() instanceof StringTag){
                                        ent.setId(((StringTag) itemEntry.getValue()).getValue());
                                    }
                                } 
                            }
                        }
                    } else if (entry.getKey().equals("TileX")){
                        if (entry.getValue() instanceof IntTag){
                            ent.setTileX((double)((IntTag)entry.getValue()).getValue() - origin.getX());
                        }
                    } else if (entry.getKey().equals("TileY")){
                        if (entry.getValue() instanceof IntTag){
                            ent.setTileY((double)((IntTag)entry.getValue()).getValue() - origin.getY());
                        }
                    } else if (entry.getKey().equals("TileZ")){
                        if (entry.getValue() instanceof IntTag){
                            ent.setTileZ((double)((IntTag)entry.getValue()).getValue() - origin.getZ());
                        }
                    }
                }

                if (ent.getType() != null) {
                    //Bukkit.getLogger().info("DEBUG: adding " + ent.getType().toString() + " at " + ent.getLocation().toString());
                    //entitiesMap.put(new BlockVector(x,y,z), mobType);
                    entitiesList.add(ent);
                }
            }
            //Bukkit.getLogger().info("DEBUG: size of entities = " + entities.size());
            // Tile entities
            List<Tag> tileEntities = getChildTag(schematic, "TileEntities", ListTag.class).getValue();
            // Map<BlockVector, Map<String, Tag>> tileEntitiesMap = new
            // HashMap<BlockVector, Map<String, Tag>>();

            for (Tag tag : tileEntities) {
                if (!(tag instanceof CompoundTag))
                    continue;
                CompoundTag t = (CompoundTag) tag;

                int x = 0;
                int y = 0;
                int z = 0;

                Map<String, Tag> values = new HashMap<String, Tag>();

                for (Map.Entry<String, Tag> entry : t.getValue().entrySet()) {
                    if (entry.getKey().equals("x")) {
                        if (entry.getValue() instanceof IntTag) {
                            x = ((IntTag) entry.getValue()).getValue();
                        }
                    } else if (entry.getKey().equals("y")) {
                        if (entry.getValue() instanceof IntTag) {
                            y = ((IntTag) entry.getValue()).getValue();
                        }
                    } else if (entry.getKey().equals("z")) {
                        if (entry.getValue() instanceof IntTag) {
                            z = ((IntTag) entry.getValue()).getValue();
                        }
                    }

                    values.put(entry.getKey(), entry.getValue());
                }

                BlockVector vec = new BlockVector(x, y, z);
                tileEntitiesMap.put(vec, values);
            }
        } catch (IOException e) {
            Bukkit.getLogger().severe("Could not load island schematic! Error in file.");
            e.printStackTrace();
            throw new IOException();
        }

        // Check for key blocks
        // Find top most bedrock - this is the key stone
        // Find top most chest
        // Find top most grass
        List<Vector> grassBlocks = new ArrayList<Vector>();
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < length; ++z) {
                    int index = y * width * length + z * width + x;
                    // Bukkit.getLogger().info("DEBUG " + index +
                    // " changing to ID:"+blocks[index] + " data = " +
                    // blockData[index]);
                    if (blocks[index] == 7) {
                        // Last bedrock
                        if (bedrock == null || bedrock.getY() < y) {
                            bedrock = new Vector(x, y, z);
                            //Bukkit.getLogger().info("DEBUG higher bedrock found:" + bedrock.toString());
                        }
                    } else if (blocks[index] == 54) {
                        // Last chest
                        if (chest == null || chest.getY() < y) {
                            chest = new Vector(x, y, z);
                            // Bukkit.getLogger().info("Island loc:" +
                            // loc.toString());
                            // Bukkit.getLogger().info("Chest relative location is "
                            // + chest.toString());
                        }
                    } else if (blocks[index] == 63) {
                        // Sign
                        if (welcomeSign == null || welcomeSign.getY() < y) {
                            welcomeSign = new Vector(x, y, z);
                            // Bukkit.getLogger().info("DEBUG higher sign found:"
                            // + welcomeSign.toString());
                        }
                    } else if (blocks[index] == 2) {
                        // Grass
                        grassBlocks.add(new Vector(x,y,z));
                    } 
                }
            }
        }
        if (bedrock == null) {
            Bukkit.getLogger().severe("Schematic must have at least one bedrock in it!");
            throw new IOException();
        }
        // Find other key blocks
        if (!grassBlocks.isEmpty()) {
            // Sort by height
            List<Vector> sorted = new ArrayList<Vector>();
            for (Vector v : grassBlocks) {
                //if (GridManager.isSafeLocation(v.toLocation(world))) {
                // Add to sorted list
                boolean inserted = false;
                for (int i = 0; i < sorted.size(); i++) {
                    if (v.getBlockY() > sorted.get(i).getBlockY()) {
                        sorted.add(i, v);
                        inserted = true;
                        break;
                    }
                }
                if (!inserted) {
                    // just add to the end of the list
                    sorted.add(v);
                }
            }
            topGrass = sorted.get(0);
        } else {
            topGrass = null;
        }

        // Preload the blocks
        prePasteSchematic(blocks, data);
    }

    /**
     * @return the biome
     */
    public Biome getBiome() {
        return biome;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * @return the heading
     */
    public String getHeading() {
        return heading;
    }

    /**
     * @return the height
     */
    public short getHeight() {
        return height;
    }

    /**
     * @return the icon
     */
    public ItemType getIcon() {
        return icon;
    }

    /**
     * @return the durability of the icon
     */
    public int getDurability() {
        return durability;
    }

    /**
     * @return the length
     */
    public short getLength() {
        return length;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the perm
     */
    public String getPerm() {
        return perm;
    }

    /**
     * @return the rating
     */
    public int getRating() {
        return rating;
    }

    /**
     * @return the tileEntitiesMap
     */
    public Map<BlockVector, Map<String, Tag>> getTileEntitiesMap() {
        return tileEntitiesMap;
    }

    /**
     * @return the width
     */
    public short getWidth() {
        return width;
    }

    /**
     * @return the useDefaultChest
     */
    public boolean isUseDefaultChest() {
        return useDefaultChest;
    }

    /**
     * @return the usePhysics
     */
    public boolean isUsePhysics() {
        return usePhysics;
    }

    /*
     * This function pastes using World Edit - problem is that because it reads a file, it's slow.
    @SuppressWarnings("deprecation")
     */
    /*
     * 
    public void pasteSchematic(final Location loc, final Player player, boolean teleport)  {
	plugin.getLogger().info("WE Pasting");
	com.sk89q.worldedit.Vector WEorigin = new com.sk89q.worldedit.Vector(loc.getBlockX(),loc.getBlockY(),loc.getBlockZ());
        EditSession es = new EditSession(new BukkitWorld(loc.getWorld()), 999999999);
        try {
        CuboidClipboard cc = CuboidClipboard.loadSchematic(file);
        cc.paste(es, WEorigin, false);
        cc.pasteEntities(WEorigin);
        } catch (Exception e) {
            e.printStackTrace();
        }

    	if (teleport) {
    		World world = loc.getWorld();

    	    player.teleport(world.getSpawnLocation());
    	    plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {

    		@Override
    		public void run() {
    		    plugin.getGrid().homeTeleport(player);

    		}}, 10L);

    	}
    }   
     */
    /**
     * This method pastes a schematic.
     * @param loc
     * @param player
     */
    public void pasteSchematic(final Location loc, final Player player, boolean teleport) {
        // If this is not a file schematic, paste the default island
        if (this.file == null) {
            if (Settings.GAMETYPE == GameType.ACIDISLAND) {
                generateIslandBlocks(loc,player);
            } else {
                loc.getBlock().setType(ItemTypes.BEDROCK);
                ASkyBlock.getPlugin().getLogger().severe("Missing schematic - using bedrock block only");
            }
            return;
        }
        World world = loc.getWorld();
        Location blockLoc = new Location(world, loc.getX(), loc.getY(), loc.getZ());
        //Location blockLoc = new Location(world, loc.getX(), Settings.island_level, loc.getZ());
        blockLoc.subtract(bedrock);
        //plugin.getLogger().info("DEBUG: blockloc = " + blockLoc);
        // Paste the island blocks
        //plugin.getLogger().info("DEBUG: islandBlock size (paste) = " + islandBlocks.size());
        for (IslandBlock b : islandBlocks) {
            b.paste(nms, blockLoc, this.usePhysics, biome);
        }
        // PASTE ENTS
        //Bukkit.getLogger().info("Block loc = " + blockLoc);
        if (pasteEntities) {
            for (EntityObject ent : entitiesList) {
                // If TileX/Y/Z id defined, we have to use it (for Item Frame & Painting)
                if(ent.getTileX() != null && ent.getTileY() != null && ent.getTileZ() != null){
                    ent.setLocation(new BlockVector(ent.getTileX(),ent.getTileY(),ent.getTileZ()));
                }

                Location entitySpot = ent.getLocation().toLocation(blockLoc.getWorld()).add(blockLoc.toVector());
                entitySpot.setPitch(ent.getPitch());
                entitySpot.setYaw(ent.getYaw());
                //Bukkit.getLogger().info("DEBUG: Entity type = " + ent.getType());
                if(ent.getType() == EntityType.PAINTING){

                    //Bukkit.getLogger().info("DEBUG: painting = " + ent.getMotive() + "; facing = " + ent.getFacing());
                    //Bukkit.getLogger().info("DEBUG: spawning " + ent.getType().toString() + " at " + entitySpot);
                    try {
                        Painting painting = blockLoc.getWorld().spawn(entitySpot, Painting.class);
                        if (painting != null) {
                            if(paintingList.containsKey(ent.getMotive())){
                                //painting.setArt(Art.GRAHAM);
                                painting.setArt(paintingList.get(ent.getMotive()), true);
                            } else {
                                // Set default
                                painting.setArt(Art.ALBAN, true);
                            }

                            // http://minecraft.gamepedia.com/Painting#Data_values
                            if(facingList.containsKey(ent.getFacing())){
                                painting.setFacingDirection(facingList.get(ent.getFacing()), true);
                            } else {
                                //set default direction
                                painting.setFacingDirection(BlockFace.NORTH, true);
                            }
                            //Bukkit.getLogger().info("DEBUG: Painting setFacingDirection: " + painting.getLocation().toString() + "; facing: " + painting.getFacing() + "; ent facing: " + ent.getFacing());
                            //Bukkit.getLogger().info("DEBUG: Painting setArt: " + painting.getLocation().toString() + "; art: " + painting.getArt() + "; ent motive: " + ent.getMotive());

                        }
                    } catch (IllegalArgumentException e) {
                        //plugin.getLogger().warning("Cannot paste painting from schematic");
                    }
                } else if(ent.getType() == EntityType.ITEM_FRAME) {

                    //Bukkit.getLogger().info("DEBUG: spawning itemframe at" + entitySpot.toString());

                    //Bukkit.getLogger().info("DEBUG: tileX: " + ent.getTileX() + ", tileY: " + ent.getTileY() + ", tileZ: " + ent.getTileZ());

                    ItemFrame itemFrame = (ItemFrame) blockLoc.getWorld().spawnEntity(entitySpot, EntityType.ITEM_FRAME);
                    if (itemFrame != null) {
                        // Need to improve this shity fix ...
                        ItemType ItemType = ItemTypes.matchItemType(ent.getId().substring(10).toUpperCase());;

                        if(ItemType == null && IslandBlock.WEtoM.containsKey(ent.getId().substring(10).toUpperCase())){
                            ItemType = IslandBlock.WEtoM.get(ent.getId().substring(10).toUpperCase());
                        }

                        ItemStack item;

                        if(ItemType != null){
                            //Bukkit.getLogger().info("DEBUG: id: " + ent.getId() + ", ItemType match: " + ItemTypes.toString()); 
                            if(ent.getCount() != null){
                                if(ent.getDamage() != null){
                                    item = new ItemStack(ItemType, ent.getCount(), ent.getDamage());
                                } else {
                                    item = new ItemStack(ItemType, ent.getCount(), (short) 0);
                                }
                            } else {
                                if(ent.getDamage() != null){
                                    item = new ItemStack(ItemType, 1, ent.getDamage());
                                } else {
                                    item = new ItemStack(ItemType, 1, (short) 0);
                                }
                            }
                        } else {
                            //Bukkit.getLogger().info("DEBUG: ItemType can't be found for: " + ent.getId() + " (" + ent.getId().substring(10).toUpperCase() + ")"); 
                            // Set to default content
                            item = new ItemStack(ItemTypes.STONE, 0, (short) 4);
                        }

                        ItemMeta itemMeta = item.getItemMeta();

                        // TODO: Implement methods to get enchantement, names, lore etc.

                        item.setItemMeta(itemMeta);
                        itemFrame.setItem(item);

                        if(facingList.containsKey(ent.getFacing())){
                            itemFrame.setFacingDirection(facingList.get(ent.getFacing()), true);
                        } else {
                            //set default direction
                            itemFrame.setFacingDirection(BlockFace.NORTH, true);
                        }

                        // TODO: Implements code to handle the rotation of the item in the itemframe
                        if(rotationList.containsKey(ent.getItemRotation())){
                            itemFrame.setRotation(rotationList.get(ent.getItemRotation()));
                        } else {
                            // Set default direction
                            itemFrame.setRotation(Rotation.NONE);
                        }
                    }
                } else {
                    //Bukkit.getLogger().info("Spawning " + ent.getType().toString() + " at " + entitySpot);
                    Entity spawned = blockLoc.getWorld().spawnEntity(entitySpot, ent.getType());
                    if (spawned != null) {
                        spawned.setVelocity(ent.getMotion());
                        if (ent.getType() == EntityType.SHEEP) {
                            Sheep sheep = (Sheep)spawned;
                            if (ent.isSheared()) {   
                                sheep.setSheared(true);
                            }
                            DyeColor[] set = DyeColor.values();
                            sheep.setColor(set[ent.getColor()]);
                            sheep.setAge(ent.getAge());
                        } else if (ent.getType() == EntityType.HORSE) {
                            Horse horse = (Horse)spawned;
                            Horse.Color[] set = Horse.Color.values();
                            horse.setColor(set[ent.getColor()]);
                            horse.setAge(ent.getAge());
                            horse.setCarryingChest(ent.isCarryingChest());
                        } else if (ent.getType() == EntityType.VILLAGER) {
                            Villager villager = (Villager)spawned;
                            villager.setAge(ent.getAge());
                            Profession[] proffs = Profession.values();
                            villager.setProfession(proffs[ent.getProfession()]);
                        } else if (!Bukkit.getServer().getVersion().contains("(MC: 1.7") && ent.getType() == EntityType.RABBIT) {
                            Rabbit rabbit = (Rabbit)spawned;
                            Rabbit.Type[] set = Rabbit.Type.values();
                            rabbit.setRabbitType(set[ent.getRabbitType()]);
                            rabbit.setAge(ent.getAge());
                        } else if (ent.getType() == EntityType.OCELOT) {
                            Ocelot cat = (Ocelot)spawned;
                            if (ent.isOwned()) {
                                cat.setTamed(true);
                                cat.setOwner(player);
                            }
                            Ocelot.Type[] set = Ocelot.Type.values();
                            cat.setCatType(set[ent.getCatType()]);
                            cat.setAge(ent.getAge());
                            cat.setSitting(ent.isSitting());
                        } else if (ent.getType() == EntityType.WOLF) {
                            Wolf wolf = (Wolf)spawned;
                            if (ent.isOwned()) {
                                wolf.setTamed(true);
                                wolf.setOwner(player);
                            }
                            wolf.setAge(ent.getAge());
                            wolf.setSitting(ent.isSitting());
                            DyeColor[] color = DyeColor.values();
                            wolf.setCollarColor(color[ent.getCollarColor()]);
                        }
                    }
                }
            }
        }
        // Find the grass spot
        final Location grass;
        if (topGrass != null) {
            Location gr = topGrass.clone().toLocation(loc.getWorld()).subtract(bedrock);
            gr.add(loc.toVector());
            gr.add(new Vector(0.5D,1.1D,0.5D)); // Center of block and a bit up so the animal drops a bit
            grass = gr;
        } else {
            grass = null;
        }	

        //Bukkit.getLogger().info("DEBUG cow location " + grass);
        Block blockToChange = null;
        // world.spawnEntity(grass, EntityType.COW);
        // Place a helpful sign in front of player
        if (welcomeSign != null) {
            // Bukkit.getLogger().info("DEBUG welcome sign schematic relative is:"
            // + welcomeSign.toString());
            Vector ws = welcomeSign.clone().subtract(bedrock);
            // Bukkit.getLogger().info("DEBUG welcome sign relative to bedrock is:"
            // + welcomeSign.toString());
            ws.add(loc.toVector());
            // Bukkit.getLogger().info("DEBUG welcome sign actual position is:"
            // + welcomeSign.toString());
            blockToChange = ws.toLocation(world).getBlock();
            BlockState signState = blockToChange.getState();
            if (signState instanceof Sign) {
                Sign sign = (Sign) signState;
                if (sign.getLine(0).isEmpty()) {
                    sign.setLine(0, plugin.myLocale(player.getUniqueId()).signLine1.replace("[player]", player.getName()));
                }
                if (sign.getLine(1).isEmpty()) {
                    sign.setLine(1, plugin.myLocale(player.getUniqueId()).signLine2.replace("[player]", player.getName()));
                }
                if (sign.getLine(2).isEmpty()) {
                    sign.setLine(2, plugin.myLocale(player.getUniqueId()).signLine3.replace("[player]", player.getName()));
                }
                if (sign.getLine(3).isEmpty()) {
                    sign.setLine(3, plugin.myLocale(player.getUniqueId()).signLine4.replace("[player]", player.getName()));
                }
                // BlockFace direction = ((org.bukkit.ItemTypes.Sign)
                // sign.getData()).getFacing();
                //((org.bukkit.ItemTypes.Sign) sign.getData()).setFacingDirection(BlockFace.NORTH);
                sign.update();
            }
        }
        if (chest != null) {
            Vector ch = chest.clone().subtract(bedrock);
            ch.add(loc.toVector());
            // Place the chest - no need to use the safe spawn function because we
            // know what this island looks like
            blockToChange = ch.toLocation(world).getBlock();
            // Bukkit.getLogger().info("Chest block = " + blockToChange);
            // blockToChange.setType(ItemTypes.CHEST);
            // Bukkit.getLogger().info("Chest item settings = " +
            // Settings.chestItems[0]);
            // Bukkit.getLogger().info("Chest item settings length = " +
            // Settings.chestItems.length);
            if (useDefaultChest) {
                // Fill the chest
                if (blockToChange.getType() == ItemTypes.CHEST) {
                    final Chest islandChest = (Chest) blockToChange.getState();
                    DoubleChest doubleChest = null;
                    InventoryHolder iH = islandChest.getInventory().getHolder();
                    if (iH instanceof DoubleChest) {
                        //Bukkit.getLogger().info("DEBUG: double chest");
                        doubleChest = (DoubleChest) iH;
                    }
                    if (doubleChest != null) {
                        Inventory inventory = doubleChest.getInventory();
                        inventory.clear();
                        inventory.setContents(defaultChestItems);
                    } else {
                        Inventory inventory = islandChest.getInventory();
                        inventory.clear();
                        inventory.setContents(defaultChestItems);
                    }
                }
            }
        }

        if (teleport) {
            plugin.getPlayers().setInTeleport(player.getUniqueId(), true);
            player.teleport(world.getSpawnLocation());
            plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {

                @Override
                public void run() {
                    plugin.getGrid().homeTeleport(player);
                    plugin.getPlayers().setInTeleport(player.getUniqueId(), false);
                }}, 10L);

        }
        if (!islandCompanion.isEmpty() && grass != null) {
            Bukkit.getServer().getScheduler().runTaskLater(ASkyBlock.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    spawnCompanion(player, grass);
                }
            }, 40L);
        }
    }
    /**
     * This method prepares to pastes a schematic.
     * @param blocks
     * @param data
     */
    @SuppressWarnings("deprecation")
    public void prePasteSchematic(short[] blocks, byte[] data) {
        //plugin.getLogger().info("DEBUG: prepaste ");
        islandBlocks = new ArrayList<IslandBlock>();
        Map<BlockVector, Map<String, Tag>> tileEntitiesMap = this.getTileEntitiesMap();
        // Start with non-attached blocks
        //plugin.getLogger().info("DEBUG: attachable size = " + attachable.size());
        //plugin.getLogger().info("DEBUG: torch = " + ItemTypes.TORCH.getId());
        //plugin.getLogger().info("DEBUG: non attachable");
        //plugin.getLogger().info("DEBUG: bedrock y = " + bedrock.getBlockY());
        //int count = 0;
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < length; ++z) {
                    int index = y * width * length + z * width + x;
                    // Only bother if this block is above ground zero and 
                    // only bother with air if it is below sea level
                    // TODO: need to check max world height too?
                    int h = Settings.island_level + y - bedrock.getBlockY();
                    if (h >= 0 && h < 255 && (blocks[index] != 0 || h < Settings.sea_level)){
                        // Only bother if the schematic blocks are within the range that y can be
                        //plugin.getLogger().info("DEBUG: height " + (count++) + ":" +h);
                        IslandBlock block = new IslandBlock(x, y, z);
                        if (!attachable.contains((int)blocks[index]) || blocks[index] == 179) {
                            if (Bukkit.getServer().getVersion().contains("(MC: 1.7") && blocks[index] == 179) {
                                // Red sandstone - use red sand instead
                                block.setBlock(12, (byte)1);
                            } else {
                                block.setBlock(blocks[index], data[index]);
                            }
                            // Tile Entities
                            if (tileEntitiesMap.containsKey(new BlockVector(x, y, z))) {
                                if (plugin.isOnePointEight()) {
                                    if (block.getTypeId() == ItemTypes.STANDING_BANNER.getId()) {
                                        block.setBanner(tileEntitiesMap.get(new BlockVector(x, y, z)));
                                    }
                                    else if (block.getTypeId() == ItemTypes.SKULL.getId()) {
                                        block.setSkull(tileEntitiesMap.get(new BlockVector(x, y, z)), block.getData());
                                    }
                                    else if (block.getTypeId() == ItemTypes.FLOWER_POT.getId()) {
                                        block.setFlowerPot(tileEntitiesMap.get(new BlockVector(x, y, z)));
                                    }
                                }
                                // Monster spawner blocks
                                if (block.getTypeId() == ItemTypes.MOB_SPAWNER.getId()) {
                                    block.setSpawnerType(tileEntitiesMap.get(new BlockVector(x, y, z)));
                                } else if ((block.getTypeId() == ItemTypes.SIGN_POST.getId())) {
                                    block.setSign(tileEntitiesMap.get(new BlockVector(x, y, z)));
                                } else if (block.getTypeId() == ItemTypes.CHEST.getId()) {
                                    block.setChest(nms, tileEntitiesMap.get(new BlockVector(x, y, z)));
                                }
                            }
                            islandBlocks.add(block);
                        }
                    }
                }
            }
        }
        //plugin.getLogger().info("Attachable blocks");
        // Second pass - just paste attachables and deal with chests etc.

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < length; ++z) {
                    int h = Settings.island_level + y - bedrock.getBlockY();
                    if (h >= 0 && h < 255){
                        int index = y * width * length + z * width + x;
                        IslandBlock block = new IslandBlock(x, y, z);
                        if (attachable.contains((int)blocks[index])) {
                            block.setBlock(blocks[index], data[index]);
                            // Tile Entities
                            if (tileEntitiesMap.containsKey(new BlockVector(x, y, z))) {
                                if (plugin.isOnePointEight()) {
                                    if (block.getTypeId() == ItemTypes.WALL_BANNER.getId()) {
                                        block.setBanner(tileEntitiesMap.get(new BlockVector(x, y, z)));
                                    }
                                }
                                // Wall Sign
                                if (block.getTypeId() == ItemTypes.WALL_SIGN.getId()) {
                                    block.setSign(tileEntitiesMap.get(new BlockVector(x, y, z)));
                                }
                            }
                            islandBlocks.add(block);
                        }
                    }
                }
            }
        }
        //plugin.getLogger().info("DEBUG: islandBlocks size = " + islandBlocks.size());
    }

    /**
     * @param biome the biome to set
     */
    public void setBiome(Biome biome) {
        this.biome = biome;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param heading the heading to set
     */
    public void setHeading(String heading) {
        this.heading = heading;
    }

    public void setIcon(ItemType icon, int damage) {
        this.icon = icon;
        this.durability = damage;    
    }
    /**
     * @param icon the icon to set
     */
    public void setIcon(ItemType icon) {
        this.icon = icon;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param perm the perm to set
     */
    public void setPerm(String perm) {
        this.perm = perm;
    }

    /**
     * @param rating the rating to set
     */
    public void setRating(int rating) {
        this.rating = rating;
    }

    /**
     * @param useDefaultChest the useDefaultChest to set
     */
    public void setUseDefaultChest(boolean useDefaultChest) {
        this.useDefaultChest = useDefaultChest;
    }

    /**
     * @param usePhysics the usePhysics to set
     */
    public void setUsePhysics(boolean usePhysics) {
        this.usePhysics = usePhysics;
    }


    /**
     * Removes all the air blocks if they are not to be pasted.
     * @param pasteAir the pasteAir to set
     */
    public void setPasteAir(boolean pasteAir) {
        if (!pasteAir) {
            Iterator<IslandBlock> it = islandBlocks.iterator();
            while (it.hasNext()) {
                if (it.next().getTypeId() == 0) {
                    it.remove();
                }
            }
        }
        //plugin.getLogger().info("DEBUG: islandBlocks after removing air blocks = " + islandBlocks.size());
    }

    /**
     * Creates an island block by block
     * @param islandLoc
     * @param player
     */
    @SuppressWarnings("deprecation")
    public void generateIslandBlocks(final Location islandLoc, final Player player) {
        // AcidIsland
        // Build island layer by layer
        // Start from the base
        // half sandstone; half sand
        int x = islandLoc.getBlockX();
        int z = islandLoc.getBlockZ();
        World world = islandLoc.getWorld();
        int y = 0;
        for (int x_space = x - 4; x_space <= x + 4; x_space++) {
            for (int z_space = z - 4; z_space <= z + 4; z_space++) {
                final Block b = world.getBlockAt(x_space, y, z_space);
                b.setType(ItemTypes.BEDROCK);
                b.setBiome(biome);
            }
        }
        for (y = 1; y < Settings.island_level + 5; y++) {
            for (int x_space = x - 4; x_space <= x + 4; x_space++) {
                for (int z_space = z - 4; z_space <= z + 4; z_space++) {
                    final Block b = world.getBlockAt(x_space, y, z_space);
                    if (y < (Settings.island_level / 2)) {
                        b.setType(ItemTypes.SANDSTONE);
                    } else {
                        b.setType(ItemTypes.SAND);
                        b.setData((byte) 0);
                    }
                }
            }
        }
        // Then cut off the corners to make it round-ish
        for (y = 0; y < Settings.island_level + 5; y++) {
            for (int x_space = x - 4; x_space <= x + 4; x_space += 8) {
                for (int z_space = z - 4; z_space <= z + 4; z_space += 8) {
                    final Block b = world.getBlockAt(x_space, y, z_space);
                    b.setType(ItemTypes.STATIONARY_WATER);
                }
            }
        }
        // Add some grass
        for (y = Settings.island_level + 4; y < Settings.island_level + 5; y++) {
            for (int x_space = x - 2; x_space <= x + 2; x_space++) {
                for (int z_space = z - 2; z_space <= z + 2; z_space++) {
                    final Block blockToChange = world.getBlockAt(x_space, y, z_space);
                    blockToChange.setType(ItemTypes.GRASS);
                }
            }
        }
        // Place bedrock - MUST be there (ensures island are not
        // overwritten
        Block b = world.getBlockAt(x, Settings.island_level, z);
        b.setType(ItemTypes.BEDROCK);
        // Then add some more dirt in the classic shape
        y = Settings.island_level + 3;
        for (int x_space = x - 2; x_space <= x + 2; x_space++) {
            for (int z_space = z - 2; z_space <= z + 2; z_space++) {
                b = world.getBlockAt(x_space, y, z_space);
                b.setType(ItemTypes.DIRT);
            }
        }
        b = world.getBlockAt(x - 3, y, z);
        b.setType(ItemTypes.DIRT);
        b = world.getBlockAt(x + 3, y, z);
        b.setType(ItemTypes.DIRT);
        b = world.getBlockAt(x, y, z - 3);
        b.setType(ItemTypes.DIRT);
        b = world.getBlockAt(x, y, z + 3);
        b.setType(ItemTypes.DIRT);
        y = Settings.island_level + 2;
        for (int x_space = x - 1; x_space <= x + 1; x_space++) {
            for (int z_space = z - 1; z_space <= z + 1; z_space++) {
                b = world.getBlockAt(x_space, y, z_space);
                b.setType(ItemTypes.DIRT);
            }
        }
        b = world.getBlockAt(x - 2, y, z);
        b.setType(ItemTypes.DIRT);
        b = world.getBlockAt(x + 2, y, z);
        b.setType(ItemTypes.DIRT);
        b = world.getBlockAt(x, y, z - 2);
        b.setType(ItemTypes.DIRT);
        b = world.getBlockAt(x, y, z + 2);
        b.setType(ItemTypes.DIRT);
        y = Settings.island_level + 1;
        b = world.getBlockAt(x - 1, y, z);
        b.setType(ItemTypes.DIRT);
        b = world.getBlockAt(x + 1, y, z);
        b.setType(ItemTypes.DIRT);
        b = world.getBlockAt(x, y, z - 1);
        b.setType(ItemTypes.DIRT);
        b = world.getBlockAt(x, y, z + 1);
        b.setType(ItemTypes.DIRT);

        // Add island items
        y = Settings.island_level;
        // Add tree (natural)
        final Location treeLoc = new Location(world, x, y + 5D, z);
        world.generateTree(treeLoc, TreeType.ACACIA);
        // Place the cow
        final Location location = new Location(world, x, (Settings.island_level + 5), z - 2);

        // Place a helpful sign in front of player
        Block blockToChange = world.getBlockAt(x, Settings.island_level + 5, z + 3);
        blockToChange.setType(ItemTypes.SIGN_POST);
        Sign sign = (Sign) blockToChange.getState();
        sign.setLine(0, ASkyBlock.getPlugin().myLocale(player.getUniqueId()).signLine1.replace("[player]", player.getName()));
        sign.setLine(1, ASkyBlock.getPlugin().myLocale(player.getUniqueId()).signLine2.replace("[player]", player.getName()));
        sign.setLine(2, ASkyBlock.getPlugin().myLocale(player.getUniqueId()).signLine3.replace("[player]", player.getName()));
        sign.setLine(3, ASkyBlock.getPlugin().myLocale(player.getUniqueId()).signLine4.replace("[player]", player.getName()));
        ((org.bukkit.ItemTypes.Sign) sign.getData()).setFacingDirection(BlockFace.NORTH);
        sign.update();
        // Place the chest - no need to use the safe spawn function
        // because we
        // know what this island looks like
        blockToChange = world.getBlockAt(x, Settings.island_level + 5, z + 1);
        blockToChange.setType(ItemTypes.CHEST);
        // Only set if the config has items in it
        if (Settings.chestItems.length > 0) {
            final Chest chest = (Chest) blockToChange.getState();
            final Inventory inventory = chest.getInventory();
            inventory.clear();
            inventory.setContents(Settings.chestItems);
            chest.update();
        }
        // Fill the chest and orient it correctly (1.8 faces it north!
        DirectionalContainer dc = (DirectionalContainer) blockToChange.getState().getData();
        dc.setFacingDirection(BlockFace.SOUTH);
        blockToChange.setData(dc.getData(), true);
        // Teleport player
        plugin.getGrid().homeTeleport(player);
        if (!islandCompanion.isEmpty()) {
            Bukkit.getServer().getScheduler().runTaskLater(ASkyBlock.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    spawnCompanion(player, location);
                }
            }, 40L);
        }
    }
    /**
     * Get child tag of a NBT structure.
     * 
     * @param items
     *            The parent tag map
     * @param key
     *            The name of the tag to get
     * @param expected
     *            The expected type of the tag
     * @return child tag casted to the expected type
     */
    private static <T extends Tag> T getChildTag(Map<String, Tag> items, String key, Class<T> expected) throws IllegalArgumentException {
        if (!items.containsKey(key)) {
            throw new IllegalArgumentException("Schematic file is missing a \"" + key + "\" tag");
        }
        Tag tag = items.get(key);
        if (!expected.isInstance(tag)) {
            throw new IllegalArgumentException(key + " tag is not of tag type " + expected.getName());
        }
        return expected.cast(tag);
    }
    /**
     * Spawns a random companion for the player with a random name at the location given
     * @param player
     * @param location
     */
    protected void spawnCompanion(Player player, Location location) {
        // Older versions of the server require custom names to only apply to Living Entities
        //Bukkit.getLogger().info("DEBUG: spawning compantion at " + location);
        if (!islandCompanion.isEmpty() && location != null) {
            Random rand = new Random();
            int randomNum = rand.nextInt(islandCompanion.size());
            EntityType type = islandCompanion.get(randomNum);
            if (type != null) {
                LivingEntity companion = (LivingEntity) location.getWorld().spawnEntity(location, type);
                if (!companionNames.isEmpty()) {
                    randomNum = rand.nextInt(companionNames.size());
                    String name = companionNames.get(randomNum).replace("[player]", player.getDisplayName());
                    //plugin.getLogger().info("DEBUG: name is " + name);
                    companion.setCustomName(name);
                    companion.setCustomNameVisible(true);
                } 
            }
        }
    }

    /**
     * @param islandCompanion the islandCompanion to set
     */
    public void setIslandCompanion(List<EntityType> islandCompanion) {
        this.islandCompanion = islandCompanion;
    }

    /**
     * @param companionNames the companionNames to set
     */
    public void setCompanionNames(List<String> companionNames) {
        this.companionNames = companionNames;
    }

    /**
     * @param defaultChestItems the defaultChestItems to set
     */
    public void setDefaultChestItems(ItemStack[] defaultChestItems) {
        this.defaultChestItems = defaultChestItems;
    }

    /**
     * @return if Biome is HELL, this is true
     */
    public boolean isInNether() {
        if (biome == Biome.HELL) {
            return true;
        }
        return false;
    }

    /**
     * @return the partnerName
     */
    public String getPartnerName() {
        return partnerName;
    }

    /**
     * @param partnerName the partnerName to set
     */
    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    /**
     * @return the pasteEntities
     */
    public boolean isPasteEntities() {
        return pasteEntities;
    }

    /**
     * @param pasteEntities the pasteEntities to set
     */
    public void setPasteEntities(boolean pasteEntities) {
        this.pasteEntities = pasteEntities;
    }

    /**
     * Whether the schematic is visible or not
     * @return the visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets if the schematic can be seen in the schematics GUI or not by the player
     * @param visible the visible to set
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * @return the order
     */
    public int getOrder() {
        return order;
    }

    /**
     * @param order the order to set
     */
    public void setOrder(int order) {
        this.order = order;
    }


    /**
     * @return true if player spawn exists in this schematic
     */
    public boolean isPlayerSpawn() {
        if (playerSpawn == null) {
            return false;
        }
        return true;
    }

    /**
     * @return the playerSpawn Location given a paste location
     */
    public Location getPlayerSpawn(Location pasteLocation) {
        return pasteLocation.clone().add(playerSpawn);
    }

    /**
     * @param playerSpawnBlock the playerSpawnBlock to set
     * @return true if block is found otherwise false
     */
    @SuppressWarnings("deprecation")
    public boolean setPlayerSpawnBlock(ItemType playerSpawnBlock) {
        if (bedrock == null) {
            return false;
        }
        playerSpawn = null;
        // Run through the schematic and try and find the spawnBlock
        for (IslandBlock islandBlock : islandBlocks) {
            if (islandBlock.getTypeId() == playerSpawnBlock.getId()) {
                playerSpawn = islandBlock.getVector().subtract(bedrock).add(new Vector(0.5D,0D,0.5D));
                // Set the block to air
                islandBlock.setTypeId((short)0);
                return true;
            }
        }
        return false;
    }


    /**
     * @return the levelHandicap
     */
    public int getLevelHandicap() {
        return levelHandicap;
    }

    /**
     * @param levelHandicap the levelHandicap to set
     */
    public void setLevelHandicap(int levelHandicap) {
        this.levelHandicap = levelHandicap;
    }

    /**
     * Set the cost
     * @param cost
     */
    public void setCost(double cost) {
       this.cost = cost;
    }

    /**
     * @return the cost
     */
    public double getCost() {
        return cost;
    }

}