package chemotaxis.r1;

import java.util.Map;

import chemotaxis.sim.DirectionType;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.Move;
import chemotaxis.sim.SimPrinter;
import chemotaxis.sim.ChemicalCell.ChemicalType;

public class Agent extends chemotaxis.sim.Agent {

	public byte FIRST_ROUND = 0;
	public byte SOLVED_GAME = 1;
	public byte UNSOLVED_GAME = 2;
	public byte RANDOM_GAME = 3;
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
		// Get green chemical surroundings
		switch (AgentMemory.classifyGame(previousState)) {

			case 1:

			case 2:

			case 3:

			case 4:
				simPrinter.println("RandomGame");
				AgentMemory.getRandomMove(randomNum, previousState, move, neighborMap);
				AgentMemory.updateRandomMoveMemory(move, previousState);
				if (AgentMemory.correctForCurl(move, neighborMap))
					AgentMemory.updateMoveMemory(move);
				break; 
		}
		


		
		return move;
	}
}
