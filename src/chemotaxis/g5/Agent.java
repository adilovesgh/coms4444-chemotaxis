package chemotaxis.g5;

import java.util.Map;
import java.awt.Point;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

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
		boolean solvedWalk = false;
		boolean wallWalk = false;
		boolean randomWalk = false;
		double red = currentCell.getConcentration(ChemicalType.RED);
		double green = currentCell.getConcentration(ChemicalType.GREEN);
		simPrinter.println("Prev Stat: " +previousState);
		switch (previousState) {
			case 0:
				break;
			case 1:
			case 2:
			case 3:
			case 4:
				solvedWalk = true;
				break;
			case 5:
			case 6:
			case 7:
			case 8:
			case 21:
			case 22:
			case 23:
			case 24:
				wallWalk = true;
				break;

			default:
				randomWalk =true;
				break;
		}
	// States: 
	//0: Wait for chemicals to decide where to go
	//1: I'm moving NORTH
	//2: I'm moving EAST
	//3: I'm moving SOUTH
	//4: I'm moving WEST

		if (previousState == 0) {
			move.currentState = 4;
			move.directionType = DirectionType.WEST;
			for (ChemicalType color : ChemicalType.values()) {
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
			for (DirectionType direction : DirectionType.values()){
				if (direction != DirectionType.CURRENT){
					ChemicalCell cell = neighborMap.get(direction);
					if (cell.isOpen()){
						double concentration = cell.getConcentration(ChemicalType.BLUE);
						if (concentration == 1.0){
							move.currentState = getUnsolvedState(direction);
							move.directionType = direction;
							simPrinter.println("WALLWALKING STATE: " + move.currentState);
						}
					}
				}
		
			}
		//	if (move.currentState == 0){
		//		randomWalk = true;
		//		simPrinter.println("RANDOMWALKING");
		//	}
		}
		else if (solvedWalk || checkSolved(red, green)) {
			simPrinter.println("checkSolved!");
			
			if (red == 1.0) {
				move = turnLeft(move, previousState);
				simPrinter.println("Red Move prev is : "+previousState+", now: "+move.currentState);
			} else if (green == 1.0) {
				move = turnRight(move, previousState);
				simPrinter.println("Green Move prev is : "+previousState+", now: "+move.currentState);
			} else {
				move = defaultMove(move, previousState, neighborMap);
				simPrinter.println("def Move prev is : "+previousState+", now: "+move.currentState);
			}
		}
		
		else if (wallWalk){
			//if previous state is 5,6,7,8
			if ((byte) ((int) previousState & 16) == 0) {
				//if we just turned after hitting a wall
				if (neighborMap.get(getPrevDirection(previousState)).isBlocked()) {
					//promote to 21 22 23 24
					previousState = (byte) (16 + (int) previousState);
					List<DirectionType> nextDirection = followTheWall2(previousState, currentCell, neighborMap);
					DirectionType wallDirection = nextDirection.get(1);
					DirectionType direction = nextDirection.get(0);
					move.currentState = (byte) (16 + (int) directionToByte(wallDirection));
					move.directionType = direction;
					return move;
				}
				else {
					move.directionType = getPrevDirection(previousState);
					move.currentState =  previousState;
					return move;
				}
			} else {


				List<DirectionType> nextDirection = followTheWall2(previousState, currentCell, neighborMap);
				DirectionType wallDirection = nextDirection.get(1);
				DirectionType direction = nextDirection.get(0);
				move.currentState = (byte) (16 + (int) directionToByte(wallDirection));
				move.directionType = direction;
				return move;
			}

		}
		/*
		else if (randomWalk){

		}*/
		simPrinter.println("nextState: " + move.currentState);
		return move;
	}

	public byte getSolvedState(DirectionType direction){

		if (direction == DirectionType.NORTH) 
			return 1;
		else if (direction == DirectionType.EAST) 
			return 2;
		else if (direction == DirectionType.SOUTH) 
			return 3;
		return 4;
	}

	public byte getUnsolvedState(DirectionType direction){

		if (direction == DirectionType.NORTH) 
			return 5;
		else if (direction == DirectionType.EAST) 
			return 7;
		else if (direction == DirectionType.SOUTH) 
			return 6;
		return 8;
	}

	public Byte directionTranslator(Byte previousState){
		switch ((byte) previousState){
			case 1:
			case 5: 
			case 23:
				return 1;
			case 2:
			case 7:
			case 22:
				return 2;
			case 3:
			case 6:
			case 24:
				return 3;	
		}
		return 4;

	}

	public Move turnLeft(Move move, Byte previousState) {
		if (directionTranslator(previousState) == 1) {
			move.directionType = DirectionType.WEST;
			move.currentState = 4;
		} else if (directionTranslator(previousState) == 2) {
			move.directionType = DirectionType.NORTH;
			move.currentState = 1;
		} else if (directionTranslator(previousState) == 3) {
			move.directionType = DirectionType.EAST;
			move.currentState = 2;
		} else if (directionTranslator(previousState) == 4) {
			move.directionType = DirectionType.SOUTH;
			move.currentState = 3;
		}
		return move;
	}

	public Move turnRight(Move move, Byte previousState) {
		if (directionTranslator(previousState) == 1) {
			move.directionType = DirectionType.EAST;
			move.currentState = 2;
		} else if (directionTranslator(previousState) == 2) {
			move.directionType = DirectionType.SOUTH;
			move.currentState = 3;
		} else if (directionTranslator(previousState) == 3) {
			move.directionType = DirectionType.WEST;
			move.currentState = 4;
		} else if (directionTranslator(previousState) == 4) {
			move.directionType = DirectionType.NORTH;
			move.currentState = 1;
		}
		return move;
	}

	public Move straight(Move move, Byte previousState) {
		if (directionTranslator(previousState) == 1) {
			move.directionType = DirectionType.NORTH;
			move.currentState = 1;
		} else if (directionTranslator(previousState) == 2) {
			move.directionType = DirectionType.EAST;
			move.currentState = 2;
		} else if (directionTranslator(previousState) == 3) {
			move.directionType = DirectionType.SOUTH;
			move.currentState = 3;
		} else if (directionTranslator(previousState) == 4) {
			move.directionType = DirectionType.WEST;
			move.currentState = 4;
		}
		move.currentState = directionTranslator(previousState);
		return move;
	}

	public boolean checkSolved(double red, double green){
		if (red == 1.0 || green == 1.0)
			return true;
		return false;
	}



	public Move defaultMove(Move move, Byte previousState, Map<DirectionType, ChemicalCell> neighborMap) {
	//1: I'm moving NORTH
	//2: I'm moving EAST
	//3: I'm moving SOUTH
	//4: I'm moving WEST
		if (previousState == 1) {
			ChemicalCell headCell = neighborMap.get(DirectionType.NORTH);
			ChemicalCell rightCell = neighborMap.get(DirectionType.EAST);
			if (headCell.isBlocked()) {
				if (rightCell.isBlocked()) {
					ChemicalCell leftCell = neighborMap.get(DirectionType.WEST);
					if (leftCell.isBlocked()) {
						move.directionType = DirectionType.SOUTH;
						move.currentState = 3;
					} else {
						move.directionType = DirectionType.WEST;
						move.currentState = 4;
					}
				} else {
					move.directionType = DirectionType.EAST;
					move.currentState = 2;
				}
			} else {
				move.directionType = DirectionType.NORTH;
				move.currentState = previousState;
			}
		} else if (previousState == 2) {
			ChemicalCell headCell = neighborMap.get(DirectionType.EAST);
			ChemicalCell rightCell = neighborMap.get(DirectionType.SOUTH);
			if (headCell.isBlocked()) {
				if (rightCell.isBlocked()) {
					ChemicalCell leftCell = neighborMap.get(DirectionType.NORTH);
					if (leftCell.isBlocked()) {
						move.directionType = DirectionType.WEST;
						move.currentState = 4;
					} else {
						move.directionType = DirectionType.NORTH;
						move.currentState = 1;
					}
				} else {
					move.directionType = DirectionType.SOUTH;
					move.currentState = 3;
				}
			} else {
				move.directionType = DirectionType.EAST;
				move.currentState = previousState;
			}
		} else if (previousState == 3) {
			ChemicalCell headCell = neighborMap.get(DirectionType.SOUTH);
			ChemicalCell rightCell = neighborMap.get(DirectionType.WEST);
			if (headCell.isBlocked()) {
				if (rightCell.isBlocked()) {
					ChemicalCell leftCell = neighborMap.get(DirectionType.EAST);
					if (leftCell.isBlocked()) {
						move.directionType = DirectionType.NORTH;
						move.currentState = 1;
					} else {
						move.directionType = DirectionType.EAST;
						move.currentState = 2;
					}
				} else {
					move.directionType = DirectionType.WEST;
					move.currentState = 4;
				}
			} else {
				move.directionType = DirectionType.SOUTH;
				move.currentState = previousState;
			}
		} else if (previousState == 4) {
			ChemicalCell headCell = neighborMap.get(DirectionType.WEST);
			ChemicalCell rightCell = neighborMap.get(DirectionType.NORTH);
			if (headCell.isBlocked()) {
				if (rightCell.isBlocked()) {
					ChemicalCell leftCell = neighborMap.get(DirectionType.SOUTH);
					if (leftCell.isBlocked()) {
						move.directionType = DirectionType.EAST;
						move.currentState = 2;
					} else {
						move.directionType = DirectionType.SOUTH;
						move.currentState = 3;
					}
				} else {
					move.directionType = DirectionType.NORTH;
					move.currentState = 1;
				}
			} else {
				move.directionType = DirectionType.WEST;
				move.currentState = previousState;
			}
		}

		return move;
	}
	private List<DirectionType> getOrthogonalDirections(DirectionType previousDirection) {
		List<DirectionType> directionList = new LinkedList<DirectionType>(); 

		if (previousDirection == DirectionType.EAST) { 
			directionList.add(DirectionType.NORTH);
			directionList.add(DirectionType.SOUTH);
		}
		else if (previousDirection == DirectionType.WEST) { 
			directionList.add(DirectionType.SOUTH);
			directionList.add(DirectionType.NORTH);
		}
		else if (previousDirection == DirectionType.NORTH) { 
			directionList.add(DirectionType.WEST);
			directionList.add(DirectionType.EAST);
		}
		else { 
			directionList.add(DirectionType.EAST);
			directionList.add(DirectionType.WEST);
		}
		return directionList; 
	}

	private DirectionType getOppositeDirections(DirectionType oppositeDirection) { 
		switch (oppositeDirection) { 
			case NORTH: return DirectionType.SOUTH;
			case SOUTH: return DirectionType.NORTH; 
			case WEST: return DirectionType.EAST; 
			case EAST: return DirectionType.WEST; 
			default: return oppositeDirection;
		}
	}

	private DirectionType getPrevDirection(Byte previousState) { 
		int previous_direction_state = previousState;
		if (previous_direction_state == 5) { 
			return DirectionType.NORTH;
		}
		else if (previous_direction_state == 6) { 
			return DirectionType.SOUTH; 
		}
		else if (previous_direction_state == 7) { 
			return DirectionType.EAST; 
		}
		return DirectionType.WEST;
	}

	private DirectionType getWallDirection(byte previousState) { 
		byte wall_direction_state = (byte) ((int) previousState & 7); 
		simPrinter.println(wall_direction_state);
		if (wall_direction_state == 5) {
			return DirectionType.NORTH;
		}
		else if (wall_direction_state == 6) { 
			return DirectionType.SOUTH;
		}
		else if (wall_direction_state == 7) {
			return DirectionType.EAST;
		}
		return DirectionType.WEST; 
	}

	private List<DirectionType> followTheWall2(Byte previousState, ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
		DirectionType wallDirection = getWallDirection(previousState);
		List<DirectionType> orthogonalCells = getOrthogonalDirections(wallDirection);
		DirectionType previousDirection = orthogonalCells.get(0);
		DirectionType secondDirection = orthogonalCells.get(1);

		ChemicalCell cellInSameDirection = neighborMap.get(previousDirection);
		ChemicalCell firstOrthogonalCell = neighborMap.get(wallDirection);
		ChemicalCell secondOrthogonalCell = neighborMap.get(secondDirection); 

		ChemicalCell wallCell = neighborMap.get(wallDirection);

		List<DirectionType> result = new LinkedList<DirectionType>();

		if (!wallCell.isBlocked()) { 
			result.add(wallDirection);
			result.add(getOppositeDirections(previousDirection));
			return result;
		}

		if (cellInSameDirection.isBlocked()) {
			if (firstOrthogonalCell.isBlocked() && secondOrthogonalCell.isBlocked()) { 
				result.add(getOppositeDirections(previousDirection));
				result.add(getOppositeDirections(wallDirection));
				return result;
			}
			else { 
				result.add(getOppositeDirections(wallDirection));
				result.add(previousDirection);
				return result;
			}
		}
		result.add(previousDirection);
		result.add(wallDirection);
		return result;

	}

	private byte directionToByte(DirectionType direction) { 
		switch (direction) { 
			case NORTH: 
			return (byte) 5;
			case SOUTH: 
			return (byte) 6;
			case EAST:
			return (byte) 7; 
			default: 
			return (byte) 8;
		}
	}
}