package csb.game;

import csb.entities.CheckPoint;
import csb.entities.Collision;
import csb.entities.Pod;
import csb.entities.Type;

public class PhysicsEngine {
  public Pod pods[];
  public CheckPoint checkPoints[];

  public boolean collisionSimualtion = true;
  /**
   * perfect simulation of the CG game Engine (w/r to movement, collision)
   * 
   * 
   * input : speed and direction of Pods are updated
   * output : new position of pods, direction, speed & new checkpoint updated
   */
  public void simulate() {
    // get collision
    Collision nextCollision = null, collision;
    double t = 0.0;
    
    while(t <1.0) {
      nextCollision = null;
      for (int i=0;i<pods.length;i++) {
          Pod pod = pods[i];
          pod.radius = 1;// sale hack pour ne considerer que le centre du pod
          collision = pod.collision(checkPoints[pod.nextCheckPointId], t);
          if (collision != null && (nextCollision == null || nextCollision.t > collision.t)) {
            nextCollision = collision;
          }
          pod.radius = 400; 
        
        if (collisionSimualtion) {
          for (int j=i+1;j<pods.length;j++) {
            collision = pod.collision(pods[j], t);
            if (collision != null && (nextCollision == null || nextCollision.t > collision.t)) {
              nextCollision = collision;
            }
          }
        }
      }    

      if (nextCollision != null) {
        double delta = nextCollision.t - t;
        for (Pod pod : pods) {
          pod.move(delta);
        }
        t = nextCollision.t;
        if (nextCollision.b.type == Type.CHECKPOINT) {
          Pod pod = (Pod) nextCollision.a;

          pod.team.timeout = 0;
          pod.nextCheckPointId++;
          if (pod.nextCheckPointId == checkPoints.length) {
            pod.nextCheckPointId = 0;
          }
          if (pod.nextCheckPointId == 1) {
            pod.lap++;
          }
        } else { /* POD */
          nextCollision.a.bounce(nextCollision.b);
        }
      } else {
        double delta = 1.0 - t;
        for (Pod pod : pods) {
          pod.move(delta);
        }
        break;
      }
    }

    for (Pod pod : pods) {
      pod.end();
    }
    // add timeout of the team of team leaders
    pods[0].team.timeout++;
    pods[2].team.timeout++;
  }
}
