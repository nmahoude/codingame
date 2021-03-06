

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

class Player {
  static Scanner in;
  
  static final int ENTITY_PLAYER = 0;
  static final int ENTITY_BOMB = 1;
  static final int ENTITY_ITEM = 2;

  static int MOVE_RIGHT = 0;
  static int MOVE_DOWN = 1;
  static int MOVE_LEFT = 2;
  static int MOVE_UP = 3;
  static int MOVE_STAY = 4;
  
  static int[] rotx = { 1, 0, -1, 0, 0 };
  static int[] roty = { 0, 1, 0, -1, 0 };
  static String[] rotString = { "RIGHT", "DOWN ", "LEFT ", "UP   ", "STAY " };

  static class Entity  {
    GameState state;
    public Entity(GameState state, int type, int owner, int x, int y) {
      this.state = state; // the state we deal with
      this.type = type;
      this.owner = owner;
      p = new P(x,y);
    }
    int type;
    int owner;
    P p;
    public void update(GameState state) {
      
    }
    public Entity duplicate(GameState newState) {
      return null;
    }
  }
  static class APlayer extends Entity {
    public APlayer(GameState state, int owner, int x, int y, int param1, int param2) {
      super(state, ENTITY_PLAYER, owner, x, y);
      bombsLeft = param1;
      bombRange = param2;
      isDead = false;
    }
    int bombRange = 3;
    int bombsLeft = 1;
    public int points = 0;
    boolean isDead = false;
    public int droppedBombs = 0;
    
    int getBoxesInRange() { 
      int boxes = 0; 
      for (int rot=0;rot<4;rot++) { 
        for (int d=1;d<bombRange;d++) { 
          int x = p.x+d*rotx[rot]; 
          int y = p.y+d*roty[rot]; 
          int value = state.getCellAt(x, y); 
          if (value == GameState.CELL_EMPTY_BOX || value == GameState.CELL_BOMBUP_BOX || value == GameState.CELL_RANGEUP_BOX) { 
            boxes++; 
            break; 
          } else if (value == GameState.CELL_WALL  
              || value == GameState.CELL_ITEM_BOMBUP 
              || value == GameState.CELL_ITEM_RANGEUP 
              || value == GameState.CELL_BOMB_0 
              || value == GameState.CELL_BOMB_1 
              || value == GameState.CELL_BOMB_2 
              || value == GameState.CELL_BOMB_3 
              || value == GameState.CELL_BOMB_4 
              || value == GameState.CELL_BOMB_5 
              || value == GameState.CELL_BOMB_6 
              || value == GameState.CELL_BOMB_7 
              || value == GameState.CELL_BOMB_8 
              || value == GameState.CELL_BOMB_9 
              ) { 
            break; 
          } 
        } 
      } 
      return boxes; 
    } 

    
    int getTotalBombs() {
      return bombsLeft + droppedBombs;
    }
    @Override
    public void update(GameState state) {
      pickupBonuses(state);
      
      super.update(state);
    }
    void pickupBonuses(GameState state) {
      int value = state.grid[p.x+13*p.y];
      if (GameState.isRangeUpItem(value)) {
        bombRange++;
        state.grid[p.x+13*p.y] = GameState.CELL_FLOOR;
      } else if (GameState.isBombUpItem(value)) {
        bombsLeft++;
        state.grid[p.x+13*p.y] = GameState.CELL_FLOOR;
      }
    }
    
    public Entity duplicate(GameState newState) {
      APlayer aPlayer = new APlayer(newState, owner, p.x, p.y, bombsLeft, bombRange);
      aPlayer.points = this.points;
      
      aPlayer.droppedBombs = this.droppedBombs;
      aPlayer.isDead = this.isDead;
      
      return aPlayer;
    }
    boolean isDead() {
      return isDead;
    }
    void setDead() {
      isDead = true;
    }
    public void dropBomb() {
      Bomb droppedBomb = new Bomb(state, owner, p.x,p.y, 8, bombRange);
      state.addEntity(droppedBomb);
      this.bombsLeft-=1;
      this.droppedBombs+=1;
    }


    public void moveTo(int x, int y) {
      p = P.get(x, y);
      pickupBonuses(this.state);
    }
  }

   static class Bomb extends Entity {
    public Bomb(GameState state, int owner, int x, int y, int ticksLeft, int range) {
      super(state, ENTITY_BOMB, owner, x, y);
      this.ticksLeft=ticksLeft;
      this.range=range;
      
      // update state grid
      updateStateGrid();
    }
    public Entity duplicate(GameState newState) {
      return new Bomb(newState, owner, p.x, p.y, ticksLeft, range);
    }

    int ticksLeft;
    int range;
    
    public void update(GameState state) {
      if (ticksLeft == 0){
        return; // already triggered
      }
      ticksLeft--;
      updateStateGrid();

      if (ticksLeft <= 0) {
        explode(state);
      } else {
        affectBoxInfluenza(state);
      }
    }
    void updateStateGrid() {
      if (ticksLeft == 0) {
        state.grid[p.x+13*p.y] = GameState.CELL_FIRE;
        state.fireCells.add(p);
      } else {
        state.grid[p.x+13*p.y] = GameState.CELL_BOMB_0+ticksLeft;
      }
    }

    void affectBoxInfluenza(GameState state) {
      for (int rot=0;rot<4;rot++) {
        for (int d=1;d<range;d++) {
        }
      }
    }

