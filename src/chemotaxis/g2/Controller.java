package chemotaxis.g2;

import java.awt.*;
import java.util.*;

import chemotaxis.sim.*;

public class Controller extends chemotaxis.sim.Controller {
    private final DirectionType INITIAL_AGENT_DIR = DirectionType.NORTH;
    private ArrayList<Point> shortestPath;
    private ArrayList<Map.Entry<Point, DirectionType>> turns;
    private Queue<Map.Entry<Point, MoveType>> movesQueue;
    private DirectionType prevDir;
    private Point prevLocation;
    /**
     * Controller constructor
     *
     * @param start       start cell coordinates
     * @param target      target cell coordinates
     * @param size     	 grid/map size
     * @param simTime     simulation time
     * @param budget      chemical budget
     * @param seed        random seed
     * @param simPrinter  simulation printer
     *
     */
    public Controller(Point start, Point target, Integer size, Integer simTime, Integer budget, Integer seed, SimPrinter simPrinter) {
        super(start, target, size, simTime, budget, seed, simPrinter);
        this.prevDir = INITIAL_AGENT_DIR;
        this.movesQueue = new LinkedList<>();
    }

    /**
     * Apply chemicals to the map
     *
     * @param currentTurn         current turn in the simulation
     * @param chemicalsRemaining  number of chemicals remaining
     * @param currentLocation     current location of the agent
     * @param grid                game grid/map
     * @return                    a cell location and list of chemicals to apply
     *
     */
    // TODO: update so that it reads from this.movesList instead
    @Override
    public ChemicalPlacement applyChemicals(Integer currentTurn, Integer chemicalsRemaining, Point currentLocation, ChemicalCell[][] grid) {
        simPrinter.println("Turn #" + currentTurn.toString());
        simPrinter.println("Location: " + currentLocation.toString());

        if (currentTurn == 1) {
            shortestPath = getShortestPath(grid);
            turns = getTurnsList();
            turnsListToMovesQueue(turns);
            applyAgentDefaultFilerToMovesList(grid);
            prevLocation = currentLocation;
        }
        updateAgentAttributes(currentLocation, currentTurn);

        ChemicalPlacement cp = new ChemicalPlacement();
        if (movesQueue.isEmpty()) {
            cp.location = currentLocation;
            return cp;
        }

        Map.Entry<Point, MoveType> moveEntry = movesQueue.peek();
        Point movePoint = moveEntry.getKey();

        if (movePoint.equals(currentLocation)) {
            // TODO: handle zig zag MoveType
            MoveType nextMove = moveEntry.getValue();
            DirectionType direction = moveToDirectionType(nextMove);
            cp.location = adjPoint(movePoint, direction);
            cp.chemicals.add(ChemicalCell.ChemicalType.RED);
            movesQueue.poll();
        }
        return cp;

        /*if (turns.size() == 0) {
            cp.location = currentLocation;
            return cp;
        }

        Map.Entry<Point, DirectionType> nextTurn = turns.get(0);
        simPrinter.println("Next turn: " + nextTurn.toString());
        if (nextTurn.getKey().equals(currentLocation)) {
            simPrinter.println("POINTS EQL");
            if (prevDir == nextTurn.getValue()) {
                simPrinter.print("prevDir: " + prevDir.toString() + " equals " + nextTurn.getValue());
                turns.remove(0);
                return cp;
            }
            else if (currentTurn == 1 || chemicalIsRequiredForTurn(currentLocation, grid)) {
                Point point = nextTurn.getKey();
                DirectionType direction = nextTurn.getValue();
                cp.location = adjPoint(point, direction);
                cp.chemicals.add(ChemicalCell.ChemicalType.RED);
                turns.remove(0);
            }
        }
        simPrinter.println("Next move: " + cp.toString());
        return cp;*/
    }

    private DirectionType moveToDirectionType(MoveType moveType) {
        switch (moveType) {
            case N:
                return DirectionType.NORTH;
            case E:
                return DirectionType.EAST;
            case S:
                return DirectionType.SOUTH;
            default:
                return DirectionType.WEST;
        }
    }

    private MoveType directionToMoveType(DirectionType dir) {
        switch (dir) {
            case NORTH:
                return MoveType.N;
            case EAST:
                return MoveType.E;
            case SOUTH:
                return MoveType.S;
            default:
                return MoveType.W;
        }
    }

    // TODO:
    private void updateMovesQueue(ArrayList<Point> path, ChemicalCell[][] grid) {
        ArrayList<Map.Entry<Point, DirectionType>> turnsList = getTurnsList();
        turnsListToMovesQueue(turnsList);
        applyAgentDefaultFilerToMovesList(grid);
        applyZigZagFilterToMovesList(grid);
    }

