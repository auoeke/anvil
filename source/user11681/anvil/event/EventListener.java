package user11681.anvil.event;

import java.util.function.Consumer;

public class EventListener<T extends AnvilEvent> implements Comparable<EventListener<T>> {
	protected final Class<? super T> eventClass;
	protected final Consumer<? super T> consumer;
	protected final int priority;
	protected final boolean isPersistent;

	public EventListener(Class<? super T> eventClass, Consumer<? super T> consumer, int priority, boolean isPersistent) {
		this.eventClass = eventClass;
		this.consumer = consumer;
		this.priority = priority;
		this.isPersistent = isPersistent;
	}

	public boolean isPersistent() {
		return this.isPersistent;
	}

	@Override public boolean equals(Object other) {
		return other instanceof EventListener && ((EventListener<?>) other).eventClass == this.eventClass;
	}

	@Override public int compareTo(EventListener<T> other) {
		return this.priority - other.priority;
	}

	public void accept(T event) {
		if (this.eventClass.isInstance(event)) {
			this.consumer.accept(event);
		}
	}
}
