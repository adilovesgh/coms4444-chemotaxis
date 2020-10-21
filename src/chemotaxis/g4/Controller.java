package chemotaxis.g4; 

import java.awt.Point;
import java.util.PriorityQueue;
import java.io.*;
import java.util.HashMap;
import java.lang.Math; 
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import chemotaxis.sim.ChemicalPlacement;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.SimPrinter;


public class Controller extends chemotaxis.sim.Controller {
    HashMap<Point, Point> came_from;
    HashMap<Point, Integer> cost_so_far;
	List<Point> path;
	
	private Node[][] bestPath;
	private PriorityQueue<Node> frontier;

	private int startX;
	private int startY;
	private int targetX;
	private int targetY;

	private int counter = 0;

	private ArrayList<DirectionChange> directionChanges;

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
		this.frontier = new PriorityQueue<>();
		this.directionChanges = new ArrayList<>(0);
		this.startX = (int)start.getX() - 1;
		this.startY = (int)start.getY() - 1;
		this.targetY = (int)target.getY() - 1;
		this.targetX = (int)target.getX() - 1;
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
		//find path in 1st round OR when no valid path was found previously
		if (currentTurn == 1 || bestPath[this.targetX][this.targetY].getParent() == null) {
			//System.out.println("calculating path...");
			findPath(currentTurn, chemicalsRemaining, currentLocation, grid);
		}

		if (bestPath[this.targetX][this.targetY].getParent() != null) {

			ChemicalPlacement chemicalPlacement = new ChemicalPlacement();
			List<ChemicalType> chemicals = new ArrayList<>();
			//chemicalPlacement.location = new Point(1, 1);

			if (directionChanges.size() > 0 && directionChanges.get(directionChanges.size() - 1).atPostion(currentLocation)) {
				chemicals.add(ChemicalType.BLUE);
				Direction direction = directionChanges.get(directionChanges.size() - 1).getDirection();
				
				if (direction == Direction.NORTH) {
					chemicalPlacement.location = new Point((int)currentLocation.getX() - 1, (int)currentLocation.getY());	
				}
				else if (direction == Direction.SOUTH) {
					chemicalPlacement.location = new Point((int)currentLocation.getX() + 1, (int)currentLocation.getY());	
				}
				else if (direction == Direction.WEST) {
					chemicalPlacement.location = new Point((int)currentLocation.getX(), (int)currentLocation.getY() - 1);	
				}
				else if (direction == Direction.EAST) {
					chemicalPlacement.location = new Point((int)currentLocation.getX(), (int)currentLocation.getY() + 1);	
				}
				directionChanges.remove(directionChanges.size() - 1);
			}
			chemicalPlacement.chemicals = chemicals;
			
			if (chemicalPlacement.location != null) {
				printAppliedChemicals(chemicalPlacement, currentTurn);
			}
			return chemicalPlacement;
		}

		//if path has more turning points than chemicals, do nothing
	
