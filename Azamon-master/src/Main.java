import aima.search.framework.Problem;
import aima.search.framework.Search;
import aima.search.framework.SearchAgent;
import aima.search.informed.HillClimbingSearch;
import aima.search.informed.SimulatedAnnealingSearch;
import aima.util.Pair;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import localsearchclasses.*;

public class Main {

    public static void main(String[] args) {
        int seed = -1;
        int npaq = 100;
        double proportion = 1.2;
        boolean runExperiments = false;
        int experimentSampleSize = 0;
        int iterations = -1;
        int stiter = -1;
        int k = -1;
        double lambda = -1;
        //Put default values here

        for (Iterator<String> it = Arrays.stream(args).iterator(); it.hasNext(); ) {
            String option = it.next();
            switch (option) {
                case "-s":
                    seed = Integer.parseInt(it.next());
                    break;
                case "-n":
                    npaq = Integer.parseInt(it.next());
                    break;
                case "-p":
                    proportion = Double.parseDouble(it.next());
                    break;
                case "-a": //All simulated annealing parameters
                    iterations = Integer.parseInt(it.next());
                    stiter = Integer.parseInt(it.next());
                    k = Integer.parseInt(it.next());
                    lambda = Double.parseDouble(it.next());
                    break;
                case "-e":
                    runExperiments = true;
                    experimentSampleSize = Integer.parseInt(it.next());
                    break;
                //Add more cases as we need them
                default:
                    throw new RuntimeException("Unexpected argument specifier found: " + option);

            }
        }

        //Run multiple experiments through the -e flag
        if (runExperiments) {
            ArrayList<ArrayList<String>> data = new ArrayList<>();
            //Creates header for CSV
            data.add(new ArrayList<>(Arrays.asList("iteration", "seed", "numPaquetes", "proportion",
                    "saIterations", "stiter", "k", "lambda", "generationTime",
                    "iniStorageCost", "iniSendCost", "iniTotalCost", "iniHappyness", "executionTime",
                    "finalStorageCost", "finalSendCost", "finalTotalCost", "finalHappyness")));

            for (int i = 0; i < experimentSampleSize; i++) {
                seed = generateSeed();
                System.out.println("Starting iteration " + i);
                System.out.println(seed);
                runExperiment(data, i, seed, npaq, proportion, 10000, 100, 1, 1);
                runExperiment(data, i, seed, npaq, proportion, 10000, 100, 5, 1);
                runExperiment(data, i, seed, npaq, proportion, 10000, 100, 25, 1);
                runExperiment(data, i, seed, npaq, proportion, 10000, 100, 125, 1);

                runExperiment(data, i, seed, npaq, proportion, 10000, 100, 1, 0.1);
                runExperiment(data, i, seed, npaq, proportion, 10000, 100, 5, 0.1);
                runExperiment(data, i, seed, npaq, proportion, 10000, 100, 25, 0.1);
                runExperiment(data, i, seed, npaq, proportion, 10000, 100, 125, 0.1);

                runExperiment(data, i, seed, npaq, proportion, 10000, 100, 1, 0.01);
                runExperiment(data, i, seed, npaq, proportion, 10000, 100, 5, 0.01);
                runExperiment(data, i, seed, npaq, proportion, 10000, 100, 25, 0.01);
                runExperiment(data, i, seed, npaq, proportion, 10000, 100, 125, 0.01);

                runExperiment(data, i, seed, npaq, proportion, 10000, 100, 1, 0.001);
                runExperiment(data, i, seed, npaq, proportion, 10000, 100, 5, 0.001);
                runExperiment(data, i, seed, npaq, proportion, 10000, 100, 25, 0.001);
                runExperiment(data, i, seed, npaq, proportion, 10000, 100, 125, 0.001);

                runExperiment(data, i, seed, npaq, proportion, 10000, 100, 1, 0.0001);
                runExperiment(data, i, seed, npaq, proportion, 10000, 100, 5, 0.0001);
                runExperiment(data, i, seed, npaq, proportion, 10000, 100, 25, 0.0001);
                runExperiment(data, i, seed, npaq, proportion, 10000, 100, 125, 0.0001);
            }
            experimentToCSV(data);
        } else { //Simple test
            if (seed == -1) {
                seed = generateSeed();
            }

            System.out.println("Seed used: " + seed); //For repeatability
            Company.init(seed, npaq, proportion);
            Pair companyAndTime = generateInitialSolution();
            Company company = (Company) companyAndTime.getFirst();
            runAlgorithm(new Company(company), false, iterations, stiter, k, lambda);
        }
        //runAlgorithm(company, false, iterations, stiter, k, lambda); TODO SA
    }

