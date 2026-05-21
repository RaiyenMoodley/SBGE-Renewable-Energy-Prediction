public interface Node {
    double eval(double[] x);
    String toInfix();
    String signature();
    int countNodes();
    int depth();
}
