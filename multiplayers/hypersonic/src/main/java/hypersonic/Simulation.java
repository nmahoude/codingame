package hypersonic;

import java.util.ArrayList;
import java.util.List;

import hypersonic.entities.Bomb;
import hypersonic.entities.Bomberman;
import hypersonic.utils.P;

public class Simulation {
  public Board board;
  
  public final int getScoreHeuristic() {
    if (board.me.isDead) {
      return -999;
    }
    return board.me.points+board.me.bombCount+board.me.currentRange;
  }
  public final boolean isFinished() {
    return false;
  }
  
  public List<Move> getPossibleMoves() {
    List<Move> moves = new ArrayList<>();
    for (Move move : Move.values()) {
      if (isMovePossible(move)) {
        moves.add(move);
      }
    }
    return moves;
  }
  
  
  public final boolean isMovePossible(Move move) {
    Bomberman me = board.me;
    switch(move) {
      case DOWN:
        return board.canMoveTo(me.position.x, me.position.y+1);
      case DOWN_BOMB:
        return me.bombsLeft > 0 && 
            board.canMoveTo(me.position.x, me.position.y+1);
      case LEFT:
        return board.canMoveTo(me.position.x-1, me.position.y);
      case LEFT_BOMB:
        return me.bombsLeft > 0 && 
            board.canMoveTo(me.position.x-1, me.position.y);
      case RIGHT:
        return board.canMoveTo(me.position.x+1, me.position.y);
      case RIGHT_BOMB:
        return me.bombsLeft > 0 && 
            board.canMoveTo(me.position.x+1, me.position.y);
      case STAY:
        return true;
      case STAY_BOMB:
        return me.bombsLeft > 0 && true;
      case UP:
        return board.canMoveTo(me.position.x, me.position.y-1);
      case UP_BOMB:
        return me.bombsLeft > 0 && 
            board.canMoveTo(me.position.x, me.position.y-1);
      default:
        return false;
    }
  }
  public final void copyFrom(Simulation simulation) {
    board = simulation.board.duplicate();
  }
  public final void simulate(Move move) {
    board.destructedBox = 0;
    for (Bomb bomb : board.bombs) {
      bomb.update();
    }
    simulateMove(move);
  }
  private void simulateMove(Move move) {
    int newX = board.me.position.x;
    int newY = board.me.position.y;
    boolean dropBomb = false;
    switch(move) {
      case DOWN_BOMB:
        dropBomb = true;
      case DOWN:
        newY+=1;
        break;
      case LEFT_BOMB:
        dropBomb = true;
      case LEFT:
        newX-=1;
        break;
      case RIGHT_BOMB:
        dropBomb = true;
      case RIGHT:
        newX+=1;
        break;
      case STAY_BOMB:
        dropBomb = true;
      case STAY:
        break;
      case UP_BOMB:
        dropBomb = true;
      case UP:
        newY-=1;
    }
    
    if (dropBomb) {
      board.addBomb(new Bomb(board, board.me.owner, board.me.position, 8, board.me.currentRange));
      board.me.bombsLeft-=1;
    }
    if (newX != board.me.position.x || newY != board.me.position.y) {
      board.walkOn(board.me, P.get(newX, newY));
    }
  }
}
