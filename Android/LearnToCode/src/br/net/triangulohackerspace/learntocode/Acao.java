package br.net.triangulohackerspace.learntocode;

import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

public class Acao extends Sprite{

	String tipoAcao;

	public Acao(float pX, float pY,
			ITextureRegion mFace1TextureRegion,
			VertexBufferObjectManager vertexBufferObjectManager, String tipoAcao) {
		super(pX, pY, mFace1TextureRegion, vertexBufferObjectManager);
		this.tipoAcao = tipoAcao;
		// TODO Auto-generated constructor stub
	}

}
