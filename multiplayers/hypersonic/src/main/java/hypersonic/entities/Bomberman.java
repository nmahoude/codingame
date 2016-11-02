package hypersonic.entities;

import hypersonic.Board;
import hypersonic.utils.P;

public class Bomberman extends Entity {

  public int bombsLeft;
  public int currentRange;

  public boolean isDead = false;
  public int  points = 0;
  
  public Bomberman(Board board, int owner, P position, int bombsLeft, int currentRange) {
    super(board, owner, EntityType.PLAYER, position);
    this.bombsLeft = bombsLeft;
    this.currentRange = currentRange;
  }

  public void move(P p) {
    if (board.canWalkOn(p)) {
      board.walkOn(this, p);
    }
  }
}