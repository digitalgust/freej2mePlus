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

public abstract class Node extends Transformable
{

	public static final int NONE  = 144;
	public static final int ORIGIN  = 145;
	public static final int X_AXIS  = 146;
	public static final int Y_AXIS  = 147;
	public static final int Z_AXIS  = 148;


	private Node alignRef;
	private Node yAlignRef;
	private float alphaFactor = 1f;
	private boolean picking = true;
	private boolean rendering = true;
	private int scope = -1;
	private Node parent;
	private int zTarget = NONE;
	private int yTarget = NONE;


	public void align(Node reference) {  }

	public Node getAlignmentReference(int axis)
	{
		checkAxis(axis);
		return axis == Z_AXIS ? alignRef : yAlignRef;
	}

	public int getAlignmentTarget(int axis)
	{
		checkAxis(axis);
		return axis == Z_AXIS ? zTarget : yTarget;
	}

	public float getAlphaFactor() { return alphaFactor; }

	public Node getParent() { return parent; }

	public int getScope() { return scope; }

	public boolean getTransformTo(Node target, Transform transform)
	{
		if (target == null || transform == null)
		{
			throw new NullPointerException();
		}
		if (getRoot(this) != getRoot(target))
		{
			return false;
		}

		Transform source = new Transform();
		Transform destination = new Transform();
		getCompositeTransform(source);
		target.getCompositeTransform(destination);
		destination.invert();
		destination.postMultiply(source);
		transform.set(destination);
		return true;
	}

	public boolean isPickingEnabled() { return picking; }

	public boolean isRenderingEnabled() { return rendering; }

	public void setAlignment(Node zRef, int zTarget, Node yRef, int yTarget)
	{
		checkTarget(zTarget);
		checkTarget(yTarget);
		if (zRef == this || yRef == this)
		{
			throw new IllegalArgumentException();
		}
		if (zRef == yRef && zRef != null && zTarget == yTarget && zTarget != NONE)
		{
			throw new IllegalArgumentException();
		}

		alignRef = zRef;
		this.zTarget = zTarget;
		yAlignRef = yRef;
		this.yTarget = yTarget;
	}

	public void setAlphaFactor(float value)
	{
		if (value < 0f || value > 1f)
		{
			throw new IllegalArgumentException();
		}
		alphaFactor = value;
	}

	public void setPickingEnable(boolean enable) { picking = enable; }

	public void setRenderingEnable(boolean enable) { rendering = enable; }

	public void setScope(int value) { scope = value; }

	void setParent(Node parent)
	{
		this.parent = parent;
	}

	private static Node getRoot(Node node)
	{
		Node current = node;
		while (current.getParent() != null)
		{
			current = current.getParent();
		}
		return current;
	}

	private static void checkAxis(int axis)
	{
		if (axis != Y_AXIS && axis != Z_AXIS)
		{
			throw new IllegalArgumentException();
		}
	}

	private static void checkTarget(int target)
	{
		if (target != NONE && target != ORIGIN && target != X_AXIS && target != Y_AXIS && target != Z_AXIS)
		{
			throw new IllegalArgumentException();
		}
	}

}
