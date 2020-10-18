package chemotaxis.g3;

import java.awt.Point;
import java.util.List;
import java.util.ArrayList;

import chemotaxis.sim.ChemicalPlacement;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.SimPrinter;

import chemotaxis.g3.Language.Translator;
import chemotaxis.g3.PathFinder;

public class Controller extends chemotaxis.sim.Controller {
    
    Point lastPoint = new Point(-1,-1);
    private Translator trans = null;
    private String lastPlacement = null;

    private List<Point> path = null;
    private Point targetLocation = null;
    private int steppingStone = 0;

    /**
     * Controller constructor
     *
     * @param start       start cell coordinates
     * @param target      target cell coordinates
     * @param size     	  grid/map size
     * @param simTime     simulation time
     * @param budget      chemical budget
     * @param seed        random seed
     * @param simPrinter  simulation printer
     *
     */
	public Controller(Point start, Point target, Integer size, Integer simTime, Integer budget, Integer seed, SimPrinter simPrinter) {
        super(start, target, size, simTime, budget, seed, simPrinter);
        this.trans = Translator.getInstance();
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
 	@Override
	public ChemicalPlacement applyChemicals(Integer currentTurn, Integer chemicalsRemaining, Point currentLocation, ChemicalCell[][] grid) {
        // TODO implement pause, calculate new path when agent is confused 
        // TODO: instruct agent to make it to open field 
        // TODO: use isPerfectAngle to see if there is a perfect path 
        //       that's more direct than the current trajectory 
        //       and has no obstacles 
        // TODO: find obstacles in path
        // TODO: get the agent to the best spot for the best instruction
            // if going diagonal is key, get them to where diagonal movement is allowed 

        simPrinter.println("\nRound:" + currentTurn);

        // find path
        if (path == null) {
            path = PathFinder.getPath(start, target, grid, size);
            path = PathFinder.cleanPath(path);
            targetLocation = path.get(steppingStone++);
            // PathFinder.triPath(path);
        }

        // cell's current location
        int currentX = currentLocation.x;
        int currentY = currentLocation.y;

        // check to see if we have made it where we need to
        if (currentLocation.equals(targetLocation)) {
            targetLocation = path.get(steppingStone++);
        }

        // TODO: check to see if the agent is already on the right track 
        // TODO: check to see if the agent is on a wall?
        // TODO: check to see if the agent went in the wrong direction 

        // calculate angle between agent and target 
        double angle = Math.toDegrees(Math.atan2(targetLocation.y - currentY, targetLocation.x - currentX));

        if (angle < 0) 
            angle += 360;

        // Pass angle into language --> returns with where to place colors
        String placements = trans.getColor(angle);
        // simPrinter.println("Calculated angle is: " + angle + " degrees.");
        // simPrinter.println("Placing new chemical: " + placements);

        // simPrinter.println("\ncurrent turn: " + currentTurn);
        if (lastPoint.equals(currentLocation) 
            || lastPoint.equals(new Point(-1,-1)) 
            || ((angle%90 == 0) && !(placements.equals(lastPlacement))) ) {
            // simPrinter.println("Agent did not move in turn " + (currentTurn - 1) );
            lastPlacement = placements;
            lastPoint.setLocation(currentX, currentY);

            ChemicalPlacement chemicalPlacement = new ChemicalPlacement();
            List<ChemicalType> chemicals = new ArrayList<>();

            // Break apart colors to see where to place, ex => "d_GB"
            if (placements.charAt(0) == 'u') 
                chemicalPlacement.location = new Point(currentX, currentY+1);
            else if (placements.charAt(0) == 'd') 
                chemicalPlacement.location = new Point(currentX, currentY-1);
            else if (placements.charAt(0) == 'l') 
                chemicalPlacement.location = new Point(currentX-1, currentY);
            else if (placements.charAt(0) == 'r') 
                chemicalPlacement.location = new Point(currentX+1, currentY);
            else 
                chemicalPlacement.location = new Point(currentX, currentY);

            if (placements.charAt(1) == 'R') 
                chemicals.add(ChemicalType.RED);
            if (placements.charAt(2) == 'G') 
                chemicals.add(ChemicalType.GREEN);
            if (placements.charAt(3) == 'B') 
                chemicals.add(ChemicalType.BLUE);
        
            chemicalPlacement.chemicals = chemicals;
            
            return chemicalPlacement;
        }
        
        lastPoint.setLocation(currentX, currentY);
        return new ChemicalPlacement();
    } 	

}
