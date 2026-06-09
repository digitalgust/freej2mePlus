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

public abstract class Transformable extends Object3D
{
	private final Transform transform = new Transform();

	public void getCompositeTransform(Transform transform)
	{
		if (transform == null)
		{
			throw new NullPointerException();
		}
		transform.setIdentity();
		appendCompositeTransform(transform, this);
	}

	public void getOrientation(float[] angleAxis)
	{
		if (angleAxis == null)
		{
			throw new NullPointerException();
		}
		if (angleAxis.length < 4)
		{
			throw new IllegalArgumentException();
		}

		float[] matrix = transform.getMatrix();
		float sx = getAxisLength(matrix[0], matrix[4], matrix[8]);
		float sy = getAxisLength(matrix[1], matrix[5], matrix[9]);
		float sz = getAxisLength(matrix[2], matrix[6], matrix[10]);
		if (sx == 0f || sy == 0f || sz == 0f)
		{
			angleAxis[0] = 0f;
			angleAxis[1] = 0f;
			angleAxis[2] = 0f;
			angleAxis[3] = 1f;
			return;
		}

		float r00 = matrix[0] / sx;
		float r11 = matrix[5] / sy;
		float r22 = matrix[10] / sz;
		float trace = r00 + r11 + r22;
		float cos = (trace - 1f) * 0.5f;
		if (cos > 1f) { cos = 1f; }
		if (cos < -1f) { cos = -1f; }
		float angle = (float) Math.toDegrees(Math.acos(cos));
		if (Math.abs(angle) < 1.0e-6f)
		{
			angleAxis[0] = 0f;
			angleAxis[1] = 0f;
			angleAxis[2] = 0f;
			angleAxis[3] = 1f;
			return;
		}

		float rx = (matrix[9] / sz - matrix[6] / sy);
		float ry = (matrix[2] / sx - matrix[8] / sz);
		float rz = (matrix[4] / sy - matrix[1] / sx);
		float length = getAxisLength(rx, ry, rz);
		if (length == 0f)
		{
			angleAxis[0] = angle;
			angleAxis[1] = 0f;
			angleAxis[2] = 0f;
			angleAxis[3] = 1f;
			return;
		}

		angleAxis[0] = angle;
		angleAxis[1] = -rx / length;
		angleAxis[2] = -ry / length;
		angleAxis[3] = -rz / length;
	}

	public void getScale(float[] xyz)
	{
		if (xyz == null)
		{
			throw new NullPointerException();
		}
		if (xyz.length < 3)
		{
			throw new IllegalArgumentException();
		}

		float[] matrix = transform.getMatrix();
		xyz[0] = getAxisLength(matrix[0], matrix[4], matrix[8]);
		xyz[1] = getAxisLength(matrix[1], matrix[5], matrix[9]);
		xyz[2] = getAxisLength(matrix[2], matrix[6], matrix[10]);
	}

	public void getTransform(Transform transform)
	{
		if (transform == null)
		{
			throw new NullPointerException();
		}
		transform.set(this.transform);
	}

	public void getTranslation(float[] xyz)
	{
		if (xyz == null)
		{
			throw new NullPointerException();
		}
		if (xyz.length < 3)
		{
			throw new IllegalArgumentException();
		}

		float[] matrix = transform.getMatrix();
		xyz[0] = matrix[3];
		xyz[1] = matrix[7];
		xyz[2] = matrix[11];
	}

	public void postRotate(float angle, float ax, float ay, float az)
	{
		transform.postRotate(angle, ax, ay, az);
	}

	public void preRotate(float angle, float ax, float ay, float az)
	{
		Transform rotation = new Transform();
		rotation.postRotate(angle, ax, ay, az);
		rotation.postMultiply(transform);
		transform.set(rotation);
	}

	public void scale(float sx, float sy, float sz)
	{
		transform.postScale(sx, sy, sz);
	}

	public void setOrientation(float angle, float ax, float ay, float az)
	{
		float[] translation = new float[3];
		float[] scale = new float[3];
		getTranslation(translation);
		getScale(scale);

		transform.setIdentity();
		transform.postTranslate(translation[0], translation[1], translation[2]);
		transform.postRotate(angle, ax, ay, az);
		transform.postScale(scale[0], scale[1], scale[2]);
	}

	public void setScale(float sx, float sy, float sz)
	{
		float[] translation = new float[3];
		float[] orientation = new float[4];
		getTranslation(translation);
		getOrientation(orientation);

		transform.setIdentity();
		transform.postTranslate(translation[0], translation[1], translation[2]);
		transform.postRotate(orientation[0], orientation[1], orientation[2], orientation[3]);
		transform.postScale(sx, sy, sz);
	}

	public void setTransform(Transform transform)
	{
		if (transform == null)
		{
			throw new NullPointerException();
		}
		this.transform.set(transform);
	}

	public void setTranslation(float tx, float ty, float tz)
	{
		float[] matrix = transform.getMatrix();
		matrix[3] = tx;
		matrix[7] = ty;
		matrix[11] = tz;
	}

	public void translate(float tx, float ty, float tz)
	{
		float[] matrix = transform.getMatrix();
		matrix[3] += tx;
		matrix[7] += ty;
		matrix[11] += tz;
	}

	private static void appendCompositeTransform(Transform target, Transformable current)
	{
		if (current instanceof Node)
		{
			Node parent = ((Node) current).getParent();
			if (parent != null)
			{
				appendCompositeTransform(target, parent);
			}
		}

		Transform local = new Transform();
		current.getTransform(local);
		target.postMultiply(local);
	}

	private static float getAxisLength(float x, float y, float z)
	{
		return (float) Math.sqrt(x * x + y * y + z * z);
	}

}
