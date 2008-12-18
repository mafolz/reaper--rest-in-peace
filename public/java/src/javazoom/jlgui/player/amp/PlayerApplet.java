/*
 * PlayerApplet.
 *
 * JavaZOOM : jlgui@javazoom.net
 *            http://www.javazoom.net
 *
 *-----------------------------------------------------------------------
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */

package javazoom.jlgui.player.amp;

import java.applet.Applet;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.ImageIcon;

import javazoom.jlgui.basicplayer.AppletMpegSPIWorkaround;
import javazoom.jlgui.basicplayer.AppletVorbisSPIWorkaround;
import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerApplet;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import javazoom.jlgui.basicplayer.BasicPlayerListener;
import javazoom.jlgui.player.amp.equalizer.ui.EqualizerApplet;
import javazoom.jlgui.player.amp.playlist.BasePlaylist;
import javazoom.jlgui.player.amp.playlist.Playlist;
import javazoom.jlgui.player.amp.playlist.PlaylistFactory;
import javazoom.jlgui.player.amp.playlist.PlaylistItem;
import javazoom.jlgui.player.amp.playlist.ui.MP3FilesApplet;
import javazoom.jlgui.player.amp.skin.ActiveComponent;
import javazoom.jlgui.player.amp.skin.InvisibleActiveComponent;
import javazoom.jlgui.player.amp.skin.SkinComponent;
import javazoom.jlgui.player.amp.skin.SkinLoader;
import javazoom.jlgui.player.amp.skin.Taftb;
import javazoom.jlgui.player.amp.skin.UrlDialog;
import javazoom.jlgui.player.amp.util.Config;
import javazoom.jlgui.player.amp.util.FileSelector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * Player is the UI core of jlGui.
 * <br>
 * @author JavaZOOM
 */
public class PlayerApplet extends Applet implements ActionListener, BasicPlayerListener, DropTargetListener, WindowListener
{
  private static Log log = LogFactory.getLog(Player.class);

  protected static int TEXT_LENGTH_MAX = 30;
  protected static long SCROLL_PERIOD = 250;
  protected static String TITLETEXT = "jlGui Applet 2.3.2 ";
  protected static String initConfig = "jlgui.ini";
  protected static String initSong = null;
  protected static String showPlaylist = null;
  protected static String showEqualizer = null;
  protected static String skinVersion = "1";   // 1, 2, for different Volume.bmp
  protected static boolean autoRun = false;
  private MP3FilesApplet fileList = null;
  private EqualizerApplet equalizer = null;

  private String currentFileOrURL = null;
  private String currentSongName = null;
  private PlaylistItem currentPlaylistItem = null;
  private boolean currentIsFile;

  /*-- Window Parameters --*/
  private Image offScreenImage;
  private Graphics offScreenGraphics;
  private int WinWidth, WinHeight;
  private int OrigineX = 0, OrigineY = 0;
  private int screenWidth = -1, screenHeight = -1;
  private String thePath = "";
  private String theMain = "main.bmp";

  /*-- Buttons Panel members --*/
  private String theButtons = "cbuttons.bmp";
  private Image imMain,imButtons;
  private ActiveComponent acPrevious,acPlay,acPause,acStop,acNext,acEject;
  private Image imPrevious,imPlay,imPause,imStop,imNext,imEject;
  private Image[] releasedImage = {imPrevious, imPlay, imPause, imStop, imNext, imEject};
  private Image[] pressedImage = {imPrevious, imPlay, imPause, imStop, imNext, imEject};
  private int[] releasedPanel = {0, 0, 23, 18, 23, 0, 23, 18, 46, 0, 23, 18, 69, 0, 23, 18, 92, 0, 22, 18, 114, 0, 22, 16};
  private int[] pressedPanel = {0, 18, 23, 18, 23, 18, 23, 18, 46, 18, 23, 18, 69, 18, 23, 18, 92, 18, 22, 18, 114, 16, 22, 16};
  private int[] panelLocation = {16, 88, 39, 88, 62, 88, 85, 88, 108, 88, 137, 89};

  /*-- Title members --*/
  private boolean FirstDrag = true;
  private int XDrag = 0, YDrag = 0;
  private String theTitleBar = "titlebar.bmp";
  private Image imTitleBar;
  private ActiveComponent acTitleBar;
  private Image imTitleB;
  private Image[] releasedTitleIm = {imTitleB};
  private Image[] pressedTitleIm = {imTitleB};
  private int[] releasedTitlePanel = {27, 0, 264 - 20, 14}; // -20 for the two button add by me
  private int[] pressedTitlePanel = {27, 15, 264 - 20, 14};// -20 for the two button add by me
  private int[] titleBarLocation = {0, 0};

  /*-- Exit member --*/
  private ActiveComponent acExit;
  private int[] releasedExitPanel = {18, 0, 9, 9};
  private int[] pressedExitPanel = {18, 9, 9, 9};
  private Image[] releasedExitIm = {null};
  private Image[] pressedExitIm = {null};
  private int[] exitLocation = {264, 3};

  /*-- Minimize member --*/
  private ActiveComponent acMinimize;
  private int[] releasedMinimizePanel = {9, 0, 9, 9};
  private int[] pressedMinimizePanel = {9, 9, 9, 9};
  private Image[] releasedMinimizeIm = {null};
  private Image[] pressedMinimizeIm = {null};
  private int[] minimizeLocation = {244, 3};

  /*-- Text Members --*/
  private int fontWidth = 5;
  private int fontHeight = 6;
  private String theText = "text.bmp";
  private Image imText;
  private String fontIndex = "ABCDEFGHIJKLMNOPQRSTUVWXYZ\"@a  "
	  + "0123456789  :()-'!_+ /[]^&%.=$#"
	  + "   ?*";
  private Image sampleRateImage;
  private String sampleRateClearText = "  ";
  private Image sampleRateClearImage;
  private int[] sampleRateLocation = {156, 43};
  private String bitsRateClearText = "   ";
  private Image bitsRateClearImage;
  private Image bitsRateImage;
  private int[] bitsRateLocation = {110, 43};
  private String titleText = TITLETEXT.toUpperCase();
  private String clearText = "                                     ";
  private Image clearImage;
  private Image titleImage;
  private int[] titleLocation = {111, 27};
  private Image[] titleScrollImage = null;
  private int scrollIndex = 0;
  private long lastScrollTime = 0L;
  private boolean scrollRight = true;

  /*-- Numbers Members --*/
  private int numberWidth = 9;
  private int numberHeight = 13;
  private String theNumbers = "numbers.bmp";
  private String theNumEx = "nums_ex.bmp";
  private Image imNumbers;
  private String numberIndex = "0123456789 ";
  private Image minuteImage;
  private Image secondImage;
  private Image minuteDImage;
  private Image secondDImage;
  private Image[] timeImage = {null, null, null, null, null, null, null, null, null, null, null};
  private int[] minuteDLocation = {48, 26};
  private int[] minuteLocation = {60, 26};
  private int[] secondDLocation = {78, 26};
  private int[] secondLocation = {90, 26};

  /*-- Mono/Stereo Members --*/
  private String theMode = "monoster.bmp";
  private Image imMode;
  private int[] activeModePanel = {0, 0, 28, 12, 29, 0, 27, 12};
  private int[] passiveModePanel = {0, 12, 28, 12, 29, 12, 27, 12};
  private Image imSA,imMA,imSI,imMI;
  private Image[] activeModeImage = {imSA, imMA};
  private Image[] passiveModeImage = {imSI, imMI};
  private int[] monoLocation = {212, 40};
  private int[] stereoLocation = {239, 40};

  /*-- Volume/Balance Panel members --*/
  private boolean FirstVolumeDrag = true;
  private int XVolumeDrag = 0;
  private String theVolume = "volume.bmp";
  private Image imVolume;
  private SkinComponent acVolume;
  private Image[] volumeImage = {null, null, null, null, null, null, null,
								 null, null, null, null, null, null, null,
								 null, null, null, null, null, null, null,
								 null, null, null, null, null, null, null};
  private String fakeIndex = "abcdefghijklmnopqrstuvwxyz01";
  private int[] volumeBarLocation = {107, 57};
  private Image[] releasedVolumeImage = {null};
  private Image[] pressedVolumeImage = {null};
  private int[] releasedVolumePanel0 = {15, 422, 14, 11};
  private int[] pressedVolumePanel0 = {0, 422, 14, 11};
  private int[] releasedVolumePanel1 = {75, 376, 14, 11};
  private int[] pressedVolumePanel1 = {90, 376, 14, 11};
  private int minGain = 0;
  private int maxGain = 100;
  private int gainValue = 60;
  private int deltaVolume = 50;
  private int[] volumeLocation = {107 + deltaVolume * gainValue / maxGain, 58};
  private int[] volumeBounds = {107, 107 + deltaVolume};

  private String theBalance = "balance.bmp";
  private SkinComponent acBalance;
  private Image imBalance;
  private Image[] balanceImage = {null, null, null, null, null, null, null,
								  null, null, null, null, null, null, null,
								  null, null, null, null, null, null, null,
								  null, null, null, null, null, null, null};
  private Image[] releasedBalanceImage = {null};
  private Image[] pressedBalanceImage = {null};
  private int[] releasedBalancePanel0 = {15, 422, 14, 11};
  private int[] pressedBalancePanel0 = {0, 422, 14, 11};
  private int[] releasedBalancePanel1 = {75, 376, 14, 11};
  private int[] pressedBalancePanel1 = {90, 376, 14, 11};

  private boolean FirstBalanceDrag = true;
  private int XBalanceDrag = 0;
  private double minBalance = -1.0;
  private double maxBalance = +1.0;
  private double balanceValue = 0.0;
  private int deltaBalance = 24;
  private int[] balanceLocation = {177 + deltaBalance / 2, 58};
  private int[] balanceBounds = {177, 177 + deltaBalance};
  private int[] balanceBarLocation = {177, 57};

  /*-- Play/Pause Icons --*/
  private String theIcons = "playpaus.bmp";
  private Image imIcons;
  private Image[] iconsImage = {null, null, null, null, null};
  private int[] iconsPanel = {0, 0, 9, 9, 9, 0, 9, 9, 18, 0, 9, 9, 36, 0, 3, 9, 27, 0, 2, 9};
  private int[] iconsLocation = {26, 27, 24, 27};

  /*-- PosBar members --*/
  private boolean FirstPosBarDrag = true;
  private int XPosBarDrag = 0;
  private String thePosBar = "posbar.bmp";
  private Image imPosBar;
  private ActiveComponent acPosBar;
  private Image[] releasedPosIm = {null};
  private Image[] pressedPosIm = {null};
  private int[] releasedPosPanel = {248, 0, 28, 10};
  private int[] pressedPosPanel = {278, 0, 28, 10};
  private double minPos = 0.0;
  private double maxPos = +1.0;
  private double posValue = 0.0;
  private boolean posValueJump = false;
  private int deltaPosBar = 219;
  private int[] posBarLocation = {16, 72};
  private int[] posBarBounds = {16, 16 + deltaPosBar};

  /*-- Equalizer/Playlist/Shuffle/Repeat  --*/
  private String theEPSRButtons = "shufrep.bmp";
  private Image imEPSRButtons;
  private ActiveComponent acEqualizer,acPlaylist,acShuffle,acRepeat;
  private Image imEqualizer,imPlaylist,imShuffle,imRepeat;
  private Image[] releasedEPSRImage = {imEqualizer, imPlaylist, imShuffle, imRepeat};
  private Image[] pressedEPSRImage = {imEqualizer, imPlaylist, imShuffle, imRepeat};
  private int[] releasedEPSRPanel = {0, 61, 22, 12, 23, 61, 22, 12, 28, 0, 47, 14, 0, 0, 28, 14};
  private int[] pressedEPSRPanel = {0, 73, 22, 12, 23, 73, 22, 12, 28, 30, 47, 14, 0, 30, 28, 14};
  private int[] panelEPSRLocation = {219, 58, 242, 58, 166, 89, 212, 89};

