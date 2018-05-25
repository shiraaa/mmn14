package decode;

import grammar.Grammar;
import grammar.Rule;

import java.util.*;

import tree.Node;
import tree.Terminal;
import tree.Tree;

public class Decode {

    public static Set<Rule> m_setGrammarRules = null;
    public static Set<Rule> m_setUnaryGrammarRules = null;
    public static Map<String, Set<Rule>> m_mapLexicalRules = null;
    public static Set<String> m_nonTerminals = null;
    public static Set<String> m_terminals = null;
    public static HashMap<String, BackPointer>[][] bp = null;
    public static Map<String, Map<String, Rule>> m_unaryRulesTable = null;
    public static Map<String, Set<Rule>> m_SytacticRulesTable = null;


    /**
     * Implementation of a singleton pattern
     * Avoids redundant instances in memory
     */
    public static Decode m_singDecoder = null;

    public static Decode getInstance(Grammar g) {
        if (m_singDecoder == null) {
            m_singDecoder = new Decode();
            m_nonTerminals = g.getNonTerminalSymbols();
            m_terminals = g.getTerminalSymbols();
            m_setGrammarRules = g.getSyntacticRules();
            m_setUnaryGrammarRules = g.getSyntacticUnaryRules();
            m_mapLexicalRules = g.getLexicalEntries();
            g.buildUnaryRulesMap();
            g.buildSyntaticRulesMap();
            m_SytacticRulesTable = g.getSyntacticRulesTable();
            m_unaryRulesTable = g.getUnaryRulesTable();
            bp = null;
        }
        return m_singDecoder;
    }

    public Tree decode(List<String> input) {

        // Done: Baseline Decoder
        //       Returns a flat tree with NN labels on all leaves


        Tree t = CYK(input);
        // TODO: CYK decoder
        //       if CYK fails,
        //       use the baseline outcome


        return t;

    }

    private Tree dummyParser(List<String> input) {
        Tree t = new Tree(new Node("TOP"));
        Iterator<String> theInput = input.iterator();
        while (theInput.hasNext()) {
            String theWord = (String) theInput.next();
            Node preTerminal = new Node("NN");
            Terminal terminal = new Terminal(theWord);
            preTerminal.addDaughter(terminal);
            t.getRoot().addDaughter(preTerminal);
        }
        return t;
    }

