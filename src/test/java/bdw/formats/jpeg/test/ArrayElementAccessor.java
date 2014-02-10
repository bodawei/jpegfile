/*
 *  Copyright 2014 柏大衛
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bdw.formats.jpeg.test;

import bdw.format.jpeg.data.DataItem;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A utility class used to access the element of an array through reflection.
 * See TestUtils for more details.
 */
public class ArrayElementAccessor implements Accessor {
	DataItem item;
	private Method setterToTest = null;
	private Method getterToTest = null;

	public ArrayElementAccessor(DataItem item, String propName) {
		String suffix = propName.toUpperCase().charAt(0) + propName.substring(1);
		String setterName = "set" + suffix;
		String getterName = "get" + suffix;
		this.item = item;
		for (Method method : item.getClass().getDeclaredMethods()) {
			if (method.getName().equals(setterName)) {
				setterToTest = method;
			}
			if (method.getName().equals(getterName)) {
				getterToTest = method;
			}
		}
	}

	@Override
	public void setValue(int value) throws Throwable {
		try {
			setterToTest.invoke(item, 0, value);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException("Couldn't invoke setter", ex);
		} catch (InvocationTargetException ex) {
			throw ex.getTargetException();
		}
	}

	@Override
	public Integer getValue() throws Throwable {
		Number result = null;
		try {
			result = (Number) getterToTest.invoke(item, 0);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException("Couldn't invoke getter", ex);
		} catch (InvocationTargetException ex) {
			throw new RuntimeException("Couldn't invoke getter", ex);
		}

		return result.intValue();
	}
}