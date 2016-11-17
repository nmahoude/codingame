package stc2.play;

import stc2.Game;
import stc2.MCNode;
import stc2.MCTSOld;
import stc2.MCTSOld.AjustementVariables;

public class AI implements IAI {
  Game game;
  int player;
  MCTSOld mcts= new MCTSOld();
  AjustementVariables ajust;
  
  public IAI.Move getMove() {
    mcts.ajust = this.ajust;
    if (player == 2) {
      mcts.attachGame(game, game.otherBoard, game.myBoard);
    } else {
      mcts.attachGame(game, game.myBoard, game.otherBoard);
    }
    
    mcts.simulate(false);
    if (mcts.bestNode == null) {
      return null;
    }
    return new IAI.Move(mcts.bestNode.rotation, mcts.bestNode.column);
  }

  public void prepare(Game game, int player) {
    this.game = game;
    this.player = player;
  }
  @Override
  public void setAjust(AjustementVariables ajust) {
    this.ajust = ajust;
  }
}
