package stc2;

import stc.P;

public class BitBoard {
  private static final int TOP_ROW_MASK = 0b100000000000;
  private static final int TOP_2_ROWS_MASK = 0b110000000000;

  public static final int SKULL_LAYER = 0;
  public static final int BLUE_LAYER = 1;
  public static final int GREEN_LAYER = 2;
  public static final int PINK_LAYER = 3;
  public static final int RED_LAYER = 4;
  public static final int YELLOW_LAYER = 5;
  public static final int COMPLETE_LAYER_MASK = 6;
  private static final char[] cellTable = { '☠', '1', '2', '3', '4', '5' };

  BitLayer layers[] = new BitLayer[7]; 
  {
    for (int i=0;i<=6;i++) {
      layers[i] = new BitLayer();
    }
  }
  
  public void copyFrom(BitBoard bboard) {
    for (int i=0;i<=6;i++) {
      layers[i].copyFrom(bboard.layers[i]);
    }
  }
  
  public void updateRow(int y, String row) {
    for (int x = 0; x < 6; x++) {
      char value = row.charAt(x);
      if (isColor(value)) {
        int layer = value-'0';
        layers[layer].setCell(x,y);
      } else if (isSkull(value)) {
        layers[0].setCell(x,y);
      } else if (isEmpty(value)) { 
        // nothing
      }
    }
  }

  public void buildCompleteLayerMask() {
    layers[COMPLETE_LAYER_MASK].reset();
    for (int i=0;i<6;i++) {
      layers[COMPLETE_LAYER_MASK].merge(layers[i]);
    }
  }
  
  private boolean isEmpty(char value) {
    return value == '.';
  }

  private boolean isSkull(char value) {
    return value == '0' || value == '@' || value == '☠';
  }

  private boolean isColor(char value) {
    return value >= '1' && value <= '5';
  }

  
  
  
  public String getDebugString() {
    char[] output = new char[7*12];
    for (int y=0;y<12;y++) {
      output[6+7*y] = '\n';
    }
    for (int x = 0; x < 6; x++) {
      for (int y = 0; y < 12; y++) {
        output[x + 7 * y] = '.';
        for (int l = 0; l < 6; l++) {
          BitLayer layer = layers[l];
          if (layer.isCellSetAt(x, y)) {
            output[x + 7 * y] = cellTable[l];
          }
        }
      }
    }
    return new String(output);
  }

  P pushBall(int color, int column) {
    int y;
    y = layers[COMPLETE_LAYER_MASK].pushFromTopOfColumn(column);
    layers[color].setCell(column, y);
    
    return P.get(column, y);
  }

  public boolean canPutBalls(int rotation, int baseColumn) {
    BitLayer allLayers = layers[COMPLETE_LAYER_MASK];
    
    if ((baseColumn == 0 && rotation == 2) || (baseColumn == 5 && rotation == 0)) {
      return false;
    }
    
    switch (rotation) {
    case 0:
      return (allLayers.getCol(baseColumn) & TOP_ROW_MASK) == 0
          && (allLayers.getCol(baseColumn+1) & TOP_ROW_MASK) == 0;
    case 1:
    case 3:
      return (allLayers.getCol(baseColumn) & TOP_2_ROWS_MASK) == 0;
    case 2:
      return (allLayers.getCol(baseColumn) & TOP_ROW_MASK) == 0
      && (allLayers.getCol(baseColumn-1) & TOP_ROW_MASK) == 0;
    }
    return false;
  }

  public void update() {
    buildCompleteLayerMask();
    int mask;
    int mvs[] = new int[5];
    
    for (int col =0;col<6;col++) {
      mask = layers[COMPLETE_LAYER_MASK].getCol(col);
      BitLayer.generateMvs(mask, mvs);
      for (int l=0;l<7;l++) {
        layers[l].setCol(col, BitLayer.compress(layers[l].getCol(col), mask, mvs));
      }
    }
  }

}
