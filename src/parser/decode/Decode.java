package decode;

import grammar.Grammar;
import grammar.Rule;

import java.util.*;

import tree.Node;
import tree.Terminal;
import tree.Tree;

public class Decode {

	public static Set<Rule> m_setGrammarRules = null;
	public static Map<String, Set<Rule>> m_mapLexicalRules = null;
	public static Set<String> m_nonTerminals = null;
	public static Set<String> m_terminals = null;


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
			m_mapLexicalRules = g.getLexicalEntries();
		}
		return m_singDecoder;
	}

	public Tree decode(List<String> input) {

		// Done: Baseline Decoder
		//       Returns a flat tree with NN labels on all leaves 

		Tree t = new Tree(new Node("TOP"));
		Iterator<String> theInput = input.iterator();
		while (theInput.hasNext()) {
			String theWord = (String) theInput.next();
			Node preTerminal = new Node("NN");
			Terminal terminal = new Terminal(theWord);
			preTerminal.addDaughter(terminal);
			t.getRoot().addDaughter(preTerminal);
		}

		CYK(input);
		// TODO: CYK decoder
		//       if CYK fails, 
		//       use the baseline outcome


		return t;

	}

	private Tree CYK(List<String> input) {
		int input_length = input.size();

		int numOfTerminals = m_terminals.size();
		HashMap<String, Double>[][] cky = new HashMap[input_length][input_length];
		HashMap<String, BackPointer>[][] bp = new HashMap[input_length][input_length];

		for (int i = 1; i <= input_length; i++) {
			Iterator it = m_mapLexicalRules.values().iterator();
			while (it.hasNext()) {
				HashSet<Rule> elemnt = (HashSet<Rule>)it.next();
				Iterator it2=elemnt.iterator();
				while (it2.hasNext()) {
					Rule rule = (Rule) it2.next();

					if (rule.getRHS().toString().compareTo(input.get(i)) == 0) {
						if(cky[i-1][i]==null){
							cky[i-1][i]= new HashMap<>();
						}
						cky[i - 1][i].put(rule.getLHS().toString(), rule.getMinusLogProb());
					}
				}

			}

		}
		return new Tree();
	}
}





	
	
	





