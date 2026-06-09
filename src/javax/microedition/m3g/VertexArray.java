/*
	This file is part of FreeJ2ME.

	FreeJ2ME is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	FreeJ2ME is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with FreeJ2ME.  If not, see http://www.gnu.org/licenses/
*/
package javax.microedition.m3g;

public class VertexArray extends Object3D
{
	private final int vertexCount;
	private final int componentCount;
	private final int componentType;
	private final short[] values;

	public VertexArray(int numVertices, int numComponents, int componentSize)
	{
		if (numVertices < 1 || numComponents < 2 || numComponents > 4 || (componentSize != 1 && componentSize != 2))
		{
			throw new IllegalArgumentException();
		}
		vertexCount = numVertices;
		componentCount = numComponents;
		componentType = componentSize;
		values = new short[numVertices * numComponents];
	}


	public void get(int firstVertex, int numVertices, byte[] values)
	{
		checkRange(firstVertex, numVertices);
		if (this.componentType != 1)
		{
			throw new IllegalStateException();
		}
		if (values == null)
		{
			throw new NullPointerException();
		}
		int required = numVertices * componentCount;
		if (values.length < required)
		{
			throw new IllegalArgumentException();
		}

		int src = firstVertex * componentCount;
		for (int i = 0; i < required; i++)
		{
			values[i] = (byte) this.values[src + i];
		}
	}

	public void get(int firstVertex, int numVertices, short[] values)
	{
		checkRange(firstVertex, numVertices);
		if (this.componentType != 2)
		{
			throw new IllegalStateException();
		}
		if (values == null)
		{
			throw new NullPointerException();
		}
		int required = numVertices * componentCount;
		if (values.length < required)
		{
			throw new IllegalArgumentException();
		}

		System.arraycopy(this.values, firstVertex * componentCount, values, 0, required);
	}

	public int getComponentCount() { return componentCount; }

	public int getComponentType() { return componentType; }

	public int getVertexCount() { return vertexCount; }

	public void set(int firstVertex, int numVertices, byte[] values)
	{
		checkRange(firstVertex, numVertices);
		if (componentType != 1)
		{
			throw new IllegalStateException();
		}
		if (values == null)
		{
			throw new NullPointerException();
		}
		int required = numVertices * componentCount;
		if (values.length < required)
		{
			throw new IllegalArgumentException();
		}

		int dst = firstVertex * componentCount;
		for (int i = 0; i < required; i++)
		{
			this.values[dst + i] = values[i];
		}
	}

	public void set(int firstVertex, int numVertices, short[] values)
	{
		checkRange(firstVertex, numVertices);
		if (componentType != 2)
		{
			throw new IllegalStateException();
		}
		if (values == null)
		{
			throw new NullPointerException();
		}
		int required = numVertices * componentCount;
		if (values.length < required)
		{
			throw new IllegalArgumentException();
		}

		System.arraycopy(values, 0, this.values, firstVertex * componentCount, required);
	}

	float getComponentAsFloat(int vertex, int component)
	{
		int value = values[vertex * componentCount + component];
		if (componentType == 1)
		{
			return (byte) value;
		}
		return value;
	}

	short[] getRawValues()
	{
		return values;
	}

	private void checkRange(int firstVertex, int numVertices)
	{
		if (firstVertex < 0 || numVertices < 0 || firstVertex + numVertices > vertexCount)
		{
			throw new IndexOutOfBoundsException();
		}
	}

}