    void updatePlayerDeath(P position) {
      for (int p=0;p<4;p++) {
        if (state.players[p] != null 
            && state.players[p].p.equals(position)) {
          state.players[p].setDead();
        }
      }
    }
    public void explode(GameState state) {
      if (state.players[owner] != null) {
        state.players[owner].bombsLeft++;
        state.players[owner].droppedBombs--;
      }
      updatePlayerDeath(this.p);
      
      for (int rot=0;rot<4;rot++) {
        for (int d=1;d<range;d++) {
          int testedX = p.x+d*rotx[rot];
          int testedY = p.y+d*roty[rot];
          
          int value = state.getCellAt(testedX, testedY);
          if (GameState.explosionBlocked(value)) {
            break;
          }

          P testedP = P.get(testedX, testedY);
          updatePlayerDeath(testedP);

          if (GameState.explosionSoftBlocked(value)) {
            if (GameState.isABomb(value)) {
              state.triggerBomb(testedP);
            } else if (GameState.isABox(value)) {
              state.hittedBoxes.add(testedP);
              if (state.players[owner] != null) {
                state.players[owner].points ++;
              }
            } else if (GameState.isAnItem(value)) {
              state.removeItem(testedP);
            }
            break;
          }
          state.grid[testedP.x+13*testedP.y] = GameState.CELL_FIRE;
          state.fireCells.add(testedP);
        }
      }
    }
  }
  static class Item extends Entity {
    final static int RANGE_UP = 1;
    final static int BOMB_UP = 2;
    int param2;
    
    public Item(GameState state, int owner, int x, int y, int param1, int param2) {
      super(state, ENTITY_ITEM, owner, x, y);
      option = param1;
      this.param2 = param2;// param2 not used
      updateStateGrid(); 
    } 
    private void updateStateGrid() { 
      state.grid[p.x+13*p.y] = GameState.CELL_ITEM_RANGEUP; 
    }    
    public Entity duplicate(GameState newState) {
      return new Item(newState, owner, p.x, p.y, option, param2);
    }

    int option;
  }  
  
  static class Action {
    P pos;
    boolean dropBomb;
    String message;
    
    String get() {
      return (dropBomb ? "BOMB" : "MOVE") + " "+ pos.x +" "+ pos.y +" "+ message; 
    }
  }
  
  static abstract class AI {
    Game game;
    enum Mode {
      CRUISING,
      DEFENSIVE,
      OFFENSIVE
    }
    Mode mode = Mode.CRUISING;
    List<APlayer> offensive = new ArrayList<>();
  
    List<Action> actions = new ArrayList<>();
    
    abstract void compute();
  }
  
  static abstract class MovementAlgorithm {
    enum AlgoType {
      FAIR,
      BOX,
      AGGRESSIVE, EARLY
    }
    AlgoType type;
    public MovementAlgorithm(AlgoType type) {
      this.type = type;
    }
    abstract Integer getBestChild(MCTS root);
    abstract int compute(APlayer player, GameState fromState, int[] possibilities);
    
    void computeBombs(APlayer player, GameState fromState, int p, boolean[] bombs) {
      boolean canDropBombOnBoard = !GameState.isABomb(fromState.grid[player.p.x+13*player.p.y]);
      if (!canDropBombOnBoard) {
        bombs[p] = false;
      } else {
        int THRESHOLD = 500;
        if (fromState.boxes.size() == 0) {
          if (player.p.x % 2 == 0 && player.p.y % 2 == 0) {
            THRESHOLD = 200;
          }
        }
        bombs[p] = ThreadLocalRandom.current().nextInt(1000) > THRESHOLD;
      }
    }
  }
  
  
  static class EarlyGameAlgorithm extends MovementAlgorithm {
    static final double SCORE_MINUS_INFINITY = -1_000_000;

    enum GameType {
      EARLY,
      MIDDLE,
      LATE
    }
    static GameType gametype = GameType.EARLY;
    
    public EarlyGameAlgorithm() {
      super(AlgoType.EARLY);
    }
    
    static double getScore(MCTS node) {
      if (node.childs.isEmpty()) {
        // score
        if (node.playerIsDead ) {
          return -1;
        } else {
          if (gametype == GameType.EARLY) {
            node.score = 10*node.points + 2*node.totalBombs + node.bombRange;
          } else if (gametype == GameType.MIDDLE) {
            node.score = 2*node.points + node.totalBombs + node.bombRange;
          } else {
            node.score = 1;
          }
          return node.score;
        }
      } else {
        if (gametype == GameType.LATE) {
          double score = SCORE_MINUS_INFINITY;
          int oppIndex = 0;
          for (int p=0;p<4;p++) {
            if (MCTS.game.myIndex != p && MCTS.game.currentState.players[p] != null) {
              oppIndex = p;
            }
          }
          P myPos = MCTS.game.currentState.players[MCTS.game.myIndex].p;
          P oppPos = MCTS.game.currentState.players[oppIndex].p;
          int oldDist = myPos.manhattanDistance(oppPos);
          
          int rot = node.key / 2;
          int bomb = (node.key+1) % 2;
          int newX = myPos.x + rotx[rot];
          int newY = myPos.y + roty[rot];
          
          int newDist = Math.abs(newX - oppPos.x) + Math.abs(newY - oppPos.y);
          System.err.println("lateDist : "+MCTSAI.keyToString(node.key, MCTS.game.myIndex)+ " : "+oldDist+"->"+newDist);
          score = Math.max(score, (2+(oldDist-newDist)+node.points+bomb) * (1.0*node.win / node.simulatedCount));
          return score;
        } else {
          double score = SCORE_MINUS_INFINITY;
          for (Entry<Integer, MCTS> m : node.childs.entrySet()) {
            //TODO bien y penser, on bypasse tous les infinity max !
            // il faut peut-etre prendre en compt le % de win dans l'heuristique?
            double ratio = 0.95;
  //          if (node.depth == 1) {
  //            ratio = (1.0*node.win / node.simulatedCount);
  //          }
            score = Math.max(score, ratio*(10*node.points+1) * getScore(m.getValue())); 
          }
          node.score = score;
          return score;
        }
      }
    }
    
