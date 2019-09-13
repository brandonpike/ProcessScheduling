import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class Driver {
	
	public static String[] SCHEDULER_TYPES = { "FCFS", "RR", "SPN", "SRT", "HRRN", "FB", "ALL" };
	public static String MARKER = "X";
	public static int totalServiceTime = 0;
	
	public static void main(String args[]) {
		
		String fileName = "jobs.txt";
		// If running from command line
		//if(args.length == 0) { System.out.print("Enter a schedule type after program name.");System.exit(0); }
		String scheduleType = "ALL";//args[0];
		scheduleType = scheduleType.toUpperCase();
		
		if(!isValidScheduler(scheduleType)) {
			System.out.println("ERROR: Scheduler choice invalid <FCFS, RR, SPN, SRT, HRRN, FB, ALL>");
			System.exit(0);
		}
		
		ArrayList lines = new ArrayList<>();
		try(Scanner scan = new Scanner(new File(fileName))){
			while(scan.hasNextLine())
				lines.add(scan.nextLine());
		} catch (FileNotFoundException e){ System.err.println(fileName + " was not found."); System.exit(0); }
		
		String jobName[] = new String[lines.size()];
		int jobArrival[] = new int[lines.size()];
		int jobServiceTime[] = new int[lines.size()];
		for(int i=0; i<lines.size();i++) {
			String output[] = ((String) lines.get(i)).split("\t");
			// Check file format
			if(output.length != 3) { System.out.print("\nERROR: Invalid file format."); System.exit(0); }
			jobName[i] = output[0]; 
			jobArrival[i] = Integer.parseInt(output[1]); 
			jobServiceTime[i] = Integer.parseInt(output[2]);
			totalServiceTime += jobServiceTime[i];
		}
		
		String output[][] = null;
		if(scheduleType.equalsIgnoreCase("FCFS")) // First Come First Serve
			output = FCFS(jobName, jobArrival, jobServiceTime);
		else if(scheduleType.equalsIgnoreCase("RR")) // 
			output = RR(jobName, jobArrival, jobServiceTime);
		else if(scheduleType.equalsIgnoreCase("SPN")) // Shortest Process Next
			output = SPN(jobName, jobArrival, jobServiceTime);
		else if(scheduleType.equalsIgnoreCase("SRT")) // Shortest Remaining Time
			output = SRT(jobName, jobArrival, jobServiceTime);
		else if(scheduleType.equalsIgnoreCase("HRRN")) // Highest Response Ratio Next
			output = HRRN(jobName, jobArrival, jobServiceTime);
		else if(scheduleType.equalsIgnoreCase("FB")) // Feedback
			output = FB(jobName, jobArrival, jobServiceTime);
		
		if(scheduleType.equalsIgnoreCase("ALL")) {
			printArray(FCFS(jobName, jobArrival, jobServiceTime), "FCFS");
			printArray(RR(jobName, jobArrival, jobServiceTime), "RR");
			printArray(SPN(jobName, jobArrival, jobServiceTime), "SPN");
			printArray(SRT(jobName, jobArrival, jobServiceTime), "SRT");
			printArray(HRRN(jobName, jobArrival, jobServiceTime), "HRRN");
			printArray(FB(jobName, jobArrival, jobServiceTime), "FB");
		}else { // Output them all
			printArray(output, scheduleType);
		}
	}
	
	// First Come First Serve
	public static String[][] FCFS(String[] jobName, int[] jobArrival, int[] jobServiceTime) {
		String output[][] = new String[jobName.length][totalServiceTime];
		int[] remainingServiceTime = new int[jobServiceTime.length];
		// Initialize
		for(int i=0; i<output.length; i++)
			for(int j=0; j<output[0].length; j++) output[i][j] = " ";
		for(int i=0; i<jobServiceTime.length; i++)
			remainingServiceTime[i] = jobServiceTime[i];
		// Increment each second of service
		for(int i=0; i<totalServiceTime; i++) {
			// Search through the jobs for one with service time left
			for(int j=0; j<jobName.length; j++) {
				if(remainingServiceTime[j] > 0) {
					output[j][i] = MARKER;
					remainingServiceTime[j]--;
					break;
				}
			}
		}
		return output;
	}
	
	// Round Robin
	public static String[][] RR(String[] jobName, int[] jobArrival, int[] jobServiceTime) { //q=1
		String output[][] = new String[jobName.length][totalServiceTime];
		int[] remainingServiceTime = new int[jobServiceTime.length];
		// Initialize
		for(int i=0; i<output.length; i++)
			for(int j=0; j<output[0].length; j++) output[i][j] = " ";
		for(int i=0; i<jobServiceTime.length; i++)
			remainingServiceTime[i] = jobServiceTime[i];
		// Increment each second of service
		int currentProcess = -1;
		Queue<Integer> queue = new LinkedList<>();
		for(int i=0; i<totalServiceTime; i++) {
			for(int j=0; j<jobServiceTime.length; j++) {
				if(i == jobArrival[j])
					queue.add(j);
			}
			if(currentProcess != -1){ // if there is currently a process running
				if(queue.size() > 0){
					queue.add(currentProcess);
				}
			}
			if(queue.size() > 0)
				currentProcess = queue.remove();
			if(remainingServiceTime[currentProcess] > 0){
				remainingServiceTime[currentProcess]--;
				output[currentProcess][i] = MARKER;
				if(remainingServiceTime[currentProcess] == 0)
					currentProcess = -1;
			}else{
				output[currentProcess][i] = "-";
			}
		}
		return output;
	}
	
	// Shortest Process Next
	public static String[][] SPN(String[] jobName, int[] jobArrival, int[] jobServiceTime) {
		String output[][] = new String[jobName.length][totalServiceTime];
		int[] remainingServiceTime = new int[jobServiceTime.length];
		// Initialize
		for(int i=0; i<output.length; i++)
			for(int j=0; j<output[0].length; j++) output[i][j] = " ";
		for(int i=0; i<jobServiceTime.length; i++)
			remainingServiceTime[i] = jobServiceTime[i];
		// Increment each second of service
		int currentProcess = -1;
		for(int i=0; i<totalServiceTime; i++) {
			// If we don't already have a shortest process
			if(currentProcess == -1) {
				int currentShortest = 0;
				for(int j=0; j<remainingServiceTime.length; j++) {
					if(jobArrival[j] <= i) {
						if(remainingServiceTime[j] > 0) {
							if(remainingServiceTime[currentShortest] == 0)
								currentShortest = j;
							else
								if(remainingServiceTime[j] < remainingServiceTime[currentShortest])
									currentShortest = j;
						}
					}
				}
				if(remainingServiceTime[currentShortest] > 0){
					remainingServiceTime[currentShortest]--;
					output[currentShortest][i] = MARKER;
					if(remainingServiceTime[currentShortest] > 0)
						currentProcess = currentShortest;
				}else{
					output[currentShortest][i] = "-";
				}
			} else { // We have a shortest process 
				remainingServiceTime[currentProcess]--;
				output[currentProcess][i] = MARKER;
				if(remainingServiceTime[currentProcess] == 0)
					currentProcess = -1;
			}
		}
		return output;
	}
	
	// Shortest Remaining Time
	public static String[][] SRT(String[] jobName, int[] jobArrival, int[] jobServiceTime) {
		String output[][] = new String[jobName.length][totalServiceTime];
		int[] remainingServiceTime = new int[jobServiceTime.length];
		// Initialize
		for(int i=0; i<output.length; i++)
			for(int j=0; j<output[0].length; j++) output[i][j] = " ";
		for(int i=0; i<jobServiceTime.length; i++)
			remainingServiceTime[i] = jobServiceTime[i];
		// Increment each second of service
		int currentProcess = -1;
		for(int i=0; i<totalServiceTime; i++) {
			// If we don't already have a shortest remaining process
			if(currentProcess == -1) {
				int currentShortest = 0;
				for(int j=0; j<remainingServiceTime.length; j++) {
					if(jobArrival[j] <= i) {
						if(remainingServiceTime[j] > 0) {
							if(remainingServiceTime[currentShortest] == 0)
								currentShortest = j;
							else
								if(remainingServiceTime[j] < remainingServiceTime[currentShortest])
									currentShortest = j;
						}
					}
				}
				if(remainingServiceTime[currentShortest] > 0){
					remainingServiceTime[currentShortest]--;
					output[currentShortest][i] = MARKER;
					if(remainingServiceTime[currentShortest] > 0)
						currentProcess = currentShortest;
				}else{
					output[currentShortest][i] = "-";
				}
			} else { // We have a shortest remaining process 
				int x = newArrival(jobArrival, remainingServiceTime, currentProcess, i);
				if(x == -1){ // no new arrival or isn't shorter than current
					remainingServiceTime[currentProcess]--;
					output[currentProcess][i] = MARKER;
					if(remainingServiceTime[currentProcess] == 0)
						currentProcess = -1;
				}else{
					currentProcess = x;
					remainingServiceTime[currentProcess]--;
					output[currentProcess][i] = MARKER;
					if(remainingServiceTime[currentProcess] == 0)
						currentProcess = -1;
				}
			}
		}
		return output;
	}
	
	// Highest Response Ratio Next
	public static String[][] HRRN(String[] jobName, int[] jobArrival, int[] jobServiceTime) {
		String output[][] = new String[jobName.length][totalServiceTime];
		int[] remainingServiceTime = new int[jobServiceTime.length];
		// Initialize
		for(int i=0; i<output.length; i++)
			for(int j=0; j<output[0].length; j++) output[i][j] = " ";
		for(int i=0; i<jobServiceTime.length; i++)
			remainingServiceTime[i] = jobServiceTime[i];
		// Increment each second of service
		int currentProcess = -1;
		for(int i=0; i<totalServiceTime; i++) {
			double[] ratio = getRatio(jobArrival, jobServiceTime, remainingServiceTime, i);
			// If we don't already have a shortest process
			if(currentProcess == -1) {
				int currentHighest = 0;
				for(int j=0; j<remainingServiceTime.length; j++) {
					if(jobArrival[j] <= i) {
						if(remainingServiceTime[j] > 0) {
							if(remainingServiceTime[currentHighest] == 0)
								currentHighest = j;
							else
								if(ratio[j] > ratio[currentHighest])
									currentHighest = j;
						}
					}
				}
				if(remainingServiceTime[currentHighest] > 0){
					remainingServiceTime[currentHighest]--;
					output[currentHighest][i] = MARKER;
					if(remainingServiceTime[currentHighest] > 0)
						currentProcess = currentHighest;
				}else{
					output[currentHighest][i] = "-";
				}
			} else { // We have a shortest process 
				remainingServiceTime[currentProcess]--;
				output[currentProcess][i] = MARKER;
				if(remainingServiceTime[currentProcess] == 0)
					currentProcess = -1;
			}
		}
		return output;
	}
	
	// Feedback
	public static String[][] FB(String[] jobName, int[] jobArrival, int[] jobServiceTime) { //q=1, 3 queues
		String output[][] = new String[jobName.length][totalServiceTime];
		int[] remainingServiceTime = new int[jobServiceTime.length];
		// Initialize
		for(int i=0; i<output.length; i++)
			for(int j=0; j<output[0].length; j++) output[i][j] = " ";
		for(int i=0; i<jobServiceTime.length; i++)
			remainingServiceTime[i] = jobServiceTime[i];
		// Increment each second of service
		int currentProcess = -1;
		Queue<Integer> topQueue = new LinkedList<>();
		Queue<Integer> middleQueue = new LinkedList<>();
		Queue<Integer> bottomQueue = new LinkedList<>();
		for(int i=0; i<totalServiceTime; i++) {
			int nextInLine = 0;
			String poppedFrom = "";
			for(int j=0; j<jobServiceTime.length; j++) {
				if(i == jobArrival[j])
					topQueue.add(j);
			}
			if(topQueue.size() > 0){
				nextInLine = topQueue.remove();
				poppedFrom = "TOP";
			}else if(middleQueue.size() > 0){
				nextInLine = middleQueue.remove();
				poppedFrom = "MIDDLE";
			}else if(bottomQueue.size() > 0){
				nextInLine = bottomQueue.remove();
				poppedFrom = "MIDDLE";
			}else
				System.out.print("ERROR: No jobs available");
			
			if(remainingServiceTime[nextInLine] > 0){
				remainingServiceTime[nextInLine]--;
				output[nextInLine][i] = MARKER;
				// If it still needs more time add it back to a queue
				if(remainingServiceTime[nextInLine] != 0){
					if(poppedFrom == "TOP"){
						middleQueue.add(nextInLine);
					}else if(poppedFrom == "MIDDLE"){
						if(topQueue.size() == 0 && middleQueue.size() == 0 && bottomQueue.size() == 0)
							middleQueue.add(nextInLine);
						else
							bottomQueue.add(nextInLine);
					}else if(poppedFrom == "BOTTOM"){
						bottomQueue.add(nextInLine);
					}else
						System.out.print("ERROR: Didn't pop from any queue");
				}
			}else{
				output[nextInLine][i] = "-";
			}
		}
		return output;
	}

	public static boolean isValidScheduler(String type) {
		for(int i=0; i<SCHEDULER_TYPES.length; i++) {
			if(type.equalsIgnoreCase(SCHEDULER_TYPES[i]))
				return true;
		}
		return false;
	}
	
	public static double[] getRatio(int[] jobArrival, int[] jobServiceTime, int[] remainingServiceTime, int i){
		double[] x = new double[jobArrival.length];
		for(int j=0; j<jobArrival.length; j++){
			x[j] = ((i-jobArrival[j])+jobServiceTime[j])/jobServiceTime[j];
			if(remainingServiceTime[j] == 0)
				x[j] = -1;
		}
		return x;
	}
	
	public static int newArrival(int[] jobArrival, int[] remainingServiceTime, int currentProcess, int currentTime){
		for(int i=0; i<jobArrival.length; i++){
			if(i!=currentProcess && jobArrival[i] == currentTime){
				if(remainingServiceTime[i] < remainingServiceTime[currentProcess])
					return i;
			}
		}
		return -1;
	}
	
	public static void printArray(String[][] array, String scheduleType) {
		System.out.println(scheduleType);
		for(int i=0; i<array.length; i++) {
			System.out.print((char)(i+65) + " "); // Job Letter
			for(int j=0; j<array[0].length; j++) {
				System.out.print(array[i][j]);
			}
			System.out.print("\n");
		}
	}
	
}
