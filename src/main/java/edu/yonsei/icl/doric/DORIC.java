package edu.yonsei.icl.doric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.Templates;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;

public class DORIC {
	Result equalsResult;
	Result withinResult;
	Result containsResult;
	Result disjointResult;
	Result touchesResult;
	Result overlapsResult;
	
	long numSameID;
	long numFiltering;
	long numComposition;
	long numDE9IM;
	long numDE9IMHolds;
	long numHRDisjoint;
	long indexingTime;
	long otrTime;
	
//	boolean composeWitDisDis;
//	boolean composeDisConDis;
//	long disjointComputation;
//	long numDisjointAlreadyExist;
	
	public DORIC() {
		this.indexingTime = 0;
		this.otrTime = 0;
		
//		this.disjointComputation = 0;
//		this.numDisjointAlreadyExist = 0;
	}
	
	public SpatialLinkSet execute(
			SpatialEntitySet sourceSESet,
			SpatialEntitySet targetSESet,
			SpatialLinkSet slSet) {
		//to discover motivate spatial link composition
//		this.composeWitDisDis = true;
//		this.composeDisConDis = false;
		
		//index sourceSESet and targetSESet
		long startTime = System.currentTimeMillis();
		SpatialEntitySet mergedSESet =
				getSESetUnion(sourceSESet, targetSESet);
		
		SpatialIndex spatialIndex = new SpatialIndex();
		
		GranularityFactors granularityFactors =
				spatialIndex.getGranularityFactors(mergedSESet);
		
		Geometry MBB = spatialIndex.getMBB(mergedSESet);
		
		spatialIndex.createSHMapping(
				MBB, granularityFactors, mergedSESet);
		//spatialIndex.createHSMapping();
		spatialIndex.createHSMapping(sourceSESet);
		spatialIndex.createHNSMapping();
		spatialIndex.createSortedHR();
		
		long finishTime = System.currentTimeMillis();
        this.indexingTime = finishTime - startTime;
		
		//compute discovery order of TR
		//and generate filtering pair list
        startTime = System.currentTimeMillis();
		MBBFilter mbbFilter =
				new MBBFilter();
		mbbFilter.createFilteringPair(
				spatialIndex, sourceSESet, targetSESet);
		
		ArrayList<String> OTR =
				getDiscoveryOrder(
						mbbFilter, sourceSESet, targetSESet);
		
//		OTR = getReverseDiscoveryOrder(OTR);
		
		finishTime = System.currentTimeMillis();
		this.otrTime = finishTime - startTime;
		
		//discover spatial links with TR
		for (int i=0; i<6; i++) {
			startTime = System.currentTimeMillis();
			
			this.numSameID = 0;
			this.numFiltering = 0;
			this.numComposition = 0;
			this.numDE9IM = 0;
			this.numDE9IMHolds = 0;
			
			System.out.println("Start processing relation...{"
					+ OTR.get(i) + "}");
						
			slSet = discoverSpatialLinks(
					sourceSESet, targetSESet, mbbFilter,
					spatialIndex, slSet, OTR.get(i));
			
			finishTime = System.currentTimeMillis();
	        long executionTime = finishTime - startTime;
	        
	        if (OTR.get(i).equals("equals")) {
				this.equalsResult =
					new Result(numSameID, numFiltering,
							numComposition, numDE9IM, 
							numDE9IMHolds, executionTime);
			}
	        if (OTR.get(i).equals("within")) {
				this.withinResult =
					new Result(numSameID, numFiltering,
							numComposition, numDE9IM, 
							numDE9IMHolds, executionTime);
			}
	        if (OTR.get(i).equals("contains")) {
				this.containsResult =
					new Result(numSameID, numFiltering,
							numComposition, numDE9IM, 
							numDE9IMHolds, executionTime);
			}
	        if (OTR.get(i).equals("disjoint")) {
				this.disjointResult =
					new Result(numSameID, numFiltering,
							numComposition, numDE9IM, 
							numDE9IMHolds, executionTime);
			}
	        if (OTR.get(i).equals("touches")) {
				this.touchesResult =
					new Result(numSameID, numFiltering,
							numComposition, numDE9IM, 
							numDE9IMHolds, executionTime);
			}
	        if (OTR.get(i).equals("overlaps")) {
				this.overlapsResult =
					new Result(numSameID, numFiltering,
							numComposition, numDE9IM, 
							numDE9IMHolds, executionTime);
			}
		}
		
		printInfo(OTR);
		
		/*System.out.println("num of disjoint found based on HRIndex is: "
				+ newDisjoint);*/
		
		return slSet;
	}
	
