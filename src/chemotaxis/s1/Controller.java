package chemotaxis.s1;

import java.awt.GridBagConstraints;
import java.util.Queue;

import java.awt.Point;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import chemotaxis.sim.ChemicalPlacement;
import chemotaxis.sim.DirectionType;
import chemotaxis.sim.ChemicalCell;
import chemotaxis.sim.ChemicalCell.ChemicalType;
import chemotaxis.sim.SimPrinter;

public class Controller extends chemotaxis.sim.Controller {

	List<Point> routeList = new ArrayList<Point>();
	List<Integer> corners = new ArrayList<Integer>();
	int previousDirection;
	boolean endReached = false;
	/**
	 * Controller constructor
	 *
	 * @param start       start cell coordinates
	 * @param target      target cell coordinates
	 * @param size        grid/map size
	 * @param simTime     simulation time
	 * @param budget      chemical budget
	 * @param seed        random seed
	 * @param simPrinter  simulation printer
	 *
	 */

	public Controller(Point start, Point target, Integer size, Integer simTime, Integer budget, Integer seed, SimPrinter simPrinter) {
		super(start, target, size, simTime, budget, seed, simPrinter);
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

		ChemicalPlacement chemicalPlacement = new ChemicalPlacement();

		if (currentTurn == 1) {
 			routeList = getShortestPath(start, target, grid);
 			corners = getCorners(routeList);
 			int needChemicals = corners.size()+1;
 			if (needChemicals > chemicalsRemaining) {
 				simPrinter.println("No enough chemicals, need: "+needChemicals+", have: "+chemicalsRemaining);
 			}
 			int initialDirection = nextDirection(routeList.get(1), currentLocation);
 			if (initialDirection == 1) {
 				chemicalPlacement.chemicals.add(ChemicalType.RED);
 			} else if (initialDirection == 2) {
 				chemicalPlacement.chemicals.add(ChemicalType.GREEN);
 			} else if (initialDirection == 3) {
 				chemicalPlacement.chemicals.add(ChemicalType.BLUE);
 			}
 			if (initialDirection != 4) {
 				chemicalPlacement.location = currentLocation;
 			}
 			previousDirection = initialDirection;
 		}
		else {
			int corner = -1;
			for (int i: corners) {
				if (routeList.get(i).x == currentLocation.x && routeList.get(i).y == currentLocation.y) {
					corner = i;
				}
			}
			if (corner != -1) {
				Point next = routeList.get(corner+1);
				int nextDirection = nextDirection(next, currentLocation);
				if (previousDirection - nextDirection == 1 || previousDirection - nextDirection == -3) {
					chemicalPlacement.chemicals.add(ChemicalType.RED);
				} else if (previousDirection - nextDirection == -1 || previousDirection - nextDirection == 3) {
					chemicalPlacement.chemicals.add(ChemicalType.GREEN);
				}
				chemicalPlacement.location = currentLocation;
				previousDirection = nextDirection;
			}
		}

		return chemicalPlacement;
	}
	
	public List<Integer> getCorners(List<Point> path) {
		List<Integer> corners = new ArrayList<Integer>();
		for (int i = 1; i<path.size()-1; i++) {
			Point before = path.get(i-1);
			Point after = path.get(i+1);
			if (after.x - before.x != 0 && after.y - before.y != 0) {
				corners.add(i);
			}
		}
		return corners;
	}
	
	public int nextDirection(Point next, Point currentLocation) {
		if (next.x- currentLocation.x == 1) {
			return 3;
		} else if (next.x - currentLocation.x == -1) {
			return 1;
		} else if (next.y - currentLocation.y == 1) {
			return 2;
		} else {
			return 4;
		}
	}

	public List<Point> getShortestPath(Point start, Point target, ChemicalCell[][] grid) {
		
		boolean[][] visited = new boolean[grid.length][grid[0].length];
		
		Node source = new Node(start.x, start.y);
		Queue<Node> queue = new LinkedList<Node>(); 
		queue.add(source);
		Node solution = null; 
		while (!queue.isEmpty()) {
			Node popped = queue.poll(); 
			if (popped.x == target.x && popped.y == target.y) {
				solution = popped;
				break;
			}
			else if (!visited[popped.x-1][popped.y-1] && !grid[popped.x-1][popped.y-1].isBlocked()) {
				visited[popped.x-1][popped.y-1] = true;
				List<Node> neighborList = addNeighbors(popped, grid, visited);

				queue.addAll(neighborList);
			}
		}
		List<Point> path = new LinkedList<Point>(); 
		while (solution != null) {
			path.add(new Point(solution.x, solution.y));
			solution = solution.parent;
			
		}
		Collections.reverse(path);
		return path;
	}
	
	private List<Node> addNeighbors(Node current, ChemicalCell[][] grid, boolean[][] visited) {
		List<Node> list = new LinkedList<Node>();
		//simPrinter.println("entered method");
		//simPrinter.println(current.x);
		//simPrinter.println(current.y);
		
		if((current.x - 1 > 0) && !visited[current.x - 2][current.y - 1]) {
			Node currNode = new Node(current.x-1, current.y);
			currNode.parent = current;
			list.add(currNode);
			//simPrinter.println("added 1: " + currNode.x + " " + currNode.y);
		}
		if((current.x + 1 <= grid.length) && !visited[current.x][current.y - 1]) {
			Node currNode = new Node(current.x+1, current.y);
			currNode.parent = current;
			list.add(currNode);
			//simPrinter.println("added 2: " + currNode.x + " " + currNode.y);
		}
		if((current.y - 1 > 0) && !visited[current.x - 1][current.y - 2]) {
			Node currNode = new Node(current.x, current.y - 1);
			currNode.parent = current;
			list.add(currNode);
			//simPrinter.println("added 3: " + currNode.x + " " + currNode.y);
		}
		if((current.y + 1 <= grid.length) && !visited[current.x - 1][current.y]) {
			Node currNode = new Node(current.x, current.y + 1);
			currNode.parent = current;
			list.add(currNode);
			//simPrinter.println("added 4: " + currNode.x + " " + currNode.y);

		}		
		return list;
	}
	
	class Node {
	    int x;
	    int y; 
	    Node parent;
	    
	    public Node(int x, int y) {
	    	this.x = x;
	    	this.y = y;
	    }
	}
}