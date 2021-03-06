package god;

import cgcollections.arrays.FastArray;
import cgutils.io.InputReader;
import god.entities.Drone;
import god.entities.Zone;
import god.utils.Point;

public class GameState {
  public static final int WIDTH = 4000;
  public static final int HEIGHT = 1800;
  public static final Point CENTER = new Point(WIDTH/2, HEIGHT/2);
  
  FastArray<Drone> drones = new FastArray<>(Drone.class, 50);
  FastArray<Drone> myDrones = new FastArray<>(Drone.class, 11);
  
  FastArray<Zone> zones = new FastArray<>(Zone.class, 8);
  int totalPoints[] = new int[4];
  int turnPoints[];
  
  public static int zoneCount;
  public static int droneCount;
  public static int playerCount;

  public static int myId;

  public void readInit(InputReader in) {
    playerCount = in.nextInt();
    myId = in.nextInt();
    droneCount = in.nextInt();
    zoneCount = in.nextInt();
    
    for (int i = 0; i < zoneCount; i++) {
      int x = in.nextInt();
      int y = in.nextInt();
      Zone zone = new Zone(x, y);
      zone.id = i;
      zones.add(zone);
    }
  }
  
  public void readRound(InputReader in) {
    turnPoints = new int[4]; // zone in possession for this turn
    
    for (int i = 0; i < zoneCount; i++) {
      int TID = in.nextInt(); 
      Zone zone = zones.elements[i];
      zone.owner = TID;
      zone.clearDrones();
      if (zone.owner != -1) {
        totalPoints[zone.owner]++;
        turnPoints[zone.owner]++;
      }
    }
    
    for (int i = 0; i < playerCount; i++) {
      for (int j = 0; j < droneCount; j++) {
        int x = in.nextInt();
        int y = in.nextInt();

        Drone drone = drones.elements[j + i*droneCount];
        if (drone == null) {
          drone = new Drone(j, x, y);
          drone.owner = i;
          drones.elements[j + i*droneCount] = drone;
          if (i == myId) {
            myDrones.add(drone);
          }
        }
        drone.update(x, y);
        affectDroneToZone(drone);
      }
    }
    
    for (int i = 0; i < zoneCount; i++) {
      Zone zone = zones.elements[i];
      zone.updateDrones();
    }

  }

  private void affectDroneToZone(Drone drone) {
    drone.inZone = null;
    for (Zone zone : zones) {
      zone.allDronesInOrder.add(drone);
      if (zone.position.dist2(drone.position) < 100*100) {
        drone.inZone = zone;
        if (drone.owner == myId) {
          zone.myDrones.add(drone);
        }
        zone.drones[drone.owner]++;
      }
    }
  }

  public Zone findClosestZone(Drone drone) {
    Zone best = null;
    int bestDist = Integer.MAX_VALUE;
    for (Zone zone : zones) {
      int dist2 = zone.position.dist2(drone.position);
      if (dist2 < bestDist) {
        bestDist = dist2;
        best = zone;
      }
    }
    return best;
  }

  public void debugFutureOwners() {
    System.err.println("Future owners");
    zones.forEach(zone -> {
      System.err.print(zone.id + " => [");
      for (int i=0;i<Zone.TURNS_IN_FUTURE;i++) {
        if (zone.futureOwner[i] != -1)
          System.err.print(zone.futureOwner[i]);
        else 
          System.err.print("x");
        System.err.print(" ");
      }
      System.err.print("]");
      System.err.println();
    });    
  }
}
