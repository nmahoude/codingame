package hypersonic;

import hypersonic.entities.Bomb;
import hypersonic.entities.Bomberman;
import hypersonic.entities.Item;
import hypersonic.utils.P;

public class State {
  Board board = new Board();
  
  public int turn;
  public Bomberman players[] = new Bomberman[4];
  public int playersFE = 4;
  public Bomberman me;
  int destructedBox;

  public State() {
    for (int i=0;i<4;i++) {
      players[i] = new Bomberman(i, null, 0, 0);
    }
    clean();
  }
  
  private void clean() {
    board.clean();
    
    playersFE = 4;
    for (int i=0;i<4;i++) {
      players[i].isDead = true;
    }
    me = null;
    destructedBox = 0;
    // cells will be copied later
  }


  public void copyFrom(State model) {
    this.clean();

    this.turn = model.turn;
    this.board.copyFrom(model.board);
    
    this.playersFE = 4;
    for (int p=0;p<model.playersFE;p++) {
      Bomberman b = model.players[p];
      if (b.isDead) {
        players[p].isDead = true;
      } else {
        players[p].copyFrom(b);
        if (b == model.me) {
          this.me = players[p];
        }
      }
    }
  }
  
  public void init() {
    clean();
    board.init();
    playersFE = 4;

    destructedBox = 0;
  }

  public void init(final int y, final String row) {
    board.init(y, row);
  }

  public void updateBombs() {
    turn++;
    
    board.updateBombs(this);
  }

  public void addBomb(final Bomb bomb) {
    board.addBomb(bomb);
  }
  
  
  void updatePoints(Bomberman orginalBomberman) {
    destructedBox++;
    if (orginalBomberman != null) {
      orginalBomberman.points++;
    }
  }

  Bomberman getBombermanWithId(final int owner) {
    for (int p=0;p<playersFE;p++) {
      Bomberman b = players[p];
      if (b.owner == owner) {
        return b;
      }
    }
    return null;
  }

  boolean isOnBoard(int x, int y) {
    return board.isOnBoard(x,y);
  }

  public Bomberman getBomberman(int index) {
    return players[index];
  }

  public void addItem(Item item) {
    board.addItem(item);
  }

  public boolean canMoveTo(int x, int y) {
    return board.canMoveTo(x,y);
  }

  public void walkOn(final Bomberman player, final P p) {
    final int value = board.cells[p.x+Board.WIDTH*p.y];
    if ( value == Board.ITEM_1) {
      player.currentRange+=1;
    } else if (value == Board.ITEM_2) {
      player.bombsLeft+=1;
      player.bombCount+=1;
    }
    player.position = p;
    board.cells[p.x+Board.WIDTH*p.y] = Board.EMPTY;
  }

  public void killPlayersAt(int x, int y) {
    for (int p=0;p<playersFE;p++) {
      Bomberman bomberman = players[p];
      if (bomberman.isDead) continue;
      
      if (bomberman.position.x == x && bomberman.position.y == y) {
        bomberman.isDead = true;
      }
    }
  }

  public boolean canWalkOn(P p) {
    return board.canWalkOn(p);
  }
}
