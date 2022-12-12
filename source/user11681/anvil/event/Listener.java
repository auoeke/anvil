package user11681.anvil.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Listener {
	int DEFAULT_PRIORITY = 50;

	int priority() default 50;

	boolean persist() default false;
}
