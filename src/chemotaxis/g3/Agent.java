package chemotaxis.g3;

import java.util.Map;
import chemotaxis.g3.Language.Translator;

import chemotaxis.sim.DirectionType;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.Move;
import chemotaxis.sim.SimPrinter;

public class Agent extends chemotaxis.sim.Agent {

    private Translator trans = null;
    private int rand = 0;

    /**
     * Agent constructor
     *
     * @param simPrinter  simulation printer
     *
     */
	public Agent(SimPrinter simPrinter) {
        super(simPrinter);
        trans = Translator.getInstance();
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
        // TODO: self-realize silent + n/a, pause
        // TODO: try to get to a position where you can recieve the most instructions 
        // TODO: start moving erratically if haven't had instructions for a bit 
        Move move = new Move();
        this.rand = randomNum;
        Integer prevByte = (int) previousState;
        char[] nextState = new char[9];
        
        // check for new instruction first
        String instructionCheck = checkForInstructions(currentCell, neighborMap);
        String prevState = null;

        // simPrinter.println("New instruction: " + instructionCheck);
        
        // if instruction exists, get the new state
        if (instructionCheck != null) {
            prevState = trans.getState(instructionCheck, prevByte);
            simPrinter.println("New instruction: " + instructionCheck + " " + prevState);
        }
        else {
            // if no instruction exists, keep running with the previous state
            prevState = trans.getState(prevByte);
            simPrinter.println("No instruction: " + prevState);
        }

        //  X    ±    N   [.*]  Y    ±    M   [.*] [C/R]
        // '0', '+', '0', '*', '0', '+', '0', '.', 'C'
        //  0    1    2    3    4    5    6    7    8
        
        // based on the prevState, check the surroundings and find an opening for the next move 
        if (prevState.equals("pause")) {
            simPrinter.println("Agent is paused");
            move.directionType = DirectionType.CURRENT;
            nextState = "pause".toCharArray();
        }
        if (prevState.equals("0+0*0+0.R") && instructionCheck == null) {
            simPrinter.println("Agent left with no instructionn, wandering");
            if (downOpen(neighborMap)) {
                nextState = "0+0.0-1*R".toCharArray();
                move.directionType = DirectionType.WEST;
            }
            else if (rightOpen(neighborMap)) {
                nextState = "0+1*0+0.R".toCharArray();
                move.directionType = DirectionType.SOUTH;
            }
            else if (upOpen(neighborMap)) {
                nextState = "0+0.0+1*R".toCharArray();
                move.directionType = DirectionType.EAST;
            }
            else if (leftOpen(neighborMap)) {
                nextState = "0-1*0+0.R".toCharArray();
                move.directionType = DirectionType.NORTH;
            }
        }
        // else if (blocked(prevState, neighborMap)) {
        //     nextState = moveWithBlock(prevState, neighborMap);
        //     move.DirectionType = blockMoveDirection(nextState);
        // }
        else if (mobilityUp(prevState, neighborMap)) {
            simPrinter.println("Agent can + should move east/up");
            nextState =  moveInY(nextState, prevState);
            move.directionType = DirectionType.EAST;
        }
        else if (mobilityDown(prevState, neighborMap)) {
            simPrinter.println("Agent can + should move west/down");
            nextState =  moveInY(nextState, prevState);
            move.directionType = DirectionType.WEST;
        }
        else if (mobilityLeft(prevState, neighborMap)) {
            simPrinter.println("Agent can + should move north/left");
            nextState = moveInX(nextState, prevState);
            move.directionType = DirectionType.NORTH;
        }
        else if (mobilityRight(prevState, neighborMap)) {
            simPrinter.println("Agent can + should move south/right");
            nextState = moveInX(nextState, prevState);
            move.directionType = DirectionType.SOUTH;
        }
        else if (mobilityUpCycle(prevState, neighborMap)) {
            simPrinter.println("Agent may be blocked, repeating cycle up");
            nextState =  moveInY(nextState, prevState);
            move.directionType = DirectionType.EAST;
        }
        else if (mobilityDownCycle(prevState, neighborMap)) {
            simPrinter.println("Agent may be blocked, repeating cycle down");
            nextState =  moveInY(nextState, prevState);
            move.directionType = DirectionType.WEST;
        }
        else if (mobilityLeftCycle(prevState, neighborMap)) {
            simPrinter.println("Agent may be blocked, repeating cycle left");
            nextState = moveInX(nextState, prevState);
            move.directionType = DirectionType.NORTH;
        }
        else if (mobilityRightCycle(prevState, neighborMap)) {
            simPrinter.println("Agent may be blocked, repeating cycle right");
            nextState = moveInX(nextState, prevState);
            move.directionType = DirectionType.SOUTH;
        }
        else {
            simPrinter.println("Agent was paused");
            nextState = "pause".toCharArray();
            move.directionType = DirectionType.CURRENT;
        }

        // translate to Byte for memory 
        Byte nextByte = trans.getByte(nextState);
        simPrinter.println("Byte for next round: " + nextByte);
        simPrinter.println("State for next round: " + String.valueOf(nextState));
        move.currentState = nextByte;
		return move;
    }
    
