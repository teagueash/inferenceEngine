import java.util.Map.Entry;
import java.util.*;
import java.io.*;

interface Term {

	public HashMap<Variable, Term> unify(Term y, HashMap<Variable, Term> theta);
	public Term findSub(HashMap<Variable, Term> theta);
	static java.io.PrintStream sout = System.out;

}

class Constant implements Term {

	String value;

	public Constant(String v) {
		this.value = v;
	}

	public HashMap<Variable, Term> unify(Term y, HashMap<Variable, Term> theta) {
		if (theta == null) return null;
		if (this.equals(y)) return theta;
		if (y instanceof Variable) {
			return y.unify(this, theta);
		} else {
			return null;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
            return true;
        }

        if (!(o instanceof Constant)) {
        	return false;
        }

        Constant c = (Constant) o;
        return value.equals(c.value);
	}

	@Override
	public int hashCode() {

		int hash = 37;

		hash = 37 * hash + value.hashCode();

		return hash;
	}

	public Term findSub(HashMap<Variable, Term> theta) {
		return this;
	}
}

class Variable implements Term {

	String name;

	public Variable(String v) {
		this.name = v;
	}

	public Term findSub(HashMap<Variable, Term> theta) {

		if (theta.containsKey(this)) {
			return theta.get(this).findSub(theta);
		} else {
			return this;
		}

	}

	public static String printTerm(Term x) {

		String term;
		if (x instanceof Constant) {
			term = ( ((Constant)(x)).value );
		} else if (x instanceof Variable) {
			term = ( ((Variable)(x)).name );
		} else {
			term = "Neither a VARIABLE, nor CONSTANT";
		}
		return term;
	}

	public HashMap<Variable, Term> unify(Term y, HashMap<Variable, Term> theta) {

		if (theta == null) return null;
		if (this.equals(y)) return theta;
		if (theta.keySet().contains(this)) {
			return (theta.get(this)).unify(y, theta);

		} 
		if (y instanceof Variable && theta.keySet().contains(y)) {
			return y.unify(this, theta);

		} 
		theta.put(this, y);
		for (Variable v: theta.keySet()) {
			theta.put(v, theta.get(v).findSub(theta));
		}
		return theta;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
            return true;
        }

        if (!(o instanceof Variable)) {
        	return false;
        }

        Variable v = (Variable) o;
        return name.equals(v.name);
	}

	@Override 
	public int hashCode() {

		int hash = 37;

		hash = 37 * hash + name.hashCode();

		return hash;
	}
}

class Tuple implements Term {

	Constant functor;
	Term[] parameters;

	public Tuple(Constant f, Term[] t) {
		this.functor = f;
		this.parameters = t;
	} 

	public String getPredicate() {

		return this.functor.value;
	}

	public Term[] getParameters() {

		return this.parameters;
	}

	public String print() {
		String fullName;
		fullName = this.functor.value;
		fullName+="(";
		for (int i = 0; i < this.parameters.length; ++i) {
			if (this.parameters[i] instanceof Variable) {
				fullName += ((Variable)this.parameters[i]).name;
			} else if (this.parameters[i] instanceof Constant) {
				fullName += ((Constant)this.parameters[i]).value;
			}
			if (i != this.parameters.length-1) {
				fullName += ",";
			}
		}
		fullName+=")";
		return fullName;
	}

	public String[] getStrings() {
		String[] vessel = new String[this.parameters.length];

		for (int i = 0; i < this.parameters.length; ++i) {
			if (this.parameters[i] instanceof Variable) {
				vessel[i] = ((Variable)this.parameters[i]).name;
			} else if (this.parameters[i] instanceof Constant) {
				vessel[i] = ((Constant)this.parameters[i]).value;
			}
		}
		return vessel;
	}

	public Term findSub(HashMap<Variable, Term> theta) {

		Term[] TermArray;
		Term[] prevArgs;
		Term newTerm;
		String predicateName;
		Tuple newTuple;

		predicateName = getPredicate();
		prevArgs = getParameters();
		TermArray = new Term[prevArgs.length];
		// Loop through all parameters, finding all substitutions
		for (int i = 0; i < prevArgs.length; ++i) {
				newTerm = prevArgs[i].findSub(theta);
				TermArray[i] = (newTerm);
		}

		newTuple = new Tuple(new Constant(predicateName), TermArray);
		return newTuple;
	}

