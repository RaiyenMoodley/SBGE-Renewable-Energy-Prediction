public class BinaryNode implements Node {
    public final String op;
    public final Node left, right;
    public BinaryNode(String op, Node left, Node right) { this.op = op; this.left = left; this.right = right; }
    public double eval(double[] x) {
        double a = left.eval(x), b = right.eval(x);
        if (!Double.isFinite(a) || !Double.isFinite(b)) return 0.0;
        return switch (op) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> Math.abs(b) < 1e-6 ? a : a / b;
            default -> 0.0;
        };
    }
    public String toInfix() { return "(" + left.toInfix() + " " + op + " " + right.toInfix() + ")"; }
    public String signature() { return op + "(" + left.signature() + "," + right.signature() + ")"; }
    public int countNodes() { return 1 + left.countNodes() + right.countNodes(); }
    public int depth() { return 1 + Math.max(left.depth(), right.depth()); }
}
