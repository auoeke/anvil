package user11681.anvil.mixin.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import user11681.anvil.mixin.duck.ArrayBackedEventDuck;

@Mixin(
   targets = {"net.fabricmc.fabric.impl.base.event.ArrayBackedEvent"},
   priority = 500
)
public abstract class ArrayBackedEventMixin implements ArrayBackedEventDuck {
   @Accessor
   @Override public abstract Class<?> getType();
}
