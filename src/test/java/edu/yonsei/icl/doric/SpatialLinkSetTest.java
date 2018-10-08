package edu.yonsei.icl.doric;

public class SpatialLinkSetTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String sourceDataset = "clc20k";
		String method = "SILK";
		String fileName = "spatialLink/" + sourceDataset +
				"_" + method + ".txt";
		
		SpatialLinkSet slSet =
				new SpatialLinkSet();
		
		slSet.readFromTxt(fileName);
		slSet.printSLInfo();
	}

}
