import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {
	private static final String filePath = "task-2.json";
	private static HashMap<Integer, Pair<String, Integer>> recentTasks;
	private static HashMap<String, Pair<Integer, Integer>> tasksByAssignee;

	public static void main(String[] args) {
		// Initialial computation of instanceId and assignee information
		recentTasks = new HashMap<>();
		tasksByAssignee = new HashMap<>();
		parseJson();

		// Pair<Integer, Integer> tasksRange =
		// parseByDate(parseDate("2014-10-06T23:32:33Z"),
		// parseDate("2014-10-08T23:32:33Z"));
		// System.out.println("Tasks Opened:" + tasksRange.getValue1() +
		// "\nTasks Closed:" + tasksRange.getValue2());
		//
		// Pair<Integer, Integer> tasksDate =
		// parseByDate(parseDate("2015-02-24T00:32:27Z"));
		// System.out.println("\nTasks Opened:" + tasksDate.getValue1() +
		// "\nTasks Closed:" + tasksDate.getValue2());
		//
		// System.out.println("Most Recent Name for instanceId 557 is:" +
		// recentTasks.get(557).getValue1()
		// + "\nThe number of tasks for instanceId 557 is:" +
		// recentTasks.get(557).getValue2());
		//
		// System.out.println("Assignee: Impact 2014 \nOpen Tasks:" +
		// tasksByAssignee.get("Impact 2014").getValue1()
		// + "\nClosed Tasks:" + tasksByAssignee.get("Impact
		// 2014").getValue2());

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
	 * Generic pair of values.
	 * 
	 * @author schmitml
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

	// /**
	// * Represents a JSON Task.
	// *
	// * @author Marc Schmitt
	// *
	// */
	// public static class Task {
	// private String instanceName;
	// private String dueDate;
	// private String priority;
	// private String closeDate;
	// private boolean instanceStatus;
	// private String assigneeType;
	// private String createDate;
	// private String name;
	// private String url;
	// private String assignee;
	// private int instanceId;
	// private String status;
	// private String variables;
	// private String processName;
	// private int id;
	//
	// public Task(JsonNode node) {
	// this(node.get("instanceName").asText(), node.get("dueDate").asText(),
	// node.get("priority").asText(),
	// node.get("closeDate").asText(), node.get("instanceStatus").asBoolean(),
	// node.get("assigneeType").asText(), node.get("createDate").asText(),
	// node.get("name").asText(),
	// node.get("url").asText(), node.get("assignee").asText(),
	// node.get("instanceId").asInt(),
	// node.get("status").asText(), "STATUS", node.get("processName").asText(),
	// node.get("id").asInt());
	// }
	//
	// public Task(String instanceName, String dueDate, String priority, String
	// closeDate, boolean instanceStatus,
	// String assigneeType, String createDate, String name, String url, String
	// assignee, int instanceId,
	// String status, String variables, String processName, int id) {
	//
	// this.instanceName = instanceName;
	// this.dueDate = dueDate;
	// this.priority = priority;
	// this.closeDate = closeDate;
	// this.instanceStatus = instanceStatus;
	// this.assigneeType = assigneeType;
	// this.createDate = createDate;
	// this.name = name;
	// this.url = url;
	// this.assignee = assignee;
	// this.instanceId = instanceId;
	// this.status = status;
	// this.variables = variables;
	// this.processName = processName;
	// this.id = id;
	// }
	//
	// @Override
	// public String toString() {
	// return this.instanceName;
	// }
	//
	// }
}
