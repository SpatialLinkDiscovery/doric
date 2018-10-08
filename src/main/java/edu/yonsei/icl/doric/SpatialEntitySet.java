package edu.yonsei.icl.doric;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

public class SpatialEntitySet {
	HashMap<String, MultiPolygon> seHash;
	HashMap<String, Geometry> mbbHash;
	static FileWriter fw;
    static BufferedWriter bw;
    static FileReader fr;
    static BufferedReader br;
	
	public SpatialEntitySet() {
		this.seHash =
				new HashMap<String, MultiPolygon>();
		this.mbbHash =
				new HashMap<String, Geometry>();
	}
	
	public SpatialEntitySet(SpatialEntitySet spatialEntitySet) {
		this.seHash = spatialEntitySet.seHash;
		this.mbbHash = spatialEntitySet.mbbHash;
	}
	
	public int getNumberOfSE() {
		return this.seHash.size();
	}
	
	public int getMaxNumPolygon() {
		int maxNumPolygon = 0;
		
		for (MultiPolygon currentMPolygon : 
				this.seHash.values()) {
			int numPolygon = currentMPolygon.getNumGeometries();
			if (maxNumPolygon < numPolygon) {
				maxNumPolygon = numPolygon;
			}
		}
		
		return maxNumPolygon;
	}
	
	public int getMPolygonMaxNumPoint() {
		int maxNumPoint = 0;
		
		for (MultiPolygon currentMPolygon : 
				this.seHash.values()) {
			int numPoint = currentMPolygon.getNumPoints();
			if (maxNumPoint < numPoint) {
				maxNumPoint = numPoint;
			}
		}
		
		return maxNumPoint;
	}
	
	public void printSEInfo() {
		System.out.println("# of SE: " + this.getNumberOfSE());
		System.out.println("max # of polygons of a multipolygon: "
				+ this.getMaxNumPolygon());
		System.out.println("max # of points of a geometry: "
				+ this.getMPolygonMaxNumPoint());
	}
	
	public Coordinate[] convertToCoordinateArray(
			ArrayList<Coordinate> coordinateArrayList) {
		Coordinate[] coordinates = 
				new Coordinate[coordinateArrayList.size()];
		
		for(int i=0; i<coordinateArrayList.size(); i++) {
			coordinates[i] = coordinateArrayList.get(i);
		}
		
		return coordinates;
	}
	
	public Polygon[] convertToPolygonArray(
			ArrayList<Polygon> polygonArrayList) {
		Polygon[] polygons = 
				new Polygon[polygonArrayList.size()];
		
		for(int i=0; i<polygonArrayList.size(); i++) {
			polygons[i] = polygonArrayList.get(i);
		}
		
		return polygons;
	}
	
