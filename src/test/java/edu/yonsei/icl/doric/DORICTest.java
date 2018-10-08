package edu.yonsei.icl.doric;

import java.io.IOException;

public class DORICTest {
	//data import
	static String nutsTtlFile =
				"dataset/nuts.ttl";
	static String gagN3File =
				"dataset/gag.n3";
	static String clcNtFile =
				"dataset/clc.nt";

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		SpatialLinkSet slSet =
				new SpatialLinkSet();
		int numSpatialEntity = 20000;
		
		String sourceDataset = "clc20k";
		String targetDataset = "gag";
		
		/*String fileName = "spatialLink/" + sourceDataset
				+ "_DORIC.txt";
		slSet = runDORIC(sourceDataset, numSpatialEntity);*/
		
		String fileName = "spatialLink/" + sourceDataset
				+ "_" + targetDataset + "_DORIC.txt";
		slSet = runDORIC(sourceDataset, targetDataset, numSpatialEntity);
		
		slSet.writeToTxt(fileName);
	}
	
	public static SpatialLinkSet runDORIC(
			String sourceDataset, String targetDataset,
			int numSpatialEntity) throws IOException {
		System.out.println("start reading data from " + sourceDataset +  "...");
		long startTime = System.currentTimeMillis();
		
		SpatialEntitySet sourceSESet =
				new SpatialEntitySet();
		SpatialLinkSet sourceSLSet =
				new SpatialLinkSet();
		if (sourceDataset.equals("nuts")) {
			sourceSESet.readNUTSFromTtl(nutsTtlFile, sourceSLSet);
		} else if (sourceDataset.equals("gag")) {
			sourceSESet.readGAGFromN3(gagN3File, sourceSLSet);
		} else {
			sourceSESet.readCLCFromNt(
					clcNtFile, sourceSLSet, numSpatialEntity);
		}
		long finishTime = System.currentTimeMillis();
        long readTime = finishTime - startTime;
        System.out.println("finish reading data from " + sourceDataset 
        		+  "...elapsed time is: " + readTime + " ms");
		
		System.out.println("start reading data from " + targetDataset +  "...");
		startTime = System.currentTimeMillis();
		
		SpatialEntitySet targetSESet =
				new SpatialEntitySet();
		SpatialLinkSet targetSLSet =
				new SpatialLinkSet();
		if (targetDataset.equals("nuts")) {
			targetSESet.readNUTSFromTtl(nutsTtlFile, targetSLSet);
		} else if (targetDataset.equals("gag")) {
			targetSESet.readGAGFromN3(gagN3File, targetSLSet);
		} else {
			targetSESet.readCLCFromNt(
					clcNtFile, targetSLSet, numSpatialEntity);
		}
		
		finishTime = System.currentTimeMillis();
        readTime = finishTime - startTime;
        System.out.println("finish reading data from " + targetDataset 
        		+  "...elapsed time is: " + readTime + " ms");
        
        DORIC doric = new DORIC();
		/*sourceSLSet = doric.execute(sourceSESet, sourceSESet, sourceSLSet);
		targetSLSet = doric.execute(targetSESet, targetSESet, targetSLSet);*/
        
        //read self spatial links
        String sourceSLFileName = "spatialLink/" + sourceDataset + ".txt";
        String targetSLFileName = "spatialLink/" + targetDataset + ".txt";
        sourceSLSet.readFromTxt(sourceSLFileName);
        targetSLSet.readFromTxt(targetSLFileName);
		
		//add the self spatial links of two datasets
		SpatialLinkSet unionSLSet =
				new SpatialLinkSet();
		unionSLSet = unionSLSet.getSLSetUnion(sourceSLSet, targetSLSet);
		
		SpatialLinkSet resultSLSet =
				new SpatialLinkSet();
		resultSLSet = doric.execute(sourceSESet, targetSESet, unionSLSet);
		
		/*System.out.println("size of sourceSLSet is: " + sourceSLSet.getNumSL());
		System.out.println("size of targetSLSet is: " + targetSLSet.getNumSL());
		System.out.println("size of unionSLSet is: " + unionSLSet.getNumSL());
		System.out.println("size of resultSLSet is: " + resultSLSet.getNumSL());*/
		
		return resultSLSet;
	}
	
	public static SpatialLinkSet runDORIC(
			String datasetName, int numSpatialEntity) throws IOException {
		System.out.println("start reading data from " + datasetName +  "...");
		long startTime = System.currentTimeMillis();
		
		SpatialEntitySet seSet =
				new SpatialEntitySet();
		SpatialLinkSet slSet =
				new SpatialLinkSet();
		if (datasetName.equals("nuts")) {
			slSet = seSet.readNUTSFromTtl(nutsTtlFile, slSet);
		} else if (datasetName.equals("gag")) {
			slSet = seSet.readGAGFromN3(gagN3File, slSet);
		} else {
			slSet = seSet.readCLCFromNt(
					clcNtFile, slSet, numSpatialEntity);
		}
		long finishTime = System.currentTimeMillis();
        long readTime = finishTime - startTime;
        System.out.println("finish reading data from " + datasetName 
        		+  "...elapsed time is: " + readTime + " ms");
		
		DORIC doric = new DORIC();
		slSet = doric.execute(seSet, seSet, slSet);
		
		return slSet;
	}
}
