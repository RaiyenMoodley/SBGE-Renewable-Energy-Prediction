import java.util.Locale;

public class TerminalNode implements Node {
    private final Integer varIndex;
    private final Double constant;

    public TerminalNode(int varIndex) { this.varIndex = varIndex; this.constant = null; }
    public TerminalNode(double constant) { this.varIndex = null; this.constant = constant; }

    public double eval(double[] x) { return varIndex == null ? constant : x[varIndex]; }
    public String toInfix() { return varIndex == null ? String.format(Locale.US, "%.4f", constant) : "x" + varIndex; }
    public String signature() { return varIndex == null ? "C" : "V"; }
    public int countNodes() { return 1; }
    public int depth() { return 1; }
}
