public class Config {
    public String csvPath = "Residential_Energy_Dataset_UK- 2014-2020.csv";
    public int nPreviousDays = 7;
    public int mPreviousValues = 8;
    public int populationSize = 250;
    public int generations = 100;
    public int genotypeLength = 120;
    public int tournamentSize = 5;
    public int maxDepth = 8;
    public double crossoverRate = 0.85;
    public double baseMutationRate = 0.04;
    public double trainRatio = 0.70;
    public long seed = 42;
    public double hitBound = 0.005;
    public double elitismFraction = 0.02;
    public double injectionFraction = 0.10;
    public double noveltyWeight = 0.18;          // higher = stronger preference for structurally novel trees
    public double convergenceThreshold = 0.22;   // if population diversity drops below this, mutation/injection increases
    public int runs = 20;

    public static Config fromArgs(String[] args) {
        Config c = new Config();
        for (String arg : args) {
            String[] p = arg.split("=", 2);
            if (p.length != 2) continue;
            switch (p[0]) {
                case "--csv" -> c.csvPath = p[1];
                case "--nDays" -> c.nPreviousDays = Integer.parseInt(p[1]);
                case "--mValues" -> c.mPreviousValues = Integer.parseInt(p[1]);
                case "--pop" -> c.populationSize = Integer.parseInt(p[1]);
                case "--gens" -> c.generations = Integer.parseInt(p[1]);
                case "--depth" -> c.maxDepth = Integer.parseInt(p[1]);
                case "--seed" -> c.seed = Long.parseLong(p[1]);
                case "--novelty" -> c.noveltyWeight = Double.parseDouble(p[1]);
            }
        }
        return c;
    }
}