    private String checkForInstructions(ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
        char[] directions = new char[] {'_','_','_','_'};

        if (currentCell.getConcentration(ChemicalType.RED) == 1)
            directions[1] = 'R';
        if (currentCell.getConcentration(ChemicalType.GREEN) == 1)
            directions[2] = 'G';
        if (currentCell.getConcentration(ChemicalType.BLUE) == 1)
            directions[3] = 'B';

        char temp = ' ';
        for (DirectionType directionType : neighborMap.keySet()) {
            if (directionType == DirectionType.SOUTH) 
                temp = 'r';
            else if (directionType == DirectionType.NORTH) 
                temp = 'l';
            else if (directionType == DirectionType.EAST) 
                temp = 'u';
            else
                temp = 'd';

            if (neighborMap.get(directionType).getConcentration(ChemicalType.RED) == 1) {
                directions[0] = temp;
                directions[1] = 'R';
            }
            if (neighborMap.get(directionType).getConcentration(ChemicalType.GREEN) == 1) {
                directions[0] = temp;
                directions[2] = 'G';
            }
            if (neighborMap.get(directionType).getConcentration(ChemicalType.BLUE) == 1) {
                directions[0] = temp;
                directions[3] = 'B';
            }
            if (!(String.valueOf(directions).equals("____"))) 
                break;
        }

        if (!(String.valueOf(directions).equals("____")))
            return String.valueOf(directions);
        return null;
    }

    private Boolean mobilityUp(String state, Map<DirectionType, ChemicalCell> surroundings) {
        // There's space above this cell
        // and the instuction is moving in that direction
        // and moving up is possible according to state
        // and either – not resetting AND you can move up in this cycle 
        //         or – are resetting AND you have not maxed out moves in that axis for this cycle 
        return (state.charAt(5) == '+'
                && ((state.charAt(8) == 'C' && state.charAt(4) <= state.charAt(6) - 1) 
                    || (state.charAt(8) == 'R' && state.charAt(4) == state.charAt(6) - 1))
                && upOpen(surroundings)
        );
    }

    private Boolean mobilityUpCycle(String state, Map<DirectionType, ChemicalCell> surroundings) {
        return (state.charAt(5) == '+' && state.charAt(6) >= '1' && upOpen(surroundings));
    }

    private Boolean upOpen(Map<DirectionType, ChemicalCell> surroundings) {
        return surroundings.get(DirectionType.EAST).isOpen();
    }

    private Boolean mobilityDown(String state, Map<DirectionType, ChemicalCell> surroundings) {
        return (state.charAt(5) == '-'
                && ((state.charAt(8) == 'C' && state.charAt(4) <= state.charAt(6) - 1) 
                    || (state.charAt(8) == 'R' && state.charAt(4) == state.charAt(6) - 1))
                && downOpen(surroundings)
        );
    }

    private Boolean mobilityDownCycle(String state, Map<DirectionType, ChemicalCell> surroundings) {
        return (state.charAt(5) == '-' && state.charAt(6) >= '1' && downOpen(surroundings));
    }

    private Boolean downOpen(Map<DirectionType, ChemicalCell> surroundings) {
        return surroundings.get(DirectionType.WEST).isOpen();
    }

    private Boolean mobilityLeft(String state, Map<DirectionType, ChemicalCell> surroundings) {
        return (state.charAt(1) == '-'
                && ((state.charAt(8) == 'C' && state.charAt(0) <= state.charAt(2) - 1) 
                    || (state.charAt(8) == 'R' && state.charAt(0) == state.charAt(2) - 1))
                && rightOpen(surroundings)
        );
    }

