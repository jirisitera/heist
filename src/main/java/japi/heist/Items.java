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
  public static final RegistryKey<Item> GAMEBAND_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Heist.MOD_ID, "gameband"));
  public static final Item GAMEBAND = Items.register(new Gameband(new Item.Settings().registryKey(GAMEBAND_KEY)), GAMEBAND_KEY);
  
  public static Item register(Item item, RegistryKey<Item> registryKey) {
    return Registry.register(Registries.ITEM, registryKey.getValue(), item);
  }
  
  public static void initialize() {
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
        .register((itemGroup) -> itemGroup.add(Items.GAMEBAND));
  }
}
