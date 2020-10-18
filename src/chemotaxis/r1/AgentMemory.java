package chemotaxis.r1;

import java.util.Map;

import chemotaxis.sim.DirectionType;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.Move;
import chemotaxis.sim.SimPrinter;

public class AgentMemory{
	public static byte NORTH = 0;
	public static byte SOUTH = 1;
	public static byte EAST = 2;
	public static byte WEST = 3;
	public static SimPrinter simPrinter = new SimPrinter(true);
	

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

	public static void getRandomMove(Integer randomNum, Byte previousState, Move move, Map<DirectionType, ChemicalCell> neighborMap) {
		if (randomNum > 0) {
			if (randomNum % 2 == 1) {
				if (neighborMap.get(DirectionType.NORTH).isOpen() && lastMove(previousState) != SOUTH){
					simPrinter.println("NORTH 1");
					move.directionType = DirectionType.NORTH;
				}
				else if (neighborMap.get(DirectionType.SOUTH).isOpen() && lastMove(previousState) != NORTH){
					simPrinter.println("SOUTH 1");
					move.directionType = DirectionType.SOUTH;
				}
				else if (neighborMap.get(DirectionType.EAST).isOpen() && lastMove(previousState) != WEST){
					simPrinter.println("EAST 1");
					move.directionType = DirectionType.EAST;
				}
				else if (neighborMap.get(DirectionType.WEST).isOpen() && lastMove(previousState) != EAST){
					simPrinter.println("WEST 1");
					move.directionType = DirectionType.WEST;
				}
			} else {
				if (neighborMap.get(DirectionType.SOUTH).isOpen() && lastMove(previousState) != NORTH){
					simPrinter.println("SOUTH 2");
					move.directionType = DirectionType.SOUTH;
				}
				else if (neighborMap.get(DirectionType.NORTH).isOpen() && lastMove(previousState) != SOUTH){
					simPrinter.println("NORTH 2");
					move.directionType = DirectionType.NORTH;
				}
				else if (neighborMap.get(DirectionType.WEST).isOpen() && lastMove(previousState) != EAST){
					simPrinter.println("WEST 2");
					move.directionType = DirectionType.WEST;
				}
				else if (neighborMap.get(DirectionType.EAST).isOpen() && lastMove(previousState) != WEST){
					simPrinter.println("EAST 2");
					move.directionType = DirectionType.EAST;
				}
			}
		} else {
			if (randomNum % 2 == -1) {
				if (neighborMap.get(DirectionType.EAST).isOpen() && lastMove(previousState) != WEST){
					simPrinter.println("EAST 3");
					move.directionType = DirectionType.EAST;
				}
				else if (neighborMap.get(DirectionType.WEST).isOpen() && lastMove(previousState) != EAST){
					simPrinter.println("WEST 3");
					move.directionType = DirectionType.WEST;
				}
				else if (neighborMap.get(DirectionType.NORTH).isOpen() && lastMove(previousState) != SOUTH){
					simPrinter.println("NORTH 3");
					move.directionType = DirectionType.NORTH;
				}
				else if (neighborMap.get(DirectionType.SOUTH).isOpen() && lastMove(previousState) != NORTH){
					simPrinter.println("SOUTH 3");
					move.directionType = DirectionType.SOUTH;
				}
			} else {
				if (neighborMap.get(DirectionType.WEST).isOpen() && lastMove(previousState) != EAST){
					simPrinter.println("WEST 4");
					move.directionType = DirectionType.WEST;
				}
				else if (neighborMap.get(DirectionType.EAST).isOpen() && lastMove(previousState) != WEST){
					simPrinter.println("EAST 4");
					move.directionType = DirectionType.EAST;
				}
				else if (neighborMap.get(DirectionType.SOUTH).isOpen() && lastMove(previousState) != NORTH){
					simPrinter.println("SOUTH 4");
					move.directionType = DirectionType.SOUTH;
				}
				else if (neighborMap.get(DirectionType.NORTH).isOpen() && lastMove(previousState) != SOUTH){
					simPrinter.println("NORTH 4");
					move.directionType = DirectionType.NORTH;
				}
			}
		}
	}

