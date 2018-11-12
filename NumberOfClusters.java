import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class NumberOfClusters {

	// DECLARE GLOBAL VARIABLEs
	static Coordinates coord1xy = null, coord2xy = null, centroid1xy = null, centroid2xy = null;
	static int count = 0, count2 = 0;

	static ArrayList<ArrayList<Integer>> c1 = new ArrayList<ArrayList<Integer>>();
	static ArrayList<ArrayList<Integer>> outliers = new ArrayList<ArrayList<Integer>>();
	static ArrayList<ArrayList<Integer>> centroids = new ArrayList<ArrayList<Integer>>();

	static String pattern = "#.#######", pattern2 = "#.###";
	static DecimalFormat decFormat = new DecimalFormat(pattern), decFormat2 = new DecimalFormat(pattern2);

	public static void main(String args[]) throws IOException {

		// DECLARE METHOD VARIABLES
		File file;
		Scanner s;

		HashMap<Integer, Coordinates> map = new HashMap<Integer, Coordinates>();

		try {
			// READ IN FROM FILE:
			file = new File("/home/jane/Desktop/test");

			// SEPARATE X AND Y VALUES
			s = new Scanner(file);
			float f1 = 0, f2 = 0;

			while (s.hasNextLine()) {
				if (s.hasNext()) {
					f1 = Float.parseFloat(s.next());
					f2 = Float.parseFloat(s.next());
				}
				// ASSIGN X AND Y VALUES TO A MAP COORDINATE
				map.put(count, new Coordinates(f1, f2));
				count = count + 1;
			}
			s.close();
		}

		// CATCH FILE EXCEPTION IF PATH NOT FOUND
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// CALL REMAINING METHODS
		clusters(map);
		distances(c1, map);
	}

	public static void clusters(HashMap<Integer, Coordinates> map) {

		// FIND ROBOTS THAT ARE INTERATING WITHIN RANGE
		for (int i = 0; i < 50; i++) {

			// INITIALISE OUR MULTIDIMENSIONAL ARRAYLIST
			c1.add(new ArrayList<Integer>());

			for (int j = 0; j < 50; j++) {
				coord1xy = map.get(i);
				coord2xy = map.get(j);

				int robot = j + 1;

				// ARE ROBOTS COMMUNICATING WITHIN 10CM?				
				if (Math.sqrt((Math.pow((coord2xy.getX() - coord1xy.getX()), 2)
						+ Math.pow((coord2xy.getY() - coord1xy.getY()), 2))) < 0.10000001) {

					// PRINT OUT COMMUNICATING ROBOTS (INCLUDES X, Y)
					// System.out.println(robot + ": " + coord2xy.getX() + ", "
					// + coord2xy.getY() + " ");

					// ADD ROBOTS TO MULTIDIMENSIONAL ARRAYLIST
					c1.get(i).add(robot);

				}
			}
		}

		// MY NEIGHBOUR IS YOUR NEIGHBOUR
		extendedNeighbours(c1);
	}

	public static void extendedNeighbours(ArrayList<ArrayList<Integer>> c1) {

		// FIND ROBOTS IN CLUSTER - INCLUDING EXTENDED NEIGHBOURHOOD
		// PICKED 6 AS A RANOM NUMBER TO DOUBLE AND TRIPLE CHECK ALL THE EXTENDED NEIGHBOURS.
		for (int loop = 0; loop <= 6; loop++) {

			for (int l = 0; l < c1.size(); l++) {

				for (int m = l + 1; m < c1.size(); m++) {

					// REMOVE DUPLICATE ROWS FROM SEARCH
					if (c1.get(l).containsAll(c1.get(m)) && c1.get(m).containsAll(c1.get(l))) {
						c1.remove(m);
						l = 0;
						m = l + 1;
					}

					else if (c1.get(l).containsAll(c1.get(m))) {
						c1.remove(m);
						l = 0;
						m = l + 1;
					}

					else if (c1.get(m).containsAll(c1.get(l))) {
						c1.remove(l);
						l = 0;
						m = l + 1;
					}

					// CHECK FOR DUPLICATE ENTRIES AT:
					// BEGINNING OF ROW
					else if (c1.get(l).containsAll(c1.get(m).subList(0, 1))) {
						c1.get(l).addAll(c1.get(m));
						c1.remove(m);
					}

					// END OF ROW
					else if (c1.get(l).containsAll(c1.get(m).subList(c1.get(m).size() - 1, c1.get(m).size()))) {
						c1.get(l).addAll(c1.get(m));
						c1.remove(m);
					}

					// IF ROBOT IS ALONE ADD TO OUTLIERS MULTIDIMENSIONAL LIST
					else if(c1.get(l).size() == 1) {
						outliers.add(c1.get(l));
						c1.remove(l);
					}

					else if(c1.get(m).size() == 1) {
						outliers.add(c1.get(m));
						c1.remove(m);
					}

				}

			}
		}
		// REMOVE DUPLICATES FROM LIST
		removeDuplicates(c1);

	}

	public static void removeDuplicates(ArrayList<ArrayList<Integer>> c1) {

		// REMOVE DUPLICATE ROBOTS IN CLUSTERS
		Set<Integer> seen = new HashSet<Integer>();

		for (List<Integer> l : c1) {
			for (Iterator<Integer> iter = l.iterator(); iter.hasNext();) {
				if (!seen.add(iter.next())) {
					iter.remove();
				}
			}
		}

		// REMOVE EMPTY LISTS
		for (int i = 0; i < c1.size(); i++) {
			if (c1.get(i).isEmpty()) {
				c1.remove(i);
			}
		}

		// GET CLUSTER INFORMATION
		clusterInfo(c1, outliers);

	}

	// THIS METHOD PRINTS OUT: CLUSTERS, OUTLIERS, AND AVERAGE SIZE OF CLUSTERS
	public static ArrayList<ArrayList<Integer>> clusterInfo(ArrayList<ArrayList<Integer>> c1, ArrayList<ArrayList<Integer>> outliers) {

		double size = 0;

		for (int i = 0; i < c1.size(); i++) {
			if(c1.get(i).size() < 1) {				
				c1.remove(i);
			}

		}

		// PRINT OUT ROBOTS IN CLUSTERS (SORTED, NO, SIZE)
		System.out.println(c1.size() + " clusters");
		System.out.println(outliers.size() + " outliers" + "\n----------");

		for (int i = 0; i < c1.size(); i++) {
			Collections.sort(c1.get(i));

			System.out.println("No." + (i + 1) + " (size " + c1.get(i).size() + "): " + c1.get(i));
			size += c1.get(i).size();

		}

		System.out.println();

		for (int i = 0; i < outliers.size(); i++) {
			System.out.println("Outliers: " + outliers.get(i));
		}

		System.out.println("Average size of cluster: " + decFormat.format(size / c1.size()) + "\n");

		size = 0;
		return c1;

	}

	public static void distances(ArrayList<ArrayList<Integer>> c1, HashMap<Integer, Coordinates> map) {

		HashMap<Integer, Coordinates> map2 = new HashMap<Integer, Coordinates>();

		double totalDist = 0, totalDistX = 0, totalDistY = 0, totalDistCentroid = 0, maxDist = 0, minDist = 10, totAvgDistCentroid = 0;
		int robot1, robot2;

		// NUMBER OF CLUSTERS
		for (int i = 0; i < c1.size(); i++) {

			System.out.println("No." + (i+1));

			// LENGTH OF EACH CLUSTER
			for (int k = 0; k < c1.get(i).size(); k++) {
				for (int j = 0; j < c1.get(i).size(); j++) {

					// GET COORDINATES (X,Y) OF EACH ROBOT IN CLUSTER
					robot1 = c1.get(i).get(k);
					robot2 = c1.get(i).get(j);

					coord1xy = map.get(robot1 - 1);
					coord2xy = map.get(robot2 - 1);

					// DISTANCE FORMULA
					double distance = Math.sqrt((Math.pow((coord2xy.getX() - coord1xy.getX()), 2)
							+ Math.pow((coord2xy.getY() - coord1xy.getY()), 2)));

					// PRINT OUT DISTANCE BETWEEN EACH ROBOT
					// System.out.println("Distance from " + (robot1) + "(" + coord1xy.getX() + " " + coord1xy.getY() + ")" + " and " +
					//(robot2) + "(" + coord2xy.getX() + " " + coord2xy.getY() + ")" + ": " + distance + "\n");

					// TOTAL DISTANCE OF EACH CLUSTER
					totalDist += distance;
				}

				// CENTROID POSITIONS (SUM Xs and Ys)
				totalDistX += coord1xy.getX();
				totalDistY += coord1xy.getY();
			}

			// AVERAGE DISTANCE OF EACH CLUSTER
			double avg = (c1.get(i).size() * c1.get(i).size()) - c1.get(i).size();
			double avgDist = (totalDist / avg) * 100; // * 100 TO CONVERT TO CM

			System.out.println("Average distance from Neighbours: " + decFormat2.format(avgDist) + "cm");

			// GET CENTROID POSITION
			float centroidPosX = (float) (totalDistX / c1.get(i).size());
			float centroidPosY = (float) totalDistY / c1.get(i).size();


			// ASSIGN X AND Y VALUES TO A MAP COORDINATE
			map2.put(count2, new Coordinates(centroidPosX, centroidPosY));
			count2 = count2 + 1;

			System.out.println("Centroid Position: " + decFormat.format(centroidPosX) + ", " + decFormat.format(centroidPosY));

			for (int k = 0; k < c1.get(i).size(); k++) {


				// GET COORDINATES (X,Y) OF EACH ROBOT IN CLUSTER
				robot1 = c1.get(i).get(k);

				coord1xy = map.get(robot1-1);

				// DISTANCE FROM CENTROID FORMULA - * 100 to get cm
				double distCentroid = Math.sqrt((Math.pow((coord1xy.getX() - centroidPosX), 2)
						+ Math.pow((coord1xy.getY() - centroidPosY), 2)))*100;

				//PRINT DISTANCE FROM CENTROID
				//System.out.println(distCentroid);

				totalDistCentroid += distCentroid;

				if (maxDist < distCentroid) {
					maxDist = distCentroid;
				}

				if (minDist > distCentroid) {
					minDist = distCentroid;
				}

			}


			double avgDistCentroid = (totalDistCentroid / c1.get(i).size());

			totAvgDistCentroid += avgDistCentroid;

			System.out.println("Average distance from Centroid: " + decFormat2.format(avgDistCentroid) + "cm");
			System.out.println("Max distance from Centroid: " + decFormat2.format(maxDist) + "cm");
			System.out.println("Min distance from Centroid: " + decFormat2.format(minDist) + "cm" + "\n");

			totalDistCentroid = 0;

			// RESET TOTAL DISTANCE FOR NEXT CLUSTER
			totalDist = 0;
			totalDistX = 0;
			totalDistY = 0;
			maxDist = 0;
			minDist = 10;

		}

		System.out.println("Average overall density: " + decFormat2.format(totAvgDistCentroid/c1.size()) + "cm");
		totAvgDistCentroid = 0;
		centroids(map2);

	}

	public static void centroids(HashMap<Integer, Coordinates> map2) {

		int centroid1, centroid2;
		double totalDist = 0, avgDist=0;

		// FIND ROBOTS THAT ARE INTERATING WITHIN RANGE
		for (int i = 0; i < map2.size(); i++) {

			// INITIALISE OUR MULTIDIMENSIONAL ARRAYLIST
			centroids.add(new ArrayList<Integer>());

			for (int j = 0; j < map2.size(); j++) {
				centroid1xy = map2.get(i);
				centroid2xy = map2.get(j);

				int robot = j + 1;
				centroids.get(i).add(robot);

			}
		}

		// LENGTH OF EACH CLUSTER
		for (int k = 0; k < centroids.size(); k++) {
			for (int j = k+1; j < centroids.size(); j++) {

				// GET COORDINATES (X,Y) OF EACH ROBOT IN CLUSTER
				centroid1 = centroids.get(k).get(k);
				centroid2 = centroids.get(k).get(j);

				coord1xy = map2.get(centroid1 - 1);
				coord2xy = map2.get(centroid2 - 1);

				// DISTANCE FORMULA
				double distance = Math.sqrt((Math.pow((coord2xy.getX() - coord1xy.getX()), 2)
						+ Math.pow((coord2xy.getY() - coord1xy.getY()), 2)));

				totalDist += distance;

			}
		}

		// AVERAGE DISTANCE OF EACH CENTROID
		avgDist = (totalDist / centroids.size()) * 100;		
		System.out.println("Average Centroid distance from Centroids: " + decFormat2.format(avgDist) + "cm" + "\n");
	}

}