package coderoyale;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import coderoyale.sites.Barrack;
import coderoyale.sites.Mine;
import coderoyale.sites.Site;
import coderoyale.sites.Structure;
import coderoyale.sites.Tower;
import coderoyale.units.Queen;
import coderoyale.units.Unit;

public class Player {
  
  private static final int KNIGHT_COST = 80;
  private static final int ARCHER_COST = 100;
  private static final int GIANT_COST  = 140;
  
  private static List<Site> sitesRenamed;
  private static List<Tower> allTowers = new ArrayList<>();
  private static List<Barrack> barracks = new ArrayList<>();
  private static List<Mine> mines = new ArrayList<>();

  public static final int WIDTH = 1920;
  static final int HEIGHT = 1000;
  static int oppositeWallX;

  static Phase phase = Phase.EXPANSION;
  
  private static List<Unit> units;
  static int turn = 0;
  public static Queen me = new Queen();
  private static Queen him = new Queen();
  
  public static void main(String args[]) {
    Scanner in = new Scanner(System.in);
    int numSites = in.nextInt();
    sitesRenamed = new ArrayList<>();
    for (int i = 0; i < numSites; i++) {
      int siteId = in.nextInt();
      int x = in.nextInt();
      int y = in.nextInt();
      int radius = in.nextInt();
      Site site = new Site(siteId, x, y, radius);
      sitesRenamed.add(site);
    }

    // game loop
    while (true) {
      turn++;
      readGameState(in, numSites);

      Site closestFree = null;
      for (Site site : sitesRenamed) {
        if (site.imOwner()) {
          continue;
        }
        if (closestFree == null ) {
          closestFree = site;
        } else if (site.imNotOwner() && closestFree.pos.dist2(me.pos) > site.pos.dist2(me.pos)) {
          closestFree = site;
        }
      }

      try {
        issueQueenCommand(closestFree);
        System.err.println("WARNING : NO COMMAND ISSUED");
        System.out.println("WAIT");
      } catch(CommandException ce) {
        ce.output();
      }
      issueTrainCommand();
    }
  }

  private static void readGameState(Scanner in, int numSites) {
    me.reset();
    him.reset();
    
    me.gold = in.nextInt();
    me.touchedSite = in.nextInt(); // -1 if none
    
    barracks.clear();
    allTowers.clear();
    mines.clear();
    
    for (int i = 0; i < numSites; i++) {
      int siteId = in.nextInt();
      int gold = in.nextInt(); // used in future leagues
      int maxMineSize = in.nextInt(); // used in future leagues
      int structureType = in.nextInt(); // -1 = No structure, 2 = Barracks
      int owner = in.nextInt(); // -1 = No structure, 0 = Friendly, 1 = Enemy
      int param1 = in.nextInt();
      int param2 = in.nextInt();
      Site site = getSite(siteId);
      site.gold = gold;
      site.maxMineSize = maxMineSize;
      
      Queen ownerQueen = owner == 0 ? me : him;
      
      if (structureType == Structure.BARRACK) {
        Barrack b= new Barrack(site);
        b.turnBeforeTrain = param1;
        b.subtype = param2; // KNIGHT, ... , GIANT
        site.structure = b;
        barracks.add(b);
        if (b.subtype == Structure.KNIGHT) {
          ownerQueen.knightBarracks.add(b);
        }
      } else if (structureType == Structure.TOWER){
        Tower t = new Tower(site);
        t.life = param1;
        t.attackRadius = param2;
        site.structure = t;
        allTowers.add(t);
        ownerQueen.towers.add(t);
      } else if (structureType == Structure.MINE) {
        Mine m = new Mine(site);
        m.incomeRate = param1;
        site.structure = m;
        mines.add(m);
        ownerQueen.mines.add(m);
      }  else {
        site.structure = Structure.NONE;
      }
      site.structure.owner = owner;
    }
    
    int numUnits = in.nextInt();
    units = new ArrayList<>();
    for (int i = 0; i < numUnits; i++) {
      int x = in.nextInt();
      int y = in.nextInt();
      int owner = in.nextInt();
      int unitType = in.nextInt(); // -1 = QUEEN, 0 = KNIGHT, 1 = ARCHER
      int health = in.nextInt();
      Unit unit;
      
      if (unitType == -1 && owner == 0) {
        me.updatePos(x, y);
        unit = me;
      } else if (unitType == -1 && owner == 1) {
        him.updatePos(x, y);
        unit = him;
      } else {
        unit = new Unit();
        unit.owner = owner;
        unit.updatePos(x, y);
        unit.type = unitType;
      }
      unit.health = health;
      units.add(unit);
    }
  }

