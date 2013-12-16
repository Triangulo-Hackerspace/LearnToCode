package br.net.triangulohackerspace.learntocode;

import java.util.ArrayList;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.ButtonSprite.OnClickListener;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.color.Color;
import org.andengine.util.debug.Debug;

import android.widget.Toast;


public class MainActivity extends SimpleBaseGameActivity implements OnClickListener{

	// ===========================================================
		// Constants
		// ===========================================================

		private static final int CAMERA_WIDTH = 480;
		private static final int CAMERA_HEIGHT = 720;

		// ===========================================================
		// Fields
		// ===========================================================

		private BuildableBitmapTextureAtlas mBitmapTextureAtlas;
		private ITextureRegion emFrenteTextureRegion;
		private ITextureRegion virarEsquerdaTextureRegion;
		private ITextureRegion virarDireitaTextureRegion;
		private ITextureRegion voltarTextureRegion;
		private ITextureRegion executarTextureRegion;
		
		ButtonSprite emFrente;
		ButtonSprite virarEsquerda;
		ButtonSprite virarDireita;
		ButtonSprite voltar;
		ButtonSprite executar;
		
		Scene scene;
		
		
		ArrayList<Acao> acoes;

		// ===========================================================
		// Constructors
		// ===========================================================

		// ===========================================================
		// Getter & Setter
		// ===========================================================

		// ===========================================================
		// Methods for/from SuperClass/Interfaces
		// ===========================================================

		@Override
		public EngineOptions onCreateEngineOptions() {
			final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

			return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
		}

		@Override
		public void onCreateResources() {
			BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

			this.mBitmapTextureAtlas = new BuildableBitmapTextureAtlas(this.getTextureManager(), 512, 512);
			this.emFrenteTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "emfrente.png");
			this.virarEsquerdaTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "viraresquerda.png");
			this.virarDireitaTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "virardireita.png");
			this.voltarTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "voltar.png");
			this.executarTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "executar.png");
			
			try {
				this.mBitmapTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 0, 0));
				this.mBitmapTextureAtlas.load();
			} catch (TextureAtlasBuilderException e) {
				Debug.e(e);
			}
			
			acoes = new ArrayList<Acao>();
		}

		@Override
		public Scene onCreateScene() {
			this.mEngine.registerUpdateHandler(new FPSLogger());

			scene = new Scene();
			scene.setBackground(new Background(Color.WHITE));

			/* Calculate the coordinates for the face, so its centered on the camera. */
			final float centerX = (CAMERA_WIDTH - this.emFrenteTextureRegion.getWidth()) / 2;
			final float centerY = (CAMERA_HEIGHT - this.emFrenteTextureRegion.getHeight()) / 2;

			/* Create the button and add it to the scene. */
			emFrente = new ButtonSprite((this.emFrenteTextureRegion.getWidth()*0.25F), CAMERA_HEIGHT-(100), this.emFrenteTextureRegion, this.virarEsquerdaTextureRegion, this.virarDireitaTextureRegion, this.getVertexBufferObjectManager(), this);
			virarEsquerda = new ButtonSprite((emFrente.getX()+emFrente.getWidth()+10), CAMERA_HEIGHT-(100), this.virarEsquerdaTextureRegion, this.emFrenteTextureRegion, this.virarDireitaTextureRegion, this.getVertexBufferObjectManager(), this);
			virarDireita = new ButtonSprite((virarEsquerda.getX()+virarEsquerda.getWidth()+10), CAMERA_HEIGHT-(100), this.virarDireitaTextureRegion, this.emFrenteTextureRegion, this.virarDireitaTextureRegion, this.getVertexBufferObjectManager(), this);
			voltar = new ButtonSprite((this.emFrenteTextureRegion.getWidth()*0.25F), CAMERA_HEIGHT-(50), this.voltarTextureRegion, this.emFrenteTextureRegion, this.virarDireitaTextureRegion, this.getVertexBufferObjectManager(), this);
			executar = new ButtonSprite((voltar.getX()+voltar.getWidth()+10), CAMERA_HEIGHT-(50), this.executarTextureRegion, this.emFrenteTextureRegion, this.virarDireitaTextureRegion, this.getVertexBufferObjectManager(), this);
				
			scene.registerTouchArea(emFrente);
			scene.attachChild(emFrente);
			
			scene.registerTouchArea(virarEsquerda);
			scene.attachChild(virarEsquerda);
			
			scene.registerTouchArea(virarDireita);
			scene.attachChild(virarDireita);
			
			scene.registerTouchArea(voltar);
			scene.attachChild(voltar);
			
			scene.registerTouchArea(executar);
			scene.attachChild(executar);
			
			scene.setTouchAreaBindingOnActionDownEnabled(true);

			return scene;

		}
		
		
		@Override
		public void onClick(final ButtonSprite pButtonSprite, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					
					final float centerX = (CAMERA_WIDTH - emFrenteTextureRegion.getWidth()) / 2;
					
					if(pButtonSprite == (ButtonSprite)scene.getChildByIndex(0)){ // em frente
						Acao sprite = new Acao(centerX, getNextSpriteHeight(), emFrenteTextureRegion, getVertexBufferObjectManager(), "EMFRENTE");
		                scene.attachChild(sprite);		                
		                acoes.add(sprite);
					} else if ((pButtonSprite == (ButtonSprite)scene.getChildByIndex(1))){ // virar esquerda
					    Acao sprite = new Acao(centerX, getNextSpriteHeight(), virarEsquerdaTextureRegion, getVertexBufferObjectManager(), "VIRARESQUERDA");
		                scene.attachChild(sprite);		                
		                acoes.add(sprite);
					} else if ((pButtonSprite == (ButtonSprite)scene.getChildByIndex(2))){ //virar direita
					    Acao sprite = new Acao(centerX, getNextSpriteHeight(), virarDireitaTextureRegion, getVertexBufferObjectManager(), "VIRARDIREITA");
		                scene.attachChild(sprite);		                
		                acoes.add(sprite);
					} else if ((pButtonSprite == (ButtonSprite)scene.getChildByIndex(3))){ //voltar
						if(!acoes.isEmpty()){
							Acao acao = acoes.get(acoes.size()-1);
							acoes.remove(acao);
							scene.detachChild(acao);
						}
						else if ((pButtonSprite == (ButtonSprite)scene.getChildByIndex(4))){ //voltar
							//TODO: Implementar a execucao
						}
					}
				}
			});
		}

		// ===========================================================
		// Methods
		// ===========================================================
		
		private float getNextSpriteHeight() {
			float init = 5;
		if(acoes.isEmpty()){
			return init;
		} else {
			float position = init;
			for (Sprite sp : acoes) {
				position = position + sp.getHeight()-1;
			}
			return position;
		}
	}

		// ===========================================================
		// Inner and Anonymous Classes
		// ===========================================================

}
