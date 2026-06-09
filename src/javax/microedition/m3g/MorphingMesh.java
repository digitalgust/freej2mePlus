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

public class MorphingMesh extends Mesh
{
	private final VertexBuffer[] morphTargets;
	private final float[] weights;


	public MorphingMesh(VertexBuffer base, VertexBuffer[] targets, IndexBuffer[] submeshes, Appearance[] appearances)
	{
		super(base, submeshes, appearances);
		morphTargets = copyTargets(targets);
		weights = new float[morphTargets.length];
	}

	public MorphingMesh(VertexBuffer base, VertexBuffer[] targets, IndexBuffer submesh, Appearance appearance)
	{
		super(base, submesh, appearance);
		morphTargets = copyTargets(targets);
		weights = new float[morphTargets.length];
	}


	public VertexBuffer getMorphTarget(int index)
	{
		if (index < 0 || index >= morphTargets.length)
		{
			throw new IndexOutOfBoundsException();
		}
		return morphTargets[index];
	}

	public int getMorphTargetCount() { return morphTargets.length; }

	public void getWeights(float[] store)
	{
		if (store == null)
		{
			throw new NullPointerException();
		}
		if (store.length < weights.length)
		{
			throw new IllegalArgumentException();
		}
		System.arraycopy(weights, 0, store, 0, weights.length);
	}

	public void setWeights(float[] values)
	{
		if (values == null)
		{
			throw new NullPointerException();
		}
		if (values.length < weights.length)
		{
			throw new IllegalArgumentException();
		}
		System.arraycopy(values, 0, weights, 0, weights.length);
	}

	private static VertexBuffer[] copyTargets(VertexBuffer[] targets)
	{
		if (targets == null)
		{
			throw new NullPointerException();
		}
		if (targets.length == 0)
		{
			throw new IllegalArgumentException();
		}
		VertexBuffer[] copy = new VertexBuffer[targets.length];
		for (int i = 0; i < targets.length; i++)
		{
			if (targets[i] == null)
			{
				throw new NullPointerException();
			}
			copy[i] = targets[i];
		}
		return copy;
	}

}
