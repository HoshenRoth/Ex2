import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class CellEntry {
    private int x;
    private int y;
    private boolean valid;

    public CellEntry(String entry) {
        parseEntry(entry);
    }

    private void parseEntry(String entry) {
        if (entry == null || entry.length() < 2) {
            valid = false;
            return;
        }

        Pattern pattern = Pattern.compile("^([A-Za-z]+)([0-9]+)$");
        Matcher matcher = pattern.matcher(entry);

        if (!matcher.matches()) {
            valid = false;
            return;
        }

        try {
            String columnPart = matcher.group(1).toUpperCase();
            String rowPart = matcher.group(2);

            x = convertColumnToIndex(columnPart);
            y = Integer.parseInt(rowPart) - 1; // Convert 1-based to 0-based index

            valid = x >= 0 && y >= 0;
        } catch (Exception e) {
            valid = false;
        }
    }

    private int convertColumnToIndex(String column) {
        int index = 0;
        for (char c : column.toCharArray()) {
            if (c < 'A' || c > 'Z') {
                throw new IllegalArgumentException("Invalid column character: " + c);
            }
            index = index * 26 + (c - 'A' + 1);
        }
        return index - 1; // Convert 1-based to 0-based index
    }

    public boolean isValid() {
        return valid;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        if (!valid) {
            return "Invalid Cell Entry";
        }

        StringBuilder columnPart = new StringBuilder();
        int tempX = x + 1; // Convert 0-based to 1-based index

        while (tempX > 0) {
            int remainder = (tempX - 1) % 26;
            columnPart.insert(0, (char) ('A' + remainder));
            tempX = (tempX - 1) / 26;
        }

        return columnPart.toString() + (y + 1); // Convert 0-based to 1-based index for row
    }

    public static boolean isValidEntry(String entry) {
        if (entry == null || entry.length() < 2) {
            return false;
        }

        Pattern pattern = Pattern.compile("^[A-Za-z]+[0-9]+$");
        return pattern.matcher(entry).matches();
    }
}
