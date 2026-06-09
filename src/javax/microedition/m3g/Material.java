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

public class Material extends Object3D
{

	public static final int AMBIENT = 1024;
	public static final int DIFFUSE = 2048;
	public static final int EMISSIVE = 4096;
	public static final int SPECULAR = 8192;


	private int ambientColor = 0x00333333;
	private int diffuseColor = 0xFFCCCCCC;
	private int emissiveColor = 0x00000000;
	private int specularColor = 0x00000000;
	private float shine;
	private boolean tracking;


	public Material() {  }


	public int getColor(int target)
	{
		int color = 0;
		if ((target & AMBIENT) != 0)
		{
			color |= ambientColor;
		}
		if ((target & DIFFUSE) != 0)
		{
			color |= diffuseColor;
		}
		if ((target & EMISSIVE) != 0)
		{
			color |= emissiveColor;
		}
		if ((target & SPECULAR) != 0)
		{
			color |= specularColor;
		}
		return color;
	}

	public float getShininess() { return shine; }

	public boolean isVertexColorTrackingEnabled() { return tracking; }

	public void setColor(int target, int ARGB)
	{
		if ((target & AMBIENT) != 0)
		{
			ambientColor = (ambientColor & 0xFF000000) | (ARGB & 0x00FFFFFF);
		}
		if ((target & DIFFUSE) != 0)
		{
			diffuseColor = ARGB;
		}
		if ((target & EMISSIVE) != 0)
		{
			emissiveColor = (emissiveColor & 0xFF000000) | (ARGB & 0x00FFFFFF);
		}
		if ((target & SPECULAR) != 0)
		{
			specularColor = (specularColor & 0xFF000000) | (ARGB & 0x00FFFFFF);
		}
	}

	public void setShininess(float shininess)
	{
		if (shininess < 0f)
		{
			throw new IllegalArgumentException();
		}
		shine = shininess;
	}

	public void setVertexColorTrackingEnable(boolean enable) { tracking = enable; }

}
