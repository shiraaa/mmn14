package grammar;

import java.util.*;

import utils.CountMap;

/**
 * 
 * @author rtsarfat
 *
 * CLASS: Grammar
 * 
 * Definition: formally <N,T,S,R> 
 * Role: holds two collection of grammatical and lexical grammar rules  
 * Responsibility: define a start symbol 
 * 
 */

public class Grammar {

	protected Set<String> m_setStartSymbols = new HashSet<String>();
	protected Set<String> m_setTerminalSymbols = new HashSet<String>();
	protected Set<String> m_setNonTerminalSymbols = new HashSet<String>();



	protected Map<String,Map<String,Rule>> m_unaryRulesTable= new HashMap<>();
	protected Set<Rule> m_setSyntacticUnaryRules = new HashSet<Rule>();

	protected Set<Rule> m_setSyntacticRules = new HashSet<Rule>();
	protected Set<Rule> m_setLexicalRules = new HashSet<Rule>();
	protected CountMap<Rule> m_cmRuleCounts = new CountMap<Rule>();
	protected Map<String, Set<Rule>> m_lexLexicalEntries = new HashMap<String, Set<Rule>>();



	protected Map<String,ArrayList<Rule>> m_syntacticRulesTable=new HashMap<>();
		
	public Grammar() {
		super();
	}
	public Set<Rule> getSyntacticUnaryRules() {
		return m_setSyntacticUnaryRules;
	}

	public Map<String, Set<Rule>> getLexicalEntries() {
		return m_lexLexicalEntries;
	}

	public void setLexicalEntries(Map<String, Set<Rule>> m_lexLexicalEntries) {
		this.m_lexLexicalEntries = m_lexLexicalEntries;
	}
	public Map<String, ArrayList<Rule>> getSyntacticRulesTable() {
		return m_syntacticRulesTable;
	}
	public Map<String, Map<String,Rule>> getUnaryRulesTable() {
		return m_unaryRulesTable;
	}
	public CountMap<Rule> getRuleCounts() {
		return m_cmRuleCounts;
	}

	public void buildUnaryRulesMap() {
		/***
		 * ynaryTable
		 *
		 * 		|rule rhs1 	     |		 |
		 * 		|________________|_______|
		 * 		| rule rhs1 lhs1 |		 |
		 * 		| rule rhs1	lhs2 |		 |
		 */
		Iterator it=m_setSyntacticUnaryRules.iterator();
		while (it.hasNext()){
			Rule entry=(Rule)it.next();
			if(m_unaryRulesTable.containsKey((entry.getRHS().toString()))){
				Map<String,Rule> table_entry=m_unaryRulesTable.get(entry.getRHS().toString());
				if(table_entry.containsKey(entry.getLHS()))
				m_unaryRulesTable.get(entry.getRHS().toString()).put(entry.getLHS().toString(),entry);
			}
			else {
				HashMap new_hashMap=new HashMap<>();
				new_hashMap.put(entry.getLHS().toString(),entry);
				m_unaryRulesTable.put(entry.getRHS().toString(),new_hashMap);
			}
		}
	}
	public void buildSyntaticRulesMap() {
		/***
		 * Syntactic Table
		 *
		 * 		|rhs1 rhs2 	 |		 |
		 * 		|____________|_______|
		 * 		| rule  lhs1 |		 |
		 * 		| rule 	lhs2 |		 |
		 */
		Iterator it=m_setSyntacticRules.iterator();
		while (it.hasNext()) {
			Rule entry = (Rule) it.next();
			if (!entry.isUnary()) {
				if (m_syntacticRulesTable.containsKey((entry.getRHS().toString()))) {
					ArrayList<Rule> table_entry = m_syntacticRulesTable.get(entry.getRHS().toString());
					if (!table_entry.contains(entry)) //Not sure what it checks
						m_syntacticRulesTable.get(entry.getRHS().toString()).add(entry);
				} else {
					ArrayList new_hashSet = new ArrayList();
					new_hashSet.add(entry);
					m_syntacticRulesTable.put(entry.getRHS().toString(), new_hashSet);
				}
			}
		}
		int counter=0;
		for(ArrayList<Rule> set:m_syntacticRulesTable.values()){
			counter +=set.size();
		}
		System.out.println("table Size "+counter);
	}

	public void addRule(Rule r)
	{	
		Event eLhs = r.getLHS();
		Event eRhs = r.getRHS();
				
		if (r.isLexical())
		{
			// update the sets T, N, R
			getLexicalRules().add(r);
			getNonTerminalSymbols().addAll(eLhs.getSymbols());
			getTerminalSymbols().addAll(eRhs.getSymbols());
			
			// update the dictionary
			if (!getLexicalEntries().containsKey(eRhs.toString()) )
				getLexicalEntries().put(eRhs.toString(), new HashSet<Rule>());
			getLexicalEntries().get(eRhs.toString()).add(r);
		}
		else 
		{
			if(eRhs.getSymbols().size()==1){
				r.setUnary(true);
				getSyntacticUnaryRules().add(r);

			}
			// update the sets T, N, R
			getSyntacticRules().add(r);
			getNonTerminalSymbols().addAll(eLhs.getSymbols());
			getNonTerminalSymbols().addAll(eRhs.getSymbols());
		}
		
		// update the start symbol(s)
		if (r.isTop())
			getStartSymbols().add(eLhs.toString());
		
		// update the rule counts 
		getRuleCounts().increment(r);
	}
	

	public Set<String> getNonTerminalSymbols() {
		return m_setNonTerminalSymbols;
	}

	public Set<Rule> getSyntacticRules() {
		return m_setSyntacticRules;
	}

	public void setSyntacticRules(Set<Rule> syntacticRules) {
		m_setSyntacticRules = syntacticRules;
	}

	public Set<Rule> getLexicalRules() {
		return m_setLexicalRules;
	}

	public void setLexicalRules(Set<Rule> lexicalRules) {
		m_setLexicalRules = lexicalRules;
	}

	public Set<String> getStartSymbols() {
		return m_setStartSymbols;
	}

	public void setStartSymbols(Set<String> startSymbols) {
		m_setStartSymbols = startSymbols;
	}

	public Set<String> getTerminalSymbols() {
		return m_setTerminalSymbols;
	}

	public void setTerminalSymbols(Set<String> terminalSymbols) {
		m_setTerminalSymbols = terminalSymbols;
	}

	public int getNumberOfLexicalRuleTypes()
	{
		return getLexicalRules().size();
	}
	
	public int getNumberOfSyntacticRuleTypes()
	{
		return getSyntacticRules().size();
	}
	
	public int getNumberOfStartSymbols()
	{
		return getStartSymbols().size();
	}
	
	public int getNumberOfTerminalSymbols()
	{
		return getTerminalSymbols().size();
	}
	
	public void addStartSymbol(String string) {
		getStartSymbols().add(string);
	}

	public void removeStartSymbol(String string) {
		getStartSymbols().remove(string);
	}

	public void addAll(List<Rule> theRules) {
		for (int i = 0; i < theRules.size(); i++) {
			addRule(theRules.get(i));
		}
	}

}
