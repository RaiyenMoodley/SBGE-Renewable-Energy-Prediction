import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class StructureBasedGE {
    private final Config cfg;
    private final Random rng;
    private final Mapper mapper;

    public StructureBasedGE(Config cfg, int featureCount) {
        this.cfg = cfg;
        this.rng = new Random(cfg.seed);
        this.mapper = new Mapper(featureCount, cfg.maxDepth);
    }

    public Individual run(double[][] trainX, double[] trainY, double[][] testX, double[] testY, PrintWriter genWriter) {
        List<Individual> pop = initialPopulation();
        evaluate(pop, trainX, trainY, testX, testY);
        Individual best = best(pop).copy();

        for (int gen = 1; gen <= cfg.generations; gen++) {
            double diversity = annotateStructuralNovelty(pop);
            boolean converged = diversity < cfg.convergenceThreshold;
            double mutationRate = converged ? cfg.baseMutationRate * 3.0 : cfg.baseMutationRate;

            List<Individual> next = new ArrayList<>();
            pop.sort(Comparator.comparingDouble(i -> i.trainRmse));
            int elites = Math.max(1, (int)Math.round(cfg.populationSize * cfg.elitismFraction));
            for (int i = 0; i < elites; i++) next.add(pop.get(i).copy());

            while (next.size() < cfg.populationSize) {
                Individual p1 = guidedTournament(pop);
                Individual p2 = structurallyComplementaryParent(pop, p1);
                Individual[] children = crossover(p1, p2);
                mutate(children[0], mutationRate);
                mutate(children[1], mutationRate);
                next.add(children[0]);
                if (next.size() < cfg.populationSize) next.add(children[1]);
            }

            if (converged) injectDiverseRandoms(next, (int)(cfg.populationSize * cfg.injectionFraction));
            pop = next;
            evaluate(pop, trainX, trainY, testX, testY);
            double avgFitness = pop.stream().mapToDouble(i -> i.trainRmse).average().orElse(0);
            double avgHits = pop.stream().mapToInt(i -> i.trainHits).average().orElse(0);
            int bestHits = pop.stream().mapToInt(i -> i.trainHits).max().orElse(0);

            int bestComplexity = pop.stream().mapToInt(i -> i.tree.countNodes()).min().orElse(0);
            double avgComplexity = pop.stream().mapToInt(i -> i.tree.countNodes()).average().orElse(0);

            long uniqueIndividuals = pop.stream().map(i -> i.tree.toInfix()).distinct().count();

            double variety = 100.0 * uniqueIndividuals / pop.size();

            Individual genBest = best(pop);
            if (genBest.trainRmse < best.trainRmse) best = genBest.copy();

            genWriter.printf(Locale.US,
                    "%d,%d,%.6f,%.6f,%d,%.2f,%d,%.2f,%.2f,%.3f,%.3f%n",
                    cfg.seed,
                    gen,
                    genBest.trainRmse,
                    avgFitness,
                    bestHits,
                    avgHits,
                    bestComplexity,
                    avgComplexity,
                    variety,
                    diversity,
                    mutationRate
            );

            if (gen % 10 == 0 || gen == 1) {
                System.out.printf(Locale.US,
                        "Gen %3d | BestFitness %.6f | AvgFitness %.6f | BestHits %d | AvgHits %.2f | BestComplexity %d | AvgComplexity %.2f | Variety %.2f%% | StructuralDiversity %.3f | MutationRate %.3f%n",
                        gen,
                        genBest.trainRmse,
                        avgFitness,
                        bestHits,
                        avgHits,
                        bestComplexity,
                        avgComplexity,
                        variety,
                        diversity,
                        mutationRate
                );
            }
        }
        return best;
    }

    private List<Individual> initialPopulation() {
        List<Individual> pop = new ArrayList<>();
        for (int i = 0; i < cfg.populationSize; i++) pop.add(randomIndividual());
        return pop;
    }

    private Individual randomIndividual() {
        int[] g = new int[cfg.genotypeLength];
        for (int i = 0; i < g.length; i++) g[i] = rng.nextInt(10_000);
        return new Individual(g);
    }

    private void evaluate(List<Individual> pop, double[][] trainX, double[] trainY, double[][] testX, double[] testY) {
        for (Individual ind : pop) {
            ind.tree = mapper.map(ind.genome);
            ind.trainRmse = rmse(ind.tree, trainX, trainY);
            ind.testRmse = rmse(ind.tree, testX, testY);
            ind.trainHits = hits(ind.tree, trainX, trainY);
            ind.testHits = hits(ind.tree, testX, testY);
        }
        annotateStructuralNovelty(pop);
    }

    private double rmse(Node tree, double[][] X, double[] y) {
        double sum = 0.0;
        for (int i = 0; i < y.length; i++) {
            double pred = tree.eval(X[i]);
            if (!Double.isFinite(pred)) pred = 0.0;
            pred = Math.max(-10, Math.min(10, pred));
            double e = pred - y[i];
            sum += e * e;
        }
        return Math.sqrt(sum / y.length);
    }

    private int hits(Node tree, double[][] X, double[] y) {
        int count = 0;
        for (int i = 0; i < y.length; i++) {
            double pred = tree.eval(X[i]);
            if (!Double.isFinite(pred)) pred = 0.0;
            pred = Math.max(-10, Math.min(10, pred));

            if (Math.abs(pred - y[i]) <= cfg.hitBound) {
                count++;
            }
        }
        return count;
    }

    private double annotateStructuralNovelty(List<Individual> pop) {
        Map<String, Integer> counts = new HashMap<>();
        for (Individual ind : pop) counts.merge(ind.tree.signature(), 1, Integer::sum);
        int unique = counts.size();
        for (Individual ind : pop) {
            int freq = counts.get(ind.tree.signature());
            double rarity = 1.0 - ((double)(freq - 1) / Math.max(1, pop.size() - 1));
            double sizePenalty = Math.min(1.0, ind.tree.countNodes() / 120.0);
            ind.novelty = 0.85 * rarity + 0.15 * (1.0 - sizePenalty);
            ind.guidedScore = ind.trainRmse - cfg.noveltyWeight * ind.novelty * ind.trainRmse;
        }
        return (double) unique / pop.size();
    }

    private Individual guidedTournament(List<Individual> pop) {
        Individual best = null;
        for (int i = 0; i < cfg.tournamentSize; i++) {
            Individual cand = pop.get(rng.nextInt(pop.size()));
            if (best == null || cand.guidedScore < best.guidedScore) best = cand;
        }
        return best;
    }

    private Individual structurallyComplementaryParent(List<Individual> pop, Individual p1) {
        Individual best = null;
        double bestScore = Double.POSITIVE_INFINITY;
        for (int i = 0; i < cfg.tournamentSize * 2; i++) {
            Individual cand = pop.get(rng.nextInt(pop.size()));
            double dist = signatureDistance(p1.tree.signature(), cand.tree.signature());
            double targetDistance = 0.45;
            double score = cand.trainRmse + Math.abs(dist - targetDistance) * cand.trainRmse;
            if (best == null || score < bestScore) { best = cand; bestScore = score; }
        }
        return best;
    }

    private double signatureDistance(String a, String b) {
        int max = Math.max(a.length(), b.length());
        if (max == 0) return 0;
        int min = Math.min(a.length(), b.length());
        int diff = max - min;
        for (int i = 0; i < min; i++) if (a.charAt(i) != b.charAt(i)) diff++;
        return (double) diff / max;
    }

    private Individual[] crossover(Individual a, Individual b) {
        int[] g1 = Arrays.copyOf(a.genome, a.genome.length);
        int[] g2 = Arrays.copyOf(b.genome, b.genome.length);
        if (rng.nextDouble() < cfg.crossoverRate) {
            int p = 1 + rng.nextInt(g1.length - 2);
            for (int i = p; i < g1.length; i++) {
                int tmp = g1[i]; g1[i] = g2[i]; g2[i] = tmp;
            }
        }
        return new Individual[]{ new Individual(g1), new Individual(g2) };
    }

    private void mutate(Individual ind, double rate) {
        for (int i = 0; i < ind.genome.length; i++) {
            if (rng.nextDouble() < rate) {
                if (rng.nextBoolean()) ind.genome[i] = rng.nextInt(10_000);
                else ind.genome[i] = Math.max(0, ind.genome[i] + rng.nextInt(401) - 200);
            }
        }
    }

    private void injectDiverseRandoms(List<Individual> pop, int count) {
        pop.sort(Comparator.comparingDouble(i -> i.guidedScore));
        for (int i = 0; i < count && i < pop.size(); i++) {
            int replaceIndex = pop.size() - 1 - i;
            pop.set(replaceIndex, randomIndividual());
        }
    }

    private Individual best(List<Individual> pop) {
        return pop.stream().min(Comparator.comparingDouble(i -> i.trainRmse)).orElseThrow();
    }
}