	public HashMap<Variable, Term> unify(Term y, HashMap<Variable, Term> theta) {

		if (theta == null) return null;
		if (this.equals(y)) {
			
			// default return theta
			return theta;

		} else if (y instanceof Tuple ){
			// Tuple with many arguments, compare each
			Tuple ty = (Tuple)y;

			if (getParameters().length != ty.parameters.length || !(getPredicate().equals(ty.getPredicate()))) {
				return null;
			} else {
				for (int i = 0; i < getParameters().length; ++i) {
					this.parameters[i].unify(ty.parameters[i], theta);
					// sout.println("here");
					if (theta == null) return null;

				}
				return theta;
			}

		} else {

			return null;
		}
	}

	@Override 
	public boolean equals(Object o) {

		if (o == this) return true;
   		if (!(o instanceof Tuple)) return false;

		Tuple thatTuple = (Tuple) o;

		boolean parity = true;

		if (getParameters().length != thatTuple.getParameters().length) return false;
		if (!getPredicate().equals(thatTuple.getPredicate())) return false;
		for (int i = 0; i < getParameters().length; ++i) {
			sout.println(i);
			if (!(this.parameters[i]).equals(thatTuple.parameters[i])) parity = false;
		} 
		return parity;

	}

	@Override 
	public int hashCode() {

		int code1 = print().hashCode();

		int hash = 37;
		hash = 37 * hash + code1;

		return hash;
	}
}

class Literal implements Comparable<Literal>{

	static java.io.PrintStream sout = System.out;

	public boolean Sign;
	Term literal;

	public Literal(boolean sign, Term t) {
		this.literal = t;
		this.Sign = sign;

	}

	public String[] getStrings() {
		Tuple tuple = (Tuple)this.literal;
		String[] vessel = new String[tuple.parameters.length];

		for (int i = 0; i < tuple.parameters.length; ++i) {
			if (tuple.parameters[i] instanceof Variable) {
				vessel[i] = ((Variable)tuple.parameters[i]).name;
			} else if (tuple.parameters[i] instanceof Constant) {
				vessel[i] = ((Constant)tuple.parameters[i]).value;
			}
		}
		return vessel;
	}

	public boolean sign() {
		return this.Sign;
	}

	public String getPredicate() {
		Tuple tuple = (Tuple)this.literal;
		return tuple.functor.value;
	}

	public int getSign() {
		return this.Sign ? -1 : 1;
	}
	public String print() {
		String str = "";
		Tuple tup = (Tuple)this.literal;
		return str;
	}

	public String toString() {
		String str = "";
		if (sign()) str+="~";
		str+=this.getPredicate();
		return str;
	}

	public Tuple getTuple() {
		return (Tuple) this.literal;
	}

	@Override
	public int compareTo(Literal thatLiteral) {

		Tuple thisTuple = (Tuple)this.literal;
		Tuple thatTuple = (Tuple)thatLiteral.literal;

		return Comparator.comparing(Literal::getSign)
					.thenComparing(Literal::getPredicate)
					.compare(this, thatLiteral);
	}

	@Override 
	public boolean equals(Object o) {

		if (o == this) return true;
   		if (!(o instanceof Literal)) return false;

		Literal thatLiteral = (Literal) o;
		Tuple thatTuple = (Tuple)thatLiteral.literal;
		Tuple thisTuple = (Tuple)literal;
		Constant thisConst;
		Constant thatConst;
		Variable thisVar;
		Variable thatVar;
		boolean parity = true;

		if (sign() != thatLiteral.sign()) return false;
		if (!thisTuple.getPredicate().equals(thatTuple.getPredicate())) return false;
		if (thisTuple.getParameters().length != thatTuple.getParameters().length) return false;

		for (int i = 0; i < thisTuple.parameters.length; ++i) {
			if (thisTuple.parameters[i] instanceof Variable && thatTuple.parameters[i] instanceof Variable) {
				thisVar = (Variable)thisTuple.parameters[i];
				thatVar = (Variable)thatTuple.parameters[i];
				if (!thisVar.equals(thatVar)) parity = false; 

			} else if (thisTuple.parameters[i] instanceof Constant && thatTuple.parameters[i] instanceof Constant) {
				thisConst = (Constant)thisTuple.functor;
				thatConst = (Constant)thatTuple.functor;
				if (!thisConst.equals(thatConst)) parity = false;
			} else {
				parity = false;
			}
		}
		return true;
	}