  private static void issueTrainCommand() {
    String train = "TRAIN ";
    List<Site> closestToHim = getSiteByClosestDistance(him).stream()
      .filter(s -> { return s.imOwner() && s.canTrain(); })
      .collect(Collectors.toList());
    
    int gold = me.gold;
    
    boolean needGiant = needGiant();
    if (needGiant && gold > GIANT_COST) {
      for (Site site : closestToHim) {
        if (site.isGiant()) {
          train += site.id + " ";
          gold -= GIANT_COST;
          needGiant = false;
        }
      }      
    }
    if (!needGiant) {
      // only spend money on knight if we dont need a giant ...
      System.err.println("Build as many knight as possible ....");
      for (Site site : closestToHim) {
        if (gold < KNIGHT_COST) break;
        train += site.id + " ";
        gold -= KNIGHT_COST;
      }
    } else {
      System.err.println("not spending for giant");
    }
    System.out.println(train.trim());
  }

  private static boolean needGiant() {
    // TODO when do we need a giant ?
    boolean giantExists = units.stream()
        .filter(u -> u.owner == me.owner)
        .filter(Unit::isGiant)
        .count() > 0;
    boolean giantBarrackExists = sitesRenamed.stream()
        .filter(Site::imOwner)
        .filter(Site::isGiant)
        .count() > 0;
        return giantBarrackExists && !giantExists ;
  }

  private static void issueQueenCommand(Site closestFree) {
    //initialRush();
    
    fleeFromTowers();
    if (needToBlitz()) {
      blitz();
    }

    takeCareOfTowers(200);
    stabilisation();
    takeCareOfTowers(600);
    doDefensiveMoves();

    //buildSomeInitialBarracks();
    buildSomeInitialMines(closestFree);
    doGiantInitiative();
    doOtherMoves(closestFree);
    
    return;
  }

  
  /**
   *  try to stabilize the game when there is enough security
   *  low creeps number, a barrier of towers, etc ...
   */
  private static void stabilisation() {
    if (me.towers.size() < 4) {
      return; // too few towers to try to stabilize
    }

    int frontierX = calculateFrontierPosition();
    List<Site> sites = getSiteByClosestDistance(me);

    // check if there is still empty sites behind the frontier (better attacks this space!)
    for (Site site : sites) {
      if (site.noBuilding() && !me.onHomeSide(site.pos.x, frontierX)) {
        System.err.println("Still room for expansion ...");
        
        return; // don't try to stabilise if there is still room to expand
      }
    }
    
    // try to build mine & barracks if needed
    for (Site site : sites) {
      if (site.noBuilding() && me.onHomeSide(site.pos.x, frontierX)) {
        // build a mine if possible
        moveToSiteAndBuildMine(site);
        
        // build a barrack if needed
        boolean needBarracks = false;
        List<Site> myBarracks = sitesRenamed.stream().filter(Site::imOwner).filter(Site::isBarrack).collect(toList());
        boolean betterBarracksExists = false;
        for (Site b : myBarracks) {
          if (me.onHomeSide(site.pos.x, b.pos.x)) {
            betterBarracksExists = true;
            System.err.println("No need to barracks illed placed");
          }
        }
        if (!betterBarracksExists) {
          me.moveTo(site).then(site::buildKnightBarrack).end();
        }
        // build a giant if needed
        // TODO find a good spot to build giant ?
        // buildGiantBarracks(site);
      }
    }
 /** all building behind our line of 'defense' (frontier) are built
 / check if we can destroy some tower ?
 */
    for (Site site : sites) {
      if (site.isTower() && me.onHomeSide(site.pos.x, frontierX)) {
        moveToSiteAndBuildMine(site);
        me.moveTo(site).then(site::buildKnightBarrack).end();
      }
    }
    
  }

