package hypersonic;

import java.util.ArrayList;
import java.util.List;

import hypersonic.entities.Bomb;
import hypersonic.entities.Bomberman;
import hypersonic.entities.Item;
import hypersonic.utils.P;

public class Board {
  public static final int HEIGHT = 11;
  public static final int WIDTH = 13;
  private static final int MAX_BOMBS = 20; // NOTE : risk taken, only 16 bombs on the board max
  
  public static final int EMPTY = '.';
  public static final int WALL = 'X';
  
  public static final int BOX = '0';
  public static final int BOX_1 = '1';
  public static final int BOX_2 = '2';
  
  public static final int ITEM_1 = 'l';
  public static final int ITEM_2 = 'k';
  public static final int BOMB = 'b';
  static final int rot[][] = {
      { 1, 0 },
      { 0, 1 },
      { -1, 0 },
      { 0, -1 }
  };

  static int explodesBoxMap[] = new int[WIDTH*HEIGHT];
  Bomb localBombs[] = new Bomb[24];
  int localBombsFE = 0;
  
  public int cells[];
  Bomb bombs[] = new Bomb[MAX_BOMBS];
  int bombsFE = 0;
  public int boxCount;
  
  
  Board() {
    cells = new int[WIDTH*HEIGHT];
  }

  public void clean() {
    for (int i=0;i<localBombsFE;i++) {
      Cache.pushBomb(localBombs[i]);
    }
    localBombsFE = 0;
    bombsFE = 0;
    boxCount = 0;
  }
  public void init() {
    bombsFE = 0;
    boxCount = 0;
  }
  public void init(int y, String row) {
    for (int x = 0; x < WIDTH; x++) {
      final char value = row.charAt(x);
      cells[x+WIDTH*y] = value;
      if (value == BOX || value == BOX_1 || value == BOX_2) {
        boxCount++;
      }
    }
  }

  public void copyFrom(Board model) {
    this.boxCount = model.boxCount;
    
    // copy des bombes non explosées (timer atteint ou chain-exploded ie ==null) 
    bombsFE = 0;
    for (int i=0;i<model.bombsFE;i++) {
      Bomb b = model.bombs[i];
      if (b != null) {
        this.bombs[this.bombsFE++] = b;
      }
    }
    System.arraycopy(model.cells, 0, this.cells, 0, WIDTH*HEIGHT);
  }


  public void updateBombs(State state) {
    // simulate one turn
    for (int i=0;i<bombsFE;i++) {
      final Bomb b = bombs[i];
      if (b == null) continue;
      
      if (b.timer == state.turn) {
        bombs[i] = null; // not in this board anymore !
        explode(state, b);
      }
    }
    
    // rearrange bombs
    int current = 0;
    for (int i=0;i<bombsFE;i++) {
      final Bomb b = bombs[i];
      if (b != null) bombs[current++] = b;
    }
    bombsFE = current;
  }

