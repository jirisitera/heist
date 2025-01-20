package japi.heist;

import net.minecraft.util.TriState;

import java.util.LinkedList;

public class Modes {
  public static final LinkedList<Mode> ALL = new LinkedList<>();
  public static Mode CURRENT;
  
  public static void initialize() {
    Modes.ALL.add(new Mode("Stealth", "textures/block/stone_bricks.png"));
    Modes.ALL.add(new Mode("Stun", "textures/block/sand.png"));
    Modes.ALL.add(new Mode("Recharge", "textures/block/deepslate.png"));
    Modes.ALL.add(new Mode("Hacking", "textures/block/stone.png"));
    Modes.ALL.add(new Mode("Sensor", "textures/block/diamond_ore.png"));
    Modes.locateCurrent();
  }
  
  public static void next() {
    if (Hud.ANIMATE != TriState.DEFAULT) {
      return;
    }
    Hud.OFFSET = 0;
    Hud.ANIMATE = TriState.TRUE;
    Modes.ALL.addFirst(Modes.ALL.getLast());
  }
  
  public static void previous() {
    if (Hud.ANIMATE != TriState.DEFAULT) {
      return;
    }
    Hud.OFFSET = 0;
    Hud.ANIMATE = TriState.FALSE;
    Modes.ALL.addLast(Modes.ALL.getFirst());
  }
  
  public static void locateCurrent() {
    CURRENT = Modes.ALL.get(Modes.ALL.size() / 2);
  }
}