  private static void buildGiantBarracks(Site site) {
    if (sitesRenamed.stream()
        .filter(Site::imOwner)
        .filter(Site::isGiant)
        .count() == 0) {
      me.moveTo(site).then(site::buildGiant).end();
    }
  }

  private static int calculateFrontierPosition() {
    List<Tower> farthestTowers = me.towers.stream()
        .sorted((t1, t2) -> Double.compare(
                                    Math.abs(t1.attachedTo.pos.x - 1920), 
                                    Math.abs(t2.attachedTo.pos.x - 1920)))
        .collect(Collectors.toList());
    int frontier = 0;
    int towers = Math.min(3, farthestTowers.size());
    for (int i=0;i<towers;i++) {
      frontier += farthestTowers.get(i).attachedTo.pos.x;
    }
    frontier /=towers;
    System.err.println("The frontier is x = " + frontier);
    return frontier;
  }

  private static void fleeFromTowers() {
    List<Site> hisTowerSites = sitesRenamed.stream()
        .filter(Site::isTower)
        .filter(Site::imNotOwner)
        .collect(Collectors.toList());
    int underAttacks = 0;
    for (Site st : hisTowerSites) {
      if (st.getTower().protects(me, 0)) {
        if (!doTowerNeedToKillCreeps(me)) {
          underAttacks++;
        }
      }
    }
    if (underAttacks > 0) {
      System.err.println("I'm in enemy territory ("+underAttacks+")");
      flee();
    }
  }

  private static boolean doTowerNeedToKillCreeps(Queen queen) {
    for (Unit unit : units) {
      if (unit.owner == queen.owner && unit.isKnight()) {
        return true;
      }
    }
    return false;
  }

  // flee from close enemy creeps
  private static void flee() {
    List<Unit> enemyUnits = units.stream()
        .filter(u -> u.owner == 1)
        .collect(Collectors.toList());
    List<Site> hisTowers = sitesRenamed.stream()
          .filter(Site::isTower)
          .filter(Site::imNotOwner)
          .filter(s -> { return s.getTower().protects(me, 0);})
          .collect(Collectors.toList());
    
    if (enemyUnits.size() == 0 && hisTowers.size() == 0) {
      return; // no need to flee ....
    }
    
    // build a flee vector
    Vector flee = new Vector();
    System.err.println("Building flee vector");
    for (Unit unit : enemyUnits) {
      double dist = unit.pos.dist(me.pos);
      Vector invDir = unit.pos.direction(me.pos).normalize();
      Vector contribution = invDir.mult(1.0 / (dist * dist)); // plus c'est loin moins ca compte
      flee = flee.add(contribution);
    }
    
    for (Site site : hisTowers) {
      Tower t = site.getTower();
      if (t.protects(me, 60)) {
        // the tower can harm me, 
        double dist = site.pos.dist(me.pos);
        Vector invDir = site.pos.direction(me.pos).normalize();
        System.err.println("dist : "+dist + " invDir : "+invDir);
        Vector contribution = invDir.mult(1.0 / (dist * dist)); // plus c'est loin moins ca compte
        System.err.println("site "+ site + " contibution : "+ contribution);
        flee = flee.add(contribution);
      }
    }
    flee = flee.normalize().mult(60);
    System.err.println("Flee Vector : " + flee);
    me.moveTo(me.pos.add(flee)).end();
    
  }
  private static void blitz() {
    // transform every tower in barracks
    List<Site> sites = getSiteByClosestDistance(me).stream()
        .filter(s -> ! (s.imNotOwner() && s.isTower()))
        .filter(s -> ! (s.imOwner() && s.isBarrack()))
        .collect(Collectors.toList());
    
    if (!sites.isEmpty()) {
      Site site = sites.get(0);
      moveToSiteAndBuildMine(site);
      if (!site.isAMine()) {
        me.moveTo(site).then(site::buildKnightBarrack).end();
      }
    }
  }

