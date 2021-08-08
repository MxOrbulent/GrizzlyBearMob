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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public static double GENERIC_MAX_HEALTH;
	public static double GENERIC_ATTACK_DAMAGE;
	public static double GENERIC_FOLLOW_RANGE; //Is also used for the rage distance.
	public static double GENERIC_MOVEMENT_SPEED;
    //Rage mode buffs
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
			createConfig();
		} else {
			loadConfig(config);
		}
		logInfo("Grizzly Bears mod is loaded!");
	}
	private void createConfig() {
		Map<String, Boolean> booleanoptions = new HashMap<>();
		booleanoptions.put("DoUseRageMode",true);
		booleanoptions.put("DoSpawnRageParticles",true);
		Map<String, Double> doublevalues = new HashMap<>();
		doublevalues.put("MaxHealth",60D);
		doublevalues.put("AttackDamage",12D);
		doublevalues.put("Range",20D);
		doublevalues.put("MovementSpeed",20D);
		doublevalues.put("PercentageToIncreaseMovementSpeed",20D);
		Map<String, String> stringvalues = new HashMap<>();
		stringvalues.put("Difficulty","hard");
	}
	private void loadConfig(File config) {
		List<String> lines = null;
		try {
			lines = Files.readAllLines(Paths.get(config.getAbsolutePath()), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
			Main.GENERIC_ATTACK_DAMAGE = 12D;
			Main.GENERIC_MAX_HEALTH = 60D;
			Main.GENERIC_FOLLOW_RANGE = 20D;
			Main.GENERIC_MOVEMENT_SPEED = 0.35D;
			Main.DifficultyForRageMode = "hard";
			Main.PercentageAsDoubleForRageModeMSSpeedBuff = 0.25D;
			Main.DoSpawnRageParticles = true;
			Main.DoUseRageMode = true;
		}

		lines.forEach(line -> System.out.println(line));
		double percentage = 0;
		double percentageasdouble = percentage / 100;
		Main.PercentageAsDoubleForRageModeMSSpeedBuff = percentageasdouble;
	}

}