    Integer getBestChild(MCTS root) {
      Entry<Integer, MCTS> bestEntry = null;
      double bestScore = SCORE_MINUS_INFINITY;
      
      for (Entry<Integer, MCTS> m : root.childs.entrySet()) {
        MCTS mcts = m.getValue();
        double ratio = 1.0*mcts.win / mcts.simulatedCount;
        double score = getScore(mcts);
        double testedScore = /*ratio * */score;
        if (testedScore > bestScore) {
          bestScore = testedScore;
          bestEntry = m;
        }
      }
      return bestEntry != null ? bestEntry.getKey() : null;
    }

    @Override
    int compute(APlayer player, GameState fromState, int[] possibilities) {
      possibilities[4] = 1; // stay
      int total = 1;
      for (int i = 0; i < 4; i++) {
        int px = player.p.x + rotx[i];
        int py = player.p.y + roty[i];
        if (px >= 0 && px < 13 && py >= 0 & py < 11) {
          int valueAtCell = fromState.grid[px+13*py];
          if (GameState.canWalkThrough(valueAtCell)) {
            possibilities[i] = 1;
            total+=1;
          } else {
            possibilities[i] = 0;
          }
        } else {
          possibilities[i] = 0;
        }
      }
      return total;
    }
    
    @Override
    void computeBombs(APlayer player, GameState fromState, int p, boolean[] bombs) {
      boolean canDropBombOnBoard = !GameState.isABomb(fromState.grid[player.p.x+13*player.p.y]); 
      if (canDropBombOnBoard) { 
        if (player.owner == MCTS.game.myIndex) { 
          int bombsInRange = player.getBoxesInRange(); 
          if (bombsInRange > 0 || EarlyGameAlgorithm.gametype == EarlyGameAlgorithm.GameType.LATE) { 
            bombs[p] = ThreadLocalRandom.current().nextInt(1000) > 500 ; 
          } else { 
            bombs[p] = false; 
          } 
        } else { 
          bombs[p] = ThreadLocalRandom.current().nextInt(1000) > 500; 
        } 
      } else { 
        bombs[p] = false; 
      }
    }
  }
  
  static class MCTS {

    //static MovementAlgorithm biasedMovementAlgorithm = new BoxedOrientedPossibilitiesAlgorithm();
    static MovementAlgorithm biasedMovementAlgorithm = new EarlyGameAlgorithm();
    // debug : start with the aggressive algorithm
    //static MovementAlgorithm biasedMovementAlgorithm = new AggressiveMovementAlgorithm();
    
    static Game game; // static reference to the game (only one). Better way to do it ?
    Map<Integer, MCTS> childs = new HashMap<>();
    
    public double score;
    public int depth;
    int simulatedCount=0; // how many branches (total > childs.size())
    int win=0;
    int points;
    int totalPoints;
    public boolean playerIsDead = false;
    public int bombRange;
    public int totalBombs;
    Integer key;

    public MCTS(Integer key) {
      this.key = key;
    }
    static int [] possibilities = new int[5]; // dont' parralelize !
    static {
      possibilities[4] = 1; // don't move is the 1st choice!
    }
    int findARandomMove() {
      int sum = 0;
      for (int i=0;i<5;i++) {
        sum+=possibilities[i];
      }
      if (sum == 1) {
        return 4; // STAY
      } else {
        int rand = getRandom(sum);
        int i;
        for(i = 0;i<5;i++) {
          rand=rand-possibilities[i];
          if (rand < 0) {
            break;
          }
        }
        return i;
      }
    }
    
    int getRandom(int range) {
      return ThreadLocalRandom.current().nextInt(range);
    }
    void calculateActions(GameState fromState, int[] dir, boolean[] bomb) {
      for (int p=0;p<game.playersCount;p++) {
        APlayer player = fromState.players[p];
        if (player == null) {
          continue ;
        }
        biasedMovementAlgorithm.compute(player, fromState, possibilities);

        dir[p] = findARandomMove();
        
        if (player.bombsLeft > 0) {
          biasedMovementAlgorithm.computeBombs(player, fromState, p, bomb);
        } else {
          bomb[p] = false;
        }
      }
    }
    Integer getKeyFromActions(int[] dir, boolean bomb[]) {
//      String key = "";
//      String myKey = dir[game.myIndex]+(bomb[game.myIndex] ? "B" : "M");
//      key = myKey+myKey+myKey+myKey;
//      return key;
//      
      return 2 * dir[game.myIndex]+(bomb[game.myIndex] ? 1 : 0);
    }

