package generation;

import java.io.File;

import gumtreediff.gen.srcml.SrcmlCppTreeGenerator;
import gumtreediff.tree.ITree;
import gumtreediff.tree.TreeContext;

public class GraphMerge {

	public static void main (String args[]) throws Exception{
		String path1 = "talker.cpp";
		String path2 = "talker2.cpp";
		File cppfile1 = new File(path1);
		TreeContext tc1 = new SrcmlCppTreeGenerator().generateFromFile(cppfile1);
		ITree root1 = tc1.getRoot();
		File cppfile2 = new File(path2);
		TreeContext tc2 = new SrcmlCppTreeGenerator().generateFromFile(cppfile2);
		ITree root2 = tc2.getRoot();
		
	}
}
