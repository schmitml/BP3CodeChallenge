import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {
	private static final String filePath = "task-2.json";
	private static HashMap<Integer, Pair<String, Integer>> recentTasks;
	private static HashMap<String, Pair<Integer, Integer>> tasksByAssignee;

	public static void main(String[] args) {
		// Initial computation of instanceId and assignee information
		recentTasks = new HashMap<>();
		tasksByAssignee = new HashMap<>();
		parseJson();

		userInputLoop();
		

	}
	
	private static void userInputLoop(){
		boolean stop = false;
		String data;
		while(!stop){
			String input = getInput(
					"Options:\nEnter 1 to search for the most recent task for an instanceId"
					+ "\nEnter 2 to get the count of tasks for a particular instanceId"
					+ "\nEnter 3 to get the count of opened and closed tasks for a particular assignee"
					+ "\nEnter 4 to get the number of opened and closed tasks on a specific date"
					+ "\nEnter 5 to get the number of opened and closed tasks from a specific start and end date"
					+ "\nEnter q to exit");
			if(input.equals("")){
				System.out.println("Sorry, could you please enter one of the options above.");
			}else if(input.equals("1")){
				// Most Recent Task for an InstanceId
				data = getInput("Option 1: Please Enter the InstanceId");
				if(!data.equals("")){
					System.out.println(mostRecentTaskByInstanceId(Integer.valueOf(data)) +"\n");
				}else{
					System.out.println("Sorry, could you please enter one of the options above.");
				}
			}else if(input.equals("2")){
				// Count of tasks for an InstanceId
				data = getInput("Option 2: Please Enter the InstanceId");
				if(!data.equals("")){
					System.out.println(countTasksByInstanceId(Integer.valueOf(data)) + " Tasks \n");
				}else{
					System.out.println("Sorry, could you please enter one of the options above.");
				}
			}else if(input.equals("3")){
				// Count of opened and closed tasks for an assignee
				data = getInput("Option 3: Please Enter the Assignee");
				if(!data.equals("")){
					Pair<Integer, Integer> out= countTasksByAssignee(data);
					System.out.println("Opened Tasks: " + out.getValue1() + "\nClosed Tasks: " + out.getValue2());
				}else{
					System.out.println("Sorry, could you please enter one of the options above.");
				}
			}else if(input.equals("4")){
				// Count of opened and closed tasks on a specific date
				data = getInput("Option 4: Please Enter a specific date in the format of YYYY:MM:DD:HR:MM:SS");
				if(!data.equals("")){
					Calendar date = createDate(data);
					Pair<Integer, Integer> out = parseByDate(date);
					System.out.println("Opened Tasks: " + out.getValue1() + "\nClosed Tasks: " + out.getValue2());
				}else{
					System.out.println("Sorry, could you please enter one of the options above.");
				}
			}else if(input.equals("5")){
				// Count of opened and closed tasks in a specific range
				data = getInput("Option 5: Please Enter a specific date in the format of YYYY:MM:DD-HR:MM:SS,YYYY:MM:DD-HR:MM:SS");
				if(!data.equals("")){
					String[] arr = data.split(",");
					Calendar start = createDate(arr[0]);
					Calendar end = createDate(arr[1]);
					Pair<Integer, Integer> out = parseByDate(start, end);
					System.out.println("Opened Tasks: " + out.getValue1() + "\nClosed Tasks: " + out.getValue2());
				}else{
					System.out.println("Sorry, could you please enter one of the options above.");
				}
			}else if(input.equals("q") || input.equals("Q")){
				System.out.print("Finished!");
				return;
			}
		}
	}

	/**
	 * Helper method that takes a properly formated string input and returns a calendar object.
	 * 
	 * @param date
	 * @return
	 */
	private static Calendar createDate(String date){
		Calendar cal = Calendar.getInstance();
		String[] arr = date.split(":");
		cal.set(Integer.valueOf(arr[0]), Integer.valueOf(arr[1]), Integer.valueOf(arr[2]), Integer.valueOf(arr[3]), Integer.valueOf(arr[4]), Integer.valueOf(arr[5]));
		
		return cal;
	}
	
	/**
	 * Prompts for user input using the given prompt.
	 * 
	 * @param prompt
	 * @return user input
	 */
	private static String getInput(String prompt) {
		String mInput = "";
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			System.out.println(prompt);
			mInput = in.readLine();
		} catch (IOException e) {
			System.err.println("Error getting user input.");
			e.printStackTrace();
		}
		return mInput;
	}

	/**
	 * Goes through JSON file and enters elements into storage.
	 * 
	 */
	public static void parseJson() {

		JsonNode nodes = readFile(Main.filePath);

		for (JsonNode node : nodes) {

			// Task mTask = new Task(node);
			Integer instanceId = node.get("instanceId").asInt();
			String name = node.get("name").asText();
			String assignee = node.get("assignee").asText();
			String status = node.get("status").asText();

			// Sets RecentTasks
			Pair<String, Integer> recentTask;
			if (recentTasks.containsKey(instanceId)) {
				recentTask = recentTasks.get(instanceId);
				int count = (int) recentTask.getValue2() + 1;
				recentTask.setValues(name, count);
			} else {
				recentTask = new Pair<String, Integer>(name, 1);
			}
			recentTasks.put(instanceId, recentTask);

			// Value 1 represents open tasks, Value 2 represents closed tasks
			Pair<Integer, Integer> assigneeTask;
			if (tasksByAssignee.containsKey(assignee)) {
				// Increments existing pair if the assignee is already in the
				// hashMap
				assigneeTask = tasksByAssignee.get(assignee);

				// Increments Open or Closed Counter
				if (!status.equals("Closed")) {
					assigneeTask.setValue1(assigneeTask.getValue1() + 1);
				} else {
					assigneeTask.setValue2(assigneeTask.getValue2() + 1);
				}
			} else {
				// Creates a new pair if the assignee is not already in the
				// hashmap
				if (!status.equals("Closed")) {
					assigneeTask = new Pair<Integer, Integer>(1, 0);
				} else {
					assigneeTask = new Pair<Integer, Integer>(0, 1);
				}
			}
			tasksByAssignee.put(assignee, assigneeTask);
		}

	}

	/**
	 * Counts the number of opened and closed tasks in a specific range.
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	public static Pair<Integer, Integer> parseByDate(Calendar start, Calendar end) {
		int opened = 0;
		int closed = 0;

		JsonNode nodes = readFile(Main.filePath);

		for (JsonNode node : nodes) {
			String textCreateDate = node.get("createDate").asText();
			String textCloseDate = node.get("closeDate").asText();

			if (!textCloseDate.equals("null") && !textCreateDate.equals("null")) {
				Calendar createDate = parseDate(textCreateDate);
				Calendar closeDate = parseDate(textCloseDate);
				if (createDate.compareTo(start) >= 0 && createDate.compareTo(end) < 0) {
					opened++;
				} else if (closeDate.compareTo(start) >= 0 && closeDate.compareTo(end) < 0) {
					closed++;
				}
			}
		}
		return new Pair<Integer, Integer>(opened, closed);
	}

	/**
	 * Counts the current number of opened and closed tasks on a given date.
	 * 
	 * @param target
	 * @return
	 */
	public static Pair<Integer, Integer> parseByDate(Calendar target) {
		int opened = 0;
		int closed = 0;

		JsonNode nodes = readFile(Main.filePath);

		for (JsonNode node : nodes) {
			String textCreateDate = node.get("createDate").asText();
			String textCloseDate = node.get("closeDate").asText();

			Calendar createDate = parseDate(textCreateDate);

			if (!textCloseDate.equals("null")) {
				Calendar closeDate = parseDate(textCloseDate);
				if (createDate.compareTo(target) <= 0 && closeDate.compareTo(target) >= 0) {
					if (node.get("status").asText().equals("Closed")) {
						closed++;
					} else {
						opened++;
					}
				}
			} else if (textCloseDate.equals("null")) {
				if (createDate.compareTo(target) <= 0) {
					opened++;
				}

			}
		}

		return new Pair<Integer, Integer>(opened, closed);
	}

	/**
	 * Given a particular instanceId, provide the name of the most recent task
	 * 
	 * @param instanceId
	 * @return
	 */
	public static String mostRecentTaskByInstanceId(int instanceId) {
		return recentTasks.get(instanceId).getValue1();
	}

	/**
	 * Given a particular instanceId, provide the count of tasks.
	 * 
	 * @param instanceId
	 * @return
	 */
	public static int countTasksByInstanceId(int instanceId) {
		return recentTasks.get(instanceId).getValue2();
	}

	/**
	 * Given a particular assignee, provide the count of open and closed tasks
	 * for that assignee.
	 * 
	 * @param assignee
	 * @return
	 */
	public static Pair<Integer, Integer> countTasksByAssignee(String assignee) {
		return tasksByAssignee.get(assignee);
	}

	/**
	 * Reads the JSON elements at a given path and returns a JSON node.
	 * 
	 * @param path
	 * @return
	 */
	public static JsonNode readFile(String path) {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode nodes;
		try {
			nodes = mapper.readTree(new File(path));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return nodes;
	}

	/**
	 * Helper method that takes date information from JSON element and puts it
	 * in a format that java understands.
	 * 
	 * @param input
	 * @return
	 */
	public static Calendar parseDate(String input) {
		String[] strArr = input.split("T");

		// YEAR MONTH DAY
		String[] dateArr = strArr[0].split("-");

		// HOUR MINUTE SECOND
		String[] timeArr = strArr[1].split(":");
		timeArr[2] = timeArr[2].substring(0, timeArr[2].length() - 1);

		Calendar calendarDate = Calendar.getInstance();
		calendarDate.set(Integer.valueOf(dateArr[0]), Integer.valueOf(dateArr[1]), Integer.valueOf(dateArr[2]),
				Integer.valueOf(timeArr[0]), Integer.valueOf(timeArr[1]), Integer.valueOf(timeArr[2]));

		return calendarDate;
	}

	/**
	 * A generic pair of values used to return multiple values.
	 * 
	 * @author Marc Schmitt
	 *
	 * @param <T>
	 * @param <E>
	 */
	public static class Pair<T, E> {
		private T value1;
		private E value2;

		public Pair(T val1, E val2) {
			this.value1 = val1;
			this.value2 = val2;
		}

		public void setValues(T val1, E val2) {
			this.value1 = val1;
			this.value2 = val2;
		}

		public T getValue1() {
			return this.value1;
		}

		public E getValue2() {
			return this.value2;
		}

		public void setValue1(T val1) {
			this.value1 = val1;
		}

		public void setValue2(E val2) {
			this.value2 = val2;
		}
	}
}
