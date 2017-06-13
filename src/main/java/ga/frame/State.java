package ga.frame;

import com.sun.istack.internal.NotNull;
import ga.collections.Population;
import ga.collections.PopulationMode;
import ga.collections.Statistics;
import ga.components.chromosomes.Chromosome;
import ga.operations.fitnessFunctions.FitnessFunction;
import ga.operations.mutators.Mutator;
import ga.operations.postOperators.PostOperator;
import ga.operations.priorOperators.PriorOperator;
import ga.operations.reproducers.Reproducer;
import ga.operations.selectionOperators.selectors.Selector;

/*
    GASEE is a Java-based genetic algorithm library for scientific exploration and experiment.
    Copyright 2016 Siu-Kei Muk

    This file is part of GASEE.

    GASEE is free library: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 2.1 of the License, or
    (at your option) any later version.

    GASEE is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with GASEE.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * This class provides an overall base implementation for the state of current generation of genetic algorithm.
 *
 * @author Siu Kei Muk (David)
 * @since 27/08/16.
 */
public abstract class State<C extends Chromosome> {

    protected int generation = 0;
    protected int numOfMates;
    protected final Population<C> population;
    protected FitnessFunction fitnessFunction;
    protected Mutator mutator;
    protected Reproducer<C> reproducer;
    protected Selector<C> selector;

    /**
     * Constructs an initial state for the GA
     * @param population initial population
     * @param fitnessFunction fitness function
     * @param mutator mutators operator
     * @param reproducer reproducers operator
     * @param selector parents selector
     * @param numOfMates number of parents per reproduction
     */
    public State(@NotNull final Population<C> population,
                 @NotNull final FitnessFunction fitnessFunction,
                 @NotNull final Mutator mutator,
                 @NotNull final Reproducer<C> reproducer,
                 @NotNull final Selector<C> selector,
                 final int numOfMates) {
        this.population = population;
        this.fitnessFunction = fitnessFunction;
        this.mutator = mutator;
        this.reproducer = reproducer;
        this.selector = selector;
        this.numOfMates = numOfMates;
        evaluate(true);
    }

    /**
     * Performs recombination using the reproducers operator.
     */
    public abstract void reproduce();

    /**
     * Performs mutators using the mutation operator.
     */
    public abstract void mutate();

    /**
     * Evaluates the fitnessFunctions function value of the whole population
     * @param recomputePhenotype determines whether to force re-computation of phenotype from genotype
     */
    public void evaluate(final boolean recomputePhenotype){
        population.evaluate(fitnessFunction, recomputePhenotype);
    }

    /**
     * Records the information of the current population to a given statistics bookkeeper
     * @param statistics
     */
    public void record(Statistics<C> statistics) {
        statistics.record(population.getIndividualsView());
    }

    /**
     * Performs operation before reproduction
     * @param priorOperator
     */
    public void preOperate(PriorOperator<C> priorOperator){
        population.setMode(PopulationMode.PRIOR);
        priorOperator.preOperate(population);
    }

    /**
     * Performs operation after reproduction
     * @param postOperator
     */
    public void postOperate(PostOperator<C> postOperator) {
        population.setMode(PopulationMode.POST);
        postOperator.postOperate(population);
    }

    /**
     * Notifies the state to proceed to the next generation
     * @return a boolean of whether the next generation is successfully reached
     */
    public boolean nextGeneration(){
        generation++;
        return population.nextGeneration();
    }

    public Population<C> getPopulation(){
        return population;
    }

    public int getGeneration() {
        return generation;
    }

    /**
     * @return the fitness function (not value) being used in the GA
     */
    public FitnessFunction getFitnessFunction() {
        return fitnessFunction;
    }

    public void setFitnessFunction(@NotNull final FitnessFunction fitnessFunction) {
        this.fitnessFunction = fitnessFunction;
    }

    public void setMutator(@NotNull final Mutator mutator) {
        this.mutator = mutator;
    }

    public void setReproducer(@NotNull final Reproducer<C> reproducer) {
        this.reproducer = reproducer;
    }
}