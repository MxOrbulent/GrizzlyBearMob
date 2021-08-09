package com.aqupd.grizzlybear;

import com.aqupd.grizzlybear.entities.GrizzlyBearEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.aqupd.grizzlybear.utils.AqLogger.logInfo;

public class Main implements ModInitializer {

	public static final EntityType<GrizzlyBearEntity> GRIZZLYBEAR = Registry.register(
			Registry.ENTITY_TYPE,
			new Identifier("aqupd", "grizzly_bear"),
			FabricEntityTypeBuilder.create(SpawnGroup.CREATURE,
					GrizzlyBearEntity::new).dimensions(EntityDimensions.changing(1.4f, 1.4f)).build()
	);

	public static final Identifier ENTITY_GRIZZLY_BEAR_AMBIENT = new Identifier("aqupd:grizzly_bear.ambient");
	public static SoundEvent GRIZZLY_BEAR_AMBIENT = new SoundEvent(ENTITY_GRIZZLY_BEAR_AMBIENT);
	public static final Identifier ENTITY_GRIZZLY_BEAR_AMBIENT_BABY = new Identifier("aqupd:grizzly_bear.ambient_baby");
	public static SoundEvent GRIZZLY_BEAR_AMBIENT_BABY = new SoundEvent(ENTITY_GRIZZLY_BEAR_AMBIENT_BABY);
	public static final Identifier ENTITY_GRIZZLY_BEAR_DEATH = new Identifier("aqupd:grizzly_bear.death");
	public static SoundEvent GRIZZLY_BEAR_DEATH = new SoundEvent(ENTITY_GRIZZLY_BEAR_DEATH);
	public static final Identifier ENTITY_GRIZZLY_BEAR_HURT = new Identifier("aqupd:grizzly_bear.hurt");
	public static SoundEvent GRIZZLY_BEAR_HURT = new SoundEvent(ENTITY_GRIZZLY_BEAR_HURT);
	public static final Identifier ENTITY_GRIZZLY_BEAR_STEP = new Identifier("aqupd:grizzly_bear.step");
	public static SoundEvent GRIZZLY_BEAR_STEP = new SoundEvent(ENTITY_GRIZZLY_BEAR_STEP);
	public static final Identifier ENTITY_GRIZZLY_BEAR_WARNING = new Identifier("aqupd:grizzly_bear.warning");
	public static SoundEvent GRIZZLY_BEAR_WARNING = new SoundEvent(ENTITY_GRIZZLY_BEAR_WARNING);

	public static final SpawnEggItem GRIZZLY_BEAR_SPAWN_EGG = new SpawnEggItem(GRIZZLYBEAR, 8545340, 4139806, new FabricItemSettings().group(ItemGroup.MISC).fireproof().maxCount(64));
	//Generic stats of the monster.
	public static double GENERIC_MAX_HEALTH_CONFIG;
	public static double GENERIC_ATTACK_DAMAGE_CONFIG;
	public static double GENERIC_FOLLOW_RANGE_CONFIG; //Is also used for the rage distance.
	public static double GENERIC_MOVEMENT_SPEED_CONFIG;
    //Rage mode buff
	public static double PercentageAsDoubleForRageModeMSSpeedBuff;
	public static String DifficultyForRageMode;
	public static boolean DoSpawnRageParticles;
	public static boolean DoUseRageMode;

