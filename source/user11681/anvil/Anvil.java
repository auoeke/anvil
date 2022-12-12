package user11681.anvil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import user11681.anvil.entrypoint.ClientEventInitializer;
import user11681.anvil.entrypoint.ClientListenerInitializer;
import user11681.anvil.entrypoint.CommonEventInitializer;
import user11681.anvil.entrypoint.CommonListenerInitializer;
import user11681.anvil.entrypoint.EventInitializer;
import user11681.anvil.entrypoint.ListenerInitializer;
import user11681.anvil.entrypoint.ServerEventInitializer;
import user11681.anvil.entrypoint.ServerListenerInitializer;
import user11681.anvil.event.AnvilEvent;
import user11681.anvil.event.EventInvocationHandler;
import user11681.anvil.event.EventListener;
import user11681.anvil.event.Listener;
import user11681.anvil.event.ListenerList;
import user11681.anvil.mixin.duck.ArrayBackedEventDuck;

public class Anvil implements PreLaunchEntrypoint {
	public static final String MOD_ID = "anvil";
	public static final Logger LOGGER = LogManager.getLogger("anvil");

	protected static final Map<Class<? extends AnvilEvent>, ListenerList<?>> EVENTS = new HashMap();
	protected static final Map<Class<? extends AnvilEvent>, Set<Class<? extends AnvilEvent>>> SUBEVENTS = new HashMap();
	protected static final Set<Event<?>> FABRIC_EVENTS = new HashSet();

	protected static boolean fabricSupport = true;
	protected static int abstractEvents;
	protected static int implementations;
	protected static int anvilEvents;
	protected static int listenedAnvilEvents;
	protected static int fabricEvents;
	protected static int totalEvents;
	protected static int anvilListeners;
	protected static int fabricListeners;
	protected static int totalListeners;

	@Override public void onPreLaunch() {
		long eventDuration = this.time(Anvil::registerEvents);
		long listenerDuration = this.time(Anvil::registerListeners);
		String implementationString = implementations == 1 ? "implementation" : "implementations";
		String anvilClassString = anvilEvents == 1 ? "class" : "classes";
		String listenerString = totalListeners == 1 ? "listener" : "listeners";
		String anvilEventString = listenedAnvilEvents == 1 ? "event" : "events";
		String fabricEventString = fabricEvents == 1 ? "event" : "events";
		String totalEventString = totalEvents == 1 ? "event was" : "events were";
		LOGGER.info("Registered {} anvil event {} ({} abstract and {} {}) in {} μs.", anvilEvents, anvilClassString, abstractEvents, implementations, implementationString, eventDuration);
		LOGGER.info("Registered {} event {} ({} to {} anvil {} and {} to {} Fabric {}) in in {} μs.", totalListeners, listenerString, anvilListeners, listenedAnvilEvents, anvilEventString, fabricListeners, fabricEvents, fabricEventString, listenerDuration);
		LOGGER.info("Registration finished after {} μs. {} {} registered.", eventDuration + listenerDuration, totalEvents, totalEventString);
	}

	protected long time(Runnable runnable) {
		long start = System.nanoTime();
		runnable.run();
		return (System.nanoTime() - start) / 1000;
	}

	protected static void registerEvents() {
		FabricLoader loader = FabricLoader.getInstance();
		List<EventInitializer> entrypoints = new ArrayList<>(loader.getEntrypoints("anvilCommonEvents", CommonEventInitializer.class));

		switch (loader.getEnvironmentType()) {
			case CLIENT:
				entrypoints.addAll(loader.getEntrypoints("anvilClientEvents", ClientEventInitializer.class));
				break;
			case SERVER:
				entrypoints.addAll(loader.getEntrypoints("anvilServerEvents", ServerEventInitializer.class));
		}

		for (EventInitializer o : entrypoints) {
			for (Class<? extends AnvilEvent> value : o.get()) {
				registerBranch(value);
			}
		}
	}

	protected static void registerBranch(Class<? extends AnvilEvent> clazz) {
		if (AnvilEvent.class.isAssignableFrom(clazz.getSuperclass())) {
			Class<? extends AnvilEvent> superclass = (Class) clazz.getSuperclass();
			registerBranch(superclass);
			SUBEVENTS.get(superclass).add(clazz);
		}

		if (!SUBEVENTS.containsKey(clazz)) {
			Set<Class<? extends AnvilEvent>> subevents = new HashSet<>();
			subevents.add(clazz);
			SUBEVENTS.put(clazz, subevents);

			if (Modifier.isAbstract(clazz.getModifiers())) {
				++abstractEvents;
			} else {
				++implementations;
			}

			++anvilEvents;
			++totalEvents;
		}

		if (!EVENTS.containsKey(clazz)) {
			EVENTS.put(clazz, new ListenerList());
		}
	}

