package dev.sapphic.wearablebackpacks.advancement;

import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class SimpleCriteriaTrigger extends AbstractCriterion<SimpleCriteriaTrigger.Conditions> {
  private final Identifier id;

  SimpleCriteriaTrigger(final Identifier id) {
    this.id = id;
  }

  @Override
  public Identifier getId() {
    return this.id;
  }

  public void trigger(final ServerPlayerEntity player) {
    this.test(player, conditions -> true);
  }

  @Override
  protected Conditions conditionsFromJson(
    final JsonObject json, final EntityPredicate.Extended predicate,
    final AdvancementEntityPredicateDeserializer deserializer
  ) {
    return new Conditions(predicate);
  }

  public final class Conditions extends AbstractCriterionConditions {
    private Conditions(final EntityPredicate.Extended predicate) {
      super(SimpleCriteriaTrigger.this.id, predicate);
    }
  }
}
