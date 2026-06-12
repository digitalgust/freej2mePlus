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

public class RayIntersection
{
	private static final int MAX_TEXTURE_UNITS = 2;

	private Node intersected;
	private float distance;
	private int submeshIndex;
	private final float[] textureS = new float[MAX_TEXTURE_UNITS];
	private final float[] textureT = new float[MAX_TEXTURE_UNITS];
	private final float[] normal = new float[] { 0f, 0f, 1f };
	private final float[] ray = new float[] { 0f, 0f, 0f, 0f, 0f, 1f };

	public RayIntersection() {  }


	public float getDistance() { return distance; }

	public Node getIntersected() { return intersected; }

	public float getNormalX() { return normal[0]; }

	public float getNormalY() { return normal[1]; }

	public float getNormalZ() { return normal[2]; }

	public void getRay(float[] ray)
	{
		if (ray == null)
		{
			throw new NullPointerException();
		}
		if (ray.length < 6)
		{
			throw new IllegalArgumentException();
		}
		System.arraycopy(this.ray, 0, ray, 0, 6);
	}

	public int getSubmeshIndex() { return submeshIndex; }

	public float getTextureS(int index)
	{
		checkTextureIndex(index);
		return textureS[index];
	}

	public float getTextureT(int index)
	{
		checkTextureIndex(index);
		return textureT[index];
	}

	void setResult(Node intersected, float distance, int submeshIndex,
			float normalX, float normalY, float normalZ,
			float textureS0, float textureT0,
			float[] ray)
	{
		this.intersected = intersected;
		this.distance = distance;
		this.submeshIndex = submeshIndex;
		this.normal[0] = normalX;
		this.normal[1] = normalY;
		this.normal[2] = normalZ;
		this.textureS[0] = textureS0;
		this.textureT[0] = textureT0;
		System.arraycopy(ray, 0, this.ray, 0, 6);
	}

	private void checkTextureIndex(int index)
	{
		if (index < 0 || index >= MAX_TEXTURE_UNITS)
		{
			throw new IndexOutOfBoundsException();
		}
	}

}
