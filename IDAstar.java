
/**
 * Quan Tong - CS410 19-Puzzle Solver. Using IDA* and A* currently using Manhanttan Distance with Linear Conflict with API: HashSet, PriorityQueue 
 */
import java.util.Comparator;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;
import java.util.Stack;

public class IDAstar {
	public static int puzzle[][] = new int[6][6];	
	public static int zeroPos[] = new int[2]; //empty tile position
	public static final int goal[][] = new int[6][6];//goal state
	public static final int findPos[][] = new int[20][2]; //look for goal position in constant time
	//public static HashSet<State> explored = new HashSet<State>();
	public static IDAstar p19 = new IDAstar();
	public static Comparator<State> comp = p19.new HeuCompare();
	//public static PriorityQueue<State> frontier = new PriorityQueue<State>(1000, comp);
	public static State goalState = p19.new State(goal, 0, 0);
	public static HashSet<State> IDAexplored = new HashSet<State>();
	public static int stateCounter = 0;
	//left, right, up, down
	public static int[] moveRow = {1, -1, 0, 0};
	public static int[] moveCol = {0, 0, 1, -1};
	
	//State class
	public class State {
		public State parent; //parent state
		public int puzzle[][] = new int[6][6];	
		public int colZ, rowZ; //coordinate of the empty tile
		public int cost; 
		public int heu;//current heuristic
		public int fVal;
		
		
		public State(State parent, int rowN, int colN) {
			this.parent = parent;
			puzzle = deepCopyArr(parent.puzzle);
			this.colZ = colN;
			this.rowZ = rowN;
			int temp = puzzle[parent.rowZ][parent.colZ];
			puzzle[parent.rowZ][parent.colZ] = puzzle[rowN][colN];
			puzzle[rowN][colN] = temp; //swap tiles
			cost = parent.cost + 1;
		}	
		public void updateManHeu(int tileHeu) {
			heu = updateHeu(puzzle, parent.heu, tileHeu, parent.rowZ, parent.colZ); //update heuristic from parent state
			fVal = cost + heu;
		}
		
		/**
		 * compute Manhattan distance Heuristic
		 */
		public void manHanttanHeu() {
			heu = heuristic(puzzle);
			fVal = cost + heu;
		}
		
		/**
		 * compute MD with linear conflict
		 */
		public void linearConHeu() {
			heu = LCHeu(puzzle);
			fVal = cost + heu;
		}
		
		/**
		 * Partition Heuristic initial state Constructor
		 * @param p
		 * @param rowZ
		 * @param colZ
		 */
		public State(int[][] p, int rowZ, int colZ) {
			puzzle = deepCopyArr(p);
			this.colZ = colZ;
			this.rowZ = rowZ;
			cost = 0;
		}
		
		public boolean hasParent() {
			return parent != null;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + Arrays.deepHashCode(puzzle);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			State other = (State) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (!Arrays.deepEquals(puzzle, other.puzzle))
				return false;
			return true;
		}
		private IDAstar getOuterType() {
			return IDAstar.this;
		}
		
	}
	
	public class HeuCompare implements Comparator<State> {

		@Override
		public int compare(State o1, State o2) {
			// TODO Auto-generated method stub
			return o1.fVal - o2.fVal;
		}
		
	}
	public static void main(String[] args) {
		//goal state
		int n = 0;
		for (int i = 0; i < 36; i++)
			goal[i/6][i%6] = -1;
		for (int i = 0; i < 2; i++) 
			for (int j = 2; j < 4; j++) {
				findPos[n][0] = i;
				findPos[n][1] = j;
				goal[i][j] = n++;
			}

		for (int i = 2; i < 4; i++) 
			for (int j = 0; j < 6; j++) {
				findPos[n][0] = i;
				findPos[n][1] = j;
				goal[i][j] = n++;
			}
		for (int i = 4; i < 6; i++) 
			for (int j = 2; j < 4; j++) {
				findPos[n][0] = i;
				findPos[n][1] = j;
				goal[i][j] = n++;
			}
		Scanner sc = new Scanner(System.in);
		//initial state
		System.out.println("Press i for input puzzle or g for generated puzzle");
		if(sc.next().equals("i")) {
			for (int i = 0; i < 36; i++)
				puzzle[i/6][i%6] = -1;
			//input
			for (int i = 0; i < 2; i++) 
				for (int j = 2; j < 4; j++) {
					puzzle[i][j] = sc.nextInt();
					if(puzzle[i][j] == 0) {
						zeroPos[0] = i;
						zeroPos[1] = j;
					}
				}
			for (int i = 2; i < 4; i++) 
				for (int j = 0; j < 6; j++) {
					puzzle[i][j] = sc.nextInt();
					if(puzzle[i][j] == 0) {
						zeroPos[0] = i;
						zeroPos[1] = j;
					}
				}
			for (int i = 4; i < 6; i++) 
				for (int j = 2; j < 4; j++) {
					puzzle[i][j] = sc.nextInt();
					if(puzzle[i][j] == 0) {
						zeroPos[0] = i;
						zeroPos[1] = j;
					}
				}
		}
		else {
			System.out.println("Choose number of random disorganizing moves");
			int loop = sc.nextInt();
			puzzle = generatePuzzle(loop);
		}
		sc.close();
		printPuzz(puzzle);
		//createPartition(); // create 3 partitions
		State init = p19.new State(puzzle, zeroPos[0], zeroPos[1]);
		init.linearConHeu();
		State result = null;
		long start = System.currentTimeMillis();
		System.out.println("IDA*:");
		result = IDAstarFunc(init);
		if(result != null) {
			printGoalPath(result);
		}
		else {
			System.out.println("No solution");
		}
		long time = System.currentTimeMillis() - start;
		System.out.println("Time: " + time + " milliseconds");
		//System.out.println(stateCounter);
	}
	
