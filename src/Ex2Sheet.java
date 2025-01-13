import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.util.Set;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;

public class Ex2Sheet implements Sheet {
    private Cell[][] table;

    public Ex2Sheet(int x, int y) {
        table= new SCell[x][y];
        for(int i=0; i<x; i++) {
            for(int j=0; j<y; j++) {
                table[i][j]= new SCell("");
            }
        }
        eval();
    }

    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    @Override
    public String value(int x, int y) {
        if(!isIn(x, y)) {
            return Ex2Utils.EMPTY_CELL;
        }
        Cell cell = get(x, y);
        if(cell==null) {
            return Ex2Utils.EMPTY_CELL;
        }
        return eval(x, y);
    }

    @Override
    public int width() {
        return table.length;
    }

    @Override
    public int height() {
        return table[0].length;
    }

    @Override
    public void set(int x, int y, String s) {
        if(!isIn(x, y)) {
            throw new IllegalArgumentException("Coordinates out of sheet bounds");
        }
        table[x][y]= new SCell(s==null ? "" : s);
        eval();
    }

    @Override
    public void eval() {
        int[][] depths = depth();
        for(int i=0; i<width(); i++) {
            for(int j=0; j<height(); j++) {
                if(depths[i][j]== Ex2Utils.ERR_CYCLE_FORM) {
                    get(i, j).setType(Ex2Utils.ERR_CYCLE_FORM);
                }
            }
        }
    }

    @Override
    public boolean isIn(int x, int y) {
        return x>=0 && y>=0 && x<width() && y<height();
    }

    @Override
    public int[][] depth() {
        int[][] depths= new int[width()][height()];
        for(int i=0; i<width(); i++) {
            for(int j=0; j<height(); j++) {
                depths[i][j]= calculateCellDepth(i, j, new HashSet<>());
            }
        }
        return depths;
    }

    private int calculateCellDepth(int x, int y, Set<String> visited) {
        if (!isIn(x, y)) {
            return 0;
        }
        Cell cell= get(x, y);
        if(cell==null || cell.getData()==null || cell.getData().isEmpty() || (cell.getType()!=Ex2Utils.FORM)) {
            return 0;
        }
        String cellId= x+ ","+ y;
        if(visited.contains(cellId)) {
            return Ex2Utils.ERR_CYCLE_FORM;
        }
        visited.add(cellId);
        String data= cell.getData();
        Pattern pattern= Pattern.compile("[A-Za-z][0-9]+");
        Matcher matcher= pattern.matcher(data);
        int maxDepth= 0;
        while(matcher.find()) {
            String cellRef= matcher.group();
            int column= Character.toUpperCase(cellRef.charAt(0)) - 'A';
            int row= Integer.parseInt(cellRef.substring(1));
            int refDepth= calculateCellDepth(column, row, visited);
            if(refDepth==Ex2Utils.ERR_CYCLE_FORM) {
                visited.remove(cellId);
                return Ex2Utils.ERR_CYCLE_FORM;
            }
            maxDepth= Math.max(maxDepth, refDepth);
        }
        visited.remove(cellId);
        return maxDepth + 1;
    }

    @Override
    public void load(String fileName) throws IOException {
        try (BufferedReader reader= new BufferedReader(new FileReader(fileName))) {
            String line;
            int row= 0;
            while((line=reader.readLine())!=null &&row<height()) {
                String[] cells= line.split(",");
                for(int col=0; col<Math.min(cells.length, width()); col++) {
                    set(col, row, cells[col]);
                }
                row++;
            }
        }
        eval();
    }

    @Override
    public void save(String fileName) throws IOException {
        try (PrintWriter writer = new PrintWriter(fileName)) {
            for(int y=0; y<height(); y++) {
                StringBuilder line= new StringBuilder();
                for(int x=0; x<width(); x++) {
                    if(x>0) {
                        line.append(",");
                    }
                    Cell cell= get(x, y);
                    line.append(cell!=null ? cell.getData() :"");
                }
                writer.println(line);
            }
        }
    }

    @Override
    public String eval(int x, int y) {
        if(!isIn(x, y)) {
            return Ex2Utils.ERR_FORM;
        }
        Cell cell= get(x, y);
        if(cell==null || cell.getData().isEmpty()) {
            return Ex2Utils.EMPTY_CELL;
        }
        int cellType= cell.getType();
        String cellData= cell.getData();
        if(cellType==Ex2Utils.NUMBER) {
            return cellData;
        }
        if(cellType==Ex2Utils.TEXT) {
            return cellData;
        }
        if(cellType==Ex2Utils.FORM) {
            return evaluateFormula(x, y, new HashSet<>());
        }
        return Ex2Utils.ERR_FORM;
    }

    private String evaluateFormula(int x, int y, Set<String> visited) {
        Cell cell = get(x, y);
        String cellId = x + "," + y;

        // בדיקת מעגליות - אם כבר ביקרנו בתא הזה
        if(visited.contains(cellId)) {
            cell.setType(Ex2Utils.ERR_CYCLE_FORM); // עדכון סוג התא
            return Ex2Utils.ERR_CYCLE; // החזרת השגיאה המתאימה
        }

        visited.add(cellId);
        String formula = cell.getData().substring(1); // Remove the '='
        Pattern pattern = Pattern.compile("[A-Za-z][0-9]+");
        Matcher matcher = pattern.matcher(formula);
        StringBuffer result = new StringBuffer();

        while(matcher.find()) {
            String cellRef = matcher.group();
            CellEntry refEntry = CellEntry.parseCellReference(cellRef);
            if(refEntry == null || !isIn(refEntry.getX(), refEntry.getY())) {
                matcher.appendReplacement(result, Ex2Utils.ERR_FORM);
                continue;
            }

            String refValue = eval(refEntry.getX(), refEntry.getY());
            if(refValue.equals(Ex2Utils.ERR_CYCLE)) {
                cell.setType(Ex2Utils.ERR_CYCLE_FORM);
                return Ex2Utils.ERR_CYCLE;
            }

            if(refValue.equals(Ex2Utils.ERR_FORM)) {
                matcher.appendReplacement(result, refValue);
                continue;
            }

            matcher.appendReplacement(result, refValue);
        }

        matcher.appendTail(result);
        visited.remove(cellId);

        try {
            double computedResult = SCell.computForm("=" + result.toString());
            return String.valueOf(computedResult);
        } catch (Exception e) {
            return Ex2Utils.ERR_FORM;
        }
    }

    @Override
    public Cell get(int x, int y) {
        return isIn(x, y) ? table[x][y] : null;
    }

    @Override
    public Cell get(String coords) {
        if(coords==null || coords.length() < 2) {
            return null;
        }
        try {
            char colChar= Character.toUpperCase(coords.charAt(0));
            int x= colChar - 'A';
            int y= Integer.parseInt(coords.substring(1));

            return isIn(x, y) ? table[x][y] : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean hasCyclicReference(int x, int y, Set<String> visited) {
        String cellId = x + "," + y;
        if (visited.contains(cellId)) {
            return true;
        }
        visited.add(cellId);
        return false;
    }

    public static final String ERR_INVALID_CELL = "#INVALID_CELL";

    public static final String ERR_MATH = "#MATH_ERROR";
}