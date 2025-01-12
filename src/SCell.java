import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SCell implements Cell {
    private String data;
    private int type;
    private int order;

    public SCell(String s) {
        setData(s);
    }

    @Override
    public String toString() {
        return getData();
    }

    @Override
    public void setData(String s) {
        this.data= (s==null) ? "" : s;
        determineType();
    }

    @Override
    public String getData() {
        return data;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setType(int t) {
        type = t;
    }

    @Override
    public boolean setOrder(int t) {
        if(t<-1) {
            return false;
        }
        order=t;
        return true;
    }

    @Override
    public int getOrder() {
        if(type==Ex2Utils.TEXT || type==Ex2Utils.NUMBER) {
            return 0;
        }
        if(type==Ex2Utils.ERR || type==Ex2Utils.ERR_CYCLE_FORM) {
            return -1;
        }
        if(type!=Ex2Utils.FORM) {
            return 0;
        }
        List<String> dependencies= findCellReferences(data.substring(1));
        if(dependencies.isEmpty()) {
            return 1;
        }
        return order;
    }

    private void determineType() {
        if(data==null || data.isEmpty()) {
            setType(Ex2Utils.TEXT);
            return;
        }
        if(data.startsWith("=")) {
            if(isForm(data)) {
                setType(Ex2Utils.FORM);
            } else {
                setType(Ex2Utils.ERR_FORM_FORMAT);
            }
            return;
        }
        try {
            Double.parseDouble(data);
            setType(Ex2Utils.NUMBER);
        } catch (NumberFormatException e) {
            setType(Ex2Utils.TEXT);
        }
    }

    private List<String> findCellReferences(String formula) {
        List<String> references= new ArrayList<>();
        Pattern pattern= Pattern.compile("[A-Za-z]+[0-9]+");
        Matcher matcher= pattern.matcher(formula);
        while (matcher.find()) {
            references.add(matcher.group());
        }
        return references;
    }

    public static boolean isForm(String txt) {
        if(txt==null || !txt.startsWith("=") || txt.contains(" ")) {
            return false;
        }
        String formula= txt.substring(1);
        if(formula.isEmpty()) {
            return false;
        }
        if(formula.matches("[A-Za-z][0-9]+")) {
            return true;
        }
        if(formula.matches("-?\\d+(\\.\\d+)?")) {
            return true;
        }
        int parentheses= 0;
        boolean lastWasOperator= true;
        for(int i=0; i<formula.length(); i++) {
            char c= formula.charAt(i);
            if(c=='(') {
                parentheses++;
                lastWasOperator= true;
                continue;
            }
            if(c==')') {
                parentheses--;
                if(parentheses<0){
                    return false;
                }
                lastWasOperator = false;
                continue;
            }
            if(parentheses<0){
                return false;
            }
            if(isOperator(c)) {
                if(lastWasOperator){
                    return false;
                }
                lastWasOperator= true;
                continue;
            }
            if(!isValidChar(c)){
                return false;
            }
            lastWasOperator = false;
        }
        return parentheses==0 && !lastWasOperator;
    }

    private static boolean isOperator(char c) {
        return c=='+' || c=='-' || c=='*' || c=='/';
    }

    private static boolean isValidChar(char c) {
        return Character.isDigit(c) || isOperator(c) || c=='.' || c=='(' || c==')'
                || (c>='A' && c<='Z') || (c>='a' && c<='z');
    }

    public static double computForm(String text) {
        text= text.trim();
        if(text.startsWith("=")) {
            text= text.substring(1);
        }
        while(text.startsWith("(") && text.endsWith(")") && isBalanced(text.substring(1, text.length() - 1))) {
            text= text.substring(1, text.length() - 1);
        }
        if(!containsOperator(text)) {
            try{
                return Double.parseDouble(text.trim());
            } catch(NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number format: " + text);
            }
        }
        int opIndex= findLowestPrecedenceOperator(text);
        if(opIndex== -1) {
            throw new IllegalArgumentException("Invalid formula format");
        }
        char operator= text.charAt(opIndex);
        String leftPart= text.substring(0, opIndex).trim();
        String rightPart= text.substring(opIndex + 1).trim();
        double leftValue= computForm(leftPart);
        double rightValue= computForm(rightPart);
        return calculate(leftValue, rightValue, operator);
    }

    private static boolean containsOperator(String text) {
        for(int i=0; i<text.length(); i++) {
            if(isOperator(text.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static double calculate(double leftValue, double rightValue, char operator) {
        switch (operator) {
            case '+': return leftValue+rightValue;
            case '-': return leftValue-rightValue;
            case '*': return leftValue*rightValue;
            case '/':
                if (rightValue == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return leftValue / rightValue;
            default: throw new IllegalArgumentException("Unknown operator: " + operator);
        }
    }

    private static int findLowestPrecedenceOperator(String text) {
        int parentheses= 0;
        int addSubIndex= -1;
        int mulDivIndex= -1;
        for(int i=0; i<text.length(); i++) {
            char c= text.charAt(i);
            if(c=='(') {
                parentheses++;
            } else if (c==')') {
                parentheses--;
            } else if (parentheses== 0) {
                if(c=='+' || c=='-') {
                    addSubIndex= i;
                } else if ((c=='*' || c=='/') && addSubIndex== -1) {
                    mulDivIndex= i;
                }
            }
        }
        return addSubIndex != -1 ? addSubIndex : mulDivIndex;
    }

    private static boolean isBalanced(String text) {
        int balance= 0;
        for(char c: text.toCharArray()) {
            if(c=='(') {
                balance++;
            } else if (c==')') {
                balance--;
            }
            if(balance<0) {
                return false;
            }
        }
        return balance==0;
    }

    private boolean isValidNumber(String num) {
        try {
            Double.parseDouble(num);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}