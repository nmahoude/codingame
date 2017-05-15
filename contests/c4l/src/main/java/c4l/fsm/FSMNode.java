package c4l.fsm;

import java.util.ArrayList;
import java.util.List;

import c4l.GameState;
import c4l.entities.Module;
import c4l.entities.Robot;
import c4l.entities.Sample;
import c4l.molecule.MoleculeOptimizerNode;

public abstract class FSMNode {

  FSM fsm;
  Robot me;
  GameState state;
  
  FSMNode(FSM fsm) {
    this.fsm = fsm;
    me = fsm.me;
    state = fsm.state;
  }

  public abstract void think();

  public void prethink() {
  }

  public abstract Module module();
  
  /**
   * Return a list of all completeSamples wr molecules & expertise
   * Warning : it doesn't mean we can complete all at once, 
   * completing one will remove molecules from storage
   * @return
   */
  public List<Sample> getCompletableSamples() {
    List<Sample> samples = new ArrayList<>();
    for (Sample sample : me.carriedSamples) {
      if (!sample.isDiscovered()) continue;
      if (me.hasMolecules(sample)) {
        samples.add(sample);
      }
    }
    return samples;
  }

  List<Sample> findDoableSampleInCloud() {
    List<Sample> samples = new ArrayList<>();
    for (Sample sample : state.availableSamples) {
      // before checking if sample is doable, filter on sample health points
      if (me.isThereEnoughMoleculeForSample(state, sample)) {
        samples.add(sample);
      }
    }
    return samples;
  }
  
  /**
   * Get new sample from SAMPLE or DIAG
   * @return certified an action will be taken (always go somewhere)
   */
  boolean getNewSamples() {
    List<Sample> samples = findDoableSampleInCloud();
    
    // TODO check for sample qualitay before taking it (rank 1 is really interesting ?)
    if (!samples.isEmpty() ) {
      fsm.goTo(Module.DIAGNOSIS, "Found a doable sample @ DIAG");
      return true;
    } else {
      fsm.goTo(Module.SAMPLES, "Nohing in DIAG, go to sample");
      return true;
    }
    
  }
  /**
   * Based on the samples and molecules I have, 
   * check if I have enough material to win the game
   * @return
   */
  boolean checkIfIHaveEnoughPointsToWin() {
    // TODO redo it we can't win anymore with 170 pts, but there is other way (in the end game)
    return false;
  }
}