    int simulate(GameState fromState, int depth) {
      this.depth = fromState.depth;
      this.simulatedCount++;
      fromState.depth++;

      // if we already now that the player will be dead, no excuse to continue this road
      if (playerIsDead) {
        return -1;
      }
      //do simulation here
      fromState.computeRound_MCTS();
      APlayer aPlayer = fromState.players[Game.myIndex];
      points = aPlayer.points;
      totalPoints += points;

      if (aPlayer.isDead()) {
        playerIsDead = true;
        return -1;
      }
      
      totalPoints = aPlayer.points;
      totalBombs = aPlayer.getTotalBombs();
      bombRange = aPlayer.bombRange;

      if (depth == 0) {
        // terminating node
        // total box killed by player to this leaf
        return 1;
      } else {
        // choose some new random moves
        int[] dir = new int[5];
        boolean[] bomb = new boolean[5];
        
        boolean simulationDone = false;
        int maxTests = 10;
        Integer key = null;
        MCTS chosenChild = null;
        // Find a non final child node
        while (!simulationDone && maxTests > 0) {
          maxTests--;
          calculateActions(fromState, dir, bomb);
          // prepare child
          key = getKeyFromActions(dir, bomb);
          chosenChild = childs.get(key);
          if (chosenChild == null || !chosenChild.playerIsDead) {
            simulationDone = true;
          }
        }

        for (int playerIndex=0;playerIndex<game.playersCount;playerIndex++) {
          int theRot = dir[playerIndex];
          APlayer player = fromState.players[playerIndex];
          if (player == null) {
            continue;
          }
          // drop bomb if needed
          if (bomb[playerIndex]) {
            player.dropBomb();
          }
          // then move
          player.moveTo(player.p.x + rotx[theRot], player.p.y + roty[theRot]);
          
        }

        if (chosenChild != null) {
          // we already go there, reuse the child !
        } else {
          // create a new one
          chosenChild = new MCTS(key);
          childs.put(key, chosenChild);
        }
        int childTotalPoints = chosenChild.simulate(fromState, depth-1);
        if (childTotalPoints >= 0) {
          totalPoints+=childTotalPoints;
          win++;
          return points+childTotalPoints;
        } else {
          return -1;
        }
      }
    }
    boolean victoryFromPoints(GameState fromState) {
      APlayer me = fromState.players[Game.myIndex];
      for (int i=1;i<4;i++) {
        if (fromState.players[i] != null && me.points < fromState.players[i].points) {
          return false;
        }
      }
      win++; // don't forget to count our victory
      return true; // we won !
    }
    
    @Override
    public String toString() {
      String dir = MCTSAI.keyToString(this.key, 0);
      return dir+" w/s:"+win+"/"+simulatedCount+" s:"+score;
    }
  }

  static class MCTSAI extends AI {
    String quotes[] = {
      "Walking on the moon",
      "Trying my best",
      "Live for yourself.",
      "Work hard. Dream big.",
      "Life is short.",
      "Bombs Everywhere"
    };
    int seed = ThreadLocalRandom.current().nextInt(20);
    static int gameRound = 0;
    static final int MAX_STEPS = 18;
    MCTS root = new MCTS(0);
    public int steps = MAX_STEPS;
    
    @Override
    void compute() {
      evaluateAlgorithmSwitch();
      
      gameRound++;
      root = new MCTS(0);
      
      // debugBoxAndOptionsDistance();

      GameState copyOfRoot = new GameState(game.currentState.width, game.currentState.height, 0);

      // when 200 simulation : steps = MAX_STEPS-gameRound;
      int simulationCount = getAffordableSimulationCount();
      for (int i=0;i<simulationCount;i++) {
        copyOfRoot.duplicateFrom(game.currentState);
        copyOfRoot.depth = 0; // FIXME redondant ?
        root.simulate(copyOfRoot, steps);
      }
      
      Integer key = MCTS.biasedMovementAlgorithm.getBestChild(root);
      MCTS chosen = root.childs.get(key);
//      if (chosen != null) {
//        System.err.println("BEST child is : "+Player.rotString[key/2]+","+(key%2==1 ? "B" : "M")+" -> "+EarlyGameAlgorithm.getScore(chosen));
//        System.err.println("FullPath is");
//        MCTS loop = chosen;
//        while (loop != null) {
//          System.err.print("v->"+Player.rotString[key/2]+","+(key%2==1 ? "B" : "M")+" -> "+EarlyGameAlgorithm.getScore(loop));
//          loop = loop.childs.get(MCTS.biasedMovementAlgorithm.getBestChild(loop));
//        }
//        System.err.println("----");
//      }
      //debugMCTS2(root, "");
      // game.currentState.debugBombs();
      
      if (chosen == null) {
        buildSayonaraAction();
        return;
      } else {
        buildBestActionFromKey(key);
      }
    }

    static void debugMCTS2(Player.MCTS root) {
      debugMCTS2(root, "");
    }
    static void debugMCTS2(Player.MCTS root, String decal) {
      System.err.println("Child count: "+root.childs.size());
      System.err.println("------");
      for (Entry<Integer, Player.MCTS> m : root.childs.entrySet()) {
        Integer key = m.getKey();
        System.err.println(decal+Player.rotString[key/2]+","+(key%2==1 ? "B" : "M")+" -> "+EarlyGameAlgorithm.getScore(m.getValue()));
      }
    }

    
    void evaluateAlgorithmSwitch() {
//      if (game.currentState.boxes.isEmpty() && MCTS.biasedMovementAlgorithm.type == MovementAlgorithm.AlgoType.BOX) {
//        MCTS.biasedMovementAlgorithm = new AggressiveMovementAlgorithm();
//      }
      return; // FIXME Aggressive algo is KO
    }