    private Boolean mobilityLeftCycle(String state, Map<DirectionType, ChemicalCell> surroundings) {
        return (state.charAt(1) == '-' && state.charAt(2) >= '1' && leftOpen(surroundings));
    }

    private Boolean leftOpen(Map<DirectionType, ChemicalCell> surroundings) {
        return surroundings.get(DirectionType.NORTH).isOpen();
    }

    private Boolean mobilityRight(String state, Map<DirectionType, ChemicalCell> surroundings) {
        return (state.charAt(1) == '+'
                && ((state.charAt(8) == 'C' && state.charAt(0) <= state.charAt(2) - 1) 
                    || (state.charAt(8) == 'R' && state.charAt(0) == state.charAt(2) - 1))
                && rightOpen(surroundings)
        );
    }

    private Boolean mobilityRightCycle(String state, Map<DirectionType, ChemicalCell> surroundings) {
        return (state.charAt(1) == '+' && state.charAt(2) >= '1' && rightOpen(surroundings));
    }

    private Boolean rightOpen(Map<DirectionType, ChemicalCell> surroundings) {
        return surroundings.get(DirectionType.SOUTH).isOpen();
    }

    private Boolean blocked(String state, Map<DirectionType, ChemicalCell> surroundings) {
        return (followingWall(state) || blockedInX(surroundings) || blockedInY(surroundings));
    }
    
