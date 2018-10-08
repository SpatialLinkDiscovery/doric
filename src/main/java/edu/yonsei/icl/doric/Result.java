package edu.yonsei.icl.doric;

public class Result {
	long numSameID;
	long numFiltering;
	long numComposition;
	long numDE9IM;
	long numDE9IMHolds;
	long executionTime;
	
	public Result() {
		this.numSameID = 0;
		this.numFiltering = 0;
		this.numComposition = 0;
		this.numDE9IM = 0;
		this.numDE9IMHolds = 0;
		this.executionTime = 0;
	}

	public Result(long numSameID, long numFiltering,
			long numComposition, long numDE9IM,
			long numDE9IMHolds,long executionTime) {
		this.numSameID = numSameID;
		this.numFiltering = numFiltering;
		this.numComposition = numComposition;
		this.numDE9IM = numDE9IM;
		this.numDE9IMHolds = numDE9IMHolds;
		this.executionTime = executionTime;
	}
	
	public void printInfo() {
		System.out.println("numSameID: " + this.numSameID);
		System.out.println("numFiltering: " + this.numFiltering);
		System.out.println("numComposition: " + this.numComposition);
		System.out.println("numDE9IM: " + this.numDE9IM);
		System.out.println("numDE9IMHolds: " + this.numDE9IMHolds);
		System.out.println("execution time: " + this.executionTime);
	}
}
