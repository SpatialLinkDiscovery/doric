package edu.yonsei.icl.doric;

import org.locationtech.jts.geom.Geometry;

public class MBBFilterTest {

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
		
		GranularityFactors granularityFactors =
				spatialIndex.getGranularityFactors(seSet);
		
		Geometry MBB = spatialIndex.getMBB(seSet);
		
		spatialIndex.createSHMapping(MBB, granularityFactors, seSet);
		spatialIndex.createHSMapping();
		spatialIndex.createHNSMapping();
		spatialIndex.createSortedHR();
		
		MBBFilter mbbFilter =
				new MBBFilter();
		mbbFilter.createFilteringPair(spatialIndex, seSet, seSet);
	}

}