  /*-- JavaSound Members --*/
  public static final int INIT = 0;
  public static final int OPEN = 1;
  public static final int PLAY = 2;
  public static final int PAUSE = 3;
  public static final int STOP = 4;
  private int playerState = INIT;
  private long secondsAmount = 0;

  private Playlist playlist = null;
  private BasicController theSoundPlayer = null;
  private Map audioInfo = null;
  private Config config = null;
  private Applet topFrame = null;

  public static final int       NONE = 0;
  public static final int       URL = 1;
  public static final int       ALL = 2;
  private int                   location = ALL;
  private String base = null;

  /**
   * Applet constructor.
   */
  public PlayerApplet()
  {
	topFrame = this;
	BasicPlayerApplet bplayer = new BasicPlayerApplet();
	// Register the front-end to low-level player events.
	bplayer.addBasicPlayerListener(this);
	// Adds controls for front-end to low-level player.
	this.setController(bplayer);
  }

  /**
   * Loads Applet parameters.
   */
  public void init()
  {
	base = getCodeBase().toString();
	log.info("Codebase:"+base);
	String skin = getParameter("skin");
	if (skin != null)
	{
	  if (!Config.startWithProtocol(skin)) skin = base + skin;
	}
	String song = getParameter("song");
	if (song != null)
	{
	  if (Config.startWithProtocol(song)) initSong = song;
	  else initSong = base + song;
	}
	String start = getParameter("start");
	if ((start != null) && (start.equals("yes"))) autoRun = true;
	String enableopenlocation = getParameter("location");
	if ((enableopenlocation != null))
	{
	  if (enableopenlocation.equalsIgnoreCase("none")) location = NONE;
	  else if (enableopenlocation.equalsIgnoreCase("url")) location = URL;
	  else if (enableopenlocation.equalsIgnoreCase("all")) location = ALL;
	}
	String skinversion = getParameter("skinversion");
	if (skinversion != null) skinVersion = skinversion;
	String init = getParameter("init");
	if (init != null)
	{
	  if (Config.startWithProtocol(init)) initConfig = init;
	  else initConfig = base+init;
	}
	else initConfig = base+"jlgui.ini";
	// Ugly workaround to make it work under Java Plugin.
	String userAgent = getParameter("useragent");
	AppletVorbisSPIWorkaround.useragent=userAgent;
	AppletMpegSPIWorkaround.useragent=userAgent;
	String forceOgg = getParameter("forceogg");
	if ((forceOgg != null) && (forceOgg.equalsIgnoreCase("true"))) BasicPlayerApplet.forceOgg=true;
	// End.
	initPlayer(skin);
	if (autoRun == true) pressStart();
  }

  /**
   * Init player applet.
   */
  public void initPlayer(String Skin)
  {
	// Config feature.
	config = Config.getInstance();
	config.load(initConfig);
	OrigineX = config.getXLocation();
	OrigineY = config.getYLocation();

	// Get screen size
	try
	{
	  Toolkit toolkit = Toolkit.getDefaultToolkit();
	  Dimension dimension = toolkit.getScreenSize();
	  screenWidth = dimension.width;
	  screenHeight = dimension.height;
	} catch (Exception e)
	{
	}

	// Minimize/Maximize/Icon features.
	//topFrame.addWindowListener(this);
	topFrame.setLocation(OrigineX, OrigineY);
	topFrame.setSize(0, 0);
	// Polis : Comment out to fix a bug under XWindow
	//topFrame.setResizable(false);
	ClassLoader cl = this.getClass().getClassLoader();
	URL iconURL = cl.getResource("javazoom/jlgui/player/amp/jlguiicon.gif");
	if (iconURL != null)
	{
	  ImageIcon jlguiIcon = new ImageIcon(iconURL);
	  //topFrame.setIconImage(jlguiIcon.getImage());
	}
	topFrame.show();

	// DnD feature.
	DropTarget dt = new DropTarget(this, DnDConstants.ACTION_COPY, this, true);

    // Volume.
    if (config.getVolume() != -1)
    {
    	gainValue = config.getVolume();
    	volumeLocation[0] = 107 + deltaVolume * gainValue / maxGain;
    }

	// Playlist feature.
	boolean playlistfound = false;
	if ((initSong != null) && (!initSong.equals(""))) playlistfound = loadPlaylist(initSong);
	else playlistfound = loadPlaylist(config.getPlaylistFilename());

	// Load skin specified in args
	if (Skin != null)
	{
	  thePath = Skin;
	  log.info("Load default skin from " + thePath);
	  loadSkin(thePath);
	  config.setDefaultSkin(thePath);
	}
	// Load skin specified in jlgui.ini
	else if ((config.getDefaultSkin() != null) && (!config.getDefaultSkin().trim().equals("")))
	{
		log.info("Load default skin from " + config.getDefaultSkin());
	  loadSkin(config.getDefaultSkin());
	}
	// Default included skin
	else
	{
	  //ClassLoader cl = this.getClass().getClassLoader();
	  InputStream sis = cl.getResourceAsStream("javazoom/jlgui/player/amp/metrix.wsz");
	  log.info("Load default skin for JAR");
	  loadSkin(sis);
	}

	// Go to playlist begining if needed.
	if ((playlist != null) && (playlistfound == true))
	{
	  if (playlist.getPlaylistSize() > 0) acNext.fireEvent();
	}

	// Display the whole
	hide();
	show();
	repaint();
  }


  /**
   * Loads a new playlist.
   */
  protected boolean loadPlaylist(String playlistName)
  {
	boolean loaded = false;
	PlaylistFactory plf = PlaylistFactory.getInstance();
	playlist = plf.getPlaylist();
	if (playlist == null)
	{
		config.setPlaylistClassName("javazoom.jlgui.player.amp.playlist.BasePlaylist");
		playlist = plf.getPlaylist();
	}
	if (playlist instanceof BasePlaylist)
	{
		((BasePlaylist)playlist).setM3UHome(base);
		((BasePlaylist)playlist).setPLSHome(base);
	}
	if ((playlistName != null) && (!playlistName.equals("")))
	{
		// M3U file or URL.
		if ((playlistName.toLowerCase().endsWith(".m3u")) || (playlistName.toLowerCase().endsWith(".pls"))) loaded = playlist.load(playlistName);
		// Simple song.
		else
		{
		  String name = playlistName;
		  if (!Config.startWithProtocol(playlistName))
		  {
			int indn = playlistName.lastIndexOf(java.io.File.separatorChar);
			if (indn != -1) name = playlistName.substring(indn + 1);
			PlaylistItem pli = new PlaylistItem(name, playlistName, -1, true);
			playlist.appendItem(pli);
			loaded = true;
		  }
		  else
		  {
			PlaylistItem pli = new PlaylistItem(name, playlistName, -1, false);
			playlist.appendItem(pli);
			loaded = true;
		  }
		}
	}
	return loaded;
  }

  /**
   * Loads a new skin from local file system.
   */
  protected void loadSkin(String skinName)
  {
	SkinLoader skl = new SkinLoader(skinName);
	try
	{
	  loadSkin(skl);
	}
	catch (Exception e)
	{
	  log.info("Can't load skin : "+skinName,e);
	  InputStream sis = this.getClass().getClassLoader().getResourceAsStream("javazoom/jlgui/player/amp/metrix.wsz");
	  log.info("Load default skin for JAR");
	  loadSkin(sis);
	}
  }

  /**
   * Loads a new skin from any input stream.
   */
  public void loadSkin(InputStream skinStream)
  {
	SkinLoader skl = new SkinLoader(skinStream);
	try
	{
	  loadSkin(skl);
	}
	catch (Exception e)
	{
	  log.info("Can't load skin : "+skinStream, e);
	  InputStream sis = this.getClass().getClassLoader().getResourceAsStream("javazoom/jlgui/player/amp/metrix.wsz");
	  log.info("Load default skin for JAR");
	  loadSkin(sis);
	}
  }

