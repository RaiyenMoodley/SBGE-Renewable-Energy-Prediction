public class Mapper {
    private final int featureCount;
    private final int maxDepth;
    private int pos;

    public Mapper(int featureCount, int maxDepth) {
        this.featureCount = featureCount;
        this.maxDepth = maxDepth;
    }

    public Node map(int[] codons) {
        pos = 0;
        return expr(codons, 0);
    }

    private Node expr(int[] c, int depth) {
        int codon = next(c);
        if (depth >= maxDepth) return terminal(codon);
        int choice = Math.floorMod(codon, 9);
        return switch (choice) {
            case 0 -> new BinaryNode("+", expr(c, depth+1), expr(c, depth+1));
            case 1 -> new BinaryNode("-", expr(c, depth+1), expr(c, depth+1));
            case 2 -> new BinaryNode("*", expr(c, depth+1), expr(c, depth+1));
            case 3 -> new BinaryNode("/", expr(c, depth+1), expr(c, depth+1));
            case 4 -> new UnaryNode("sin", expr(c, depth+1));
            case 5 -> new UnaryNode("cos", expr(c, depth+1));
            case 6 -> new UnaryNode("abs", expr(c, depth+1));
            case 7 -> new UnaryNode("log", expr(c, depth+1));
            default -> terminal(next(c));
        };
    }

    private Node terminal(int codon) {
        if (Math.floorMod(codon, 5) == 0) {
            double val = (Math.floorMod(codon, 2001) - 1000) / 1000.0;
            return new TerminalNode(val);
        }
        return new TerminalNode(Math.floorMod(codon, featureCount));
    }

    private int next(int[] c) { return c[(pos++) % c.length]; }
}
