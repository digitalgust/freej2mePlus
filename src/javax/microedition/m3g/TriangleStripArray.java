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

public class TriangleStripArray extends IndexBuffer
{
	private final int[] indices;
	private final int[] stripLengths;

	public TriangleStripArray(int[] indices, int[] stripLengths)
	{
		if (indices == null || stripLengths == null)
		{
			throw new NullPointerException();
		}
		validateStripLengths(stripLengths);
		int expected = 0;
		for (int i = 0; i < stripLengths.length; i++)
		{
			expected += stripLengths[i];
		}
		if (indices.length < expected)
		{
			throw new IllegalArgumentException();
		}

		this.indices = new int[expected];
		this.stripLengths = new int[stripLengths.length];
		System.arraycopy(indices, 0, this.indices, 0, expected);
		System.arraycopy(stripLengths, 0, this.stripLengths, 0, stripLengths.length);
	}

	public TriangleStripArray(int firstIndex, int[] stripLengths)
	{
		if (firstIndex < 0)
		{
			throw new IllegalArgumentException();
		}
		if (stripLengths == null)
		{
			throw new NullPointerException();
		}
		validateStripLengths(stripLengths);
		int count = 0;
		for (int i = 0; i < stripLengths.length; i++)
		{
			count += stripLengths[i];
		}
		this.indices = new int[count];
		this.stripLengths = new int[stripLengths.length];
		System.arraycopy(stripLengths, 0, this.stripLengths, 0, stripLengths.length);
		for (int i = 0; i < count; i++)
		{
			this.indices[i] = firstIndex + i;
		}
	}

	@Override
	public int getIndexCount()
	{
		return indices.length;
	}

	@Override
	public void getIndices(int[] indices)
	{
		if (indices == null)
		{
			throw new NullPointerException();
		}
		if (indices.length < this.indices.length)
		{
			throw new IllegalArgumentException();
		}
		System.arraycopy(this.indices, 0, indices, 0, this.indices.length);
	}

	int[] getRawIndices()
	{
		return indices;
	}

	int[] getStripLengths()
	{
		return stripLengths;
	}

	private void validateStripLengths(int[] stripLengths)
	{
		if (stripLengths.length == 0)
		{
			throw new IllegalArgumentException();
		}
		for (int i = 0; i < stripLengths.length; i++)
		{
			if (stripLengths[i] < 3)
			{
				throw new IllegalArgumentException();
			}
		}
	}

}
