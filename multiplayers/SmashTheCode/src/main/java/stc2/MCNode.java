package stc2;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;

import utils.Cache;

public class MCNode {
  static final ThreadLocalRandom random = ThreadLocalRandom.current();
  private static final MCNode IMPOSSIBLE_NODE = new MCNode();
  static Cache<MCNode> cache = new Cache<>();
  static {
    for(int i=0;i<150_000;i++) {
      cache.push(new MCNode());
    }
  }
  
  public Simulation simulation = new Simulation();
  public BitBoard board = new BitBoard();
  
  Map<Integer, MCNode> childs = new HashMap<>();
  int color1;
  int color2;
  int simCount;
  private int rotation;
  private int column;
  
  private MCNode() {
    simulation.board = board;
  }
  
  
  public final void simulate(Game game, int depth, int maxDepth, int[] bestPointsAtDepth) {
    if (depth >= maxDepth) {
      return;
    }

    int key = getRandomKey();
    
    MCNode child = childs.get(key);
    if (child != null) {
      if (child == IMPOSSIBLE_NODE) {
        return;
      }
      child.simCount++;
      child.simulate(game, depth+1, maxDepth, bestPointsAtDepth);
    } else {
      if (!board.canPutBalls(rotation, column)) {
        childs.put(key, IMPOSSIBLE_NODE);
        return;
      }
      // build a child
      child = buildNewChild(rotation, column, depth, game);
      if (bestPointsAtDepth[depth] < child.simulation.points) {
        bestPointsAtDepth[depth] = child.simulation.points;
      }
      childs.put(key, child);
      child.simulate(game, depth+1, maxDepth, bestPointsAtDepth);
    }
  }


  final private int getRandomKey() {
    rotation = random.nextInt(color1 == color2 ? 2 : 4);
    column = random.nextInt(6);
    Integer key = (rotation + column*4);
    return key;
  }
  
  public MCNode buildNewChild(int rotation, int column, int depth, Game game) {
    MCNode child = get();
    child.simCount = 1;
    child.color1 = game.nextBalls[depth+1];
    child.color2 = game.nextBalls2[depth+1];
    child.board.copyFrom(this.board);
    child.simulation.putBallsNoCheck(color1, color2, rotation, column);
    return child;
  }
  
  public double getScore() {
    if (this == IMPOSSIBLE_NODE) {
      return -1_000_000;
    }
    if (simulation.points > 0 ) {
      return simulation.points 
          + getColorGroupScore()
          + getColumnScore();
    } else {
      return getColorGroupScore()
          + getColumnScore();
    }
  }
  
  private double getColorGroupScore() {
    return - 0*simulation.groupsCount[2]
           + 100*simulation.groupsCount[3]
           - 100*simulation.groupsCount[1];  
  }


  private double getColumnScore() {
    return 
        -2*simulation.board.getColHeight(0)
        -1*simulation.board.getColHeight(1)
        +2*simulation.board.getColHeight(2)
        +2*simulation.board.getColHeight(3)
        -1*simulation.board.getColHeight(4)
        -2*simulation.board.getColHeight(5);
  }


  double getBestScore() {
    if (childs.isEmpty()) {
      return getScore();
    } else {
      double maxScore = Integer.MIN_VALUE;
      for ( Entry<Integer, MCNode> childEntry : childs.entrySet()) {
        MCNode child = childEntry.getValue();
        double score = child.getBestScore();
        if (score > maxScore) {
          maxScore = score;
        }
      }
      return Math.max(0.8*maxScore, getScore());
    }
  }
  
  public static MCNode get() {
    if (cache.isEmpty()) {
      return new MCNode();
    } else {
      MCNode node = cache.pop();
      return node;
    }
  }
  
  
  public void release() {
    if (this == IMPOSSIBLE_NODE) {
      return; // don't give back IMP_NODE
    }
    
    for (MCNode child : childs.values()) {
      child.release();
    }
    childs.clear();
    simulation.clear();
    cache.retrocede(this);
  }
}
