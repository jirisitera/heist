package japi.heist.gameband;

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
    switch (Modes.getCurrent().getName()) {
      case "Recharge":
        // Recharge the gameband
        break;
      case "Hacking":
        // Hack the gameband
        break;
      case "Sensor":
        // Use the sensor
        break;
      default:
        break;
    }
    return ActionResult.SUCCESS;
  }
}
