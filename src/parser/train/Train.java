package train;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multimap;
import grammar.Event;
import grammar.Grammar;
import grammar.Rule;

import java.util.*;


import tree.Node;
import tree.Tree;
import treebank.Treebank;
import utils.CountMap;
import utils.LineWriter;


/**
 * @author Reut Tsarfaty
 * <p>
 * CLASS: Train
 * <p>
 * Definition: a learning component
 * Role: reads off a grammar from a treebank
 * Responsibility: keeps track of rule counts
 */

public class Train {


    /**
     * Implementation of a singleton pattern
     * Avoids redundant instances in memory
     */
    public static Train m_singTrainer = null;
    public HashMap<String, Integer> counting_dic = null;
    private final ImmutableMultiset<String> strings = null;

    public Train() {

        counting_dic = new HashMap<>();
    }


    public static Train getInstance() {
        if (m_singTrainer == null) {
            m_singTrainer = new Train();
        }
        return m_singTrainer;
    }

    public static void main(String[] args) {

    }

    public Grammar train(Treebank myTreebank) {
        Grammar myGrammar = new Grammar();
        LineWriter writer = new LineWriter("binarize.exps");
        LineWriter writer2 = new LineWriter("cfgTree.exps");
        for (int i = 0; i < myTreebank.size(); i++) {
            writer2.writeLine(myTreebank.getAnalyses().get(i).toString());
            Tree myTree = binarize(myTreebank.getAnalyses().get(i));
            writer.writeLine(myTree.toString());
            List<Rule> theRules = getRules(myTree);
            myGrammar.addAll(theRules);
        }
        writer.close();
        writer2.close();
        calcCFGProb(myGrammar);

        return myGrammar;
    }

    private Tree binarize(Tree tree) {
        Node root = binarize(tree.getRoot());
        Tree newTree = new Tree(root);
        return newTree;
    }

    private Node binarize(Node parent) {
        if (parent.getNumberOfDaughters() > 2) {
            int split = 1;
            List<Node> originals_daughters = parent.getDaughters();
            List<Node> cloned_daughters = ((Node) parent.clone()).getDaughters();

            Node split_node = new Node();
            parent.setDaughters(List.of(cloned_daughters.get(0), split_node));
            split_node.setParent(parent);
            split_node.setIdentifier(createIdentifier(originals_daughters, 0));
            binarize(cloned_daughters.get(0));


            while (split < originals_daughters.size() - 1) {

                cloned_daughters.get(split).setParent(split_node);

                Node new_node = new Node();
                new_node.setParent(split_node);
                new_node.setIdentifier(createIdentifier(originals_daughters, split));

                split_node.setDaughters(List.of(cloned_daughters.get(split), new_node));
                split_node = new_node;

                binarize(cloned_daughters.get(split));
                split++;
            }
            //num of daughters is even

            split_node.setDaughters(List.of(cloned_daughters.get(split)));


        } else if (parent.getNumberOfDaughters() > 0) {
            parent.getDaughters().forEach(daughter -> binarize(daughter));

        }
        return parent;
    }

    private String createIdentifier(List<Node> daughters, int split) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        sb.append("@");
        for (Node s : daughters) {
            if (i == split + 1) {
                sb.append("|");
            }
            sb.append(s.getIdentifier());
            i++;
        }
        return sb.toString();

    }

    private void calcCFGProb(Grammar myGrammer) {
        Iterator iter = myGrammer.getRuleCounts().entrySet().iterator();
        while (iter.hasNext()) {
            Object element = iter.next();
            counting_dic.compute(((Rule) (((Map.Entry) element).getKey())).getLHS().toString(), (key, oldValue) -> ((oldValue == null) ?  (int) ((Map.Entry) element).getValue(): oldValue + (int) ((Map.Entry) element).getValue()));

        }
        //syntactical prob
        iter = myGrammer.getSyntacticRules().iterator();
        while (iter.hasNext()) {
            Object element = iter.next();

            int lhs_count = counting_dic.get(((Rule) element).getLHS().toString());
            int rule_count = myGrammer.getRuleCounts().get(element);
            double calc=-Math.log((double) rule_count / (double) lhs_count);
            if(calc < 0){
                System.out.println(calc);
            }
            ((Rule) element).setMinusLogProb(calc);
            System.out.println("hi");
        }
        //lexical prob
        //not the most efficient but better for understanding
        iter = myGrammer.getLexicalRules().iterator();
        while (iter.hasNext()) {
            Object element = iter.next();

            int lhs_count = counting_dic.get(((Rule) element).getLHS().toString());
            int rule_count = myGrammer.getRuleCounts().get(element);
            double calc=-Math.log((double) rule_count / (double) lhs_count);
            if(calc < 0){
                System.out.println(calc);
            }
            ((Rule) element).setMinusLogProb(-Math.log((double) rule_count / (double) lhs_count));
            System.out.println("hi");
        }

    }

    public List<Rule> getRules(Tree myTree) {
        List<Rule> theRules = new ArrayList<Rule>();

        List<Node> myNodes = myTree.getNodes();
        for (int j = 0; j < myNodes.size(); j++) {
            Node myNode = myNodes.get(j);
            if (myNode.isInternal()) {
                Event eLHS = new Event(myNode.getIdentifier());
                Iterator<Node> theDaughters = myNode.getDaughters().iterator();
                StringBuffer sb = new StringBuffer();
                while (theDaughters.hasNext()) {
                    Node n = (Node) theDaughters.next();
                    sb.append(n.getIdentifier());
                    if (theDaughters.hasNext())
                        sb.append(" ");
                }
                Event eRHS = new Event(sb.toString());
                Rule theRule = new Rule(eLHS, eRHS);
                if (myNode.isPreTerminal())
                    theRule.setLexical(true);
                if (myNode.isRoot())
                    theRule.setTop(true);
                theRules.add(theRule);
            }
        }
        return theRules;
    }

}
