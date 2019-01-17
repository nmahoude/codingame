package hypersonic;

import java.util.Random;
import java.util.Scanner;

import hypersonic.ai.MC;
import hypersonic.entities.Bomb;
import hypersonic.entities.Bomberman;
import hypersonic.entities.Item;
import hypersonic.utils.P;

public class Player {
  public static boolean DEBUG_INPUT = true;
  public static boolean DEBUG_AI = false;
  public static Random rand = new Random(0);
  
  public static long startTime;
  public static int myId;

  
  public Board board = new Board();
  private int turn = 0;
  private Scanner in;

  public Player(Scanner in) {
    this.in = in;
  }

  void play() {
    readInitialData();
    
    while (true) {
      turn++;
      readGameState();
      
      board.updateBombs(); // pre-calculated next step first move !
      
      // now look what I can do !
      MC mc = new MC();
      mc.think(board);
      
      
      final Move move = mc.bestMove;
      outputMove(board.me, move, mc.message);
    }
  }

  private void readInitialData() {
    int width = in.nextInt();
    int height = in.nextInt();
    myId = in.nextInt();
    if (Player.DEBUG_INPUT) {
      System.err.println(""+width+" "+height+" "+myId);
    }
  }
  
  private void outputMove(final Bomberman me, final Move move, String message) {
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
      System.out.println("BOMB "+newX+" "+newY+ " "+message);
    } else {
      System.out.println("MOVE "+newX+" "+newY+ " "+message);
    }
  }
  public void readGameState() {
    initBoard();
    initEntities();
  }
  private void initEntities() {
    final int bombsOnBoard[] = new int[4];
    
    final int entitiesCount = in.nextInt();
    if (Player.DEBUG_INPUT) {
      System.err.println(entitiesCount);
    }
    startTime = System.currentTimeMillis();
    for (int i = 0; i < entitiesCount; i++) {
      final int entityType = in.nextInt();
      final int owner = in.nextInt();
      final int x = in.nextInt();
      final int y = in.nextInt();
      final int param1 = in.nextInt();
      final int param2 = in.nextInt();
      if (Player.DEBUG_INPUT) {
        System.err.println(""+entityType + " "+owner+" "+x+" "+y+" "+param1+" "+param2);
      }
      
      if (entityType == 0) {
        final Bomberman player = new Bomberman(board, owner, P.get(x, y), param1, param2);
        board.addPlayer(player);
        if (player.owner == myId) {
          board.me = player;
        }
      } else if (entityType == 1) {
        int turnAtExplosion = turn + param1;
        final Bomb bomb = BombCache.pop(owner, P.get(x, y), turnAtExplosion, param2);
        board.addBomb(bomb);
        bombsOnBoard[owner]+=1;
      } else if (entityType == 2) {
        final Item item = Item.create(board, owner, P.get(x, y), param1, param2);
        board.addItem(item);
      }
    }
    // update bombsCount
    for (int p=0;p<board.playersFE;p++) {
      Bomberman b = board.players[p];
      b.bombCount = b.bombsLeft + bombsOnBoard[b.owner];
    }
    //System.err.println("ME == pos: "+board.me.position+" bLeft: "+board.me.bombsLeft+ "/"+board.me.bombCount+" - range:"+board.me.currentRange);
  }

  private void initBoard() {
    board.turn = this.turn;
    board.init();
    for (int y = 0; y < Board.HEIGHT; y++) {
      final String row = in.next();
      if (Player.DEBUG_INPUT) {
        System.err.println(row);
      }
      board.init(y, row);
    }
  }
  public static void main(final String args[]) {
    Scanner in = new Scanner(System.in);
    
    final Player p = new Player(in);
    p.play();
  }
}