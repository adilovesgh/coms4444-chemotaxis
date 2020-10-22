package chemotaxis.rahul;

import java.util.Map;
import java.util.*;

import chemotaxis.sim.DirectionType;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.Move;
import chemotaxis.sim.SimPrinter;

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

    private DirectionType followTheWall(Byte previousState, ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
        simPrinter.println("Follow Wall");
        DirectionType previousDirection = getPrevDirection(previousState);
        ChemicalCell cellInSameDirection = neighborMap.get(previousDirection);
        List<DirectionType> orthogonalDirections = getOrthogonalDirections(previousDirection);

        DirectionType firstOrthogonalDirection = orthogonalDirections.get(0);
        DirectionType secondOrthogonalDirection = orthogonalDirections.get(1);
        ChemicalCell firstOrthogonalCell = neighborMap.get(orthogonalDirections.get(0));
        ChemicalCell secondOrthogonalCell = neighborMap.get(orthogonalDirections.get(1)); 

        if (cellInSameDirection.isBlocked())  { 
            if (firstOrthogonalCell.isBlocked() && secondOrthogonalCell.isBlocked()) { 
                return getOppositeDirections(previousDirection);
            }
            else if (firstOrthogonalCell.isBlocked()) { 
                return secondOrthogonalDirection; 
            }
            else { 
                return firstOrthogonalDirection;
            }
        }
        else if (firstOrthogonalCell.isBlocked() || firstOrthogonalCell.isBlocked()) { 
            return previousDirection;
        }
        return previousDirection;
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
	@Override
	public Move makeMove(Integer randomNum, Byte previousState, ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
        Move nextMove = new Move();

        // Get green chemical surroundings
        /*
        double highest = 0; 
        DirectionType maxDirection = null;
        for (DirectionType directionType: neighborMap.keySet()) { 
            if (highest < neighborMap.get(directionType).getConcentration(ChemicalType.GREEN)) {
                highest = neighborMap.get(directionType).getConcentration(ChemicalType.GREEN);
                maxDirection = directionType;
            }
        }
        */

        /* if (highest != 0) {
            DirectionType nextDirection = getOppositeDirections(maxDirection);
            nextMove.currentState = directionToByte(nextDirection);
            nextMove.directionType = nextDirection;
            if (neighborMap.get(nextDirection).isBlocked()) { 
                DirectionType modifiedDirection = followTheWall(directionToByte(nextDirection), currentCell, neighborMap);
                nextMove.currentState = directionToByte(modifiedDirection);
                nextMove.directionType = modifiedDirection;
            }
            else {
                nextMove.currentState = directionToByte(nextDirection);
                nextMove.directionType = nextDirection;
            }
            return nextMove;
        
        } */
       //  else {
        if ((byte) ((int) previousState & 16) == 0) {
            if (neighborMap.get(getPrevDirection(previousState)).isBlocked()) {
                previousState = (byte) (16 + (int) previousState);
                List<DirectionType> nextDirection = followTheWall2(previousState, currentCell, neighborMap);
                DirectionType wallDirection = nextDirection.get(1);
                DirectionType direction = nextDirection.get(0);
                nextMove.currentState = (byte) (16 + (int) directionToByte(wallDirection));
                nextMove.directionType = direction;
                return nextMove;
            }
            else {
                nextMove.directionType = getPrevDirection(previousState);
                nextMove.currentState =  previousState;
                return nextMove;
            }
        }
        else {
            Random rand = new Random();
            int answer = rand.nextInt(100) + 1;
            if (answer == 1) {
                List<DirectionType> nextDirection = followTheWall2(previousState, currentCell, neighborMap);
                DirectionType wallDirection = nextDirection.get(1);
                DirectionType direction = nextDirection.get(0);
                nextMove.currentState = directionToByte(getOppositeDirections(wallDirection));
                nextMove.directionType = getOppositeDirections(wallDirection);
                return nextMove;
            }


            List<DirectionType> nextDirection = followTheWall2(previousState, currentCell, neighborMap);
            DirectionType wallDirection = nextDirection.get(1);
            DirectionType direction = nextDirection.get(0);
            nextMove.currentState = (byte) (16 + (int) directionToByte(wallDirection));
            nextMove.directionType = direction;
            return nextMove;
        }

        
    }
}