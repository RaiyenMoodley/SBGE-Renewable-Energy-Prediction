public class UnaryNode implements Node {
    public final String op;
    public final Node child;
    public UnaryNode(String op, Node child) { this.op = op; this.child = child; }
    public double eval(double[] x) {
        double v = child.eval(x);
        if (!Double.isFinite(v)) return 0.0;
        return switch (op) {
            case "sin" -> Math.sin(v);
            case "cos" -> Math.cos(v);
            case "abs" -> Math.abs(v);
            case "log" -> Math.log(Math.abs(v) + 1e-6);
            default -> v;
        };
    }
    public String toInfix() { return op + "(" + child.toInfix() + ")"; }
    public String signature() { return op + "(" + child.signature() + ")"; }
    public int countNodes() { return 1 + child.countNodes(); }
    public int depth() { return 1 + child.depth(); }
}