    /*
    // wall UDLR ±  X [.*] ±  Y [.*] W
    //  Y    R   +  X  *   +  Y  .   W
    //  0    1   2  3  4   5  6  7   8 
    private char[] moveWithBlock(String prevState, Map<DirectionType, ChemicalCell> surroundings) {
        // TODO: will this cause a problem in corners, the agent doesnt know what wall it is following 
        //       You can make it follow past move UDLR before resorting to major axis?
        
        // if you previously were blocked in any axis but are now open 
        if (followingWall(prevState) && !blockedInX(surroundings) && !blockedInY(surroundings)) {
            // conversion back into regular state string 
            if (getMajorAxis(prevState) == 'Y') {
                return {'0', prevState.charAt(2), '1', '.', '0', prevState.charAt(5), '2', '*', 'C'};
            }
            return {'0', prevState.charAt(2), '2', '*', '0', prevState.charAt(5), '1', '.', 'C'};
        }

        // if you previously were blocked in both 

        // were following a wall blocked in Y, now blocked in X somehow 
        else if (followingWall(prevState) && prevState.charAt(0) == 'Y' && !blockedInY(surroundings)) { 
            // take the movement from before and apply it to you now if possible
            if (getMajorAxis(prevState) == 'Y') {
                // try moving in that y dir
                if (prevState.charAt(2) == '+' && prevState.charAt(5) == '+') { // +X +Y
                    if (upOpen(surroundings)) { 
                        return {'X', 'U', '+', 'X', '.', '+', 'Y', '*', 'W'};
                    }
                    else if (rightOpen(surroundings)) { 
                        return {'X', 'R', '+', 'X', '.', '+', 'Y', '*', 'W'};
                    }
                    else {
                        if (prevState.charAt(1) == 'L') {
                            if (downOpen(surroundings)) { 
                                return {'X', 'D', '+', 'X', '.', '+', 'Y', '*', 'W'};
                            }
                            else { // going  left 
                                return {'X', 'L', '+', 'X', '.', '+', 'Y', '*', 'W'};
                            }
                        }
                        else { // (prevState.charAt(1) == 'D'
                            if (leftOpen(surroundings)) { 
                                return {'X', 'L', '+', 'X', '.', '+', 'Y', '*', 'W'};
                            }
                            else { // going down
                                return {'X', 'D', '+', 'X', '.', '+', 'Y', '*', 'W'};
                            }
                        }
                    }
                }
                else if (prevState.charAt(2) == '+' && prevState.charAt(5) == '-') { // +X -Y
                    if (downOpen(surroundings)) { 
                        return {'X', 'D', '+', 'X', '.', '-', 'Y', '*', 'W'};
                    }
                    else if (rightOpen(surroundings)) { 
                        return {'X', 'R', '+', 'X', '.', '-', 'Y', '*', 'W'};
                    }
                    else {
                        if (prevState.charAt(1) == 'L') {
                            if (upOpen(surroundings)) { 
                                return {'X', 'U', '+', 'X', '.', '-', 'Y', '*', 'W'};
                            }
                            else { // going  left 
                                return {'X', 'L', '+', 'X', '.', '-', 'Y', '*', 'W'};
                            }
                        }
                        else { // prevState.charAt(1) == 'U'
                            if (leftOpen(surroundings)) { 
                                return {'X', 'L', '+', 'X', '.', '-', 'Y', '*', 'W'};
                            }
                            else { // going up
                                return {'X', 'U', '+', 'X', '.', '-', 'Y', '*', 'W'};
                            }
                        }
                    }
                }
                if (prevState.charAt(2) == '-' && prevState.charAt(5) == '+') { // -X +Y
                    if (upOpen(surroundings)) { 
                        return {'X', 'U', '-', 'X', '.', '+', 'Y', '*', 'W'};
                    }
                    else if (leftOpen(surroundings)) { 
                        return {'X', 'L', '-', 'X', '.', '+', 'Y', '*', 'W'};
                    }
                    else {
                        if (prevState.charAt(1) == 'D') {
                            if (rightOpen(surroundings)) { 
                                return {'X', 'R', '-', 'X', '.', '+', 'Y', '*', 'W'};
                            }
                            else { // going  down 
                                return {'X', 'D', '-', 'X', '.', '+', 'Y', '*', 'W'};
                            }
                        }
                        else { // prevState.charAt(1) == 'R'
                            if (downOpen(surroundings)) { 
                                return {'X', 'D', '-', 'X', '.', '+', 'Y', '*', 'W'};
                            }
                            else { // going right
                                return {'X', 'R', '-', 'X', '.', '+', 'Y', '*', 'W'};
                            }
                        }
                    }
                }
                else { // -X -Y
                    if (downOpen(surroundings)) { 
                        return {'X', 'D', '-', 'X', '.', '-', 'Y', '*', 'W'};
                    }
                    else if (leftOpen(surroundings)) { 
                        return {'X', 'L', '-', 'X', '.', '-', 'Y', '*', 'W'};
                    }
                    else {
                        if (prevState.charAt(1) == 'R') {
                            if (upOpen(surroundings)) { 
                                return {'X', 'U', '-', 'X', '.', '-', 'Y', '*', 'W'};
                            }
                            else { // going  right 
                                return {'X', 'R', '-', 'X', '.', '-', 'Y', '*', 'W'};
                            }
                        }
                        else { // prevState.charAt(1) == 'U'
                            if (rightOpen(surroundings)) { 
                                return {'X', 'R', '-', 'X', '.', '-', 'Y', '*', 'W'};
                            }
                            else { // going up
                                return {'X', 'U', '-', 'X', '.', '-', 'Y', '*', 'W'};
                            }
                        }
                    }
                }
            }

            else { // major movement is in X direction 
                // try moving in that y dir
                if (prevState.charAt(2) == '+' && prevState.charAt(5) == '+') { // +X +Y
                    if (rightOpen(surroundings)) { 
                        return {'X', 'R', '+', 'X', '*', '+', 'Y', '.', 'W'};
                    }
                    else if (upOpen(surroundings)) { 
                        return {'X', 'U', '+', 'X', '*', '+', 'Y', '.', 'W'};
                    }
                    else {
                        if (prevState.charAt(1) == 'L') {
                            if (downOpen(surroundings)) { 
                                return {'X', 'D', '+', 'X', '*', '+', 'Y', '.', 'W'};
                            }
                            else { // going  left 
                                return {'X', 'L', '+', 'X', '*', '+', 'Y', '.', 'W'};
                            }
                        }
                        else { // (prevState.charAt(1) == 'D'
                            if (leftOpen(surroundings)) { 
                                return {'X', 'L', '+', 'X', '*', '+', 'Y', '.', 'W'};
                            }
                            else { // going down
                                return {'X', 'D', '+', 'X', '*', '+', 'Y', '.', 'W'};
                            }
                        }
                    }
                }
                else if (prevState.charAt(2) == '+' && prevState.charAt(5) == '-') { // +X -Y
                    if (rightOpen(surroundings)) { 
                        return {'X', 'R', '+', 'X', '*', '-', 'Y', '.', 'W'};
                    }
                    else if (downOpen(surroundings)) { 
                        return {'X', 'D', '+', 'X', '*', '-', 'Y', '.', 'W'};
                    }
                    else {
                        if (prevState.charAt(1) == 'L') {
                            if (upOpen(surroundings)) { 
                                return {'X', 'U', '+', 'X', '*', '-', 'Y', '.', 'W'};
                            }
                            else { // going  left 
                                return {'X', 'L', '+', 'X', '*', '-', 'Y', '.', 'W'};
                            }
                        }
                        else { // prevState.charAt(1) == 'U'
                            if (leftOpen(surroundings)) { 
                                return {'X', 'L', '+', 'X', '*', '-', 'Y', '.', 'W'};
                            }
                            else { // going up
                                return {'X', 'U', '+', 'X', '*', '-', 'Y', '.', 'W'};
                            }
                        }
                    }
                }
                if (prevState.charAt(2) == '-' && prevState.charAt(5) == '+') { // -X +Y
                    if (leftOpen(surroundings)) { 
                        return {'X', 'L', '-', 'X', '*', '+', 'Y', '.', 'W'};
                    }
                    else if (upOpen(surroundings)) { 
                        return {'X', 'U', '-', 'X', '*', '+', 'Y', '.', 'W'};
                    }
                    else {
                        if (prevState.charAt(1) == 'D') {
                            if (rightOpen(surroundings)) { 
                                return {'X', 'R', '-', 'X', '*', '+', 'Y', '.', 'W'};
                            }
                            else { // going  down 
                                return {'X', 'D', '-', 'X', '*', '+', 'Y', '.', 'W'};
                            }
                        }
                        else { // prevState.charAt(1) == 'R'
                            if (downOpen(surroundings)) { 
                                return {'X', 'D', '-', 'X', '*', '+', 'Y', '.', 'W'};
                            }
                            else { // going right
                                return {'X', 'R', '-', 'X', '*', '+', 'Y', '.', 'W'};
                            }
                        }
                    }
                }
                else { // -X -Y
                    if (leftOpen(surroundings)) { 
                        return {'X', 'L', '-', 'X', '*', '-', 'Y', '.', 'W'};
                    }
                    else if (downOpen(surroundings)) { 
                        return {'X', 'D', '-', 'X', '*', '-', 'Y', '.', 'W'};
                    }
                    else {
                        if (prevState.charAt(1) == 'R') {
                            if (upOpen(surroundings)) { 
                                return {'X', 'U', '-', 'X', '*', '-', 'Y', '.', 'W'};
                            }
                            else { // going  right 
                                return {'X', 'R', '-', 'X', '*', '-', 'Y', '.', 'W'};
                            }
                        }
                        else { // prevState.charAt(1) == 'U'
                            if (rightOpen(surroundings)) { 
                                return {'X', 'R', '-', 'X', '*', '-', 'Y', '.', 'W'};
                            }
                            else { // going up
                                return {'X', 'U', '-', 'X', '*', '-', 'Y', '.', 'W'};
                            }
                        }
                    }
                }
            }
        }   

        // were following a wall blocked in X, now blocked in Y somehow 
        else if (followingWall(prevState) && prevState.charAt(0) == 'X' && !blockedInX(surroundings) { 

        }


        // if you were previously blocked and still are 
            // which axis were you blocked in?
                X -> X
                  -> Y
                  -> XY 
                //   -> none
                Y -> X
                  -> Y
                  -> XY
                //   -> none
                XY -> X
                  -> Y
                  -> XY 
                //   -> none
                None -> X
                  -> Y
                  -> XY
                //   -> none 
            // which axis are you blocked in?
            // both
                // turn around, follow previous wall in opposite direction
            // new axis, but not old
                // follow axis in preferable directoin if possible
            // same axis
                // continue on your way 
        
        // if you were not blocked before and now you are 
        if (!followingWall(prevState) && blockedInX(surroundings)) {
            // see if you can continue moving in your position position
                // if not, translate to blocked language 
                // if so, move in that direction and keep going 
            

            // try to go where you want
            // try to go somewhere else that you want
            // try not to go back from where you came
            // go back from where you came 
        }

        if (!followingWall(prevState) && blockedInY(surroundings)) {

        }

        // if moving in perpendicular manner (regardless of your previous movements) 
        // and not don't know which direction to move in now that you've hit something 
            // is there a way to move that isn't where you came? 
            // random 
            

    
    
        
        return nextState;
    }
    */