	/*
	 * __________IDA*____________________________________
	 */
	/**
	 * this function works like depth first search with bound for IDA*
	 * @param current state
	 * @param maxDepth for DFS
	 * @param for passing new depth to recursive calls
	 * @return 0 if found the goal or the new search depth
	 */
	public static int DFS(State curState, int maxDepth, int minNewDepth) {
		stateCounter++;
		IDAexplored.add(curState);
		if(curState.heu == 0) {
			goalState = curState;
			return 0;
		}
		int min = minNewDepth;
		//queue for at most 4 child states of a state
		PriorityQueue<State> IDfrontier = new PriorityQueue<State>(4, comp);
		for(int i = 0; i < 4; i++) { 
			int childRow = curState.rowZ + moveRow[i];
			int childCol = curState.colZ + moveCol[i];
			//check if it's legal to move
			if(childRow >=0 && childRow < 6 && childCol >= 0 && childCol < 6 && curState.puzzle[childRow][childCol] > 0) {
				//int moveHeu = tileHeu(curState.puzzle, childRow, childCol);
				State nextState = p19.new State(curState, childRow, childCol);
				nextState.linearConHeu();
				//check if child state's f value is not larger than the max depth allowed
				if(!IDAexplored.contains(nextState)) {
					if(nextState.fVal <= maxDepth) {
						//add child state to the frontier
						IDfrontier.add(nextState);
					}
					else if(nextState.fVal > maxDepth && (min == maxDepth || nextState.fVal < min)) min = nextState.fVal; 
				}
				//if child state has larger f value, save this value for the max depth next iteration
				
			}
		}
		while(!IDfrontier.isEmpty()) {
			State next = IDfrontier.poll();
			int res = DFS(next, maxDepth, min);
			if(res == 0) {
				return 0;
			}
			else min = res;
		}
		IDAexplored.remove(curState);
		//return the new max depth
		return min;
	}
	/**
	 * Iterative Deepening A* for hard test cases
	 * @param initial : initial state
	 * @return true if found the solution
	 */
	public static State IDAstarFunc(State initial) {
		//initial.linearConHeu();
		int maxDepth = initial.fVal;
		int newDepth = initial.fVal;
		while(maxDepth != 0) {
			System.out.println("Depth: " + maxDepth);
			newDepth = DFS(initial, maxDepth, newDepth);
			if(newDepth == maxDepth) break;
			maxDepth = newDepth;
		}
		if(maxDepth == 0) return goalState;
		return null;
		
			
	/*
	 *_________________ HEURISTIC FUNCTIONS______________________________
	 * 
	 */
	}
	/**
	 * 
	 * @param p : puzzle
	 * @return heuristic
	 */
	public static int heuristic(int[][] p) {
		int h = 0;
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 6; j++) {
				int tileVal = p[i][j];
				if(tileVal > 0) {
					h = h + Math.abs(i-findPos[tileVal][0]) + Math.abs(j - findPos[tileVal][1]);
				}
			}
		}
		return h;
	}
	
	/**
	 * compute MD with LC heuristic
	 * @param p : puzze
	 * @return heuristic
	 */
	public static int LCHeu(int[][] p) {
		int h = 0;
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 6; j++) {
				int tileVal = p[i][j];
				if(tileVal > 0) {
					h = h + Math.abs(i-findPos[tileVal][0]) + Math.abs(j - findPos[tileVal][1]);
					//row linear conflict
					if(i == findPos[tileVal][0]) {
						for(int k = 0; k < j; k++) {
							int oTileVal = p[i][k];
							if(oTileVal > tileVal && i == findPos[oTileVal][0]) {
								h += 2;
							}
						}
					}
					//column linear conflict
					if(j == findPos[tileVal][1]) {
						for(int k = 0; k < i; k++) {
							int oTileVal = p[k][j];
							if(oTileVal > tileVal && j == findPos[oTileVal][1]) {
								h += 2;
							}
						}
					}
				}
			}
		}
		return h;
	}

	
	/**
	 * 
	 * @param p: puzzle
	 * @param parent heuristic
	 * @param heuristic of the tile the is moved
	 * @param new row of the moved tile
	 * @param new column of the moved tile
	 * @return the updated heuristic
	 */
	public static int updateHeu(int[][] p, int parentHeu, int tileHeu, int row, int col) {
		return parentHeu - tileHeu + tileHeu(p, row, col);
	}
	
	/**
	 * 
	 * @param p: puzzle
	 * @param row
	 * @param col
	 * @return the heuristic of one tile
	 */
	public static int tileHeu(int[][] p, int row, int col) {
		int tileVal = p[row][col];
		return Math.abs(row-findPos[tileVal][0]) + Math.abs(col - findPos[tileVal][1]);
	}
	
	
	/*
	 * ______________PRINTING FUNCTIONS______________________
	 */
	/**
	 * This function prints the puzzle
	 * @param p
	 */
	public static void printPuzz(int[][] p) {
		for (int i = 0; i < 2; i++) {
			System.out.print("\t\t");
			for (int j = 2; j < 4; j++) {
				System.out.print(p[i][j] + "\t");
			}
			System.out.println("");
		}
		for (int i = 2; i < 4; i++) {
			for (int j = 0; j < 6; j++) {
				System.out.print(p[i][j] + "\t");
			}
			System.out.println("");
		}
		for (int i = 4; i < 6; i++) {
			System.out.print("\t\t");
			for (int j = 2; j < 4; j++) {
				System.out.print(p[i][j] + "\t");
			}
			System.out.println("");
		}
	}
	
	public static void printGoalPath(State goalState) {
		Stack<State> printStack = new Stack<State>();
		while (goalState.hasParent()) {
			printStack.push(goalState);
			goalState = goalState.parent;
		}
		int t = printStack.size();
		while(!printStack.isEmpty()) {
			printPuzz(printStack.pop().puzzle);
		}
		System.out.println("Path to Goal requires: " + t + " moves");
	}
	
	
	/*
	 * _______OTHER HELPER FUNCTIONS_______________________
	 */
	
	
	/**
	 * this function copies the array without reference
	 * @param a
	 * @return the array
	 */
	public static int[][] deepCopyArr(int[][] a) {
		int[][] b = new int[6][6];
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 6; j++) {
				b[i][j] = a[i][j];
			}
		}
		return b;
	}
	
	public static int[][] generatePuzzle(int loop) {
		int[][] generate = new int[6][6];
		generate = deepCopyArr(goal);
		int oZrow = 0;
		int oZcol = 2;

		//move tile up
		while (loop > 0) {
			Random rand = new Random();
			int ranMove = rand.nextInt(4)+1;
			if(ranMove == 1 && oZrow > 0 && generate[oZrow-1][oZcol] > 0) {
				int temp = generate[oZrow-1][oZcol];
				generate[oZrow-1][oZcol] = generate[oZrow][oZcol];
				generate[oZrow][oZcol] = temp;
				oZrow -= 1;
				loop--;
			}
			//move tile right
			else if(ranMove == 2 && oZcol < 5 && generate[oZrow][oZcol+1] > 0) {;
				int temp = generate[oZrow][oZcol+1];
				generate[oZrow][oZcol+1] = generate[oZrow][oZcol];
				generate[oZrow][oZcol] = temp;
				oZcol += 1;
			}
			//move tile down
			else if(ranMove == 3 && oZrow < 5 && generate[oZrow+1][oZcol] > 0) {
				int temp = generate[oZrow+1][oZcol];
				generate[oZrow+1][oZcol] = generate[oZrow][oZcol];
				generate[oZrow][oZcol] = temp;
				oZrow += 1;
				loop--;
			}
			//move tile left
			else if(ranMove == 4 && oZcol > 0 && generate[oZrow][oZcol-1] > 0) {
				int temp = generate[oZrow][oZcol-1];
				generate[oZrow][oZcol-1] = generate[oZrow][oZcol];
				generate[oZrow][oZcol] = temp;
				oZcol-= 1;
				loop--;
			}
		}
		zeroPos[0] = oZrow;
		zeroPos[1] = oZcol;
		return generate;
	}
	
}

