package br.net.triangulohackerspace.learntocode;

import java.util.ArrayList;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.entity.scene.menu.item.TextMenuItem;
import org.andengine.entity.scene.menu.item.decorator.ColorMenuItemDecorator;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.ButtonSprite.OnClickListener;
import org.andengine.entity.util.FPSLogger;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
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

import android.opengl.GLES20;
import android.view.KeyEvent;
import android.view.Menu;


public class MainActivity extends SimpleBaseGameActivity implements OnClickListener, IOnMenuItemClickListener{

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
		
		protected static final int MENU_RESET = 0;
		protected static final int MENU_QUIT = MENU_RESET + 1;
		
		
		ArrayList<Acao> acoes;
		
		protected Menu mMenu;
		
		private Camera mCamera;
		
		private Font mFont;

		protected Scene mMainScene;
		protected MenuScene mMenuScene;


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
			this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

			return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);
		}

		@Override
		public void onCreateResources() {
			FontFactory.setAssetBasePath("font/");
			
			final ITexture fontTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
			this.mFont = FontFactory.createFromAsset(this.getFontManager(), fontTexture, this.getAssets(), "Plok.ttf", 48, true, android.graphics.Color.WHITE);
			this.mFont.load();

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
			
			this.mMenuScene = this.createMenuScene();

			this.mMainScene = new Scene();
			this.mMainScene.setBackground(new Background(Color.WHITE));

			/* Calculate the coordinates for the face, so its centered on the camera. */
			final float centerX = (CAMERA_WIDTH - this.emFrenteTextureRegion.getWidth()) / 2;
			final float centerY = (CAMERA_HEIGHT - this.emFrenteTextureRegion.getHeight()) / 2;

			/* Create the button and add it to the . */
			emFrente = new ButtonSprite((this.emFrenteTextureRegion.getWidth()*0.25F), CAMERA_HEIGHT-(100), this.emFrenteTextureRegion, this.virarEsquerdaTextureRegion, this.virarDireitaTextureRegion, this.getVertexBufferObjectManager(), this);
			virarEsquerda = new ButtonSprite((emFrente.getX()+emFrente.getWidth()+10), CAMERA_HEIGHT-(100), this.virarEsquerdaTextureRegion, this.emFrenteTextureRegion, this.virarDireitaTextureRegion, this.getVertexBufferObjectManager(), this);
			virarDireita = new ButtonSprite((virarEsquerda.getX()+virarEsquerda.getWidth()+10), CAMERA_HEIGHT-(100), this.virarDireitaTextureRegion, this.emFrenteTextureRegion, this.virarDireitaTextureRegion, this.getVertexBufferObjectManager(), this);
			voltar = new ButtonSprite((this.emFrenteTextureRegion.getWidth()*0.25F), CAMERA_HEIGHT-(50), this.voltarTextureRegion, this.emFrenteTextureRegion, this.virarDireitaTextureRegion, this.getVertexBufferObjectManager(), this);
			executar = new ButtonSprite((voltar.getX()+voltar.getWidth()+10), CAMERA_HEIGHT-(50), this.executarTextureRegion, this.emFrenteTextureRegion, this.virarDireitaTextureRegion, this.getVertexBufferObjectManager(), this);
				
			this.mMainScene.registerTouchArea(emFrente);
			this.mMainScene.attachChild(emFrente);
			
			this.mMainScene.registerTouchArea(virarEsquerda);
			this.mMainScene.attachChild(virarEsquerda);
			
			this.mMainScene.registerTouchArea(virarDireita);
			this.mMainScene.attachChild(virarDireita);
			
			this.mMainScene.registerTouchArea(voltar);
			this.mMainScene.attachChild(voltar);
			
			this.mMainScene.registerTouchArea(executar);
			this.mMainScene.attachChild(executar);
			
			this.mMainScene.setTouchAreaBindingOnActionDownEnabled(true);

			return this.mMainScene;

		}
		
		
		@Override
		public void onClick(final ButtonSprite pButtonSprite, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					
					final float centerX = (CAMERA_WIDTH - emFrenteTextureRegion.getWidth()) / 2;
					
					if(pButtonSprite == (ButtonSprite)mMainScene.getChildByIndex(0)){ // em frente
						Acao sprite = new Acao(centerX, getNextSpriteHeight(), emFrenteTextureRegion, getVertexBufferObjectManager(), "EMFRENTE");
						mMainScene.attachChild(sprite);		                
		                acoes.add(sprite);
					} else if ((pButtonSprite == (ButtonSprite)mMainScene.getChildByIndex(1))){ // virar esquerda
					    Acao sprite = new Acao(centerX, getNextSpriteHeight(), virarEsquerdaTextureRegion, getVertexBufferObjectManager(), "VIRARESQUERDA");
					    mMainScene.attachChild(sprite);		                
		                acoes.add(sprite);
					} else if ((pButtonSprite == (ButtonSprite)mMainScene.getChildByIndex(2))){ //virar direita
					    Acao sprite = new Acao(centerX, getNextSpriteHeight(), virarDireitaTextureRegion, getVertexBufferObjectManager(), "VIRARDIREITA");
					    mMainScene.attachChild(sprite);		                
		                acoes.add(sprite);
					} else if ((pButtonSprite == (ButtonSprite)mMainScene.getChildByIndex(3))){ //voltar
						if(!acoes.isEmpty()){
							Acao acao = acoes.get(acoes.size()-1);
							acoes.remove(acao);
							mMainScene.detachChild(acao);
						}
						else if ((pButtonSprite == (ButtonSprite)mMainScene.getChildByIndex(4))){ //voltar
							//TODO: Implementar a execucao
						}
					}
				}
			});
		}

		
		@Override
		public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
			if(pKeyCode == KeyEvent.KEYCODE_MENU && pEvent.getAction() == KeyEvent.ACTION_DOWN) {
				if(this.mMainScene.hasChildScene()) {
					/* Remove the menu and reset it. */
					this.mMenuScene.back();
				} else {
					/* Attach the menu. */
					this.mMainScene.setChildScene(this.mMenuScene, false, true, true);
				}
				return true;
			} else {
				return super.onKeyDown(pKeyCode, pEvent);
			}
		}

		@Override
		public boolean onMenuItemClicked(final MenuScene pMenuScene, final IMenuItem pMenuItem, final float pMenuItemLocalX, final float pMenuItemLocalY) {
			switch(pMenuItem.getID()) {
				case MENU_RESET:
					/* Restart the animation. */
					this.mMainScene.reset();

					/* Remove the menu and reset it. */
					this.mMainScene.clearChildScene();
					this.mMenuScene.reset();
					return true;
				case MENU_QUIT:
					/* End Activity. */
					this.finish();
					return true;
				default:
					return false;
			}
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
		
		protected MenuScene createMenuScene() {
			final MenuScene menuScene = new MenuScene(this.mCamera);

			final IMenuItem resetMenuItem = new ColorMenuItemDecorator(new TextMenuItem(MENU_RESET, this.mFont, "RESET", this.getVertexBufferObjectManager()), new Color(1,0,0), new Color(0,0,0));
			resetMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
			menuScene.addMenuItem(resetMenuItem);

			final IMenuItem quitMenuItem = new ColorMenuItemDecorator(new TextMenuItem(MENU_QUIT, this.mFont, "QUIT", this.getVertexBufferObjectManager()), new Color(1,0,0), new Color(0,0,0));
			quitMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
			menuScene.addMenuItem(quitMenuItem);

			menuScene.buildAnimations();

			menuScene.setBackgroundEnabled(false);

			menuScene.setOnMenuItemClickListener(this);
			return menuScene;
		}

		// ===========================================================
		// Inner and Anonymous Classes
		// ===========================================================

}