	public ArrayList<String> getDiscoveryOrder(
			MBBFilter mbbFilter,
			SpatialEntitySet sourceSESet,
			SpatialEntitySet targetSESet){
		ArrayList<String> OTR =
				new ArrayList<String>();
		
		ArrayList<Double> scoreArray =
				new ArrayList<Double>();
		
		double totalNumber = 
			sourceSESet.getNumberOfSE() * targetSESet.getNumberOfSE();
		double filteringFactorEquals =
			1-((double)(mbbFilter.filteringPairEquals.size()+1)
					/(totalNumber+1));
		double filteringFactorWithin =
			1-((double)(mbbFilter.filteringPairWithin.size()+1)
					/(totalNumber+1));
		double filteringFactorContains =
			1-((double)(mbbFilter.filteringPairContains.size()+1)
					/(totalNumber+1));
		double filteringFactorDisjoint =
			1-((double)(mbbFilter.filteringPairDisjoint.size()+1)
					/(totalNumber+1));
		
		//add scores
		scoreArray.add(1.2/filteringFactorEquals);
		scoreArray.add(0.278/filteringFactorWithin);
		scoreArray.add(0.139/filteringFactorContains);
		scoreArray.add(0.125/filteringFactorDisjoint);
		scoreArray.add(0.125);
		scoreArray.add(0.167);
		
		//add tr
		OTR.add("equals");
		OTR.add("within");
		OTR.add("contains");
		OTR.add("disjoint");
		OTR.add("touches");
		OTR.add("overlaps");
		
		//sort OTR
		for (int i=0; i<6; i++) {
			for (int j=i+1; j<6; j++) {
				double scorei = scoreArray.get(i);
				double scorej = scoreArray.get(j);
				if (scorei<scorej) {
					String stringi = OTR.get(i);
					String stringj = OTR.get(j);
					
					OTR.set(i, stringj);
					OTR.set(j, stringi);
					
					scoreArray.set(i, scorej);
					scoreArray.set(j, scorei);
				}
			}
		}
				
		return OTR;
	}
	
	public ArrayList<String> getReverseDiscoveryOrder(
			ArrayList<String> OTR){
		for (int i = 0; i < OTR.size()/2; i++) {
			String temp = OTR.get(i);
			OTR.set(i, OTR.get(OTR.size()-i-1));
			OTR.set(OTR.size()-i-1, temp);
		}
		
		return OTR;
	}
	
	public SpatialEntitySet getSESetUnion(
			SpatialEntitySet sourceSESet,
			SpatialEntitySet targetSESet) {
		SpatialEntitySet resultSESet = sourceSESet;
		
		for (Map.Entry<String, MultiPolygon> targetSE:
				targetSESet.seHash.entrySet()) {
			if (!resultSESet.seHash.containsKey(targetSE.getKey())) {
				resultSESet.addSE(
						targetSE.getKey(), targetSE.getValue());
			}
		}
		
		return resultSESet;
	}
	
