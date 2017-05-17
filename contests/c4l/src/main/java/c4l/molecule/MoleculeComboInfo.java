package c4l.molecule;

import java.util.ArrayList;
import java.util.List;

import c4l.entities.MoleculeType;

  /** in order, which molecules needed for which sample*/
public class MoleculeComboInfo {
  public List<MoleculeInfo> infos = new ArrayList<>();
  public double score;
  
  @Override
  public String toString() {
    String output = "s("+score+") \n\r";
    for (MoleculeInfo info : infos) {
      output += info.toString();
    }
    return output;
  }

  public boolean canFinishAtLeastOneSample() {
    return !infos.isEmpty();
  }
  public int neededMoleculeToRealiseCombo() {
    return infos.stream().mapToInt(info -> info.getNeededMolecules().size()).sum();
  }

  public int scoreRealizedWithoutMolecule() {
    return infos.stream().mapToInt(info -> info.getNeededMolecules().size() == 0 ? info.health : 0).sum();
  }

  public int totalMoleculeTaken() {
    return infos.stream().mapToInt(info -> info.getNeededMolecules().size() ).sum();
  }

  public List<MoleculeType> getNeededMolecules() {
    List<MoleculeType> all = new ArrayList<>();
    for (MoleculeInfo info : infos) {
      all.addAll(info.getNeededMolecules());
    }
    return all;
  }
  
}
