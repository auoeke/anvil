package user11681.anvil.event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class ListenerList<T extends AnvilEvent> implements Iterable<EventListener<? super T>> {
	protected final List<EventListener<? super T>> delegate = new ArrayList<>();

	public void add(Class<? super T> clazz, Consumer<? super T> consumer, int priority, boolean persist) {
		EventListener<? super T> listener = new EventListener<>(clazz, consumer, priority, persist);
		List<EventListener<? super T>> delegate = this.delegate;
		int size = delegate.size();
		int index = size;

		for (int i = 0; i < size; ++i) {
			EventListener other = delegate.get(i);

			if (other.equals(listener)) {
				int comparison = listener.compareTo(other);

				if (comparison > 0) {
					index = i;
					break;
				}

				index = i + 1;
			}
		}

		delegate.add(index, listener);
	}

	public int size() {
		return this.delegate.size();
	}

	@Override public Iterator<EventListener<? super T>> iterator() {
		return this.delegate.iterator();
	}
}
