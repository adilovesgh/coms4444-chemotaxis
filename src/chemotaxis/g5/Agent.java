package chemotaxis.g5;

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

<<<<<<< HEAD
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
		
		Move move = new Move();

		/*
		 * Bit flags:
		 * 0: Last Move: 	0 =	North 	1 =	South
		 * 1: Last Move: 	0 =	East 	1 =	West
		 * 2: Color Type:	[0		[1		[0		 [1
		 * 3: Color Type: 	 0] Red 0] Blue 1] Green 1] Any
		 * 4: undecided
		 * 5: undecided
		 * 6: 
		 * 7: 
		 */

		ChemicalType chosenChemicalType = ChemicalType.RED;

		ChemicalType currentColor = AgentMemory.getColor(previousState, move.currentState);
	

		boolean atHighest = true;
		boolean allZero = true;
		double highestConcentration = currentCell.getConcentration(currentColor);
		for(DirectionType directionType : neighborMap.keySet()) {
			double neighborConcentration = neighborMap.get(directionType).getConcentration(chosenChemicalType);
			//System.out.println("for loop, neighborConcentration: "+neighborConcentration);
			//System.out.println("for loop, directionType: "+directionType);
			if(highestConcentration < neighborConcentration) {
				highestConcentration = neighborConcentration;
				move.directionType = directionType;
				move.currentState = previousState;
				atHighest = false;
			}
			if (neighborConcentration > 0.01) {
				allZero = false;
			}
		}
		if (atHighest) {
			move.directionType = DirectionType.CURRENT;
			if (!allZero) {
				move.currentState = (byte) (previousState + 1);
				if (move.currentState >= 3) {
					move.currentState = 0;
				}
			}
			else {
				move.currentState = previousState;
			}
		}

		//System.out.println("Chemical Type: "+chosenChemicalType);
		//System.out.println("highestConcentration: "+highestConcentration);
		//System.out.println("direction: "+move.directionType);
		System.out.println("PreviousState: "+previousState);
		System.out.println("CurrentState: "+move.currentState);
		return move;
	}
}
=======
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
	   }

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
>>>>>>> upstream/master
