package generation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import gumtreediff.gen.srcml.SrcmlCppTreeGenerator;
import gumtreediff.matchers.Mapping;
import gumtreediff.matchers.MappingStore;
import gumtreediff.tree.ITree;
import gumtreediff.tree.TreeContext;
import structure.Edge;

public class EdgeGeneration {
	
	public static void main (String args[]) throws Exception{
		String path1 = "talker.cpp";
		File cppfile1 = new File(path1);
		TreeContext tc1 = new SrcmlCppTreeGenerator().generateFromFile(cppfile1);
		List<ITree> des1 = tc1.getRoot().getDescendants();
		System.out.println("size:"+des1.size());
		String path2 = "talker2.cpp";
		File cppfile2 = new File(path2);
		TreeContext tc2 = new SrcmlCppTreeGenerator().generateFromFile(cppfile2);          
		ITree root1 = tc1.getRoot();
		System.out.println(root1.getId()+","+tc1.getTypeLabel(root1));
        ITree root2 = tc2.getRoot();
        System.out.println(root2.getId()+","+tc2.getTypeLabel(root2));
        
        IsomorphicSearch isoSearch = new IsomorphicSearch();
        String edgeMode = "rootLink";
        MappingStore mappings = isoSearch.isoMatch(root1, root2, edgeMode);   
        for(Mapping map : mappings) {
        	ITree src = map.getFirst();
        	ITree dst = map.getSecond();
        	System.out.println("Mapping:"+src.getId()+"->"+dst.getId());
        }
        System.out.println("mapSize:"+mappings.asSet().size());
        edgeGeneration(tc1, tc2, mappings);
	}
	
	public static void edgeGeneration(TreeContext tc1, TreeContext tc2, 
			MappingStore mappings) throws IOException {
		File outFile = new File("edges.txt");
		File outFile1 = new File("id_map.txt");
		BufferedWriter br = new BufferedWriter(new FileWriter(outFile));
		BufferedWriter br1 = new BufferedWriter(new FileWriter(outFile1));
		ITree root1 = tc1.getRoot();
		ITree root2 = tc2.getRoot();
		ArrayList<Edge> edges1 = new ArrayList<Edge>();
		collectEdge(root1, edges1);
		ArrayList<Edge> edges2 = new ArrayList<Edge>();
		collectEdge(root2, edges2);
		List<ITree> des1 = root1.getDescendants();
		for(ITree node : des1) {
			int id = node.getId();
			br1.append(String.valueOf(id)+"->"+String.valueOf(id));
			br1.newLine();
			br1.flush();
		}
		int size1 = des1.size()+1;
		List<ITree> des2 = root2.getDescendants();
		for(ITree node : des2) {
			int id = node.getId();
			br1.append(String.valueOf(id)+"->"+String.valueOf(id+size1));
			br1.newLine();
			br1.flush();
		}
		int size2 = des2.size()+1;
		for(Edge edge : edges1) {
			int par = edge.getSource();
			int child = edge.getTarget();
			br.append(par+" "+child);
			br.newLine();
			br.flush();
		}
		for(Edge edge : edges2) {
			int par = edge.getSource()+size1;
			int child = edge.getTarget()+size1;
			br.append(par+" "+child);
			br.newLine();
			br.flush();
		}
		
		ArrayList<Integer> virtualList = virtualLink(size1, size2, mappings, br, br1);
		tokenGeneration(tc1, tc2, virtualList);
	}
	
	public static ArrayList<Integer> virtualLink(int size1, int size2, MappingStore mappings, 
			BufferedWriter br, BufferedWriter br1) throws IOException {
		int virtualID = size1+size2;
		ArrayList<Integer> virtualList = new ArrayList<Integer>();
		for(Mapping map : mappings) {
        	ITree src = map.getFirst();
        	int srcID = src.getId();
        	ITree dst = map.getSecond();
        	int dstID = dst.getId();
        	dstID = dstID+size1;
        	br.append(srcID+" "+virtualID);
        	br.newLine();
        	br.append(dstID+" "+virtualID);
        	br.newLine();
        	br.flush();
        	br1.append(String.valueOf(virtualID));
        	br1.newLine();
        	br1.flush();
        	virtualList.add(virtualID);
        	virtualID++;
        }
        br.close();
        br1.close();
        
        return virtualList;
	}//virtal nodes for root nodes in isomorphic subtree
	
	public static void tokenGeneration(TreeContext tc1, TreeContext tc2, 
			ArrayList<Integer> virtualList) throws IOException {
		String path = "tokens.txt";
		BufferedWriter br = new BufferedWriter(new FileWriter(new File(path)));
		HashMap<Integer, ITree> idMap = new HashMap<Integer, ITree>();
		List<ITree> tree1 = tc1.getRoot().getTrees();
		System.out.println("size:"+tree1.size());
		List<ITree> tree2 = tc2.getRoot().getTrees();
		System.out.println("size:"+tree2.size());
		for(ITree node : tree1) {			
			int id = node.getId();
			idMap.put(id, node);						
		}
		for(ITree node : tree2) {
			int id = node.getId();
			id += tree1.size();
			idMap.put(id, node);
		}
		//将keySet放入list
        ArrayList<Integer> idList = new ArrayList<>(idMap.keySet());
        //调用sort方法并重写比较器进行升/降序
        Collections.sort(idList, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1>o2?1:-1;
            }
        });
        
        for(int id : idList) {
        	ITree node = idMap.get(id);
        	String type = tc1.getTypeLabel(node);
        	if(type==null)
        		type = tc2.getTypeLabel(node);
        	String label = node.getLabel();
        	if(label=="")
        		label = "NULL";
        	System.out.println(id+","+type+","+label);
        	br.append(String.valueOf(id)+","+type+","+label);
        	br.newLine();
        	br.flush();
        }
        
        for(int virtualID : virtualList) {
        	br.append(String.valueOf(virtualID)+",virtual,NULL");
        	br.newLine();
        	br.flush();
        }
        br.close();
	}
	
	public static ArrayList<Edge> collectEdge(ITree node, ArrayList<Edge> edges) {
		List<ITree> childs = node.getChildren();
		int src = node.getId();
		for(ITree child : childs) {
			int dst = child.getId();
			Edge edge = new Edge(src, dst);
			edges.add(edge);
			collectEdge(child, edges);
		}
		return edges;		
	}//收集AST树中所有边
	
	
	
	
	
	
	
	
	
	
	
	
	

}
