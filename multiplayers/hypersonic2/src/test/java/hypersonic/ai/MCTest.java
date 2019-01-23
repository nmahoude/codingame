package hypersonic.ai;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Scanner;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import hypersonic.Move;
import hypersonic.Player;

public class MCTest {

  @After
  public void teardown() {
    Player.myId = 0;
  }
  
  @Test
  public void doNotDropTooManyBombs() throws Exception {
    String input = ".....01......\r\n" + 
        ".X.X.X.X.X.X.\r\n" + 
        ".............\r\n" + 
        ".X.X.X.X.X.X.\r\n" + 
        ".....11......\r\n" + 
        "2X.X2X.X.X.X.\r\n" + 
        ".....1111....\r\n" + 
        "2X2X.X1X.X2X2\r\n" + 
        "20.....1...02\r\n" + 
        ".X.X.X2X.X.X.\r\n" + 
        ".............\r\n" + 
        "18\r\n" + 
        "0 0 10 4 1 5\r\n" + 
        "0 1 4 6 0 4\r\n" + 
        "0 2 8 4 1 4\r\n" + 
        "0 3 2 2 0 4\r\n" + 
        "1 0 7 0 2 5\r\n" + 
        "1 1 7 10 2 3\r\n" + 
        "1 3 2 5 2 4\r\n" + 
        "1 2 8 0 3 4\r\n" + 
        "1 2 8 2 6 4\r\n" + 
        "1 3 0 3 6 4\r\n" + 
        "1 2 8 3 7 4\r\n" + 
        "1 0 10 3 8 5\r\n" + 
        "2 0 5 8 1 1\r\n" + 
        "2 0 12 5 2 2\r\n" + 
        "2 0 7 4 1 1\r\n" + 
        "2 0 8 5 2 2\r\n" + 
        "2 0 6 1 2 2\r\n" + 
        "2 0 6 3 1 1";
    Scanner in = new Scanner(input);
    Player.myId = 3;
    
    Player player = new Player(in);
    player.readGameState();
    
    MC mc = new MC();
    
    Player.startTime = System.currentTimeMillis()+100;//_000;
    mc.think(player.state);
    
  }
  
  @Test
  public void goGrabTheBonus() throws Exception {
    String input =  
        "..00.....00..\r\n" + 
        ".X1X2X2X2X1X.\r\n" + 
        "1.02.....20.1\r\n" + 
        "2X.X.X.X.X.X2\r\n" + 
        "01.211.112.10\r\n" + 
        ".X.X0X.X0X.X.\r\n" + 
        "01.211.112.10\r\n" + 
        "2X.X.X.X.X.X2\r\n" + 
        "1.02.....20.1\r\n" + 
        ".X1X2X2X2X1X.\r\n" + 
        "..00.....00..\r\n" + 
        "2\r\n" + 
        "0 0 0 0 1 3\r\n" + 
        "0 1 12 10 1 3";
    Scanner in = new Scanner(input);
    Player.myId = 0;
    
    Player player = new Player(in);
    player.readGameState();
    
    MC mc = new MC();
    
    Player.startTime = System.currentTimeMillis()+100;//_000;
    mc.think(player.state);
    
    System.err.println(Arrays.asList(MC.bestMoves));
    assertThat(MC.bestMoves[0], is(Move.DOWN));
  }
  
  
  @Test
  @Ignore
  public void debug() throws Exception {
    String input  ="....20.021...\r\n" + 
        ".X.X0X2X0X.X.\r\n" + 
        "....2...2....\r\n" + 
        ".X.X.X.X.X.X.\r\n" + 
        "....0.0......\r\n" + 
        ".X.X2X2X2X.X.\r\n" + 
        ".1..0.0.....1\r\n" + 
        ".X.X.X.X.X.X.\r\n" + 
        "...22...22.21\r\n" + 
        ".X.X0X2X.X.X.\r\n" + 
        ".....0.......\r\n" + 
        "18\r\n" + 
        "0 0 3 6 1 5\r\n" + 
        "0 1 11 10 2 5\r\n" + 
        "0 2 10 4 1 5\r\n" + 
        "0 3 2 7 1 6\r\n" + 
        "1 3 4 10 4 6\r\n" + 
        "1 0 2 4 5 5\r\n" + 
        "1 2 11 2 5 5\r\n" + 
        "1 2 10 2 7 5\r\n" + 
        "2 0 2 1 1 1\r\n" + 
        "2 0 3 0 1 1\r\n" + 
        "2 0 12 5 1 1\r\n" + 
        "2 0 3 2 2 2\r\n" + 
        "2 0 1 4 1 1\r\n" + 
        "2 0 0 5 1 1\r\n" + 
        "2 0 0 6 1 1\r\n" + 
        "2 0 1 8 2 2\r\n" + 
        "2 0 10 1 1 1\r\n" + 
        "2 0 11 6 1 1";
    Player.myId = 0;
    Player.DEBUG_AI = true;
    
    Scanner in = new Scanner(input);
    Player player = new Player(in);
    player.readGameState();
    
    MC mc = new MC();
    
    Player.startTime = System.currentTimeMillis()+100; //_000;
    mc.think(player.state);
    
    System.err.println(Arrays.asList(MC.bestMoves));
    assertThat(MC.bestMoves[0], is(Move.UP_BOMB));
  }
}
