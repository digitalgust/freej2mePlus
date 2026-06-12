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

public class VertexBuffer extends Object3D
{
	private static final int MAX_TEXTURE_UNITS = 2;

	private VertexArray positions;
	private VertexArray normals;
	private VertexArray colors;
	private final VertexArray[] texCoords = new VertexArray[MAX_TEXTURE_UNITS];
	private final float[] positionScaleBias = new float[] { 1f, 0f, 0f, 0f };
	private final float[][] texCoordScaleBias = new float[MAX_TEXTURE_UNITS][4];
	private int defaultColor = 0xFFFFFFFF;

	{
		for (int i = 0; i < MAX_TEXTURE_UNITS; i++)
		{
			texCoordScaleBias[i][0] = 1f;
		}
	}

	public VertexBuffer() {  }


	public VertexArray getColors() { return colors; }

	public int getDefaultColor() { return defaultColor; }

	public VertexArray getNormals() { return normals; }

	public VertexArray getPositions(float[] scaleBias)
	{
		if (scaleBias != null)
		{
			if (scaleBias.length < 4)
			{
				throw new IllegalArgumentException();
			}
			System.arraycopy(positionScaleBias, 0, scaleBias, 0, 4);
		}
		return positions;
	}

	public VertexArray getTexCoords(int index, float[] scaleBias)
	{
		checkTextureIndex(index);
		VertexArray texCoord = texCoords[index];
		if (scaleBias != null)
		{
			if (texCoord == null)
			{
				throw new IllegalArgumentException();
			}
			int required = texCoord.getComponentCount() + 1;
			if (scaleBias.length < required)
			{
				throw new IllegalArgumentException();
			}
			System.arraycopy(texCoordScaleBias[index], 0, scaleBias, 0, required);
		}
		return texCoord;
	}

	public int getVertexCount()
	{
		if (positions != null) { return positions.getVertexCount(); }
		if (normals != null) { return normals.getVertexCount(); }
		if (colors != null) { return colors.getVertexCount(); }
		for (int i = 0; i < MAX_TEXTURE_UNITS; i++)
		{
			if (texCoords[i] != null) { return texCoords[i].getVertexCount(); }
		}
		return 0;
	}

	public void setColors(VertexArray colors)
	{
		if (colors != null)
		{
			if (colors.getComponentType() != 1)
			{
				throw new IllegalArgumentException();
			}
			int count = colors.getComponentCount();
			if (count != 3 && count != 4)
			{
				throw new IllegalArgumentException();
			}
			checkVertexCount(colors);
		}
		this.colors = colors;
	}

	public void setDefaultColor(int ARGB) { defaultColor = ARGB; }

	public void setNormals(VertexArray normals)
	{
		if (normals != null)
		{
			if (normals.getComponentCount() != 3)
			{
				throw new IllegalArgumentException();
			}
			checkVertexCount(normals);
		}
		this.normals = normals;
	}

	public void setPositions(VertexArray positions, float scale, float[] bias)
	{
		if (positions != null)
		{
			if (positions.getComponentCount() != 3)
			{
				throw new IllegalArgumentException();
			}
			if (bias != null && bias.length < 3)
			{
				throw new IllegalArgumentException();
			}
			checkVertexCount(positions);
		}
		this.positions = positions;
		positionScaleBias[0] = scale;
		positionScaleBias[1] = bias != null ? bias[0] : 0f;
		positionScaleBias[2] = bias != null ? bias[1] : 0f;
		positionScaleBias[3] = bias != null ? bias[2] : 0f;
	}

	public void setTexCoords(int index, VertexArray texCoords, float scale, float[] bias)
	{
		checkTextureIndex(index);
		if (texCoords != null)
		{
			int count = texCoords.getComponentCount();
			if (count != 2 && count != 3)
			{
				throw new IllegalArgumentException();
			}
			if (bias != null && bias.length < count)
			{
				throw new IllegalArgumentException();
			}
			checkVertexCount(texCoords);
		}
		this.texCoords[index] = texCoords;
		texCoordScaleBias[index][0] = scale;
		texCoordScaleBias[index][1] = bias != null ? bias[0] : 0f;
		texCoordScaleBias[index][2] = bias != null ? bias[1] : 0f;
		texCoordScaleBias[index][3] = bias != null && bias.length > 2 ? bias[2] : 0f;
	}

	float[] getPositionScaleBias()
	{
		return positionScaleBias;
	}

	float[] getTexCoordScaleBias(int index)
	{
		checkTextureIndex(index);
		return texCoordScaleBias[index];
	}

	private void checkTextureIndex(int index)
	{
		if (index < 0 || index >= MAX_TEXTURE_UNITS)
		{
			throw new IndexOutOfBoundsException();
		}
	}

	private void checkVertexCount(VertexArray array)
	{
		int count = getVertexCount();
		if (count != 0 && array.getVertexCount() != count)
		{
			throw new IllegalArgumentException();
		}
	}

}
