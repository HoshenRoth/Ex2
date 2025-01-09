public class CellEntry implements Index2D {
    private final int x;
    private final int y;

    public CellEntry(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static CellEntry parseCellReference(String ref) {
        if (ref == null || ref.length() < 2) {
            return null;
        }
        try {
            char columnChar = Character.toUpperCase(ref.charAt(0));
            if (columnChar < 'A' || columnChar > 'Z') {
                return null;
            }
            int x = columnChar - 'A';
            int y = Integer.parseInt(ref.substring(1));
            if (y < 0 || y >= Ex2Utils.HEIGHT) {
                return null;
            }
            if (x < 0 || x >= Ex2Utils.WIDTH) {
                return null;
            }
            return new CellEntry(x, y);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String toCellReference(int x, int y) {
        if (!isValidPosition(x, y)) {
            return null;
        }
        char column = (char) ('A' + x);
        return column + String.valueOf(y);
    }

    private static boolean isValidPosition(int x, int y) {
        return x >= 0 && x < Ex2Utils.WIDTH && y >= 0 && y < Ex2Utils.HEIGHT;
    }

    @Override
    public boolean isValid() {
        return isValidPosition(x, y);
    }

    @Override
    public int getX() {
        return isValid() ? x : Ex2Utils.ERR;
    }

    @Override
    public int getY() {
        return isValid() ? y : Ex2Utils.ERR;
    }

    @Override
    public String toString() {
        return isValid() ? toCellReference(x, y) : "";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CellEntry)) return false;
        CellEntry other = (CellEntry) obj;
        return x == other.x && y == other.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }
}