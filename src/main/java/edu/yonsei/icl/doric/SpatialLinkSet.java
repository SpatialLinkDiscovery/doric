package edu.yonsei.icl.doric;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

public class SpatialLinkSet {
	Set<Triple<String, String, String>> spatialLinkSet;
	HashMap<String, StringPairSet> sourceIndex;
	HashMap<Pair<String, String>, Set<String>> sourceTRPairIndex;
//	HashMap<Pair<String, String>, Set<String>> sourceTargetPairIndex;
	ArrayList<String> nutsRelations;
	
	static FileWriter fw;
    static BufferedWriter bw;
    static FileReader fr;
    static BufferedReader br;
	
	public SpatialLinkSet() {
		// TODO Auto-generated constructor stub
		this.spatialLinkSet =
				new HashSet<Triple<String, String, String>>();
		this.sourceIndex =
				new HashMap<String, StringPairSet>();
		this.sourceTRPairIndex =
				new HashMap<Pair<String, String>, Set<String>>();
		/*this.sourceTargetPairIndex =
				new HashMap<Pair<String, String>, Set<String>>();*/
		
		this.nutsRelations = 
				new ArrayList<String>();
		initalizeNutsRelations();
	}
	
	public void addSpatialLink(Triple<String, String, String> spatialLink) {
		this.spatialLinkSet.add(spatialLink);
		
		//add to source target pair index
		/*Pair<String, String> sourceTargetPair = 
				new ImmutablePair<String, String>(
						spatialLink.getLeft(), spatialLink.getMiddle());
		if (!sourceTargetPairIndex.containsKey(sourceTargetPair)) {
			sourceTargetPairIndex.put(sourceTargetPair, 
					new HashSet<String>());
		} else {
			sourceTargetPairIndex.get(sourceTargetPair).add(spatialLink.getRight());
		}*/
		
		//add to source tr pair index
		Pair<String, String> sourceTRPair = 
				new ImmutablePair<String, String>(
						spatialLink.getLeft(), spatialLink.getRight());
		if (!sourceTRPairIndex.containsKey(sourceTRPair)) {
			sourceTRPairIndex.put(sourceTRPair, 
					new HashSet<String>());
		} else {
			sourceTRPairIndex.get(sourceTRPair).add(spatialLink.getMiddle());
		}
		
		//add to source index
		if (!sourceIndex.containsKey(spatialLink.getLeft())) {
			sourceIndex.put(spatialLink.getLeft(), 
					new StringPairSet());
		} //if sourceID already exists
		else {
			sourceIndex.get(spatialLink.getLeft()).addStringPair(
							spatialLink.getMiddle(),
							spatialLink.getRight());
		}
	}
	
	public void addSpatialLink(String sourceID,
			String targetID, String tr) {
		this.spatialLinkSet.add(
				new ImmutableTriple<String, String, String>(
						sourceID, targetID, tr));
		
		//add to source target pair index
		/*Pair<String, String> sourceTargetPair = 
				new ImmutablePair<String, String>(
						sourceID, targetID);
		if (!sourceTargetPairIndex.containsKey(sourceTargetPair)) {
			sourceTargetPairIndex.put(sourceTargetPair, 
					new HashSet<String>());
		} else {
			sourceTargetPairIndex.get(sourceTargetPair).add(tr);
		}*/
		
		//add to source tr pair index
		Pair<String, String> sourceTRPair = 
				new ImmutablePair<String, String>(
						sourceID, tr);
		if (!sourceTRPairIndex.containsKey(sourceTRPair)) {
			sourceTRPairIndex.put(sourceTRPair, 
					new HashSet<String>());
		} else {
			sourceTRPairIndex.get(sourceTRPair).add(targetID);
		}
		
		//add to source index
		if (!sourceIndex.containsKey(sourceID)) {
			sourceIndex.put(sourceID, 
					new StringPairSet());
		} //if sourceID already exists
		else {
			sourceIndex.get(sourceID).addStringPair(
							targetID, tr);
		}
	}
	
	public int getNumSL() {
		return this.spatialLinkSet.size();
	}
	
	public int getNumSL(String tr) {
		int count = 0;
				
		for (Triple<String, String, String> currentSL:
				spatialLinkSet) {
			if (currentSL.getRight().equals(tr)) {
				count++;
			}
		}
		
		return count;
	}
	
	public void printSLInfo() {
		System.out.println("# of SL: " + this.getNumSL());
		System.out.println("equals: " + this.getNumSL("equals"));
		System.out.println("within: " + this.getNumSL("within"));
		System.out.println("contains: " + this.getNumSL("contains"));
		System.out.println("disjoint: " + this.getNumSL("disjoint"));
		System.out.println("intersects: " + this.getNumSL("intersects"));
		System.out.println("touches: " + this.getNumSL("touches"));
		System.out.println("overlaps: " + this.getNumSL("overlaps"));
	}
	