    void debugBoxAndOptionsDistance() {
      if(Game.nearestBox != null) {
        System.err.println("NEAREST Box is "+Game.nearestBox);
      } else {
        System.err.println("hmmm no nearest box ?");
      }
      if(Game.nearestOption != null) {
        System.err.println("NEAREST Option is "+Game.nearestOption);
      } else {
        System.err.println("hmmm no nearest option ?");
      }
    }

    void buildBestActionFromKey(Integer chosenKey) {
      Action action = new Action();

      action.message = "Drop bomb, move, don't die, repeat";
 
      APlayer player = game.currentState.players[Game.myIndex];
      action.dropBomb = (chosenKey % 2 == 1);
 
      int actionIndex = chosenKey / 2 ;
      int newPosX = player.p.x+rotx[actionIndex];
      int newPosY = player.p.y+roty[actionIndex];
      action.pos = P.get(newPosX, newPosY);
 
      actions.clear();
      actions.add(action);
    }

    int getAffordableSimulationCount() {
      switch(game.playersCount) {
        case 4:
          return 1_500;
        case 3:
          return 2_000;
        case 2 : 
        default:
          return 2_500;
      }
    }

    void buildSayonaraAction() {
      Action action = new Action();
      action.pos = game.currentState.players[Game.myIndex].p;
      action.dropBomb = false;
      action.message = "Sayonara";
      actions.clear();
      actions.add(action);
      return;
    }

    static String keyToString(Integer chosenKey, int playerIndex) {
      return (chosenKey % 2 == 1 ? "BOMB" : "MOVE")+ " " +rotString[chosenKey / 2];
    }

  }
  
  static class Game {
    public int playersCount=2;
    static final String MOVE_STAY_NOBOMB = "  ";
    static final int MAX_STEPS = 20;
    int width, height;
    GameState currentState;
    int depth = 0;
    static P nearestOption;
    static P nearestBox;
    
    static int myIndex = 0;
    
    Game(int width, int height) {
      this.width = width;
      this.height = height;
      
      currentState = new GameState(width, height, 0);
    }
    
    void play() {
      MCTS.game = this; // OUTCH, it's ugly
      
      AI ai = new MCTSAI();
      ai.game = this;

      while (true) {
        long long1 = System.currentTimeMillis();
        prepareGameState();
        long long2 = System.currentTimeMillis();
        updateNearestBoxes();
        updateNearestOption();
        //currentState.updateBoxInfluenza(currentState.players[Game.myIndex].bombRange);
        //currentState.computeRound();
        long long3 = System.currentTimeMillis();
        //updateNextStates();
        long long4 = System.currentTimeMillis();

        
/** debug informations */
//        System.err.println("Current grid:");
//        System.err.println("-------------");
//        debugThreats();
//        currentState.debugPlayerAccessibleCellsWithAStar();
//        currentState.debugBoxInfluenza();
//        currentState.debugBombs();
//        System.err.println("Next grid:");
//        System.err.println("----------");
//        states[1].debugBombs();
//        System.err.println("8th grid:");
//        System.err.println("----------");
//        states[7].debugBombs();
        long long5 = System.currentTimeMillis();

        
        /*ai */long aiBefore = System.currentTimeMillis();
        ai.compute();
        Action action = ai.actions.get(0);
        /*ai */long aiAfter= System.currentTimeMillis();
        
//        System.err.println(" ------- Stats ------------");
//        System.err.println("prepareGame : "+(long2-long1));
//        System.err.println("computeRound: "+(long3-long2));
//        System.err.println("updateStates: "+(long4-long3));
//        System.err.println("debug       : "+(long5-long4));
//        System.err.println("AI          : "+(aiAfter-aiBefore));
        
        
        System.out.println(action.get());
      }
    }
    
    
    void updateNearestOption() {
      int minDist = 10000;
      nearestOption = null;
      P playerPos = currentState.players[myIndex].p;
      for (Entity e : currentState.entities) {
        if (e.type == ENTITY_ITEM) {
          int manhattanDistance = e.p.manhattanDistance(playerPos);
          if (manhattanDistance < minDist) {
            minDist = manhattanDistance;
            nearestOption = e.p;
          }
        }
      }
    }

    void updateNearestBoxes() {
      int minDist = 10000;
      P nearestBoxes = null;
      P playerPos = currentState.players[myIndex].p;
      for (P box : currentState.boxes) {
        int manhattanDistance = box.manhattanDistance(playerPos);
        if (manhattanDistance < minDist) {
          minDist = box.manhattanDistance(playerPos);
          nearestBoxes = box;
        }
      }
      this.nearestBox = nearestBoxes;
    }

    void debugThreats() {
      for (int y = 0; y < height; y++) {
        String result="";
        for (int x = 0; x < width; x++) {
          char c= ' ';
          if (currentState.isThreat(P.get(x, y)) >= 0) {
            c='!';
          }
          result+=c;
        }
        System.err.println(result);
      }
    }



    void updateNextStates() {
      currentState.simulate(MOVE_STAY_NOBOMB);
    }

