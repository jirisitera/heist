package japi.heist;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class Gameband extends Item {
  public Gameband(Settings settings) {
    super(settings);
  }
  
  @Override
  public ActionResult use(World world, PlayerEntity user, Hand hand) {
    switch (Modes.CURRENT.getName()) {
      case "recharge":
        // Recharge the gameband
        break;
      case "hacking":
        // Hack the gameband
        break;
      case "lightning":
        // Strike lightning
        break;
    }
    return ActionResult.SUCCESS;
  }
}
