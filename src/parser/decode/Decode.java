package decode;

import grammar.Grammar;
import grammar.Rule;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import tree.Node;
import tree.Terminal;
import tree.Tree;

public class Decode {

    public static Set<Rule> m_setGrammarRules = null;
    public static Set<Rule> m_setUnaryGrammarRules = null;
    public static Map<String, Set<Rule>> m_mapLexicalRules = null;
    public static Set<String> m_nonTerminals = null;
    public static Set<String> m_terminals = null;
    public static Map<String, Map<String, Rule>> m_unaryRulesTable = null;
    public static Map<String, ArrayList<Rule>> m_SytacticRulesTable = null;
    public static List<String> sentence = null;


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
        HashMap<String, BackPointer>[][] bp = null;
        Map<String, Double>[][] cky = null;
        int input_length = input.size();
        //Rule NN=(Rule)m_mapLexicalRules.get("NN");
        sentence = input;

        int numOfTerminals = m_terminals.size();
        cky = new ConcurrentHashMap[input_length + 1][input_length + 1];

        //Back pointer
        bp = new HashMap[input_length + 1][input_length + 1];
//        try {

        Iterator it;
        Iterator it2;
        Iterator it3;

        for (int i = 1; i <= input_length; i++) {

            it = m_mapLexicalRules.values().iterator();
            while (it.hasNext()) {
                HashSet<Rule> elemnt = (HashSet<Rule>) it.next();
                it2 = elemnt.iterator();
                while (it2.hasNext()) {
                    Rule rule = (Rule) it2.next();

                    if (rule.getRHS().toString().compareTo(input.get(i - 1)) == 0) {
                        if (cky[i - 1][i] == null) {
                            cky[i - 1][i] = new ConcurrentHashMap<>();
                        }
                        cky[i - 1][i].put(rule.getLHS().toString(), rule.getMinusLogProb());

                        if (m_unaryRulesTable.containsKey(rule.getLHS().toString())) {
                            it3 = m_unaryRulesTable.get(rule.getLHS().toString()).entrySet().iterator();

                            while (it3.hasNext()) {

                                Map.Entry el2 = (Map.Entry) it3.next();
                                Rule rule2 = (Rule) el2.getValue();
                                if (cky[i - 1][i].get(el2.getKey().toString()) == null)
                                    cky[i - 1][i].put(el2.getKey().toString(), cky[i - 1][i].get(rule2.getRHS().toString()) + rule.getMinusLogProb()); //**CHECK PROB**//

                            }

                        }
                    }
                }


            }

            if (cky[i - 1][i] == null) {
                cky[i - 1][i] = new ConcurrentHashMap<>();
                cky[i - 1][i].put("NN", (double) 0);
            }

            //unary rules A-->B


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

        Iterator it_B;
        Iterator it_C;
        ArrayList<Rule> A_set;
        Iterator a_itr;
        Rule A_rule = null;
        String A = null;
        Map.Entry B;
        Map.Entry C;
        String B_str;
        String C_str;
        String[] Barr;
        String B_opt = "";
        Map.Entry unar_rule;
        Rule rule;
        BackPointer unar_bp;
        ArrayList set;
        //Tuple<Tuple<Map.Entry,Map.Entry>,ArrayList<Rule>> rules_CB;
        ArrayList<Tuple<Tuple, ArrayList<Rule>>> rules_CBk;

        for (int span = 2; span <= input_length; span++) {
            for (int j = span - 2; j >= 0; j--) {
                for (int k = j + 1; k <= span - 1; k++) {
                    //System.out.println(span);
                    if (cky[j][k] != null && cky[k][span] != null) {

                        it_B = cky[j][k].entrySet().iterator();

                        rules_CBk = new ArrayList<>();

                        while (it_B.hasNext()) {

                            B = (Map.Entry) it_B.next();

                            B_str = B.getKey().toString();

                            if (!B_str.startsWith("@")) {

                                it_C = cky[k][span].entrySet().iterator();
                                while (it_C.hasNext()) {

                                    C = (Map.Entry) it_C.next();
                                    C_str = C.getKey().toString();
                                    set = m_SytacticRulesTable.get(B_str + " " + C_str);
                                    rules_CBk.add(new Tuple<>(new Tuple(B, C), set));

                                }
                            }
                        }
                        for (Tuple<Tuple, ArrayList<Rule>> entry :
                                rules_CBk) {
                            Tuple x = entry.x;
                            A_set = entry.y;

                            B = (Map.Entry) x.x;

                            B_str = B.getKey().toString();
                            C = (Map.Entry) x.y;

                            C_str = B.getKey().toString();

                            if (A_set != null) {

                                double B_prob = (double) B.getValue();
                                double C_prob = (double) C.getValue();

                                a_itr = A_set.iterator();


                                while (a_itr.hasNext()) {
                                    A_rule = (Rule) a_itr.next();
                                    A = A_rule.getLHS().toString();


                                    if (cky[j][span] == null) {
                                        cky[j][span] = new ConcurrentHashMap<>();
                                    }

                                           /* if (B_prob < 0 && C_prob < 0){
                                                System.out.println("prob ERROR");
                                            }*/

                                    if (cky[j][span].get(A) == null || cky[j][span].get(A) > A_rule.getMinusLogProb() + B_prob + C_prob) {
                                        cky[j][span].put(A, A_rule.getMinusLogProb() + B_prob + C_prob);

                                        if (bp[j][span] == null) {
                                            bp[j][span] = new HashMap<>();
                                        }
                                        bp[j][span].put(A, new BackPointer(k, B_str, C_str));

                                        //Unary
                                        if (m_unaryRulesTable.containsKey(A)) {

                                            it2 = m_unaryRulesTable.get(A).entrySet().iterator();
                                            while (it2.hasNext()) {
                                                unar_rule = (Map.Entry) it2.next();
                                                rule = (Rule) unar_rule.getValue();
                                                if (cky[j][span].get(unar_rule.getKey()) == null) { //add unary only if not exist , no point to add twice
                                                    cky[j][span].put(unar_rule.getKey().toString(), A_rule.getMinusLogProb() + B_prob + C_prob + rule.getMinusLogProb()); //**CHECK PROB**//

                                                    bp[j][span].put(unar_rule.getKey().toString(), new BackPointer(k, B_str, C_str));
                                                }
                                            }

                                        }
                                    }
                                }
                            }

                        }
                        rules_CBk.clear();

                    }
                }


//                if (cky[j][span] != null) {
//                    //String key;
//


//                    Iterator it2;
//                    Map.Entry el2;
//                    Rule rule;
//                    BackPointer unar_bp;
//                    for (String key : cky[j][span].keySet()) {
//                        // while (it.hasNext()) {
//                        //key=it.next().toString();
//                        //double el_prob = cky[j][span].get( key);
//                        if (m_unaryRulesTable.containsKey(key)) {
//                            it2 = m_unaryRulesTable.get(key).entrySet().iterator();
//                            while (it2.hasNext()) {
//                                el2 = (Map.Entry) it2.next();
//                                rule = (Rule) el2.getValue();
//                                cky[j][span].put(el2.getKey().toString(), cky[j][span].get(rule.getRHS().toString()) + rule.getMinusLogProb()); //**CHECK PROB**//
//                                unar_bp = bp[j][span].get(rule.getRHS().toString());
//                                bp[j][span].put(el2.getKey().toString(), new BackPointer(unar_bp.getK(), unar_bp.getNonT1(), unar_bp.getNonT2()));
//
//                            }
//
//                        }
//                    }
//
//
//                }

            }
        }
//        } catch (Exception e) {
//            System.out.println(e);
//            return dummyParser(input);
//        }
        Node top = new Node("TOP");
        top.setRoot(true);

        if (bp[0][input_length] != null && bp[0][input_length].get("S") != null) {
            BackPointer s = bp[0][input_length].get("S");
            Node root = new Node("S");
            root.setParent(top);
            top.addDaughter(root);
            buildTree(root, s, 0, input_length,bp,cky);
            Tree tree = new Tree(top);
            return tree;
        } else {
            return dummyParser(input);
        }

    }


