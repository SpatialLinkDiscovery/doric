package edu.yonsei.icl.doric;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class StringPairSet {
	Set<Pair<String, String>> stringPairSet;
	
	public StringPairSet() {
		// TODO Auto-generated constructor stub
		this.stringPairSet =
				new HashSet<Pair<String, String>>();
	}
	
	public void addStringPair(String string1, String string2) {
		this.stringPairSet.add(
				new ImmutablePair<String, String>(
						string1,string2));
	}
}