	protected static void registerListeners() {
		FabricLoader loader = FabricLoader.getInstance();
		List<ListenerInitializer> entrypoints = new ArrayList<>(loader.getEntrypoints("anvilCommonListeners", CommonListenerInitializer.class));

		switch (loader.getEnvironmentType()) {
			case CLIENT:
				entrypoints.addAll(loader.getEntrypoints("anvilClientListeners", ClientListenerInitializer.class));
				break;
			case SERVER:
				entrypoints.addAll(loader.getEntrypoints("anvilServerListeners", ServerListenerInitializer.class));
		}

		for (ListenerInitializer value : entrypoints) {
			for (Class<? extends EventListener<?>> o : value.get()) {
				for (Method method : o.getDeclaredMethods()) {
					Listener annotation = method.getAnnotation(Listener.class);

					if (annotation != null) {
						int modifiers = method.getModifiers();

						if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers)) {
							registerListener((Class<? extends AnvilEvent>) method.getParameterTypes()[0], method, annotation);
							++totalListeners;
						}
					}
				}
			}
		}
	}

	protected static <T extends AnvilEvent> void registerListener(Class<T> eventClass, Method method, Listener annotation) {
		int priority;

		if (AnvilEvent.class.isAssignableFrom(eventClass)) {
			if (method.getReturnType() == Void.TYPE && method.getParameterCount() == 1) {
				for (Class<? extends T> o : (Set<Class<? extends T>>) SUBEVENTS.get(eventClass)) {
					priority = annotation.priority();

					if (priority < 0) {
						throw new IllegalArgumentException(String.format("%s priority < 0", method));
					}

					if (priority > 100) {
						throw new IllegalArgumentException(String.format("%s priority > 100", method));
					}

					ListenerList<?> listeners = EVENTS.get(o);
					listeners.add((Class) o, eventx -> {
						try {
							method.invoke(null, eventx);
						} catch (IllegalAccessException | InvocationTargetException var4) {
							LOGGER.error(String.format("An error occurred while attempting to fire %s: ", o.getName()), var4.getCause());
						}
					}, priority, annotation.persist());

					++anvilListeners;

					if (listeners.size() == 1) {
						++listenedAnvilEvents;
					}
				}
			}
		} else if (fabricSupport) {
			if (annotation.priority() != 50) {
				throw new IllegalArgumentException("anvil does not support non-anvil event priorities.");
			}

			if (annotation.persist()) {
				throw new IllegalArgumentException("anvil cannot send canceled events to non-anvil events.");
			}

			for (Field field : eventClass.getDeclaredFields()) {
				String className = "net.fabricmc.fabric.impl.base.event.ArrayBackedEvent";

				try {
					boolean accessible = field.isAccessible();
					field.setAccessible(true);
					Event event = (Event) field.get(null);
					field.setAccessible(accessible);

					if (Class.forName(className).isInstance(event)) {
						event.register(EventInvocationHandler.proxy(((ArrayBackedEventDuck) event).getType(), method));
					}

					if (FABRIC_EVENTS.add(event)) {
						++fabricEvents;
						++totalEvents;
					}

					++fabricListeners;
				} catch (IllegalAccessException exception) {
					LOGGER.error("illegal access? Impossible.", exception);
				} catch (ClassNotFoundException exception) {
					LOGGER.error(String.format("unable to class find %s.", "net.fabricmc.fabric.impl.base.event.ArrayBackedEvent"), exception);
					fabricSupport = false;
				}
			}
		}
	}

	public static int getAbstractEvents() {
		return abstractEvents;
	}

	public static int getEventImplementations() {
		return implementations;
	}

	public static int getAnvilEvents() {
		return anvilEvents;
	}

	public static int getFabricEvents() {
		return fabricEvents;
	}

	public static int getTotalEvents() {
		return totalEvents;
	}

	public static int getAnvilListeners() {
		return anvilListeners;
	}

	public static int getFabricListenerS() {
		return fabricListeners;
	}

	public static int getTotalListeners() {
		return totalListeners;
	}

	public static <T extends AnvilEvent> T fire(T event) {
		ListenerList<T> listeners = (ListenerList<T>) EVENTS.get(event.getClass());

		if (listeners == null) {
			throw new IllegalStateException(String.format("attempted to call an unregistered event %s.", event.getClass().getName()));
		}

		for (EventListener<? super T> listener : listeners) {
			if (event.shouldContinue() || listener.isPersistent()) {
				listener.accept(event);
			}
		}

		return event;
	}
}