    private void turnsListToMovesQueue(ArrayList<Map.Entry<Point, DirectionType>> turnsList) {
        movesQueue = new LinkedList<>();
        for (Map.Entry<Point, DirectionType> turnEntry: turnsList) {
            Point point = turnEntry.getKey();
            MoveType moveType = directionToMoveType(turnEntry.getValue());
            movesQueue.add(new AbstractMap.SimpleEntry(point, moveType));
        }
    }

    // TODO: Joe
    private void applyAgentDefaultFilerToMovesList(ChemicalCell[][] grid) {
        MoveType agentPrevMove = MoveType.N;
        MoveType agentPrevOrthMove = MoveType.E;

        Iterator<Map.Entry<Point, MoveType>> it = movesQueue.iterator();
        while (it.hasNext()) {
            Map.Entry<Point, MoveType> moveEntry = it.next();
            Point point = moveEntry.getKey();
            MoveType moveToMake = moveEntry.getValue();
            ArrayList<MoveType> possibleMoves = new ArrayList<>();

            Point westPoint = new Point(point.x, point.y - 1);
            if (pointIsOpen(westPoint, grid)) {
                possibleMoves.add(MoveType.W);
            }
            Point eastPoint = new Point(point.x, point.y + 1);
            if (pointIsOpen(eastPoint, grid)) {
                possibleMoves.add(MoveType.E);
            }
            Point southPoint = new Point(point.x + 1, point.y);
            if (pointIsOpen(southPoint, grid)) {
                possibleMoves.add(MoveType.S);
            }
            Point northPoint = new Point(point.x - 1, point.y);
            if (pointIsOpen(northPoint, grid)) {
                possibleMoves.add(MoveType.N);
            }

            MoveType agentDefaultMove = getAgentsDefaultMove(agentPrevMove, agentPrevOrthMove, possibleMoves);
            if (agentDefaultMove == moveToMake) {
                it.remove();
            }

            // reset agents prev moves
            if (agentPrevMove == MoveType.N || agentPrevMove == MoveType.S) {
                if (moveToMake == MoveType.E || moveToMake == MoveType.W) {
                    agentPrevOrthMove = agentPrevMove;
                }
            }
            else {
                if (moveToMake == MoveType.N || moveToMake == MoveType.S) {
                    agentPrevOrthMove = agentPrevMove;
                }
            }
            agentPrevMove = moveToMake;
        }
    }

    private boolean pointIsOpen(Point p, ChemicalCell[][] grid) {
        int rowIndex = p.x - 1;
        int colIndex = p.y - 1;

        if (rowIndex >= 0 && colIndex >= 0 && rowIndex < grid.length && colIndex < grid.length) {
            ChemicalCell cell = grid[rowIndex][colIndex];
            return cell.isOpen();
        }
        return false;
    }

    private MoveType getAgentsDefaultMove(MoveType agentPrevMove,
                                               MoveType agentPrevOrthMove,
                                               ArrayList<MoveType> possibleMoves) {
        if (possibleMoves.contains(agentPrevMove)) {
            return agentPrevMove;
        }

        if (possibleMoves.contains(agentPrevOrthMove)) {
            return agentPrevOrthMove;
        }

        MoveType oppOfPrevOrthMove = getOppositeMove(agentPrevOrthMove);
        if (possibleMoves.contains(oppOfPrevOrthMove)) {
            return oppOfPrevOrthMove;
        }

        simPrinter.println("Error in getAgentsDefaultMove => agent is repeating points!");
        return getOppositeMove(agentPrevMove);
    }

    // TODO:
    private void applyZigZagFilterToMovesList(ChemicalCell[][] grid) {

    }

    private void updateAgentAttributes(Point currentLocation, Integer currentTurn) {
        int xDiff = currentLocation.x - this.prevLocation.x;
        int yDiff = currentLocation.y - this.prevLocation.y;

        if (currentTurn == 1) {
            this.prevDir = DirectionType.NORTH;
        }
        else if (yDiff == -1) {
            this.prevDir = DirectionType.WEST;
        }
        else if (yDiff == 1) {
            this.prevDir = DirectionType.EAST;
        }
        else if (xDiff == -1) {
            this.prevDir = DirectionType.NORTH;
        }
        else if (xDiff == 1) {
            this.prevDir = DirectionType.SOUTH;
        }
        else {
            this.prevDir = DirectionType.CURRENT;
        }
        this.prevLocation = currentLocation;
    }

    private boolean chemicalIsRequiredForTurn(Point currentLocation, ChemicalCell[][] grid) {
        return true;
    }

