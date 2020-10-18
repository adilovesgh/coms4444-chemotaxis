package chemotaxis.g1; // TODO modify the package name to reflect your team

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import chemotaxis.sim.DirectionType;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.Move;
import chemotaxis.sim.SimPrinter;
import chemotaxis.sim.ChemicalCell.ChemicalType;
//import jdk.internal.agent.resources.agent;

public class Agent extends chemotaxis.sim.Agent {

    /**
     * Agent constructor
     *
     * @param simPrinter simulation printer
     *
     */

    enum AgentDefault {
        MOVERIGHT,
        MOVELEFT,
        MOVEUP,
        MOVEDOWN,
        EXPLORE, 
    }

    public Agent(SimPrinter simPrinter) {
        super(simPrinter);
    }

    /**
     * Move agent
     *
     * @param randomNum     random number available for agents
     * @param previousState byte of previous state
     * @param currentCell   current cell
     * @param neighborMap   map of cell's neighbors
     * @return agent move
     *
     */
    @Override
    public Move makeMove(Integer randomNum, Byte previousState, ChemicalCell currentCell,
                         Map<DirectionType, ChemicalCell> neighborMap) {
        Move agentMove = new Move();
        // Map<DirectionType, ChemicalType> move = getMostConcentratedChemical(neighborMap);
        // for(DirectionType key: move.keySet()){
        //     if(move.get(key) == ChemicalType.BLUE){
        //         agentMove.directionType = key;
        //     } else if (move.get(key) == ChemicalType.RED){
        //         agentMove.directionType = getOppositeDirection(key);
        //     }
        // }
        //TO-DO: need to make use of previousState, when agent fluctuates between states
        agentMove.directionType = getStrongestAffinity(neighborMap);
        if(!isValidMove(agentMove.directionType , neighborMap)){
            AgentDefault defaultOption =  findAlternativeRoute(agentMove.directionType , neighborMap);
            System.out.print(" df. " + defaultOption + "\n"); 
            return getAgentDefaultMove(defaultOption);
        }
        return agentMove;
    }

    private Map<DirectionType, ChemicalType> getMostConcentratedChemical(Map<DirectionType, ChemicalCell> neighborCellMap) {
        DirectionType max = null;
        ChemicalType maxType = null;
        double maxConc = -10.0;
        for (DirectionType neighborDir : neighborCellMap.keySet()) {
            ChemicalCell neighbor = neighborCellMap.get(neighborDir);
            Map<ChemicalType, Double> neighborMap = neighbor.getConcentrations();

            Double maxValueInMap = (Collections.max(neighborMap.values())); // This will return max value in the Hashmap
            for (Entry<ChemicalType, Double> entry : neighborMap.entrySet()) { // Itrate through hashmap
                if (entry.getValue() == maxValueInMap) {
                    if(maxConc < maxValueInMap){
                        maxConc = maxValueInMap;
                        maxType = entry.getKey();
                        max = neighborDir;
                    }
                }
            }
        }
        Map<DirectionType, ChemicalType> move = new HashMap<>();
        move.put(max, maxType);
        return move;
    }
    private DirectionType getOppositeDirection(DirectionType inDirection){
        if(inDirection.equals(DirectionType.EAST)){
            return DirectionType.WEST;
        } else if (inDirection.equals(DirectionType.WEST)){
            return DirectionType.EAST;
        } else if (inDirection.equals(DirectionType.NORTH)){
            return DirectionType.SOUTH;
        } else {
            return DirectionType.NORTH;
        }
    }

    private Map<DirectionType, List<Double>> getAffinities(Map<DirectionType, ChemicalCell> neighborCellMap){
        double blueAffinity = 1.0;
        double redAffinity = -1.0;
        double greenAffinity = 10.0;
        Map<DirectionType, List<Double>> affinity = new HashMap<>();
        for (DirectionType neighborDir : neighborCellMap.keySet()) {
            ChemicalCell neighbor = neighborCellMap.get(neighborDir);
            Map<ChemicalType, Double> neighborMap = neighbor.getConcentrations();
            ArrayList<Double> affinities = new ArrayList<>();
            for (Entry<ChemicalType, Double> entry : neighborMap.entrySet()) { // Itrate through hashmap
                if(entry.getKey().equals(ChemicalType.BLUE)){
                    affinities.add(blueAffinity*entry.getValue());
                } else if(entry.getKey().equals(ChemicalType.RED)){
                    affinities.add(redAffinity*entry.getValue());
                } else {
                    affinities.add(greenAffinity*entry.getValue());
                }
            }
            affinity.put(neighborDir, affinities);
        }
        return affinity;
    }

