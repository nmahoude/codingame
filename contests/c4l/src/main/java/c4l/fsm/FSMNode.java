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

  /**
   * Common method to handle the case where we don"t have any samples
   */
  void handleCarriedSampleEmpty() {
    //TODO check to recycle at diag (cloud)
    List<Sample> samples = findDoableSampleInCloud();
    samples.sort(Sample.orderByHealthDecr);
    
    if (!samples.isEmpty() && 
        (samples.get(0).health >= 10 || samples.get(0).health+me.score >= 170)) {
      System.err.println("Found a doable sample @ DIAG");
      if (fsm.isAt(Module.DIAGNOSIS)) {
        fsm.connect(samples.get(0).id, "hey ! we are @DIAG, so get the sample");
        return;
      } else {
        fsm.goTo(Module.DIAGNOSIS, "Need to go to diag to get a sample");
        return;
      }
    } else {
      fsm.goTo(Module.SAMPLES, "Nohing in DIAG, go to sample");
      return;
    }
  }

  List<Sample> findDoableSampleInCloud() {
    List<Sample> samples = new ArrayList<>();
    for (Sample sample : state.availableSamples) {
      // before checking if sample is doable, filter on sample health points
      if (sample.health < 10 && me.score + sample.health < 170) continue;
      
      if (me.isThereEnoughMoleculeForSample(state, sample)) {
        samples.add(sample);
      }
    }
    return samples;
  }
  /**
   * Based on the samples and molecules I have, 
   * check if I have enough material to win the game
   * @return
   */
  boolean checkIfIHaveEnoughPointsToWin() {
    MoleculeOptimizerNode node = new MoleculeOptimizerNode();
    int index = 0;
    for (Sample sample : me.carriedSamples) {
      node.createSample(index++, sample.costs, sample.health);
    }
    node.createStorage(me.storage);
    node.createExpertise(me.expertise);
    node.createAvailable(state.availables);

    double score = node.getLocalScore().score;
    if (me.score + score >= 170.0) {
      return true;
    }
    return false;
  }
}