  private static boolean needToBlitz() {
    if (turn < 150) return false;
    if (me.health > him.health) return false;
    System.err.println("BLITZ !");
    return true;
  }

  private static void buildSomeInitialBarracks() {
    if (barracks.stream().filter(b -> { return b.owner == me.owner;}).count() == 0) {
      List<Site> sites = getSiteByClosestDistance(me);
      Site site = sites.get(0);
      me.moveTo(site).then(site::buildKnightBarrack).end();
    }
  }

  static boolean initialRush = true;
  static Site rushSite  = null;
  private static void initialRush() {
    if (!initialRush) return;
    System.err.println("STILL IN RUSH");
    
    if (rushSite == null) {
      // find the farthest site that is closest to me than him
      double farther = 0;
      Site best = null;
      for (Site site : sitesRenamed) {
        double dist = site.pos.dist(me.pos);
        if (dist > farther && dist < site.pos.dist(him.pos)) {
          farther = dist;
          best = site;
        }
      }
      rushSite = best;
      System.err.println("Deciding rush site " + rushSite);
    }
    
    me.moveTo(rushSite)
          .then(rushSite::buildTower)
          .then(rushSite::upgradeTower)
          .end();
    System.err.println("finishing rush");
    initialRush = false;    
  }

  private static void takeCareOfTowers(int energyThreshold) {
    System.err.println("Take care of towers ...");
    List<Site> towers = getSiteByClosestDistance(me).stream()
        .filter(Site::imOwner)
        .filter(Site::isTower)
        .collect(Collectors.toList());
    
    if (towers.isEmpty()) {
      return;
    }
    for (int i=0;i<Math.min(2, towers.size());i++) {
      Site site = towers.get(i);
      Tower tower = (Tower)(site.structure);
      if (tower.life < energyThreshold) {
        System.err.println("Refill tower " + site);
        me.moveTo(site).then(site::upgradeTower).end();
      }
    }
  }

  private static void doDefensiveMoves() {
    int maxRange = 600;
    int ennemyKnightAround = getEnnemyKnightAround(maxRange);
    System.err.println("Ennemy knights around : "+ ennemyKnightAround);
    if (ennemyKnightAround > 2) {
      doDefensiveMove();
    }
  }

  private static boolean doGiantInitiative() {
    // check if we need to do a giant barracks & build giant
    long ennemyTowers = allTowers.stream()
                          .filter(t -> {return t.owner != 0; })
                          .count();
    if (ennemyTowers > 100) { // TODO yolo
      Site site = getSiteByClosestDistance(me).stream()
          .filter(s -> { return s.isTower(); } )
          .findFirst().orElse(null);
      if (site != null) {
        if (site.isGiant()) {
          // TODO hmmmm now we need to train a giant ....
          System.err.println("ARGGG need to train a giant now in priority !");
        } else {
          return me.moveTo(site).then(site::buildGiant).end();
        }
      }
      
    } 
    return false;
  }

  private static boolean buildSomeInitialMines(Site closestFree) {
    System.err.println("Dealing with mines ...");
    List<Mine> myMines = mines.stream()
        .filter(mine -> {return mine.owner == 0;})
        .collect(Collectors.toList());

    int minedGoldPerTurn = myMines.stream()
        .filter(mine -> mine.hasGold())
        .map( mine -> mine.incomeRate)
        .reduce(0, Integer::sum);

    // check if we are near an upgradable mine first !
    finishNearMineUpgrade();
    
    System.err.println("I'm currently mining " + minedGoldPerTurn);
    if (minedGoldPerTurn < 4) {
      System.err.println("Still got to add some mines ...");
      List<Site> sites= getSiteByClosestDistance(me).stream()
                            .filter(s -> !s.isTower())
                            .filter(s -> s.imNotOwner() || s.noBuilding())
                            .filter(s -> { return s.imNotOwner() || !s.maxMined(); })
                            .collect(Collectors.toList());
      for (Site site : sites) {
        if (site.maxMineSize == 1 && me.knightBarracks.size() == 0) {
          System.err.println("Not a good mine and need some barracks, so will build on it");
          me.moveTo(site).then(site::buildKnightBarrack).end();
        }
        
        moveToSiteAndBuildMine(site);
        System.err.println("Site " + site.id + " is not eligeable");
      }
    }
    return false;
  }

