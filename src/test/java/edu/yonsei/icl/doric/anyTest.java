package edu.yonsei.icl.doric;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

public class anyTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Set testSet = new HashSet<>();
		
		Triple<String, String, String> t1 =
				new ImmutableTriple<String, String, String>("s1", "t1", "r1");
		Triple<String, String, String> t2 =
				new ImmutableTriple<String, String, String>("s2", "t2", "r2");
		Triple<String, String, String> t3 =
				new ImmutableTriple<String, String, String>("s3", "t3", "r3");
		Triple<String, String, String> t4 =
				new ImmutableTriple<String, String, String>("s1", "t1", "r1");
		
		testSet.add(t1);
		testSet.add(t2);
		testSet.add(t3);
		//testSet.add(sp4);
		
		
		
		System.out.println(testSet.contains(t4));
	}

}
