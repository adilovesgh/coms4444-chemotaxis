package chemotaxis.g4; // TODO modify the package name to reflect your team

import java.util.Map;

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
   @Override
   public Move makeMove(Integer randomNum, Byte previousState, ChemicalCell currentCell, Map<DirectionType, ChemicalCell> neighborMap) {
      Move move = new Move();
      move.currentState = previousState;
    
      ChemicalType chosenChemicalType = ChemicalType.BLUE;
      
      for(DirectionType directionType : neighborMap.keySet()) {
         if (neighborMap.get(directionType).getConcentration(chosenChemicalType) == 1) {
            move.currentState = (byte)directionType.ordinal();
         }
       }

      move.directionType = DirectionType.values()[move.currentState];      
      return move;

   }
}