	@Override 
	public int hashCode() {

		int code1 = toString().hashCode();

		int hash = 37;
		hash = 37 * hash + code1;

		return hash;
	}
}

class Clause {

	Literal[] literals;

	public Clause(Literal[] literals) {
		// this.literals = literals;
		// Literal[] temp = literals;
		// temp = mutateLiterals();
		this.literals = literals;
		// this.literals = temp;
	}

	public void mutateLiterals() {

		ArrayList<Literal> aLit = new ArrayList<>(Arrays.asList(literals));
		Set<Literal> sLit = new LinkedHashSet<>(aLit);
		aLit = new ArrayList<>(sLit);
		Literal[] lit = aLit.toArray(new Literal[literals.length]);

		this.literals = lit;
		// return lit;
	}

	public Literal[] getLiterals() {
		return this.literals;
	}

	public int getLength() {
		return this.literals.length;
	}

	public Literal get(int n) {
		return this.literals[n];
	}

	public Literal[] getArray() {
		return this.literals;
	}

	public boolean isEmpty() {

		return this.literals.length == 0;
	}

	public Clause standardizeClause() {

		int index = 0;


		Arrays.sort(this.literals);
		int size = this.literals.length;
		Literal[] literalArray = new Literal[size];
		HashMap<Variable, Variable> hm = new HashMap<Variable, Variable>();

		Clause newClause;
		for (int i = 0; i < this.literals.length; ++i) {
			Literal l = this.literals[i];
			boolean sign = l.sign();
			
			Tuple tup = (Tuple) l.literal;
			Constant constant = (Constant) tup.functor;
			Term[] termArray = new Term[tup.parameters.length];

			for (int j = 0; j < tup.parameters.length; ++j) {

				if (tup.parameters[j] instanceof Variable) {
					Variable v = (Variable) tup.parameters[j];
					if (hm.containsKey(v)) {
						termArray[j] = hm.get(v);
					} else {
						Variable var = new Variable("y"+index);
						termArray[j] = var;
						++index;
						hm.put(v, var);
					}
				} else {
					termArray[j] = tup.parameters[j];
				}
			}

			Tuple tup2 = new Tuple(constant, termArray);
			Literal lit2 = new Literal(sign, tup2);
			literalArray[i] = lit2;
		}
		Arrays.sort(literalArray);
		newClause = new Clause(literalArray);
		System.out.println(newClause.toString());
		return newClause;

	}

	public boolean equalsArray(Literal[] otherLiteralArray) {

		Literal l1;
		Literal l2;

		if (this.literals.length != otherLiteralArray.length) return false;

		for (int i = 0; i < this.literals.length; ++i) {
			l1 = this.literals[i];
			l2 = otherLiteralArray[i];
			if ( !(l1.equals(l2)) ) return false;
		}
		return true;
	}

	@Override 
	public boolean equals(Object o) {

		if (o == this) return true;
   		if (!(o instanceof Clause)) return false;

		Clause otherClause = (Clause) o;
		Literal l1;
		Literal l2;
		if (literals.length != otherClause.literals.length) return false;

		return equalsArray(otherClause.literals);
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		Literal[] cArr = getLiterals();

		for (Literal l : cArr) {

			if (l.sign()) sb.append("~");
			Tuple t = l.getTuple();
			sb.append(t.print());
			sb.append(" | ");

		}

		if (sb.length() > 0) sb.setLength(sb.length()-3);


		return sb.toString();

	}

	@Override
	public int hashCode() {

		// Standardize this, then take hashcode of string of this

		Clause c = standardizeClause();

		int hash = 37;
		int code2 = c.toString().hashCode();

		hash = 37 * hash + code2;

		return hash;
	}
}

class homework {

	static java.io.PrintStream sout = System.out;
	static final long TIME_LIMIT = 15000;
	static long startTime;
	static long endTime;

	private homework() {}

	static int clauseNumber;

	public static void incrementClauseNumber() {
		++clauseNumber;
	}