	public SpatialLinkSet discoverSpatialLinks(
			SpatialEntitySet sourceSESet,
			SpatialEntitySet targetSESet,
			MBBFilter mbbFilter,
			SpatialIndex spatialIndex,
			SpatialLinkSet slSet,
			String tr) {
		SpatialLinkSet resultSLSet = slSet;
		
//		for (int i=0; i<spatialIndex.sortedHyperrectangles.size();
//				i++){
		//reverse hns
		for (int i=spatialIndex.sortedHyperrectangles.size()-1; 
				i>-1; i--){
			//processing intersects instead of disjoint 
			if (tr.equals("disjoint")) {
				tr = "intersects";
			}
			
			System.out.println("hyperrectangle processing for {"
					+ tr + "} ..."
					+ (i+1) + " of " 
					+ spatialIndex.sortedHyperrectangles.size());
			
			int hrIndex = 
					spatialIndex.sortedHyperrectangles.get(i);
			SpatialEntitySet hrSESet =
					spatialIndex.hsMapping.get(hrIndex);
			
			SpatialEntitySet currentSourceSESet =
					getSESetIntersection(
							hrSESet, sourceSESet);
			SpatialEntitySet currentTargetSESet =
					getSESetIntersection(
							hrSESet, targetSESet);
			
			for (Map.Entry<String, MultiPolygon> sourceEntry :
					currentSourceSESet.seHash.entrySet()) {
				for (Map.Entry<String, MultiPolygon> targetEntry :
						currentTargetSESet.seHash.entrySet()) {
					String sourceID = sourceEntry.getKey();
					String targetID = targetEntry.getKey();
					Pair<String, MultiPolygon> sourceSE =
							new ImmutablePair<String, MultiPolygon>(
									sourceEntry.getKey(),
									sourceEntry.getValue());
					Pair<String, MultiPolygon> targetSE =
							new ImmutablePair<String, MultiPolygon>(
									targetEntry.getKey(),
									targetEntry.getValue());
					
					/*if (tr.equals("disjoint")) {
						this.disjointComputation++;
					}*/
					
					//check whether sourceID==targetID
					if (tr.equals("equals")
							&& sourceID.equals(targetID)) {
						Triple<String, String, String> slEqual = 
								new ImmutableTriple<String, String, String>(
										sourceID, targetID, "equals");
						
						if (!resultSLSet.spatialLinkSet.contains(slEqual)) {
							this.numSameID++;
							
							resultSLSet.addSpatialLink(slEqual);
						}
					}
					
					//check whether (s,t,tr) exists
					else if (resultSLSet.doesExist(sourceID, targetID, tr)) {
							//do nothing, skip
						/*if (tr.equals("disjoint")) {
							this.numDisjointAlreadyExist++;
						}*/
					}
					
					//check whether (s,t,tr) is filterable
					else if (checkFilterable(
							sourceID, targetID, tr, mbbFilter)) {
						this.numFiltering++;
						
						if (tr.equals("disjoint")) {
							resultSLSet.addSpatialLink(
									sourceID, targetID, tr);
						}
					}
					
					//check whether (s,t,tr) is composable
					else if (checkComposable(sourceID, targetID,
								tr, resultSLSet)) {
						this.numComposition++;
						
						if (tr.equals("intersects")) {
							resultSLSet.addSpatialLink(
									sourceID, targetID, "disjoint");
						} else {
							resultSLSet.addSpatialLink(
									sourceID, targetID, tr);
						}
					}
					
					//check whether (s,t,tr) holds by DE-9IM
					else {
						this.numDE9IM++;

						if (checkDE9IM(sourceSE, targetSE, tr)) {
							this.numDE9IMHolds++;
							
							resultSLSet.addSpatialLink(
									sourceID, targetID, tr);
						}
						
						//if disjoint does not hold, add intersects
						else if (tr.equals("disjoint")) {
							resultSLSet.addSpatialLink(
									sourceID, targetID, "intersects");
						}
						else if (tr.equals("intersects")) {
							resultSLSet.addSpatialLink(
									sourceID, targetID, "disjoint");
						}
					}
				}
			}
		}
			
		return resultSLSet;
	}
	
	public SpatialLinkSet discoverDisjoint(
			SpatialEntitySet sourceSESet, SpatialEntitySet targetSESet,
			SpatialIndex spatialIndex, SpatialLinkSet slSet) {
		int index = 0;
		
		for (Map.Entry<String, MultiPolygon> sourceEntry:
				sourceSESet.seHash.entrySet()) {
			index++;
			System.out.println("disjoint relation processing..."
					+ index + " of " + sourceSESet.seHash.size());
			
			for (Map.Entry<String, MultiPolygon> targetEntry:
					targetSESet.seHash.entrySet()) {
				Pair<String, MultiPolygon> sourceSE =
						new ImmutablePair<String, MultiPolygon>(
								sourceEntry.getKey(), 
								sourceEntry.getValue());
				Pair<String, MultiPolygon> targetSE =
						new ImmutablePair<String, MultiPolygon>(
								targetEntry.getKey(), 
								targetEntry.getValue());
				
				
				Set<Integer> sourceHRHash =
						spatialIndex.shMapping.get(sourceSE);
				Set<Integer> targetHRHash =
						spatialIndex.shMapping.get(targetSE);
				
				//judge whether two spatial entities
				//share common hyperrectangel
				boolean hasCommonHR = false;
				for (int hrIndex:
						targetHRHash) {
					if (sourceHRHash.contains(hrIndex)) {
						hasCommonHR = true;
						break;
					}
				}
				
				//if there is no common hyperrectangle
				//two spatial entities are disjoint
				if (hasCommonHR==false) {
					slSet.addSpatialLink(sourceSE.getLeft(),
							targetSE.getLeft(), "disjoint");
					this.numHRDisjoint++;
				}
			}
		}
		
		return slSet;
	}
	
