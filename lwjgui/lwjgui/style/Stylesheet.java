package lwjgui.style;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import lwjgui.scene.Node;

public class Stylesheet {
	private String source;
	private boolean compiled;
	private HashMap<String, StyleSelector> idToStyleSelector = new HashMap<>();
	private HashMap<StyleSelector, StyleData> styleData = new HashMap<>();

	public Stylesheet(String css) {
		this.source = css;
	}
	
	public String getSource() {
		return this.source;
	}
	
	/**
	 * Apply generic styling to a node via its classes/tag
	 * @param node
	 */
	public void applyStyling(Node node) {
		// Start list of operations
		Map<String, StyleOperationValue> declarations = new HashMap<>();
		
		// JUST FOR NORMAL SELECTOR STYLING FIRST
		{
			// Apply styling for the DOM TAG
			computeStyling(node, StyleSelectorType.TAG, node.getElementType(), declarations, false);
			
			// Apply styling for the ID
			computeStyling(node, StyleSelectorType.ID, node.getElementId(), declarations, false);
			
			// Apply styling for the class
			ArrayList<String> classList = node.getClassList();
			for (int i = 0; i < classList.size(); i++) {
				String claz = classList.get(i);
				computeStyling(node, StyleSelectorType.CLASS, claz, declarations, false);
			}
		}
		
		// NEXT JUST FOR PSEUDO CLASSES
		{
			// Apply styling for the DOM TAG
			computeStyling(node, StyleSelectorType.TAG, node.getElementType(), declarations, true);
			
			// Apply styling for the ID
			computeStyling(node, StyleSelectorType.ID, node.getElementId(), declarations, true);
			
			// Apply styling for the class
			ArrayList<String> classList = node.getClassList();
			for (int i = 0; i < classList.size(); i++) {
				String claz = classList.get(i);
				computeStyling(node, StyleSelectorType.CLASS, claz, declarations, true);
			}
		}
		
		applyStyling(node, declarations);
		declarations.clear();
	}

	/**
	 * Apply styling to a node usined the specified element tag
	 * @param node
	 * @param forceElementType
	 */
	public void applyStyling(Node node, String forceElementType) {
		Map<String, StyleOperationValue> declarations = new HashMap<>();
		computeStyling(node, StyleSelectorType.TAG, forceElementType, declarations, false);
		computeStyling(node, StyleSelectorType.TAG, forceElementType, declarations, true);
		applyStyling( node, declarations );
		declarations.clear();
	}
	