	public static Set<Clause> resolve(Clause ci, Clause cj) {


		LinkedHashSet<Clause> resolvents = new LinkedHashSet<Clause>();
		ArrayList<Literal> pos = new ArrayList<Literal>();
		ArrayList<Literal> neg = new ArrayList<Literal>();

		Literal lit;
		for (int i = 0; i < ci.getLength(); ++i) {
			lit = ci.get(i);
			if (lit.sign()) {
				neg.add(lit);
			} else {
				pos.add(lit);
			}
		}
		for (int i = 0; i < cj.getLength(); ++i) {
			lit = cj.get(i);
			if (lit.sign()) {
				neg.add(lit);
			} else {
				pos.add(lit);
			}
		}

		// Look at positives of outer and negatives of inner
		// Then inverse
		for (int i = 0; i < 2; ++i) {

			ArrayList<Literal> tempPos = new ArrayList<Literal>();
			ArrayList<Literal> tempNeg = new ArrayList<Literal>();

			if (i == 0) {
				for ( Literal l : ci.getArray() ) {
					if (l.sign()) tempNeg.add(l);
				}
				for ( Literal l : cj.getArray() ) {
					if (!l.sign()) tempPos.add(l);
				}
			} else {
				for ( Literal l : cj.getArray() ) {
					if (l.sign()) tempNeg.add(l);
				}
				for ( Literal l : ci.getArray() ) {
					if (!l.sign()) tempPos.add(l);
				}
			}

			for ( Literal posLit : tempPos ) {
				for ( Literal negLit : tempNeg ) {

					LinkedHashMap<Variable, Term> theta = new LinkedHashMap<Variable, Term>();

					if ( posLit.getTuple().unify(negLit.getTuple(), theta) != null ) {

						ArrayList<Literal> newPL = new ArrayList<Literal>();
						ArrayList<Literal> newNL = new ArrayList<Literal>();
						Clause resolventClause;

						boolean found = false;
						for ( Literal l : pos ) {
							if (!found && posLit.equals(l)) {
								found = true;
								continue;
							}
							newPL.add(new Literal(l.sign(), l.getTuple().findSub(theta)));
						}
						found = false;
						for ( Literal l : neg ) {
							if (!found && negLit.equals(l)) {
								found = true;
								continue;
							}
							newNL.add(new Literal(l.sign(), l.getTuple().findSub(theta)));
						}
						Literal[] litArray = new Literal[newPL.size() + newNL.size()];
						int counter = 0;
						for (int k = 0; k < newPL.size(); ++k, ++counter) {
							litArray[counter] = newPL.get(k);
						}	
						for (int k = 0; k < newNL.size(); ++k, ++counter) {
							litArray[counter] = newNL.get(k);
						}
						resolventClause = new Clause(litArray);
						resolvents.add(resolventClause);

					}
				}
			}

		}

		return resolvents;
	}

	public static boolean resolution(LinkedHashSet<Clause> kB, Clause alpha) {

		// Copy over KB into new knowledge base
		Set<Clause> knowledgeBase = new LinkedHashSet<Clause>(kB);
		// Add negated queries to kB
		knowledgeBase.add(alpha);
		Long currentTime;
	
		Set<Clause> newClauses = new LinkedHashSet<Clause>();

		do {
			currentTime = System.currentTimeMillis();
			if (currentTime > endTime) return false;
			List<Clause> clausesAsList = new ArrayList<Clause>(knowledgeBase);

			for (int i = 0; i < clausesAsList.size()-1; ++i) {

				Clause ci = clausesAsList.get(i);

				for (int j = i+1; j < clausesAsList.size(); ++j) {

					Clause cj = clausesAsList.get(j);
					// Unify and create resolvents
					Set<Clause> resolvents = resolve(ci, cj);
					// check if size > 0
					if (resolvents.size() > 0) {
						// loop through set of clauses and check if any are empty
						for (Clause c : resolvents) {
							if (c.isEmpty()) {
								return true;
							}
						}
					}
					// Generate new sentences via union with resolvents
					newClauses.addAll(resolvents);
				}
			}
			// If subset, return false
			if (knowledgeBase.containsAll(newClauses)) {
				return false;
			}

			// Update what we know
			knowledgeBase.addAll(newClauses);
			sout.println("");
			sout.println("updated KB: ");
			for ( Clause c : knowledgeBase) {
				sout.println(c.toString());
			}
			sout.println("end");

		} while (true);
	}

	/************************************************************  
	 *	 	carve up kBMessenger elements to parse out literals, 
	 *		predicates, and arguments
	 ************************************************************/ 

