package edu.yonsei.icl.doric;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;

public class MBBFilter {
	Set<Pair<String,String>> filteringPairEquals;
	Set<Pair<String,String>> filteringPairWithin;
	Set<Pair<String,String>> filteringPairContains;
	Set<Pair<String,String>> filteringPairDisjoint;
	
	public MBBFilter() {
		this.filteringPairEquals =
			new HashSet<Pair<String,String>>();
		this.filteringPairWithin =
			new HashSet<Pair<String,String>>();
		this.filteringPairContains =
			new HashSet<Pair<String,String>>();
		this.filteringPairDisjoint =
			new HashSet<Pair<String,String>>();
	}

	public void createFilteringPair(
			SpatialIndex spatialIndex,
			SpatialEntitySet sourceSESet,
			SpatialEntitySet targetSESet) {
		for (SpatialEntitySet hrSESet :
				spatialIndex.hsMapping.values()) {
			SpatialEntitySet currentSourceSESet = 
				getSESetIntersection(hrSESet, sourceSESet);
			SpatialEntitySet currentTargetSESet =
				getSESetIntersection(hrSESet, targetSESet);
			
			for (Map.Entry<String, Geometry> csse:
					currentSourceSESet.mbbHash.entrySet()){
				Geometry csseMBB = csse.getValue();
				for (Map.Entry<String, Geometry> ctse:
						currentTargetSESet.mbbHash.entrySet()){
					Geometry ctseMBB = ctse.getValue();
					
					String csseID = csse.getKey();
					String ctseID = ctse.getKey();
					
					if (!csseMBB.equalsTopo(ctseMBB)) {
						this.filteringPairEquals.add(
								new ImmutablePair<String, String>(
										csseID, ctseID));
					}
					if (!csseMBB.within(ctseMBB)) {
						this.filteringPairWithin.add(
								new ImmutablePair<String, String>(
										csseID, ctseID));
					}
					if (!csseMBB.contains(ctseMBB)) {
						this.filteringPairContains.add(
								new ImmutablePair<String, String>(
										csseID, ctseID));
					}
					if (csseMBB.disjoint(ctseMBB)) {
						this.filteringPairDisjoint.add(
								new ImmutablePair<String, String>(
										csseID, ctseID));
					}
				}
			}
		}
	}
	
	public SpatialEntitySet getSESetIntersection(
			SpatialEntitySet seSet1,
			SpatialEntitySet seSet2) {
		SpatialEntitySet seSetIntersection =
			new SpatialEntitySet();
		
		if (seSet1.seHash.size()<seSet2.seHash.size()) {
			for (Map.Entry<String, MultiPolygon> se1:
					seSet1.seHash.entrySet()) {
				if (seSet2.seHash.containsKey(se1.getKey())) {
					seSetIntersection.addSE(
							se1.getKey(), se1.getValue());
				}
			}
		} else {
			for (Map.Entry<String, MultiPolygon> se2:
					seSet2.seHash.entrySet()) {
				if (seSet1.seHash.containsKey(se2.getKey())) {
					seSetIntersection.addSE(
							se2.getKey(), se2.getValue());
				}
			}
		}
		
		return seSetIntersection;
	}
	
	
}
