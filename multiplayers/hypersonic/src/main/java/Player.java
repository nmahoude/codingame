import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

class Player {
  private static Scanner in;
  private static int myIndex;

  private static final int ENTITY_PLAYER = 0;
  private static final int ENTITY_BOMB = 1;
  private static final int ENTITY_ITEM = 2;

  static int[] rotx = { 1, 0, -1, 0 };
  static int[] roty = { 0, 1, 0, -1 };

  enum EntityType {
    PLAYER, BOMB
  }

  static class P {
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

    @Override
    public String toString() {
      return "(" + x + "," + y + ")";
    }
  }

  static class Entity {
  }

  static class Bomb extends Entity {
    APlayer player; // who drops the bomb
    int timer;
    int range;

    public Bomb(APlayer player, int timer, int range) {
      super();
      this.player = player;
      this.timer = timer;
      this.range = range;
    }
  }

  static class APlayer extends Entity {
    int index;
    int x, y; // his position
    int bombsLeft = 1;
    public int bombRange = 3;
    public Cell cell;
  }

  static class Cell {
    static final int UP = 0;
    static final int DOWN = 1;
    static final int RIGHT = 2;
    static final int LEFT = 3;

    static final int OPTION_EXTRARANGE = 1;
    static final int OPTION_EXTRABOMB = 2;
    public static final int MIN_WEIGHT = -2000000;

    enum Type {
      BOX, FLOOR, WALL
    }

    int x;
    int y;
    Type type;
    List<Entity> entities; // present on cell (type floor)
    public int weight = 0;
    public int option;
    private int threat;
    public int willExplode;
    public int boxCount;

    public Cell(int x, int y) {
      this.x = x;
      this.y = y;
      reset();
    }

    public int hvDistanceTo(APlayer me) {
      if (me.x == x) {
        return Math.abs(me.y - y);
      } else if (me.y == y) {
        return Math.abs(me.x - x);
      } else {
        return Integer.MAX_VALUE;
      }
    }

    public boolean isBlocked() {
      return type == Type.WALL || type == Type.BOX;
    }

    public void reset() {
      weight = 0;
      threat = 0;
      boxCount = 0;
      willExplode = Integer.MAX_VALUE;
    }
  }

  static class Game {
    private static final Cell NO_CELL = new Cell(-1, -1);
    static {
      NO_CELL.weight = Cell.MIN_WEIGHT;
    }
    private int width;
    private int height;

    List<Cell> boxes = new ArrayList<>();

    Map<Integer, APlayer> players = new HashMap<>();
    APlayer me = null;

    Cell[][] grid = new Cell[13][11];

    public Game(int width, int height) {
      this.width = width;
      this.height = height;
      initBoard();
    }

    private void initBoard() {
      for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {
          Cell c = new Cell(x, y);
          grid[x][y] = c;
        }
      }
    }

    public void addRow(int rowIndex, String row) {
      System.err.println("row: "+row);
      for (int i = 0; i < row.length(); i++) {
        char c = row.charAt(i);
        Cell cell = grid[i][rowIndex];
        if (c == '.') {
          cell.type = Cell.Type.FLOOR;
        } else if (c == 'X') {
          cell.type = Cell.Type.WALL;
          updateWallInfluence(cell);
        } else if (c >= '0') {
          cell.type = Cell.Type.BOX;
          cell.option = c - '0';
          boxes.add(cell);
        }
      }
    }

    private void updateWallInfluence(Cell cell) {
      cell.weight = Cell.MIN_WEIGHT;
    }

    private void updateBoxInfluence(Cell originCell) {
      int x = originCell.x;
      int y = originCell.y;
      for (int rot = 0; rot < 4; rot++) {
        for (int d = 1; d < me.bombRange; d++) {
          Cell c = getCellAt(x + d * rotx[rot], y + d * roty[rot]);
          if (c.type == Cell.Type.WALL || c.type == Cell.Type.BOX) {
            break;
          }
          c.boxCount++;
          
          if (c.willExplode > 0 && c.willExplode < Integer.MAX_VALUE - 2) {
            continue;
          }
        }
      }
    }

    void debug() {
      for (int y=0;y<11;y++) {
        String result = "";
        for (int x=0;x<13;x++) {
          if (grid[x][y].type == Cell.Type.WALL) {
            result+="X";
          } else {
            result+=""+grid[x][y].boxCount;
          }
        }
        System.err.println(result);
      }
    }
    private void updateOptionInfluence(Cell cell) {
      cell.weight += 2;
    }

    private Cell getCellAt(int x, int y) {
      if (x < 0 || x >= 13 || y < 0 || y >= 11) {
        return NO_CELL;
      }
      return grid[x][y];
    }

