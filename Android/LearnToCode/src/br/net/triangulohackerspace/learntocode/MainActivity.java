package br.net.triangulohackerspace.learntocode;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

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

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.opengl.GLES20;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends SimpleBaseGameActivity implements
		OnClickListener, IOnMenuItemClickListener {

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
	ButtonSprite menu;

	ButtonSprite posicaoAtual;

	private static final int MENU_BLUETOOTH = 0;
	protected static final int MENU_RESET = 1;
	protected static final int MENU_QUIT = 2;

	private static final int REQUESTCODE_BLUETOOTH_CONNECT = 1;
	private static final String TAG_BT = "bluetooth1";
	private BluetoothAdapter btAdapter = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null;
	private String address;

	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

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

		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED,
				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);
	}

	@Override
	public void onCreateResources() {

		btAdapter = BluetoothAdapter.getDefaultAdapter();
		checkBTState();

		FontFactory.setAssetBasePath("font/");

		final ITexture fontTexture = new BitmapTextureAtlas(
				this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		this.mFont = FontFactory.createFromAsset(this.getFontManager(),
				fontTexture, this.getAssets(), "Plok.ttf", 48, true,
				android.graphics.Color.WHITE);
		this.mFont.load();

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		this.mBitmapTextureAtlas = new BuildableBitmapTextureAtlas(
				this.getTextureManager(), 512, 512);
		this.emFrenteTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this, "emfrente.png");
		this.virarEsquerdaTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this,
						"viraresquerda.png");
		this.virarDireitaTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this,
						"virardireita.png");
		this.voltarTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this, "voltar.png");
		this.executarTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this, "executar.png");

		try {
			this.mBitmapTextureAtlas
					.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(
							0, 0, 0));
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

		/*
		 * Calculate the coordinates for the face, so its centered on the
		 * camera.
		 */
		final float centerX = (CAMERA_WIDTH - this.emFrenteTextureRegion
				.getWidth()) / 2;
		final float centerY = (CAMERA_HEIGHT - this.emFrenteTextureRegion
				.getHeight()) / 2;

		/* Create the button and add it to the . */
		emFrente = new ButtonSprite(
				(this.emFrenteTextureRegion.getWidth() * 0.25F),
				CAMERA_HEIGHT - (100), this.emFrenteTextureRegion,
				this.virarEsquerdaTextureRegion,
				this.virarDireitaTextureRegion,
				this.getVertexBufferObjectManager(), this);
		virarEsquerda = new ButtonSprite(
				(emFrente.getX() + emFrente.getWidth() + 10),
				CAMERA_HEIGHT - (100), this.virarEsquerdaTextureRegion,
				this.emFrenteTextureRegion, this.virarDireitaTextureRegion,
				this.getVertexBufferObjectManager(), this);
		virarDireita = new ButtonSprite((virarEsquerda.getX()
				+ virarEsquerda.getWidth() + 10), CAMERA_HEIGHT - (100),
				this.virarDireitaTextureRegion, this.emFrenteTextureRegion,
				this.virarDireitaTextureRegion,
				this.getVertexBufferObjectManager(), this);
		voltar = new ButtonSprite(
				(this.emFrenteTextureRegion.getWidth() * 0.25F),
				CAMERA_HEIGHT - (50), this.voltarTextureRegion,
				this.emFrenteTextureRegion, this.virarDireitaTextureRegion,
				this.getVertexBufferObjectManager(), this);
		executar = new ButtonSprite((voltar.getX() + voltar.getWidth() + 10),
				CAMERA_HEIGHT - (50), this.executarTextureRegion,
				this.emFrenteTextureRegion, this.virarDireitaTextureRegion,
				this.getVertexBufferObjectManager(), this);
		menu = new ButtonSprite((executar.getX() + executar.getWidth() + 10),
				CAMERA_HEIGHT - (50), this.executarTextureRegion,
				this.emFrenteTextureRegion, this.virarDireitaTextureRegion,
				this.getVertexBufferObjectManager(), this);

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

		this.mMainScene.registerTouchArea(menu);
		this.mMainScene.attachChild(menu);

		this.mMainScene.setTouchAreaBindingOnActionDownEnabled(true);

		return this.mMainScene;

	}

	@Override
	public void onClick(final ButtonSprite pButtonSprite,
			final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {

				final float centerX = (CAMERA_WIDTH - emFrenteTextureRegion
						.getWidth()) / 2;

				if (pButtonSprite == (ButtonSprite) mMainScene
						.getChildByIndex(0)) { // em frente
					Acao sprite = new Acao(centerX, getNextSpriteHeight(),
							emFrenteTextureRegion,
							getVertexBufferObjectManager(), TipoAcao.EMFRENTE);
					mMainScene.attachChild(sprite);
					acoes.add(sprite);
				} else if ((pButtonSprite == (ButtonSprite) mMainScene
						.getChildByIndex(1))) { // virar esquerda
					Acao sprite = new Acao(centerX, getNextSpriteHeight(),
							virarEsquerdaTextureRegion,
							getVertexBufferObjectManager(),
							TipoAcao.VIRARESQUEDA);
					mMainScene.attachChild(sprite);
					acoes.add(sprite);
				} else if ((pButtonSprite == (ButtonSprite) mMainScene
						.getChildByIndex(2))) { // virar direita
					Acao sprite = new Acao(centerX, getNextSpriteHeight(),
							virarDireitaTextureRegion,
							getVertexBufferObjectManager(),
							TipoAcao.VIRARDIREITA);
					mMainScene.attachChild(sprite);
					acoes.add(sprite);
				} else if ((pButtonSprite == (ButtonSprite) mMainScene
						.getChildByIndex(3))) { // voltar
					if (!acoes.isEmpty()) {
						Acao acao = acoes.get(acoes.size() - 1);
						acoes.remove(acao);
						mMainScene.detachChild(acao);
					}
				} else if ((pButtonSprite == (ButtonSprite) mMainScene
						.getChildByIndex(4))) { // executar
					executarAcoes();
				} else if ((pButtonSprite == (ButtonSprite) mMainScene
						.getChildByIndex(5))) { // menu
					if (mMainScene.hasChildScene()) {
						mMenuScene.back();
					} else {
						mMainScene.setChildScene(mMenuScene, false, true, true);
					}
				}
			}

			private void executarAcoes() {
				if (acoes == null || acoes.isEmpty()) {
					return;
				}

				ButtonSprite posicao = new ButtonSprite((acoes.get(0)
						.getWidth() * 0.25F), acoes.get(0).getHeight(),
						voltarTextureRegion, voltarTextureRegion,
						voltarTextureRegion, getVertexBufferObjectManager(),
						MainActivity.this);
				mMainScene.attachChild(posicao);
				for (Acao acao : acoes) {
					posicao.setPosition(acao.getX() + acao.getWidth(),
							acao.getY());
					String dados = new String();
					if (acao.getTipoAcao().equals(TipoAcao.EMFRENTE)) {
						dados = "F:"; // go foward
					} else if (acao.getTipoAcao().equals(TipoAcao.VIRARESQUEDA)) {
						dados = "L:"; // turn left
					} else if (acao.getTipoAcao().equals(TipoAcao.VIRARDIREITA)) {
						dados = "R:"; // turn right
					}
					
					if(!sendData(dados))
						break; 
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
					}

				}
				mMainScene.detachChild(posicao);
			}

			private boolean sendData(String message) {
				try {
					if(btSocket == null){
						throw new IOException();
					}
					
					btSocket.connect();
					
					byte[] msgBuffer = message.getBytes();
					Log.d(TAG_BT, "...Sending data " + message + "...");
					outStream.write(msgBuffer);
					return true;
				} catch (IOException e) {
					String msg = "Nao foi possivel enviar os dados, bluetooth nao pareado!";
					
					Toast.makeText(getBaseContext(), "sendData()" + " - " + msg,
							Toast.LENGTH_LONG).show();
					Log.e(TAG_BT, "sendData()" + " - " + msg);
					return false;
				}
			}
		});
	}

	@Override
	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
		if (pKeyCode == KeyEvent.KEYCODE_MENU
				&& pEvent.getAction() == KeyEvent.ACTION_DOWN) {
			if (this.mMainScene.hasChildScene()) {
				/* Remove the menu and reset it. */
				this.mMenuScene.back();
			} else {
				/* Attach the menu. */
				this.mMainScene.setChildScene(this.mMenuScene, false, true,
						true);
			}
			return true;
		} else {
			return super.onKeyDown(pKeyCode, pEvent);
		}
	}

	@Override
	public boolean onMenuItemClicked(final MenuScene pMenuScene,
			final IMenuItem pMenuItem, final float pMenuItemLocalX,
			final float pMenuItemLocalY) {
		switch (pMenuItem.getID()) {
		case MENU_BLUETOOTH:
			final Intent intent = new Intent(MainActivity.this,
					BluetoothListDevicesActivity.class);
			MainActivity.this.startActivityForResult(intent,
					REQUESTCODE_BLUETOOTH_CONNECT);
			this.mMainScene.clearChildScene();
			return true;
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
		if (acoes.isEmpty()) {
			return init;
		} else {
			float position = init;
			for (Sprite sp : acoes) {
				position = position + sp.getHeight() - 1;
			}
			return position;
		}
	}

	protected MenuScene createMenuScene() {
		final MenuScene menuScene = new MenuScene(this.mCamera);

		final IMenuItem bluetoothMenuItem = new ColorMenuItemDecorator(
				new TextMenuItem(MENU_BLUETOOTH, this.mFont, "Bluetooth",
						this.getVertexBufferObjectManager()),
				new Color(1, 0, 0), new Color(0, 0, 0));
		bluetoothMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA,
				GLES20.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(bluetoothMenuItem);

		final IMenuItem resetMenuItem = new ColorMenuItemDecorator(
				new TextMenuItem(MENU_RESET, this.mFont, "RESET",
						this.getVertexBufferObjectManager()),
				new Color(1, 0, 0), new Color(0, 0, 0));
		resetMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA,
				GLES20.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(resetMenuItem);

		final IMenuItem quitMenuItem = new ColorMenuItemDecorator(
				new TextMenuItem(MENU_QUIT, this.mFont, "QUIT",
						this.getVertexBufferObjectManager()),
				new Color(1, 0, 0), new Color(0, 0, 0));
		quitMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA,
				GLES20.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(quitMenuItem);

		menuScene.buildAnimations();
		menuScene.setBackgroundEnabled(false);
		menuScene.setOnMenuItemClickListener(this);
		return menuScene;
	}

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {
		Log.i(TAG_BT, "onActivityResult");
		if (requestCode == REQUESTCODE_BLUETOOTH_CONNECT
				&& resultCode == RESULT_OK && data != null) {
			String macAddress = data.getStringExtra("BLUETOOTH_MAC");
			Log.d(TAG_BT, "Selecionado de volta: " + macAddress);
			this.address = macAddress;
			conectarBTDevice();
			return;
		}
	}

	@SuppressLint("NewApi")
	private void conectarBTDevice() {

		if (btSocket != null && btSocket.isConnected())
			return;

		Log.d(TAG_BT, "Conectando a: " + address);
		// Set up a pointer to the remote node using it's address.
		BluetoothDevice device = btAdapter.getRemoteDevice(address);

		// Two things are needed to make a connection:
		// A MAC address, which we got above.
		// A Service ID or UUID. In this case we are using the
		// UUID for SPP.

		try {
			btSocket = createBluetoothSocket(device);
		} catch (IOException e1) {
			errorExit("Fatal Error", "In onResume() and socket create failed: "
					+ e1.getMessage() + ".");
		}

		// Discovery is resource intensive. Make sure it isn't going on
		// when you attempt to connect and pass your message.
		btAdapter.cancelDiscovery();

		// Establish the connection. This will block until it connects.
		Log.d(TAG_BT, "...Connecting...");
		try {
			btSocket.connect();
			Log.d(TAG_BT, "...Connection ok...");
			Toast.makeText(this, "Bluetooth Conectado", Toast.LENGTH_SHORT)
					.show();

		} catch (IOException e) {

			try {
				btSocket.close();
				Toast.makeText(this, "Bluetooth NAO Conectado",
						Toast.LENGTH_LONG).show();

				Log.d(TAG_BT, "Noo houve conexao com: " + address);
			} catch (IOException e2) {
				errorExit("Fatal Error",
						"In onResume() and unable to close socket during connection failure"
								+ e2.getMessage() + ".");
			}
		}

		// Create a data stream so we can talk to server.
		Log.d(TAG_BT, "...Create Socket...");

		try {
			outStream = btSocket.getOutputStream();
		} catch (IOException e) {
			errorExit(
					"Fatal Error",
					"In onResume() and output stream creation failed:"
							+ e.getMessage() + ".");
		}
	}

	private BluetoothSocket createBluetoothSocket(BluetoothDevice device)
			throws IOException {
		if (Build.VERSION.SDK_INT >= 10) {
			try {
				final Method m = device.getClass().getMethod(
						"createInsecureRfcommSocketToServiceRecord",
						new Class[] { UUID.class });
				return (BluetoothSocket) m.invoke(device, MY_UUID);
			} catch (Exception e) {
				Log.e(TAG_BT, "Could not create Insecure RFComm Connection", e);
			}
		}
		return device.createRfcommSocketToServiceRecord(MY_UUID);
	}

	private void checkBTState() {
		if (btAdapter == null) {
			errorExit("Fatal Error", "Bluetooth not support");
		} else {
			if (btAdapter.isEnabled()) {
				Log.d(TAG_BT, "...Bluetooth ON...");
			} else {
				// Prompt user to turn on Bluetooth
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, 1);
			}
		}
	}

	private void errorExit(String title, String message) {
		Toast.makeText(getBaseContext(), title + " - " + message,
				Toast.LENGTH_LONG).show();
		Log.e(TAG_BT, title + " - " + message);
		finish();
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
