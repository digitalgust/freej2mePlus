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

public class Mesh extends Node
{
	private Appearance[] appearances;
	private IndexBuffer[] indexbuffers;
	private VertexBuffer vertexbuffer;


	public Mesh() { /* DELETE THIS */ }

	public Mesh(VertexBuffer vertices, IndexBuffer[] submeshes, Appearance[] appearances)
	{
		if (vertices == null || submeshes == null || appearances == null)
		{
			throw new NullPointerException();
		}
		if (submeshes.length == 0 || submeshes.length != appearances.length)
		{
			throw new IllegalArgumentException();
		}

		this.vertexbuffer = vertices;
		this.indexbuffers = new IndexBuffer[submeshes.length];
		this.appearances = new Appearance[appearances.length];
		System.arraycopy(submeshes, 0, this.indexbuffers, 0, submeshes.length);
		System.arraycopy(appearances, 0, this.appearances, 0, appearances.length);
	}

	public Mesh(VertexBuffer vertices, IndexBuffer submesh, Appearance appearance)
	{
		if (vertices == null || submesh == null || appearance == null)
		{
			throw new NullPointerException();
		}
		this.vertexbuffer = vertices;
		this.indexbuffers = new IndexBuffer[] { submesh };
		this.appearances = new Appearance[] { appearance };
	}

	public Appearance getAppearance(int index) { return appearances[index]; }

	public IndexBuffer getIndexBuffer(int index) { return indexbuffers[index]; }

	public int getSubmeshCount() { return indexbuffers != null ? indexbuffers.length : 0; }

	public VertexBuffer getVertexBuffer() { return vertexbuffer; }

	public void setAppearance(int index, Appearance a) { appearances[index] = a; }

}
