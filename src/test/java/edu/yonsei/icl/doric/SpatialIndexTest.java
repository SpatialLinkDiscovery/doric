package edu.yonsei.icl.doric;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

public class SpatialIndexTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String nutsTtlFile =
				"dataset/nuts.ttl";
		SpatialLinkSet slSet =
				new SpatialLinkSet();
		
		SpatialEntitySet seSet =
				new SpatialEntitySet();
		slSet = seSet.readNUTSFromTtl(nutsTtlFile, slSet);
		
		SpatialIndex spatialIndex = new SpatialIndex();
		
//		testCreateSortedHR(spatialIndex, seSet);
		
//		testCreateHSMapping(spatialIndex,seSet);
		
//		testCreateSHMapping(spatialIndex, seSet);
				
		testGranularityFactors(spatialIndex,seSet);
		
//		testGetMBB(spatialIndex,seSet);
	}
	
	public static void testGranularityFactors(
			SpatialIndex spatialIndex,
			SpatialEntitySet seSet) {
		GranularityFactors granularityFactors =
				new GranularityFactors();
		
		granularityFactors = 
				spatialIndex.getGranularityFactors(seSet);
		
		System.out.println("graFacLongitude: " 
				+ granularityFactors.graFacLongitude);
		System.out.println("graFacLatitude: "
				+ granularityFactors.graFacLatitude);
	}
	
	public static void testGetMBB(
			SpatialIndex spatialIndex,
			SpatialEntitySet seSet) {
		Geometry MBB = spatialIndex.getMBB(seSet);
		Coordinate[] coordinates = MBB.getCoordinates();
		for (int i=0; i<4; i++) {
			System.out.println(coordinates[i]);
		}
	}
	
	public static void testCreateSHMapping(
			SpatialIndex spatialIndex,
			SpatialEntitySet seSet) {
		GranularityFactors granularityFactors =
				spatialIndex.getGranularityFactors(seSet);
		
		Geometry MBB = spatialIndex.getMBB(seSet);
		
		spatialIndex.createSHMapping(MBB, granularityFactors, seSet);
	}
	
	public static void testCreateHSMapping(
			SpatialIndex spatialIndex,
			SpatialEntitySet seSet) {
		testCreateSHMapping(spatialIndex,seSet);
		spatialIndex.createHSMapping();
	}
	
	public static void testCreateSortedHR(
			SpatialIndex spatialIndex,
			SpatialEntitySet seSet) {
		testCreateHSMapping(spatialIndex,seSet);
		spatialIndex.createHNSMapping();
		spatialIndex.createSortedHR();
	}
}
