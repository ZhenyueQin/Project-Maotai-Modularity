package ga.operations.reproducers;

import ga.components.chromosomes.GenderDiploid;
import ga.components.materials.Material;
import ga.components.materials.SimpleMaterial;
import ga.operations.expressionMaps.DiploidEvolvedMap;
import ga.operations.expressionMaps.ExpressionMap;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by zhenyueqin on 17/6/17.
 */
public class SimpleGenderReproducer extends GenderReproducer<GenderDiploid> {

    public SimpleGenderReproducer(final int numOfChildren) {
        super(numOfChildren);
    }

    @Override
    protected GenderDiploid recombine(@NotNull final List<GenderDiploid> mates) {
        GenderDiploid father = mates.get(0);
        GenderDiploid mother = mates.get(1);
        List<Material> maleGametes = crossover(father);
        List<Material> femaleGametes = crossover(mother);
        final int maleMatch = ThreadLocalRandom.current().nextInt(maleGametes.size());
        final int femaleMatch = ThreadLocalRandom.current().nextInt(femaleGametes.size());
        final boolean masculine = ThreadLocalRandom.current().nextBoolean();
        final ExpressionMap<SimpleMaterial, SimpleMaterial> dominanceMap;
        if (Math.random() < 0.5) {
            dominanceMap = father.getMapping().copy();
        } else {
            dominanceMap = mother.getMapping().copy();
        }
        return new GenderDiploid((SimpleMaterial) maleGametes.get(maleMatch),
                (SimpleMaterial) femaleGametes.get(femaleMatch),
                dominanceMap, masculine);
    }
}
