package chemotaxis.g5;

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

	boolean isSolvedGame = true;
	boolean isCrapShoot = false;
	List<Point> routeList = new ArrayList<Point>();
	List<Integer> corners = new ArrayList<Integer>();
	Point agentPreviousLocation = new Point();
	//int previousDirection;
	
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

			if (chemicalsRemaining == 0)
				isCrapShoot = true;

 			routeList = getShortestPath(start, target, grid);
 			routeList = recursiveImprovePath(routeList, grid);
 			corners = getCorners(routeList);
 			corners = getCornersImproved(routeList, corners, grid);
 			/*
 			simPrinter.println("Corners before");
 			for (int i: corners) {
 				Point p = routeList.get(i);
 				System.out.println(i+ ": "+p.x+" "+p.y);
 			}
 			
 			simPrinter.println("Corners after");
 			for (int i: corners) {
 				Point p = routeList.get(i);
 				simPrinter.println(i+ ": "+p.x+" "+p.y);
 			}*/
 			
 			int needChemicals = corners.size();
 			
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
 				needChemicals++;
 			}

 			if (needChemicals > chemicalsRemaining) {
 				chemicalPlacement.chemicals.clear();
 				chemicalPlacement.chemicals.add(ChemicalType.BLUE);
 				chemicalPlacement.location = currentLocation;
 				switch (initialDirection) {
 					case 1:
 						chemicalPlacement.location.x -= 1;
 						break; 
 					case 2:
 						chemicalPlacement.location.y += 1;
 						break;
 					case 3:
 						chemicalPlacement.location.x += 1;
 						break;
 					case 4: 
 						chemicalPlacement.location.y -= 1;
 						break;
 				}
 				isSolvedGame = false;
 				simPrinter.println("No enough chemicals, need: "+needChemicals+", have: "+chemicalsRemaining);
 			}
 			
 		} else if (isSolvedGame) {
			int corner = -1;
			for (int i: corners) {
				if (routeList.get(i).x == currentLocation.x && routeList.get(i).y == currentLocation.y) {
					corner = i;
				}
			}
			if (corner != -1) {
				Point next = routeList.get(corner+1);
				Point pre = routeList.get(corner-1);
				int previousDirection = nextDirection(currentLocation, pre);
				int nextDirection = nextDirection(next, currentLocation);
				if (previousDirection - nextDirection == 1 || previousDirection - nextDirection == -3) {
					chemicalPlacement.chemicals.add(ChemicalType.RED);
				} else if (previousDirection - nextDirection == -1 || previousDirection - nextDirection == 3) {
					chemicalPlacement.chemicals.add(ChemicalType.GREEN);
				}
				chemicalPlacement.location = currentLocation;
				previousDirection = nextDirection;
			}

		} else if (!isSolvedGame) {

			routeList = getShortestPath(currentLocation, target, grid);
 			routeList = recursiveImprovePath(routeList, grid);
 			corners = getCorners(routeList);
 			corners = getCornersImproved(routeList, corners, grid);
 			/*
 			simPrinter.println("Corners before");
 			for (int i: corners) {
 				Point p = routeList.get(i);
 				System.out.println(i+ ": "+p.x+" "+p.y);
 			}
 			
 			simPrinter.println("Corners after");
 			for (int i: corners) {
 				Point p = routeList.get(i);
 				simPrinter.println(i+ ": "+p.x+" "+p.y);
 			}*/
 			
 			int needChemicals = corners.size() + 1;

 			if (needChemicals <= chemicalsRemaining) {
 				simPrinter.println("SOOOLLLVVEDSHIT: NEED: "+needChemicals+", have: "+chemicalsRemaining);
 				simPrinter.println("prev location: "+agentPreviousLocation+", currentTurn: "+currentLocation);
 				isSolvedGame = true;
	 			int previousDirection = nextDirection(currentLocation, agentPreviousLocation);
	 			int nextDirection = nextDirection(routeList.get(1), currentLocation);
	 			simPrinter.println("PrevDIR: "+previousDirection+", nextDir: "+nextDirection);
	 			if (previousDirection - nextDirection == 1 || previousDirection - nextDirection == -3) {
					chemicalPlacement.chemicals.add(ChemicalType.RED);
				} else if (previousDirection - nextDirection == -1 || previousDirection - nextDirection == 3) {
					chemicalPlacement.chemicals.add(ChemicalType.GREEN);
				}
				chemicalPlacement.location = currentLocation;
			} else {
 				isSolvedGame = false;
 				//simPrinter.println("Not enough chemicals, need: "+needChemicals+", have: "+chemicalsRemaining);
 			}


		} else if (isCrapShoot) {
			return chemicalPlacement;
		}
		agentPreviousLocation = currentLocation;
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
	
	public List<Integer> getCornersImproved(List<Point> path, List<Integer> corners, ChemicalCell[][] grid) {
		//1: I'm moving NORTH
		//2: I'm moving EAST
		//3: I'm moving SOUTH
		//4: I'm moving WEST
		
		List<Integer> newCorners = new ArrayList<Integer>();
		for (int i: corners) {
			Point corner = path.get(i);
			Point prePoint = path.get(i-1);
			Point postPoint = path.get(i+1);
			int preDirection = nextDirection(corner, prePoint);
			int postDirection = nextDirection(postPoint, corner);
			
			//System.out.println("corner: "+i+ ": "+corner.x+ " "+ corner.y);
			//System.out.println("preDirection: "+preDirection+ "; postDirection: "+postDirection);
			//System.out.println(corner.x-2 < 0 || grid[corner.x-2][corner.y-1].isBlocked());
			//System.out.println(corner.y >= grid[0].length || grid[corner.x-1][corner.y].isBlocked());
			//System.out.println(corner.x >= grid.length || grid[corner.x][corner.y-1].isBlocked());
			//System.out.println(corner.y-2 < 0 || grid[corner.x-1][corner.y-2].isBlocked());
			
			if (preDirection == 1 && (corner.x-2 < 0 || grid[corner.x-2][corner.y-1].isBlocked())) {
				
				//System.out.println("Left and right open: "+(!(corner.y >= grid[0].length || grid[corner.x-1][corner.y].isBlocked()) && !(corner.y-2 < 0 || grid[corner.x-1][corner.y-2].isBlocked())));
				if (preDirection - postDirection == -3 && !(corner.y >= grid[0].length || grid[corner.x-1][corner.y].isBlocked()) && !(corner.y-2 < 0 || grid[corner.x-1][corner.y-2].isBlocked())) {
					newCorners.add(i);
				}
			} else if (preDirection == 2 && (corner.y >= grid[0].length || grid[corner.x-1][corner.y].isBlocked())) {
				
				//System.out.println("Left and right open: "+(!(corner.x >= grid.length || grid[corner.x][corner.y-1].isBlocked()) && !(corner.x-2 < 0 || grid[corner.x-2][corner.y-1].isBlocked())));
				if (preDirection - postDirection == 1 && !(corner.x >= grid.length || grid[corner.x][corner.y-1].isBlocked()) && !(corner.x-2 < 0 || grid[corner.x-2][corner.y-1].isBlocked())) {
					newCorners.add(i);
				}
			} else if (preDirection == 3 && (corner.x >= grid.length || grid[corner.x][corner.y-1].isBlocked())) {
				
				//System.out.println("Left and right open: "+(!(corner.y-2 < 0 || grid[corner.x-1][corner.y-2].isBlocked()) && !(corner.y >= grid[0].length || grid[corner.x-1][corner.y].isBlocked())));
				if (preDirection - postDirection == 1 && !(corner.y-2 < 0 || grid[corner.x-1][corner.y-2].isBlocked()) && !(corner.y >= grid[0].length || grid[corner.x-1][corner.y].isBlocked())) {
					newCorners.add(i);
				}
			} else if (preDirection == 4 && (corner.y-2 < 0 || grid[corner.x-1][corner.y-2].isBlocked())) {
				
				//System.out.println("Left and right open: "+(!(corner.x-2 < 0 || grid[corner.x-2][corner.y-1].isBlocked()) && !(corner.x >= grid.length || grid[corner.x][corner.y-1].isBlocked())));
				if (preDirection - postDirection == 1 && !(corner.x-2 < 0 || grid[corner.x-2][corner.y-1].isBlocked()) && !(corner.x >= grid.length || grid[corner.x][corner.y-1].isBlocked())) {
					newCorners.add(i);
				}
			} else {
				newCorners.add(i);
			}
			
		}
		return newCorners;
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
	
	public List<Point> recursiveImprovePath(List<Point> path, ChemicalCell[][] grid) {
		List<Integer> corners = getCorners(path);
		corners.add(0, 0);
		corners.add(path.size()-1);
		List<Point> newPath = new LinkedList();
		boolean allDone = true;
		while (corners.size() > 3) {
			int s = corners.get(0);
			int t = corners.get(3);
			Point start = path.get(s);
			Point target = path.get(t);
			List<Point> improvedPart = shortestPathLimitTurns(start, target, grid, t-s);
			if (improvedPart != null) {
				if (corners.get(3) != path.size()-1) {
					improvedPart.addAll(path.subList(corners.get(3)+1, path.size()));
				}
				path = improvedPart;
				allDone = false;
				break;
			} else {
				newPath.addAll(path.subList(0, corners.get(1)));
				path = path.subList(corners.get(1), path.size());
				corners = getCorners(path);
				corners.add(0, 0);
				corners.add(path.size()-1);
			}
		}
		if (allDone) {
			newPath.addAll(path);
			return newPath;
		}
		newPath.addAll(recursiveImprovePath(path, grid));
		return newPath;
	}
	
	public List<Point> getCornersAsPoints(List<Point> path, List<Integer> corners) {
		List<Point> cornerPoints = new LinkedList<Point>();
		for (int i: corners) {
			cornerPoints.add(path.get(i));
		}
		return cornerPoints;
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
	
	public List<Point> shortestPathLimitTurns(Point start, Point target, ChemicalCell[][] grid, int depthLimit) {
		
		boolean[][] visited = new boolean[grid.length][grid[0].length];
		LNode source = new LNode(start.x, start.y, false, 0);
		Queue<LNode> queue = new LinkedList<LNode>();
		queue.add(source);
		LNode solution = null;
		while (!queue.isEmpty()) {
			LNode popped = queue.poll();
			if (popped.x == target.x && popped.y == target.y) {
				solution = popped;
				break;
			}
			else if (!visited[popped.x-1][popped.y-1] && !grid[popped.x-1][popped.y-1].isBlocked()) {
				visited[popped.x-1][popped.y-1] = true;
				List<LNode> neighborList = addLimitedNeighbors(popped, grid, visited, depthLimit);
				
				queue.addAll(neighborList);
			}
		}
		if (solution == null) {
			//System.out.println("Can't imporve between: "+start.x+" "+start.y+"; "+target.x+" "+target.y);
			return null;
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
	
	private List<LNode> addLimitedNeighbors(LNode current, ChemicalCell[][] grid, boolean[][] visited, int depthLimit) {
		List<LNode> list = new LinkedList<LNode>();
		if (current.depth >= depthLimit) {
			return list;
		}
		else if (current.parent == null){
			if((current.x - 1 > 0) && !visited[current.x - 2][current.y - 1]) {
				LNode currNode = new LNode(current.x-1, current.y, current.turned, current.depth+1);
				currNode.parent = current;
				list.add(currNode);
				//simPrinter.println("added 1: " + currNode.x + " " + currNode.y);
			}
			if((current.x + 1 <= grid.length) && !visited[current.x][current.y - 1]) {
				LNode currNode = new LNode(current.x+1, current.y, current.turned, current.depth+1);
				currNode.parent = current;
				list.add(currNode);
				//simPrinter.println("added 2: " + currNode.x + " " + currNode.y);
			}
			if((current.y - 1 > 0) && !visited[current.x - 1][current.y - 2]) {
				LNode currNode = new LNode(current.x, current.y - 1, current.turned, current.depth+1);
				currNode.parent = current;
				list.add(currNode);
				//simPrinter.println("added 3: " + currNode.x + " " + currNode.y);
			}
			if((current.y + 1 <= grid.length) && !visited[current.x - 1][current.y]) {
				LNode currNode = new LNode(current.x, current.y + 1, current.turned, current.depth+1);
				currNode.parent = current;
				list.add(currNode);
				//simPrinter.println("added 4: " + currNode.x + " " + currNode.y);

			}
		}
		else {
			if (current.parent.x - current.x == 1) {
				if((current.x - 1 > 0) && !visited[current.x - 2][current.y - 1]) {
					LNode currNode = new LNode(current.x-1, current.y, current.turned, current.depth+1);
					currNode.parent = current;
					list.add(currNode);
					//simPrinter.println("added 1: " + currNode.x + " " + currNode.y);
				}
			} else if (!current.turned){
				if((current.x - 1 > 0) && !visited[current.x - 2][current.y - 1]) {
					LNode currNode = new LNode(current.x-1, current.y, true, current.depth+1);
					currNode.parent = current;
					list.add(currNode);
					//simPrinter.println("added 1: " + currNode.x + " " + currNode.y);
				}
			}
			if (current.parent.x - current.x == -1) {
				if((current.x + 1 <= grid.length) && !visited[current.x][current.y - 1]) {
					LNode currNode = new LNode(current.x+1, current.y, current.turned, current.depth+1);
					currNode.parent = current;
					list.add(currNode);
					//simPrinter.println("added 2: " + currNode.x + " " + currNode.y);
				}
			} else if (!current.turned) {
				if((current.x + 1 <= grid.length) && !visited[current.x][current.y - 1]) {
					LNode currNode = new LNode(current.x+1, current.y, true, current.depth+1);
					currNode.parent = current;
					list.add(currNode);
					//simPrinter.println("added 2: " + currNode.x + " " + currNode.y);
				}
			}
			if (current.parent.y - current.y == 1) {
				if((current.y - 1 > 0) && !visited[current.x - 1][current.y - 2]) {
					LNode currNode = new LNode(current.x, current.y - 1, current.turned, current.depth+1);
					currNode.parent = current;
					list.add(currNode);
					//simPrinter.println("added 3: " + currNode.x + " " + currNode.y);
				}
			} else if (!current.turned) {
				if((current.y - 1 > 0) && !visited[current.x - 1][current.y - 2]) {
					LNode currNode = new LNode(current.x, current.y - 1, true, current.depth+1);
					currNode.parent = current;
					list.add(currNode);
					//simPrinter.println("added 3: " + currNode.x + " " + currNode.y);
				}
			}
			
			if (current.parent.y - current.y == -1) {
				if((current.y + 1 <= grid.length) && !visited[current.x - 1][current.y]) {
					LNode currNode = new LNode(current.x, current.y + 1, current.turned, current.depth+1);
					currNode.parent = current;
					list.add(currNode);
					//simPrinter.println("added 4: " + currNode.x + " " + currNode.y);
				}
			} else if (!current.turned) {
				if((current.y + 1 <= grid.length) && !visited[current.x - 1][current.y]) {
					LNode currNode = new LNode(current.x, current.y + 1, true, current.depth+1);
					currNode.parent = current;
					list.add(currNode);
					//simPrinter.println("added 4: " + currNode.x + " " + currNode.y);
				}
			}
				
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
	
	class LNode {
		int x;
		int y;
		boolean turned;
		int depth;
		LNode parent;
		
		public LNode(int x, int y, boolean turned, int depth) {
			this.x = x;
			this.y = y;
			this.turned = turned;
			this.depth = depth;
		}
	}
}