		ChemicalPlacement chemicalPlacement = new ChemicalPlacement();
		List<ChemicalType> chemicals = new ArrayList<>();
		//chemicals.add(ChemicalType.BLUE);
		//chemicalPlacement.location = new Point(5, 5);
		chemicalPlacement.chemicals = chemicals;
		//System.out.println("no valid");
		return chemicalPlacement;
   }

   private void printAppliedChemicals(ChemicalPlacement placement, int turn) {
	   System.out.println("Applied at position " + placement.location + "at turn " + turn + "\n=================================================================\n");
   }


   private void findPath(Integer currentTurn, Integer chemicalsRemaining, Point currentLocation, ChemicalCell[][] grid){
		this.bestPath = new Node[this.size][this.size];

		for (int i = 0; i < this.size; i++) {
			for (int j = 0; j < this.size; j++) {
				bestPath[i][j] = new Node();
			}
		}

		int startX = (int)currentLocation.getX() - 1;
		int startY = (int)currentLocation.getY() - 1;
		bestPath[startX][startY] = new Node(startX, startY);
		frontier.add(new Node(startX, startY));
		
		boolean pathFound = false;
		while (!frontier.isEmpty() && !pathFound) {
			// System.out.println(counter++ + ",  " + frontier.size());
			Node currentNode = frontier.remove();
			
			// Search path north
			if (currentNode.getX() > 0) {
				if (grid[currentNode.getX() - 1][currentNode.getY()].isOpen()) {
					Node newPath = new Node(currentNode, currentNode.getX() - 1, currentNode.getY());
					if (newPath.getTurns() <= chemicalsRemaining) {
						int newTurns = newPath.getTurns();
						int oldTurns = bestPath[newPath.getX()][newPath.getY()].getTurns();
						int newLength = newPath.getLength();
						int oldLength = bestPath[newPath.getX()][newPath.getY()].getLength();


						if ((newTurns < oldTurns && newLength <= oldLength) || ((newTurns <= oldTurns && newLength < oldLength))) {
							frontier.add(newPath);
							bestPath[newPath.getX()][newPath.getY()] = newPath;

							if (this.targetX == newPath.getX() && this.targetY == newPath.getY()) {
								pathFound = true;
							}
						}
						else if (newTurns > oldTurns && newLength < oldLength) {
							frontier.add(newPath);
							bestPath[newPath.getX()][newPath.getY()] = newPath;
						}
						else if (newTurns < oldTurns && newLength > oldLength) {
							frontier.add(newPath);
						}
					}
				}
			}

			// Search path south
			if (currentNode.getX() < this.size - 1) {
				if (grid[currentNode.getX() + 1][currentNode.getY()].isOpen()) {
					Node newPath = new Node(currentNode, currentNode.getX() + 1, currentNode.getY());
					if (newPath.getTurns() <= chemicalsRemaining) {
						int newTurns = newPath.getTurns();
						int oldTurns = bestPath[newPath.getX()][newPath.getY()].getTurns();
						int newLength = newPath.getLength();
						int oldLength = bestPath[newPath.getX()][newPath.getY()].getLength();


						if ((newTurns < oldTurns && newLength <= oldLength) || ((newTurns <= oldTurns && newLength < oldLength))) {
							frontier.add(newPath);
							bestPath[newPath.getX()][newPath.getY()] = newPath;

							if (this.targetX == newPath.getX() && this.targetY == newPath.getY()) {
								pathFound = true;
							}
						}
						else if (newTurns > oldTurns && newLength < oldLength) {
							frontier.add(newPath);
							bestPath[newPath.getX()][newPath.getY()] = newPath;
						}
						else if (newTurns < oldTurns && newLength > oldLength) {
							frontier.add(newPath);
						}
					}
				}
			}

			// Search path east
			if (currentNode.getY() < this.size - 1) {
				if (grid[currentNode.getX()][currentNode.getY() + 1].isOpen()) {
					Node newPath = new Node(currentNode, currentNode.getX(), currentNode.getY() + 1);
					if (newPath.getTurns() <= chemicalsRemaining) {
						int newTurns = newPath.getTurns();
						int oldTurns = bestPath[newPath.getX()][newPath.getY()].getTurns();
						int newLength = newPath.getLength();
						int oldLength = bestPath[newPath.getX()][newPath.getY()].getLength();


						if ((newTurns < oldTurns && newLength <= oldLength) || ((newTurns <= oldTurns && newLength < oldLength))) {
							frontier.add(newPath);
							bestPath[newPath.getX()][newPath.getY()] = newPath;

							if (this.targetX == newPath.getX() && this.targetY == newPath.getY()) {
								pathFound = true;
							}
						}
						else if (newTurns > oldTurns && newLength < oldLength) {
							frontier.add(newPath);
							bestPath[newPath.getX()][newPath.getY()] = newPath;
						}
						else if (newTurns < oldTurns && newLength > oldLength) {
							frontier.add(newPath);
						}
					}
				}
			}

			// Search path west
			if (currentNode.getY() > 0) {
				if (grid[currentNode.getX()][currentNode.getY() - 1].isOpen()) {
					Node newPath = new Node(currentNode, currentNode.getX(), currentNode.getY() - 1);
					if (newPath.getTurns() <= chemicalsRemaining) {
						int newTurns = newPath.getTurns();
						int oldTurns = bestPath[newPath.getX()][newPath.getY()].getTurns();
						int newLength = newPath.getLength();
						int oldLength = bestPath[newPath.getX()][newPath.getY()].getLength();


						if ((newTurns < oldTurns && newLength <= oldLength) || ((newTurns <= oldTurns && newLength < oldLength))) {
							frontier.add(newPath);
							bestPath[newPath.getX()][newPath.getY()] = newPath;

							if (this.targetX == newPath.getX() && this.targetY == newPath.getY()) {
								pathFound = true;
							}
						}
						else if (newTurns > oldTurns && newLength < oldLength) {
							frontier.add(newPath);
							bestPath[newPath.getX()][newPath.getY()] = newPath;
						}
						else if (newTurns < oldTurns && newLength > oldLength) {
							frontier.add(newPath);
						}
					}
				}
			}
		}

		// Reconstruct path
		if (bestPath[this.targetX][this.targetY].getParent() != null) {
			Node currentNode = bestPath[this.targetX][this.targetY];

			while (currentNode.getParent() != null) {
				Node parent = currentNode.getParent();

				if (parent.getDirection() != currentNode.getDirection()) {
					directionChanges.add(new DirectionChange(parent.getX(), parent.getY(), currentNode.getDirection()));
				}

				currentNode = parent;
			}

			//for (DirectionChange d : directionChanges) {
			//System.out.println(d.getX() + ", " + d.getY() + ", " + d.getDirection());
			//}

		}

   }

}