	@Override
	public void onInitialize() {
		Registry.register(Registry.SOUND_EVENT, Main.ENTITY_GRIZZLY_BEAR_AMBIENT, GRIZZLY_BEAR_AMBIENT);
		Registry.register(Registry.SOUND_EVENT, Main.ENTITY_GRIZZLY_BEAR_AMBIENT_BABY, GRIZZLY_BEAR_AMBIENT_BABY);
		Registry.register(Registry.SOUND_EVENT, Main.ENTITY_GRIZZLY_BEAR_DEATH, GRIZZLY_BEAR_DEATH);
		Registry.register(Registry.SOUND_EVENT, Main.ENTITY_GRIZZLY_BEAR_HURT, GRIZZLY_BEAR_HURT);
		Registry.register(Registry.SOUND_EVENT, Main.ENTITY_GRIZZLY_BEAR_STEP, GRIZZLY_BEAR_STEP);
		Registry.register(Registry.SOUND_EVENT, Main.ENTITY_GRIZZLY_BEAR_WARNING, GRIZZLY_BEAR_WARNING);

		Registry.register(Registry.ITEM, new Identifier("aqupd", "grizzly_bear_spawn_egg"), GRIZZLY_BEAR_SPAWN_EGG);
		FabricDefaultAttributeRegistry.register(GRIZZLYBEAR, com.aqupd.grizzlybear.entities.GrizzlyBearEntity.createGrizzlyBearAttributes());

		BiomeModifications.addSpawn(
				selection -> selection.getBiome().getCategory() == Biome.Category.TAIGA,
				SpawnGroup.CREATURE,
				GRIZZLYBEAR,
				60, 2, 4 // weight/min group size/max group size
		);
		//Calling this so that the spawn egg for the grizzly bear can properly be used with dispensers.
		DispenserBehavior.registerDefaults();
		//Do Config stuff here.
		//Check if this mod already has a config
		File config = new File("./config/grizzlybear.conf");
		if (!config.exists()) {
			createConfig(config);
		} else {
			loadConfig(config);
		}
		logInfo("[GrizzlyBearMod] has loaded!");
	}
	private void createConfig(File config) {
		Map<String, Boolean> booleanoptions = new HashMap<>();
		booleanoptions.put("DoUseRageMode",true);
		booleanoptions.put("DoSpawnRageParticles",true);
		Map<String, Double> doublevalues = new HashMap<>();
		doublevalues.put("MaxHealth",60D);
		doublevalues.put("AttackDamage",12D);
		doublevalues.put("Range",20D);
		doublevalues.put("MovementSpeed",0.30D);
		doublevalues.put("PercentageToIncreaseMovementSpeed",25D);
		Map<String, String> stringvalues = new HashMap<>();
		stringvalues.put("Difficulty","hard");

		BufferedWriter bf = null;;
		try {
			config.createNewFile();
			//If we have not gotten a exception yet, we continue writing to our newly created file.
			try{

				//create new BufferedWriter for the output file
				bf = new BufferedWriter( new FileWriter(config) );

				//iterate map entries
				bf.write("# This config file has been autogenerated by the GrizzlyBear Mod");
				bf.newLine();
				bf.write("# For Double values (the Percentage field is basically a double, but treat it as if you were writing percentages.");
				bf.newLine();
				bf.write("# For example, if you want the buff to be 50.5 %, then just write 50.5  Do not write 0.505 !");
				bf.newLine();
				bf.write("# I do not suggest adding so much more to the movementspeed, 0.35 is plenty fast.");
				bf.newLine();
				bf.write("# The # symbol at the start of the line denotes a comment, if you comment something out, default values will be used.");
				bf.newLine();
				bf.write("# But it might also break something so it's not recommended. If you for example want to turn off the Rage Mode, just set");
				bf.newLine();
				bf.write("# DoUseRageMode to false. DO NOT USE NEGATIVE VALUES OR AN EXACT 0 YOU WILL HAVE A BAD TIME AND BREAK THE GAME!");
				bf.newLine();
				for(Map.Entry<String, Double> entry : doublevalues.entrySet()){

					//put key and value separated by a colon
					bf.write( entry.getKey() + "=" + entry.getValue().toString().replaceAll("[^0-9.]", "").trim() );

					//new line
					bf.newLine();
				}
				bf.write("# Boolean options (true or false, do not be a smartass and write TrUe or TRUE or misspell)");
				bf.newLine();
				for(Map.Entry<String, Boolean> entry : booleanoptions.entrySet()){

					//put key and value separated by a colon
					bf.write( entry.getKey() + "=" + entry.getValue().toString() );

					//new line
					bf.newLine();
				}
				bf.write("# This basically sets the minimum difficulty required for the bear to be able to use ragemode, I'd suggest keeping it on hard.");
				bf.newLine();
				for(Map.Entry<String, String> entry : stringvalues.entrySet()){

					//put key and value separated by a colon
					bf.write( entry.getKey() + "=" + entry.getValue() );

					//new line
					bf.newLine();
				}

				bf.flush();

			}catch(IOException e){
				e.printStackTrace();
				logInfo("[GrizzlyBearMod] Something went wrong writing to "+config.getAbsolutePath());
				logInfo("[GrizzlyBearMod] Using default values!");
				Main.GENERIC_ATTACK_DAMAGE_CONFIG = 12D;
				Main.GENERIC_MAX_HEALTH_CONFIG = 60D;
				Main.GENERIC_FOLLOW_RANGE_CONFIG = 20D;
				Main.GENERIC_MOVEMENT_SPEED_CONFIG = 0.30D;
				Main.DifficultyForRageMode = "hard";
				Main.PercentageAsDoubleForRageModeMSSpeedBuff = 0.25D;
				Main.DoSpawnRageParticles = true;
				Main.DoUseRageMode = true;
				logInfo("[GrizzlyBearMod] Done setting default values!");
			}finally{

				try{
					//always close the writer
					bf.close();
				}catch(Exception e){}
			}
			//Now we load the newly created config.
			loadConfig(config);
		} catch (IOException e) {
			//Creating the file went to hell.
			e.printStackTrace();
			logInfo("[GrizzlyBearMod] Could not create the config file at "+config.getAbsolutePath());
			logInfo("[GrizzlyBearMod] Using default values!");
			Main.GENERIC_ATTACK_DAMAGE_CONFIG = 12D;
			Main.GENERIC_MAX_HEALTH_CONFIG = 60D;
			Main.GENERIC_FOLLOW_RANGE_CONFIG = 20D;
			Main.GENERIC_MOVEMENT_SPEED_CONFIG = 0.30D;
			Main.DifficultyForRageMode = "hard";
			Main.PercentageAsDoubleForRageModeMSSpeedBuff = 0.25D;
			Main.DoSpawnRageParticles = true;
			Main.DoUseRageMode = true;
			logInfo("[GrizzlyBearMod] Done setting default values!");
		}


	}
	private void loadConfig(File config) {
		List<String> lines = null;
		try {
			lines = Files.readAllLines(Paths.get(config.getAbsolutePath()), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();

		}
		if (lines != null) {
			lines.forEach(line -> {
				if (!line.startsWith("#")) {
					String[] splittedstring = line.split("=");
					String key = splittedstring[0].toString();
					String value = splittedstring[1].toString();
					double parsedDouble = Double.parseDouble(value.replaceAll("[^0-9.]", "").trim());
					switch (key) {
						case "MaxHealth": {
							Main.GENERIC_MAX_HEALTH_CONFIG = parsedDouble;

							break;
						}
						case "AttackDamage": {
							Main.GENERIC_ATTACK_DAMAGE_CONFIG = parsedDouble;

							break;
					}
						case "Range": {
							Main.GENERIC_FOLLOW_RANGE_CONFIG = parsedDouble;

							break;
				}
						case "MovementSpeed": {
							Main.GENERIC_MOVEMENT_SPEED_CONFIG = parsedDouble;

							break;
			}
						case "PercentageToIncreaseMovementSpeed": {
							double percentage = parsedDouble;
							double percentageasdouble = percentage / 100;
							Main.PercentageAsDoubleForRageModeMSSpeedBuff = percentageasdouble;
							break;
		}
						case "DoUseRageMode":
							if (value.toLowerCase().equals("true")) {
								Main.DoUseRageMode = true;
							} else if (value.toLowerCase().equals("false")) {
								Main.DoUseRageMode = false;
							} else {
								Main.DoUseRageMode = true;
							}
							break;
						case "DoSpawnRageParticles":
							if (value.toLowerCase().equals("true")) {
								Main.DoSpawnRageParticles = true;
							} else if (value.toLowerCase().equals("false")) {
								Main.DoSpawnRageParticles = false;
							} else {
								Main.DoSpawnRageParticles = true;
							}
							break;
						case "Difficulty":
							if (!value.equals("peacefull") || !value.equals("easy") || !value.equals("normal") || !value.equals("hard") || !value.equals("hardcore")) {
								Main.DifficultyForRageMode = "hard";
							} else {
								Main.DifficultyForRageMode = value;
							}

							break;
						default:

							break;
					}


				}
			});
			logInfo("[GrizzlyBearMod] Done loading config values from "+config.getAbsolutePath());
		} else {
			logInfo("[GrizzlyBearMod] Could not load the config file at "+config.getAbsolutePath());
			logInfo("[GrizzlyBearMod] Using default values!");
			Main.GENERIC_ATTACK_DAMAGE_CONFIG = 12D;
			Main.GENERIC_MAX_HEALTH_CONFIG = 60D;
			Main.GENERIC_FOLLOW_RANGE_CONFIG = 20D;
			Main.GENERIC_MOVEMENT_SPEED_CONFIG = 0.30D;
			Main.DifficultyForRageMode = "hard";
			Main.PercentageAsDoubleForRageModeMSSpeedBuff = 0.25D;
			Main.DoSpawnRageParticles = true;
			Main.DoUseRageMode = true;
			logInfo("[GrizzlyBearMod] Done setting default values!");
		}

	}

}
