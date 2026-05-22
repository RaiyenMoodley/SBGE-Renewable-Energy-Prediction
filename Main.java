import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.*;


public class Main {
    public static void main(String[] args) throws Exception {
        Random  random = new Random();
        Config cfg = new Config();
        System.out.println("Loading: " + cfg.csvPath);
        TimeSeriesDataset ds = TimeSeriesDataset.fromCsv(Path.of(cfg.csvPath), cfg.mPreviousValues, cfg.nPreviousDays);
        PrintWriter runWriter = new PrintWriter(new FileWriter("run_metrics.csv"));
        runWriter.println("Run,Seed,EndRunBestFitness,EndRunBestSolution,TreeDepth,TimeTakenMs,TestRMSE");
        PrintWriter genWriter = new PrintWriter(new FileWriter("generation_metrics.csv"));
        genWriter.println("Seed,Generation,BestFitness,AvgFitness,BestHits,AvgHits,BestComplexity,AvgComplexity,Variety,StructuralDiversity,MutationRate");
        for (int r = 1; r < cfg.runs + 1; r++) {
            System.out.println("Run: " + r);
            cfg.seed = random.nextLong();

            long startTime = System.currentTimeMillis();

            ds.shuffleAndSplit(cfg.trainRatio, cfg.seed);
            StructureBasedGE ge = new StructureBasedGE(cfg, ds.featureNames.length);
            Individual best = ge.run(ds.trainX, ds.trainY, ds.testX, ds.testY, genWriter);

            long endTime = System.currentTimeMillis();
            long timeTaken = endTime - startTime;

            runWriter.printf(Locale.US,
                    "%d,%d,%.6f,\"%s\",%d,%d,%.6f%n",
                    r,
                    cfg.seed,
                    best.trainRmse,
                    best.tree.toInfix(),
                    best.tree.depth(),
                    timeTaken,
                    best.testRmse
            );

            System.out.printf(Locale.US,
                    "Run %d | Seed %d | EndRunBestFitness %.6f | EndRunBestSolution %s | EndRunDepth %d | TimeTakenMs %d | TestRMSE %.6f%n",
                    r,
                    cfg.seed,
                    best.trainRmse,
                    best.tree.toInfix(),
                    best.tree.depth(),
                    timeTaken,
                    best.testRmse
            );
        }
        runWriter.close();
        genWriter.close();
    }
}
