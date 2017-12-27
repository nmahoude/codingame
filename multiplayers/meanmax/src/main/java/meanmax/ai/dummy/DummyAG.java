package meanmax.ai.dummy;

import meanmax.Game;
import meanmax.Player;
import meanmax.entities.Doof;
import meanmax.entities.Reaper;
import meanmax.entities.Wreck;
import meanmax.simulation.Action;
import trigo.Position;

/**
 * Straightforward ai
 */
public class DummyAG {
  private static final int NO_TARGET = -500_000;

  public Action[] actions = new Action[3];
  {
    actions[0] = new Action();
    actions[1] = new Action();
    actions[2] = new Action();
  }

  public void think(Player player) {
    dummyReaper(actions[0], player);
    dummyDestroyer(actions[1], player);
    dummyDoof(actions[2], player);
  }

  public void dummyReaper(Action action, Player player) {
    Position.random(action.target, Game.MAP_RADIUS);
    double minDist = Double.POSITIVE_INFINITY;
    Wreck closer = null;
    for (int i=0;i<Game.wrecks_FE;i++) {
      Wreck wreck = Game.wrecks[i];
      if (wreck.dead) continue;
      double dist2 = player.reaper.distance2(wreck);
      if (dist2 < minDist) {
        minDist = dist2;
        closer = wreck;
      }
    }
    if (closer != null) {
      // go to closest wreck
      action.target.x = closer.position.x - 1.5 * player.reaper.speed.vx;
      action.target.y = closer.position.y - 1.5 * player.reaper.speed.vy;
      action.thrust = 300;
    } else {
      // go to destroyer
      action.thrust = 0;
      action.target.x = player.destroyer.position.x - 1.5 * player.reaper.speed.vx;
      action.target.y = player.destroyer.position.y - 1.5 * player.reaper.speed.vy;
      action.thrust = 300;
    }
  }

  /*
   * Go to nearest tanker
   */
  public void dummyDestroyer(Action action, Player player) {
    action.thrust = 0;
//    double bestScore = Double.NEGATIVE_INFINITY;
//    Tanker best = null;
//    for (int i=0;i<Game.tankers_FE;i++) {
//      Tanker tanker = Game.tankers[i];
//      if (tanker.dead) continue;
//      double score = 1.0 -  player.destroyer.distance(tanker);
//      if (score > bestScore) {
//        bestScore = score;
//        best = tanker;
//      }
//    }
//    if (best != null) {
//      action.target.x = best.position.x - 1.5 * player.destroyer.speed.vx;
//      action.target.y = best.position.y - 1.5 * player.destroyer.speed.vy;
//      action.thrust = 300;
//    } else {
//      action.thrust = 0;
//    }
  }
  public void dummyDoof(Action action, Player player) {
    action.thrust = 0;// TODO no doof

    // go to nearest reaper 
    Doof doof = player.doof;
    Reaper closest = null;
    double bestDist = Double.MAX_VALUE;
    for (Player p: Game.players) {
      if (p == player) continue;
      double dist = p.reaper.distance2(doof);
      if (dist < bestDist) {
        bestDist = dist;
        closest = p.reaper;
      }
    }
    action.target.x = closest.position.x - 1.5 * doof.speed.vx;
    action.target.y = closest.position.y - 1.5 * doof.speed.vy;
    action.thrust = 300;
  }
  
  public void output() {
    actions[0].output();
    actions[1].output();
    actions[2].output();
  }

  public void compareEx() {
    // TODO Auto-generated method stub
    
  }

  public void saveExpected() {
    // TODO Auto-generated method stub
    
  }

}
