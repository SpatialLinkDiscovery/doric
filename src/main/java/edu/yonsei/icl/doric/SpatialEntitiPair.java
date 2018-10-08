package edu.yonsei.icl.doric;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.locationtech.jts.geom.MultiPolygon;

public class SpatialEntitiPair {
	Pair<String, MultiPolygon> sourceSE;
	Pair<String, MultiPolygon> targetSE;
	
	public SpatialEntitiPair() {
		// TODO Auto-generated constructor stub
		this.sourceSE = new ImmutablePair<String, MultiPolygon>(null, null);
		this.targetSE = new ImmutablePair<String, MultiPolygon>(null, null);
	}

	public SpatialEntitiPair(Pair<String, MultiPolygon> sourceSE,
			Pair<String, MultiPolygon> targetSE) {
		this.sourceSE = sourceSE;
		this.targetSE = targetSE;
	}
	
}
