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

package com.wasteofplastic.askyblock.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.wasteofplastic.askyblock.ASkyBlock;
import com.wasteofplastic.askyblock.PlayerCache;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

/**
 * A set of utility methods
 * 
 * @author tastybento
 * 
 */
public class Util {
	private static ASkyBlock instance = ASkyBlock.getInstance();

	/**
	 * Loads a YAML file and if it does not exist it is looked for in the JAR
	 * 
	 * @param file
	 * @return
	 */
	public static CommentedConfigurationNode loadFile(String file) {

		Path configFile = Paths.get(instance.configDir() + file);
		ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(configFile).build();
		CommentedConfigurationNode configNode = null;

		if (!Files.exists(configFile)) {
			try {
				instance.getLogger().info("No " + file + " found. Creating it...");
				if (Sponge.getAssetManager().getAsset(instance, file).isPresent()) {
					instance.getLogger().info("Using default found in jar file.");
					Sponge.getAssetManager().getAsset(instance, file).get().copyToFile(configFile);
					configNode = configLoader.load();
				} else {
					Files.createFile(configFile);
					configNode = configLoader.load();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				configNode = configLoader.load();
			} catch (IOException e) {
				instance.getLogger().info(file + " failed to load. Invalid! " + e);
			}
		}
		
		return configNode;
	}

	/**
	 * Saves a YAML file
	 * 
	 * @param yamlFile
	 * @param fileLocation
	 */
	public static void saveFile(CommentedConfigurationNode file, String fileLocation) {
		
		Path configFile = Paths.get(instance.configDir() + fileLocation);
		ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(configFile).build();
		
		try {
			configLoader.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Cuts up a string into multiple lines with the same color code at the
	 * start of each line
	 * 
	 * @param color
	 * @param longLine
	 * @param length
	 * @return List containing the colored lines
	 */
	public static List<String> chop(TextColor color, String longLine, int length) {
		List<String> result = new ArrayList<String>();
		// int multiples = longLine.length() / length;
		int i = 0;
		for (i = 0; i < longLine.length(); i += length) {
			// for (int i = 0; i< (multiples*length); i += length) {
			int endIndex = Math.min(i + length, longLine.length());
			String line = longLine.substring(i, endIndex);
			// Do the following only if i+length is not the end of the string
			if (endIndex < longLine.length()) {
				// Check if last character in this string is not a space
				if (!line.substring(line.length() - 1).equals(" ")) {
					// If it is not a space, check to see if the next character
					// in long line is a space.
					if (!longLine.substring(endIndex, endIndex + 1).equals(" ")) {
						// If it is not, then we are cutting a word in two and
						// need to backtrack to the last space if possible
						int lastSpace = line.lastIndexOf(" ");
						// Only do this if there is a space in the line to
						// backtrack to...
						if (lastSpace != -1 && lastSpace < line.length()) {
							line = line.substring(0, lastSpace);
							i -= (length - lastSpace - 1);
						}
					}
				}
			}
			// }
			result.add(color + line);
		}
		// result.add(color + longLine.substring(i, longLine.length()));
		return result;
	}

	/**
	 * Converts block face direction to radial degrees. Returns 0 if block face
	 * is not radial.
	 * 
	 * @param d
	 * @return degrees
	 */
	public static float blockFaceToFloat(Direction d) {
		switch (d) {
		case EAST:
			return 90F;
		case EAST_NORTHEAST:
			return 67.5F;
		case EAST_SOUTHEAST:
			return 0F;
		case NORTH:
			return 0F;
		case NORTHEAST:
			return 45F;
		case NORTH_NORTHEAST:
			return 22.5F;
		case NORTH_NORTHWEST:
			return 337.5F;
		case NORTHWEST:
			return 315F;
		case SOUTH:
			return 180F;
		case SOUTHEAST:
			return 135F;
		case SOUTH_SOUTHEAST:
			return 157.5F;
		case SOUTH_SOUTHWEST:
			return 202.5F;
		case SOUTHWEST:
			return 225F;
		case WEST:
			return 270F;
		case WEST_NORTHWEST:
			return 292.5F;
		case WEST_SOUTHWEST:
			return 247.5F;
		default:
			return 0F;
		}
	}

	/**
	 * Converts a name like IRON_INGOT into Iron Ingot to improve readability
	 * 
	 * @param ugly
	 *            The string such as IRON_INGOT
	 * @return A nicer version, such as Iron Ingot
	 * 
	 *         Credits to mikenon on GitHub!
	 */
	public static String prettifyText(String ugly) {
		if (!ugly.contains("_") && (!ugly.equals(ugly.toUpperCase())))
			return ugly;
		String fin = "";
		ugly = ugly.toLowerCase();
		if (ugly.contains("_")) {
			String[] splt = ugly.split("_");
			int i = 0;
			for (String s : splt) {
				i += 1;
				fin += Character.toUpperCase(s.charAt(0)) + s.substring(1);
				if (i < splt.length)
					fin += " ";
			}
		} else {
			fin += Character.toUpperCase(ugly.charAt(0)) + ugly.substring(1);
		}
		return fin;
	}

	/**
	 * Converts a serialized location to a Location. Returns null if string is
	 * empty
	 * 
	 * @param s
	 *            - serialized location in format "world:x:y:z"
	 * @return Location
	 */
	static public Location<World> getLocationString(final String s) {
		if (s == null || s.trim() == "") {
			return null;
		}
		final String[] parts = s.split(":");
		if (parts.length == 4) {
			final World w = Sponge.getServer().getWorld(parts[0]).get();
			if (w == null) {
				return null;
			}
			final int x = Integer.parseInt(parts[1]);
			final int y = Integer.parseInt(parts[2]);
			final int z = Integer.parseInt(parts[3]);
			return new Location<World>(w, x, y, z);
		}
		return null;
	}

	/**
	 * Converts a location to a simple string representation If location is
	 * null, returns empty string
	 * 
	 * @param location
	 * @return String of location
	 */
	static public String getStringLocation(final Location<World> location) {
		if (location == null || location.getExtent() == null) {
			return "";
		}
		return location.getExtent().getName() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":"
				+ location.getBlockZ();
	}

	/**
	 * Returns all of the items that begin with the given start, ignoring case.
	 * Intended for tabcompletion.
	 * 
	 * @param list
	 * @param start
	 * @return List of items that start with the letters
	 */
	public static List<String> tabLimit(final List<String> list, final String start) {
		final List<String> returned = new ArrayList<String>();
		for (String s : list) {
			if (s.toLowerCase().startsWith(start.toLowerCase())) {
				returned.add(s);
			}
		}

		return returned;
	}

	/**
	 * Gets a list of all players who are currently online.
	 * 
	 * @return list of online players
	 */
	public static List<String> getOnlinePlayerList() {
		final List<String> returned = new ArrayList<String>();
		final List<Player> players = PlayerCache.getOnlinePlayers();
		for (Player p : players) {
			returned.add(p.getName());
		}
		return returned;
	}
}
