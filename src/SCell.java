// Add your documentation below:

import java.util.ArrayList;

public class SCell implements Cell {
    private String line;
    private int type;
    private Ex2Sheet sheet;
    // Add your code here

    public SCell(String s) {
        // Add your code here
        setData(s);
    }

    public void setSeet(Ex2Sheet sheet){
        this.sheet= sheet;
    }

    @Override
    public int getOrder() {
        if (isNumber(getData())) {
            return 1;
        } else if (isForm(getData())) {
            return 2;
        }
        return 0;
    }

    @Override
    public String toString() {
        if(isForm(getData())){
            double value= computForm(getData());
            return String.valueOf(value);
        }
        return getData();
    }

    @Override
    public void setData(String s) {
        line = s;
        if(isForm(s)){
            setType(Ex2Utils.NUMBER);
        } else if (isForm(s)) {
            setType(Ex2Utils.FORM);
        } else {
            setType(Ex2Utils.TEXT);
        }
    }

    @Override
    public String getData() {
        return line;
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
    public void setOrder(int t) {
        // Add your code here

    }

    public boolean isNumber(String txt) {
        boolean input = false;
        try {
            Double.parseDouble(txt);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTxt(String text) {
        return !isNumber(text) && !isForm(text);
    }

    public static boolean isForm(String txt) {
        if (txt.contains(" ")) {
            return false;
        }
        if (!txt.startsWith("=")) {
            return false;
        }
        txt = txt.substring(1);
        int balance = 0;
        char lastChar = ' ';
        for (int i = 0; i < txt.length(); i++) {
            char c = txt.charAt(i);
            if (c == '(') {
                balance++;
            }
            if (c == ')') {
                balance--;
            }
            if (balance < 0) {
                return false;
            }
            if (isOperator(c)) {
                if (i == 0 || i == txt.length() - 1) {
                    return false;
                }
                if (isOperator(lastChar)) {
                    return false;
                }
            }
            if (!isValidChar(c)) {
                return false;
            }
            lastChar = c;
        }
        return balance == 0;
    }

    private static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private static boolean isValidChar(char c) {
        return Character.isDigit(c) || isOperator(c) || c == '.' || c == '(' || c == ')'
                || (c >= 'A' && c <= 'Z');
    }

    private static double evalCellCall(String cellCall){
        char columm= cellCall.charAt(0);
        int row= Integer.parseInt(cellCall.substring(1));
        int x= columm= 'A';
        Cell cell= Ex2Sheet.getStaticCell(x, row);
        if(cell!=null){
            if (cell.getType()==Ex2Utils.NUMBER){
                return Double.parseDouble(cell.getData());
            } else if (cell.getType()==Ex2Utils.FORM) {
                return computForm(cell.getData());
            }
        }
        return 0;
    }

    public static double computForm(String text) {
        if(!isForm(text)){
            return -1;
        }
        String formula= text.substring(1);
        if(formula.matches("[A-Z]\\d+")){
            return evalCellCall(formula);
        }
        computForm calculator= new computForm(formula);
        return calculator.calculate();
    }

   public static class computForm {
       private ArrayList<Double> priorties;
       private String formula;
       private final double MULTIPLY_PRIORITY = 0.1;
       private final double ADD_PRIORITY = 0.5;

       public computForm(String formula) {
           this.formula = formula;
           this.priorties = new ArrayList<Double>();
           mapPriorities();
       }

       private void mapPriorities() {
           for (int i = 0; i < formula.length(); i++) {
               char c = formula.charAt(i);
               if (Character.isDigit(c)) {
                   priorties.add(null);
               } else {
                   if (c == '+' || c == '-') {
                       priorties.add(ADD_PRIORITY + i);
                   } else {
                       if (c == '*' || c == '/') {
                           priorties.add(MULTIPLY_PRIORITY + i);
                       }
                   }
               }
           }
       }

       private int findNextOperation() {
           double minPriority = Double.MAX_VALUE;
           int minIndex = -1;
           for (int i = 0; i < priorties.size(); i++) {
               if (priorties.get(i) != null && priorties.get(i) < minPriority) {
                   minPriority = priorties.get(i);
                   minIndex = i;
               }
           }
           return minIndex;
       }

       public double calculate() {
           if (formula.length() == 1) {
               return Double.parseDouble(formula);
           }
           int opIndex = findNextOperation();
           if (opIndex == -1) {
               return Double.parseDouble(formula);
           }
           double result = getResult(opIndex);
           String newFornula = formula.substring(0, opIndex - 1) + result +
                   formula.substring(opIndex + 2);
           computForm newClac = new computForm(newFornula);
           return newClac.calculate();
       }

       private double getResult(int opIndex) {
           double num1 = Double.parseDouble(formula.substring(opIndex - 1, opIndex));
           double num2 = Double.parseDouble(formula.substring(opIndex + 1, opIndex + 2));
           double result = 0;
           char operator = formula.charAt(opIndex);
           if (operator == '+') {
               result = num1 + num2;
           } else if (operator == '-') {
               result = num1 - num2;
           } else if (operator == '*') {
               result = num1 * num2;
           } else if (operator == '/') {
               result = num1 / num2;
           }
           return result;
       }
   }
}


