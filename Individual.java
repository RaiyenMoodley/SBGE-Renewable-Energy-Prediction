import java.util.Arrays;

public class Individual {
    public int[] genome;
    public Node tree;
    public double trainRmse = Double.POSITIVE_INFINITY;
    public double testRmse = Double.POSITIVE_INFINITY;
    public double novelty = 0.0;
    public double guidedScore = Double.POSITIVE_INFINITY;
    public int trainHits = 0;
    public int testHits = 0;

    public Individual(int[] genome) { this.genome = genome; }
    public Individual copy() {
        Individual x = new Individual(Arrays.copyOf(genome, genome.length));
        x.tree = tree;
        x.trainRmse = trainRmse;
        x.testRmse = testRmse;
        x.novelty = novelty;
        x.guidedScore = guidedScore;
        x.trainHits = trainHits;
        x.testHits = testHits;
        return x;
    }
}
