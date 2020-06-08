package generation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gumtreediff.matchers.Mapping;
import gumtreediff.matchers.MappingStore;
import gumtreediff.matchers.MultiMappingStore;
import gumtreediff.tree.ITree;

public class IsomorphicSearch {
	private static int MIN_HEIGHT = 2;
	
	public MappingStore isoMatch(ITree sRoot, ITree dRoot, String mode) {
        MultiMappingStore multiMappings = new MultiMappingStore();

        PriorityTreeList srcTrees = new PriorityTreeList(sRoot);
        PriorityTreeList dstTrees = new PriorityTreeList(dRoot);

        while (srcTrees.peekHeight() != -1 && dstTrees.peekHeight() != -1) {
            while (srcTrees.peekHeight() != dstTrees.peekHeight())
                popLarger(srcTrees, dstTrees);

            List<ITree> currentHeightSrcTrees = srcTrees.pop();
            List<ITree> currentHeightDstTrees = dstTrees.pop();
//            for(ITree node : currentHeightSrcTrees) {
//            	System.out.print(node.getId()+",");
//            }
//            for(ITree node : currentHeightDstTrees) {
//            	System.out.print(node.getId()+",");
//            }
            

            boolean[] marksForSrcTrees = new boolean[currentHeightSrcTrees.size()];
            boolean[] marksForDstTrees = new boolean[currentHeightDstTrees.size()];

            for (int i = 0; i < currentHeightSrcTrees.size(); i++) {
                for (int j = 0; j < currentHeightDstTrees.size(); j++) {
                    ITree src = currentHeightSrcTrees.get(i);
                    ITree dst = currentHeightDstTrees.get(j);

                    if (src.isIsomorphicTo(dst)) {
                        multiMappings.link(src, dst);
                        marksForSrcTrees[i] = true;
                        marksForDstTrees[j] = true;
                    }
                }
            }

            for (int i = 0; i < marksForSrcTrees.length; i++)
                if (marksForSrcTrees[i] == false)
                    srcTrees.open(currentHeightSrcTrees.get(i));
            for (int j = 0; j < marksForDstTrees.length; j++)
                if (marksForDstTrees[j] == false)
                    dstTrees.open(currentHeightDstTrees.get(j));
            srcTrees.updateHeight();
            dstTrees.updateHeight();
        }
        Set<Mapping> multiMap_set = multiMappings.getMappings();
        for(Mapping map : multiMap_set) {
        	ITree src = map.getFirst();
        	ITree dst = map.getSecond();        	
        	System.out.println("greedyDownMap ID:"+src.getId()+"->"+dst.getId());
        }
        
        MappingStore mappings = filterMappings(multiMappings, mode);
        
        return mappings;
    }
	
	private MappingStore filterMappings(MultiMappingStore multiMappings, String mode) {
        // Select unique mappings first
		MappingStore mappings = new MappingStore();
        List<Mapping> ambiguousList = new ArrayList<>();
        Set<ITree> ignored = new HashSet<>();
        for (ITree src: multiMappings.getSrcs()) {
        	boolean isMappingUnique = false;
            if (multiMappings.isSrcUnique(src)) {            	
                if (multiMappings.isSrcUnique(src)) {
                    ITree dst = multiMappings.getDst(src).iterator().next();
                    if (multiMappings.isDstUnique(dst)) {
                    	if(mode.equals("rootLink")) {
                    		mappings.link(src, dst);                   		
                    	}else if(mode.equals("nodeLink")) {
                    		addMappingRecursively(src, dst, mappings);
                    	}                                         	
                        isMappingUnique = true;
                    }
                }
            }           	
            if (!(ignored.contains(src) || isMappingUnique)) {
                Set<ITree> adsts = multiMappings.getDst(src);
                Set<ITree> asrcs = multiMappings.getSrc(multiMappings.getDst(src).iterator().next());
                for (ITree asrc : asrcs)
                    for (ITree adst: adsts)
                        ambiguousList.add(new Mapping(asrc, adst));
                ignored.addAll(asrcs);
            }
        }
        for(Mapping map : ambiguousList) {
        	ITree src = map.first;
        	ITree dst = map.second;
        	System.out.println("ambiguousMap:"+src.getId()+","+dst.getId());
        }

/**
 * AmbiguousMapping seems useless for us        
 */
        
//        // Rank the mappings by score.
//        Set<ITree> srcIgnored = new HashSet<>();
//        Set<ITree> dstIgnored = new HashSet<>();
//        Collections.sort(ambiguousList, new SiblingsMappingComparator(ambiguousList, mappings, getMaxTreeSize()));
//
//        // Select the best ambiguous mappings
//        retainBestMapping(ambiguousList, srcIgnored, dstIgnored);
        
        return mappings;
    }

    protected void addMappingRecursively(ITree src, ITree dst, MappingStore mappings) {
    	System.out.println("mapsize:"+mappings.asSet().size());
        List<ITree> srcTrees = src.getTrees();
        List<ITree> dstTrees = dst.getTrees();
        for (int i = 0; i < srcTrees.size(); i++) {
            ITree currentSrcTree = srcTrees.get(i);
            ITree currentDstTree = dstTrees.get(i);
            mappings.link(currentSrcTree, currentDstTree);
        }
    }
	
	private void popLarger(PriorityTreeList srcTrees, PriorityTreeList dstTrees) {
        if (srcTrees.peekHeight() > dstTrees.peekHeight())
            srcTrees.open();
        else
            dstTrees.open();
    }
	
	private static class PriorityTreeList {

        private List<ITree>[] trees;

        private int maxHeight;

        private int currentIdx;

        @SuppressWarnings("unchecked")
        public PriorityTreeList(ITree tree) {
            int listSize = tree.getHeight() - MIN_HEIGHT + 1;
            if (listSize < 0)
                listSize = 0;
            if (listSize == 0)
                currentIdx = -1;
            trees = (List<ITree>[]) new ArrayList[listSize];
            maxHeight = tree.getHeight();
            addTree(tree);
        }

        private int idx(ITree tree) {
            return idx(tree.getHeight());
        }

        private int idx(int height) {
            return maxHeight - height;
        }

        private int height(int idx) {
            return maxHeight - idx;
        }

        private void addTree(ITree tree) {
            if (tree.getHeight() >= MIN_HEIGHT) {
                int idx = idx(tree);
                if (trees[idx] == null) trees[idx] = new ArrayList<>();
                trees[idx].add(tree);
            }
        }

        public List<ITree> open() {
            List<ITree> pop = pop();
            if (pop != null) {
                for (ITree tree: pop) open(tree);
                updateHeight();
                return pop;
            } else return null;
        }

        public List<ITree> pop() {
            if (currentIdx == -1)
                return null;
            else {
                List<ITree> pop = trees[currentIdx];
                trees[currentIdx] = null;
                return pop;
            }
        }

        public void open(ITree tree) {
            for (ITree c: tree.getChildren()) addTree(c);
        }

        public int peekHeight() {
            return (currentIdx == -1) ? -1 : height(currentIdx);
        }

        public void updateHeight() {
            currentIdx = -1;
            for (int i = 0; i < trees.length; i++) {
                if (trees[i] != null) {
                    currentIdx = i;
                    break;
                }
            }
        }
    }

}
