import java.nio.file.Path;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        Config cfg = Config.fromArgs(args);
        System.out.println("Loading: " + cfg.csvPath);
        TimeSeriesDataset ds = TimeSeriesDataset.fromCsv(Path.of(cfg.csvPath), cfg.mPreviousValues, cfg.nPreviousDays);
        ds.shuffleAndSplit(cfg.trainRatio, cfg.seed);
        System.out.printf("Examples: train=%d test=%d features=%d%n", ds.trainX.length, ds.testX.length, ds.featureNames.length);
        System.out.println("Features: " + String.join(", ", ds.featureNames));

        StructureBasedGE ge = new StructureBasedGE(cfg, ds.featureNames.length);
        Individual best = ge.run(ds.trainX, ds.trainY, ds.testX, ds.testY);

        System.out.println("\nBest expression:");
        System.out.println(best.tree.toInfix());
        System.out.printf(Locale.US, "Train RMSE: %.6f%n", best.trainRmse);
        System.out.printf(Locale.US, "Test RMSE : %.6f%n", best.testRmse);
        System.out.printf(Locale.US, "Tree nodes: %d depth: %d signature: %s%n", best.tree.countNodes(), best.tree.depth(), best.tree.signature());
    }
}
