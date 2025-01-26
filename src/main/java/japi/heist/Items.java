package japi.heist;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class Items {
  private static final RegistryKey<Item> GAMEBAND_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Heist.MOD_ID, "gameband"));
  private static final RegistryKey<Item> PENCIL_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Heist.MOD_ID, "pencil"));
  private static final RegistryKey<Item> RUBBER_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Heist.MOD_ID, "rubber"));
  private static final Item GAMEBAND = Items.register(new Gameband(new Item.Settings().registryKey(GAMEBAND_KEY)), GAMEBAND_KEY);
  private static final Item PENCIL = Items.register(new Item(new Item.Settings().registryKey(PENCIL_KEY)), PENCIL_KEY);
  private static final Item RUBBER = Items.register(new Item(new Item.Settings().registryKey(RUBBER_KEY)), RUBBER_KEY);
  
  private Items() {
    throw new IllegalStateException("Not to be used directly");
  }
  
  private static Item register(Item item, RegistryKey<Item> registryKey) {
    return Registry.register(Registries.ITEM, registryKey.getValue(), item);
  }
  
  public static void initialize() {
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(group -> {
      group.add(GAMEBAND);
      group.add(PENCIL);
      group.add(RUBBER);
    });
  }
}