  private static boolean finishNearMineUpgrade() {
    List<Site> sList = getSiteByClosestDistance(me);
    if (sList.isEmpty()) return false;
    
    Site mineSite = sList.get(0);
    if (mineSite.isInRange(me) && mineSite.isAMine() && mineSite.imOwner()) {
      System.err.println("Finish upgrading mine "+ mineSite.id);
      if (me.moveTo(mineSite).then(mineSite::buildMine).then(mineSite::upgradeMine).end()) {
        return true;
      }
    }
    return false;
  }

  private static List<Site> getSiteByClosestDistance(Queen queen) {
    return sitesRenamed.stream()
        .sorted((s1, s2) -> Double.compare(s1.pos.dist(queen.pos), s2.pos.dist(queen.pos)))
        .collect(Collectors.toList());
  }

  private static boolean moveToSiteAndBuildMine(Site site) {
    if (site.isAMine() && site.maxMined()) {
      return false; 
    }
    if (getEnnemyKnightAround(60) > 0) {
      System.err.println("Won't build a mine with ennemy around ...");
      return false;
    }
    if (site.gold == 0) {
      System.err.println("No more gold in this site");
      return false;
    }
    
    System.err.println("Move and build to site "+ site.id);
    return me.moveTo(site).then(site::buildMine).then(site::upgradeMine).end();
  }

  private static boolean doOtherMoves(Site closestFree) {
    buildNewBarracks(closestFree);
    
    List<Site> closestAvailableSites = getSiteByClosestDistance(me).stream()
        .filter( s -> {return s.imNotOwner() && !s.isTower();})
        .collect(Collectors.toList());

    buildNewTowers(closestAvailableSites);
    buildNewMineOrBarracks(closestAvailableSites);
    
    
    //TODO Here, THERE IS A LOT TO DO
    //  - if we are endanger, we may goback near tower and (re)power them
    //  - we may want to build outpost to get nearest the ennemy (faster waves of creeps)
    //  - we may want to build new towers to increase protection
    
    upgradeClosestTower();
    return false;
  }

  private static void upgradeClosestTower() {
    List<Site> closestTowers = getSiteByClosestDistance(me).stream()
        .filter(s -> { return s.imOwner() && s.isTower(); })
        .collect(Collectors.toList());
    
    if (!closestTowers.isEmpty()) {
      System.err.println("Move back to tower and upgrade it");
      Site towerSite = closestTowers.get(0);
      me.action(towerSite::moveTo)
               .then(towerSite::buildTower)
               .then(towerSite::upgradeTower)
               .end();
    } else {
      System.err.println("No Tower to move back");
    }
  }

  private static void buildNewMineOrBarracks(List<Site> closestAvailableSites) {
    if (!closestAvailableSites.isEmpty()) {
      Site site = closestAvailableSites.get(0);
      System.err.println("Try to build mine");
      moveToSiteAndBuildMine(site);
      System.err.println("Try to build barracks");
      me.moveTo(site).then(site::buildKnightBarrack).end();
    }
  }

  private static void buildNewTowers(List<Site> closestAvailableSites) {
    if (!closestAvailableSites.isEmpty() && !towerCoverMap(me)) {
      Site site = closestAvailableSites.get(0);
      System.err.println("Try to build tower");
      me.moveTo(site).then(site::buildTower).then(site::upgradeTower).end();
    }
  }

  private static void buildNewBarracks(Site closestFree) {
    long myBarracksCount = barracks.stream()
        .filter(Structure::isMine)
        .count();

    if (myBarracksCount == 0) {
      System.err.println("Building first barracks ....");
      me.moveTo(closestFree).then(closestFree::buildKnightBarrack).end();
    }
  }

