package cotc.ai.ag;

import java.util.Map;
import java.util.Random;

import cotc.GameState;
import cotc.ai.AISolution;
import cotc.entities.Action;
import cotc.entities.Ship;
import cotc.game.Simulation;
import cotc.utils.Coord;

public class AGSolution implements AISolution{
  public final static int AGACTION_SIZE = 1000;
  private static final Action[] ACTION_VALUES = Action.values();
  public static int DEPTH = 5;
  public static Random rand = new Random();

  public static double patience[] = new double[]{ 0.0, 0.0, 0.0, 0.0, 0.0};
  
  public ShipActions actions[] = new ShipActions[DEPTH];
  protected int shipCount;
  
  protected GameState state;
  public double energy;
  private FeatureWeight weights;
 

  protected AGSolution() {
  }
  
  public AGSolution(GameState state, FeatureWeight weights) {
    this.state = state;
    this.weights = weights;
    this.shipCount = state.teams[0].shipsAlive.size();
    for (int i=0;i<DEPTH;i++) {
      actions[i] = new ShipActions();
    }
  }
  
  @Override
  public String[] output() {
    String[] output = new String[shipCount];
    int i=0;
    for (int s=0;s<state.teams[0].shipsAlive.FE;s++) {
      output[i++] = actions[0].actions[s].toString();
    }
    return output;
  }

  public void randomize(GameState state, StateAnalyser analyser) {
    for (int s=0;s<state.teams[0].shipsAlive.FE;s++) {
      for (int i=0;i<DEPTH;i++) {
        Ship ship = state.teams[0].shipsAlive.elements[s];
        ShipStateAnalysis shipAnalysis = analyser.analyse.get(ship);
        Action action;
      
        if (i == 0) {
          action = ACTION_VALUES[rand.nextInt(ACTION_VALUES.length)];
          Coord target = Simulation.COORD_ZERO;
          
          // eliminate impossible actions
          if (action == Action.SLOWER && ship.speed == 0) {
            action = Action.FASTER;
          }
          if (action == Action.FASTER && ship.speed == 2) {
            action = Action.WAIT;
          }
          if (action == Action.MINE) {
            action = Action.WAIT;
            // TODO enemy ship can brake, but we drop a mine anyway
            if (shipAnalysis.enemyAtStern[0] == true) {
              action = Action.MINE;
            } else if (shipAnalysis.enemyAtStern[1] == true && shipAnalysis.closestEnemy.speed == 2) {
              action = Action.MINE;
            }
          }
          if (action == Action.FIRE) {
            ShipStateAnalysis otherShipAnalysis = analyser.analyse.get(shipAnalysis.closestEnemy);
            if (shipAnalysis.closestEnemy.position.distanceTo(ship.position) <= 3) {
              // target the next position of the ship
              Coord c1 = shipAnalysis.closestEnemy.position.neighborsCache[shipAnalysis.closestEnemy.orientation];
              Coord c2 = c1.neighborsCache[shipAnalysis.closestEnemy.orientation];
              if (shipAnalysis.closestEnemy.speed == 2 && c2.isInsideMap()) {
                target = c2;
              } else {
                target = c1;
              }
            }
            if (otherShipAnalysis.canMove[0] == false) {
              target = shipAnalysis.closestEnemy.position;
            } else {
              action = Action.WAIT;
            }
          }
          actions[i].actions[s] = new AGAction(action, target);
        } else {
          // other turns is more random
          action = ACTION_VALUES[rand.nextInt(ACTION_VALUES.length)];
          if (action == Action.FIRE) {
            action = Action.WAIT;
          }
          actions[i].actions[s] = new AGAction(action, null);
        }
      }
    }
  }
  
  public void debugOutput() {
    System.err.println("Actions ");
    
    for (int sa=0;sa<DEPTH;sa++) {
      ShipActions shipActions = actions[sa];
      System.err.print("  For "+sa);
      for (AGAction action : shipActions.actions) {
        if (action != null) {
          System.err.print(action.toString()+", ");
        }
      }
      System.err.println();
    }
  }
  public static AGSolution createFake() {
    AGSolution fake = new AGSolution();
    fake.energy = Double.NEGATIVE_INFINITY;
    return fake;
  }

  public void updateEnergyTurn(int turn, GameState state) {
    // ATM, the health with a patience coef is not a good result (really not !)
  }

  public void updateEnergyEnd(GameState state) {
    // feature after turn DEPTH, less precise, but more insight
    Feature feature = new Feature();
    feature.calculateFeatures(state);
    energy += feature.applyWeights(weights);
  }

  @Override
  public void setEnergy(int energy) {
    this.energy = energy;
  }

  @Override
  public void resetEnergy() {
    this.energy = 0;
  }
  
  @Override
  public ShipActions[] getActionsNew() {
    return actions;
  }

  @Override
  public Map<Ship, AGAction[]> getActions() {
    // TODO Auto-generated method stub
    return null;
  }
}