    private DirectionType blockMoveDirection(char[] nextState) {
        if (nextState[8] == 'W') {
            if (nextState[1] == 'R') return DirectionType.SOUTH;
            else if (nextState[1] == 'L') return DirectionType.NORTH;
            else if (nextState[1] == 'U') return DirectionType.EAST;
            else  return DirectionType.WEST;
        }
        else {
            if (nextState[3] == '*' && nextState[7] == '.') {
                if (nextState[1] == '+') return DirectionType.SOUTH;
                else return DirectionType.NORTH;
            }
            else {
                if (nextState[5] == '+') return DirectionType.EAST;
                else return DirectionType.WEST;
            }
        }
    }

    private Boolean blockedInX(Map<DirectionType, ChemicalCell> surroundings) {
        return (!rightOpen(surroundings) || !leftOpen(surroundings));
    }

    private Boolean blockedInY(Map<DirectionType, ChemicalCell> surroundings) {
        return (!upOpen(surroundings) || !downOpen(surroundings));
    }

    // creates new state string according to translation laws, preserving direction
    private char[] moveInX(char[] nextState, String prevState) {
        if (needsToRepeat(prevState)) {
            nextState = new char[] { '0', prevState.charAt(1), prevState.charAt(2), '*',
                                     '0', prevState.charAt(5), prevState.charAt(6), '.', 'C'};
            if (nextState[2] == '0' || nextState[6] == '0')
                nextState[8] = 'R';
        }
        else {
            nextState = new char[] { (char)((int) prevState.charAt(0) + 1), prevState.charAt(1), prevState.charAt(2), '*',
                                     prevState.charAt(4), prevState.charAt(5), prevState.charAt(6), '.', 'C'};
            if ((nextState[4] == (char)((int)nextState[6] - 1) && nextState[0] == nextState[2]) ||
                (nextState[0] == (char)((int)nextState[2] - 1) && nextState[4] == nextState[6]))
                nextState[8] = 'R';
        } 
        if (!trans.validByte(nextState)) {
            simPrinter.println("Invalid state was created: " + nextState);
            nextState = new char[] { '0', prevState.charAt(1), prevState.charAt(2), '*',
                                     '0', prevState.charAt(5), prevState.charAt(6), '.', 'C'};
            if (nextState[2] == '0' || nextState[6] == '0')
                nextState[8] = 'R';
        }
        return nextState;
    }

