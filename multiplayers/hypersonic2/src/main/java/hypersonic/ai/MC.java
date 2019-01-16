package hypersonic.ai;

import java.util.Arrays;

import hypersonic.Board;
import hypersonic.Move;
import hypersonic.Player;
import hypersonic.entities.Bomb;
import hypersonic.simulation.MoveGenerator;
import hypersonic.simulation.Simulation;

public class MC {
  private static final int DEPTH = 20;
  
  static double patience[];
  static {
    patience = new double[DEPTH];
    for (int i=0;i<DEPTH;i++) {
      patience[i] = 1.0 * (DEPTH-i) / DEPTH;
    }
  }
  Board board = new Board();
  public Move bestMove = null;
  public String message = "";
  Simulation simulator = new Simulation(board);
  MoveGenerator gen = new MoveGenerator(board);
  
  Move[] moves = new Move[16];
  int movesFE;
  
  public void think(Board model) {
    this.board.copyFrom(model);

    Move allMoves[] = new Move[DEPTH];
    double bestScore = Double.NEGATIVE_INFINITY;
    
    int simu = 0;
    while (true) {
      simu++;
      if ((simu & 255) == 0 ) {
        if (System.currentTimeMillis() - Player.startTime > 95) {
          break;
        }
      }
      
      this.board.copyFrom(model);
      
      double score = 0;
      for (int t=0;t<DEPTH;t++) {
        if (t <= DEPTH - Bomb.DEFAULT_TIMER - 1 ) {
          movesFE = gen.getPossibleMoves(moves);
        } else {
          movesFE = gen.getPossibleMovesWithoutBombs(moves);
        }

        Move move = moves[Player.rand.nextInt(movesFE)];
        
        allMoves[t] = move;
        simulator.simulate(move);
        if (this.board.me.isDead) {
          score = -1_000_000 + t; // die the latest
          break;
        } else {
          score += patience[t] * score();
        }
      }
    
      if (score > bestScore) {
        bestScore = score;
        bestMove = allMoves[0];

        if(Player.DEBUG_AI) {
          System.err.println("best move : "+Arrays.asList(allMoves));
          System.err.println("Status pos = "+this.board.me.position);
          System.err.println("Status dead = "+this.board.me.isDead);
          System.err.println("Bombs : ");
          for (int b=0;b<board.bombsFE;b++) {
            Bomb bomb = board.bombs[b];
            System.err.println(bomb);
          }
        }
      }
    }
    if (Player.DEBUG_AI) {
      System.err.println("Simulations : " + simu);
    }
    message = ""+simu + " / "+(System.currentTimeMillis()-Player.startTime);
  }

  private double score() {
    double score = 0.0;
    
    score += 100.0 * board.me.points;
    score += 1.1 * board.me.bombCount;
    score += board.me.currentRange;
    score += board.me.bombsLeft;
    return score;
  }
}