  /**
   * Loads a skin from a SkinLoader.
   */
  protected void loadSkin(SkinLoader skl) throws Exception
  {
	skl.loadImages();
	imMain = skl.getImage(theMain);
	imButtons = skl.getImage(theButtons);
	imTitleBar = skl.getImage(theTitleBar);
	imText = skl.getImage(theText);
	imMode = skl.getImage(theMode);
	imNumbers = skl.getImage(theNumbers);
	// add by John Yang
	if (imNumbers == null)
	{
	  log.info("Try load nums_ex.bmp !");
	  imNumbers = skl.getImage(theNumEx);
	}
	imVolume = skl.getImage(theVolume);
	imBalance = skl.getImage(theBalance);
	imIcons = skl.getImage(theIcons);
	imPosBar = skl.getImage(thePosBar);
	imEPSRButtons = skl.getImage(theEPSRButtons);

	// Computes volume slider height :
	int vh = (imVolume.getHeight(null)-422);
	if (vh > 0)
	{
		releasedVolumePanel0[3] = vh;
		pressedVolumePanel0[3] = vh;
		releasedVolumePanel1[3] = vh;
		pressedVolumePanel1[3] = vh;
	}
	// Computes balance slider height :
	if (imBalance == null) imBalance = imVolume;
	int bh = (imBalance.getHeight(null)-422);
	if (bh > 0)
	{
		releasedBalancePanel0[3] = bh;
		pressedBalancePanel0[3] = bh;
		releasedBalancePanel1[3] = bh;
		pressedBalancePanel1[3] = bh;
	}

	// Compute posbar height.
	int ph = imPosBar.getHeight(null);
	if (ph > 0)
	{
		releasedPosPanel[3] = ph;
		pressedPosPanel[3] = ph;
	}

	WinHeight = imMain.getHeight(this); // 275
	WinWidth = imMain.getWidth(this);   // 116
	setSize(WinWidth, WinHeight);
	setLocation(OrigineX, OrigineY);
	//setBackground(Color.black);
	show();

	offScreenImage = createImage(WinWidth, WinHeight);
	offScreenGraphics = offScreenImage.getGraphics();
	// E.B Fix for JDK 1.4 slow down problem.
	hide();
	// End Fix.
	offScreenGraphics.drawImage(imMain, 0, 0, this);

	// M.S : Remove all components when loading a new skin.
	if (acPrevious != null) remove(acPrevious);
	if (acPlay != null) remove(acPlay);
	if (acPause != null) remove(acPause);
	if (acStop != null) remove(acStop);
	if (acNext != null) remove(acNext);
	if (acEject != null) remove(acEject);
	if (acTitleBar != null) remove(acTitleBar);
	if (acExit != null) remove(acExit);
	if (acMinimize != null) remove(acMinimize);
	if (acVolume != null) remove((Component)acVolume);
	if (acBalance != null) remove((Component)acBalance);
	if (acPosBar != null) remove(acPosBar);
	if (acPlaylist != null) remove(acPlaylist);
	if (acRepeat != null)  remove(acRepeat);
	if (acShuffle != null) remove(acShuffle);
	if (acEqualizer != null)  remove(acEqualizer);
	if (fileList != null) remove(fileList);
	if (equalizer != null) remove(equalizer);
	System.gc();

	/*-- Buttons --*/
	readPanel(releasedImage, releasedPanel, pressedImage, pressedPanel, imButtons);
	setButtonsPanel();

	/*-- Volume/Balance --*/
	if (skinVersion.equals("1"))
	{
		readPanel(releasedVolumeImage, releasedVolumePanel0, pressedVolumeImage, pressedVolumePanel0, imVolume);
		readPanel(releasedBalanceImage, releasedBalancePanel0, pressedBalanceImage, pressedBalancePanel0, imBalance);
	}
	else
	{
		readPanel(releasedVolumeImage, releasedVolumePanel1, pressedVolumeImage, pressedVolumePanel1, imVolume);
		readPanel(releasedBalanceImage, releasedBalancePanel1, pressedBalanceImage, pressedBalancePanel1, imBalance);
	}
	setVolumeBalancePanel(vh, bh);

	/*-- Title Bar --*/
	readPanel(releasedTitleIm, releasedTitlePanel, pressedTitleIm, pressedTitlePanel, imTitleBar);
	setTitleBarPanel();

	/*-- Exit --*/
	readPanel(releasedExitIm, releasedExitPanel, pressedExitIm, pressedExitPanel, imTitleBar);
	setExitPanel();

	/*-- Minimize --*/
	readPanel(releasedMinimizeIm, releasedMinimizePanel, pressedMinimizeIm, pressedMinimizePanel, imTitleBar);
	setMinimizePanel();

	/*-- Mode --*/
	readPanel(activeModeImage, activeModePanel, passiveModeImage, passiveModePanel, imMode);
	offScreenGraphics.drawImage(passiveModeImage[0], stereoLocation[0], stereoLocation[1], this);
	offScreenGraphics.drawImage(passiveModeImage[1], monoLocation[0], monoLocation[1], this);

	/*-- Text --*/
	sampleRateClearImage = (new Taftb(fontIndex, imText, fontWidth, fontHeight, 0, sampleRateClearText)).getBanner();
	bitsRateClearImage = (new Taftb(fontIndex, imText, fontWidth, fontHeight, 0, bitsRateClearText)).getBanner();
	clearImage = (new Taftb(fontIndex, imText, fontWidth, fontHeight, 0, clearText)).getBanner(0, 0, 155, 6);
	titleImage = (new Taftb(fontIndex, imText, fontWidth, fontHeight, 0, titleText)).getBanner(0, 0, 155, 6);
	offScreenGraphics.drawImage(titleImage, titleLocation[0], titleLocation[1], this);

	/*-- Numbers --*/
	for (int h = 0; h < numberIndex.length(); h++)
	{
	  timeImage[h] = (new Taftb(numberIndex, imNumbers, numberWidth, numberHeight, 0, "" + numberIndex.charAt(h))).getBanner();
	}

	/*--  Icons --*/
	readPanel(iconsImage, iconsPanel, null, null, imIcons);
	offScreenGraphics.drawImage(iconsImage[2], iconsLocation[0], iconsLocation[1], this);

	/*-- Pos Bar --*/
	readPanel(releasedPosIm, releasedPosPanel, pressedPosIm, pressedPosPanel, imPosBar);
	setPosBarPanel();

	/*-- Equalizer/Playlist/Shuffle/Repeat  --*/
	readPanel(releasedEPSRImage, releasedEPSRPanel, pressedEPSRImage, pressedEPSRPanel, imEPSRButtons);
	setEPSRButtonsPanel();

	// Popup menu on TitleBar
	PopupMenu mainpopup = new PopupMenu("Setup");
	Font fnt = new Font("Dialog", Font.PLAIN, 11);
	mainpopup.setFont(fnt);
	MenuItem mi = new MenuItem(TITLETEXT + "- JavaZOOM");
	mi.setEnabled(false);
	mi.addActionListener(this);
	mainpopup.add(mi);
	mainpopup.addSeparator();
	mi = new MenuItem("Preferences");
	mi.setEnabled(false);
	mi.addActionListener(this);
	mainpopup.add(mi);
	mi = new MenuItem("Skins");
	mi.setEnabled(false);
	mi.addActionListener(this);
	mainpopup.add(mi);
	mainpopup.addSeparator();
	mi = new MenuItem("Exit");
	mi.setEnabled(false);
	mi.addActionListener(this);
	mainpopup.add(mi);
	acTitleBar.setPopup(mainpopup);

	/* -- create MP3File List Window --*/
	if (showPlaylist != null) config.setPlaylistEnabled(true);
	fileList = new MP3FilesApplet(topFrame, this, playlist, skl, OrigineX, OrigineY + WinHeight, config.isPlaylistEnabled());
	add(fileList);

	/* -- create Equalizer Window --*/
	if (showEqualizer != null) config.setEqualizerEnabled(true);
	int factor = 1;
	if (config.isPlaylistEnabled()) factor = 2;
	equalizer = new EqualizerApplet(topFrame, this, skl, OrigineX, OrigineY + WinHeight*factor, config.isEqualizerEnabled());
	add(equalizer);
	show();
  }


  /**
   * Crop Panel Features from image file.
   */
  public void readPanel(Image[] releasedImage, int[] releasedPanel,
						 Image[] pressedImage, int[] pressedPanel, Image imPanel)
  {
	int xul,yul,xld,yld;
	int j = 0;
	if (releasedImage != null)
	{
	  for (int i = 0; i < releasedImage.length; i++)
	  {
		releasedImage[i] = createImage(releasedPanel[j + 2], releasedPanel[j + 3]);
		xul = releasedPanel[j];
		yul = releasedPanel[j + 1];
		xld = releasedPanel[j] + releasedPanel[j + 2];
		yld = releasedPanel[j + 1] + releasedPanel[j + 3];
		(releasedImage[i].getGraphics()).drawImage(imPanel, 0, 0, releasedPanel[j + 2], releasedPanel[j + 3],
												   xul, yul, xld, yld, null);
		j = j + 4;
	  }
	}
	j = 0;

	if (pressedImage != null)
	{
	  for (int i = 0; i < pressedImage.length; i++)
	  {
		pressedImage[i] = createImage(pressedPanel[j + 2], pressedPanel[j + 3]);
		xul = pressedPanel[j];
		yul = pressedPanel[j + 1];
		xld = pressedPanel[j] + pressedPanel[j + 2];
		yld = pressedPanel[j + 1] + pressedPanel[j + 3];
		(pressedImage[i].getGraphics()).drawImage(imPanel, 0, 0, pressedPanel[j + 2], pressedPanel[j + 3],
												  xul, yul, xld, yld, null);
		j = j + 4;
	  }
	}
  }

