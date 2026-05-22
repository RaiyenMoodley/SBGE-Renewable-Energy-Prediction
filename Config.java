public class Config {
    public String csvPath = "Residential_Energy_Dataset_UK- 2014-2020.csv";
    public int nPreviousDays = 7;
    public int mPreviousValues = 8;
    public int populationSize = 100;
    public int generations = 50;
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
    public double noveltyWeight = 0.18;
    public double convergenceThreshold = 0.22;
    public int runs = 20;

    public Config() {}
}
