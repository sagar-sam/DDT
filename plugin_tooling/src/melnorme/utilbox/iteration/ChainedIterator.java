/*******************************************************************************
 * Copyright (c) 2013, 2014 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package melnorme.utilbox.iteration;


/**
 * An iterator composed of two sub-iterators, one iterated after the other. 
 * Also supports creating a copy of the current state of iteration.
 */
public class ChainedIterator<T> implements ICopyableIterator<T> {
	
	public static <U> ICopyableIterator<U> create(ICopyableIterator<U> firstIter, ICopyableIterator<U> secondIter) {
		return new ChainedIterator<U>(firstIter, secondIter);
	}
	
	protected ICopyableIterator<T> firstIterator;
	protected final ICopyableIterator<T> secondIterator;
	
	public ChainedIterator(ICopyableIterator<T> _firstIter, ICopyableIterator<T> _secondIter) {
		this.firstIterator = _firstIter.optimizedSelf();
		this.secondIterator = _secondIter.optimizedSelf();
	}
	
	@Override
	public boolean hasNext() {
		return firstIterator.hasNext() || secondIterator.hasNext();
		
	}
	
	@Override
	public T next() {
		if(firstIterator.hasNext())
			return firstIterator.next();
		return secondIterator.next();
		
	}
	
	@Override
	public ICopyableIterator<T> copyState() {
		return new ChainedIterator<T>(firstIterator.copyState(), secondIterator.copyState());
	}
	
	@Override
	public ICopyableIterator<T> optimizedSelf() {
		if(firstIterator.hasNext()) {
			firstIterator = firstIterator.optimizedSelf();
			return this;
		} else {
			return secondIterator.optimizedSelf();
		}
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
}