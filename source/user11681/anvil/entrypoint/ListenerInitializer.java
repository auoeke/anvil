package user11681.anvil.entrypoint;

import java.util.Collection;
import user11681.anvil.event.EventListener;

public interface ListenerInitializer {
	Collection<Class<? extends EventListener<?>>> get();
}