    void prepareGameState() {
      currentState.reset();

      for (int y = 0; y < height; y++) {
        String row = in.nextLine();
        currentState.addRow(y, row);
      }
      
      if (currentState.boxes.isEmpty()) {
        EarlyGameAlgorithm.gametype = EarlyGameAlgorithm.GameType.LATE;
      }
      
      int entitiesCount = in.nextInt();
      int playersCount = 1;
      for (int i = 0; i < entitiesCount; i++) {
        int entityType = in.nextInt();
        int owner = in.nextInt();
        int x = in.nextInt();
        int y = in.nextInt();
        int param1 = in.nextInt();
        int param2 = in.nextInt();
        Entity entity = null;
        if (entityType == ENTITY_BOMB) {
          entity = new Bomb(currentState, owner, x,y, param1, param2);
          currentState.players[owner].droppedBombs++;
        } else if (entityType == ENTITY_PLAYER) {
          APlayer player = new APlayer(currentState, owner, x,y, param1, param2);
          currentState.players[owner] = player;
          entity = player;
          playersCount = Math.max(playersCount, owner+1);
        } else if (entityType == ENTITY_ITEM) {
          entity = new Item(currentState, owner, x,y, param1, param2);
        } else {
          System.err.println("Hmmm entitytype not found");
        }
        if (entity != null) {
          currentState.addEntity(entity);
        }
      }
      in.nextLine();

      this.playersCount = playersCount;
    }
  }
  static class GameState {
    static final int CELL_FLOOR = 0;
    static final int CELL_WALL = Integer.MAX_VALUE;
    static final int CELL_EMPTY_BOX = 10;
    static final int CELL_BOMBUP_BOX = 11;
    static final int CELL_RANGEUP_BOX = 12;
    static final int CELL_BOMB_0 = 90;
    static final int CELL_BOMB_1 = 91;
    static final int CELL_BOMB_2 = 92;
    static final int CELL_BOMB_3 = 93;
    static final int CELL_BOMB_4 = 94;
    static final int CELL_BOMB_5 = 95;
    static final int CELL_BOMB_6 = 96;
    static final int CELL_BOMB_7 = 97;
    static final int CELL_BOMB_8 = 98;
    static final int CELL_BOMB_9 = 99;
    static final int CELL_FIRE = 200;
    static final int CELL_ITEM_BOMBUP = 31;
    static final int CELL_ITEM_RANGEUP = 32;

    public void explodeBox(P p) {
      int value = getCellAt(p.x, p.y);
      if (value == CELL_EMPTY_BOX) {
        grid[p.x+13*p.y] = CELL_FLOOR;
      }else if (value == CELL_BOMBUP_BOX) {
        grid[p.x+13*p.y] = CELL_ITEM_BOMBUP;
      }else if (value == CELL_RANGEUP_BOX) {
        grid[p.x+13*p.y] = CELL_ITEM_RANGEUP;
      }
    }
    public void removeItem(P testedP) {
      Iterator<Entity> iteE = entities.iterator();
      while (iteE.hasNext()) {
        Entity e =iteE.next(); 
        if (e.type == ENTITY_ITEM && e.p.equals(testedP)) {
          iteE.remove();
          return; // only one item per cell
        }
      }
    }
    
    public void simulate(String theMove) {
      if (this.depth < Game.MAX_STEPS) {
        GameState nextState = childs.get(Game.MOVE_STAY_NOBOMB);
        if (nextState != null) {
          nextState.clone(this);
          nextState.computeRound();
          nextState.simulate(theMove);
        }
      }
    }
    public void triggerBomb(P pointToCheck) {
      List<Entity> entitiesBackup = new ArrayList<>(entities);
      for (Entity e : entitiesBackup) {
        if (e.type == ENTITY_BOMB && e.p.equals(pointToCheck)) {
          Bomb b = (Bomb)e;
          if (b.ticksLeft > 0) { // 0 == already triggered
            b.ticksLeft = 1; // TODO ouch. besoin de ça pour qu'elle explose
            b.update(this);
          }
        }
      }
    }
    
    static boolean canWalkThrough(int value) {
      // correct way :
      // return !isFire(value) && !isABomb(value) && !isWall(value) && !isABox(value);
      // optimize for endGame (fire second)
      return value != CELL_WALL 
          && value != CELL_FIRE 
          && value != CELL_EMPTY_BOX && value != CELL_BOMBUP_BOX && value != CELL_RANGEUP_BOX
          && !(value == CELL_BOMB_0)
          && !(value == CELL_BOMB_1)
          && !(value == CELL_BOMB_2)
          && !(value == CELL_BOMB_3)
          && !(value == CELL_BOMB_4)
          && !(value == CELL_BOMB_5)
          && !(value == CELL_BOMB_6)
          && !(value == CELL_BOMB_7)
          && !(value == CELL_BOMB_8)
          && !(value == CELL_BOMB_9);
    }
    
    static boolean isFire(int value) {
      return value == CELL_FIRE;
    }
    static public boolean explosionBlocked(int value) {
      return isWall(value);
    }
    static boolean isWall(int value) {
      return value == CELL_WALL;
    }
    static public boolean explosionSoftBlocked(int value) {
      return isABox(value) || isAnItem(value) || isABomb(value);
    }

    static boolean isAnItem(int value) {
      return isBombUpItem(value) || isRangeUpItem(value);
    }

    public static boolean isBombUpItem(int value) {
      return value == CELL_ITEM_BOMBUP;
    }
    public static boolean isRangeUpItem(int value) {
      return value == CELL_ITEM_RANGEUP;
    }