    private Map<DirectionType, ChemicalCell.ChemicalType> getChemicalDirections() {
        Map<DirectionType, ChemicalCell.ChemicalType> chemicalDirs = new HashMap<>();
        // order of elements chemicalTypes and directionTypes is critical to making sure
        // values map correctly for both agent and controller
        ChemicalCell.ChemicalType[] chemicalTypes = {
                ChemicalCell.ChemicalType.RED,
                ChemicalCell.ChemicalType.GREEN,
                ChemicalCell.ChemicalType.BLUE
        };

        DirectionType[] directionTypes = {
                DirectionType.NORTH,
                DirectionType.EAST,
                DirectionType.SOUTH,
                DirectionType.WEST
        };

        int dirIndex = 0;
        for (int i = 0; i < chemicalTypes.length; i++) {
            if (directionTypes[dirIndex] == prevDir) {
                dirIndex++;
            }
            chemicalDirs.put(directionTypes[dirIndex], chemicalTypes[i]);
            dirIndex++;
        }
        return chemicalDirs;
    }

    // TODO:
    private ArrayList<Point> getOptimalPath() {
        //
        return null;
    }

    // source: https://www.techiedelight.com/lee-algorithm-shortest-path-in-a-maze/
    private ArrayList<Point> getShortestPath(ChemicalCell[][] grid) {
        // after finding shortest path:
        // if (pathCost > budget) => continue to next shortest path

        int[] rowPosMovs = { -1, 0, 0, 1 };
        int[] colPosMovs = { 0, -1, 1, 0 };
        // up, left, right, down

        // construct a matrix to keep track of visited cells
        boolean[][] visited = new boolean[size][size];
        Point[][] prevCell = new Point[size][size];

        // create an empty queue
        Queue<Node> q = new ArrayDeque<>();

        int i = (int) start.getX()-1;
        int j = (int) start.getY()-1;


        // mark source cell as visited and enqueue the source node
        visited[i][j] = true;
        prevCell[i][j] = null;
        q.add(new Node(i, j , 0));

        // stores length of longest path from source to destination
        int min_dist = Integer.MAX_VALUE;

        // loop till queue is empty
        while (!q.isEmpty())
        {
            // pop front node from queue and process it
            Node node = q.poll();

            // (i, j) represents current cell and dist stores its
            // minimum distance from the source
            i = node.x;
            j = node.y;
            int dist = node.dist;

            // if destination is found, update min_dist and stop
            if (i == (int) target.getX()-1 && j == (int) target.getY()-1)
            {
                min_dist = dist;
                break;
            }

            
            // get previous move. (i's parent). move in same direction first.
            if (!(i == (int) start.getX()-1 && j==(int) start.getY()-1)){ 
                Point grandparent = prevCell[i][j];
                int directionR = i - (int) (grandparent.getX() - 1.0);
                int directionC = j - (int) (grandparent.getY() - 1.0);

                if (directionR == 0) {
                    if (directionC == 1){
                        rowPosMovs[0] = 0;
                        colPosMovs[0] = 1;
                        rowPosMovs[1] = -1;
                        colPosMovs[1] = 0;
                        rowPosMovs[2] = 0;
                        colPosMovs[2] = -1;
                        rowPosMovs[3] = 1;
                        colPosMovs[3] = 0;
                    }
                    else if (directionC == -1){
                        rowPosMovs[0] = 0;
                        colPosMovs[0] = -1;
                        rowPosMovs[1] = -1;
                        colPosMovs[1] = 0;
                        rowPosMovs[2] = 0;
                        colPosMovs[2] = 1;
                        rowPosMovs[3] = 1;
                        colPosMovs[3] = 0;
                    }
                } 
                else if (directionC == 0) {
                    if (directionR == 1){
                        rowPosMovs[0] = 1;
                        colPosMovs[0] = 0;
                        rowPosMovs[1] = -1;
                        colPosMovs[1] = 0;
                        rowPosMovs[2] = 0;
                        colPosMovs[2] = -1;
                        rowPosMovs[3] = 0;
                        colPosMovs[3] = 1;
                    }
                    else if (directionR == -1){
                        rowPosMovs[0] = -1;
                        colPosMovs[0] = 0;
                        rowPosMovs[1] = 1;
                        colPosMovs[1] = 0;
                        rowPosMovs[2] = 0;
                        colPosMovs[2] = -1;
                        rowPosMovs[3] = 0;
                        colPosMovs[3] = 1;
                    }
                }
            }

            // check for all 4 possible movements from current cell
            // and enqueue each valid movement
            for (int k = 0; k < 4; k++)
            {
                // check if it is possible to go to position
                // (i + row[k], j + col[k]) from current position
                if (isValid(grid, visited, i + rowPosMovs[k], j + colPosMovs[k]))
                {
                    // mark next cell as visited and enqueue it

                    visited[i + rowPosMovs[k]][j + colPosMovs[k]] = true;
                    prevCell[i + rowPosMovs[k]][j + colPosMovs[k]] = new Point(i + 1, j + 1);
                    q.add(new Node(i + rowPosMovs[k], j + colPosMovs[k], dist + 1));
                }
            }
        }

        if (min_dist != Integer.MAX_VALUE) {
            ArrayList<Point> sp = new ArrayList<Point>();
            sp.add(target);
            Point cur = prevCell[(int) target.getX()-1][(int) target.getY()-1];    
            while (cur != null){
                sp.add(0, cur);
                cur = prevCell[(int) cur.getX()-1][(int) cur.getY()-1];
            }
            return sp;
        }
        else {
            System.out.println("Destination can't be reached from given source");
            // what to do in else???
        }

        return null;
    }

