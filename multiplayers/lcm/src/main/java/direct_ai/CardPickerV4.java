<<<<<<< Updated upstream
package direct_ai;

import lcm.Agent;
import lcm.PlayerOld;
import lcm.cards.Card;
import lcm.cards.CardTriplet;

/**
 * use a gaussian for the mana curve
 * 
 * @author nmahoude
 *
 */
public class CardPickerV4 implements Picker {
 
  static int calculatedCardValues[] = new int[] {
      // closet v1
      0, 123,72,143,101,139,138,168,137,118,26,114,82,83,33,93,55,94,154,108,32,92,57,103,53,86,132,85,149,158,84,45,152,116,51,43,36,111,128,121,40,58,25,41,146,30,47,119,169,164,150,167,144,162,153,16,49,28,52,88,38,75,68,54,145,163,125,147,166,159,98,78,61,74,48,115,35,69,37,70,156,79,127,130,157,135,91,110,122,65,56,131,15,124,97,141,134,107,80,136,62,46,31,126,76,117,102,23,44,109,12,96,67,20,120,89,160,24,99,100,66,87,64,42,22,71,90,60,77,113,34,29,19,129,63,105,73,106,17,161,14,140,21,18,151,95,50,133,155,27,142,165,104,10,39,112,13,59,148,81,11,
      // cloest v2
      //      0, 117,72,139,101,140,138,165,134,119,26,120,82,83,33,93,55,94,155,108,32,92,57,103,53,86,132,85,147,159,84,45,152,121,51,43,36,116,124,115,40,58,25,41,146,30,47,113,168,164,150,162,144,161,154,16,49,28,52,88,38,75,68,54,145,163,128,151,169,157,98,78,61,74,48,112,35,69,37,70,158,79,129,130,156,135,91,111,125,65,56,127,15,122,97,142,133,107,80,136,62,46,31,126,76,118,102,23,44,110,12,96,67,20,123,89,166,24,99,100,66,87,64,42,22,71,90,60,77,114,34,29,19,131,63,105,73,106,17,160,14,141,21,18,148,95,50,137,153,27,143,167,104,10,39,109,13,59,149,81,11,
  
      // my pick
      // 0, 255,124,280,104,-2,-11,1227,214,248,-333,141,239,116,-423,261,-252,257,552,400,-451,153,43,277,-210,28,162,24,88,150,23,-287,24,-12,-66,-313,-199,124,314,-121,27,82,-423,-325,482,-165,-332,81,486,574,458,838,349,552,390,189,-119,-387,-188,88,-380,-77,-211,480,501,1131,641,480,833,465,-35,-231,54,88,-119,173,-307,-160,-424,-127,191,-72,104,173,886,250,86,222,399,-167,-323,228,-444,157,440,384,78,78,273,391,450,-176,-226,339,-95,98,-43,-641,-230,341,-170,-205,-309,-691,-171,-149,405,-391,-221,-182,62,-251,-278,-293,-322,-304,-388,-622,-257,-337,-771,-618,-527,-161,-291,-654,190,-389,-405,40,-271,308,-302,-540,302,-283,-365,42,217,-405,321,554,10,-725,-373,117,-699,-233,96,-204,-638,

  };

  public int pick(Agent agent, CardTriplet triplet) {
    int index = 0;
    int bestScore = calculatedCardValues[triplet.cards[0].instanceId];
    for (int i=0;i<3;i++) {
      int score = calculatedCardValues[triplet.cards[i].instanceId];
      if (score > bestScore) {
        bestScore = score;
        index = i;
      }
    }
    return index;
  }
}
=======
package direct_ai;

import lcm.Agent;
import lcm.PlayerOld;
import lcm.cards.Card;
import lcm.cards.CardTriplet;

/**
 * use a gaussian for the mana curve
 * 
 * @author nmahoude
 *
 */
public class CardPickerV4 implements Picker {
 
  static int calculatedCardValues[] = new int[] {
      // closet v1
      0, 123,72,143,101,139,138,168,137,118,26,114,82,83,33,93,55,94,154,108,32,92,57,103,53,86,132,85,149,158,84,45,152,116,51,43,36,111,128,121,40,58,25,41,146,30,47,119,169,164,150,167,144,162,153,16,49,28,52,88,38,75,68,54,145,163,125,147,166,159,98,78,61,74,48,115,35,69,37,70,156,79,127,130,157,135,91,110,122,65,56,131,15,124,97,141,134,107,80,136,62,46,31,126,76,117,102,23,44,109,12,96,67,20,120,89,160,24,99,100,66,87,64,42,22,71,90,60,77,113,34,29,19,129,63,105,73,106,17,161,14,140,21,18,151,95,50,133,155,27,142,165,104,10,39,112,13,59,148,81,11,
      // cloest v2
      //      0, 117,72,139,101,140,138,165,134,119,26,120,82,83,33,93,55,94,155,108,32,92,57,103,53,86,132,85,147,159,84,45,152,121,51,43,36,116,124,115,40,58,25,41,146,30,47,113,168,164,150,162,144,161,154,16,49,28,52,88,38,75,68,54,145,163,128,151,169,157,98,78,61,74,48,112,35,69,37,70,158,79,129,130,156,135,91,111,125,65,56,127,15,122,97,142,133,107,80,136,62,46,31,126,76,118,102,23,44,110,12,96,67,20,123,89,166,24,99,100,66,87,64,42,22,71,90,60,77,114,34,29,19,131,63,105,73,106,17,160,14,141,21,18,148,95,50,137,153,27,143,167,104,10,39,109,13,59,149,81,11,
  
      // my pick
      // 0, 255,124,280,104,-2,-11,1227,214,248,-333,141,239,116,-423,261,-252,257,552,400,-451,153,43,277,-210,28,162,24,88,150,23,-287,24,-12,-66,-313,-199,124,314,-121,27,82,-423,-325,482,-165,-332,81,486,574,458,838,349,552,390,189,-119,-387,-188,88,-380,-77,-211,480,501,1131,641,480,833,465,-35,-231,54,88,-119,173,-307,-160,-424,-127,191,-72,104,173,886,250,86,222,399,-167,-323,228,-444,157,440,384,78,78,273,391,450,-176,-226,339,-95,98,-43,-641,-230,341,-170,-205,-309,-691,-171,-149,405,-391,-221,-182,62,-251,-278,-293,-322,-304,-388,-622,-257,-337,-771,-618,-527,-161,-291,-654,190,-389,-405,40,-271,308,-302,-540,302,-283,-365,42,217,-405,321,554,10,-725,-373,117,-699,-233,96,-204,-638,

  };

  public int pick(Agent agent, CardTriplet triplet) {
    int index = 0;
    int bestScore = calculatedCardValues[triplet.cards[0].instanceId];
    for (int i=0;i<3;i++) {
      int score = calculatedCardValues[triplet.cards[i].instanceId];
      if (score > bestScore) {
        bestScore = score;
        index = i;
      }
    }
    return index;
  }
}
>>>>>>> Stashed changes