    static boolean isABox(int value) {
      return value == CELL_EMPTY_BOX || value == CELL_BOMBUP_BOX || value == CELL_RANGEUP_BOX;
    }
    boolean isHardBlocked(P pos) {
      return isHardBlocked(grid[pos.x+13*pos.y]);
    }
    static boolean isHardBlocked(int value) {
      return value == CELL_WALL || isABomb(value);
    }
    static boolean isSoftBlock(int value) {
      return value == CELL_BOMBUP_BOX || value == CELL_EMPTY_BOX || value == CELL_RANGEUP_BOX;
    }
    static boolean isABomb(int value) {
      return value >= CELL_BOMB_0 && value <= CELL_BOMB_9;
    }
    
    int width, height;
    Map<String, GameState> childs = new HashMap<>();

    int[] grid;
    APlayer players[] = new APlayer[4];

    List<Entity> entities = new ArrayList<>();
    List<P> boxes = new ArrayList<>();
    List<P> hittedBoxes = new ArrayList<>();
    List<P> fireCells = new ArrayList<>();
    int depth;
    
    GameState(int width, int height, int depth) {
      this.width = width;
      this.height = height;
      this.depth = depth;
      grid = new int[13*11];
      
      if( depth < Game.MAX_STEPS) {
        childs.put("  ", new GameState(width,height, depth+1));
      }
    }

    // clean for next simulation on same spot
    void softReset() {
      hittedBoxes.clear();
      fireCells.clear();
    }
    // clean cumulative states
    void reset() {
      softReset();
      boxes.clear();
      entities.clear();
      players[0] = null;
      players[1] = null;
      players[2] = null;
      players[3] = null;
    }

    public void duplicateFrom(GameState fromState) {
      reset();
      for (Entity e : fromState.entities) {
        Entity duplicate = e.duplicate(this);
        if (duplicate.type == ENTITY_PLAYER) {
          this.players[duplicate.owner] = (APlayer)duplicate;
        }
        this.entities.add(duplicate);
      }

      System.arraycopy(fromState.grid, 0, grid, 0, 13*11);
    }
    