	public boolean checkFilterable(
			String sourceID, String targetID,
			String tr, MBBFilter mbbFilter) {
		Pair<String, String> sourceTargetIDPair =
				new ImmutablePair<String, String>(
						sourceID, targetID);
		
		if (tr.equals("equals")) {
			return mbbFilter.filteringPairEquals.contains(sourceTargetIDPair);
		}
		if (tr.equals("contains")) {
			return mbbFilter.filteringPairContains.contains(sourceTargetIDPair);
		}
		if (tr.equals("within")) {
			return mbbFilter.filteringPairWithin.contains(sourceTargetIDPair);
		}
		if (tr.equals("disjoint")) {
			return mbbFilter.filteringPairDisjoint.contains(sourceTargetIDPair);
		}
		
		return false;
	}
	
	public boolean checkComposable(
			String sourceID,
			String targetID,
			String tr,
			SpatialLinkSet slSet) {
		//in the case that processing intersects instead of disjoint 
		if (tr.equals("intersects")) {
			tr = "disjoint";
		}
		
		//for equals
		if (tr.equals("equals")) {
			if (checkComposableTr(
					sourceID, targetID, 
					slSet, "equals", "equals")) {
				return Boolean.TRUE;
			}
		}
		//for within
		if (tr.equals("within")) {
			if (checkComposableTr(
					sourceID, targetID, 
					slSet, "equals", "within")) {
				return Boolean.TRUE;
			}
			if (checkComposableTr(
					sourceID, targetID, 
					slSet, "within", "equals")) {
				return Boolean.TRUE;
			}
			if (checkComposableTr(
					sourceID, targetID, 
					slSet, "within", "within")) {
				return Boolean.TRUE;
			}
		}
		//for contains
		if (tr.equals("contains")) {
			if (checkComposableTr(
					sourceID, targetID, 
					slSet, "equals", "contains")) {
				return Boolean.TRUE;
			}
			if (checkComposableTr(
					sourceID, targetID, 
					slSet, "contains", "equals")) {
				return Boolean.TRUE;
			}
			if (checkComposableTr(
					sourceID, targetID, 
					slSet, "contains", "contains")) {
				return Boolean.TRUE;
			}
		}
		//for disjoint
		if (tr.equals("disjoint")) {
			if (checkComposableTr(
					sourceID, targetID, 
					slSet, "equals", "disjoint")) {
				return Boolean.TRUE;
			}
			if (checkComposableTr(
					sourceID, targetID, 
					slSet, "within", "disjoint")) {
				/*if (this.composeWitDisDis) {
					System.out.println("WitDisDis Example,"
						+ " sourceID - " + sourceID
						+ " intermediateID - "
						+ getComposableIntermediateID(
								sourceID, targetID, slSet,
								"within", "disjoint")
						+ " targetID - " + targetID);
					
					this.composeWitDisDis = Boolean.FALSE;
				}*/
				
				return Boolean.TRUE;
			}
			if (checkComposableTr(
					sourceID, targetID, 
					slSet, "disjoint", "equals")) {
				return Boolean.TRUE;
			}
			if (checkComposableTr(
					sourceID, targetID, 
					slSet, "disjoint", "contains")) {
				/*if (!this.composeDisConDis) {
					System.out.println("DisConDis Example,"
						+ " sourceID: " + sourceID
						+ " targetID: " + targetID);
					
					this.composeWitDisDis = true;
				}*/
				
				return Boolean.TRUE;
			}
		}
		//for touches
		if (tr.equals("touches")) {
			if (checkComposableTr(
					sourceID, targetID, 
					slSet, "equals", "touches")) {
				return Boolean.TRUE;
			}
			if (checkComposableTr(
					sourceID, targetID, 
					slSet, "touches", "equals")) {
				return Boolean.TRUE;
			}
		}
		//for overlaps
		if (tr.equals("overlaps")) {
			if (checkComposableTr(
					sourceID, targetID, 
					slSet, "equals", "overlaps")) {
				return Boolean.TRUE;
			}
			if (checkComposableTr(
					sourceID, targetID, 
					slSet, "overlaps", "equals")) {
				return Boolean.TRUE;
			}
		}
		
		return Boolean.FALSE;
	}
	
	public boolean checkComposableTr(
			String sourceID,
			String targetID,
			SpatialLinkSet slSet,
			String eLink1,
			String eLink2) {		
		Set<String> intermediateIDSet = 
				slSet.getTargetIDSet(sourceID, eLink1);
		
		if (intermediateIDSet==null
				|| intermediateIDSet.isEmpty()) {
			return Boolean.FALSE;
		} else {
			for (String intermediateID:
					intermediateIDSet) {
				if (slSet.doesExist(intermediateID, targetID, eLink2)) {
					return Boolean.TRUE;
				}
			}
			
			return Boolean.FALSE;
		}
	}
	
