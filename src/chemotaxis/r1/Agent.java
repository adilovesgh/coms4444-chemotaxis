package chemotaxis.r1;

import java.util.Map;

import chemotaxis.sim.DirectionType;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.Move;
import chemotaxis.sim.SimPrinter;
import chemotaxis.sim.ChemicalCell.ChemicalType;

public class Agent extends chemotaxis.sim.Agent {


/**
* Agent constructor
*
* @param simPrinter  simulation printer
*
*/
public Agent(SimPrinter simPrinter) {
	super(simPrinter);
}

/**
* Move agent
*
* @param randomNum        random number available for agents
* @param previousState    byte of previous state
* @param currentCell      current cell
* @param neighborMap      map of cell's neighbors
* @return                 agent move
*
*/
@Override
public Move makeMove(Integer randomNum, Byte previousState, ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
// TODO add your code here to move the agent
	Move move = new Move();
// States: 
//0: Wait for chemicals to decide where to go
//1: I'm moving NORTH
//2: I'm moving EAST
//3: I'm moving SOUTH
//4: I'm moving WEST
	
	AgentMemory.getRandomMove(randomNum, previousState, move, neighborMap);
	AgentMemory.updateRandomMoveMemory(move, previousState);
	AgentMemory.correctForCurl(move, neighborMap);
	simPrinter.println("AFTER: " + move.currentState);
	/*
	if (previousState == 0) {
		move.currentState = 4;
		move.directionType = DirectionType.WEST;
		for (ChemicalType color: ChemicalType.values()) {
			double concentration = currentCell.getConcentration(color);
			if (concentration == 1.0) {
				if (color == ChemicalType.RED) {
					move.currentState = 1;
					move.directionType = DirectionType.NORTH;
				} else if (color == ChemicalType.GREEN) {
					move.currentState = 2;
					move.directionType = DirectionType.EAST;
				} else if (color == ChemicalType.BLUE) {
					move.currentState = 3;
					move.directionType = DirectionType.SOUTH;
				}
			}
		}
		if (move.currentState = 4){}
	} else {
		double red = currentCell.getConcentration(ChemicalType.RED);
		double green = currentCell.getConcentration(ChemicalType.GREEN);
		if (red == 1.0) {
			move = turnLeft(move, previousState);
		} else if (green == 1.0) {
			move = turnRight(move, previousState);
		} else {
			move = straight(move, previousState);
		}
	} */

	return move;
// TODO modify the return statement to return your agent move
}

public Move turnLeft(Move move, Byte previousState) {
	if (previousState == 1) {
		move.directionType = DirectionType.WEST;
		move.currentState = 4;
	} else if (previousState == 2) {
		move.directionType = DirectionType.NORTH;
		move.currentState = 1;
	} else if (previousState == 3) {
		move.directionType = DirectionType.EAST;
		move.currentState = 2;
	} else if (previousState == 4) {
		move.directionType = DirectionType.SOUTH;
		move.currentState = 3;
	}
	return move;
}

public Move turnRight(Move move, Byte previousState) {
	if (previousState == 1) {
		move.directionType = DirectionType.EAST;
		move.currentState = 2;
	} else if (previousState == 2) {
		move.directionType = DirectionType.SOUTH;
		move.currentState = 3;
	} else if (previousState == 3) {
		move.directionType = DirectionType.WEST;
		move.currentState = 4;
	} else if (previousState == 4) {
		move.directionType = DirectionType.NORTH;
		move.currentState = 1;
	}
	return move;
}

public Move straight(Move move, Byte previousState) {
	if (previousState == 1) {
		move.directionType = DirectionType.NORTH;
	} else if (previousState == 2) {
		move.directionType = DirectionType.EAST;
	} else if (previousState == 3) {
		move.directionType = DirectionType.SOUTH;
	} else if (previousState == 4) {
		move.directionType = DirectionType.WEST;
	}
	move.currentState = previousState;
	return move;
}
}