    private char[] moveInY(char[] nextState, String prevState) {
        if (needsToRepeat(prevState)) {
            nextState = new char[] { '0', prevState.charAt(1), prevState.charAt(2), '.',
                                     '0', prevState.charAt(5), prevState.charAt(6), '*', 'C'};
            if (nextState[2] == '0' || nextState[6] == '0')
                nextState[8] = 'R';
        }
        else {
            nextState = new char[] { prevState.charAt(0), prevState.charAt(1), prevState.charAt(2), '.',
                                     (char)((int) prevState.charAt(4) + 1), prevState.charAt(5), prevState.charAt(6), '*', 'C'};
            if ((nextState[4] == (char)((int)nextState[6] - 1) && nextState[0] == nextState[2]) ||
                (nextState[0] == (char)((int)nextState[2] - 1) && nextState[4] == nextState[6]))
                nextState[8] = 'R';
        }
        if (!trans.validByte(nextState)) {
            simPrinter.println("Invalid state was created: " + nextState);
            nextState = new char[] { '0', prevState.charAt(1), prevState.charAt(2), '.',
                                     '0', prevState.charAt(5), prevState.charAt(6), '*', 'C'};
            if (nextState[2] == '0' || nextState[6] == '0')
                nextState[8] = 'R';
        }
        return nextState;
    }

    private Boolean needsToRepeat(String state) {
        return state.charAt(8) == 'R';
    }

    private Boolean followingWall(String state) {
        return state.charAt(8) == 'W';
    }

    private char getMajorAxis(String state) {
        if (state.charAt(8) == 'R' || state.charAt(8) == 'C') {
            if (state.charAt(2) > state.charAt(6)) return 'X';
            else if (state.charAt(2) < state.charAt(6)) return 'Y';
            else if (rand >= 0) return 'X';
            return 'Y';
        }
        else if (state.charAt(8) == 'W') {
            if (state.charAt(4) == '*') return 'X';
            return 'Y';
        }
        else {
            if (rand >= 0) return 'X';
            return 'Y';
        }
    }
    

}