  /**
   * Returns whether our towers build a WALL between opponent and ourself 
   */
  private static boolean towerCoverMap(Queen me) {
    // TODO really check map coverage vs creeps barracks ...
    long myTowers = allTowers.stream()
        .filter(u -> u.owner == me.owner)
        .count();
    return myTowers > 3;
  }

  private static boolean doDefensiveMove() {
    System.err.println("Defensive mode activated ... ");
    // defensive move, go back to tower !
    List<Site> closestTowers = getSiteByClosestDistance(me).stream()
        .filter(s -> { return s.imOwner() && s.isTower(); })
        .collect(Collectors.toList());
    
    int protection = 0;
    for (Site towerSite : closestTowers) {
      Tower tower = towerSite.getTower();
      if (tower.protects(me, 0)) {
        protection++;
      } else {
      }
    }
    if (protection >= 4 ) {
      System.err.println("current tower protection " + protection + " not going anywhere");
      return false;
    }
    
    if (!closestTowers.isEmpty()) {
      Site closestTower = closestTowers.get(0);
      Tower t = (Tower)closestTower.structure;
      if (!t.protects(me, 0)) {
        System.err.println("Move to closest tower because it doesnt protect us! ");
        return closestTower.moveTo();
      } else {
        // check if there is a second tower in viccinity to protects us more
        if (tryToMoveBetweenSecondClosestTower(closestTowers, closestTower)) {
          return true;
        }
        if (tryToBuildASecondTowerInProtectionRange(closestTower)) {
          return true;
        } else if (increaseTowerRange(closestTower)) {
          return true;
        }
      }
    } else {
      System.err.println("Will build a  tower near to defend me");
      List<Site> closestToMe = getSiteByClosestDistance(me);
      Site closestSite = closestToMe.get(0);
      return me.moveTo(closestSite).then(closestSite::buildTower).end();
    }
    return false; // TODO remove this and check all paths !
  }

  private static boolean increaseTowerRange(Site closestTower) {
    System.err.println("Increasing tower range ... ");
    return closestTower.buildTower();
  }

  private static boolean tryToBuildASecondTowerInProtectionRange(Site closestTower) {
    Tower t = (Tower)closestTower.structure;
    
    // site qui ne sont pas closestTower, mais qui sont dans son range
    Site site = sitesRenamed.stream()
        // not the current protecting tower 
      .filter(s -> { return s != closestTower;})
        // but protection range
      .filter(s -> { return s.pos.dist(closestTower.pos) < t.attackRadius + s.radius; } )
      .filter(s -> (s.imNotOwner() && !s.isTower()) || s.noBuilding())
      .sorted(me::closest)
      .findFirst().orElse(null);
    
    if (site != null) {
      System.err.println("Will move & build on seconde site " + site.id);
      return me.moveTo(site).then(site::buildTower).end();
    }
      
    return false;
  }

  private static boolean tryToMoveBetweenSecondClosestTower(List<Site> closestTowers, Site closestTower) {
    Tower t = (Tower)closestTower.structure;
    if (closestTowers.size() >=2) {
      Site secondTowerSite = closestTowers.get(1);
      Tower t2 = (Tower)secondTowerSite.structure;
      if (t2.protects(me, 0)) {
        // it already protects me, no need to go there
        return false; 
      }
      if (secondTowerSite.pos.dist(closestTower.pos) < t.attackRadius + t2.attackRadius) { 
        // TODO move to the barycenter ???
        System.err.println("Move to second closest tower because we can be protected by both ! ");
        return secondTowerSite.moveTo();
      }
    } 
    return false;
  }

  private static int getEnnemyKnightAround(int range) {
    return (int)units.stream()
        .filter(unit -> unit.owner == 1)
        .filter(unit -> unit.isKnight())
        .filter(unit -> unit.pos.dist(me.pos) < range)
        .count();
  }

  private static Site getSite(int siteId) {
    List<Site> sList = sitesRenamed.stream().filter(s -> s.id == siteId).collect(Collectors.toList());
    if (sList.isEmpty()) {
      System.out.println("Cant find id " + siteId +" !!!!!!!");
      return null;
    } else {
      return sList.get(0);
    }
  }
}
