package user11681.anvil.entrypoint;

import java.util.Collection;
import user11681.anvil.event.AnvilEvent;

public interface EventInitializer {
	Collection<Class<? extends AnvilEvent>> get();
}
