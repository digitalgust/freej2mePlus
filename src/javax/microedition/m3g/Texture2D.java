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

public class Texture2D extends Transformable
{

	public static final int FILTER_BASE_LEVEL = 208;
	public static final int FILTER_LINEAR = 209;
	public static final int FILTER_NEAREST = 210;
	public static final int FUNC_ADD = 224;
	public static final int FUNC_BLEND = 225;
	public static final int FUNC_DECAL = 226;
	public static final int FUNC_MODULATE = 227;
	public static final int FUNC_REPLACE = 228;
	public static final int WRAP_CLAMP = 240;
	public static final int WRAP_REPEAT = 241;


	private int blending = FUNC_MODULATE;
	private int blendcolor = 0;
	private int filter = FILTER_NEAREST;
	private int filterlevel = FILTER_BASE_LEVEL;
	private int wraps = WRAP_REPEAT;
	private int wrapt = WRAP_REPEAT;

	private Image2D texImage;

	public Texture2D(Image2D image)
	{
		setImage(image);
	}


	public int getBlendColor() { return blendcolor; }

	public int getBlending() { return blending; }

	public Image2D getImage() { return texImage; }

	public int getImageFilter() { return filter; }

	public int getLevelFilter() { return filterlevel; }

	public int getWrappingS() { return wraps; }

	public int getWrappingT() { return wrapt; }

	public void setBlendColor(int RGB) { blendcolor = RGB & 0x00FFFFFF; }

	public void setBlending(int func)
	{
		if (func < FUNC_ADD || func > FUNC_REPLACE)
		{
			throw new IllegalArgumentException();
		}
		blending = func;
	}

	public void setFiltering(int levelFilter, int imageFilter)
	{
		if (levelFilter < FILTER_BASE_LEVEL || levelFilter > FILTER_NEAREST || imageFilter < FILTER_LINEAR || imageFilter > FILTER_NEAREST)
		{
			throw new IllegalArgumentException();
		}
		filterlevel = levelFilter;
		filter = imageFilter;
	}

	public void setImage(Image2D image)
	{
		if (image == null)
		{
			throw new NullPointerException();
		}
		if (!isPositivePowerOfTwo(image.getWidth()) || !isPositivePowerOfTwo(image.getHeight()))
		{
			throw new IllegalArgumentException();
		}
		texImage = image;
	}

	public void setWrapping(int wrapS, int wrapT)
	{
		if ((wrapS != WRAP_CLAMP && wrapS != WRAP_REPEAT) || (wrapT != WRAP_CLAMP && wrapT != WRAP_REPEAT))
		{
			throw new IllegalArgumentException();
		}
		wraps = wrapS;
		wrapt = wrapT;
	}

	private static boolean isPositivePowerOfTwo(int value)
	{
		return value > 0 && (value & (value - 1)) == 0;
	}

}
