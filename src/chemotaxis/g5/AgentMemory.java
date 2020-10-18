package chemotaxis.g5;

import java.util.Map;

import chemotaxis.sim.DirectionType;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.Move;
import chemotaxis.sim.SimPrinter;

public class AgentMemory{

    /*
     * Bit flags:
     *
     * 0: Last Move:    0 = North   1 = South
     * 1: Last Move:    0 = East    1 = West
     * 2: Color Type:  [0      [1      [0       [1
     * 3: Color Type:    0] Red 0] Blue 1] Green 1] Any
     *
     *
     *
     *
     *
     */
    
    public static ChemicalType getColor(Byte previousState, Byte currentState){

        if(bitAt(previousState, 2) == 0) {
            if (bitAt(previousState, 3) == 0) {
                flipBit(currentState, 2);
                return ChemicalType.RED;
            }
            else {
                flipBit(currentState, 3);
                return ChemicalType.GREEN;
            }
        }
        flipBit(currentState, 2);
        flipBit(currentState, 3);
        return ChemicalType.BLUE;
    }
    
    public static byte bitAt(Byte currentState, int pos){
        return (byte) (currentState.byteValue() >> pos & 1);
    }

    public static void flipBit(Byte currentState, int bit){
        currentState = (byte) (currentState.byteValue() ^ (1 << bit));
    }

    public static void dumpMemory(Byte currentState) {
        currentState = 0;
    }

    public static void hitPeak(Byte currentState){
        currentState = (byte) (currentState.byteValue() | (1 << 8));
    }

    public static void hitValley(Byte currentState){
        currentState = (byte) (currentState.byteValue() & ~(1 << 8));
    }

    public static boolean goingUp(Byte currentState){
        return currentState > 0;
    }
}