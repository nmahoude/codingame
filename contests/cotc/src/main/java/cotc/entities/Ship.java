package cotc.entities;

import java.util.List;

import cotc.utils.Coord;

public class Ship extends Entity {
  public static final int MAX_SHIP_HEALTH = 100;

  public Action action; // TODO why need this ?
  
  public final int owner;
  public int orientation;
  public int speed;
  public int health;
  public int cannonCooldown;
  public int mineCooldown;

  private int b_orientation;
  private int b_speed;
  private int b_health;
  private int b_cannonCooldown;
  private int b_mineCooldown;

  // temporary values before collisions / fire ball, ..
  public int newOrientation;
  public Coord newPosition;
  public Coord newBowCoordinate;
  public Coord newSternCoordinate;
  public Coord target;

  public Ship(int entityId, int x, int y, int orientation, int owner) {
    super(EntityType.SHIP, entityId, x, y);
    this.orientation = orientation;
    this.speed = 0;
    this.health = MAX_SHIP_HEALTH;
    this.owner = owner;
  }

  public void update(int x, int y, int orientation, int speed, int health, int owner) {
    super.update(x, y);
    this.orientation = orientation;
    this.speed = speed;
    this.health = health;
  }


  public void backup() {
    super.backup();

    b_orientation = orientation;
    b_speed = speed;
    b_health = health;
    b_cannonCooldown = cannonCooldown;
    b_mineCooldown = mineCooldown;
  }

  public void restore() {
    super.restore();

    orientation = b_orientation;
    speed = b_speed;
    health = b_health;
    cannonCooldown = b_cannonCooldown;
    mineCooldown = b_mineCooldown;
  }

  public void faster() {
    this.action = Action.FASTER;
  }

  public void slower() {
    this.action = Action.SLOWER;
  }

  public void port() {
    this.action = Action.PORT;
  }

  public void starboard() {
    this.action = Action.STARBOARD;
  }

  public void placeMine() {
    this.action = Action.MINE;
  }

  public Coord stern() {
    return position.neighbor((orientation + 3) % 6);
  }

  public Coord bow() {
    return position.neighbor(orientation);
  }

  public Coord newStern() {
    return position.neighbor((newOrientation + 3) % 6);
  }

  public Coord newBow() {
    return position.neighbor(newOrientation);
  }

  public boolean at(Coord coord) {
    Coord stern = stern();
    Coord bow = bow();
    return stern != null && stern.equals(coord) || bow != null && bow.equals(coord) || position.equals(coord);
  }

  public boolean newBowIntersect(Ship other) {
    return newBowCoordinate != null && (newBowCoordinate.equals(other.newBowCoordinate) || newBowCoordinate.equals(other.newPosition)
        || newBowCoordinate.equals(other.newSternCoordinate));
  }

  public boolean newBowIntersect(List<Ship> ships) {
    for (Ship other : ships) {
      if (this != other && newBowIntersect(other)) {
        return true;
      }
    }
    return false;
  }

  public boolean newPositionsIntersect(Ship other) {
    boolean sternCollision = newSternCoordinate != null && (newSternCoordinate.equals(other.newBowCoordinate)
        || newSternCoordinate.equals(other.newPosition) || newSternCoordinate.equals(other.newSternCoordinate));
    boolean centerCollision = newPosition != null && (newPosition.equals(other.newBowCoordinate) || newPosition.equals(other.newPosition)
        || newPosition.equals(other.newSternCoordinate));
    return newBowIntersect(other) || sternCollision || centerCollision;
  }

  public boolean newPositionsIntersect(List<Ship> ships) {
    for (Ship other : ships) {
      if (this != other && newPositionsIntersect(other)) {
        return true;
      }
    }
    return false;
  }

  public void damage(int health) {
    this.health -= health;
    if (this.health <= 0) {
      this.health = 0;
    }
  }

  public void heal(int health) {
    this.health += health;
    if (this.health > MAX_SHIP_HEALTH) {
      this.health = MAX_SHIP_HEALTH;
    }
  }

  public void fire(int x, int y) {
    Coord target = Coord.get(x, y);
    this.target = target;
    this.action = Action.FIRE;
  }
  
  public String toPlayerString(int playerIdx) {
    return toPlayerString(orientation, speed, health, owner == playerIdx ? 1 : 0);
  }


}