  /**
   * Instantiate Buttons Panel with ActiveComponent.
   * Add them to window and ActionListener.
   */
  protected void setButtonsPanel()
  {
	int l = 0;
	setLayout(null);
	acPrevious = new ActiveComponent(releasedImage[0], pressedImage[0], AWTEvent.MOUSE_EVENT_MASK);
	acPrevious.setLocation(panelLocation[l++], panelLocation[l++]);
	add(acPrevious);
	acPrevious.setActionCommand("Previous");
	acPrevious.addActionListener(this);

	acPlay = new ActiveComponent(releasedImage[1], pressedImage[1], AWTEvent.MOUSE_EVENT_MASK);
	acPlay.setLocation(panelLocation[l++], panelLocation[l++]);
	add(acPlay);
	acPlay.setActionCommand("Play");
	acPlay.addActionListener(this);

	acPause = new ActiveComponent(releasedImage[2], pressedImage[2], AWTEvent.MOUSE_EVENT_MASK);
	acPause.setLocation(panelLocation[l++], panelLocation[l++]);
	add(acPause);
	acPause.setActionCommand("Pause");
	acPause.addActionListener(this);

	acStop = new ActiveComponent(releasedImage[3], pressedImage[3], AWTEvent.MOUSE_EVENT_MASK);
	acStop.setLocation(panelLocation[l++], panelLocation[l++]);
	add(acStop);
	acStop.setActionCommand("Stop");
	acStop.addActionListener(this);

	acNext = new ActiveComponent(releasedImage[4], pressedImage[4], AWTEvent.MOUSE_EVENT_MASK);
	acNext.setLocation(panelLocation[l++], panelLocation[l++]);
	add(acNext);
	acNext.setActionCommand("Next");
	acNext.addActionListener(this);

	acEject = new ActiveComponent(releasedImage[5], pressedImage[5], AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
	acEject.setLocation(panelLocation[l++], panelLocation[l++]);
	add(acEject);
	acEject.setActionCommand("Eject");
	acEject.addActionListener(this);
  }

  /**
   * Instantiate Title Panel with ActiveComponent.
   * Add them to window and ActionListener.
   */
  protected void setTitleBarPanel()
  {
	int l = 0;
	acTitleBar = new ActiveComponent(releasedTitleIm[0], pressedTitleIm[0], AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
	// TODO - Title bar disabled.
	acTitleBar.setEnabled(false);
	acTitleBar.setLocation(titleBarLocation[l++], titleBarLocation[l++]);
	add(acTitleBar);
	acTitleBar.setActionCommand("TitleBar");
	acTitleBar.addActionListener(this);
  }

  /**
   * Instantiate Exit Panel with ActiveComponent.
   * Add them to window and ActionListener.
   */
  protected void setExitPanel()
  {
	int l = 0;
	acExit = new ActiveComponent(releasedExitIm[0], pressedExitIm[0], AWTEvent.MOUSE_EVENT_MASK);
	// TODO - Exit disabled
	acExit.setEnabled(false);
	acExit.setLocation(exitLocation[l++], exitLocation[l++]);
	add(acExit);
	acExit.setActionCommand("Exit");
	acExit.addActionListener(this);
  }

  /**
   * Instantiate Minimize Panel with ActiveComponent.
   * Add them to window and ActionListener.
   */
  protected void setMinimizePanel()
  {
	int l = 0;
	acMinimize = new ActiveComponent(releasedMinimizeIm[0], pressedMinimizeIm[0], AWTEvent.MOUSE_EVENT_MASK);
	//	TODO - Minimize disabled
	acMinimize.setEnabled(false);
	acMinimize.setLocation(minimizeLocation[l++], minimizeLocation[l++]);
	add(acMinimize);
	acMinimize.setActionCommand("Minimize");
	acMinimize.addActionListener(this);
  }

  /**
   * Instantiate Volume/Balance Panel with ActiveComponent.
   * Add them to window and ActionListener.
   */
  protected void setVolumeBalancePanel(int vheight, int bheight)
  {
	// Volume.
	int l = 0;
	if (vheight > 0) acVolume = new ActiveComponent(releasedVolumeImage[0], pressedVolumeImage[0], AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
	else acVolume = new InvisibleActiveComponent(releasedVolumeImage[0], pressedVolumeImage[0], AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
	acVolume.setLocation(volumeLocation[l++], volumeLocation[l++]);
	add((Component)acVolume);
	acVolume.setActionCommand("Volume");
	acVolume.addActionListener(this);
	for (int k = 0; k < volumeImage.length; k++)
	{
	  volumeImage[k] = (new Taftb(fakeIndex, imVolume, 68, 13, 2, "" + fakeIndex.charAt(k))).getBanner();
	}
	offScreenGraphics.drawImage(volumeImage[(int) Math.round(((double) gainValue / (double) maxGain) * (volumeImage.length - 1))], volumeBarLocation[0], volumeBarLocation[1], this);

	// Balance
	Image cropBalance = createImage(37, 418);
	Graphics g = cropBalance.getGraphics();
	g.drawImage(imBalance, 0, 0, 37, 418, 9, 0, 9 + 37, 0 + 418, null);

	l = 0;
	if (bheight > 0) acBalance = new ActiveComponent(releasedBalanceImage[0], pressedBalanceImage[0], AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
	else acBalance = new InvisibleActiveComponent(releasedBalanceImage[0], pressedBalanceImage[0], AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
	acBalance.setLocation(balanceLocation[l++], balanceLocation[l++]);
	add((Component)acBalance);
	acBalance.setActionCommand("Balance");
	acBalance.addActionListener(this);
	for (int k = 0; k < balanceImage.length; k++)
	{
		balanceImage[k] = (new Taftb(fakeIndex, cropBalance, 37, 13, 2, "" + fakeIndex.charAt(k))).getBanner();
	}
	offScreenGraphics.drawImage(balanceImage[(int) Math.round(((double) Math.abs(balanceValue) / (double) 1) * (balanceImage.length - 1))], balanceBarLocation[0], balanceBarLocation[1], this);
  }

  /**
   * Instantiate PosBar Panel with ActiveComponent.
   * Add them to window and ActionListener.
   */
  protected void setPosBarPanel()
  {
	int l = 0;
	acPosBar = new ActiveComponent(releasedPosIm[0], pressedPosIm[0], AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
	acPosBar.setLocation(posBarLocation[l++], posBarLocation[l++]);
	add(acPosBar);
	acPosBar.setActionCommand("Seek");
	acPosBar.addActionListener(this);
	remove(acPosBar);
  }

  /**
   * Instantiate EPSR Buttons Panel with ActiveComponent.
   * Add them to window and ActionListener.
   */
  protected void setEPSRButtonsPanel()
  {
	int l = 0;
	setLayout(null);
	acEqualizer = new ActiveComponent(releasedEPSRImage[0], pressedEPSRImage[0], AWTEvent.MOUSE_EVENT_MASK, true, config.isEqualizerEnabled());
	acEqualizer.setLocation(panelEPSRLocation[l++], panelEPSRLocation[l++]);
	add(acEqualizer);
	acEqualizer.setActionCommand("Equalizer");
	acEqualizer.addActionListener(this);

	acPlaylist = new ActiveComponent(releasedEPSRImage[1], pressedEPSRImage[1], AWTEvent.MOUSE_EVENT_MASK, true, config.isPlaylistEnabled());
	acPlaylist.setLocation(panelEPSRLocation[l++], panelEPSRLocation[l++]);
	add(acPlaylist);
	acPlaylist.setActionCommand("Playlist");
	acPlaylist.addActionListener(this);

	acShuffle = new ActiveComponent(releasedEPSRImage[2], pressedEPSRImage[2], AWTEvent.MOUSE_EVENT_MASK, true, config.isShuffleEnabled());
	acShuffle.setLocation(panelEPSRLocation[l++], panelEPSRLocation[l++]);
	add(acShuffle);
	acShuffle.setActionCommand("Shuffle");
	acShuffle.addActionListener(this);

	acRepeat = new ActiveComponent(releasedEPSRImage[3], pressedEPSRImage[3], AWTEvent.MOUSE_EVENT_MASK, true, config.isRepeatEnabled());
	acRepeat.setLocation(panelEPSRLocation[l++], panelEPSRLocation[l++]);
	add(acRepeat);
	acRepeat.setActionCommand("Repeat");
	acRepeat.addActionListener(this);
  }

  /**
   * Sets the current song to play and start playing if needed.
   */
  public void setCurrentSong(PlaylistItem pli)
  {
	int playerStateMem = playerState;
	if ((playerState == PAUSE) || (playerState == PLAY))
	{
	  try
	  {
		theSoundPlayer.stop();
	  }
	  catch (BasicPlayerException e)
	  {
		log.error("Cannot stop",e);
	  }
	  playerState = STOP;
	  secondsAmount = 0;
	  acPosBar.setLocation(posBarBounds[0], posBarLocation[1]);
	  offScreenGraphics.drawImage(iconsImage[2], iconsLocation[0], iconsLocation[1], this);
	  offScreenGraphics.drawImage(iconsImage[4], iconsLocation[2], iconsLocation[3], this);
	}
	playerState = OPEN;
	if (pli != null)
	{
	  // Read tag info.
	  pli.getTagInfo();
	  currentSongName = pli.getFormattedName();
	  currentFileOrURL = pli.getLocation();
	  currentIsFile = pli.isFile();
	  currentPlaylistItem = pli;
	}
	// Playlist ended.
	else
	{
	  // Try to repeat ?
	  if (config.isRepeatEnabled())
	  {
		if (playlist != null)
		{
		  // PlaylistItems available ?
		  if (playlist.getPlaylistSize() > 0)
		  {
			playlist.begin();
			PlaylistItem rpli = playlist.getCursor();
			if (rpli != null)
			{
			  // OK, Repeat the playlist.
			  rpli.getTagInfo();
			  currentSongName = rpli.getFormattedName();
			  currentFileOrURL = rpli.getLocation();
			  currentIsFile = rpli.isFile();
			  currentPlaylistItem = rpli;
			}
		  }
		  // No, so display Title.
		  else
		  {
			currentSongName = TITLETEXT;
			currentFileOrURL = null;
			currentIsFile = false;
			currentPlaylistItem = null;
		  }
		}
	  }
	  // No, so display Title.
	  else
	  {
		currentSongName = TITLETEXT;
		currentFileOrURL = null;
		currentIsFile = false;
		currentPlaylistItem = null;
	  }
	}
	if (currentIsFile == true)
	{
	  add(acPosBar);
	  acPosBar.repaint();
	}
	else
	{
	  config.setLastURL(currentFileOrURL);
	  remove(acPosBar);
	}
	titleText = currentSongName.toUpperCase();
	showMessage(titleText);
	repaint();

	// Start playing if needed.
	if ((playerStateMem == PLAY) || (playerStateMem == PAUSE))
	{
	  acPlay.fireEvent();
	}
  }

  /*-----------------------------------------*/
  /*--    BasicPlayerListener Interface    --*/
  /*-----------------------------------------*/

  /**
   * Open callback, stream is ready to play.
   */
  public void opened(Object stream, Map properties)
  {
	audioInfo = properties;
	log.debug(properties.toString());
  }

  /**
   * Progress callback while playing.
   */
  public void progress(int bytesread, long microseconds, byte[] pcmdata, Map properties)
  {
	int byteslength = -1;
	long total = -1;
	// Try to get time from playlist item.
	if (currentPlaylistItem != null) total = currentPlaylistItem.getLength();
	// If it fails then try again with JavaSound SPI.
	if (total <=0) total = (long) Math.round(getTimeLengthEstimation(audioInfo)/1000);
	// If it fails again then it might be stream => Total = -1
	if (total <=0) total = -1;
	if (audioInfo.containsKey("audio.length.bytes"))
	{
		byteslength = ((Integer) audioInfo.get("audio.length.bytes")).intValue();
	}
	float progress = -1.0f;
	if ((bytesread > 0) && ((byteslength > 0))) progress = bytesread*1.0f/byteslength*1.0f;

	if (audioInfo.containsKey("audio.type"))
	{
		String audioformat = (String) audioInfo.get("audio.type");
		if (audioformat.equalsIgnoreCase("mp3"))
		{
			//if (properties.containsKey("mp3.position.microseconds")) secondsAmount = (long) Math.round(((Long) properties.get("mp3.position.microseconds")).longValue()/1000000);
			// Shoutcast stream title.
			if (properties.containsKey("mp3.shoutcast.metadata.StreamTitle"))
			{
				String shoutTitle = ((String) properties.get("mp3.shoutcast.metadata.StreamTitle")).trim();
				if (shoutTitle.length()>0)
				{
					if (currentPlaylistItem != null)
					{
						String sTitle = " ("+currentPlaylistItem.getFormattedDisplayName()+")";
						if (!currentPlaylistItem.getFormattedName().equals(shoutTitle+sTitle))
						{

							currentPlaylistItem.setFormattedDisplayName(shoutTitle+sTitle);
							showTitle((shoutTitle+sTitle).toUpperCase());
							fileList.displayAll();
						}
					}
				}
			}
			// Equalizer
			if (properties.containsKey("mp3.equalizer"))equalizer.setBands((float[])properties.get("mp3.equalizer"));
			if (total > 0) secondsAmount = (long) (total*progress);
			else secondsAmount = -1;
		}
		else if (audioformat.equalsIgnoreCase("wave"))
		{
			secondsAmount = (long) (total*progress);
		}
		else
		{
			secondsAmount = (long) Math.round(microseconds/1000000);
			equalizer.setBands(null);
		}
	}
	else
	{
		secondsAmount = (long) Math.round(microseconds/1000000);
		equalizer.setBands(null);
	}
	if (secondsAmount < 0) secondsAmount = (long) Math.round(microseconds/1000000);

	/*-- Display elapsed time --*/
	int secondD=0,second=0,minuteD=0,minute=0;
	int seconds=(int) secondsAmount;
	int minutes=(int) Math.floor(seconds/60);
	int hours=(int) Math.floor(minutes/60);
	minutes=minutes-hours*60;
	seconds=seconds-minutes*60-hours*3600;
	if (seconds < 10)
	{
	  secondD = 0;
	  second = seconds;
	}
	else
	{
	  secondD = ((int) seconds / 10);
	  second = ((int) (seconds - (((int) seconds / 10)) * 10));
	}
	if (minutes < 10)
	{
	  minuteD = 0;
	  minute = minutes;
	}
	else
	{
	  minuteD = ((int) minutes / 10);
	  minute = ((int) (minutes - (((int) minutes / 10)) * 10));
	}
	// Update PosBar location.
	if (total != 0)
	{
	  if ((FirstPosBarDrag == true) && (posValueJump==false))
	  {
		posBarLocation[0] = ((int) Math.round(secondsAmount * deltaPosBar / total)) + posBarBounds[0];
		if (posBarLocation[0] < posBarBounds[0]) posBarLocation[0] = posBarBounds[0];
		else if (posBarLocation[0] > posBarBounds[1]) posBarLocation[0] = posBarBounds[1];
		acPosBar.setLocation(posBarLocation[0], posBarLocation[1]);
		acPosBar.repaint();
	  }
	}
	else posBarLocation[0] = posBarBounds[0];
	offScreenGraphics.drawImage(timeImage[minuteD], minuteDLocation[0], minuteDLocation[1], this);
	offScreenGraphics.drawImage(timeImage[minute], minuteLocation[0], minuteLocation[1], this);
	offScreenGraphics.drawImage(timeImage[secondD], secondDLocation[0], secondDLocation[1], this);
	offScreenGraphics.drawImage(timeImage[second], secondLocation[0], secondLocation[1], this);

	long ctime = System.currentTimeMillis();
	long lctime = lastScrollTime;
	// Scroll title ?
	if ((titleScrollImage != null) && (titleScrollImage.length > 0))
	{
		if (ctime-lctime > SCROLL_PERIOD)
		{
			lastScrollTime = ctime;
			if (scrollRight == true)
			{
				scrollIndex++;
				if (scrollIndex>=titleScrollImage.length)
				{
					scrollIndex--;
					scrollRight=false;
				}
			}
			else
			{
				scrollIndex--;
				if (scrollIndex<=0)
				{
					scrollRight=true;
				}
			}
			if ((offScreenGraphics != null) && (clearImage != null) && (titleScrollImage != null))
			{
				offScreenGraphics.drawImage(clearImage, titleLocation[0], titleLocation[1], this);
				offScreenGraphics.drawImage(titleScrollImage[scrollIndex], titleLocation[0], titleLocation[1], this);
			}
		}
	}
	repaint();
  }

  /**
   * Notification callback.
   */
  public void stateUpdated(BasicPlayerEvent event)
  {
	log.debug("Event:"+event);
	/*-- End Of Media reached --*/
	int state = event.getCode();
	Object obj = event.getDescription();
	if (state==BasicPlayerEvent.EOM)
	{
	  if ((playerState == PAUSE) || (playerState == PLAY))
	  {
		playlist.nextCursor();
		fileList.nextCursor();
		PlaylistItem pli = playlist.getCursor();
		this.setCurrentSong(pli);
	  }
	}
	else if (state==BasicPlayerEvent.PLAYING)
	{
		lastScrollTime = System.currentTimeMillis();
		posValueJump = false;
	}
	else if (state==BasicPlayerEvent.SEEKING)
	{
		posValueJump = true;
	}
	else if (state==BasicPlayerEvent.OPENING)
	{
		if ((obj instanceof URL) || (obj instanceof InputStream))
		{
			showTitle("PLEASE WAIT ... BUFFERING ...");
			paintAll(getGraphics());
		}

	}
  }

  /**
   * A handle to the BasicPlayer, plugins may control the player through
   * the controller (play, stop, ...)
   */
  public void setController(BasicController controller)
  {
	theSoundPlayer = controller;
  }

  /**
   * Process seek feature.
   */
  protected void processSeek()
  {
	try
	{
	  if (audioInfo.containsKey("audio.type"))
	  {
		String type = (String) audioInfo.get("audio.type");
		// Seek support for MP3.
		if ((type.equalsIgnoreCase("mp3")) && (audioInfo.containsKey("audio.length.bytes")))
		{
		  long skipBytes = (long) Math.round(((Integer) audioInfo.get("audio.length.bytes")).intValue()*posValue);
		  log.debug("Seek value (MP3) : "+skipBytes);
		  theSoundPlayer.seek(skipBytes);
		}
		// Seek support for WAV.
		else if ((type.equalsIgnoreCase("wave")) && (audioInfo.containsKey("audio.length.bytes")))
		{
		  long skipBytes = (long) Math.round(((Integer) audioInfo.get("audio.length.bytes")).intValue()*posValue);
		  log.debug("Seek value (WAVE) : "+skipBytes);
		  theSoundPlayer.seek(skipBytes);
		}
		else posValueJump = false;
	  }
	  else posValueJump = false;
	}
	catch (BasicPlayerException ioe)
	{
	  log.error("Cannot skip",ioe);
	  posValueJump = false;
	}
  }

  /**
   * Manages events.
   */
  public void actionPerformed(ActionEvent e)
  {

	/*------------------------------------*/
	/*--        Interact on Seek        --*/
	/*------------------------------------*/
	if (e.getActionCommand().equals("Seek"))
	{
	  if (acPosBar.isMousePressed() == false)
	  {
		FirstPosBarDrag = true;
		posValueJump = true;
		processSeek();
		repaint();
	  }
	  else
	  {
		int DeltaX = 0;
		if (FirstPosBarDrag == false)
		{
		  DeltaX = acPosBar.getMouseX() - XPosBarDrag;
		  XPosBarDrag = acPosBar.getMouseX() - DeltaX;
		  if (posBarLocation[0] + DeltaX < posBarBounds[0]) posBarLocation[0] = posBarBounds[0];
		  else if (posBarLocation[0] + DeltaX > posBarBounds[1]) posBarLocation[0] = posBarBounds[1];
		  else posBarLocation[0] = posBarLocation[0] + DeltaX;
		  acPosBar.setLocation(posBarLocation[0], posBarLocation[1]);
		  double a = (maxPos - minPos) / (posBarBounds[1] - posBarBounds[0]);
		  posValue = (a * (posBarLocation[0] - posBarBounds[0]) + minPos);
		}
		else
		{
		  FirstPosBarDrag = false;
		  XPosBarDrag = acPosBar.getMouseX();
		}
	  }
	}

	/*------------------------------------*/
	/*--       Interact on Volume       --*/
	/*------------------------------------*/
	else if (e.getActionCommand().equals("Volume"))
	{
	if (acVolume.isMousePressed() == false)
	{
		FirstVolumeDrag = true;
		offScreenGraphics.drawImage(clearImage, titleLocation[0], titleLocation[1], this);
		offScreenGraphics.drawImage(titleImage, titleLocation[0], titleLocation[1], this);
		repaint();
	}
	else
	{
		int DeltaX = 0;
		if (FirstVolumeDrag == false)
		{
			DeltaX = acVolume.getMouseX() - XVolumeDrag;
			XVolumeDrag = acVolume.getMouseX() - DeltaX;
			if (volumeLocation[0] + DeltaX < volumeBounds[0]) volumeLocation[0] = volumeBounds[0];
			else if (volumeLocation[0] + DeltaX > volumeBounds[1]) volumeLocation[0] = volumeBounds[1];
			else volumeLocation[0] = volumeLocation[0] + DeltaX;
			acVolume.setLocation(volumeLocation[0], volumeLocation[1]);
			double a = (maxGain - minGain) / (volumeBounds[1] - volumeBounds[0]);
			gainValue = (int) (a * (volumeLocation[0] - volumeBounds[0]) + minGain);
			try
			{
				if (gainValue == 0) theSoundPlayer.setGain(0);
				else theSoundPlayer.setGain(((double) gainValue / (double) maxGain));
				config.setVolume(gainValue);
			}
			catch (BasicPlayerException e1)
			{
				log.debug("Cannot set gain",e1);
			}
			String volumeText = "VOLUME: " + (int) Math.round((100 / (volumeBounds[1] - volumeBounds[0])) * (volumeLocation[0] - volumeBounds[0])) + "%";
			Image volImage = (new Taftb(fontIndex, imText, fontWidth, fontHeight, 0, volumeText)).getBanner();
			offScreenGraphics.drawImage(volumeImage[(int) Math.round(((double) gainValue / (double) maxGain) * (volumeImage.length - 1))], volumeBarLocation[0], volumeBarLocation[1], this);
			offScreenGraphics.drawImage(clearImage, titleLocation[0], titleLocation[1], this);
			offScreenGraphics.drawImage(volImage, titleLocation[0], titleLocation[1], this);
		}
		else
		{
			FirstVolumeDrag = false;
			XVolumeDrag = acVolume.getMouseX();
		}
	}
	}

	/*------------------------------------*/
	/*--       Interact on Balance       --*/
	/*------------------------------------*/
	else if (e.getActionCommand().equals("Balance"))
	{
	  if (acBalance.isMousePressed() == false)
	  {
		FirstBalanceDrag = true;
		offScreenGraphics.drawImage(clearImage, titleLocation[0], titleLocation[1], this);
		offScreenGraphics.drawImage(titleImage, titleLocation[0], titleLocation[1], this);
		repaint();
	  } else
	  {
		int DeltaX = 0;
		if (FirstBalanceDrag == false)
		{
		  DeltaX = acBalance.getMouseX() - XBalanceDrag;
		  XBalanceDrag = acBalance.getMouseX() - DeltaX;
		  if (balanceLocation[0] + DeltaX < balanceBounds[0]) balanceLocation[0] = balanceBounds[0];
		  else if (balanceLocation[0] + DeltaX > balanceBounds[1]) balanceLocation[0] = balanceBounds[1];
		  else balanceLocation[0] = balanceLocation[0] + DeltaX;
		  acBalance.setLocation(balanceLocation[0], balanceLocation[1]);
		  double a = (maxBalance - minBalance) / (balanceBounds[1] - balanceBounds[0]);
		  balanceValue = (a * (balanceLocation[0] - balanceBounds[0]) + minBalance);
		  try
		  {
			  theSoundPlayer.setPan((float) balanceValue);
		  }
		  catch (BasicPlayerException e1)
		  {
			log.debug("Cannot set pan",e1);
		  }
		  String balanceText = "BALANCE: " + (int) Math.abs(balanceValue * 100) + "%";
		  if (balanceValue > 0) balanceText = balanceText + " RIGHT";
		  else if (balanceValue < 0) balanceText = balanceText + " LEFT";
		  else balanceText = "BALANCE: CENTER";
		  Image balImage = (new Taftb(fontIndex, imText, fontWidth, fontHeight, 0, balanceText)).getBanner();
		  offScreenGraphics.drawImage(balanceImage[(int) Math.round(((double) Math.abs(balanceValue) / (double) 1) * (balanceImage.length - 1))], balanceBarLocation[0], balanceBarLocation[1], this);
		  offScreenGraphics.drawImage(clearImage, titleLocation[0], titleLocation[1], this);
		  offScreenGraphics.drawImage(balImage, titleLocation[0], titleLocation[1], this);
		}
		else
		{
		  FirstBalanceDrag = false;
		  XBalanceDrag = acBalance.getMouseX();
		}
	  }
	}

	/*------------------------------------*/
	/*-- Select Filename or URL to load --*/
	/*------------------------------------*/
	else if (e.getActionCommand().equals("Eject"))
	{
	  if ((playerState == PLAY) || (playerState == PAUSE))
	  {
		try
		{
			theSoundPlayer.stop();
		}
		catch (BasicPlayerException e1)
		{
			log.info("Cannot stop",e1);
		}
		playerState = STOP;
	  }
	  if ((playerState == INIT) || (playerState == STOP) || (playerState == OPEN))
	  {
		System.gc();
		PlaylistItem pli = null;
		if (location != NONE)
		{
			// Local File.
			// E.B : FileSelector added as M.S did.
			if (acEject.getMouseButton() == MouseEvent.BUTTON1_MASK)
			{
			  String fsFile = null;
			  if (location == ALL)
			  {
				Frame f = new Frame();
				f.setLocation(this.getBounds().x, this.getBounds().y + 10);
				FileSelector.setWindow(f);
				fsFile = FileSelector.selectFile(FileSelector.OPEN, config.getExtensions(), config.getLastDir());
				fsFile = FileSelector.getFile();
			  }
			  else if (location == URL)
			  {
				UrlDialog UD = new UrlDialog("Open location",this.getBounds().x,this.getBounds().y + 10,280,120,config.getLastURL());
				UD.show();
				if (UD.getFile() != null)
				{
				  config.setLastURL(UD.getURL());
				  fsFile = UD.getURL();
				}
			  }
			  if (fsFile != null)
			  {
				if (location == ALL) config.setLastDir(FileSelector.getDirectory());
				else config.setLastDir("");
				if (fsFile != null)
				{
				  // Loads a new playlist.
				  if ((fsFile.toLowerCase().endsWith(".m3u")) || (fsFile.toLowerCase().endsWith(".pls")))
				  {
					if (loadPlaylist(config.getLastDir() + fsFile))
					{
					  config.setPlaylistFilename(config.getLastDir() + fsFile);
					  playlist.begin();
					  fileList.initPlayList();
					  this.setCurrentSong(playlist.getCursor());
					  fileList.repaint();
					}
				  }
				  else if (fsFile.toLowerCase().endsWith(".wsz"))
				  {
					//this.dispose();
					loadSkin(config.getLastDir() + fsFile);
					config.setDefaultSkin(config.getLastDir() + fsFile);
				  }
				  else
				  {
					if (location == ALL) pli = new PlaylistItem(fsFile, config.getLastDir()+fsFile, -1, true);
					else pli = new PlaylistItem(fsFile, fsFile, -1, false);
				  }
				}
			  }
			}
			// Remote File.
			else if (acEject.getMouseButton() == MouseEvent.BUTTON3_MASK)
			{
			  UrlDialog UD = new UrlDialog("Open location", this.getBounds().x, this.getBounds().y + 10, 280, 130, config.getLastURL());
			  UD.show();
			  if (UD.getFile() != null)
			  {
				showTitle("PLEASE WAIT ... LOADING ...");
				displayAll();
				if (fileList != null) fileList.displayAll();
				if (equalizer != null) equalizer.displayAll();
				// Remote playlist ?
				if ((UD.getURL().toLowerCase().endsWith(".m3u")) || (UD.getURL().toLowerCase().endsWith(".pls")))
				{
					if (loadPlaylist(UD.getURL()))
					{
					  config.setPlaylistFilename(UD.getURL());
					  playlist.begin();
					  fileList.initPlayList();
					  this.setCurrentSong(playlist.getCursor());
					  fileList.repaint();
					}
				}
				// Remote file or stream.
				else
				{
					pli = new PlaylistItem(UD.getFile(), UD.getURL(), -1, false);
				}
				config.setLastURL(UD.getURL());
			  }
			}
		}

		if (pli != null)
		{
		  playlist.removeAllItems();
		  playlist.appendItem(pli);
		  playlist.nextCursor();
		  fileList.initPlayList();
		  this.setCurrentSong(pli);
		}
	  }
	  offScreenGraphics.drawImage(iconsImage[2], iconsLocation[0], iconsLocation[1], this);
	  offScreenGraphics.drawImage(iconsImage[4], iconsLocation[2], iconsLocation[3], this);
	  repaint();
	}

	/*---------------------------*/
	/*-- Play the current File --*/
	/*---------------------------*/
	else if (e.getActionCommand().equals("Play"))
	{
	  if (playlist.isModified())  // playlist has been modified since we were last there, must update our cursor pos etc.
	  {
		PlaylistItem pli = playlist.getCursor();
		if (pli == null)
		{
		  playlist.begin();
		  pli = playlist.getCursor();
		}
		this.setCurrentSong(pli);
		playlist.setModified(false);
		fileList.repaint();
	  }

	  // Resume is paused.
	  if (playerState == PAUSE)
	  {
		try
		{
			theSoundPlayer.resume();
		}
		catch (BasicPlayerException e1)
		{
			log.error("Cannot resume",e1);
		}
		playerState = PLAY;
		offScreenGraphics.drawImage(iconsImage[0], iconsLocation[0], iconsLocation[1], this);
		offScreenGraphics.drawImage(iconsImage[3], iconsLocation[2], iconsLocation[3], this);
		repaint();
	  }

	  // Stop if playing.
	  else if (playerState == PLAY)
	  {
		try
		{
			theSoundPlayer.stop();
		}
		catch (BasicPlayerException e1)
		{
			log.error("Cannot stop",e1);
		}
		playerState = PLAY;
		secondsAmount = 0;
		offScreenGraphics.drawImage(timeImage[0], minuteDLocation[0], minuteDLocation[1], this);
		offScreenGraphics.drawImage(timeImage[0], minuteLocation[0], minuteLocation[1], this);
		offScreenGraphics.drawImage(timeImage[0], secondDLocation[0], secondDLocation[1], this);
		offScreenGraphics.drawImage(timeImage[0], secondLocation[0], secondLocation[1], this);
		repaint();
		if (currentFileOrURL != null)
		{
		  try
		  {
			if (currentIsFile == true) theSoundPlayer.open(openFile(currentFileOrURL));
			else theSoundPlayer.open(new URL(currentFileOrURL));
			theSoundPlayer.play();
		  } catch (Exception ex)
		  {
			log.error("Cannot read file : " + currentFileOrURL,ex);
			showMessage("INVALID FILE");
		  }
		}
	  }
	  else if ((playerState == STOP) || (playerState == OPEN))
	  {
		try
		{
			theSoundPlayer.stop();
		}
		catch (BasicPlayerException e1)
		{
			log.error("Stop failed",e1);
		}
		if (currentFileOrURL != null)
		{
		  try
		  {
			if (currentIsFile == true) theSoundPlayer.open(openFile(currentFileOrURL));
			else theSoundPlayer.open(new URL(currentFileOrURL));
			theSoundPlayer.play();
			titleText = currentSongName.toUpperCase();

			// Get bitrate, samplingrate, channels, time in the following order :
			// PlaylistItem, BasicPlayer (JavaSound SPI), Manual computation.
			int bitRate = -1;
			if (currentPlaylistItem != null) bitRate = currentPlaylistItem.getBitrate();
			if ((bitRate <= 0) && (audioInfo.containsKey("bitrate"))) bitRate = ((Integer) audioInfo.get("bitrate")).intValue();
			if ((bitRate <= 0) && (audioInfo.containsKey("audio.framerate.fps")) && (audioInfo.containsKey("audio.framesize.bytes")))
			{
				float FR = ((Float)audioInfo.get("audio.framerate.fps")).floatValue();
				int FS = ((Integer)audioInfo.get("audio.framesize.bytes")).intValue();
				bitRate = Math.round(FS * FR * 8);
			}
			int channels = -1;
			if (currentPlaylistItem != null) channels = currentPlaylistItem.getChannels();
			if ((channels <= 0) &&(audioInfo.containsKey("audio.channels"))) channels = ((Integer) audioInfo.get("audio.channels")).intValue();
			float sampleRate = -1.0f;
			if (currentPlaylistItem != null) sampleRate = currentPlaylistItem.getSamplerate();
			if ((sampleRate <= 0) && (audioInfo.containsKey("audio.samplerate.hz"))) sampleRate = ((Float) audioInfo.get("audio.samplerate.hz")).floatValue();
			long lenghtInSecond = -1L;
			if (currentPlaylistItem != null) lenghtInSecond = currentPlaylistItem.getLength();
			if ((lenghtInSecond <= 0) && (audioInfo.containsKey("duration"))) lenghtInSecond = ((Long) audioInfo.get("duration")).longValue()/1000000;
			if ((lenghtInSecond <= 0) && (audioInfo.containsKey("audio.length.bytes")))
			{
				// Try to compute time length.
				lenghtInSecond = (long) Math.round(getTimeLengthEstimation(audioInfo)/1000);
				if (lenghtInSecond > 0)
				{
					int minutes=(int) Math.floor(lenghtInSecond/60);
					int hours=(int) Math.floor(minutes/60);
					minutes=minutes-hours*60;
					int seconds=(int) (lenghtInSecond-minutes*60-hours*3600);
					if (seconds >= 10) titleText = "(" + minutes + ":" + seconds + ") "+titleText;
					else titleText = "(" + minutes + ":0" + seconds + ") "+titleText;
				}
			}
			bitRate = Math.round(( bitRate/ 1000));
			sampleRateImage = (new Taftb(fontIndex, imText, fontWidth, fontHeight, 0, "" + Math.round((sampleRate / 1000)))).getBanner();
			if (bitRate > 999)
			{
				bitRate = (int) (bitRate/100);
				bitsRateImage = (new Taftb(fontIndex, imText, fontWidth, fontHeight, 0, "" + bitRate+"H")).getBanner();
			}
			else bitsRateImage = (new Taftb(fontIndex, imText, fontWidth, fontHeight, 0, "" + bitRate)).getBanner();
			offScreenGraphics.drawImage(sampleRateImage, sampleRateLocation[0], sampleRateLocation[1], this);
			offScreenGraphics.drawImage(bitsRateImage, bitsRateLocation[0], bitsRateLocation[1], this);
			if (channels == 2)
			{
			  offScreenGraphics.drawImage(activeModeImage[0], stereoLocation[0], stereoLocation[1], this);
			}
			else if (channels == 1)
			{
			  offScreenGraphics.drawImage(activeModeImage[1], monoLocation[0], monoLocation[1], this);
			}
			showTitle(titleText);
			offScreenGraphics.drawImage(timeImage[0], minuteDLocation[0], minuteDLocation[1], this);
			offScreenGraphics.drawImage(timeImage[0], minuteLocation[0], minuteLocation[1], this);
			offScreenGraphics.drawImage(timeImage[0], secondDLocation[0], secondDLocation[1], this);
			offScreenGraphics.drawImage(timeImage[0], secondLocation[0], secondLocation[1], this);

			offScreenGraphics.drawImage(iconsImage[0], iconsLocation[0], iconsLocation[1], this);
			offScreenGraphics.drawImage(iconsImage[3], iconsLocation[2], iconsLocation[3], this);
		  }
		  catch (BasicPlayerException bpe)
		  {
				log.info("Stream error :" + currentFileOrURL,bpe);
				showMessage("INVALID FILE");
		  }
		  catch (MalformedURLException mue)
		  {
			log.info("Stream error :" + currentFileOrURL,mue);
			showMessage("INVALID FILE");
		  }

		  // Set pan/gain.
		  try
		  {
			  theSoundPlayer.setGain(((double) gainValue / (double) maxGain));
			  theSoundPlayer.setPan((float) balanceValue);
		  }
		  catch (BasicPlayerException e2)
		  {
			log.info("Cannot set control",e2);
		  }

		  playerState = PLAY;
		  repaint();
		  log.info(titleText);
		}
	  }
	}

	/*-----------------------------------*/
	/*-- Pause/Resume the current File --*/
	/*-----------------------------------*/
	else if (e.getActionCommand().equals("Pause"))
	{
	  if (playerState == PLAY)
	  {
		try
		{
			theSoundPlayer.pause();
		}
		catch (BasicPlayerException e1)
		{
			log.error("Cannot pause",e1);
		}
		playerState = PAUSE;
		offScreenGraphics.drawImage(iconsImage[1], iconsLocation[0], iconsLocation[1], this);
		offScreenGraphics.drawImage(iconsImage[4], iconsLocation[2], iconsLocation[3], this);
		repaint();
	  }
	  else if (playerState == PAUSE)
	  {
		try
		{
			theSoundPlayer.resume();
		}
		catch (BasicPlayerException e1)
		{
			log.info("Cannot resume",e1);
		}
		playerState = PLAY;
		offScreenGraphics.drawImage(iconsImage[0], iconsLocation[0], iconsLocation[1], this);
		offScreenGraphics.drawImage(iconsImage[3], iconsLocation[2], iconsLocation[3], this);
		repaint();
	  }
	}

	/*------------------*/
	/*-- Stop to play --*/
	/*------------------*/
	else if (e.getActionCommand().equals("Stop"))
	{
	  if ((playerState == PAUSE) || (playerState == PLAY))
	  {
		try
		{
			theSoundPlayer.stop();
		}
		catch (BasicPlayerException e1)
		{
			log.info("Cannot stop",e1);
		}
		playerState = STOP;
		secondsAmount = 0;
		acPosBar.setLocation(posBarBounds[0], posBarLocation[1]);
		offScreenGraphics.drawImage(iconsImage[2], iconsLocation[0], iconsLocation[1], this);
		offScreenGraphics.drawImage(iconsImage[4], iconsLocation[2], iconsLocation[3], this);
		repaint();
	  }
	}

	/*----------*/
	/*-- Next --*/
	/*----------*/
	else if (e.getActionCommand().equals("Next"))
	{
	  // Try to get next song from the playlist
	  playlist.nextCursor();
	  fileList.nextCursor();
	  PlaylistItem pli = playlist.getCursor();
	  this.setCurrentSong(pli);
	}

	/*--------------*/
	/*-- Previous --*/
	/*--------------*/
	else if (e.getActionCommand().equals("Previous"))
	{
	  // Try to get previous song from the playlist
	  playlist.previousCursor();
	  fileList.nextCursor();
	  PlaylistItem pli = playlist.getCursor();
	  this.setCurrentSong(pli);
	}

	/*--------------------------------------------*/
	/*--     Exit window through Exit Button    --*/
	/*--------------------------------------------*/
	else if (e.getActionCommand().equals("Exit"))
	{
	  closePlayer();
	}

	/*----------------------------------------------------*/
	/*--     Minimize window through Minimize Button    --*/
	/*----------------------------------------------------*/
	else if (e.getActionCommand().equals("Minimize"))
	{
	  // Iconify top frame.
	  topFrame.setLocation(OrigineX, OrigineY);
	  //topFrame.setState(Frame.ICONIFIED);
	  //topFrame.show();
	}

	/*--------------------------------------------*/
	/*-- Move full window through its Title Bar --*/
	/*--------------------------------------------*/
	else if (e.getActionCommand().equals("TitleBar"))
	{
	  //log.info("X="+acTitle.getMouseX()+" Y="+acTitle.getMouseY());
	  if (acTitleBar.isMousePressed() == false) FirstDrag = true;
	  else
	  {
		int DeltaX = 0;
		int DeltaY = 0;
		if (FirstDrag == false)
		{
		  DeltaX = acTitleBar.getMouseX() - XDrag;
		  DeltaY = acTitleBar.getMouseY() - YDrag;
		  XDrag = acTitleBar.getMouseX() - DeltaX;
		  YDrag = acTitleBar.getMouseY() - DeltaY;
		  OrigineX = OrigineX + DeltaX;
		  OrigineY = OrigineY + DeltaY;

		  if (config.isScreenLimit())
		  {
			// Keep player window in screen
			if (OrigineX < 0) OrigineX = 0;
			if (OrigineY < 0) OrigineY = 0;
			if (screenWidth != -1)
			{
			  if (OrigineX > screenWidth - WinWidth) OrigineX = screenWidth - WinWidth;
			}
			if (screenHeight != -1)
			{
			  if (OrigineY > screenHeight - WinHeight) OrigineY = screenHeight - WinHeight;
			}
		  }
		  // Moves top frame.
		  topFrame.setLocation(OrigineX, OrigineY);
		  topFrame.setSize(0, 0);
		  // Moves the main window + playlist
		  setLocation(OrigineX, OrigineY);
		  fileList.setLocation(OrigineX, OrigineY + WinHeight);
		  int factor = 1;
		  if (config.isPlaylistEnabled()) factor = 2;
		  equalizer.setLocation(OrigineX, OrigineY + WinHeight*factor);
		}
		else
		{
		  FirstDrag = false;
		  XDrag = acTitleBar.getMouseX();
		  YDrag = acTitleBar.getMouseY();
		}
	  }
	}
	/*-----------------------------------------*/
	/*--     Playlist window hide/display    --*/
	/*-----------------------------------------*/
	else if (e.getActionCommand().equals("Playlist"))
	{
	  if (acPlaylist.getCheckboxState())
	  {
		config.setPlaylistEnabled(true);
		if (config.isEqualizerEnabled())
		{
			equalizer.setLocation(OrigineX, OrigineY + WinHeight*2);
		}
		fileList.setVisible(true);
	  }
	  else
	  {
		config.setPlaylistEnabled(false);
		fileList.setVisible(false);
		if (config.isEqualizerEnabled())
		{
			equalizer.setLocation(OrigineX, OrigineY + WinHeight);
		}
	  }
	}

	/*--------------------------------------*/
	/*--     Playlist window equalizer    --*/
	/*--------------------------------------*/
	else if (e.getActionCommand().equals("Equalizer"))
	{
	  if (acEqualizer.getCheckboxState())
	  {
		config.setEqualizerEnabled(true);
		int factor = 1;
		if (config.isPlaylistEnabled()) factor = 2;
		equalizer.setLocation(OrigineX, OrigineY + WinHeight*factor);
		equalizer.setVisible(true);
	  }
	  else
	  {
		config.setEqualizerEnabled(false);
		equalizer.setVisible(false);
	  }
	}

	/*--------------------*/
	/*--     Shuffle    --*/
	/*--------------------*/
	else if (e.getActionCommand().equals("Shuffle"))
	{
	  if (acShuffle.getCheckboxState())
	  {
		config.setShuffleEnabled(true);
		if (playlist != null) {
			playlist.shuffle();
			fileList.initPlayList();
			// Play from the top
			PlaylistItem pli = playlist.getCursor();
			this.setCurrentSong(pli);
		}
	  } else
	  {
		config.setShuffleEnabled(false);
	  }
	}
	/*-------------------*/
	/*--     Repeat    --*/
	/*-------------------*/
	else if (e.getActionCommand().equals("Repeat"))
	{
	  if (acRepeat.getCheckboxState())
	  {
		config.setRepeatEnabled(true);
	  }
	  else
	  {
		config.setRepeatEnabled(false);
	  }
	}
	/*----------------------*/
	/*--     Equalizer    --*/
	/*----------------------*/
	else if (e.getActionCommand().equals("Equalizer"))
	{
	  if (acEqualizer.getCheckboxState())
	  {
		config.setEqualizerEnabled(true);
	  }
	  else
	  {
		config.setEqualizerEnabled(false);
	  }
	}

	else
	{
		// Unknown action.
	}
  }

  /**
   * Shows message in title an updates bitRate,sampleRate, Mono/Stereo,time features.
   */
  protected void showMessage(String titleText)
  {
	showTitle(titleText);
	offScreenGraphics.drawImage(sampleRateClearImage, sampleRateLocation[0], sampleRateLocation[1], this);
	offScreenGraphics.drawImage(bitsRateClearImage, bitsRateLocation[0], bitsRateLocation[1], this);
	offScreenGraphics.drawImage(passiveModeImage[0], stereoLocation[0], stereoLocation[1], this);
	offScreenGraphics.drawImage(passiveModeImage[1], monoLocation[0], monoLocation[1], this);
	offScreenGraphics.drawImage(timeImage[0], minuteDLocation[0], minuteDLocation[1], this);
	offScreenGraphics.drawImage(timeImage[0], minuteLocation[0], minuteLocation[1], this);
	offScreenGraphics.drawImage(timeImage[0], secondDLocation[0], secondDLocation[1], this);
	offScreenGraphics.drawImage(timeImage[0], secondLocation[0], secondLocation[1], this);
  }

  protected void showTitle(String titleText)
  {
	titleScrollImage = null;
	scrollIndex = 0;
	scrollRight = true;
	if (titleText.length() > TEXT_LENGTH_MAX)
	{
		int a = ((titleText.length())-(TEXT_LENGTH_MAX))+1;
		titleScrollImage = new Image[a];
		for (int k=0;k<a;k++)
		{
			String sText = titleText.substring(k, TEXT_LENGTH_MAX+k);
			titleScrollImage[k] = (new Taftb(fontIndex, imText, fontWidth, fontHeight, 0, sText)).getBanner();
		}
		titleText = titleText.substring(0, TEXT_LENGTH_MAX);
	}
	titleImage = (new Taftb(fontIndex, imText, fontWidth, fontHeight, 0, titleText)).getBanner();
	offScreenGraphics.drawImage(clearImage, titleLocation[0], titleLocation[1], this);
	offScreenGraphics.drawImage(titleImage, titleLocation[0], titleLocation[1], this);
  }

  public void paint(Graphics g)
  {
	if (offScreenImage != null)
	{
	  g.drawImage(offScreenImage, 0, 0, this);
	}
  }

  public void update(Graphics g)
  {
	paint(g);
  }

  /*-------------------------------------------*/
  /*--        WindowListener interface       --*/
  /*-------------------------------------------*/

  /**
   * Invoked when the window is set to be the user's active window,
   * which means the window (or one of its subcomponents) will receive
   * keyboard events.
   */
  public void windowActivated(WindowEvent e)
  {
	topFrame.setSize(0, 0);
	show();
  }

  /**
   * Invoked when a window has been closed as the result
   * of calling dispose on the window
   */
  public void windowClosed(WindowEvent e)
  {
	topFrame.setSize(0, 0);
  }

  /**
   * Invoked when the user attempts to close the window from the window's
   * system menu.
   */
  public void windowClosing(WindowEvent e)
  {
	// Closing window (Alt+F4 under Win32)
	closePlayer();
  }

  /**
   * Invoked when a window is no longer the user's active window,
   * which means that keyboard events will no longer be delivered to
   * the window or its subcomponents
   */
  public void windowDeactivated(WindowEvent e)
  {
	topFrame.setSize(0, 0);
  }

  /**
   * Invoked when a window is changed from a minimized to a normal state.
   */
  public void windowDeiconified(WindowEvent e)
  {
	topFrame.setLocation(OrigineX, OrigineY);
	topFrame.setSize(0, 0);
	//this.toFront();
	//topFrame.hide();
	// Show main window to fix Unix problem.
	show();
	// Show playlist window if needed.
	if (acPlaylist.getCheckboxState()) fileList.setVisible(true);
	else fileList.setVisible(false);
	// Show equalizer window if needed
	if (acEqualizer.getCheckboxState()) equalizer.setVisible(true);
	else equalizer.setVisible(false);

  }

  /**
   * Invoked when a window is changed from a normal to a minimized state.
   */
  public void windowIconified(WindowEvent e)
  {
	topFrame.setLocation(OrigineX, OrigineY);
	topFrame.setSize(0, 0);
	//topFrame.show();
  }

  /**
   * Invoked the first time a window is made visible.
   */
  public void windowOpened(WindowEvent e)
  {
	topFrame.setSize(0, 0);
  }

  /*-------------------------------------------*/
  /*--         Drag and drop interface       --*/
  /*-------------------------------------------*/

  /**
   * DnD : dragEnter implementation.
   */
  public void dragEnter(DropTargetDragEvent e)
  {
	if (isDragOk(e) == false)
	{
	  e.rejectDrag();
	  return;
	}
  }

  /**
   * DnD : dragOver implementation.
   */
  public void dragOver(DropTargetDragEvent e)
  {
	if (isDragOk(e) == false)
	{
	  e.rejectDrag();
	  return;
	}
  }

  /**
   * DnD : dragExit implementation.
   */
  public void dragExit(DropTargetEvent e)
  {
  }

  /**
   * DnD : dropActionChanged implementation.
   */
  public void dropActionChanged(DropTargetDragEvent e)
  {
	if (isDragOk(e) == false)
	{
	  e.rejectDrag();
	  return;
	}
  }

  /**
   * DnD : Drop implementation.
   * Adds all dropped files to the playlist.
   */
  public void drop(DropTargetDropEvent e)
  {
	// Check DataFlavor
	DataFlavor[] dfs = e.getCurrentDataFlavors();
	DataFlavor tdf = null;
	for (int i = 0; i < dfs.length; i++)
	{
	  if (DataFlavor.javaFileListFlavor.equals(dfs[i]))
	  {
		tdf = dfs[i];
		break;
	  }
	}
	// Is file list ?
	if (tdf != null)
	{
	  // Accept COPY DnD only.
	  if ((e.getSourceActions() & DnDConstants.ACTION_COPY) != 0)
	  {
		e.acceptDrop(DnDConstants.ACTION_COPY);
	  } else
		return;
	  try
	  {
		Transferable t = e.getTransferable();
		Object data = t.getTransferData(tdf);
		// How many files ?
		if (data instanceof java.util.List)
		{
		  java.util.List al = (java.util.List) data;
		  // Read the first File.
		  if (al.size() > 0)
		  {
			File file = null;
			// Stops the player if needed.
			if ((playerState == PLAY) || (playerState == PAUSE))
			{
			  theSoundPlayer.stop();
			  playerState = STOP;
			}
			// Clean the playlist.
			playlist.removeAllItems();
			// Add all dropped files to playlist.
			ListIterator li = al.listIterator();
			while (li.hasNext())
			{
			  file = (File) li.next();
			  PlaylistItem pli = null;
			  if (file != null)
			  {

				pli = new PlaylistItem(file.getName(), file.getAbsolutePath(), -1, true);
				if (pli != null) playlist.appendItem(pli);
			  }
			}
			// Start the playlist from the top.
			playlist.nextCursor();
			fileList.initPlayList();
			this.setCurrentSong(playlist.getCursor());
		  }
		}
		else
		{
			log.info("Unknown dropped objects");
		}
	  } catch (IOException ioe)
	  {
		log.info("Drop error",ioe);
		e.dropComplete(false);
		return;
	  } catch (UnsupportedFlavorException ufe)
	  {
		log.info("Drop error",ufe);
		e.dropComplete(false);
		return;
	  } catch (Exception ex)
	  {
		log.info("Drop error",ex);
		e.dropComplete(false);
		return;
	  }
	  e.dropComplete(true);
	}
  }

  /**
   * Checks if Drag allowed.
   */
  protected boolean isDragOk(DropTargetDragEvent e)
  {
	// Check DataFlavor
	DataFlavor[] dfs = e.getCurrentDataFlavors();
	DataFlavor tdf = null;
	for (int i = 0; i < dfs.length; i++)
	{
	  if (DataFlavor.javaFileListFlavor.equals(dfs[i]))
	  {
		tdf = dfs[i];
		break;
	  }
	}
	// Only file list allowed.
	if (tdf != null)
	{
	  // Only DnD COPY allowed.
	  if ((e.getSourceActions() & DnDConstants.ACTION_COPY) != 0)
	  {
		return true;
	  } else
		return false;
	} else
	  return false;
  }


  /*--------------------------------*/
  /*--            Misc            --*/
  /*--------------------------------*/

  /**
   * Try to compute time length in milliseconds.
   */
  public long getTimeLengthEstimation(Map properties)
  {
	  long milliseconds = -1;
	  int byteslength = -1;
	  if (properties != null)
	  {
		if (properties.containsKey("audio.length.bytes"))
		{
		  byteslength = ((Integer) properties.get("audio.length.bytes")).intValue();
		}
		if (properties.containsKey("duration"))
		{
			milliseconds = (int) (((Long) properties.get("duration")).longValue())/1000;
		}
		else
		{
			// Try to compute duration
			int bitspersample = -1;
			int channels = -1;
			float samplerate = -1.0f;
			int framesize = -1;
			if (properties.containsKey("audio.samplesize.bits"))
			{
				bitspersample = ((Integer) properties.get("audio.samplesize.bits")).intValue();
			}
			if (properties.containsKey("audio.channels"))
			{
				channels = ((Integer) properties.get("audio.channels")).intValue();
			}
			if (properties.containsKey("audio.samplerate.hz"))
			{
				samplerate = ((Float) properties.get("audio.samplerate.hz")).floatValue();
			}
			if (properties.containsKey("audio.framesize.bytes"))
			{
				framesize = ((Integer) properties.get("audio.framesize.bytes")).intValue();
			}
			if (bitspersample > 0)
			{
				milliseconds = (int) (1000.0f*byteslength/(samplerate * channels * (bitspersample/8)));
			}
			else
			{
				milliseconds = (int)(1000.0f*byteslength/(samplerate*framesize));
			}
		}
	  }
	  return milliseconds;
  }
  /**
   * Returns a File from a filename.
   */
  protected File openFile(String file)
  {
	return new File(file);
  }

  /**
   * Sets skin filename.
   */
  public void setSkin(String sk)
  {
	thePath = sk;
  }

  /**
   * Returns Playlist instance.
   */
  public Playlist getPlaylist()
  {
	return playlist;
  }

  /**
   * Returns Player state.
   */
  public int getPlayerState()
  {
	return playerState;
  }

  /**
   * Free ressources and close the player.
   */
  protected void closePlayer()
  {
	if ((playerState == PAUSE) || (playerState == PLAY))
	{
	  try
	  {
		  theSoundPlayer.stop();
	  }
	  catch (BasicPlayerException e)
	  {
		log.error("Cannot stop",e);
	  }
	}
	config.setLocation(OrigineX, OrigineY);
	config.save();
	// Polis : Frame instead of Window.
	//topFrame.dispose();
	System.gc();
	exit(0);
  }

  /**
   * Kills the player.
   */
  public void exit(int status)
  {
	System.exit(status);
  }

  /**
   * Return playlist UI.
   * @return
   */
  public MP3FilesApplet getPlaylistUI()
  {
	return fileList;
  }

  /**
   * Return equalizer UI.
   * @return
   */
  public EqualizerApplet getEqualizerUI()
  {
	return equalizer;
  }

  /**
   * Force display of all components.
   */
  public void displayAll()
  {
	acVolume.display();
	acBalance.display();
	acPlay.display();
	acStop.display();
	acPrevious.display();
	acNext.display();
	acEject.display();
	acPosBar.display();
	acExit.display();
	acMinimize.display();
	acRepeat.display();
	acPlaylist.display();
	acEqualizer.display();
	acPause.display();
	acTitleBar.display();
	acShuffle.display();
	paintAll(getGraphics());
  }

  /**
   * Refresh Playlist.
   */
  protected void resetPlaylist()
  {
	playlist.begin();
	if (fileList != null) fileList.initPlayList();
	this.setCurrentSong(playlist.getCursor());
	if (fileList != null) fileList.repaint();
	repaint();
  }

  /*--------------------------------------*/
  /*--  Methods for scriptable Applet   --*/
  /*--  AccessController.doPrivileged   --*/
  /*--  is needed for Java 1.4+ plugin  --*/
  /*--------------------------------------*/

  /**
   * Simulates "Play" selection.
   */
  public void pressStart()
  {
	AccessController.doPrivileged(new PrivilegedAction() {
		public Object run()
		{
			acPlay.fireEvent();
			return null;
		}
	});
  }

  /**
   * Simulates "Pause" selection.
   */
  public void pressPause()
  {
	AccessController.doPrivileged(new PrivilegedAction() {
		public Object run()
		{
			acPause.fireEvent();
			return null;
		}
	});
  }

  /**
   * Simulates "Stop" selection.
   */
  public void pressStop()
  {
	AccessController.doPrivileged(new PrivilegedAction() {
		public Object run()
		{
			acStop.fireEvent();
			return null;
		}
	});
  }

  /**
   * Simulates "Next" selection.
   */
  public void pressNext()
  {
	AccessController.doPrivileged(new PrivilegedAction() {
		public Object run()
		{
			acNext.fireEvent();
			return null;
		}
	});
  }

  /**
   * Simulates "Previous" selection.
   */
  public void pressPrevious()
  {
	AccessController.doPrivileged(new PrivilegedAction() {
		public Object run()
		{
			acPrevious.fireEvent();
			return null;
		}
	});
  }

  /**
   * Simulates "Eject" selection.
   */
  public void pressEject()
  {
	AccessController.doPrivileged(new PrivilegedAction() {
		public Object run()
		{
			acEject.fireEvent();
			return null;
		}
	});
  }

  /**
   * Load skin.
   */
  public void loadMySkin(final String skn)
  {
	AccessController.doPrivileged(new PrivilegedAction() {
		public Object run()
		{
			loadSkin(skn);
			return null;
		}
	});
  }

  /**
   * Reset Playlist.
   */
  public void resetMyPlaylist()
  {
	AccessController.doPrivileged(new PrivilegedAction() {
		public Object run()
		{
			resetPlaylist();
			return null;
		}
	});
  }

  /**
   * Load a playlist.
   * @param playlistName
   */
  public void loadMyPlaylist(final String playlistName)
  {
	AccessController.doPrivileged(new PrivilegedAction() {
		public Object run()
		{
			loadPlaylist(playlistName);
			return null;
		}
	});
  }

  /**
   * Returns Playlist Dump.
   */
  public String getPlaylistDump()
  {
	String plist = "";
	if (this.playlist != null)
	{
	  for (int i=0;i<playlist.getPlaylistSize();i++)
	  {
		PlaylistItem pli = playlist.getItemAt(i);
		plist = plist + "#"+pli.getName() + "|" + pli.getLocation();
	  }
	}
	return plist;
  }

  /**
   * Simulates "Shuffle" selection.
   */
  public void pressShuffle()
  {
	final MouseEvent smevt = new MouseEvent(this, MouseEvent.MOUSE_RELEASED, 0, 1, 0, 0, 1, false);
	AccessController.doPrivileged(new PrivilegedAction() {
		public Object run()
		{
			acShuffle.processEvent(smevt);
			return null;
		}
	});
  }

  /**
   * Simulates "Repeat" selection.
   */
  public void pressRepeat()
  {
	final MouseEvent rmevt = new MouseEvent(this, MouseEvent.MOUSE_RELEASED, 0, 1, 0, 0, 1, false);
	AccessController.doPrivileged(new PrivilegedAction() {
		public Object run()
		{
			acRepeat.processEvent(rmevt);
			return null;
		}
	});
  }

  /**
   * Returns Gain in [0-100] range.
   */
  public int getGain()
  {
	return gainValue;
  }

  /**
   * Set Gain value in [0,100]
   * @param val
   */
  public void setGain(String val)
  {
	try
	{
		gainValue = Integer.parseInt(val);
		if (gainValue < 0) gainValue = 0;
		if (gainValue > maxGain) gainValue = maxGain;
		if (gainValue == 0) theSoundPlayer.setGain(0);
		else theSoundPlayer.setGain(((double) gainValue / (double) maxGain));
	} catch (NumberFormatException nfe)
	  {
		log.info("Cannot set gain",nfe);
	  }
	catch (BasicPlayerException e)
	{
		log.debug("Cannot set gain",e);
	}
  }

  /**
   * Returns current song path.
   * @return
   */
  public String getCurrentSongPath()
  {
	return currentFileOrURL;
  }

  /**
   * Returns current song name.
   * @return
   */
  public String getCurrentSongName()
  {
	return currentSongName;
  }

  /**
   * Returns Balance in [-1.0,+1.0] range.
   */
  public double getBalance()
  {
	return balanceValue;
  }

  /**
   * Set Balance value in [-1.0, +1.0]
   * @param val
   */
  public void setBalance(String val)
  {
  	try
  	{
		balanceValue = Double.parseDouble(val);
		if (balanceValue < -1.0) balanceValue = -1.0;
		if (balanceValue > +1.0) balanceValue = +1.0;
		theSoundPlayer.setPan((float) balanceValue);

  	} catch (NumberFormatException nfe)
  	  {
		log.info("Cannot set balance",nfe);
  	  }
  	  catch (BasicPlayerException e)
	  {
	    log.debug("Cannot set balance",e);
	  }
  }

  /**
   * Entry point.
   */
  public static void main(String[] args)
  {
	Player theGUI;
	String currentArg = null;
	String currentValue = null;
	String skin = null;
	for (int i = 0; i < args.length; i++)
	{
	  currentArg = args[i];
	  if (currentArg.startsWith("-"))
	  {
		if (currentArg.toLowerCase().equals("-init"))
		{
		  i++;
		  if (i >= args.length) usage("init value missing");
		  currentValue = args[i];
		  if (Config.startWithProtocol(currentValue))
			initConfig = currentValue;
		  else
			initConfig = currentValue.replace('\\', '/').replace('/', java.io.File.separatorChar);
		}
		else if (currentArg.toLowerCase().equals("-song"))
		{
		  i++;
		  if (i >= args.length) usage("song value missing");
		  currentValue = args[i];
		  if (Config.startWithProtocol(currentValue))
			initSong = currentValue;
		  else
			initSong = currentValue.replace('\\', '/').replace('/', java.io.File.separatorChar);
		}
		else if (currentArg.toLowerCase().equals("-start"))
		{
		  autoRun = true;
		}
		else if (currentArg.toLowerCase().equals("-showplaylist"))
		{
		  showPlaylist = "true";
		}
		else if (currentArg.toLowerCase().equals("-showequalizer"))
		{
		  showEqualizer = "true";
		}
		else if (currentArg.toLowerCase().equals("-skin"))
		{
		  i++;
		  if (i >= args.length) usage("skin value missing");
		  currentValue = args[i];
		  if (Config.startWithProtocol(currentValue))
			skin = currentValue;
		  else
			skin = currentValue.replace('\\', '/').replace('/', java.io.File.separatorChar);
		}
		else if (currentArg.toLowerCase().equals("-v"))
		{
		  i++;
		  if (i >= args.length) usage("skin version value missing");
		  skinVersion = args[i];
		}
		else usage("Unknown parameter : " + currentArg);
	  }
	  else
	  {
		usage("Invalid parameter :" + currentArg);
	  }
	}
	// Instantiate AWT front-end.
	theGUI = new Player(skin, new Frame(TITLETEXT));
	// Instantiate low-level player.
	BasicPlayer bplayer = new BasicPlayer();
	// Register the front-end to low-level player events.
	bplayer.addBasicPlayerListener(theGUI);
	// Adds controls for front-end to low-level player.
	theGUI.setController(bplayer);
	// Display.
	theGUI.show();
	if (autoRun == true) theGUI.pressStart();
  }

  /**
   * Displays usage.
   */
  protected static void usage(String msg)
  {
	System.out.println(TITLETEXT + " : " + msg);
	System.out.println("");
	System.out.println(TITLETEXT + " : Usage");
	System.out.println("              java javazoom.jlgui.player.amp.Player [-skin skinFilename] [-song audioFilename] [-start] [-showplaylist] [-showequalizer] [-init configFilename] [-v skinversion]");
	System.out.println("");
	System.out.println("              skinFilename   : Filename or URL to a Winamp Skin2.x");
	System.out.println("              audioFilename  : Filename or URL to initial song or playlist");
	System.out.println("              start          : Starts playing song (from the playlist)");
	System.out.println("              showplaylist   : Show playlist");
	System.out.println("              showequalizer  : Show equalizer");

	System.out.println("");
	System.out.println("              Advanced parameters :");
	System.out.println("              skinversion    : 1 or 2 (default 1)");
	System.out.println("              configFilename : Filename or URL to jlGui initial configuration (playlist,skin,parameters ...)");
	System.out.println("                               Initial configuration won't be overriden by -skin and -song arguments");
	System.out.println("");
	System.out.println("Homepage    : http://www.javazoom.net");
	System.exit(0);
  }
}
