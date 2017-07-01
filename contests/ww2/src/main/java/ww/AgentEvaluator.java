package ww;

import ww.paths.AccessibleCellsCalculator;
import ww.paths.InfluenceMap;
import ww.paths.Voronoi;
import ww.sim.Simulation;

public class AgentEvaluator {
  GameState state;
  Agent agent;
  
  private static int FEATURE_END = 0;
  public static final int FEATURE_SCORE = FEATURE_END++;
  public static final int FEATURE_POSITION = FEATURE_END++;
  public static final int FEATURE_ELEVATION = FEATURE_END++;
  public static final int FEATURE_NEIGHBOURS = FEATURE_END++;
  public static final int FEATURE_ACTIONS = FEATURE_END++;
  public static final int FEATURE_CLIFF = FEATURE_END++;
  public static final int FEATURE_ACCESSIBLE_CELLS = FEATURE_END++;
  public static final int FEATURE_POTENTIAL_CELLS = FEATURE_END++;
  private static final double[] elevationScore = new double[]{ 1, 4, 9, 16 };
  double features[] = new double [FEATURE_END];
  static String[] featuresString = new String[]{
      "SCORE",
      "POSITION",
      "ELEVATION",
      "NEIGHBORS",
      "ACTIONS",
      "CLIFF",
      "ACCESSIBLE CELLS",
      "POTENTIAL CELLS",
  };
  
  AgentEvaluator(GameState state, Agent agent) {
    this.state = state;
    this.agent = agent;
  }
  
  public static double score(GameState state) {
    double score = 0.0;
    score += AgentEvaluator.score(state, state.agents[0]);
    score += AgentEvaluator.score(state, state.agents[1]);
    score -= AgentEvaluator.score(state, state.agents[2]);
    score -= AgentEvaluator.score(state, state.agents[3]);
    
    score += 200.0 * voronoi(state);
    //score += 5.0 * state.agents[0].position.manhattan(state.agents[1].position);
    return score;    
  }

  private static double influenceMap(GameState state) {
    InfluenceMap map[] = new InfluenceMap[4];
    for (int i=0;i<4;i++) {
      map[i] = new InfluenceMap();
      if (!state.agents[i].inFogOfWar()) {
        map[i].calculateInfluence(state, state.agents[i]);
      }
    }
    double score = 0;
    for (int y=0;y<GameState.size;y++) {
      for (int x=0;x<GameState.size;x++) {
        double cellValue = 1.0;
        for (int i=0;i<4;i++) {
          score += cellValue * map[i].influenceMap[x][y];
        }
      }
    }    
    return score;
  }

  private static double voronoi2(GameState state) {
    double score = 0.0;
    Voronoi v = new Voronoi();
    int cells[];
    if (!state.agents[2].inFogOfWar()) {
      cells = v.voronoi2(state, state.agents[0], state.agents[2]);
      score += (cells[0]-cells[1]);
      cells = v.voronoi2(state, state.agents[1], state.agents[2]);
      score += (cells[0]-cells[1]);
    }
    if (!state.agents[3].inFogOfWar()) {
      cells = v.voronoi2(state, state.agents[0], state.agents[3]);
      score += (cells[0]-cells[1]);
      cells = v.voronoi2(state, state.agents[1], state.agents[3]);
      score += (cells[0]-cells[1]);
    }
    if (Player.DEBUG_SCORING) {
      System.err.println("Voronoi : "+ cells);
    }
    return score;
  }

  private static double voronoi(GameState state) {
    double score = 0.0;
    Voronoi v = new Voronoi();
    int cells[] = v.voronoi4(state);
    score += (cells[0]+cells[1]-cells[2]-cells[3]);
    if (Player.DEBUG_SCORING) {
      System.err.println("Voronoi : "+ cells);
    }
    return score;
  }

  public static double score(GameState state, Agent agent) {
    if (agent.inFogOfWar()) return 0.0;
    
    AgentEvaluator ae = new AgentEvaluator(state, agent);

    ae.features[FEATURE_SCORE] = 20.0 * agent.score;
    ae.features[FEATURE_POSITION] = 1.0 * ae.position();
    ae.features[FEATURE_ELEVATION] = 50.0 * ae.elevation();
    ae.features[FEATURE_NEIGHBOURS] = 1.0 * ae.neighbouringElevation();
    ae.features[FEATURE_ACTIONS] =0.0; //1.0 * ae.countActions();
    ae.features[FEATURE_CLIFF] = 1.0 * ae.dangerousCliffs();
    ae.features[FEATURE_ACCESSIBLE_CELLS] = 0.0; //1.0 * ae.accessibleCells();
    ae.features[FEATURE_POTENTIAL_CELLS] = 0.0; // * ae.potentialCells();
    
    if (Player.DEBUG_SCORING) {
      System.err.println("Scores for agent "+agent.id);
      for (int i=0;i<FEATURE_END;i++) {
        System.err.println("    "+featuresString[i]+" = "+ae.features[i]);
      }
    }
    
    double score = 0.0;
    for (int i=0;i<FEATURE_END;i++) {
      score += ae.features[i];
    }
    
    return score;
  }

  private double potentialCells() {
    int count = AccessibleCellsCalculator.countWithoutLevel(state, agent);
    return count;
  }

  private double accessibleCells() {
    int count = AccessibleCellsCalculator.count(state, agent);
    if (count == 0) {
      return -10_000.0;
    }else {
      return count;
    }
//    if (count > 0)
//      return AccessibleCellsCalculator.countWithoutLevel(state, agent);
//    else {
//      return -10_000; // big malus
//    }
  }

  private double neighbouringElevation() {
    double score = 0.0;
    for (int i = 0; i < Dir.LENGTH; i++) {
        Cell checkCell = agent.cell.neighbors[i];
        int height = checkCell.height;
        if (height != 4 && height <= agent.cell.height + 1) {
            score += elevationScore[height]; //(1 + checkCell.height) * (1 + checkCell.height);
        }
    }
    return score;
}

  private int elevation() {
    return agent.cell.height * agent.cell.height;
  }

  /**
   * Big malus if we put ourself in a pushed position with a high cliff
   */
  double dangerousCliffs() {
    double score = 0.0;

    for (int i=0;i<4;i++) {
      if (agent.id  == i) continue;
      Agent other = state.agents[i];
      if (agent.isFriendly(other)) continue;
      if (other.inFogOfWar()) continue;
      if (!other.position.inRange(1, agent.position)) continue;
      
      Dir dir1 = other.cell.dirTo(agent.cell);
      for (Dir dir2  : dir1.pushDirections()) {
        Cell pushCell = agent.cell.get(dir2);
        if (pushCell.agent == null && pushCell.height < agent.cell.height-1) {
          score -= 1.0; // malus
        }
      }
    }
    return score;
  }

  double countActions() {
    int possibleActionsCount = Simulation.getPossibleActionsCount(state, agent);
    if (possibleActionsCount == 0) {
      return -100_000;
    }
    return possibleActionsCount;
  }

  double position() {
    int manhattanDistance = Math.abs(agent.position.x - GameState.size / 2) + Math.abs(agent.position.y - GameState.size / 2);
    return  -1.0*manhattanDistance; // malus if we are far from center
  }
}
