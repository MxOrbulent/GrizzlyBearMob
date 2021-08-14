package com.aqupd.grizzlybear;

import com.aqupd.grizzlybear.entities.GrizzlyBearEntity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.math.NumberUtils;

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

import static com.aqupd.grizzlybear.utils.AqLogger.logError;
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
	public static int DifficultyForRageMode;
	public static int RageModeTimeInTicks;
	public static boolean DoSpawnRageParticles;
	public static boolean DoUseRageMode;
	public static boolean DebugMod = false;
	public static boolean incorrectConfigValues = false;


	@Override
	public void onInitialize() {
		//Config options must be done before doing anything with the bear or it won't properly obtain values from main.
		//If the bear is not given proper values, for example 0 health. Then the bear dies in one hit and several things
		//break down and causes errors.
		//Do Config stuff here.
		//Check if this mod already has a config
		File config = new File("./config/grizzlybear.conf");
		if (!config.exists()) {
			logInfo("[GrizzlyBearMod] File does not exist. Creating.");
			createConfig(config);
		} else {
			logInfo("[GrizzlyBearMod] File exists. Loading.");
			loadConfig(config);
		}
		//Checking so the double attribute values are not below or equal to 0, if they are. Crash the server.
		if (GENERIC_MAX_HEALTH_CONFIG <= 0 || GENERIC_MOVEMENT_SPEED_CONFIG <= 0 || GENERIC_FOLLOW_RANGE_CONFIG <= 0 || GENERIC_ATTACK_DAMAGE_CONFIG <= 0) {
			if (DebugMod) {


			logError("Critical Error, you have entered 0 or below (negative number) (0.1 is still above zero) as a value for one or several attribute values");
			logInfo("In the config, they are named AttackDamage\n" +
					"MovementSpeed\n" +
					"Range\n" +
					"MaxHealth");
			logInfo("While we could have set them to default values for you, it's clearly not what you intended.");
			logInfo("Therefore, stopping the server is safer. If you need a new generation of a config if you really messed things up, simply delete the old one and run the server.");
			logInfo("If your intention was to simply have the bear die in one hit for real for example, just set MaxHealth to 0.1");
			logInfo("For your information, these are the values we got in the Main class after creating and loading, or loading the config:");
			if (GENERIC_MAX_HEALTH_CONFIG <= 0) {
				logError("GENERIC_MAX_HEALTH_CONFIG (MaxHealth) | "+GENERIC_MAX_HEALTH_CONFIG);
			} else {
				logInfo("GENERIC_MAX_HEALTH_CONFIG (MaxHealth) | "+GENERIC_MAX_HEALTH_CONFIG);
			}

				if (GENERIC_ATTACK_DAMAGE_CONFIG <= 0) {
					logError("GENERIC_ATTACK_DAMAGE_CONFIG (AttackDamage) | "+GENERIC_ATTACK_DAMAGE_CONFIG);
				} else {
					logInfo("GENERIC_ATTACK_DAMAGE_CONFIG (AttackDamage) | "+GENERIC_ATTACK_DAMAGE_CONFIG);
				}

				if (GENERIC_FOLLOW_RANGE_CONFIG <= 0) {
					logError("GENERIC_FOLLOW_RANGE_CONFIG (Range) | "+GENERIC_FOLLOW_RANGE_CONFIG);
				} else {
					logInfo("GENERIC_FOLLOW_RANGE_CONFIG (Range) | "+GENERIC_FOLLOW_RANGE_CONFIG);
				}

				if (GENERIC_MOVEMENT_SPEED_CONFIG <= 0) {

					logError("GENERIC_MOVEMENT_SPEED_CONFIG (MovementSpeed) | "+GENERIC_MOVEMENT_SPEED_CONFIG);
				} else {
					logInfo("GENERIC_MOVEMENT_SPEED_CONFIG (MovementSpeed) | "+GENERIC_MOVEMENT_SPEED_CONFIG);
				}




			logError("Done printing. incorrectConfigValues will be set to true, which means that the ");
			logError("Server (or client) will close very soon...");
			}
			incorrectConfigValues = true;


		}
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
		//Because it is a unique map, duplicates are not registering again, so this is perfectly safe to use.
		DispenserBehavior.registerDefaults();
		try {
			this.ClientLife();
		} catch (NoSuchMethodError e) {
			if (DebugMod) {


			logInfo("Mod is not in a client environment.");
			}
		}
		//hai
		try {
			this.ServerLife();
		} catch (NoSuchMethodError e) {
			if (DebugMod) {
				logInfo("Mod is not in a server environment..");
			}
		}


		logInfo("[GrizzlyBearMod] has loaded!");
	}
	@Environment(EnvType.CLIENT)
	public void ClientLife() {
		if (DebugMod) {
			logInfo("EnvType.CLIENT detected!");
		}

		ClientLifecycleEvents.CLIENT_STARTED.register(this::onClientStarted);
	}
	@Environment(EnvType.SERVER)
	public void ServerLife() {
		if (DebugMod) {
			logInfo("EnvType.SERVER detected!");
		}

		ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
	}




	private void onClientStarted(MinecraftClient minecraftClient) {
		System.out.println("Started client");

		MinecraftClient client = minecraftClient;


			if (Main.incorrectConfigValues) {
				logError("Closing the client due to incorrect config values. Do not use negative values or 0");
				logError("You will see some nasty stacktrace errors, but it's harmless.");
				logError("Just having some issues closing the client normally.");
				client.scheduleStop();
			}

	}
	private void onServerStarted(MinecraftServer minecraftServer) {
		//System.out.println("Started server");

		MinecraftServer server = minecraftServer;

		if (server.isRunning()) {
			if (Main.incorrectConfigValues) {

				logError("Closing the server because of incorrect config values. Do not use negative values or 0");
				server.getCommandManager().execute(server.getCommandSource(),"/stop");
			}
		}


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
		Map<String, Integer> integervalues = new HashMap<>();
		integervalues.put("RageModeInTicks",600);
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


					bf.write( entry.getKey() + "=" + entry.getValue().toString().replaceAll("[^0-9.]", "").trim() );


					bf.newLine();
				}
				bf.write("# Set how many ticks the rage mode will last for when activated (it does not decrease if the bear has a player target)");
				bf.newLine();
				bf.write("# 1 second = 20 ticks. Default is 600 ticks (30 seconds)");
				bf.newLine();
				for(Map.Entry<String, Integer> entry : integervalues.entrySet()){


					bf.write( entry.getKey() + "=" + entry.getValue().toString().replaceAll("[^0-9.]", "").trim() );


					bf.newLine();
				}

				bf.write("# Boolean options (true or false, do not be a smartass and write TrUe or TRUE or misspell)");
				bf.newLine();
				for(Map.Entry<String, Boolean> entry : booleanoptions.entrySet()){


					bf.write( entry.getKey() + "=" + entry.getValue().toString() );


					bf.newLine();
				}
				bf.write("# This basically sets the minimum difficulty required for the bear to be able to use ragemode, I'd suggest keeping it on hard.");
				bf.newLine();
				for(Map.Entry<String, String> entry : stringvalues.entrySet()){


					bf.write( entry.getKey() + "=" + entry.getValue() );


					bf.newLine();
				}

				bf.flush();

			}catch(IOException e){
				e.printStackTrace();
				logInfo("Something went wrong writing to "+config.getAbsolutePath());
				logInfo("Using default values!");
				Main.GENERIC_ATTACK_DAMAGE_CONFIG = 12D;
				Main.GENERIC_MAX_HEALTH_CONFIG = 60D;
				Main.GENERIC_FOLLOW_RANGE_CONFIG = 20D;
				Main.GENERIC_MOVEMENT_SPEED_CONFIG = 0.30D;
				Main.RageModeTimeInTicks = 600;
				Main.DifficultyForRageMode = 3;
				Main.PercentageAsDoubleForRageModeMSSpeedBuff = 0.25D;
				Main.DoSpawnRageParticles = true;
				Main.DoUseRageMode = true;
				logInfo("Done setting default values!");
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
			logInfo("Could not create the config file at "+config.getAbsolutePath());
			logInfo("Using default values!");
			Main.GENERIC_ATTACK_DAMAGE_CONFIG = 12D;
			Main.GENERIC_MAX_HEALTH_CONFIG = 60D;
			Main.GENERIC_FOLLOW_RANGE_CONFIG = 20D;
			Main.GENERIC_MOVEMENT_SPEED_CONFIG = 0.30D;
			Main.RageModeTimeInTicks = 600;
			Main.DifficultyForRageMode = 3;
			Main.PercentageAsDoubleForRageModeMSSpeedBuff = 0.25D;
			Main.DoSpawnRageParticles = true;
			Main.DoUseRageMode = true;
			logInfo("Done setting default values!");
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
					if (DebugMod) {
						logInfo("key is: "+key);
						logInfo("value is: "+value);
					}

					double parsedDouble = 0;
					if (NumberUtils.isCreatable(value.replaceAll("[^0-9.]", "").trim())) {
						parsedDouble = Double.parseDouble(value.replaceAll("[^0-9.]", "").trim());
					}

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
						case "RageModeInTicks": {
							Main.RageModeTimeInTicks = Integer.valueOf(value.replaceAll("[^0-9]", "").trim());

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

								int valueAsInteger = -1;
								switch (value) {
									case "peaceful":
										valueAsInteger = 0;
										break;
									case "easy":
										valueAsInteger = 1;
										break;
									case "normal":
										valueAsInteger = 2;
										break;
									case "hard":
										valueAsInteger = 3;
										break;
									default:
										 valueAsInteger = 3;
										break;
								}
								Main.DifficultyForRageMode = valueAsInteger;


							break;
						default:

							break;
					}


				}
			});
			logInfo("Done loading config values from "+config.getAbsolutePath());
		} else {
			logInfo("Could not load the config file at "+config.getAbsolutePath());
			logInfo("Using default values!");
			Main.GENERIC_ATTACK_DAMAGE_CONFIG = 12D;
			Main.GENERIC_MAX_HEALTH_CONFIG = 60D;
			Main.GENERIC_FOLLOW_RANGE_CONFIG = 20D;
			Main.GENERIC_MOVEMENT_SPEED_CONFIG = 0.30D;
			Main.RageModeTimeInTicks = 600;
			Main.DifficultyForRageMode = 3;
			Main.PercentageAsDoubleForRageModeMSSpeedBuff = 0.25D;
			Main.DoSpawnRageParticles = true;
			Main.DoUseRageMode = true;
			logInfo("Done setting default values!");
		}

	}

}
