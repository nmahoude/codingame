package c4l.fsm;

import c4l.entities.Module;

public class FSMSample extends FSMNode {
  FSMSample(FSM fsm) {
    super(fsm);
  }
  @Override
  public void think() {
    
  }
  @Override
  public Module module() {
    return Module.SAMPLES;
  }

}