    private DirectionType getStrongestAffinity(Map<DirectionType, ChemicalCell> neighborCellMap){
        Map<DirectionType, List<Double>> affinityMap = getAffinities(neighborCellMap);
        Map<DirectionType, Double> maxAffinityMap = new HashMap<>();
        for (DirectionType neighborDir : affinityMap.keySet()) {
            List<Double> affinities = affinityMap.get(neighborDir);
            int maxIndex = 0;
            for (int i = 0; i < affinities.size(); i++){
                if(Math.abs(affinities.get(i)) > affinities.get(maxIndex)){
                    maxIndex = i;
                }
            }
            maxAffinityMap.put(neighborDir, affinities.get(maxIndex));
        }
        //TO-DO Add default behavior
        DirectionType max = DirectionType.NORTH;
        double maxAffinity = -1.0;
        for(DirectionType dir : maxAffinityMap.keySet()){
            if(Math.abs(maxAffinityMap.get(dir)) > maxAffinity){
                maxAffinity = maxAffinityMap.get(dir);
                max = dir;
            }
        }
        if(maxAffinity < 0.0){
            return getOppositeDirection(max);
        } else {
            return max;
        }
    }

    private boolean isValidMove(DirectionType directionType, Map<DirectionType, ChemicalCell> neighborCellMap){
        //System.out.print(" s. " + neighborCellMap.get(directionType).isOpen() + "\n"); 
        return neighborCellMap.get(directionType).isOpen();
    }

    private AgentDefault findAlternativeRoute(DirectionType directionType, Map<DirectionType, ChemicalCell> neighborCellMap){
     
        //Check if Blocked corner || blocked left +right || blocked up and down || blocked my current cell
        if (isCornerEdge(directionType, neighborCellMap) != null){
            return (isCornerEdge(directionType, neighborCellMap));
        } else if (isBlockedHorz(directionType, neighborCellMap) != null){
            return (isBlockedHorz(directionType, neighborCellMap));
        } else if (isBlockedHorz(directionType, neighborCellMap) != null){
            return (isBlockedVert(directionType, neighborCellMap));
        } else {
        return directionToAgentDefault(getOppositeDirection(directionType));
        }
    }

    private AgentDefault isCornerEdge(DirectionType directionType, Map<DirectionType, ChemicalCell> neighborCellMap){
        if(neighborCellMap.get(DirectionType.WEST).isBlocked() && neighborCellMap.get(DirectionType.NORTH).isBlocked()){
            return AgentDefault.MOVEDOWN;
        }
        else if (neighborCellMap.get(DirectionType.EAST).isBlocked() && neighborCellMap.get(DirectionType.SOUTH).isBlocked()) {
            return AgentDefault.MOVELEFT;
        }
        else if (neighborCellMap.get(DirectionType.NORTH).isBlocked() && neighborCellMap.get(DirectionType.EAST).isBlocked()) {
            return AgentDefault.MOVERIGHT;
        } else {
            return null;
        }
    }

    private AgentDefault isBlockedHorz(DirectionType directionType, Map<DirectionType, ChemicalCell> neighborCellMap){
        if(neighborCellMap.get(DirectionType.WEST).isBlocked() && neighborCellMap.get(DirectionType.EAST).isBlocked()){
            return AgentDefault.MOVEDOWN; 
        } else {
            return null;
        }
    }

    private AgentDefault isBlockedVert(DirectionType directionType, Map<DirectionType, ChemicalCell> neighborCellMap){
        if(neighborCellMap.get(DirectionType.NORTH).isBlocked() && neighborCellMap.get(DirectionType.SOUTH).isBlocked()){
            return AgentDefault.MOVERIGHT; 
        } else {
            return null;
        }
    }

    private Move getAgentDefaultMove(AgentDefault defaultOption){
        Move defmove = new Move();
        switch (defaultOption) {
            case MOVEUP:
                defmove.directionType = DirectionType.NORTH;
                defmove.currentState = (byte) 1; //makes the agent aware of it current default behavior and stores it in prevState
                return defmove;
            case MOVERIGHT:
                defmove.directionType = DirectionType.EAST;
                defmove.currentState = (byte) 2;
                return defmove;
            case MOVEDOWN:
                defmove.directionType = DirectionType.SOUTH; 
                defmove.currentState = (byte) 3;
                return defmove;
            case MOVELEFT:
                defmove.directionType = DirectionType.WEST; 
                defmove.currentState = (byte) 4;
                return defmove;
            // case for explore
            default:
                defmove.directionType = DirectionType.CURRENT;
                defmove.currentState = (byte) 0;
                return defmove;
        }
    }

    private AgentDefault directionToAgentDefault(DirectionType directionType){
        switch (directionType) {
            case NORTH:
                return AgentDefault.MOVEUP;
            case SOUTH:
                return AgentDefault.MOVEDOWN;
            case WEST:
                return AgentDefault.MOVERIGHT;
            case EAST:
                return AgentDefault.MOVELEFT;
            default:
                return AgentDefault.EXPLORE;
        }
    }

}