    public void clone(GameState fromState) {
      reset();
      for (Entity e : fromState.entities) {
        this.entities.add(e.duplicate(this));
      }

      for (int p=0;p<4;p++) {
        this.players[p] = fromState.players[p];
      } 
      
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          int value = fromState.grid[x+13*y];
          if (isABomb(value)) {
            value --;
            if (value == CELL_BOMB_0) {
            }
          } else if (isFire(value)) {
            value = CELL_FLOOR; // fire from bomb return to floor
          }
          grid[x+13*y] = value;
        }
      }
    }

    public void addEntity(Entity entity) {
      entities.add(entity);
      
      if (entity.type == ENTITY_PLAYER) {
        players[entity.owner] = (APlayer)entity;
      }

    }

    int getCellAt(int x, int y) {
      if (x < 0 || x >= width || y < 0 || y >= height) {
        return CELL_WALL;
      }
      return grid[x+13*y];
    }

    public void computeRound() {
      for (Entity entity : entities) {
        entity.update(this);
      }
      removeHittedBoxes();
    }

    public void computeRound_MCTS() {
      // remove old fire
      for (P p : fireCells) {
        grid[p.x+13*p.y] = GameState.CELL_FLOOR;
      }
      fireCells.clear();

      List<Entity> entitiesBackup = new ArrayList<>(entities);
      for (Entity entity : entitiesBackup) {
        entity.update(this);
      }
      removeHittedBoxes();
    }
    void resetPlayerPoints() {
      for (int i=0;i<4;i++) {
        if (players[i] != null) {
          players[i].points = 0;
        }
      }
    }

    void debugPlayerAccessibleCellsWithAStar(APlayer player) {
      // TODO don't use A* to check this !
      System.err.println("Accessible cells from "+player);

      for (int y = 0; y < height; y++) {
        String result="";
        for (int x = 0; x < width; x++) {
          Path path = new Path(this, player.p, P.get(x, y));
          path.find();
          if (path.path.size() > 0) {
            result+="A";
          } else {
            result+=" ";
          }
        }
        System.err.println(result);
      }
    }

    void removeHittedBoxes() {
      for (P p : hittedBoxes ) {
        int boxValue = grid[p.x+13*p.y];
        if (boxValue == CELL_EMPTY_BOX) {
          grid[p.x+13*p.y] = CELL_FLOOR;
        } else if (boxValue == CELL_BOMBUP_BOX ) {
          grid[p.x+13*p.y] = CELL_ITEM_BOMBUP;
        } else if (boxValue == CELL_RANGEUP_BOX) {
          grid[p.x+13*p.y] = CELL_ITEM_RANGEUP;
        }
      }
    }

    public void addRow(int y, String row) {
      for (int x = 0; x < row.length(); x++) {
        char c = row.charAt(x);
        if (c == '.') {
          grid[x+13*y] = CELL_FLOOR;
        } else if (c == 'X') {
          grid[x+13*y] = CELL_WALL;
        } else if (c == '0') {
          grid[x+13*y] = CELL_EMPTY_BOX;
          addABox(y, x);
        } else if (c == '1') {
          grid[x+13*y] = CELL_RANGEUP_BOX;
          addABox(y, x);
        } else if (c == '2') {
          grid[x+13*y] = CELL_BOMBUP_BOX;
          addABox(y, x);
        }
      }
    }
    void addABox(int y, int x) {
      boxes.add(P.get(x, y));
    }

    void debugBombs() {
      for (int y = 0; y < height; y++) {
        String result="";
        for (int x = 0; x < width; x++) {
          int value = grid[x+13*y];
          char c= '?';
          if (value == CELL_FLOOR) { c = ' '; }
          else if (value == CELL_WALL) { c = 'X'; }
          else if (value == CELL_EMPTY_BOX
            || value == CELL_BOMBUP_BOX
            || value == CELL_RANGEUP_BOX) {
              c = 'b';
          } else if (value == CELL_ITEM_BOMBUP || value == CELL_ITEM_RANGEUP) {
            c= '+';
          } else if (value >= CELL_BOMB_0 && value <= CELL_BOMB_9) {
              c = (char)('0' + (value - CELL_BOMB_0));
          }
          result+=c;
        }
        System.err.println(result);
      }
    }
    int isThreat(P p) {
      GameState theState = this;
      for (int layer = 0;layer<Game.MAX_STEPS;layer++) {
        if (theState == null) {
          return -1;
        }
        int value = theState.grid[p.x+13*p.y];
        if (GameState.isFire(value)) {
          return layer;
        }
        theState = theState.childs.get(Game.MOVE_STAY_NOBOMB);
      }
      return -1;
    }
  }
  
  public static void main(String[] args) {
    in = new Scanner(System.in);
    int width = in.nextInt();
    int height = in.nextInt();
    int myIndex = in.nextInt();
    in.nextLine();

    Game game = new Game(width, height);
    Game.myIndex = myIndex;

    game.play();
  }
  
  /**
   * PATH : A*
   *
   */
  public static class Path {
    Map<P, PathItem> closedList = new HashMap<>();
    List<PathItem> openList = new ArrayList<>();
    
    List<PathItem> path = new ArrayList<>();
    
    GameState rootState;
    P from;
    P target;
    
    Path(GameState root, P from, P target) {
      this.rootState = root;
      this.from = from;
      this.target = target;
    }

    public void debug() {
      System.err.println("found a path: "+target);
      System.err.println("path ("+path.size()+ ") :  ");
      for (Path.PathItem i : path) {
        System.err.print(i.pos+" --> ");
      }
      System.err.println("");
    }

    PathItem find() {
      PathItem item = calculus();
      path.clear();
      if (item != null) {
        calculatePath(item);
      }
      return item;
    }

    void calculatePath(PathItem item) {
      PathItem i = item;
      while (i != null) {
        path.add(0, i);
        i = i.precedent;
      }
    }
    PathItem calculus() {
      PathItem root = new PathItem();
      root.pos = this.from;
      root.state = this.rootState;
      openList.add(root);

      while (openList.size() > 0) {
        PathItem visiting = openList.remove(0); // imagine it's the best
        GameState theState = visiting.state;
        P pos = visiting.pos;
        if (pos.equals(target)) {
          return visiting;
        }

        closedList.put(pos, visiting);
        if (pos.y > 0) {
          addToOpenList(visiting, pos , P.get(pos.x, pos.y-1));
        }
        if (pos.y < theState.height - 1) {
          addToOpenList(visiting, pos , P.get(pos.x, pos.y+1));
        }
        if (pos.x > 0) {
          addToOpenList(visiting, pos , P.get(pos.x-1, pos.y));
        }
        if (pos.x < theState.width - 1) {
          addToOpenList(visiting, pos , P.get(pos.x+1, pos.y));
        }
        // sort with distances
        Collections.sort(openList, new Comparator<PathItem>() {
          @Override
          public int compare(PathItem o1, PathItem o2) {
            return Integer.compare(o1.totalPrevisionalLength, o2.totalPrevisionalLength);
          }
        });
      }
      return null; // not found !
    }

    void addToOpenList(PathItem visiting, P fromCell, P toCell) {
      if (closedList.containsKey(toCell)) {
        return;
      }
      int value = visiting.state.getCellAt(toCell.x, toCell.y);
      if (GameState.canWalkThrough(value)) {
        PathItem pi = new PathItem();
        pi.pos = toCell;
        pi.cumulativeLength = visiting.cumulativeLength + 1;
        pi.totalPrevisionalLength = pi.cumulativeLength + fromCell.manhattanDistance(target);
        pi.precedent = visiting;
        pi.state = visiting.state.childs.get(Game.MOVE_STAY_NOBOMB);
        if (pi.state == null) {
          pi.state = visiting.state;
        }
        openList.add(pi);
      }
    }


    public static class PathItem {
      public GameState state;
      int cumulativeLength = 0;
      int totalPrevisionalLength = 0;
      PathItem precedent = null;
      P pos;

      public int length() {
        PathItem i = this;
        int count = 0;
        while (i != null) {
          count++;
          i = i.precedent;
        }
        return count;
      }
    }
  }
  /** End of PATH */
  
  static class P {
    static P[][] ps = new P[20][20]; // maximum board
    static {
      for (int x=0;x<20;x++) {
        for (int y=0;y<20;y++) {
          ps[x][y] = new P(x,y);
        }
      }
    }
    static P get(int x,int y) {
      return ps[x][y];
    }
    
    
    final int x;
    final int y;

    public P(int x, int y) {
      super();
      this.x = x;
      this.y = y;
    }

    int distance(P p) {
      return (p.x - x) * (p.x - x) + (p.y - y) * (p.y - y);
    }

    int manhattanDistance(P p) {
      return Math.abs(x - p.x) + Math.abs(y - p.y);
    }

    @Override
    public String toString() {
      return "(" + x + "," + y + ")";
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + x;
      result = prime * result + y;
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      P other = (P) obj;
      return x == other.x && y == other.y;
    }
  }

}
