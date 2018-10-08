package edu.yonsei.icl.doric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

public class SpatialIndex {
	HashMap<Integer, Coordinate> hyperrectangles;
	HashMap<Pair<String, MultiPolygon>, Set<Integer>> shMapping;
	HashMap<Integer, SpatialEntitySet> hsMapping;
	HashMap<Integer, Integer> hnsMapping;
	ArrayList<Integer> sortedHyperrectangles;
	int numWidthHR;
	int numHeightHR;
	
	public SpatialIndex() {
		this.hyperrectangles =
				new HashMap<Integer, Coordinate>();
		this.shMapping =
				new HashMap<Pair<String, MultiPolygon>, Set<Integer>>();
		this.hsMapping =
				new HashMap<Integer, SpatialEntitySet>();
		this.hnsMapping =
				new HashMap<Integer, Integer>();
		this.sortedHyperrectangles =
				new ArrayList<Integer>();
	}
	
	public GranularityFactors getGranularityFactors(
			SpatialEntitySet seSet) {
		GranularityFactors granularityFactors =
				new GranularityFactors();
		double sumWidth = 0;
		double sumHeight = 0;
		
		for (Geometry currentMBB:
				seSet.mbbHash.values()) {
			sumWidth += getMBBWidth(currentMBB);
			sumHeight += getMBBHeight(currentMBB);
		}
		
		double averageWidth = 
				sumWidth/(double)seSet.getNumberOfSE();
		granularityFactors.graFacLongitude = 1/averageWidth;
		
		double averageHeight =
				sumHeight/(double)seSet.getNumberOfSE();
		granularityFactors.graFacLatitude = 1/averageHeight;
		
		return granularityFactors;
	}
	
	public double getMBBWidth(Geometry geometry) {
		double width = 0;
		
		Coordinate[] coordinates = geometry.getCoordinates();
		
		//if the MBB is a point, return 0
		if (coordinates.length==1) {
			return 0;
		} 
		//if the MBB is a linestring, calculate the width
		else if (coordinates.length==2) {
			return Math.abs(coordinates[0].x - coordinates[1].x);
		}
		//if the MBB is a rectangle, calculate the width
		else {
			int i = 0;
			while (i<coordinates.length-1 && width==0) {
				width = coordinates[i].x - coordinates[i+1].x;
				i++;
			}
			
			return Math.abs(width);
		}		
	}
	
	public double getMBBHeight(Geometry geometry) {
		double height = 0;
		Coordinate[] coordinates = geometry.getCoordinates();
		
		//if the MBB is a point, return 0
		if (coordinates.length==1) {
			return 0;
		} 
		//if the MBB is a linestring, calculate the width
		else if (coordinates.length==2) {
			return Math.abs(coordinates[0].y - coordinates[1].y);
		}
		//if the MBB is a rectangle, calculate the width
		else {
			int i = 0;
			while (i<coordinates.length-1 && height==0) {
				height = coordinates[i].y - coordinates[i+1].y;
				i++;
			}
			
			return Math.abs(height);
		}
	}
	
	public Geometry getMBB(SpatialEntitySet seSet) {
		Geometry MBB = new GeometryFactory().createPolygon();
		
		for (Geometry currentMBB :
				seSet.mbbHash.values()) {
			
			if (MBB.isEmpty()) {
				MBB = currentMBB;
			}else {
				Polygon[] temppolygons = 
					{convertMBBtoPolygon(MBB),
					 convertMBBtoPolygon(currentMBB)};
				MultiPolygon tempMPolygon =
					new GeometryFactory().createMultiPolygon(temppolygons);
				
				MBB = tempMPolygon.getEnvelope();
			}
		}
		
		return MBB;
	}
	
	/*public ArrayList<Integer> getHRList(String seID){
		ArrayList<Integer> hrList =
				new ArrayList<Integer>();
		
		for (SpatialEntity se: this.shMapping.keySet()) {
			if (se.ID.equals(seID)) {
				hrList = this.shMapping.get(se);
				break;
			}
		}
		
		return hrList;
	}*/
	
	public Polygon convertMBBtoPolygon(Geometry MBB) {
		Coordinate[] coordinates = MBB.getCoordinates();
		Polygon polygon = 
				new GeometryFactory().createPolygon(coordinates);
		
		return polygon;
	}
	
