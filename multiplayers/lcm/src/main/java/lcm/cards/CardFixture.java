package lcm.cards;

import java.util.Scanner;

import lcm.PlayerOld;
import lcm.ai.eval.Eval8;
import lcm.cards.Card;
import lcm.cards.Location;

public class CardFixture {
  static String NL = "\n\r";
  private static Card[] cards = new Card[512];

  static {
    PlayerOld.DEBUG_INPUT = false;
    String input = "" +
        "1   -1 0 0 1 2 1 -      1 0 0" + NL +
        "2   -1 0 0 1 1 2 -      0 -1 0" + NL +
        "3   -1 0 0 1 2 2 -      0 0 0" + NL +
        "4   -1 0 0 2 1 5 -      0 0 0" + NL +
        "5   -1 0 0 2 4 1 -      0 0 0" + NL +
        "6   -1 0 0 2 3 2 -      0 0 0" + NL +
        "7   -1 0 0 2 2 2 W      0 0 0" + NL +
        "8   -1 0 0 2 2 3 -      0 0 0" + NL +
        "9   -1 0 0 3 3 4 -      0 0 0" + NL +
        "10  -1 0 0 3 3 1 D      0 0 0" + NL +
        "11  -1 0 0 3 5 2 -      0 0 0" + NL +
        "12  -1 0 0 3 2 5 -      0 0 0" + NL +
        "13  -1 0 0 4 5 3 -      1 -1 0" + NL +
        "14  -1 0 0 4 9 1 -      0 0 0" + NL +
        "15  -1 0 0 4 4 5 -      0 0 0" + NL +
        "16  -1 0 0 4 6 2 -      0 0 0" + NL +
        "17  -1 0 0 4 4 5 -      0 0 0" + NL +
        "18  -1 0 0 4 7 4 -      0 0 0" + NL +
        "19  -1 0 0 5 5 6 -      0 0 0" + NL +
        "20  -1 0 0 5 8 2 -      0 0 0" + NL +
        "21  -1 0 0 5 6 5 -      0 0 0" + NL +
        "22  -1 0 0 6 7 5 -      0 0 0" + NL +
        "23  -1 0 0 7 8 8 -      0 0 0" + NL +
        "24  -1 0 0 1 1 1 -      0 -1 0" + NL +
        "25  -1 0 0 2 3 1 -      -2 -2 0" + NL +
        "26  -1 0 0 2 3 2 -      0 -1 0" + NL +
        "27  -1 0 0 2 2 2 -      2 0 0" + NL +
        "28  -1 0 0 2 1 2 -      0 0 1" + NL +
        "29  -1 0 0 2 2 1 -      0 0 1" + NL +
        "30  -1 0 0 3 4 2 -      0 -2 0" + NL +
        "31  -1 0 0 3 3 1 -      0 -1 0" + NL +
        "32  -1 0 0 3 3 2 -      0 0 1" + NL +
        "33  -1 0 0 4 4 3 -      0 0 1" + NL +
        "34  -1 0 0 5 3 5 -      0 0 1" + NL +
        "35  -1 0 0 6 5 2 B      0 0 1" + NL +
        "36  -1 0 0 6 4 4 -      0 0 2" + NL +
        "37  -1 0 0 6 5 7 -      0 0 1" + NL +
        "38  -1 0 0 1 1 3 D      0 0 0" + NL +
        "39  -1 0 0 1 2 1 D      0 0 0" + NL +
        "40  -1 0 0 3 2 3 GD     0 0 0" + NL +
        "41  -1 0 0 3 2 2 CD     0 0 0" + NL +
        "42  -1 0 0 4 4 2 D      0 0 0" + NL +
        "43  -1 0 0 6 5 5 D      0 0 0" + NL +
        "44  -1 0 0 6 3 7 DL     0 0 0" + NL +
        "45  -1 0 0 6 6 5 BD     -3 0 0" + NL +
        "46  -1 0 0 9 7 7 D      0 0 0" + NL +
        "47  -1 0 0 2 1 5 D      0 0 0" + NL +
        "48  -1 0 0 1 1 1 L      0 0 0" + NL +
        "49  -1 0 0 2 1 2 GL     0 0 0" + NL +
        "50  -1 0 0 3 3 2 L      0 0 0" + NL +
        "51  -1 0 0 4 3 5 L      0 0 0" + NL +
        "52  -1 0 0 4 2 4 L      0 0 0" + NL +
        "53  -1 0 0 4 1 1 CL     0 0 0" + NL +
        "54  -1 0 0 3 2 2 L      0 0 0" + NL +
        "55  -1 0 0 2 0 5 G      0 0 0" + NL +
        "56  -1 0 0 4 2 7 -      0 0 0" + NL +
        "57  -1 0 0 4 1 8 -      0 0 0" + NL +
        "58  -1 0 0 6 5 6 B      0 0 0" + NL +
        "59  -1 0 0 7 7 7 -      1 -1 0" + NL +
        "60  -1 0 0 7 4 8 -      0 0 0" + NL +
        "61  -1 0 0 9 10 10 -      0 0 0" + NL +
        "62  -1 0 0 12 12 12 BG     0 0 0" + NL +
        "63  -1 0 0 2 0 4 GW     0 0 0" + NL +
        "64  -1 0 0 2 1 1 GW     0 0 0" + NL +
        "65  -1 0 0 2 2 2 W      0 0 0" + NL +
        "66  -1 0 0 5 5 1 W      0 0 0" + NL +
        "67  -1 0 0 6 5 5 W      0 -2 0" + NL +
        "68  -1 0 0 6 7 5 W      0 0 0" + NL +
        "69  -1 0 0 3 4 4 B      0 0 0" + NL +
        "70  -1 0 0 4 6 3 B      0 0 0" + NL +
        "71  -1 0 0 4 3 2 BC     0 0 0" + NL +
        "72  -1 0 0 4 5 3 B      0 0 0" + NL +
        "73  -1 0 0 4 4 4 B      4 0 0" + NL +
        "74  -1 0 0 5 5 4 BG     0 0 0" + NL +
        "75  -1 0 0 5 6 5 B      0 0 0" + NL +
        "76  -1 0 0 6 5 5 BD     0 0 0" + NL +
        "77  -1 0 0 7 7 7 B      0 0 0" + NL +
        "78  -1 0 0 8 5 5 B      0 -5 0" + NL +
        "79  -1 0 0 8 8 8 B      0 0 0" + NL +
        "80  -1 0 0 8 8 8 BG     0 0 1" + NL +
        "81  -1 0 0 9 6 6 BC     0 0 0" + NL +
        "82  -1 0 0 7 5 5 BDW    0 0 0" + NL +
        "83  -1 0 0 0 1 1 C      0 0 0" + NL +
        "84  -1 0 0 2 1 1 CDW    0 0 0" + NL +
        "85  -1 0 0 3 2 3 C      0 0 0" + NL +
        "86  -1 0 0 3 1 5 C      0 0 0" + NL +
        "87  -1 0 0 4 2 5 CG     0 0 0" + NL +
        "88  -1 0 0 5 4 4 C      0 0 0" + NL +
        "89  -1 0 0 5 4 1 C      2 0 0" + NL +
        "90  -1 0 0 8 5 5 C      0 0 0" + NL +
        "91  -1 0 0 0 1 2 G      0 1 0" + NL +
        "92  -1 0 0 1 0 1 G      2 0 0" + NL +
        "93  -1 0 0 1 2 1 G      0 0 0" + NL +
        "94  -1 0 0 2 1 4 G      0 0 0" + NL +
        "95  -1 0 0 2 2 3 G      0 0 0" + NL +
        "96  -1 0 0 2 3 2 G      0 0 0" + NL +
        "97  -1 0 0 3 3 3 G      0 0 0" + NL +
        "98  -1 0 0 3 2 4 G      0 0 0" + NL +
        "99  -1 0 0 3 2 5 G      0 0 0" + NL +
        "100 -1 0 0 3 1 6 G      0 0 0" + NL +
        "101 -1 0 0 4 3 4 G      0 0 0" + NL +
        "102 -1 0 0 4 3 3 G      0 -1 0" + NL +
        "103 -1 0 0 4 3 6 G      0 0 0" + NL +
        "104 -1 0 0 4 4 4 G      0 0 0" + NL +
        "105 -1 0 0 5 4 6 G      0 0 0" + NL +
        "106 -1 0 0 5 5 5 G      0 0 0" + NL +
        "107 -1 0 0 5 3 3 G      3 0 0" + NL +
        "108 -1 0 0 5 2 6 G      0 0 0" + NL +
        "109 -1 0 0 5 5 6 -      0 0 0" + NL +
        "110 -1 0 0 5 0 9 G      0 0 0" + NL +
        "111 -1 0 0 6 6 6 G      0 0 0" + NL +
        "112 -1 0 0 6 4 7 G      0 0 0" + NL +
        "113 -1 0 0 6 2 4 G      4 0 0" + NL +
        "114 -1 0 0 7 7 7 G      0 0 0" + NL +
        "115 -1 0 0 8 5 5 GW     0 0 0" + NL +
        "116 -1 0 0 12 8 8 BCGDLW 0 0 0" + NL +
        "117 -1 0 1 1 1 1 B      0 0 0" + NL +
        "118 -1 0 1 0 0 3 -      0 0 0" + NL +
        "119 -1 0 1 1 1 2 -      0 0 0" + NL +
        "120 -1 0 1 2 1 0 L      0 0 0" + NL +
        "121 -1 0 1 2 0 3 -      0 0 1" + NL +
        "122 -1 0 1 2 1 3 G      0 0 0" + NL +
        "123 -1 0 1 2 4 0 -      0 0 0" + NL +
        "124 -1 0 1 3 2 1 D      0 0 0" + NL +
        "125 -1 0 1 3 1 4 -      0 0 0" + NL +
        "126 -1 0 1 3 2 3 -      0 0 0" + NL +
        "127 -1 0 1 3 0 6 -      0 0 0" + NL +
        "128 -1 0 1 4 4 3 -      0 0 0" + NL +
        "129 -1 0 1 4 2 5 -      0 0 0" + NL +
        "130 -1 0 1 4 0 6 -      4 0 0" + NL +
        "131 -1 0 1 4 4 1 -      0 0 0" + NL +
        "132 -1 0 1 5 3 3 B      0 0 0" + NL +
        "133 -1 0 1 5 4 0 W      0 0 0" + NL +
        "134 -1 0 1 4 2 2 -      0 0 1" + NL +
        "135 -1 0 1 6 5 5 -      0 0 0" + NL +
        "136 -1 0 1 0 1 1 -      0 0 0" + NL +
        "137 -1 0 1 2 0 0 W      0 0 0" + NL +
        "138 -1 0 1 2 0 0 G      0 0 1" + NL +
        "139 -1 0 1 4 0 0 LW     0 0 0" + NL +
        "140 -1 0 1 2 0 0 C      0 0 0" + NL +
        "141 -1 0 2 0 -1 -1 -      0 0 0" + NL +
        "142 -1 0 2 0 0 0 BCGDLW 0 0 0" + NL +
        "143 -1 0 2 0 0 0 G      0 0 0" + NL +
        "144 -1 0 2 1 0 -2 -      0 0 0" + NL +
        "145 -1 0 2 3 -2 -2 -      0 0 0" + NL +
        "146 -1 0 2 4 -2 -2 -      0 -2 0" + NL +
        "147 -1 0 2 2 0 -1 -      0 0 1" + NL +
        "148 -1 0 2 2 0 -2 BCGDLW 0 0 0" + NL +
        "149 -1 0 2 3 0 0 BCGDLW 0 0 1" + NL +
        "150 -1 0 2 2 0 -3 -      0 0 0" + NL +
        "151 -1 0 2 5 0 -99 BCGDLW 0 0 0" + NL +
        "152 -1 0 2 7 0 -7 -      0 0 1" + NL +
        "153 -1 0 3 2 0 0 -      5 0 0" + NL +
        "154 -1 0 3 2 0 0 -      0 -2 1" + NL +
        "155 -1 0 3 3 0 -3 -      0 -1 0" + NL +
        "156 -1 0 3 3 0 0 -      3 -3 0" + NL +
        "157 -1 0 3 3 0 -1 -      1 0 1" + NL +
        "158 -1 0 3 3 0 -4 -      0 0 0" + NL +
        "159 -1 0 3 4 0 -3 -      3 0 0" + NL +
        "160 -1 0 3 2 0 0 -      2 -2 0" + NL +
        "";

    try {
      Scanner in = new Scanner(input);
      while (true) {
        Card card = new Card();
        card.read(in);
        cards[card.instanceId] = card;
      }
    } catch (Exception end) {
    }

  }

  public static Card card(int instanceId, int id) {
    Card c = new Card();
    c.copyFrom(cards[instanceId]);
    c.location = Location.MY_HAND;
    c.id = id;
    return c;
  }
  
  public static void main(String[] args) {
    Eval8 eval = new Eval8();
    for (int i=1;i<=160;i++) {
      Card card = card(i,-1);
      double score = eval.evaluateCard(card);
      System.err.println(""+score);
    }
  }
}