    public void resetGame() {
      for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {
          grid[x][y].reset();
        }
      }
      boxes.clear();
    }

    static class EntityInfo {
      int entityType, owner, x, y;
      private int param1;
      private int param2;

      public EntityInfo(int entityType, int owner, int x, int y, int param1, int param2) {
        super();
        this.entityType = entityType;
        this.owner = owner;
        this.x = x;
        this.y = y;
        this.param1 = param1;
        this.param2 = param2;
      }
    }

    void updateEntities(List<EntityInfo> entities) {
      for (EntityInfo info : entities) {
        Cell cell = grid[info.x][info.y];

        if (info.entityType == ENTITY_PLAYER) {
          APlayer player = players.get(info.owner);
          if (player == null) {
            player = new APlayer();
            players.put(info.owner, player);
          }
          player.x = info.x;
          player.y = info.y;
          player.cell = cell;
          if (info.owner == myIndex) {
            me = player;
          }
        } else {
          if (info.entityType == ENTITY_ITEM) {
            updateOptionInfluence(cell);
          } else if (info.entityType == ENTITY_BOMB) {
            updateBombInfluence(cell, info.owner, info.param1 /* tickLeft */, info.param2/* range */);
          }
        }
      }
    }

    private void updateBombInfluence(Cell cell, int index, int tickLeft, int range) {
      // TODO
      // if (cell.willExplode < tickLeft ) {
      // tickLeft = cell.willExplode;
      // }

      for (int rot = 0; rot < 4; rot++) {
        for (int d = 1; d < me.bombRange; d++) {
          Cell c = getCellAt(cell.x + d * rotx[rot], cell.y + d * roty[rot]);
          // TODO
          // if (c.hasBombs() && c.bomb().tickLeft > tickLeft) {
          // updateBombInfluence(c, c.bomb().index, tickLeft, c.bomb().range);
          // }
          if (c.isBlocked()) {
            break;
          }
          c.willExplode = Math.min(c.willExplode, tickLeft);
          if (index != me.index) {
            c.threat = Math.max(8 - tickLeft, c.threat);
          }
        }
      }
    }

    private void play() {
      Path.PathItem currentPath = null;
      Cell nextCell = null;

      while (true) {
        resetGame();
        for (int y = 0; y < height; y++) {
          String row = in.nextLine();
          addRow(y, row);
        }
        int entitiesCount = in.nextInt();
        List<EntityInfo> entities = new ArrayList<>();
        for (int i = 0; i < entitiesCount; i++) {
          int entityType = in.nextInt();
          int owner = in.nextInt();
          int x = in.nextInt();
          int y = in.nextInt();
          int param1 = in.nextInt();
          int param2 = in.nextInt();
          entities.add(new EntityInfo(entityType, owner, x, y, param1, param2));
        }
        // update board now !
        updateEntities(entities);
        for (Cell cell : boxes) {
          updateBoxInfluence(cell);
        }
        debug();
        
        int maxBox = -1;
        Cell maxCell = me.cell;
        
        Path.PathItem maxItem = null;
        for (int y=0;y<11;y++) {
          for (int x=0;x<13;x++) {
            Cell c = getCellAt(x, y);
            if (c.boxCount > maxBox) {
              Path path = new Path(grid, me.x, me.y, c.x, c.y);
              Path.PathItem item = path.find();
              if (item != null) {
                System.err.println("found : "+c.x+","+c.y+" with weight: "+c.boxCount);
                maxBox = c.boxCount;
                maxCell = c;
                maxItem = item;
              }
            }
          }
        }
        in.nextLine();

        if (maxItem == null) {
          System.out.println("MOVE "+me.x+" "+me.y);
        } else {
          String command ="MOVE ";
          if (me.x == maxCell.x && me.y == maxCell.y) {
            command = "BOMB ";
          }
          System.out.println(command+maxItem.cell.x+" "+maxItem.cell.y);
        }
      }
    }

  }

  public static void main(String args[]) {
    in = new Scanner(System.in);
    int width = in.nextInt();
    int height = in.nextInt();
    myIndex = in.nextInt();
    in.nextLine();

    Game game = new Game(width, height);
    game.me = new APlayer();
    game.me.index = myIndex;

    game.play();
  }

  public static class Path {
    Map<Cell, PathItem> closedList = new HashMap<>();
    List<PathItem> openList = new ArrayList<>();
    private Cell[][] grid;
    private int ix;
    private int iy;
    private int tx;
    private int ty;

    Path(Cell[][] grid, int ix, int iy, int tx, int ty) {
      this.grid = grid;
      this.ix = ix;
      this.iy = iy;
      this.tx = tx;
      this.ty = ty;

    }

    PathItem find() {
      Cell origin = grid[ix][iy];
      PathItem root = new PathItem();
      root.cell = origin;

      openList.add(root);

      while (openList.size() > 0) {
        PathItem visiting = openList.remove(0); // imagine it's the best
        Cell cell = visiting.cell;
        if (cell.x == tx && cell.y == ty) {
          return visiting;
        }

        closedList.put(cell, visiting);
        if (cell.y > 0) {
          Cell cellUp = grid[cell.x][cell.y - 1];
          addToOpenList(visiting, cell, cellUp);
        }
        if (cell.y < grid[0].length - 1) {
          Cell cellDown = grid[cell.x][cell.y + 1];
          addToOpenList(visiting, cell, cellDown);
        }
        if (cell.x > 0) {
          Cell cellLeft = grid[cell.x - 1][cell.y];
          addToOpenList(visiting, cell, cellLeft);
        }
        if (cell.x < grid.length - 1) {
          Cell cellRight = grid[cell.x + 1][cell.y];
          addToOpenList(visiting, cell, cellRight);
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

    private void addToOpenList(PathItem visiting, Cell fromCell, Cell toCell) {
      if (closedList.containsKey(toCell)) {
        return;
      }
      if (!toCell.isBlocked()) {
        PathItem pi = new PathItem();
        pi.cell = toCell;
        pi.cumulativeLength = visiting.cumulativeLength + 1;
        pi.totalPrevisionalLength = pi.cumulativeLength + manhattanDistance(fromCell);
        pi.precedent = visiting;
        openList.add(pi);
      }
    }

    private int manhattanDistance(Cell cell) {
      return Math.abs(cell.x - tx) + Math.abs(cell.y - ty);
    }

    public static class PathItem {
      int cumulativeLength = 0;
      int totalPrevisionalLength = 0;
      PathItem precedent = null;
      Cell cell;

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
}