	public boolean checkDE9IM(
			Pair<String, MultiPolygon> sourceSE,
			Pair<String, MultiPolygon> targetSE,
			String tr) {
		MultiPolygon sourceMPolygon = sourceSE.getRight();
		MultiPolygon targetMPolygon = targetSE.getRight();
		
		if (tr.equals("equals")) {
			if (sourceMPolygon.equalsTopo(targetMPolygon)) {
				return Boolean.TRUE;
			}
			return Boolean.FALSE;
		}
		if (tr.equals("within")) {
			if (sourceMPolygon.within(targetMPolygon)) {
				return Boolean.TRUE;
			}
			return Boolean.FALSE;
		}
		if (tr.equals("contains")) {
			if (sourceMPolygon.contains(targetMPolygon)) {
				return Boolean.TRUE;
			}
			return Boolean.FALSE;
		}
		if (tr.equals("disjoint")) {
			if (sourceMPolygon.disjoint(targetMPolygon)) {
				return Boolean.TRUE;
			}
			return Boolean.FALSE;
			
			/*if (sourceMPolygon.intersects(targetMPolygon)) {
				return Boolean.FALSE;
			}
			return Boolean.TRUE;*/
		}
		if (tr.equals("touches")) {
			if (sourceMPolygon.touches(targetMPolygon)) {
				return Boolean.TRUE;
			}
			return Boolean.FALSE;
		}
		if (tr.equals("overlaps")) {
			if (sourceMPolygon.overlaps(targetMPolygon)) {
				return Boolean.TRUE;
			}
			return Boolean.FALSE;
		}
		if (tr.equals("intersects")) {
			if (sourceMPolygon.intersects(targetMPolygon)) {
				return Boolean.TRUE;
			}
		}
		
		return Boolean.FALSE;
	}
		
	public SpatialEntitySet getSESetIntersection(
			SpatialEntitySet sourceSESet,
			SpatialEntitySet targetSESet) {
		SpatialEntitySet seSetIntersection =
			new SpatialEntitySet();
		
		for (Map.Entry<String, MultiPolygon> targetSE:
				targetSESet.seHash.entrySet()) {
			if (sourceSESet.seHash.containsKey(targetSE.getKey())) {
				seSetIntersection.addSE(targetSE.getKey(),
						targetSE.getValue());
			}
		}
		
		return seSetIntersection;
	}
	
	/*public SpatialLinkSet discoverRemainingDisjoint(
			SpatialLinkSet slSet,
			SpatialEntitySet sourceSESet,
			SpatialEntitySet targetSESet) {
		int index = 0;
		
		for (String sourceID:
				sourceSESet.seHash.keySet()) {
			index++;
			System.out.println("disjoint relation processing..."
					+ index + " of " + sourceSESet.seHash.size());
			
			for (String targetID: 
					targetSESet.seHash.keySet()) {
				Pair<String, String> sourceTargetPair =
						new ImmutablePair<String, String>(
								sourceID, targetID);
				
				if (!slSet.sourceTargetPairIndex.containsKey(sourceTargetPair)) {
					slSet.addSpatialLink(sourceID, targetID, "disjoint");
				}
			}
		}
		
		return slSet;
	}*/
	
	public void printInfo(ArrayList<String> OTR) {
		for (int i=0; i<6; i++) {
			if (OTR.get(i).equals("equals")) {
				System.out.println("Result for equals...");
				this.equalsResult.printInfo();
			}
			if (OTR.get(i).equals("within")) {
				System.out.println("Result for within...");
				this.withinResult.printInfo();
			}
			if (OTR.get(i).equals("contains")) {
				System.out.println("Result for contains...");
				this.containsResult.printInfo();
			}
			if (OTR.get(i).equals("disjoint")) {
				System.out.println("Result for disjoint...");
				this.disjointResult.printInfo();
				System.out.println("numHRDisjoint: "
						+ this.numHRDisjoint);
			}
			if (OTR.get(i).equals("touches")) {
				System.out.println("Result for touches...");
				this.touchesResult.printInfo();
			}
			if (OTR.get(i).equals("overlaps")) {
				System.out.println("Result for overlaps...");
				this.overlapsResult.printInfo();
			}
			System.out.println();
		}
		
		System.out.println("Indexing time is..."
				+ this.indexingTime);
		System.out.println("getDiscoveryOrder() time is..."
				+ this.otrTime);
	}
	
}