    // TODO:
    private ArrayList<Point> getLeastTurnsPath(Point start, Point end, ChemicalCell[][] grid) {
        return null;
    }

    private boolean isValid(ChemicalCell grid[][], boolean visited[][], int i, int j) {
        return (i >= 0) && (i < size) && (j >= 0) && (j < size) && grid[i][j].isOpen() && !visited[i][j];
    }


    private ArrayList<Map.Entry<Point, DirectionType>> getTurnsList() {
       //to print shortest path
        /* System.out.println("shortest path is ");
        for(Point pt : shortestPath) {
            System.out.println(pt);
        }*/

        ArrayList<Map.Entry<Point, DirectionType>> turns = new ArrayList<>();

        //edge case
        if(shortestPath.size() < 2) {
            return turns;
        }

        //first direction
        DirectionType lastDir = findDirection(shortestPath.get(0), shortestPath.get(1));
        turns.add(new AbstractMap.SimpleEntry(shortestPath.get(0), lastDir));

        //for each point, determine direction to next point
        for(int i = 1; i < shortestPath.size()-1; i++) {
            DirectionType curDir = findDirection(shortestPath.get(i), shortestPath.get(i+1));

            //if the direction changed, record the point and the new direction
            if(lastDir != curDir) {
                turns.add(new AbstractMap.SimpleEntry(shortestPath.get(i), curDir));
                lastDir = curDir;
            }
        }

        //to print the turns list
        /*System.out.println("turns list is ");
        for(Map.Entry<Point, DirectionType> turn: turns) {
            System.out.println(turn.getKey() + ":  " + turn.getValue());
        }*/

        return turns;
    }

    //from start and end points, determine direction
    private DirectionType findDirection(Point start, Point end) {
        //up
        if(start.getX() < end.getX()) {
            return DirectionType.SOUTH;
        }

        //down
        if(start.getX() > end.getX()) {
            return DirectionType.NORTH;
        }

        //right
        if(start.getY() < end.getY()) {
            return DirectionType.EAST;
        }

        //left
        if(start.getY() > end.getY()) {
            return DirectionType.WEST;
        }

        return DirectionType.CURRENT;
    }

    private Point adjPoint(Point point, DirectionType dir) {
        Point adjacent = new Point();
        adjacent.x = point.x;
        adjacent.y = point.y;
        switch (dir) {
            case NORTH:
                adjacent.x = point.x - 1;
                break;
            case SOUTH:
                adjacent.x = point.x + 1;
                break;
            case EAST:
                adjacent.y = point.y + 1;
                break;
            case WEST:
                adjacent.y = point.y - 1;
                break;
        }
        simPrinter.println("Adj: " + adjacent.toString());
        return adjacent;
    }

    private MoveType getOppositeMove(MoveType moveType) {
        switch (moveType) {
            case N:
                return MoveType.S;
            case S:
                return MoveType.N;
            case W:
                return MoveType.E;
            case E:
                return MoveType.W;
            default:
                simPrinter.println("ERROR in getOppositeMove: returning default MoveType.S");
                return MoveType.S;
        }
    }

    private int getPathCost() {
        return turns.size();
    }

    private class Node {

        // (x, y) represents matrix cell coordinates
        // dist represent its minimum distance from the source
        private int x;
        private int y; 
        private int dist;

        Node(int x, int y, int dist) {
            this.x = x;
            this.y = y;
            this.dist = dist;
        }
    }

    private void printColorMap(Map<DirectionType, ChemicalCell.ChemicalType> chemDirs) {
        for (Map.Entry<DirectionType, ChemicalCell.ChemicalType> chemDir: chemDirs.entrySet()) {
            simPrinter.println(chemDir.getKey().toString() + ": " + chemDir.getValue().toString());
        }
    }
}
