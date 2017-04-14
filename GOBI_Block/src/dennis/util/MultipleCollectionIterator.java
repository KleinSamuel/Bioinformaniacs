package dennis.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public class MultipleCollectionIterator<T> implements Iterator<T> {

	private LinkedList<Iterator<T>> collections = null;

	public MultipleCollectionIterator(Collection<Collection<T>> collections) {
		this.collections = new LinkedList<>();
		for (Collection<T> coll : collections) {
			this.collections.add(coll.iterator());
		}
	}

	@Override
	public boolean hasNext() {
		if (collections.isEmpty()) {
			return false;
		}
		if (collections.getFirst().hasNext()) {
			return true;
		}
		collections.removeFirst();
		return hasNext();
	}

	@Override
	public T next() {
		if (collections.isEmpty()) {
			return null;
		}
		if (collections.getFirst().hasNext()) {
			return collections.getFirst().next();
		}
		collections.removeFirst();
		return next();
	}
}