	private void applyStyling(Node node, Map<String, StyleOperationValue> declarations) {
		if ( declarations.size() == 0 )
			return;
		
		// Iterate over operations and apply
		Iterator<Entry<String, StyleOperationValue>> iterator = declarations.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<String, StyleOperationValue> val = iterator.next();
			val.getValue().process(node);
		}
	}

	private boolean computeStyling(Node node, StyleSelectorType type, String id, Map<String, StyleOperationValue> combinedDeclarations, boolean justPseudoClasses) {
		if ( id == null || id.length() == 0 )
			return false;
		
		StyleSelector selector = idToStyleSelector.get(id);
		if ( selector == null )
			return false;
		
		if ( selector.getType() != type )
			return false;
		
		StyleData data = styleData.get(selector);
		if ( data == null )
			return false;
		
		// Apply the styling!
		if ( justPseudoClasses ) {
			List<PseudoClass> pseudoClasses = data.getPseudoClassOrder();
			for (int i = 0; i < pseudoClasses.size(); i++) {
				PseudoClass pseudoClass = pseudoClasses.get(i);
				if ( pseudoClass == PseudoClass.NORMAL )
					continue;
				
				// Add this pseudoClasses declarations to the combined list
				if ( pseudoClass.isActive(node) )
					addPseudoClassStyle(data, pseudoClass, combinedDeclarations);
			}
		} else {
			if ( PseudoClass.NORMAL.isActive(node) ) {
				addPseudoClassStyle(data, PseudoClass.NORMAL, combinedDeclarations);
			}
		}
		
		return true;
	}

	/**
	 * Adds a style data to the combined declarations. (Can combine multiple pseudo classes, but only keep the most recent entries).
	 * @param data
	 * @param methodType
	 * @param combinedDeclarations
	 */
	private void addPseudoClassStyle(StyleData data, PseudoClass methodType, Map<String, StyleOperationValue> combinedDeclarations) {
		List<StyleOperationValue> declarations = data.getDeclarationData(methodType);
		if ( declarations == null )
			return;
		
		if ( declarations.size() <= 0 )
			return;
		
		for (int i = 0; i < declarations.size(); i++) {
			StyleOperationValue op = declarations.get(i);
			combinedDeclarations.put(op.getName(), op);
		}
	}

	public boolean isCompiled() {
		return this.compiled;
	}

	/**
	 * Compule Stylesheet
	 */
	public boolean compile() throws StylesheetCompileError {
		String newSource = source.replaceAll("[?<=\\/\\*](.*)[?=\\*\\/]", "");
		try {
			StringBuilder currentSelector = new StringBuilder();
			for (int i = 0; i < newSource.length(); i++) {
				char c = newSource.charAt(i);
	
				if (c == '{') {
					List<StyleSelector> selectors = parseSelectors(currentSelector.toString());
					currentSelector.setLength(0);
					if (selectors == null)
						continue;
	
					StringBuilder content = new StringBuilder();
					for (int j = i; j < newSource.length(); j++) {
						char cc = newSource.charAt(j);
	
						if (cc == '}') {
							// Parse content
							parseContent(selectors, content.toString());
							i = j;
							break;
						} else {
							content.append(cc);
						}
					}
				} else {
					// Read selector string
					currentSelector.append(c);
				}
			}
			this.compiled = true;
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			throw new StylesheetCompileError();
		}
	}

	/**
	 * Parse the declaration data for the selector.
	 * @param selectors
	 * @param content
	 */
	private void parseContent(List<StyleSelector> selectors, String content) {
		System.out.println("Found selectors (" + selectors.size() + "): " + Arrays.toString(selectors.toArray()));

		Map<Object, StyleVarArgs> data = new HashMap<>();

		// Parse out declaration data
		String currentKey = null;
		StringBuilder t = new StringBuilder();
		for (int i = 1; i < content.length(); i++) {
			char c = content.charAt(i);

			// Store key
			if (c == ':') {
				currentKey = t.toString().trim();
				t.setLength(0);
				continue;
			}

			// End key
			if (c == ';') {
				String currentVal = t.toString().trim();
				StyleVarArgs val = parseArgs(currentVal);
				if ( val != null ) {
					if ( data.containsKey(currentKey) ) {
						data.get(currentKey).add(val);
					} else {
						data.put(currentKey, val);
					}
				}
				currentKey = null;
				t.setLength(0);
				continue;
			}

			// Continue reading
			t.append(c);
		}
		
		// In case there was an unfinished declaration...
		if ( t.length() > 0 && currentKey != null ) {
			String currentVal = t.toString().trim();
			StyleVarArgs val = parseArgs(currentVal);
			if ( val != null ) {
				if ( data.containsKey(currentKey) ) {
					data.get(currentKey).add(val);
				} else {
					data.put(currentKey, val);
				}
			}
			currentKey = null;
			t.setLength(0);
		}
		
		// Iterate over all selectors and attach declaration data
		for (int i = 0; i < selectors.size(); i++) {
			StyleSelector selector = selectors.get(i);
			StyleSelector key = idToStyleSelector.get(selector.selector);
			if ( key == null ) {
				idToStyleSelector.put(selector.selector, selector);
				key = selector;
			}
			
			StyleData sData = styleData.get(key);
			if ( sData == null ) {
				sData = new StyleData();
				styleData.put(selector, sData);
			}

			final StyleData sDataFinal = sData;

			data.entrySet().forEach(entry -> {
				System.out.println(selector.selector + " :: '" + entry.getKey() + "' = '" + entry.getValue() + "'");
				StyleOperation op = StyleOperations.match(entry.getKey().toString());
				
				if ( op != null ) {
					StyleOperationValue operation = new StyleOperationValue(op, entry.getValue());
					sDataFinal.addDeclarationData(selector.getModifier(), operation); 
				}
			});
		}
		
		data.clear();
	}
	
	/**
	 * Take a css value and parse it into a list of args
	 * @param content
	 * @return
	 */
	private StyleVarArgs parseArgs(String content) {
		content = content.replace(", ", ",").replace(") ", ")").replace("( ", "(");
		
		StyleVarArgs arguments = new StyleVarArgs();
		
		ArrayList<Object> temp = new ArrayList<Object>();
		String current = "";
		boolean inFunction = false;
		StyleFunction sFunc = null;
		for (int i = 0; i < content.length(); i++) {
			char c = content.charAt(i);
			
			boolean isLastChar = i+1 == content.length();
			
			if ( (c == ' ' || i+1 == content.length()) && !inFunction ) {
				if ( isLastChar && !inFunction )
					current += c;
				
				// Get the value from the param
				String t = current.trim();
				current = "";
				Object o = parseVal(t);
				
				// Add current params as an argument, and reset.
				if ( o != null )
					temp.add(o);
				
				// If this is the last character, Add current params as an argument, and reset.
				if ( isLastChar && !inFunction ) {
					StyleParams params = new StyleParams(temp.toArray(new Object[temp.size()]));
					if ( params.size() > 0 ) {
						arguments.add(params);
						temp.clear();
					}
				}
			} else {
				if ( c == '(' ) {
					inFunction = true;
					sFunc = new StyleFunction(current.trim());
					current = "";
					continue;
				} else if ( c == ')' ) {
					inFunction = false;
					StyleVarArgs argFunc = parseArgs(current.trim().replace(" ", "").replace(",", " "));
					if ( argFunc.size() > 0 ) {
						sFunc.args = argFunc;
						temp.add(sFunc);
						sFunc = null;
					}
					current = "";
					
					if ( isLastChar ) {
						arguments.add(new StyleParams(temp.toArray(new Object[temp.size()])));
						temp.clear();
					}
					continue;
				} else if ( c == ',' && !inFunction ) { // Add current params as an argument, and reset.
					// Get the value from the param
					String t = current.trim();
					current = "";
					Object o = parseVal(t);
					
					// Add current params as an argument, and reset.
					if ( o != null )
						temp.add(o);
					
					arguments.add(new StyleParams(temp.toArray(new Object[temp.size()])));
					temp.clear();
					current = "";
				} else {
					current = current + c;
				}
			}
		}
		return arguments;
	}

	/**
	 * Try to parse string to a value (number, percent, string).
	 * 
	 * @param value
	 * @return
	 */
	private Object parseVal(String value) {
		Object t = parseNumber(value);
		if (t != null)
			return t;

		t = parsePercent(value);
		if (t != null)
			return t;

		return value;
	}

	/**
	 * Try to parse string to number.
	 * 
	 * @param value
	 * @return
	 */
	private Number parseNumber(String value) {
		if (!value.endsWith("px") && !value.endsWith("pt"))
			return null;
		
		value = value.substring(0,value.length()-2);
		try {
			Number t = Double.parseDouble(value);
			return t;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Try to parse string to percentage.
	 * 
	 * @param value
	 * @return
	 */
	private Percentage parsePercent(String value) {
		if (!value.endsWith("%"))
			return null;

		value = value.substring(0, value.length() - 1);
		Number num = parseNumber(value + "px");
		if (num == null)
			return null;

		return new Percentage(num.doubleValue());
	}

	/**
	 * Turn string of selectors into list.
	 * @param selectorString
	 * @return
	 */
	private List<StyleSelector> parseSelectors(String selectorString) {
		List<StyleSelector> ret = new ArrayList<>();
		selectorString = selectorString.trim();
		String[] t = selectorString.contains(",") ? selectorString.split("\\,") : new String[] { selectorString };
		for (int i = 0; i < t.length; i++) {
			String t2 = t[i].trim();
			ret.add(new StyleSelector(t2));
		}

		return ret;
	}

	/**
	 * This class defines the order in which CSS pseudo classes are fired. It also contines the data (routine) of the pseudo class.
	 * @author Andrew
	 *
	 */
	class StyleData {

		private Map<PseudoClass, List<StyleOperationValue>> routines = new HashMap<>();
		private List<PseudoClass> routineOrder = new ArrayList<>();

		public void addDeclarationData(PseudoClass pseudoClass, StyleOperationValue styleOperationValue) {
			if ( pseudoClass == null )
				return;
			
			if ( !this.routines.containsKey(pseudoClass) ) {
				this.routines.put(pseudoClass, new ArrayList<>());
				this.routineOrder.add(pseudoClass);
			}
			
			this.routines.get(pseudoClass).add(styleOperationValue);
		}
		
		public List<PseudoClass> getPseudoClassOrder() {
			return this.routineOrder;
		}

		public List<StyleOperationValue> getDeclarationData(PseudoClass pseudoClass) {
			return this.routines.get(pseudoClass);
		}
	}

	class Percentage {
		private double percent;

		public Percentage(double percent) {
			this.percent = percent;
		}

		public double getPercent() {
			return percent;
		}

		public double getValue() {
			return percent / 100d;
		}
		
		public String toString() {
			return percent + "%";
		}
	}

	class StyleSelector {
		private String selector;
		private StyleSelectorType type;
		private PseudoClass modifier = PseudoClass.NORMAL; // Useful for :hover support

		public StyleSelector(String selector) {
			// Parse out event
			if ( selector.contains(":") ) {
				String[] t = selector.split(":", 2);
				selector = t[0];
				this.modifier = PseudoClass.match(t[1]);
			}
			
			// Parse out class
			if ( selector.startsWith(".") ) {
				selector = selector.substring(1);
				type = StyleSelectorType.CLASS;
			} else if ( selector.startsWith("#") ) {
				selector = selector.substring(1);
				type = StyleSelectorType.ID;
			} else {
				type = StyleSelectorType.TAG;
			}
				
			// Set selector
			this.selector = selector;
		}
		
		public PseudoClass getModifier() {
			return this.modifier;
		}

		public StyleSelectorType getType() {
			return this.type;
		}

		public String getSelector() {
			return this.selector;
		}

		@Override
		public String toString() {
			return getSelector() + "["+type+"]";
		}
		
		public int hashCode() {
			return selector.hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			if ( !(o instanceof StyleSelector) )
				return false;
			
			StyleSelector s = (StyleSelector)o;
			if ( !s.selector.equals(selector) )
				return false;
			
			return true;
		}
	}

	enum StyleSelectorType {
		TAG, CLASS, ID;
	}

	/**
	 * Check all listed styles (in reverse order) and check if any parent node styles in the stylesheets contain the specified style operation.
	 * @param currentStyling
	 * @param fONT_SIZE
	 */
	public static void findAndApplyStyle(List<Stylesheet> sheets, Node applyNode, Node parentNode, StyleOperation... operations) {
		if ( parentNode == null )
			return;
		
		if ( operations == null || operations.length == 0 )
			return;
		
		// System.out.println("("+sheets.size()+") Searching node " + parentNode + "("+parentNode.getClassList()+") / for operation: " + Arrays.toString(operations) + " \t\t" + applyNode + "\t" + parentNode.getParent());
		Map<String, StyleOperationValue> declarations = new WeakHashMap<>();
		
		for (int i = 0; i<sheets.size(); i++) {
			Stylesheet sheet = sheets.get(i);
			
			// Apply styling for the DOM TAG
			sheet.computeStyling(parentNode, StyleSelectorType.TAG, parentNode.getElementType(), declarations, false);
			sheet.computeStyling(parentNode, StyleSelectorType.ID, parentNode.getElementId(), declarations, false);
			
			sheet.computeStyling(parentNode, StyleSelectorType.TAG, parentNode.getElementType(), declarations, true);
			sheet.computeStyling(parentNode, StyleSelectorType.ID, parentNode.getElementId(), declarations, true);
			
			// Apply styling for the class
			ArrayList<String> classList = parentNode.getClassList();
			for (int j = 0; j < classList.size(); j++) {
				String claz = classList.get(j);
				sheet.computeStyling(parentNode, StyleSelectorType.CLASS, claz, declarations, false);
				sheet.computeStyling(parentNode, StyleSelectorType.CLASS, claz, declarations, true);
			}
		}

		// Apply style, otherwise check parent (recursive?)
		int t = operations.length;
		for (int i = 0; i < operations.length; i++) {
			if ( declarations.containsKey(operations[i].getName()) ) {
				declarations.get(operations[i].getName()).process(applyNode);
				t--;
				operations[i] = null;
			}
		}
		
		// Continue if we still have operations to find!
		if ( t > 0 ) {
			int a = 0;
			StyleOperation[] ops = new StyleOperation[t];
			for (int i = 0; i < operations.length; i++) {
				if ( operations[i] != null )
					ops[a++] = operations[i];
			}
			findAndApplyStyle(sheets, applyNode, parentNode.getParent(), ops);
		}
	}
}

abstract class DataCallback<T, E> {
	public abstract T callback(E object);
}

/**
 * Class used to define a function name and which args were passed to it through user-supplied CSS.
 * Can be an argument when checking StyleVarArgs in StyleOperations
 * @author Andrew
 *
 */
class StyleFunction {
	protected StyleVarArgs args;
	private String name;
	
	public StyleFunction(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public StyleVarArgs getArgs() {
		return this.args;
	}
	
	@Override
	public String toString() {
		return name + "(" + args + ")";
	}
}

/**
 * Class used to define each grouping of parameters that represent the values for a css property. Most properties will have 1 Var Arg with multiple params.<br>
 * i.e.<br>
 * padding: 16px 24px						<b>--> This has 1 Var Arg, with 2 params.<br></b>
 * box-shadow: 0px 0px, 32px 32px 32px red	<b>--> This has 2 var args. The first has 2 params, the second has 4 params.</b>
 * @author Andrew
 *
 */
class StyleVarArgs {
	private List<StyleParams> params = new ArrayList<>();
	
	public StyleVarArgs(StyleParams...params) {
		for (int i = 0; i < params.length; i++) {
			this.params.add(params[i]);
		}
	}
	
	public void add(StyleVarArgs val) {
		for (int i = 0; i < val.size(); i++) {
			this.add(val.get(i));
		}
	}

	public void add(StyleParams styleParams) {
		params.add(styleParams);
	}

	public int size() {
		return params.size();
	}
	
	public StyleParams get(int index) {
		if ( index >= params.size() || index < 0 )
			return null;
		return params.get(index);
	}
	
	@Override
	public String toString() {
		return Arrays.toString(params.toArray(new Object[params.size()]));
	}
}

/**
 * Class used to represent parameters in a argument.
 * @author Andrew
 *
 */
class StyleParams {
	private List<Object> values = new ArrayList<Object>();
	
	public StyleParams(Object...objects) {
		for (int i = 0; i < objects.length; i++) {
			values.add(objects[i]);
		}
	}
	
	public int size() {
		return values.size();
	}
	
	public Object get(int index) {
		return values.get(index);
	}
	
	@Override
	public String toString() {
		return Arrays.toString(values.toArray(new Object[values.size()]));
	}
}