    private void buildTree(Node parent, BackPointer A, int j, int i, HashMap<String, BackPointer>[][] bp, Map<String, Double>[][] cky) {


        if (A == null) {
            return;
        }

        int k = A.getK();

        String B = A.getNonT1();
        String C = A.getNonT2();
        Node new_node_B = new Node(B);
        Node new_node_C = new Node(C);

        if (C.startsWith("@")) {
            parent.addDaughter(new_node_B);

            if (bp[j][k] != null) {

                buildTree(new_node_B, bp[j][k].get(B), j, k, bp, cky);
            } else {
                new_node_B.addDaughter(new Node(sentence.get(j)));
            }
            if (bp[k][i] != null) {

                buildTree(parent, bp[k][i].get(C), k, i, bp, cky);

            } else {
                //new_node_C.addDaughter(new Node(sentence.get(k)));
            }

        } else {

            parent.addDaughter(new_node_B);
            parent.addDaughter(new_node_C);


            if (bp[j][k] != null) {
                buildTree(new_node_B, bp[j][k].get(B), j, k, bp, cky);
            } else {
                new_node_B.addDaughter(new Node(sentence.get(j)));
            }

            if (bp[k][i] != null) {
                buildTree(new_node_C, bp[k][i].get(C), k, i, bp, cky);
            } else {
                new_node_C.addDaughter(new Node(sentence.get(k)));
            }
        }

    }
}





	
	
	