  static Bomb bombsToExplode[] = new Bomb[MAX_BOMBS];
  static int bombsToExplodeFE = 0;
  static int destroyedBoxes[] = new int[WIDTH*HEIGHT];
  static int destroyedBoxesFE;
  void explode(State state, Bomb bomb) {
    bombsToExplodeFE = 0;
    destroyedBoxesFE = 0;
    
    bombsToExplode[bombsToExplodeFE++] = bomb;
    int currentBombToExplode = 0;
    while (currentBombToExplode != bombsToExplodeFE) {
      final Bomb b = bombsToExplode[currentBombToExplode++];
      
      final Bomberman orginalBomberman = state.players[b.owner];
      orginalBomberman.bombsLeft+=1; // he may be dead, but yolo
      
      P p = b.position;
      
      checkExplosion(state, b.owner, p.x, p.y); // at (0,0)
      int x = p.x;
      int y = p.y;
      int correctedRange;
      correctedRange = Math.min(b.range-1, WIDTH-1-p.x);
      for (int d = 0; d < correctedRange; d++) {
        x++;
        if (checkExplosion(state, b.owner, x, y)) {
          break;
        }
      }
      
      x = p.x;
      correctedRange = Math.min(b.range-1, p.x);
      for (int d = 0; d < correctedRange; d++) {
        x--;
        if (checkExplosion(state, b.owner, x, y)) {
          break;
        }
      }
      
      x = p.x;
      correctedRange = Math.min(b.range-1, HEIGHT-1-p.y);
      for (int d = 0; d < correctedRange; d++) {
        y++;
        if (checkExplosion(state, b.owner, x, y)) {
          break;
        }
      }
      
      y = p.y;
      correctedRange = Math.min(b.range-1, p.y);
      for (int d = 0; d < correctedRange; d++) {
        y--;
        if (checkExplosion(state, b.owner, x, y)) {
          break;
        }
      }
    }

    // clear bombs places
    for (int b=0;b<bombsToExplodeFE;b++) {
      Bomb removedBombs = bombsToExplode[b];
      int x = removedBombs.position.x;
      int y = removedBombs.position.y;
      cells[x+WIDTH*y] = EMPTY;
    }
    
    // clear boxes
    for (int b=0;b<destroyedBoxesFE;b++) {
      int mapIndex = destroyedBoxes[b];
      int playerMask = explodesBoxMap[mapIndex];
      
      if (playerMask == 0) continue; // already cleared
      
      explodesBoxMap[mapIndex] = 0;
      
      if ((playerMask & 0b1) != 0) state.updatePoints(state.players[0]);
      if ((playerMask & 0b10) != 0) state.updatePoints(state.players[1]);
      if ((playerMask & 0b100) != 0) state.updatePoints(state.players[2]);
      if ((playerMask & 0b1000) != 0) state.updatePoints(state.players[3]);
      
      int value = cells[mapIndex];
      switch (value) {
      case BOX:
        cells[mapIndex] = EMPTY;
        boxCount--;
        break;
      case BOX_1:
        cells[mapIndex] = ITEM_1;
        boxCount--;
        break;
      case BOX_2:
        cells[mapIndex] = ITEM_2;
        boxCount--;
        break;
      }
    }
  }

  private boolean checkExplosion(State state, int bombOwner, int x, int y) {
    if ((x & 0b1) != 0 && (y & 0b1) != 0) return true; // fast wall
    
    int mapIndex = x+WIDTH*y;
    int cellValue = cells[mapIndex];
    
    state.killPlayersAt(x,y);
    
    switch (cellValue) {
      case BOX:
      case BOX_1:
      case BOX_2:
        explodesBoxMap[mapIndex] |= (1 << bombOwner);
        destroyedBoxes[destroyedBoxesFE++] = mapIndex;
        return true;
      case ITEM_1:
      case ITEM_2:
        cells[x+WIDTH*y] = EMPTY;
        return true; // stop explosion
      case BOMB:
        int bombIndex = getBombIndexAt(x, y);
        if (bombIndex != -1) {
          Bomb newBombToExplode = bombs[bombIndex];
          // this bomb will explode and will not be here anymore (can't change state of bomb anymore
          bombs[bombIndex] = null; 
          bombsToExplode[bombsToExplodeFE++] = newBombToExplode;
        }
        return true; // stop explosion
    }
    return false;
  }

  private int getBombIndexAt(final int x, final int y) {
    for (int i=0;i<bombsFE;i++) {
      Bomb b = bombs[i];
      
      if (b != null && b.position.x == x && b.position.y == y) {
        return i;
      }
    }
    return -1;
  }

  
  public void addBomb(Bomb bomb) {
    localBombs[localBombsFE++] = bomb;
    bombs[bombsFE++] = bomb;
    cells[bomb.position.x+WIDTH*bomb.position.y] = BOMB;
  }


  public boolean isOnBoard(int x, int y) {
    if (x < 0 || x > WIDTH-1) {
      return false;
    }
    if (y < 0 || y > HEIGHT-1) {
      return false;
    }
    return true;
  }


  public void addItem(Item item) {
    if (item.type == 1) {
      cells[item.position.x+WIDTH*item.position.y] = ITEM_1;
    } else {
      cells[item.position.x+WIDTH*item.position.y] = ITEM_2;
    }
  }

  public boolean canWalkOn(final P p) {
    final int value = cells[p.x+WIDTH*p.y];
    return value != Board.WALL 
        && value != Board.BOX
        && value != Board.BOX_1
        && value != Board.BOX_2
        && value != Board.BOMB
        ;
  }

  public boolean canMoveTo(int x, int y) {
    if (!isOnBoard(x, y)) {
      return false;
    }
    
    final int value = cells[x+WIDTH*y];
    return value == EMPTY || value == ITEM_1 || value == ITEM_2;
  }
  
}