	public SpatialLinkSet readNUTSFromTtl(String filename,
			SpatialLinkSet slSet) {
		String[] lineWords = null;
		Calendar cal = null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-HH:mm:ss");
    	
    	try {
    		fr = new FileReader(filename);
    		br = new BufferedReader(fr);
    		
    		String currentLine;
    		int lineNumber = 0;
    		
    		/*cal = Calendar.getInstance();
	        System.out.println("Start reading " + filename + 
	        		"..." + sdf.format(cal.getTime()));*/
	        
	        String currentID = "nuts:IS";
	        double longitude = 0;
	        double latitude = 0;
	        Coordinate[] coordinates;
	        ArrayList<Coordinate> coordinateArrayList =
	        		new ArrayList<Coordinate>();
	        Polygon[] polygons;
	        ArrayList<Polygon> polygonArrayList =
	        		new ArrayList<Polygon>();
	        
    		while((currentLine = br.readLine()) != null) {
    			currentLine = currentLine.replaceAll("\t","");
    			currentLine = currentLine.trim();
    			
    			//if the line starts with @ or _, skip the line
    			if(currentLine.startsWith("@") 
    					|| currentLine.startsWith("_")) {
    				lineNumber++;
    				continue;
    			}
    			if (currentLine.isEmpty()) {
					continue;
				}
    			lineWords = currentLine.split("\\s+");
    			String firstWord = lineWords[0];
    			//spatial link line
    			if (currentID.equals(firstWord)) {
					String secondWord = lineWords[1];
					if (secondWord.startsWith("spatial:")) {
						if (slSet.nutsRelations.contains(secondWord)) {
							String thirdWord = lineWords[2];
							slSet.addSpatialLinkNuts(firstWord,
								thirdWord, secondWord);
						}
					}
					lineNumber++;
	    			continue;
				}
    			//skip the geometry line
    			if (firstWord.endsWith("geometry")) {
    				lineNumber++;
        			continue;
				}
				//extract longitude
				if (firstWord.equals("[")) {
					String thirdWord = lineWords[2];
					thirdWord = thirdWord.substring(
							1, thirdWord.length()-2);
					longitude = Double.parseDouble(
							thirdWord);
					lineNumber++;
        			continue;
				}
				//extract latitude and create a coordinate
				if (firstWord.equals("geo:lat")) {
					String secondWord = lineWords[1];
					secondWord = secondWord.substring(
							1, secondWord.length()-1);
					latitude = Double.parseDouble(
							secondWord);
					coordinateArrayList.add(new Coordinate(
							longitude, latitude));
					lineNumber++;
        			continue;
				}
				//create a polygon, ) line
				if (lineWords.length<3) {
					coordinates = 
						convertToCoordinateArray(
								coordinateArrayList);
					polygonArrayList.add(
						new GeometryFactory().
						createPolygon(coordinates));
					coordinateArrayList.clear();
					lineNumber++;
        			continue;
				}
				//create a multipolygon, then a spatial entity
				if (firstWord.startsWith("nuts:")
						&& !currentID.equals(firstWord)) {
					polygons =
						convertToPolygonArray(polygonArrayList);
					
					MultiPolygon mPolygon = 
							new GeometryFactory().createMultiPolygon(polygons);
					seHash.put(currentID, mPolygon);
					mbbHash.put(currentID, mPolygon.getEnvelope());
					
					polygonArrayList.clear();
					currentID = firstWord;
				}
			}
    		
    		/*cal = Calendar.getInstance();
	        System.out.println("End reading " + filename +
	        		"..." + sdf.format(cal.getTime()));
    		System.out.println("Number of lines: " + lineNumber +
    				"," + filename);*/
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			if(br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(fr != null) {
				try {
					fr.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
    	
    	return slSet;
	}
	
	public SpatialLinkSet readGAGFromN3(String filename,
			SpatialLinkSet slSet) throws IOException {
		FileInputStream inputStream = null;
		Scanner sc = null;
		try {			
		    inputStream = new FileInputStream(filename);
		    sc = new Scanner(inputStream, "UTF-8");
		    
		    int processingLine = 0;
		    
		    while (sc.hasNextLine()) {
		    	processingLine++;
		    	/*if (processingLine%1000==0) {
					System.out.println("Current processing line is..." + processingLine);
				}*/
		    	//System.out.println("Current processing line is..." + processingLine);
		    	
		        String currentLine = sc.nextLine();
		        currentLine = currentLine.replaceAll(","," ");
    			currentLine = currentLine.trim();
    			
    			if (currentLine.isEmpty()) {
					continue;
				}
    			
    			String[] lineWords;
    			lineWords = currentLine.split("\\s+");
    			
    			//add existing spatial links to slSet
    			if (lineWords[1].equals("<http://geo.linkedopendata.gr/gag/ontology/ανήκει_σε>")) {
					slSet.addSpatialLink(lineWords[0], lineWords[2], "within");
				}
    			
    			//generate spatial entity set
    			if (lineWords[1].equals("<http://geo.linkedopendata.gr/gag/ontology/έχει_γεωμετρία>")) {
    				int i = 2;
    				
    				//process the first longitude, i.e. delete "MULTIPOLYGON(((
    				if (lineWords[2].length()<16) {
						i++;
						lineWords[3] = lineWords[3].substring(
								lineWords[3].lastIndexOf("(")+1);
					} else {
						lineWords[2] = lineWords[2].substring(
								lineWords[2].lastIndexOf("(")+1);
					}
    				
    				double longitude = 0;
    		        double latitude = 0;
    		        Coordinate[] coordinates;
    		        ArrayList<Coordinate> coordinateArrayList =
    		        		new ArrayList<Coordinate>();
    		        Polygon[] polygons;
    		        ArrayList<Polygon> polygonArrayList =
    		        		new ArrayList<Polygon>();
    				
    				for (; i < lineWords.length-1; i+=2) {
    					longitude = Double.parseDouble(lineWords[i]);
    					
    					//if the point is !not! the end of a polygon
						if (!lineWords[i+1].contains(")")) {
							latitude = Double.parseDouble(lineWords[i+1]);
							
							Coordinate coordinate = new Coordinate(
									longitude, latitude);
							coordinate = transformToWGS84(coordinate);
							coordinateArrayList.add(coordinate);
						} //if the point is the end of a polygon 
						else if (!lineWords[i+1].contains(">")) {
							//delete the suffix )
							lineWords[i+1] = lineWords[i+1].substring(
									0, lineWords[i+1].indexOf(")"));
							latitude = Double.parseDouble(lineWords[i+1]);
							
							Coordinate coordinate = new Coordinate(
									longitude, latitude);
							coordinate = transformToWGS84(coordinate);
							coordinateArrayList.add(coordinate);
							
							//if the first and last coordinates are not equal
							//coordinateArrayList = makeSelfClosed(coordinateArrayList);
							
							//create a polygon
							coordinates = 
									convertToCoordinateArray(
											coordinateArrayList);
							
							Polygon polygon = 
									new GeometryFactory().
									createPolygon(coordinates);
							
							if (polygon.isValid()) {
								polygonArrayList.add(polygon);
							}
							
							coordinateArrayList.clear();
							
							//delete the prefix (
							lineWords[i+2] = lineWords[i+2].substring(
									lineWords[i+2].lastIndexOf("(")+1);
						} //if the point is the end of a multipolygon
						else {
							//delete the suffix )
							lineWords[i+1] = lineWords[i+1].substring(
									0, lineWords[i+1].indexOf(")"));
							latitude = Double.parseDouble(lineWords[i+1]);
							
							Coordinate coordinate = new Coordinate(
									longitude, latitude);
							coordinate = transformToWGS84(coordinate);
							coordinateArrayList.add(coordinate);
							
							//if the first and last coordinates are not equal
							//coordinateArrayList = makeSelfClosed(coordinateArrayList);
							
							//create a polygon
							coordinates = 
									convertToCoordinateArray(
											coordinateArrayList);
							polygonArrayList.add(
								new GeometryFactory().
								createPolygon(coordinates));
							coordinateArrayList.clear();
							
							//create a multipolygon, then a spatial entity
							polygons =
									convertToPolygonArray(polygonArrayList);
							
							//test whether geometry is valid
							MultiPolygon multiPolygon = 
									new GeometryFactory().
									createMultiPolygon(polygons);
							
							if (multiPolygon.isValid()) {
								MultiPolygon mPolygon = 
										new GeometryFactory().createMultiPolygon(polygons);
								seHash.put(lineWords[0], mPolygon);
								mbbHash.put(lineWords[0], mPolygon.getEnvelope());
							} /*else {
								System.out.println("invalid geometry: "
										+ lineWords[0]);
							}*/
							
							polygonArrayList.clear();
						}
					}
				}
		    }
		    // note that Scanner suppresses exceptions
		    if (sc.ioException() != null) {
		        throw sc.ioException();
		    }
		} finally {
		    if (inputStream != null) {
		        inputStream.close();
		    }
		    if (sc != null) {
		        sc.close();
		    }
		}
		
		return slSet;
	}
	
	public SpatialLinkSet readCLCFromNt(String filename,
			SpatialLinkSet slSet) throws IOException {
		FileInputStream inputStream = null;
		Scanner sc = null;
		try {			
		    inputStream = new FileInputStream(filename);
		    sc = new Scanner(inputStream, "UTF-8");
		    
		    int processingLine = 0;
		    
		    while (sc.hasNextLine()) {
		    	processingLine++;
		    	/*if (processingLine%10000==0) {
					System.out.println("Current reading line is..." + processingLine);
				}*/
		    	/*System.out.println("Current reading line is..." 
		    			+ processingLine);*/
		    	
		        String currentLine = sc.nextLine();
		        currentLine = currentLine.replaceAll(","," ");
    			currentLine = currentLine.trim();
    			
    			if (currentLine.isEmpty()) {
					continue;
				}
    			
    			String[] lineWords;
    			lineWords = currentLine.split("\\s+");
    			
    			//add existing spatial links to slSet
    			/*if (lineWords[1].equals("<http://geo.linkedopendata.gr/gag/ontology/ανήκει_σε>")) {
					slSet.addSpatialLink(lineWords[0], lineWords[2], "within");
				}*/
    			
    			//generate spatial entity set
    			if (lineWords[1].equals(
    					"<http://www.opengis.net/ont/geosparql#asWKT>")) {
    				int i = 3;
    				
    				//process the first longitude, i.e. delete POLYGON((
    				
					lineWords[3] = lineWords[3].substring(
							lineWords[3].lastIndexOf("(")+1);
					    				
    				double longitude = 0;
    		        double latitude = 0;
    		        Coordinate[] coordinates;
    		        ArrayList<Coordinate> coordinateArrayList =
    		        		new ArrayList<Coordinate>();
    		        Polygon[] polygons;
    		        ArrayList<Polygon> polygonArrayList =
    		        		new ArrayList<Polygon>();
    				
    				for (; i < lineWords.length-1; i+=2) {
    					longitude = Double.parseDouble(lineWords[i]);
    					
    					//if the point is !not! the end of a polygon
						if (!lineWords[i+1].contains(")")) {
							latitude = Double.parseDouble(lineWords[i+1]);
							
							Coordinate coordinate = new Coordinate(
									longitude, latitude);
							coordinateArrayList.add(coordinate);
						} //if the point is the end of a polygon 
						else if (!lineWords[i+1].contains(">")) {
							//delete the suffix )
							lineWords[i+1] = lineWords[i+1].substring(
									0, lineWords[i+1].indexOf(")"));
							latitude = Double.parseDouble(lineWords[i+1]);
							
							Coordinate coordinate = new Coordinate(
									longitude, latitude);
							coordinateArrayList.add(coordinate);
							
							//if the first and last coordinates are not equal
							//coordinateArrayList = makeSelfClosed(coordinateArrayList);
							
							//create a polygon
							coordinates = 
									convertToCoordinateArray(
											coordinateArrayList);
							
							Polygon polygon = 
									new GeometryFactory().
									createPolygon(coordinates);
							
							if (polygon.isValid()) {
								polygonArrayList.add(polygon);
							}
							
							coordinateArrayList.clear();
							
							//delete the prefix (
							if (lineWords[i+2].contains("(")) {
								lineWords[i+2] = lineWords[i+2].substring(
										lineWords[i+2].lastIndexOf("(")+1);
							}
						} //if the point is the end of a multipolygon
						else {
							//delete the suffix )
							lineWords[i+1] = lineWords[i+1].substring(
									0, lineWords[i+1].indexOf(")"));
							latitude = Double.parseDouble(lineWords[i+1]);
							
							Coordinate coordinate = new Coordinate(
									longitude, latitude);
							coordinateArrayList.add(coordinate);
							
							//if the first and last coordinates are not equal
							//coordinateArrayList = makeSelfClosed(coordinateArrayList);
							
							//create a polygon
							coordinates = 
									convertToCoordinateArray(
											coordinateArrayList);
							Polygon polygon = 
									new GeometryFactory().
									createPolygon(coordinates);
							
							if (polygon.isValid()) {
								polygonArrayList.add(polygon);
							}
							coordinateArrayList.clear();
							
							//create a multipolygon, then a spatial entity
							polygons =
									convertToPolygonArray(polygonArrayList);
							
							//test whether geometry is valid
							MultiPolygon multiPolygon = 
									new GeometryFactory().
									createMultiPolygon(polygons);
							
							if (multiPolygon.isValid()
									&& !multiPolygon.isEmpty()) {
								MultiPolygon mPolygon = 
										new GeometryFactory().createMultiPolygon(polygons);
								seHash.put(lineWords[0], mPolygon);
								mbbHash.put(lineWords[0], mPolygon.getEnvelope());
							} /*else {
								System.out.println("invalid geometry: "
										+ lineWords[0]);
							}*/
							
							polygonArrayList.clear();
						}
					}
				}
		    }
		    // note that Scanner suppresses exceptions
		    if (sc.ioException() != null) {
		        throw sc.ioException();
		    }
		} finally {
		    if (inputStream != null) {
		        inputStream.close();
		    }
		    if (sc != null) {
		        sc.close();
		    }
		}
		
		return slSet;
	}
	
	public SpatialLinkSet readCLCFromNt(String filename,
			SpatialLinkSet slSet, int numSpatialEntity) throws IOException {
		FileInputStream inputStream = null;
		Scanner sc = null;
		try {			
		    inputStream = new FileInputStream(filename);
		    sc = new Scanner(inputStream, "UTF-8");
		    
		    int processingLine = 0;
		    
		    while (sc.hasNextLine() && seHash.size()<numSpatialEntity) {
		    	/*processingLine++;
		    	if (processingLine%10000==0) {
					System.out.println("Current reading line is..." + processingLine);
				}*/
		    	/*System.out.println("Current reading line is..." 
		    			+ processingLine);*/
		    	
		    	/*if (!spatialEntitySet.isEmpty()
		    			&& spatialEntitySet.size()%1000==0) {
					System.out.println("Current number of spatial entities is..."
							+ spatialEntitySet.size()/1000 + " k");
				}*/
		    	
		        String currentLine = sc.nextLine();
		        currentLine = currentLine.replaceAll(","," ");
    			currentLine = currentLine.trim();
    			
    			if (currentLine.isEmpty()) {
					continue;
				}
    			
    			String[] lineWords;
    			lineWords = currentLine.split("\\s+");
    			
    			//add existing spatial links to slSet
    			/*if (lineWords[1].equals("<http://geo.linkedopendata.gr/gag/ontology/ανήκει_σε>")) {
					slSet.addSpatialLink(lineWords[0], lineWords[2], "within");
				}*/
    			
    			//generate spatial entity set
    			if (lineWords[1].equals(
    					"<http://www.opengis.net/ont/geosparql#asWKT>")) {
    				int i = 3;
    				
    				//process the first longitude, i.e. delete POLYGON((
    				
					lineWords[3] = lineWords[3].substring(
							lineWords[3].lastIndexOf("(")+1);
					    				
    				double longitude = 0;
    		        double latitude = 0;
    		        Coordinate[] coordinates;
    		        ArrayList<Coordinate> coordinateArrayList =
    		        		new ArrayList<Coordinate>();
    		        Polygon[] polygons;
    		        ArrayList<Polygon> polygonArrayList =
    		        		new ArrayList<Polygon>();
    				
    				for (; i < lineWords.length-1; i+=2) {
    					longitude = Double.parseDouble(lineWords[i]);
    					
    					//if the point is !not! the end of a polygon
						if (!lineWords[i+1].contains(")")) {
							latitude = Double.parseDouble(lineWords[i+1]);
							
							Coordinate coordinate = new Coordinate(
									longitude, latitude);
							coordinateArrayList.add(coordinate);
						} //if the point is the end of a polygon 
						else if (!lineWords[i+1].contains(">")) {
							//delete the suffix )
							lineWords[i+1] = lineWords[i+1].substring(
									0, lineWords[i+1].indexOf(")"));
							latitude = Double.parseDouble(lineWords[i+1]);
							
							Coordinate coordinate = new Coordinate(
									longitude, latitude);
							coordinateArrayList.add(coordinate);
							
							//if the first and last coordinates are not equal
							//coordinateArrayList = makeSelfClosed(coordinateArrayList);
							
							//create a polygon
							coordinates = 
									convertToCoordinateArray(
											coordinateArrayList);
							
							Polygon polygon = 
									new GeometryFactory().
									createPolygon(coordinates);
							
							if (polygon.isValid()) {
								polygonArrayList.add(polygon);
							}
							
							coordinateArrayList.clear();
							
							//delete the prefix (
							if (lineWords[i+2].contains("(")) {
								lineWords[i+2] = lineWords[i+2].substring(
										lineWords[i+2].lastIndexOf("(")+1);
							}
						} //if the point is the end of a multipolygon
						else {
							//delete the suffix )
							lineWords[i+1] = lineWords[i+1].substring(
									0, lineWords[i+1].indexOf(")"));
							latitude = Double.parseDouble(lineWords[i+1]);
							
							Coordinate coordinate = new Coordinate(
									longitude, latitude);
							coordinateArrayList.add(coordinate);
							
							//if the first and last coordinates are not equal
							//coordinateArrayList = makeSelfClosed(coordinateArrayList);
							
							//create a polygon
							coordinates = 
									convertToCoordinateArray(
											coordinateArrayList);
							Polygon polygon = 
									new GeometryFactory().
									createPolygon(coordinates);
							
							if (polygon.isValid()) {
								polygonArrayList.add(polygon);
							}
							coordinateArrayList.clear();
							
							//create a multipolygon, then a spatial entity
							polygons =
									convertToPolygonArray(polygonArrayList);
							
							//test whether geometry is valid
							MultiPolygon multiPolygon = 
									new GeometryFactory().
									createMultiPolygon(polygons);
							
							if (multiPolygon.isValid()
									&& !multiPolygon.isEmpty()) {
								MultiPolygon mPolygon = 
										new GeometryFactory().createMultiPolygon(polygons);
								seHash.put(lineWords[0], mPolygon);
								mbbHash.put(lineWords[0], mPolygon.getEnvelope());
							} /*else {
								System.out.println("invalid geometry: "
										+ lineWords[0]);
							}*/
							
							polygonArrayList.clear();
						}
					}
				}
		    }
		    // note that Scanner suppresses exceptions
		    if (sc.ioException() != null) {
		        throw sc.ioException();
		    }
		} finally {
		    if (inputStream != null) {
		        inputStream.close();
		    }
		    if (sc != null) {
		        sc.close();
		    }
		}
		
		return slSet;
	}
	
	public Coordinate transformToWGS84(
			Coordinate coordinate) {
		//WGS84 18.2700, 33.2300, 29.9700, 41.7700
		//EPSG:2100 -34387.6695, 3691163.5140, 1056496.8434, 4641211.3222
		double longitude = coordinate.x;
		longitude = 18.27 + ((longitude+34387.6695)/93237.9925);
		
		double latitude = coordinate.y;
		latitude = 33.23 + ((latitude-3691163.5140)/111246.8159);
		
		coordinate.x = longitude;
		coordinate.y = latitude;
		
		return coordinate;
	}
	
	public ArrayList<Coordinate> makeSelfClosed(
			ArrayList<Coordinate> coordinateArrayList) {
		Coordinate first = coordinateArrayList.get(0);
		Coordinate last = coordinateArrayList.get(
							coordinateArrayList.size()-1);
		
		if (!(first.x==last.x && first.y==last.y)) {
			coordinateArrayList.add(new Coordinate(
					first.x, first.y));
		}
		
		return coordinateArrayList;
	}
	
	public void printMPolygon(String seID) {
		MultiPolygon mPolygon = this.seHash.get(seID);
		
		Coordinate[] coordinates =
				mPolygon.getCoordinates();
		
		Coordinate firstCoordinate = coordinates[0];
		System.out.println(
				firstCoordinate.x + " "
				+ firstCoordinate.y);
		
		for (int i=1; i<coordinates.length; i++) {
			System.out.println(
					coordinates[i].x + " "
					+ coordinates[i].y);
			
			if (firstCoordinate.equals(coordinates[i])
					&& i<coordinates.length-1) {
				System.out.println("----------------------");
				System.out.println("This is a new polygon!");
				System.out.println("----------------------");
					
				firstCoordinate = coordinates[i+1];
				System.out.println(
						firstCoordinate.x + " "
						+ firstCoordinate.y);
					
				i++;
			}
		}
	}
	
	public void addSE(String seID, MultiPolygon mPolygon) {
		this.seHash.put(seID, mPolygon);
		this.mbbHash.put(seID, mPolygon.getEnvelope());
	}
}
