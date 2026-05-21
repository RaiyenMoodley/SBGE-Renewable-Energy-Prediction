import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TimeSeriesDataset {
    public double[][] trainX, testX;
    public double[] trainY, testY;
    public String[] featureNames;

    private double[][] allX;
    private double[] allY;

    public static TimeSeriesDataset fromCsv(Path path, int mPreviousValues, int nPreviousDays) throws IOException {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        List<LocalDateTime> times = new ArrayList<>();
        List<Double> load = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(path)) {
            String header = br.readLine();
            if (header == null) throw new IllegalArgumentException("CSV is empty");
            String[] cols = header.split(",");
            int timeCol = indexOf(cols, "utc_timestamp");
            int loadCol = indexOf(cols, "Electricity_load");
            if (timeCol < 0 || loadCol < 0) throw new IllegalArgumentException("CSV must contain utc_timestamp and Electricity_load");
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split(",", -1);
                try {
                    times.add(LocalDateTime.parse(parts[timeCol].trim(), fmt));
                    load.add(Double.parseDouble(parts[loadCol].trim()));
                } catch (Exception ignored) { }
            }
        }

        int stepsPerDay = inferStepsPerDay(times);
        int start = Math.max(mPreviousValues, nPreviousDays * stepsPerDay);
        int featureCount = mPreviousValues + nPreviousDays;
        List<double[]> X = new ArrayList<>();
        List<Double> y = new ArrayList<>();

        for (int i = start; i < load.size(); i++) {
            double[] row = new double[featureCount];
            int c = 0;
            for (int lag = 1; lag <= mPreviousValues; lag++) row[c++] = load.get(i - lag);
            for (int day = 1; day <= nPreviousDays; day++) row[c++] = load.get(i - day * stepsPerDay);
            X.add(row);
            y.add(load.get(i));
        }

        TimeSeriesDataset ds = new TimeSeriesDataset();
        ds.allX = X.toArray(new double[0][]);
        ds.allY = y.stream().mapToDouble(Double::doubleValue).toArray();
        ds.featureNames = buildFeatureNames(mPreviousValues, nPreviousDays);
        return ds;
    }

    public void shuffleAndSplit(double trainRatio, long seed) {
        int n = allY.length;
        Integer[] idx = new Integer[n];
        for (int i = 0; i < n; i++) idx[i] = i;
        Collections.shuffle(Arrays.asList(idx), new Random(seed));
        int split = (int)Math.round(n * trainRatio);
        trainX = new double[split][]; trainY = new double[split];
        testX = new double[n - split][]; testY = new double[n - split];
        for (int i = 0; i < n; i++) {
            if (i < split) { trainX[i] = allX[idx[i]]; trainY[i] = allY[idx[i]]; }
            else { int j = i - split; testX[j] = allX[idx[i]]; testY[j] = allY[idx[i]]; }
        }
    }

    private static int indexOf(String[] cols, String target) {
        for (int i = 0; i < cols.length; i++) if (cols[i].trim().equalsIgnoreCase(target)) return i;
        return -1;
    }

    private static int inferStepsPerDay(List<LocalDateTime> times) {
        if (times.size() < 2) return 96;
        long minutes = Duration.between(times.get(0), times.get(1)).abs().toMinutes();
        if (minutes <= 0) return 96;
        return (int)(24 * 60 / minutes);
    }

    private static String[] buildFeatureNames(int m, int n) {
        String[] names = new String[m+n];
        int c = 0;
        for (int i = 1; i <= m; i++) names[c++] = "x_lag_" + i;
        for (int i = 1; i <= n; i++) names[c++] = "x_day_" + i;
        return names;
    }
}