	public static void correctForCurl(Move move, Map<DirectionType, ChemicalCell> neighborMap) {

		byte b = (byte) move.currentState;

		switch(b){
			//ENWS -> N or W
			case -115:
				if (neighborMap.get(DirectionType.NORTH).isOpen()) {
					move.directionType = DirectionType.NORTH;
					updateMoveMemory(move);
					simPrinter.println("curlfix on: -115 North");
				} else if (neighborMap.get(DirectionType.WEST).isOpen()) {
					move.directionType = DirectionType.WEST;
					updateMoveMemory(move);
					simPrinter.println("curlfix on: -115 West");
				}
				simPrinter.println("end curlfix on: -115");
				break;
			//ESWN -> S or W 
			case -100:
				if (neighborMap.get(DirectionType.SOUTH).isOpen()) {
					move.directionType = DirectionType.SOUTH;
					updateMoveMemory(move);
					simPrinter.println("curlfix on: -100 South");
				} else if (neighborMap.get(DirectionType.WEST).isOpen()) {
					move.directionType = DirectionType.WEST;
					updateMoveMemory(move);
					simPrinter.println("curlfix on: -100 West");
				}
				simPrinter.println("end curlfix on: -100")				;
				break;
			//WNES -> N or E
			case -55:
				if (neighborMap.get(DirectionType.NORTH).isOpen()) {
					move.directionType = DirectionType.NORTH;
					updateMoveMemory(move);
					simPrinter.println("curlfix on: -55 North");
				} else if (neighborMap.get(DirectionType.EAST).isOpen()) {
					move.directionType = DirectionType.EAST;
					updateMoveMemory(move);
					simPrinter.println("curlfix on: -55 East");
				}
				simPrinter.println("end curlfix on: -55");
				break;
			//WSEN -> S or E
			case -40:
				if (neighborMap.get(DirectionType.SOUTH).isOpen()) {
					move.directionType = DirectionType.SOUTH;
					updateMoveMemory(move);
					simPrinter.println("curlfix on: -40 South");
				} else if (neighborMap.get(DirectionType.EAST).isOpen()) {
					move.directionType = DirectionType.EAST;
					updateMoveMemory(move);
					simPrinter.println("curlfix on: -40 East");
				}
				simPrinter.println("end curlfix on: -40");
				break;
			//NESW -> E or S
			case 39:
				if (neighborMap.get(DirectionType.EAST).isOpen()) {
					move.directionType = DirectionType.EAST;
					updateMoveMemory(move);
					simPrinter.println("curlfix on: 39 East");
				} else if (neighborMap.get(DirectionType.SOUTH).isOpen()) {
					move.directionType = DirectionType.SOUTH;
					updateMoveMemory(move);
					simPrinter.println("curlfix on: 39 South");
				}
				simPrinter.println("end curlfix on: 39");
				break;
			//NWSE -> W or S
			case 54:
				if (neighborMap.get(DirectionType.WEST).isOpen()) {
					move.directionType = DirectionType.WEST;
					updateMoveMemory(move);
					simPrinter.println("curlfix on: 54 West");
				} else if (neighborMap.get(DirectionType.SOUTH).isOpen()) {
					move.directionType = DirectionType.SOUTH;
					updateMoveMemory(move);
					simPrinter.println("curlfix on: 54 South");
				}
				simPrinter.println("end curlfix on: 54");
				break;
			//SENW -> E or N
			case 99:
				if (neighborMap.get(DirectionType.EAST).isOpen()) {
					move.directionType = DirectionType.EAST;
					updateMoveMemory(move);
					simPrinter.println("curlfix on: 99 East");
				} else if (neighborMap.get(DirectionType.NORTH).isOpen()) {
					move.directionType = DirectionType.NORTH;
					updateMoveMemory(move);
					simPrinter.println("curlfix on: 99 North");
				}
				simPrinter.println("end curlfix on: 99");
				break;
			//SWNE -> W or N
			case 114:
				if (neighborMap.get(DirectionType.WEST).isOpen()) {
					move.directionType = DirectionType.WEST;
					updateMoveMemory(move);
					simPrinter.println("curlfix on: 114 West");
				} else if (neighborMap.get(DirectionType.NORTH).isOpen()) {
					move.directionType = DirectionType.NORTH;
					updateMoveMemory(move);
					simPrinter.println("curlfix on: 114 North");
				}
				simPrinter.println("end curlfix on: 114");
				break;

		}
	}

	public static byte lastMove (Byte previousState) {

		byte b = previousState;

		b <<= 6;
		b = (byte) ((b & 0xff) >>> 6);

		return b;
	}

	public static void updateRandomMoveMemory(Move move, Byte previousState) {
		move.currentState = (byte) (previousState << 2);
		//simPrinter.println("updateRandomMoveMemory PRE: " + move.currentState);

		if (move.directionType == DirectionType.SOUTH) {
			move.currentState = flipBit(move.currentState, 0);
		} else if (move.directionType == DirectionType.EAST) {
			move.currentState = flipBit(move.currentState, 1);
		} else if (move.directionType == DirectionType.WEST) {
			move.currentState = flipBit(move.currentState, 0);
			move.currentState = flipBit(move.currentState, 1);
		}
		//simPrinter.println("updateRandomMoveMemory POST: " + move.currentState);
	}

	public static void updateMoveMemory(Move move){
		move.currentState = (byte) (move.currentState >> 2);
		move.currentState = (byte) (move.currentState << 2);
		simPrinter.println("updateMoveMemory");
		if (move.directionType == DirectionType.SOUTH) {
			move.currentState = flipBit(move.currentState, 0);
		} else if (move.directionType == DirectionType.EAST) {
			move.currentState = flipBit(move.currentState, 1);
		} else if (move.directionType == DirectionType.WEST) {
			move.currentState = flipBit(move.currentState, 0);
			move.currentState = flipBit(move.currentState, 1);
		} 
	}
	
	/*public static ChemicalType getColor(Byte previousState, Byte currentState) {

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
	}*/
	
	public static byte bitAt(Byte currentState, int pos) {
		return (byte) (currentState.byteValue() >> pos & 1);
	}

	public static byte flipBit(Byte currentState, int bit) {
		return (byte) (currentState.byteValue() ^ (1 << bit));
	}

	public static void dumpMemory(Byte currentState) {
		currentState = 0;
	}

	public static void hitPeak(Byte currentState) {
		currentState = (byte) (currentState.byteValue() | (1 << 8));
	}

	public static void hitValley(Byte currentState) {
		currentState = (byte) (currentState.byteValue() & ~(1 << 8));
	}

	public static boolean goingUp(Byte currentState) {
		return currentState > 0;
	}
}