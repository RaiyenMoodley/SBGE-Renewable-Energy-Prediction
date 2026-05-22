import java.nio.file.Path;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        Config cfg = Config.fromArgs(args);
        System.out.println("Loading: " + cfg.csvPath);
        TimeSeriesDataset ds = TimeSeriesDataset.fromCsv(Path.of(cfg.csvPath), cfg.mPreviousValues, cfg.nPreviousDays);
        for (int r = 0; r < cfg.runs; r++) {
            cfg.seed = 42 + r;

            long startTime = System.currentTimeMillis();

            ds.shuffleAndSplit(cfg.trainRatio, cfg.seed);
            StructureBasedGE ge = new StructureBasedGE(cfg, ds.featureNames.length);
            Individual best = ge.run(ds.trainX, ds.trainY, ds.testX, ds.testY);

            long endTime = System.currentTimeMillis();
            long timeTaken = endTime - startTime;

            System.out.printf(Locale.US,
                    "Seed %d | EndRunBestFitness %.6f | EndRunBestSolution %s | EndRunDepth %d | TimeTakenMs %d | TestRMSE %.6f%n",
                    cfg.seed,
                    best.trainRmse,
                    best.tree.toInfix(),
                    best.tree.depth(),
                    timeTaken,
                    best.testRmse
            );
        }
    }
}
