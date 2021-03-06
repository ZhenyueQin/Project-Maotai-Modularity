package experiments.exp5_crossover_aid_validation;

import ga.collections.DetailedStatistics;
import ga.collections.Population;
import ga.components.chromosomes.SimpleDiploid;
import ga.frame.frames.Frame;
import ga.frame.frames.SimpleDiploidMultipleTargetFrame;
import ga.frame.states.SimpleDiploidState;
import ga.frame.states.State;
import ga.operations.dominanceMapMutators.DiploidDominanceMapMutator;
import ga.operations.dominanceMapMutators.ExpressionMapMutator;
import ga.operations.fitnessFunctions.FitnessFunction;
import ga.operations.fitnessFunctions.GRNFitnessFunctionMultipleTargetsFast;
import ga.operations.initializers.DiploidGRNInitializer;
import ga.operations.mutators.GRNEdgeMutator;
import ga.operations.mutators.Mutator;
import ga.operations.postOperators.PostOperator;
import ga.operations.postOperators.SimpleFillingOperatorForNormalizable;
import ga.operations.priorOperators.PriorOperator;
import ga.operations.priorOperators.SimpleElitismOperator;
import ga.operations.reproducers.Reproducer;
import ga.operations.reproducers.SimpleDiploidNoXReproducer;
import ga.operations.selectionOperators.selectionSchemes.SimpleTournamentScheme;
import ga.operations.selectionOperators.selectors.Selector;
import ga.operations.selectionOperators.selectors.SimpleTournamentSelector;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Zhenyue Qin (秦震岳) on 25/6/17.
 * The Australian National University.
 */
public class DiploidGRNFastMatrixNoXMain {
    /* The three targets that the GA evolve towards */
    private static final int[] target1 = {
            1, -1, 1, -1,
            1, -1, 1,
            -1, 1, -1
    };
    private static final int[] target2 = {
            1, -1, 1, -1,
            1, -1, 1,
            1, -1, 1
    };
    private static final int[] target3 = {
            1, -1, 1, -1,
            -1, 1, -1,
            1, -1, 1
    };

    /* Parameters of the GRN */
    private static final int maxCycle = 20;
    private static final int edgeSize = 20;
    private static final int perturbations = 300;
    private static final double geneMutationRate = 0.03;
    private static final int perturbationCycleSize = 100;
    private static final double dominanceMutationRate = 0.002;
    private static final double perturbationRate = 0.15;

    /* Parameters of the GA */
    private static final int numElites = 1;
    private static final int populationSize = 100;
    private static final int tournamentSize = 3;
    private static final double reproductionRate = 0.9;
    private static final int maxGen = 1550;
    private static final List<Integer> thresholds = Arrays.asList(0, 300, 1050);

    /* Settings for text outputs */
    private static final String summaryFileName = "Diploid-GRN-3-Target-10-Matrix-No-X.txt";
    private static final String csvFileName = "Diploid-GRN-3-Target-10-Matrix-No-X.csv";
    private static final String outputDirectory = "diploid-grn-3-target-10-matrix-no-x";
    private static final String mainFileName = "DiploidGRNFastMatrixNoXMain.java";
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    private static Date date = new Date();

    /* Settings for graph outputs */
    private static final String plotTitle = "Diploid GRN 3 Targets 10 Matrix No X";
    private static final String plotFileName = "Diploid-GRN-3-Target-10-Matrix-No-X.png";

    public static void main(String[] args) throws IOException {
        int[][] targets = {target1, target2, target3};

        /* Fitness function */
        FitnessFunction fitnessFunction = new GRNFitnessFunctionMultipleTargetsFast(
                targets, maxCycle, perturbations, perturbationRate, thresholds, perturbationCycleSize);

        /* It is not necessary to write an initializer, but doing so is convenient to
        repeat the experiment using different parameter */
        DiploidGRNInitializer initializer =
                new DiploidGRNInitializer(populationSize, target1.length, edgeSize);

        /* Population */
        Population<SimpleDiploid> population = initializer.initialize();

        /* Mutator for chromosomes */
        Mutator mutator = new GRNEdgeMutator(geneMutationRate);

        /* Selector for reproduction */
        Selector<SimpleDiploid> selector = new SimpleTournamentSelector<>(tournamentSize);

        /* Selector for elites */
        PriorOperator<SimpleDiploid> priorOperator = new SimpleElitismOperator<>(numElites);

        /* PostOperator is required to fill up the vacancy */
        PostOperator<SimpleDiploid> fillingOperator = new SimpleFillingOperatorForNormalizable<>(new SimpleTournamentScheme(tournamentSize));

        /* Reproducer for reproduction */
        Reproducer<SimpleDiploid> reproducer = new SimpleDiploidNoXReproducer(0.5, target1.length);

        /* Statistics for keeping track the performance in generations */
        DetailedStatistics<SimpleDiploid> statistics = new DetailedStatistics<>();

        /* Dominance expression map mutator */
        ExpressionMapMutator expressionMapMutator = new DiploidDominanceMapMutator(dominanceMutationRate);

        /* The state of an GA */
        State<SimpleDiploid> state = new SimpleDiploidState<>(population, fitnessFunction, mutator, reproducer,
                selector, 2, reproductionRate, expressionMapMutator);
        state.record(statistics);

        /* The frame of an GA to change states */
        Frame<SimpleDiploid> frame = new SimpleDiploidMultipleTargetFrame<>(
                state, fillingOperator, statistics, priorOperator);

        /* Set output paths */
        statistics.setDirectory(outputDirectory + "/" + dateFormat.format(date));
        statistics.copyMainFile(mainFileName, System.getProperty("user.dir") +
                "/src/main/java/experiments/exp5_crossover_aid_validation/" + mainFileName);

        statistics.print(0); // print the initial state of an population
        /* Actual GA evolutions */
        for (int i = 0; i < maxGen; i++) {
            frame.evolve();
            statistics.print(i);
        }

        /* Generate output files */
        statistics.save(summaryFileName);
        statistics.generateNormalCSVFile(csvFileName);
        statistics.generatePlot(plotTitle, plotFileName);
    }
}
