package csb.ai.ag;

import csb.GameState;
import csb.entities.CheckPoint;
import csb.entities.Pod;
import lib.trigo.VectorLib;

public class AGEvaluator {
  GameState state;
  
  AGEvaluator(GameState state) {
    this.state = state;
  }

  public void evaluate(AGSolution sol) {
    sol.energy = 0.0
        + 10.0*checkPointPassedFeature(state.myRunner)
        + 5.0*distanceToCheckPointFeature(state.checkPoints[state.myRunner.nextCheckPointId], state.myRunner)
        + 0.5*exitSpeedFeature(state.myRunner)
        
        + 3.0 * distanceToCheckPointFeature(state.checkPoints[state.hisRunner.nextCheckPointId], state.myBlocker)
        + 1.0 * distance2ToPodFeature(state.myBlocker, state.hisRunner)
        - 1.0 * checkPointPassedFeature(state.hisRunner)
        + 0.5*distanceToCheckPointFeature(state.checkPoints[state.hisRunner.nextCheckPointId], state.hisRunner)
        
        ;
  }

  private double distance2ToPodFeature(Pod pod, Pod target) {
    final int MAX_DIST = 16000*16000+9000*9000;
    double dist2 = VectorLib.distance2(pod.x-target.x, pod.y-target.y);
    return (MAX_DIST - Math.min(MAX_DIST, dist2)) / MAX_DIST;
  }

  private double exitSpeedFeature(Pod pod) {
    if (pod.nextCheckPointId == pod.b_nextCheckPointId) return 0.0;
    
    final int MAX_SPEED = 600;
    double speed = VectorLib.length(pod.vx, pod.vy) / MAX_SPEED;
    return speed;
  }

  /**
   * has the pod passed through a checkpoint (only one), [0;1]
   * 1 is yes, 0 is false
   */
  private double checkPointPassedFeature(Pod pod) {
    return pod.nextCheckPointId != pod.b_nextCheckPointId ? 1.0 : 0.0;
  }
  /**
   * distance to next checkpoint score : [0;1]
   * 0 is far
   * 1 is on
   */
  private double distanceToCheckPointFeature(CheckPoint nextCheckPoint, Pod pod) {
    final int MAX_DIST = 16000*16000+9000*9000;
    double dist2 = VectorLib.distance2(pod.x-nextCheckPoint.x, pod.y-nextCheckPoint.y) - CheckPoint.RADIUS*CheckPoint.RADIUS;
    return (MAX_DIST - Math.min(MAX_DIST, dist2)) / MAX_DIST;
  }
}
