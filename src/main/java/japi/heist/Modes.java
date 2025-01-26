package japi.heist;

import net.minecraft.util.TriState;

import java.util.LinkedList;
import java.util.List;

public class Modes {
  protected static final List<Mode> ALL = new LinkedList<>();
  private static Mode current;
  
  private Modes() {
    throw new IllegalStateException("Not to be used directly");
  }
  
  public static Mode getCurrent() {
    return current;
  }
  
  public static void initialize() {
    Modes.ALL.add(new Mode("Stealth", "textures/block/stone_bricks.png"));
    Modes.ALL.add(new Mode("Stun", "textures/block/sand.png"));
    Modes.ALL.add(new Mode("Recharge", "textures/block/iron_ore.png"));
    Modes.ALL.add(new Mode("Hacking", "textures/block/stone.png"));
    Modes.ALL.add(new Mode("Sensor", "textures/block/diamond_ore.png"));
    Modes.locateCurrent();
  }
  
  public static void next() {
    if (Hud.getAnimate() != TriState.DEFAULT) {
      return;
    }
    Hud.setOffset(0);
    Hud.setAnimate(TriState.TRUE);
    Modes.ALL.addFirst(Modes.ALL.getLast());
  }
  
  public static void previous() {
    if (Hud.getAnimate() != TriState.DEFAULT) {
      return;
    }
    Hud.setOffset(0);
    Hud.setAnimate(TriState.FALSE);
    Modes.ALL.addLast(Modes.ALL.getFirst());
  }
  
  public static void locateCurrent() {
    current = Modes.ALL.get(Modes.ALL.size() / 2);
  }
}
