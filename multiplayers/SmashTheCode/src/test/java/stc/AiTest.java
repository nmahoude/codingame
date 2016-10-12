package stc;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;

public class AiTest {

  @Test
  public void minimizeHeight() throws Exception {
    // preload
    DFSNode.add(500_000);
    
    Game game = new Game();
    GameTest.setNextBlocks(game, 
        "11",
        "23",
        "44"
        );
    BoardTest.prepareBoard(game.myBoard,
        "......",
        "......",
        "......",
        "......",
        "......",
        "......",
        "......",
        "......",
        "......",
        "......",
        "......",
        "....11");
    
    long time1 = System.currentTimeMillis();
    Ai ai = new Ai(game);
    ai.think();
    long time2 = System.currentTimeMillis();
    System.err.println("T2/T1: "+time1+","+time2);
    assertThat((time2-time1 < 100), is(true));
  }
  @Test
  public void oneRounds() throws Exception {
    DFSNode dummy = new DFSNode();
    
    Game game = new Game();
    GameTest.setNextBlocks(game, 
        "12",
        "21",
        "44"
        );
    BoardTest.prepareBoard(game.myBoard,
        "......",
        "......",
        "......",
        "......",
        "......",
        "......",
        "......",
        "......",
        "......",
        "......",
        "......",
        "....11");
    
    Ai ai = new Ai(game);
    ai.think();
  }
  
  @Test
  @Ignore
  public void twoRounds() throws Exception {
    Game game = new Game();
    GameTest.setNextBlocks(game, 
        "12",
        "21",
        "44"
        );
    BoardTest.prepareBoard(game.myBoard,
        "......",
        "......",
        "......",
        "......",
        "......",
        "......",
        "......",
        "......",
        "......",
        "......",
        "......",
        "....11");
    
    Ai ai = new Ai(game);
    ai.think();
    ai.think();
  }
  
  @Test
  @Ignore
  public void debugWhy1_2() throws Exception {
    Game game = new Game();
    GameTest.setNextBlocks(game, 
        "41",
        "41",
        "51",
        "34",
        "12",
        "41",
        "24",
        "24"
        );
  
    BoardTest.prepareBoard(game.myBoard,
        "092000",
        "092099",
        "051999",
        "999999",
        "912922",
        "914331",
        "933159",
        "153553",
        "924299",
        "344134",
        "529991",
        "552921");
    
    Ai ai = new Ai(game);
    ai.think();
    
    assertThat(ai.command, is(not("1 0")));
  }
}
