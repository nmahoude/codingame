package marsLander.ai;

import java.util.Arrays;
import java.util.Comparator;

import marsLander.Mars;
import marsLander.MarsLander;
import marsLander.Player;
import marsLander.sim.Simulation;

public class AG {
  public final static int SIZE = 50;

  Mars mars;
  MarsLander lander;
  private MarsLander originalLander;

  private Simulation simulation;
  public AGSolution solutions[] = new AGSolution[SIZE];

  private AGSolution oldSolution;

  public AG(Mars mars, MarsLander originalLander) {
    for (int i = 0; i < SIZE; i++) {
      solutions[i] = new AGSolution();
    }
    this.mars = mars;
    this.originalLander = originalLander;
    this.lander = new MarsLander();

    this.simulation = new Simulation(mars, this.lander);
  }

  public void think() {
    System.err.println("mars at x : " + mars.dist[originalLander.getXAsInt()]);
    System.err.println("Dist to landing zone : " + mars.distanceToLandingZone(originalLander));

    randomizePopulation();
    if (oldSolution != null) {
      solutions[0].copyFrom(oldSolution);
    }
    
    int iter = 0;
    System.err.println("Start :"+Player.start);
    System.err.println("Current : "+System.currentTimeMillis());
    while (System.currentTimeMillis() - Player.start < 99) {
    //int iterations = Player.turn == 0 ? 500 : 100;
    //for (int iter = 0; iter < iterations; iter++) {
      oneIteration();
      evolvePopulation();
      iter++;
    }
    System.err.println("Best score found : " + solutions[0].score);
    System.err.println("Iterations : " + iter);
  }

  public void randomizePopulation() {
    for (int i = 0; i < SIZE; i++) {
      solutions[i].randomize();
    }
  }

  public void oneIteration() {
    for (int i = 0; i < SIZE; i++) {
      lander.copyFrom(originalLander);

      play(solutions[i]);
    }
  }

  public static double score(Simulation simulation, Mars mars, MarsLander lander) {

    double score = 0.0;
    
    if (simulation.result != 1) {
      // crashed
      if (mars.distanceToLandingZone(lander) != 0.0) {
        // out of landing zone
        
        score += (100 - 100 * mars.distanceToLandingZone(lander));

        double currentSpeed = Math.sqrt(lander.vx * lander.vx + lander.vy * lander.vy);
        score -= 0.5 * Math.max(currentSpeed - 100, 0);

      } else {
        // in landing zone
        double xPen = 0.0;
        if (20 < Math.abs(lander.vx)) {
          xPen = (Math.abs(lander.vx) - 20) / 2;
        }
        double yPen = 0.0;
        if (lander.vy < -40) {
          yPen = (-40 - lander.vy) / 2;
        }
        score += (200 - xPen - yPen);
      }
    } else {
      // no crash
      score += 200.0 + 100.0 * lander.fuel / Player.maxFuel;
    }
      
    return score;
  }

  public static double score_old(Simulation simulation, Mars mars, MarsLander lander) {

    double score = 0.0;
    if (simulation.result == -1) {
      score -= 10_000;
    } else if (simulation.result == 1) {
      return Double.POSITIVE_INFINITY;
    }
    if (mars.distanceToLandingZone(lander) == 0.0) {
      score += 1_000.0;
    }
    
    score += 10.0 * (100 - 100 * mars.distanceToLandingZone(lander));

    if (lander.vy < -40 || 20 < Math.abs(lander.vx)) {
      double xPen = 0.0;
      if (20 < Math.abs(lander.vx)) {
        xPen = (Math.abs(lander.vx) - 20) / 2;
      }
      double yPen = 0.0;
      if (lander.vy < -40) {
        yPen = (-40 - lander.vy) / 2;
      }
      score += 0.5 * (100 - xPen - yPen);
    }

    score += 10.0 * lander.fuel / Player.maxFuel;

    double currentSpeed = Math.sqrt(lander.vx * lander.vx + lander.vy * lander.vy);
    score -= 0.5 * Math.max(currentSpeed - 100, 0);

    return score;
  }

  public void play(AGSolution solution) {
    solution.score = 0;
    simulation.reset();
    for (int i = 0; i < AGSolution.DEPTH; i++) {
      simulation.update(solution.values[i]);
      if (simulation.result != 0)
        break;
    }
    solution.score += score(simulation, mars, lander);
  }

  public void evolvePopulation() {
    Arrays.sort(solutions, new Comparator<AGSolution>() {
      @Override
      public int compare(AGSolution o1, AGSolution o2) {
        return Double.compare(o2.score, o1.score);
      }
    });
    
    int ratio = 2;
    int next = SIZE / ratio;
    for (;next<SIZE;next++) {
      int rand1 = Player.rand.nextInt(SIZE / ratio);
      int rand2 = Player.rand.nextInt(SIZE / ratio);
      solutions[next].crossover(solutions[rand1], solutions[rand2]);
    }
    
  }

  public void prepareNextSolution() {
    oldSolution = new AGSolution();
    oldSolution.copyWithdecal(solutions[0]);
  }
}
