package chemotaxis.g1; // TODO modify the package name to reflect your team

import java.util.HashMap;
import java.util.Map;

import chemotaxis.sim.DirectionType;
import chemotaxis.sim.ChemicalCell;
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
    @Override
    public Move makeMove(Integer randomNum, Byte previousState, ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
        Move agentMove = new Move();
        DirectionType dir = getBlueDirection(neighborMap, 0.99);
        if(dir != DirectionType.CURRENT) {
            agentMove.currentState = getDirectionByte(dir);
            agentMove.directionType = dir;
        }
        else {
            agentMove.directionType = getDirectionFromByte(previousState);
            agentMove.currentState = getDirectionByte(agentMove.directionType);
        }
        return agentMove;
    }

    private DirectionType getBlueDirection(Map<DirectionType, ChemicalCell> neighborMap, Double blueThreshold) {
        Map<DirectionType, Map<ChemicalCell.ChemicalType, Double>> concentrationMap = getConcentrations(neighborMap);
        DirectionType absoluteBlue = DirectionType.CURRENT;

        for(DirectionType dir : concentrationMap.keySet()) {
            if(concentrationMap.get(dir).get(ChemicalCell.ChemicalType.BLUE) >= blueThreshold) absoluteBlue = dir;
        }

        return absoluteBlue;
    }

    private Byte getDirectionByte(DirectionType dir) {
        switch (dir) {
            case SOUTH:
                return (byte) 4;
            case WEST:
                return (byte) 3;
            case NORTH:
                return (byte) 2;
            case EAST:
                return (byte) 1;
            default:
                return (byte) 0;
        }
    }

    private DirectionType getDirectionFromByte(Byte b) {
        switch (b) {
            case (byte) 4:
                return DirectionType.SOUTH;
            case (byte) 3:
                return DirectionType.WEST;
            case (byte) 2:
                return DirectionType.NORTH;
            case (byte) 1:
                return DirectionType.EAST;
            default:
                return DirectionType.CURRENT;
        }
    }

    private Map<DirectionType, Map<ChemicalCell.ChemicalType, Double>> getConcentrations(Map<DirectionType, ChemicalCell> neighborMap) {
        Map<DirectionType, Map<ChemicalCell.ChemicalType, Double>> concentrationMap = new HashMap<>();

        for(DirectionType dir : neighborMap.keySet()) {
            concentrationMap.put(dir, neighborMap.get(dir).getConcentrations());
        }

        return concentrationMap;
    }
}