class Node implements Comparable<Node> {
	private Node parent;
	private int x;
	private int y;
	private Direction direction;
	private int turns;
	private int length;

	// Constructor for initial nodes
	public Node() {
		this.parent = null;
		this.x = 0;
		this.y = 0;
		this.direction = Direction.NULL;
		this.turns = 9999999;
		this.length = 9999999;
	}

	// Constructor for start node
	public Node(int x, int y) {
		this.parent = null;
		this.x = x;
		this.y = y;
		this.direction = Direction.NULL;
		this.turns = 0;
		this.length = 0;
	}

	// Constructor for possible paths
	public Node(Node parent, int x, int y) {
		this.parent = parent;
		this.x = x;
		this.y = y;
		this.length = parent.getLength() + 1;
		// Get direction
		if (this.x == parent.getX()) {
			if (this.y > parent.getY()) {
				this.direction = Direction.EAST;
			}
			else {
				this.direction = Direction.WEST;
			}
		}
		else {
			if (this.x < parent.getX()) {
				this.direction = Direction.NORTH;
			}
			else {
				this.direction = Direction.SOUTH;
			}
		}
		if (this.direction == parent.getDirection()) {
			this.turns = parent.getTurns();
		}
		else {
			this.turns = parent.getTurns() + 1;
		}
	}

	@Override
    public int compareTo(Node other) {
		if (this.length != other.getLength()) {
			return this.length - other.getLength();
		}
		return this.turns - other.getTurns();	
    }

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public Node getParent() {
		return this.parent;
	}

	public Direction getDirection() {
		return this.direction;
	}

	public int getTurns() {
		return this.turns;
	}

	public int getLength() {
		return this.length;
	}
}

class DirectionChange {
	private int x;
	private int y;
	private Direction direction;

	public DirectionChange(int x, int y, Direction direction) {
		this.x = x;
		this.y = y;
		this.direction = direction;
	}
	
	public boolean atPostion(Point p) {
		//System.out.println(this.x);
		//System.out.println(this.y);
		return p.getX() == this.x + 1 && p.getY() == this.y + 1;
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public Direction getDirection() {
		return this.direction;
	}
}


enum Direction {
    NORTH,
    SOUTH,
	EAST,
	WEST,
	NULL
}