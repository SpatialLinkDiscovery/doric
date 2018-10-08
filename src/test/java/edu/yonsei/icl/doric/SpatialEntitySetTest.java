package edu.yonsei.icl.doric;

import java.io.IOException;

import org.locationtech.jts.geom.Coordinate;

public class SpatialEntitySetTest {
	public static void main(String args[]) throws IOException {
		String nutsTtlFile =
				"dataset/nuts.ttl";
		String gagN3File =
				"dataset/gag.n3";
		String clcNtFile =
				"dataset/clc.nt";
		int numSpatialEntity = 200000;
		
		/*SpatialLinkSet slSet =
				new SpatialLinkSet();
		
		SpatialEntitySet seSet =
				new SpatialEntitySet();
		slSet = seSet.readNUTSFromTtl(nutsTtlFile, slSet);
		
		seSet.printSEInfo();
		slSet.printSLInfo();*/
		
		SpatialEntitySet seSet =
				new SpatialEntitySet();
		SpatialLinkSet slSet =
				new SpatialLinkSet();
		
		//slSet = seSet.readNUTSFromTtl(nutsTtlFile, slSet);
		//slSet = seSet.readGAGFromN3(gagN3File, slSet);
		slSet = seSet.readCLCFromNt(clcNtFile, slSet, numSpatialEntity);
		
		System.out.println("num. of geometries: " + seSet.getNumberOfSE());
		System.out.println("max num. of polygons: " + seSet.getMaxNumPolygon());
		System.out.println("max num of points: " + seSet.getMPolygonMaxNumPoint());
		System.out.println("num. of initial spatial links: " + slSet.getNumSL());
	}

}