	private static LinkedHashSet<Clause> buildKB(LinkedHashSet<Clause> knowledgeBase, ArrayList<ArrayList<String>> vessel) {

		Term Term;
		Clause clause;
		Literal literal;

		int numLiterals;
		String stringLiteral;
		String stringPredicate;
		String stringArgs;
		String[] stringArrayArgs;
		Literal[] arrayOfLiterals;
		Term[] TermArgs;

		int totalNumClauses = vessel.size(); 
		for (int i = 0; i < totalNumClauses; ++i) {

			numLiterals = vessel.get(i).size();
			arrayOfLiterals = new Literal[numLiterals];
			clause = new Clause(arrayOfLiterals);

			// Generate each literal in current clause
			for (int j = 0; j < numLiterals; ++j) {
				boolean negated = false;
				stringLiteral = vessel.get(i).get(j);
				/*********************************
				 * BANDAID TO FIX EXTRA SPACING
				 *********************************/
				stringLiteral = stringLiteral.replaceAll("\\s","");
				/*********************************
				 * END OF BANDAID
				 *********************************/
				// DeTermine if negated predicate
				if (stringLiteral.charAt(0) == '~') {
					negated = true;
					stringLiteral = stringLiteral.replaceAll("\\~", "");
				}
				// Extract the functor to a String
				stringPredicate = stringLiteral.replaceAll("\\(.*\\)", "");
				// Extract the arguments to a String
				int start = stringLiteral.indexOf("(");
				int end = stringLiteral.lastIndexOf(")");
				stringArgs = stringLiteral.substring(start+1, end);
				stringArrayArgs = stringArgs.split(",");

				// Loop through arguments and istantiate Constants/Variables
				TermArgs = new Term[stringArrayArgs.length];
				for (int k = 0; k < stringArrayArgs.length; ++k) {
					if ( Character.isLowerCase(stringArrayArgs[k].charAt(0))) {
						// data is a variable
						// stringArrayArgs[k] = stringArrayArgs[k]+clauseNumber;
						stringArrayArgs[k] = stringArrayArgs[k];
						Variable var = new Variable(stringArrayArgs[k]);
						TermArgs[k] = var;

					} else {
						// data is a constant
						Constant constant = new Constant(stringArrayArgs[k]);
						TermArgs[k] = constant;
					}

				} 
				// Instantiate Tuples & Literals
				Constant predicate = new Constant(stringPredicate);
				Tuple tuple = new Tuple(predicate, TermArgs);
				literal = new Literal(negated, tuple); 

				// add literal to clause
				clause.literals[j] = literal; 
			}
			// Sort clauses based off negation and monotonicity
			Arrays.sort(clause.literals);
			// Add sorted clauses to KB
			knowledgeBase.add(clause);
			// New sentence constructed, update global clause # counter
			// incrementClauseNumber();
		}
		return knowledgeBase;
	}

	public static Clause ask(String query) {

		Clause clause;
		Literal literal;

		boolean negated;
		String stringLiteral;
		String stringPredicate;
		String stringArgs;
		String[] stringArrayArgs;
		Literal[] arrayLiterals;
		Term[] TermArgs;

		// Instantiate clause object that holds 1 query (literal)
		clause = new Clause(new Literal[1]);
		// stringLiteral used to obtain various components 
		stringLiteral = query;
		// Extract the functor to a String & deTermine if negated predicate
		if (stringLiteral.charAt(0) == '~') {
			negated = false;
			stringLiteral = stringLiteral.replaceAll("\\~", "");
		} else {
			negated = true;
		}
		stringPredicate = stringLiteral.replaceAll("\\(.*\\)", "");
		// Extract the arguments to a String
		int start = stringLiteral.indexOf("(");
		int end = stringLiteral.lastIndexOf(")");
		stringArgs = stringLiteral.substring(start+1, end);
		stringArrayArgs = stringArgs.split(",");

		// Loop through arguments and istantiate Constants/Variables
		TermArgs = new Term[stringArrayArgs.length];
		for (int i = 0; i < stringArrayArgs.length; ++i) {
			if ( Character.isLowerCase(stringArrayArgs[i].charAt(0))) {
				// data is a variable
				Variable var = new Variable(stringArrayArgs[i]);
				TermArgs[i] = var;

			} else {
				// data is a constant
				Constant constant = new Constant(stringArrayArgs[i]);
				TermArgs[i] = constant;
			}
		} 

		// Instantiate Predicate, Tuples & Literals
		Constant predicate = new Constant(stringPredicate);
		Tuple tuple = new Tuple(predicate, TermArgs);
		literal = new Literal(negated, tuple); 
		// Assign value to clause object's literals array
		clause.literals[0] = literal;

		return clause;
	}

