package c4l.molecule;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import c4l.entities.MoleculeType;

public class MoleculeOptimizerNodeTest {

  MoleculeOptimizerNode root;
  int index = 0;
  
  @Before
  public void setup() {
    root = new MoleculeOptimizerNode();
    for (int i=0;i<root.values.length;i++) {
      root.values[i] = 99;
    }
    index = 0;
  }

  @Test
  public void createStorage_empty() throws Exception {
    root.createStorage(new int[] { 0,0,0,0,0});
    assertThat(root.freeStorage, is (10));
  }
  
  @Test
  public void createStorage_full() throws Exception {
    root.createStorage(new int[] { 3,0, 4, 0, 3});
    assertThat(root.freeStorage, is (0));
  }
  
  
  @Test
  public void depth0_easyChoice() throws Exception {
    createSample(0, new int[]{0, 0, 1, 0, 0}, 30);
    
    createSample(1, new int[]{2, 0, 0, 2, 3}, 10);
    createSample(2, new int[]{0, 3, 0, 2, 3}, 10);
    
    createStorage(new int[]{0, 0, 0, 0, 0});
    createExpertise(new int[]{0, 0, 0, 0, 0});
    createAvailable(new int[]{0, 0, 1, 0, 0});
    
    root.freeStorage = 1;
    
    root.start();
    
    assertThat(root.score, is(closeTo(30.0, 1.0)));// take into account getPercentage
  }

  @Test
  public void depth0_chooseBest_sameCombination() throws Exception {
    createSample(0, new int[]{0, 0, 1, 0, 0}, 30);
    createSample(1, new int[]{0, 0, 1, 0, 0}, 40);
    createSample(2, new int[]{0, 3, 0, 2, 3}, 10);
    
    createStorage(new int[]{0, 0, 0, 0, 0});
    createExpertise(new int[]{0, 0, 0, 0, 0});
    createAvailable(new int[]{0, 0, 1, 0, 0});
    
    root.freeStorage = 1;
    
    root.start();
    
    assertThat(root.score, is(closeTo(40.0, 1.0)));// take into account getPercentage
  }
  
  @Test
  public void depth0_chooseBest_differentCombination() throws Exception {
    createSample(0, new int[]{0, 0, 1, 0, 0}, 30);
    createSample(1, new int[]{0, 0, 0, 1, 0}, 40);
    createSample(2, new int[]{0, 3, 0, 2, 3}, 10);
    
    createStorage(new int[]{0, 0, 0, 0, 0});
    createExpertise(new int[]{0, 0, 0, 0, 0});
    createAvailable(new int[]{0, 0, 1, 1, 0});
    
    root.freeStorage = 1;
    
    root.start();
    
    assertThat(root.score, is(closeTo(40.0, 1.0))); // take into account getPercentage
  }

  @Test
  public void searchAllMolecules() throws Exception {
    createSample(0, new int[]{0,0,5,0,0},20);
    createSample(4, new int[]{0,0,0,0,5},20);
    createSample(2, new int[]{0,5,3,0,0},20);
    createStorage(  new int[]{0, 0, 0, 0, 0});
    createExpertise(new int[]{0, 0, 0, 0, 0});
    createAvailable(new int[]{6, 6, 6, 6, 6});
    root.freeStorage = 6;
    root.start();
    
    assertThat(root.children.size(), is(5));
  }
  
  @Test
  public void getTheA() throws Exception {
    createSample(0, new int[]{0, 2, 0, 0, 2},1);
    createSample(2, new int[]{4, 0, 0, 0, 0},10);
    createStorage(  new int[]{0, 0, 0, 0, 3});
    createExpertise(new int[]{0, 0, 0, 0, 1});
    createAvailable(new int[]{0, 2, 6, 6, 3});
    
    root.freeStorage = 6;
    root.start();
    
    assertThat(root.getBestChild().pickedMolecule, is(MoleculeType.B));
  }
  
  @Test
  public void noMoleculeNeeded() throws Exception {
    createSample(7, new int[]{6, 0, 0, 0, 0},30);
    createSample(16, new int[]{2, 3, 0, 0, 2},10);
    createSample(17, new int[]{3, 2, 2, 0, 0},10);
    createStorage(  new int[]{4, 0, 0, 2, 1});
    createExpertise(new int[]{2, 0, 0, 2, 1});
    createAvailable(new int[]{0, 3, 4, 3, 3});
    
    root.start();
    
    assertThat(root.score, closeTo(30.0, 1.0)); // take percentage complete into account
  }

  @Test
  @Ignore
  public void TestDePerf() throws Exception {
    createSample(2, new int[]{0,6,0,0,0},30);
    createSample(0, new int[]{0,0,0,5,0},20);
    createSample(4, new int[]{1,4,2,0,0},20);
    createStorage(new int[]{0, 0, 0, 0, 0});
    createExpertise(new int[]{0, 0, 0, 0, 0});
    createAvailable(new int[]{6, 6, 6, 6, 6});
    root.start();
    
    assertThat(root.score, is(not(0.0)));
  }

  @Test
  public void percentCompletionWhenAllComplete() throws Exception {
    createSample(2, new int[]{0,1,0,0,0},30);
    createSample(0, new int[]{0,0,1,0,0},20);
    createSample(4, new int[]{0,0,0,1,0},20);
    
    createStorage(  new int[]{0, 1, 0, 1, 0});
    createExpertise(new int[]{0, 0, 1, 0, 0});
    createAvailable(new int[]{6, 6, 6, 6, 6});
    
    assertThat(root.getPercentCompletion(), is(1.0));
  }
  
  @Test
  public void percentCompletionWhenNoneComplete() throws Exception {
    createSample(2, new int[]{0,2,0,0,0},30);
    createSample(0, new int[]{0,0,2,0,0},20);
    createSample(4, new int[]{0,0,0,2,0},20);
    
    createStorage(  new int[]{4, 0, 0, 0, 0});
    createExpertise(new int[]{0, 0, 0, 0, 2});
    createAvailable(new int[]{6, 6, 6, 6, 6});
    
    assertThat(root.getPercentCompletion(), is(0.0));
  }

  private void createStorage(int[] storage) {
    root.createStorage(storage);
  }
  
  private void createExpertise(int[] xp) {
    root.createExpertise(xp);
  }

  private void createAvailable(int[] available) {
    root.createAvailable(available);
  }

  private void createSample(int id, int[] costs, int health) {
    root.createSample(index, costs, health);
    index++;
  }
  
}
