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

import javax.microedition.lcdui.Image;

public class Image2D extends Object3D
{

	public static final int ALPHA = 96;
	public static final int LUMINANCE = 97;
	public static final int LUMINANCE_ALPHA = 98;
	public static final int RGB = 99;
	public static final int RGBA = 100;


	private byte[] image;
	private int width = 0;
	private int height = 0;
	private int format = RGB;
	private boolean mutable = true;

	public Image2D(int fmt, int w, int h)
	{
		validateFormat(fmt);
		if (w <= 0 || h <= 0)
		{
			throw new IllegalArgumentException();
		}
		width = w;
		height = h;
		format = fmt;
		image = new byte[w * h * getBytesPerPixel(fmt)];
		mutable = true;
	}

	public Image2D(int fmt, int w, int h, byte[] img)
	{
		this(fmt, w, h);
		if (img == null)
		{
			throw new NullPointerException();
		}
		if (img.length < image.length)
		{
			throw new IllegalArgumentException();
		}
		System.arraycopy(img, 0, image, 0, image.length);
		mutable = false;
	}

	public Image2D(int fmt, int w, int h, byte[] img, byte[] palette)
	{
		this(fmt, w, h);
		if (img == null || palette == null)
		{
			throw new NullPointerException();
		}
		int componentCount = getBytesPerPixel(fmt);
		if (img.length < w * h)
		{
			throw new IllegalArgumentException();
		}
		if (palette.length < 256 * componentCount && (palette.length % componentCount) != 0)
		{
			throw new IllegalArgumentException();
		}
		int paletteEntries = palette.length / componentCount;
		for (int i = 0; i < w * h; i++)
		{
			int index = img[i] & 0xFF;
			if (index >= paletteEntries)
			{
				continue;
			}
			int src = index * componentCount;
			int dst = i * componentCount;
			System.arraycopy(palette, src, image, dst, componentCount);
		}
		mutable = false;
	}

	public Image2D(int fmt, Object img)
	{
		validateFormat(fmt);
		if (img == null)
		{
			throw new NullPointerException();
		}
		if (!(img instanceof Image))
		{
			throw new IllegalArgumentException();
		}

		Image source = (Image) img;
		width = source.getWidth();
		height = source.getHeight();
		format = fmt;
		image = convertImage(source, fmt);
		mutable = source.isMutable();
	}


	public int getFormat() { return format; }

	public int getHeight() { return height; }

	public int getWidth() { return width; }

	public boolean isMutable() { return mutable; }

	public void set(int x, int y, int w, int h, byte[] img)
	{
		if (!mutable)
		{
			throw new IllegalStateException();
		}
		if (img == null)
		{
			throw new NullPointerException();
		}
		if (x < 0 || y < 0 || w <= 0 || h <= 0 || x + w > width || y + h > height)
		{
			throw new IllegalArgumentException();
		}

		int bytesPerPixel = getBytesPerPixel(format);
		if (img.length < w * h * bytesPerPixel)
		{
			throw new IllegalArgumentException();
		}

		for (int row = 0; row < h; row++)
		{
			int srcOffset = row * w * bytesPerPixel;
			int dstOffset = ((y + row) * width + x) * bytesPerPixel;
			System.arraycopy(img, srcOffset, image, dstOffset, w * bytesPerPixel);
		}
	}

	byte[] getImageData()
	{
		return image;
	}

	private static void validateFormat(int fmt)
	{
		if (fmt < ALPHA || fmt > RGBA)
		{
			throw new IllegalArgumentException();
		}
	}

	private static int getBytesPerPixel(int fmt)
	{
		switch (fmt)
		{
			case ALPHA:
			case LUMINANCE:
				return 1;
			case LUMINANCE_ALPHA:
				return 2;
			case RGB:
				return 3;
			case RGBA:
				return 4;
			default:
				throw new IllegalArgumentException();
		}
	}

	private static byte[] convertImage(Image source, int fmt)
	{
		int width = source.getWidth();
		int height = source.getHeight();
		int[] argb = new int[width * height];
		source.getRGB(argb, 0, width, 0, 0, width, height);
		byte[] data = new byte[argb.length * getBytesPerPixel(fmt)];
		int out = 0;
		for (int i = 0; i < argb.length; i++)
		{
			int pixel = argb[i];
			int a = (pixel >>> 24) & 0xFF;
			int r = (pixel >>> 16) & 0xFF;
			int g = (pixel >>> 8) & 0xFF;
			int b = pixel & 0xFF;
			int luminance = (r + g + b) / 3;
			switch (fmt)
			{
				case ALPHA:
					data[out++] = (byte) a;
					break;
				case LUMINANCE:
					data[out++] = (byte) luminance;
					break;
				case LUMINANCE_ALPHA:
					data[out++] = (byte) luminance;
					data[out++] = (byte) a;
					break;
				case RGB:
					data[out++] = (byte) r;
					data[out++] = (byte) g;
					data[out++] = (byte) b;
					break;
				case RGBA:
					data[out++] = (byte) r;
					data[out++] = (byte) g;
					data[out++] = (byte) b;
					data[out++] = (byte) a;
					break;
				default:
					throw new IllegalArgumentException();
			}
		}
		return data;
	}
}