	public static void printAnswers(String[] answers) {
		StringBuilder sb = new StringBuilder();
		sb.append("CONCLUSIONS BELOW:");
		sb.append("\n");
		sb.append(Arrays.toString(answers));
		sb.append("\n");
		sout.println(sb);
	}

	public static void main(String[] args) {

		ArrayList<String> fileContents = new ArrayList<String>();
		ArrayList<String> queries = new ArrayList<String>();
		ArrayList<String> sentences = new ArrayList<String>();
		LinkedHashSet<Clause> kB = new LinkedHashSet<Clause>();
		ArrayList<ArrayList<String>> kBMessenger = new ArrayList<ArrayList<String>>();
		ArrayList<String> fact;
		String[] answers;
		int queryLength = 0;
		int totalLength = 0;

		/*  Read input file and store each line as a String in
		 *  arraylist fileContents
		 */
		try (BufferedReader br = new BufferedReader(new FileReader("input.txt"))) {

			String currentLine;

			while ((currentLine = br.readLine()) != null) {
				fileContents.add(currentLine); 
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Add File Contents to Query and Sentence Arrays
		queryLength = Integer.parseInt(fileContents.get(0));
		totalLength = fileContents.size();
		for (int i = 1; i <= queryLength; ++i) {
			queries.add(fileContents.get(i));
		}
		int buffer = 2;
		for (int i = queryLength + buffer; i < totalLength; ++i) {
			sentences.add(fileContents.get(i));
		}

		/*   Regex each element in sentences, removing spaces and delimiters
		 *	 Add result of regex operation to knowledge base
		 */  

		LinkedHashSet<Clause> lhs = new LinkedHashSet<Clause>();
		Literal[] lit = new Literal[3];
		Literal[] lit2 = new Literal[3];
		Term[] termArr = new Term[3];
		Term[] termArr2 = new Term[1];
		Term[] termArr3 = new Term[3];
		Term[] debugTerm = new Term[3];
		debugTerm[0] = new Variable("y");
		debugTerm[1] = new Variable("x");
		debugTerm[2] = new Variable("z");
		termArr[0] = new Variable("x");
		termArr[1] = new Variable("y");
		termArr[2] = new Variable("z");
		termArr2[0] = new Constant("Allen");
		// sout.println(termArr[0].equals(debugTerm[1]));
		Tuple t = new Tuple(new Constant("Man"), termArr);
		Tuple t2 = new Tuple(new Constant("Boy"), termArr2);
		Tuple t3 = new Tuple(new Constant("Man"), termArr);
		Tuple t4 = new Tuple(new Constant("Man"), debugTerm);
		Literal l = new Literal(false, t);
		Literal l2 = new Literal(true, t2);
		Literal l3 = new Literal(false, t3);
		Literal la = new Literal(false, t);
		Literal la2 = new Literal(true, t2);
		Literal la3 = new Literal(false, t3);
		// sout.println(l.equals(l3));
		lit[0] = l;
		lit[1] = l2;
		lit[2] = l3;
		lit2[0] = la;
		lit2[1] = la2;
		lit2[2] = la3;
		Clause c = new Clause(lit);
		Clause c2 = new Clause(lit2);
		lhs.add(c);
		lhs.add(c2);

		c = c.standardizeClause();
		sout.println(c.toString());
		sout.println("\n");

		String[] temp;
		for (int i = 0; i < sentences.size(); ++i) {
			temp = sentences.get(i).split("\\s+\\|");
			fact = new ArrayList<String>(Arrays.asList(temp));
			kBMessenger.add(fact);
		}

		// build knowledge base
		buildKB(kB, kBMessenger);

		Iterator<Clause> iter = kB.iterator();
		boolean result = false;
		answers = new String[queries.size()];
		int i = 0;
		while (i < queries.size()) {
			startTime = System.currentTimeMillis();
			endTime = startTime + 20000;
			result = resolution(kB, ask(queries.get(i)));
			answers[i] = String.valueOf(result).toUpperCase();
			++i;
		}


	
		printAnswers(answers);
		
		try {
		    PrintWriter writer = new PrintWriter("output.txt", "UTF-8");
		    for (String str : answers) {
		    	writer.println(str);
		    }
			// writer.println(Arrays.toString(answers));
			writer.close();
		} catch (IOException e) {
		   System.out.println("Error");
		}
	}
}