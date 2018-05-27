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
    public static HashMap<String, BackPointer> bp = null;
    public static Map<String, Double> cky = null;
    public static Map<String, ArrayList<String>> stem = null;
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

    private String cyk_str(int j, int i, String A) {
        return j + " " + i + " " + A;
    }

    private String stem_str(int j, int i) {
        return j + " " + i;
    }

    private Tree CYK(List<String> input) {
        int input_length = input.size();
        //Rule NN=(Rule)m_mapLexicalRules.get("NN");
        sentence = input;

        int numOfTerminals = m_terminals.size();
        cky = new HashMap();
        stem = new HashMap();

        //Back pointer
        bp = new HashMap();
//        try {


        for (int i = 1; i <= input_length; i++) {

            if (m_mapLexicalRules.containsKey(input.get(i - 1))) {

                for (Rule lex : m_mapLexicalRules.get(input.get(i - 1))) {

                    cky.put(cyk_str((i - 1), i, lex.getLHS().toString()), lex.getMinusLogProb());


                    if (!stem.containsKey(stem_str((i - 1), i))) {
                        stem.put(stem_str((i - 1), i),new ArrayList<>());
                    }

                        stem.get(stem_str((i - 1), i)).add(lex.getLHS().toString());


                    if (m_unaryRulesTable.containsKey(lex.getLHS().toString())) {

                        for (Map.Entry<String, Rule> unar_rule : m_unaryRulesTable.get(lex.getLHS().toString()).entrySet()) {

                            Rule rule = unar_rule.getValue();

                            if (!cky.containsKey(cyk_str((i - 1), i, unar_rule.getKey()))) {

                                cky.put(cyk_str((i - 1), i, unar_rule.getKey()), cky.get(cyk_str((i - 1), i, rule.getRHS().toString())) + rule.getMinusLogProb()); //**CHECK PROB**//
                                stem.get(stem_str((i - 1), i)).add( unar_rule.getKey());

                            }
                        }

                    }

                }

            } else {

                cky.put(cyk_str((i - 1), i, "NN"), (double) 0);
                if (!stem.containsKey(stem_str((i - 1), i))) {
                    stem.put(stem_str((i - 1), i),new ArrayList<>());
                }

                stem.get(stem_str((i - 1), i)).add("NN");

                if (m_unaryRulesTable.containsKey("NN")) {

                    for (Map.Entry<String, Rule> unar_rule : m_unaryRulesTable.get("NN").entrySet()) {

                        Rule rule = unar_rule.getValue();

                        if (!cky.containsKey(cyk_str((i - 1), i, unar_rule.getKey()))) {

                            cky.put(cyk_str((i - 1), i, unar_rule.getKey()), cky.get(cyk_str((i - 1), i, rule.getRHS().toString())) + rule.getMinusLogProb()); //**CHECK PROB**//
                           stem.get(stem_str((i - 1), i)).add(unar_rule.getKey());

                        }
                    }

                }

            }


        }


        ArrayList<Rule> A_set;
        String A = null;
        Rule rule;
        String unar_lhs;
        double A_prob;
        double B_prob;
        double C_prob;
        ArrayList<String> arr_b;
        ArrayList<String> arr_c;

        for (int span = 2; span <= input_length; span++) {
            for (int j = span - 2; j >= 0; j--) {
                for (int k = j + 1; k <= span - 1; k++) {
                    //System.out.println(span);


                    if (stem.containsKey(stem_str(j, k)) && stem.containsKey(stem_str(k, span))) {
                        arr_b = stem.get(stem_str(j, k));
                        arr_c = stem.get(stem_str(k, span));

                    } else {
                        continue;
                    }

                    for (String B :arr_b ) { //m_nonTerminals

                        if (!B.startsWith("@") ) { //&& cky.containsKey(cyk_str(j, k, B))


                            for (String C : arr_c) { //m_nonTerminals

                                A_set = m_SytacticRulesTable.get(B + " " + C);

                                if (A_set != null && cky.containsKey(cyk_str(k, span, C))) {

                                    B_prob = cky.get(cyk_str(j, k, B));
                                    C_prob = cky.get(cyk_str(k, span, C));

                                    for (Rule A_rule : A_set) {

                                        A = A_rule.getLHS().toString();
                                        A_prob = A_rule.getMinusLogProb();

                                        boolean contains = cky.containsKey(cyk_str(j, span, A));

                                        if (!contains || cky.get(cyk_str(j, span, A)) > A_prob + B_prob + C_prob) {

                                            cky.put(cyk_str(j, span, A), A_prob + B_prob + C_prob);
                                            bp.put(cyk_str(j, span, A), new BackPointer(k, B, C));

                                            if (!stem.containsKey(stem_str(j,span))) {
                                                stem.put(stem_str(j,span),new ArrayList<>());
                                            }
                                            if(!contains) {
                                                stem.get(stem_str(j, span)).add(A);
                                            }



                                            //Unary
                                            if (!contains && m_unaryRulesTable.containsKey(A)) {

                                                for (Map.Entry<String, Rule> unar_rule : m_unaryRulesTable.get(A).entrySet()) {

                                                    rule = unar_rule.getValue();
                                                    unar_lhs = unar_rule.getKey();

                                                    if (!cky.containsKey(cyk_str(j, span, unar_lhs))) { //add unary only if not exist , no point to add twice same non terminal

                                                        cky.put(cyk_str(j, span, unar_lhs), A_prob + B_prob + C_prob + rule.getMinusLogProb()); //**CHECK PROB**//
                                                        bp.put(cyk_str(j, span, unar_lhs), new BackPointer(k, B, C));
                                                        stem.get(stem_str(j,span)).add(unar_lhs);


                                                    }
                                                }

                                            }

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


//        } catch (Exception e) {
//            System.out.println(e);
//            return dummyParser(input);
//        }
        Node top = new Node("TOP");
        top.setRoot(true);

        if (bp.containsKey(0 + " " + input_length + " " + "S")) {
            BackPointer s = bp.get(0 + " " + input_length + " " + "S");
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

        if (C.startsWith("@")) {
            parent.addDaughter(new_node_B);

            if (bp.containsKey(cyk_str(j, k, B))) {

                buildTree(new_node_B, bp.get(cyk_str(j, k, B)), j, k);
            } else {
                new_node_B.addDaughter(new Node(sentence.get(j)));
            }
            if (bp.containsKey(cyk_str(k, i, C))) {

                buildTree(parent, bp.get(cyk_str(k, i, C)), k, i);

            } else {
                //new_node_C.addDaughter(new Node(sentence.get(k)));
            }

        } else {

            parent.addDaughter(new_node_B);
            parent.addDaughter(new_node_C);


            if (bp.containsKey((cyk_str(j, k, B)))) {

                buildTree(new_node_B, bp.get(cyk_str(j, k, B)), j, k);

            } else {

                new_node_B.addDaughter(new Node(sentence.get(j)));
            }

            if (bp.containsKey(cyk_str(k, i, C))) {

                buildTree(new_node_C, bp.get(cyk_str(k, i, C)), k, i);
            } else {
                new_node_C.addDaughter(new Node(sentence.get(k)));
            }
        }

    }
}





	
	
	