    private static void runExperiment(ArrayList<ArrayList<String>> data, int i, int seed, int npaq, double proportion, int iterations, int stiter, int k, double lambda) {
        ArrayList<String> experiment = new ArrayList<>();

        //Metadata
        experiment.add(String.valueOf(i));
        experiment.add(String.valueOf(seed));
        experiment.add(String.valueOf(npaq));
        experiment.add(String.valueOf(proportion));

        experiment.add(String.valueOf(iterations));
        experiment.add(String.valueOf(stiter));
        experiment.add(String.valueOf(k));
        experiment.add(String.valueOf(lambda));

        Company.init(seed, npaq, proportion);
        Pair companyAndInitialTime = generateInitialSolution();
        Company company = (Company) companyAndInitialTime.getFirst();

        //Data from initial solution
        experiment.add(String.valueOf((Double) companyAndInitialTime.getSecond()));
        experiment.add(String.valueOf(company.getStorageCost()));
        experiment.add(String.valueOf(company.getSendCost()));
        experiment.add(String.valueOf(company.getSendCost() + company.getStorageCost()));
        experiment.add(String.valueOf(company.getHappyness()));

        //Data from algorithm
        ArrayList<String> algorithmData = runAlgorithm(new Company(company), false, iterations, stiter, k, lambda);
        experiment.addAll(algorithmData);
        data.add(experiment);
    }

    private static void experimentToCSV(ArrayList<ArrayList<String>> data) {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        CsvWriter writer = new CsvWriter();
        writer.writeFile(dateFormat.format(date) + ".csv", data);
    }

    private static int generateSeed() {
        int seed;
        int randomTime = (int) new Date().getTime();
        double randomNumber = Math.random();
        seed = (int) Math.floor(randomTime * randomNumber);
        return seed;
    }

    private static Pair generateInitialSolution() {
        Pair result;

        try {
            long startTime;
            long stopTime;
            Company company;

            startTime = System.nanoTime();
            company = new Company();
            stopTime = System.nanoTime();
            double executionTime = (stopTime - startTime) / Math.pow(10, 9);
            result = new Pair(company, executionTime);

            printInitialResults(company, stopTime - startTime);
        } catch (Exception e) {
            System.err.println("Unexpected error computing initial solution: " + e.getMessage());
            throw e;
        }
        return result;
    }

    private static ArrayList<String> runAlgorithm(Company company, boolean use_hill, int iterations, int stiter, int k, double lambda) {
        Problem problem;
        Search alg;
        String algorithm;
        ArrayList<String> resultData = new ArrayList<>();

        if (use_hill) {
            algorithm = "Hill climbing";
            problem = new Problem(company,
                    new HillSuccessors(),
                    new IsSolution(),
                    new Heuristic());
            alg = new HillClimbingSearch();
        } else {
            algorithm = "Simulated Annealing";
            problem = new Problem(company,
                    new AnnealingSuccessors(),
                    new IsSolution(),
                    new Heuristic());
            if (iterations != -1) {
                //iteraciones, iteraciones/cambio de temperatura. K y lambda = para funcion de acceptacion.  K = temperatura inicial. Lambda = suavidad con que cae
                alg = new SimulatedAnnealingSearch(iterations, stiter, k, lambda);
            } else {
                alg = new SimulatedAnnealingSearch();
            }
        }

        //Execute algorithm
        SearchAgent agent;
        long startTime;
        long stopTime;
        try {
            startTime = System.nanoTime();
            agent = new SearchAgent(problem, alg);
            stopTime = System.nanoTime();
        } catch (Exception e) {
            System.err.println("Unexpected error executing algorithm: " + e.getMessage());
            throw new RuntimeException(e);
        }

        //getting data
        double executionTime = (stopTime - startTime) / Math.pow(10, 9);
        Company companySolution = (Company) alg.getGoalState();
        resultData.add(String.valueOf(executionTime));
        resultData.add(String.valueOf(companySolution.getStorageCost()));
        resultData.add(String.valueOf(companySolution.getSendCost()));
        resultData.add(String.valueOf(companySolution.getStorageCost() + companySolution.getSendCost()));
        resultData.add(String.valueOf(companySolution.getHappyness()));

        //Results
        printResults(algorithm, alg, agent, stopTime - startTime);
        return resultData;
    }

    private static void printInitialResults(Company company, long time) {
        double elapsedTimeInSeconds = time / Math.pow(10, 9);

        System.out.println("Printing results for initial solution");
        System.out.println(company);
        System.out.println("Initial solution took " + elapsedTimeInSeconds + " seconds");
        System.out.println("\n");
    }

    private static void printResults(String algorithm, Search alg, SearchAgent agent, long time) {
        double elapsedTimeInSeconds = time / Math.pow(10, 9);

        System.out.println("Printing results after applying algorithm " + algorithm);
        Company company = (Company) alg.getGoalState();
        System.out.println(company);
        System.out.println("Algorithm took " + elapsedTimeInSeconds + " seconds");

        ////
        printInstrumentation(agent.getInstrumentation());
        printActions(agent.getActions());
        System.out.println("\n");

    }

    private static void printActions(List actions) {
        //System.out.println("Actons taken to get to the result:");
        //for (Object o : actions) {
        //    String action = (String) o;
        //    System.out.println(action);
        //}
    }

    private static void printInstrumentation(Properties properties) {
        System.out.println("Properties of the algorithm:");
        for (Object o : properties.keySet()) {
            String key = (String) o;
            String property = properties.getProperty(key);
            System.out.println(key + " : " + property);
        }

    }
}