	//create hyperrectangles and shMapping
	public void createSHMapping(
			Geometry MBB, 
			GranularityFactors granularityFactors,
			SpatialEntitySet seSet) {
		double widthUnit =
				1/granularityFactors.graFacLongitude;
		double heightUnit = 
				1/granularityFactors.graFacLatitude;
		double minLong, maxLong, minLat, maxLat;
		
		Coordinate[] coordinates = MBB.getCoordinates();
		minLong = coordinates[0].x;
		maxLong = coordinates[2].x;
		minLat = coordinates[0].y;
		maxLat = coordinates[1].y;
		
		numWidthHR = (int)((maxLong-minLong)/widthUnit);
		if ((maxLong-minLong)%widthUnit!=0) {
			numWidthHR += 1;
		}

		numHeightHR = (int)((maxLat-minLat)/heightUnit);
		if ((maxLat-minLat)%heightUnit!=0) {
			numHeightHR += 1;
		}
		
		for (int i=0; i<numWidthHR*numHeightHR; i++) {
			double leftDownLong = 
					minLong + widthUnit*(i%numWidthHR);
			double leftDownLat = 
					minLat + heightUnit*(i/numWidthHR);
			this.hyperrectangles.put(i, 
					new Coordinate(leftDownLong, leftDownLat));
		}
		
		//create SHMapping
		for (Map.Entry<String, MultiPolygon> se:
				seSet.seHash.entrySet()) {
			Geometry tempMBB = se.getValue().getEnvelope();
			Coordinate[] tempCoordinates = 
					tempMBB.getCoordinates();
			double leftDownLong = tempCoordinates[0].x;
			double rightDownLong = tempCoordinates[3].x;
			double leftDownLat = tempCoordinates[0].y;
			double leftUpLat = tempCoordinates[1].y;
			
			int minIndexLong =
				(int)((leftDownLong - minLong)/widthUnit);
			int maxIndexLong =
				(int)((rightDownLong - minLong)/widthUnit);
			int minIndexLat =
				(int)((leftDownLat - minLat)/heightUnit);
			int maxIndexLat =
				(int)((leftUpLat - minLat)/heightUnit);
			
			Set<Integer> hrHash =
					new HashSet<Integer>();
			
			for (int j=minIndexLat; j<=maxIndexLat; j++) {
				for (int i=minIndexLong; i<=maxIndexLong; i++) {
					hrHash.add(i+j*numWidthHR);
				}
			}
			
			Pair<String, MultiPolygon> spatialEntity =
					new ImmutablePair<String, MultiPolygon>(
							se.getKey(), se.getValue());
			this.shMapping.put(spatialEntity, hrHash);
		}
	}
	
	public void createHSMapping(
			SpatialEntitySet sourceSESet) {
		//process source se first
		this.shMapping.forEach((se,hrHash)->{
			if (!hrHash.isEmpty()
					&&sourceSESet.seHash.containsKey(
							se.getLeft())) {
				for (int hrIndex: hrHash) {
					if (!this.hsMapping.containsKey(hrIndex)) {
						this.hsMapping.put(hrIndex, 
								new SpatialEntitySet());
					}
					this.hsMapping.get(hrIndex).addSE(
							se.getLeft(), se.getRight());
				}
			}
		});
		
		//process target se next
		this.shMapping.forEach((se,hrHash)->{
			if (!hrHash.isEmpty()
					&&!sourceSESet.seHash.containsKey(
							se.getLeft())) {
				for (int hrIndex: hrHash) {
					if (this.hsMapping.containsKey(hrIndex)) {
						this.hsMapping.get(hrIndex).addSE(
								se.getLeft(), se.getRight());
					}
				}
			}
		});
	}
	
	public void createHSMapping() {
		this.shMapping.forEach((se,hrHash)->{
			if (!hrHash.isEmpty()) {
				for (int hrIndex: hrHash) {
					if (!this.hsMapping.containsKey(hrIndex)) {
						this.hsMapping.put(hrIndex, 
								new SpatialEntitySet());
					}
					this.hsMapping.get(hrIndex).addSE(
							se.getLeft(), se.getRight());
				}
			}
		});
	}
	
	public void createHNSMapping() {
		this.hsMapping.forEach((hrIndex,seSet)->{
			this.hnsMapping.put(hrIndex, seSet.getNumberOfSE());
		});
	}
	
	public void createSortedHR() {
		this.sortedHyperrectangles =
			new ArrayList<Integer>(this.hnsMapping.keySet());
		ArrayList<Integer> numSEArray =
			new ArrayList<Integer>(this.hnsMapping.values());
		
		for (int i=0; i<hnsMapping.size(); i++) {
			for (int j=i+1; j<hnsMapping.size(); j++) {
				int numSEi = numSEArray.get(i);
				int numSEj = numSEArray.get(j);
				if (numSEi<numSEj) {
					numSEArray.set(i, numSEj);
					numSEArray.set(j, numSEi);
					
					int hrIndexi = 
						this.sortedHyperrectangles.get(i);
					int hrIndexj = 
						this.sortedHyperrectangles.get(j);
					this.sortedHyperrectangles.set(i, hrIndexj);
					this.sortedHyperrectangles.set(j, hrIndexi);
				}
			}
		}
	}
	
}
