package japi.heist;

import japi.heist.camera.CameraEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class Entities {
  private static final RegistryKey<EntityType<?>> CAMERA_KEY = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(Heist.MOD_ID, "camera"));
  public static final EntityType<CameraEntity> CAMERA = Registry.register(
      Registries.ENTITY_TYPE,
      Identifier.of(Heist.MOD_ID, "camera"),
      EntityType.Builder.create(CameraEntity::new, SpawnGroup.CREATURE).dimensions(1F, 1F).build(CAMERA_KEY)
  );
  
  private Entities() {
    throw new IllegalStateException("Not to be used directly");
  }
  
  public static void initialize() {
    FabricDefaultAttributeRegistry.register(CAMERA, CameraEntity.createCameraAttributes().build());
  }
}
