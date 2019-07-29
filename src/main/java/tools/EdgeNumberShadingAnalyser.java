package tools;

import ga.components.materials.SimpleMaterial;
import ga.operations.fitnessFunctions.FitnessFunction;
import ga.operations.fitnessFunctions.GRNFitnessFunctionMultipleTargetsAllCombinationBalanceAsymmetricZhenyue;
import ga.others.GeneralMethods;
import ga.others.ModularityPathAnalyzer;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.util.*;

import static org.apache.commons.math3.util.CombinatoricsUtils.binomialCoefficientDouble;

public class EdgeNumberShadingAnalyser {
    private static final int populationSize = 100;
    private static final int tournamentSize = 3;
    private static final int maxGen = 2000;

    public static List<Double> getTournamentSelectionProbabilities(int populationSize, int tournamentSize) {
        List<Double> tournamentProbabilities = new ArrayList<>();
        for (int i=1; i<=(populationSize-tournamentSize+1); i++) {
            tournamentProbabilities.add(binomialCoefficientDouble(populationSize-i, tournamentSize-1) /
                    binomialCoefficientDouble(populationSize, tournamentSize));
        }
        for (int i=0; i<(tournamentSize-1); i++) {
            tournamentProbabilities.add(0.0);
        }
        return tournamentProbabilities;
    }

    public static List<Double> getProportionalSelectionProbabilities(FitnessFunction fitness, List<String[]> GRNs,
                                                                     int populationSize, int currentGen) {
        List<Double> fitnessList = new ArrayList<>();
        for (int i=0; i<populationSize; i++) {
            String[] aGRN = GRNs.get(i);
            SimpleMaterial aMaterial = GeneralMethods.convertStringArrayToSimpleMaterial(aGRN);
            List<Double> removeNoEdgeFitnessesZhenyueSym = ModularityPathAnalyzer.removeEdgeAnalyzer(0, aMaterial,
                    fitness, true, currentGen, null, false);
            fitnessList.add(removeNoEdgeFitnessesZhenyueSym.get(0));
        }

        List<Double> normedFitnessList = new ArrayList<>();
        double fitnessSum = fitnessList.stream().mapToDouble(Double::doubleValue).sum();
        for (int i=0; i<populationSize; i++) {
            normedFitnessList.add(fitnessList.get(i) / fitnessSum);
        }

        return normedFitnessList;
    }

    public static List<List<Double>> getEdgeProbabilityShading(FitnessFunction fitness, String selectionType, int maxGen,
                                                 String targetDir) {
        int edgeUpperBound = 50;
        int edgeLowerBound = 10;
        List<Double> tournamentProbabilities = getTournamentSelectionProbabilities(populationSize, tournamentSize);

        List<List<Double>> edgeProbabilitiesAllGen = new ArrayList<>();
        for (int aGen=0; aGen<maxGen; aGen++) {
            if (aGen % 50 == 0) {
                System.out.println("Process generation: " + aGen);
            }
            String test = String.format(targetDir, aGen);
            List<String[]> lines = GeneralMethods.readFileLineByLine(test);

            List<Double> concernedProbabilities = null;
            if (selectionType.equals("tournament")) {
                concernedProbabilities = tournamentProbabilities;
            } else if (selectionType.equals("proportional")) {
                concernedProbabilities = getProportionalSelectionProbabilities(fitness, lines, populationSize, aGen);
            } else {
                throw new NotImplementedException();
            }

            HashMap<Integer, List<Double>> edgeNumProbabilityMap = new HashMap<>();
            for (int i = 0; i < 100; i++) {
                String[] lastGRNString = lines.get(i);
                SimpleMaterial aMaterial = GeneralMethods.convertStringArrayToSimpleMaterial(lastGRNString);

                int edgeNum = GeneralMethods.getEdgeNumber(aMaterial);

                if (!edgeNumProbabilityMap.containsKey(edgeNum)) {
                    List<Double> aNewProbabilityList = new ArrayList<>();
                    aNewProbabilityList.add(concernedProbabilities.get(i));
                    edgeNumProbabilityMap.put(edgeNum, aNewProbabilityList);
                } else {
                    edgeNumProbabilityMap.get(edgeNum).add(concernedProbabilities.get(i));
                }
            }

            List<Double> edgeProbabilities = new ArrayList<>();
            for (int anEdge=edgeLowerBound; anEdge<=edgeUpperBound; anEdge++) {
                if (edgeNumProbabilityMap.containsKey(anEdge)) {
                    List<Double> aEdgeProbabilities = edgeNumProbabilityMap.get(anEdge);
                    edgeProbabilities.add(aEdgeProbabilities.stream().mapToDouble(Double::doubleValue).sum());
                } else {
                    edgeProbabilities.add(0.0);
                }
            }
            edgeProbabilitiesAllGen.add(edgeProbabilities);
        }
        System.out.println(edgeProbabilitiesAllGen);
        return edgeProbabilitiesAllGen;
    }

    private static final int[] target1 = {
            1, -1, 1, -1, 1,
            -1, 1, -1, 1, -1
    };
    private static final int[] target2 = {
            1, -1, 1, -1, 1,
            1, -1, 1, -1, 1
    };

    private static final int maxCycle = 100;

    private static final double perturbationRate = 0.15;
    private static final List<Integer> thresholds = Arrays.asList(0, 500);
    private static final int[] perturbationSizes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    private static final double stride = 0.00;
    private static final int[][] targets = {target1, target2};

    public static void main(String[] args) {
        FitnessFunction fitnessFunctionZhenyueSym = new GRNFitnessFunctionMultipleTargetsAllCombinationBalanceAsymmetricZhenyue(
                targets, maxCycle, perturbationRate, thresholds, perturbationSizes, 0.00);

        String targetPath = "/media/zhenyue-qin/New Volume/Data-Warehouse/Data-Experiments/Project-Maotai/tec-simultaneous-experiments/distributional-proportional";
        File[] directories = new File(targetPath).listFiles(File::isDirectory);

        List<List<List<Double>>> edgePrbabilityShadingList = new ArrayList<>();
        for (File aDirectory : directories) {
            String targetDir = aDirectory + "/population-phenotypes/all-population-phenotype_gen_%d.lists";

            String lastGenStringGRN = String.format(targetDir, maxGen);
            List<String[]> lastGenStringGRNList = GeneralMethods.readFileLineByLine(lastGenStringGRN);
            String[] eliteInLastGen = lastGenStringGRNList.get(0);
            SimpleMaterial eliteInLastGenMaterial = GeneralMethods.convertStringArrayToSimpleMaterial(eliteInLastGen);
            List<Double> removeNoEdgeFitnessesZhenyueSym = ModularityPathAnalyzer.removeEdgeAnalyzer(0, eliteInLastGenMaterial,
                    fitnessFunctionZhenyueSym, true, 1000, null, false);
            double finalFitness = removeNoEdgeFitnessesZhenyueSym.get(0);

            if (finalFitness > 0.9462) {
                System.out.println("Current Final Fitness: " + finalFitness);
                System.out.println("a directory: " + aDirectory);
                System.out.println("targetDir: " + targetDir);
                edgePrbabilityShadingList.add(getEdgeProbabilityShading(fitnessFunctionZhenyueSym, "proportional", maxGen, targetDir));
            }
        }

    }
}