    private Tree CYK(List<String> input) {
        int input_length = input.size();
        //Rule NN=(Rule)m_mapLexicalRules.get("NN");

        int numOfTerminals = m_terminals.size();
        HashMap<String, Double>[][] cky = new HashMap[input_length + 1][input_length + 1];

        //Back pointer
        bp = new HashMap[input_length + 1][input_length + 1];
        try {


            for (int i = 1; i <= input_length; i++) {
                Iterator it = m_mapLexicalRules.values().iterator();
                while (it.hasNext()) {
                    HashSet<Rule> elemnt = (HashSet<Rule>) it.next();
                    Iterator it2 = elemnt.iterator();
                    while (it2.hasNext()) {
                        Rule rule = (Rule) it2.next();

                        if (rule.getRHS().toString().compareTo(input.get(i - 1)) == 0) {
                            if (cky[i - 1][i] == null) {
                                cky[i - 1][i] = new HashMap<>();
                            }
                            cky[i - 1][i].put(rule.getLHS().toString(), rule.getMinusLogProb());
                        }
                    }


                }

                if (cky[i - 1][i] == null) {
                    cky[i - 1][i] = new HashMap<>();
                    cky[i - 1][i].put("NN", (double) 0);
                }

                //unary rules A-->B

                HashMap cloned = (HashMap) cky[i - 1][i].clone();
                Iterator it1 = cloned.entrySet().iterator(); //without clone getting concurrency iterator issue

                while (it1.hasNext()) {

                    Map.Entry el = (Map.Entry) it1.next();
                    if (m_unaryRulesTable.containsKey(el.getKey())) {
                        Iterator it2 = m_unaryRulesTable.get(el.getKey()).entrySet().iterator();

                        while (it2.hasNext()) {

                            Map.Entry el2 = (Map.Entry) it2.next();
                            Rule rule = (Rule) el2.getValue();
                            cky[i - 1][i].put(el2.getKey().toString(), cky[i - 1][i].get(rule.getRHS().toString()) + rule.getMinusLogProb()); //**CHECK PROB**//

                        }

                    }
                }
            }
              /*  Iterator it1 = m_setUnaryGrammarRules.iterator();
                HashSet<Rule> check_List = new HashSet<>();
                while (it1.hasNext()) {
                    Rule rule = (Rule) it1.next();
                    List<String> symb_lst = rule.getRHS().getSymbols();

                    if (symb_lst.size() == 1) { //check if unary rule
                        if (cky[i - 1][i].get(rule.getRHS().toString()) != null) {   //check left side is in this entry
                            if (!check_List.contains(rule)) {
                                cky[i - 1][i].put(rule.getLHS().toString(), cky[i - 1][i].get(rule.getRHS().toString()) + rule.getMinusLogProb());
                                check_List.add(rule);
                                it1 = m_setUnaryGrammarRules.iterator(); // A new rule was found restart rules iteration
                            }

                        }
                    }

                }*/


            for (int span = 2; span <= input_length; span++) {
                for (int j = span - 2; j >= 0; j--) {
                    for (int k = j + 1; k <= span - 1; k++) {
                        //System.out.println(span);
                        if (cky[j][k] != null && cky[k][span] != null) {

                            Iterator it_B = cky[j][k].entrySet().iterator();
                            Iterator it_C ;

                            while (it_B.hasNext()) {

                                Map.Entry B = (Map.Entry) it_B.next();
                                it_C = cky[k][span].entrySet().iterator();

                                while (it_C.hasNext()) {

                                    Map.Entry C = (Map.Entry) it_C.next();
                                    Set<Rule> A_set = m_SytacticRulesTable.get(B.getKey().toString() + " " + C.getKey().toString());

                                    if (A_set != null) {

                                        double B_prob = (double) B.getValue();
                                        double C_prob = (double) C.getValue();

                                        Iterator a_itr = A_set.iterator();

                                        Rule A_rule = null;
                                        while (a_itr.hasNext()) {
                                            A_rule= (Rule) a_itr.next();
                                            String A=A_rule.getLHS().toString();


                                            if (cky[j][span] == null) {
                                                cky[j][span] = new HashMap<>();
                                            }

                                           /* if (B_prob < 0 && C_prob < 0){
                                                System.out.println("prob ERROR");
                                            }*/

                                            if (cky[j][span].get(A) == null || cky[j][span].get(A) > A_rule.getMinusLogProb() + B_prob + C_prob)
                                            {
                                                cky[j][span].put(A, A_rule.getMinusLogProb() + B_prob + C_prob);

                                                if (bp[j][span] == null) {
                                                    bp[j][span] = new HashMap<>();
                                                }
                                                bp[j][span].put(A, new BackPointer(k, B.getKey().toString(), C.getKey().toString()));
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                    if (cky[j][span] != null) {

                        HashMap cloned = (HashMap) cky[j][span].clone();
                        Iterator it = cloned.entrySet().iterator(); //without clone getting concurrency iterator issue

                        while (it.hasNext()) {
                            Map.Entry el = (Map.Entry) it.next();
                            if (m_unaryRulesTable.containsKey(el.getKey())) {
                                Iterator it2 = m_unaryRulesTable.get(el.getKey()).entrySet().iterator();
                                while (it2.hasNext()) {
                                    Map.Entry el2 = (Map.Entry) it2.next();
                                    Rule rule = (Rule) el2.getValue();
                                    cky[j][span].put(el2.getKey().toString(), cky[j][span].get(rule.getRHS().toString()) + rule.getMinusLogProb()); //**CHECK PROB**//
                                }

                            }
                        }
                       /* Iterator it1 = m_setUnaryGrammarRules.iterator();
                        HashSet<Rule> check_List = new HashSet<>();
                        int checkLst_size = check_List.size();
                        while (it1.hasNext()) {
                            Rule rule = (Rule) it1.next();
                            List<String> symb_lst = rule.getRHS().getSymbols();
                            if (symb_lst.size() == 1) { //check if unary rule
                                if (cky[j][span].get(rule.getRHS().toString()) != null) {   //check left side is in this entry

                                    if (!check_List.contains(rule)) {
                                        check_List.add(rule);
                                        cky[j][span].put(rule.getLHS().toString(), cky[j][span].get(rule.getRHS().toString()) + rule.getMinusLogProb()); *//**CHECK PROB**//*

                                    }

                                }
                            }
                            if (!it1.hasNext() && check_List.size() > checkLst_size) {
                                it1 = m_setUnaryGrammarRules.iterator(); // A new NoN terminal fount restart rules iteration
                                checkLst_size= check_List.size();
                            }
                        }*/

                    }

                }
            }
        } catch (Exception e) {
            System.out.println(e);
            return dummyParser(input);
        }
        Node top = new Node("TOP");
        top.setRoot(true);

        if (bp[0][input_length] != null && bp[0][input_length].get("S") != null) {
            BackPointer s = bp[0][input_length].get("S");
            Node root = new Node("S");
            root.setParent(top);
            top.addDaughter(root);
            buildTree(root, s, 0, input_length);
            Tree tree = new Tree(top);
            return tree;
        } else {
            return dummyParser(input);
        }

    }


    private void buildTree(Node parent, BackPointer A, int j, int i) {

        if (A == null) {
            return;
        }

        int k = A.getK();

        String B = A.getNonT1();
        String C = A.getNonT2();

        Node new_node_B = new Node(B);
        Node new_node_C = new Node(C);

        parent.addDaughter(new_node_B);
        parent.addDaughter(new_node_C);

        new_node_B.setParent(parent);
        new_node_C.setParent(parent);

        if (bp[j][k] != null) {
            buildTree(new_node_B, bp[j][k].get(B), j, k);
        }

        if (bp[k][i] != null) {
            buildTree(new_node_C, bp[k][i].get(C), k, i);
        }


    }
}





	
	
	