	public void initalizeNutsRelations(){
		this.nutsRelations.add("spatial:DC");
		this.nutsRelations.add("spatial:EC");
		this.nutsRelations.add("spatial:EQ");
		this.nutsRelations.add("spatial:NTPP");
		this.nutsRelations.add("spatial:NTPPi");
		this.nutsRelations.add("spatial:O");
		this.nutsRelations.add("spatial:P");
		this.nutsRelations.add("spatial:PO");
		this.nutsRelations.add("spatial:Pi");
		this.nutsRelations.add("spatial:TPP");
		this.nutsRelations.add("spatial:TPPi");
	}
	
	public void addSpatialLinkNuts(String sourceSEID,
			String targetSEID, String nutsRelation) {
		Triple<String, String, String> spatialLink = null;
		
		if (nutsRelation.equals("spatial:EQ")) {
			spatialLink = new ImmutableTriple<String, String, String>
					(sourceSEID, targetSEID, "equals");
			
		}
		if (nutsRelation.equals("spatial:NTPP")
			|| nutsRelation.equals("spatial:TPP")) {
			spatialLink = new ImmutableTriple<String, String, String>
					(sourceSEID, targetSEID, "within");
		}
		if (nutsRelation.equals("spatial:NTPPi")
			|| nutsRelation.equals("spatial:Pi")
			|| nutsRelation.equals("spatial:TPPi")) {
			spatialLink = new ImmutableTriple<String, String, String>
					(sourceSEID, targetSEID, "contains");
		}
		if (nutsRelation.equals("spatial:DC")) {
			spatialLink = new ImmutableTriple<String, String, String>
					(sourceSEID, targetSEID, "disjoint");
		}
		if (nutsRelation.equals("spatial:EC")) {
			spatialLink = new ImmutableTriple<String, String, String>
					(sourceSEID, targetSEID, "touches");
		}
		if (nutsRelation.equals("spatial:O")
			|| nutsRelation.equals("spatial:P")
			|| nutsRelation.equals("spatial:PO")) {
			spatialLink = new ImmutableTriple<String, String, String>
					(sourceSEID, targetSEID, "overlaps");
		}
		
		this.addSpatialLink(spatialLink);
	}
	
	public boolean doesExist(String sourceID,
			String targetID, String tr) {
		Triple<String, String, String> triple =
				new ImmutableTriple<String, String, String>(
						sourceID, targetID, tr);
		
		return this.spatialLinkSet.contains(triple);
	}
	
	public Set<String> getTargetIDSet(
			String sourceID, String tr){
		Pair<String, String> sourceTRPair = 
				new ImmutablePair<String, String>(sourceID, tr);
		
		return this.sourceTRPairIndex.get(sourceTRPair);
	}
	
	public SpatialLinkSet getSLSetUnion(
			SpatialLinkSet slSet1, SpatialLinkSet slSet2) {
		SpatialLinkSet resultSLSet =
				new SpatialLinkSet();
		
		for (Triple<String, String, String> triple: 
			slSet1.spatialLinkSet) {
			resultSLSet.addSpatialLink(triple);
		}
		
		for (Triple<String, String, String> triple: 
				slSet2.spatialLinkSet) {
			if (!resultSLSet.spatialLinkSet.contains(triple)) {
				resultSLSet.addSpatialLink(triple);
			}
		}
		
		return resultSLSet;
	}
	
	public void writeToTxt(String fileName) {
//		String fileName = "spatialLink/" + sourceDataset + ".txt";
		
		try {
			fw = new FileWriter(fileName);
			bw = new BufferedWriter(fw);
			
			for (Triple<String, String, String> spatialLink:
					this.spatialLinkSet) {
				bw.write(spatialLink.getLeft());
				bw.write(" ");
				bw.write(spatialLink.getMiddle());
				bw.write(" ");
				bw.write(spatialLink.getRight());
				bw.newLine();
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if(bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public void readFromTxt(String fileName) {
//		String fileName = "spatialLink/" + sourceDataset + ".txt";
		
    	try {
    		fr = new FileReader(fileName);
    		br = new BufferedReader(fr);
    		String currentLine;
			
    		//Read file line by line
    		while((currentLine = br.readLine()) != null) {
    			String[] lineWords;
    			lineWords = currentLine.split("\\s+");
    			
    			String sourceID = lineWords[0];
    			String targetID = lineWords[1];
    			String tr = lineWords[2];
    			
    			this.addSpatialLink(sourceID, targetID, tr);
    		}
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
    }
}
