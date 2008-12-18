/*
 * MP3FilesApplet.
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

package javazoom.jlgui.player.amp.playlist.ui;

import java.applet.Applet;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.Vector;

import javazoom.jlgui.player.amp.PlayerApplet;
import javazoom.jlgui.player.amp.playlist.Playlist;
import javazoom.jlgui.player.amp.playlist.PlaylistItem;
import javazoom.jlgui.player.amp.skin.ActiveComponent;
import javazoom.jlgui.player.amp.skin.SkinLoader;
import javazoom.jlgui.player.amp.skin.UrlDialog;
import javazoom.jlgui.player.amp.tag.TagInfo;
import javazoom.jlgui.player.amp.tag.TagInfoFactory;
import javazoom.jlgui.player.amp.tag.ui.TagInfoDialog;
import javazoom.jlgui.player.amp.util.Config;
import javazoom.jlgui.player.amp.util.FileSelector;

/**
 * MP3FilesApplet.
 * This class implements a playlist UI WinAmp javazoom.jlgui.player.amp.skins compliant.
 *
 * @author:	JOHN YANG
 * @date:   02/11/2002
 */
public class MP3FilesApplet extends Panel implements ActionListener
{
  //private FileDialog          FD = null ; // use for add music to playlist

  private String pledit = "pledit.txt";
  private Color current = new Color(102, 204, 255);
  private Color normal = new Color(0xb2, 0xe4, 0xf6);
  private Color selBg = Color.black;
  private Color bgcolor = Color.black;

  private int topIndex = 0;
  private int currentSelection = -1;
//    private Vector    names ;
//    private Vector    dirs ;
//    private byte []   selected = new byte [100] ;

  private Image imWinamp;
  private Image listBack;
  private int WinWidth, WinHeight;
  private int listarea [] = {12, 24 - 4, 256, 78}; //172} ;
  private Image offScreenImage;
  private Graphics offScreenGraphics;
  private Graphics gG = null;

  private Image scrollBarNormal;
  private Image scrollBarClicked;
  private boolean FirstBarDrag = true;
  private int[] scrollBarRange = {20, 75 - 15};
  private int scrollBarX = -1;
  private int scrollBarLocation = scrollBarRange[0];
  private int XBarDrag = 0;

  private Image titleCenter;
  private Image titleLeft;
  private Image titleRight;
  private Image titleStretch;

  private Image btmLeft;
  private Image btmRight;

  private Image bodyLeft;
  private Image bodyRight;

  private Image downScrollButton;
  private Image upScrollButton;
  private ActiveComponent acUpScrollButton;
  private ActiveComponent acDownScrollButton;

  private ActiveComponent acScrollBar;
  //private Window addFileWnd = null;
  //private Window subFileWnd = null;
  //private Window selFileWnd = null;
  //private Window optFileWnd = null;
  private Panel addFileWnd = null;
  private Panel subFileWnd = null;
  private Panel selFileWnd = null;
  private Panel optFileWnd = null;
  private final int gapsInButtons = 29;
  private int[] addarea = {14, 116 - 38 + 6, 14 + 22, 116 - 38 + 6 + 18};

  private Playlist playlist = null;
  private Config config = null;
  private PlayerApplet player = null;
  // add by John Yang: for add Dir functions
  private Vector exts = null;
  public static int MAXDEPTH = 4;
  private boolean isSearching;

  private PopupMenu fipopup = null;

  /**
   * Constructor.
   */
  public MP3FilesApplet(Applet parent, PlayerApplet player, Playlist playlist, SkinLoader skl, int xPos, int yPos, boolean showit)
  {
    //super(parent);
    setLayout(null);
    addMouseListener(new MouseAdapter()
    {
      public void mousePressed(MouseEvent e)
      {
        handleMouseClick(e);
      }
    });
    this.playlist = playlist;
    this.player = player;
    // Config feature.
    config = Config.getInstance();

    //for ( int i = 0 ; i < 100 ; i ++ ) selected [i] = 0 ;
    //names = new Vector () ;
    //dirs = new Vector () ;

    getPleditInfo(skl);

    imWinamp = skl.getImage("pledit.bmp");
    WinWidth = 275;
    WinHeight = 116;
    // E.B Fix : WinWidth - 15 not WinWidth - 13
    scrollBarX = WinWidth - 15;

    // added 07/02/2002
    // E.B Fix : Size=8*18 not 5*15
    scrollBarNormal = parent.createImage(8, 18);
    Graphics g = scrollBarNormal.getGraphics();
    g.drawImage(imWinamp, 0, 0, 8, 18, 52, 53, 52 + 8, 53 + 18, null);
    scrollBarClicked = parent.createImage(8, 18);
    g = scrollBarClicked.getGraphics();
    g.drawImage(imWinamp, 0, 0, 8, 18, 61, 53, 61 + 8, 53 + 18, null);

    acScrollBar = new ActiveComponent(scrollBarNormal, scrollBarClicked, AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
    acScrollBar.setLocation(scrollBarX, scrollBarLocation);
    add(acScrollBar);
    acScrollBar.setActionCommand("Scroll");
    acScrollBar.addActionListener(this);

    titleCenter = parent.createImage(100, 20);
    g = titleCenter.getGraphics();
    g.drawImage(imWinamp, 0, 0, 100, 20, 26, 0, 126, 20, null);
    titleLeft = parent.createImage(25, 20);
    g = titleLeft.getGraphics();
    g.drawImage(imWinamp, 0, 0, 25, 20, 0, 0, 25, 20, null);
    titleStretch = parent.createImage(25, 20);
    g = titleStretch.getGraphics();
    g.drawImage(imWinamp, 0, 0, 25, 20, 127, 0, 152, 20, null);
    titleRight = parent.createImage(25, 20);
    g = titleRight.getGraphics();
    g.drawImage(imWinamp, 0, 0, 25, 20, 153, 0, 178, 20, null);

    btmLeft = parent.createImage(125, 38);
    g = btmLeft.getGraphics();
    g.drawImage(imWinamp, 0, 0, 125, 38, 0, 72, 125, 110, null);
    btmRight = parent.createImage(150, 38);
    g = btmRight.getGraphics();
    // E.B Fix : 127 -> 126, 277 -> 276
    g.drawImage(imWinamp, 0, 0, 150, 38, 126, 72, 276, 110, null);

    bodyLeft = parent.createImage(12, 28);
    g = bodyLeft.getGraphics();
    g.drawImage(imWinamp, 0, 0, 12, 28, 0, 42, 12, 70, null);
    bodyRight = parent.createImage(20, 28);
    g = bodyRight.getGraphics();
    g.drawImage(imWinamp, 0, 0, 20, 28, 31, 42, 51, 70, null);


    upScrollButton = parent.createImage(8, 4);
    g = upScrollButton.getGraphics();
    g.drawImage(imWinamp, 0, 0, 8, 4, 261, 75, 269, 79, null);
    acUpScrollButton = new ActiveComponent(upScrollButton, upScrollButton, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
    acUpScrollButton.setLocation(WinWidth - 15, WinHeight - 35);
    add(acUpScrollButton);
    acUpScrollButton.setActionCommand("ScrollUp");
    acUpScrollButton.addActionListener(this);


    downScrollButton = parent.createImage(8, 4);
    g = downScrollButton.getGraphics();
    g.drawImage(imWinamp, 0, 0, 8, 4, 261, 80, 269, 84, null);
    acDownScrollButton = new ActiveComponent(downScrollButton, downScrollButton, AWTEvent.MOUSE_EVENT_MASK);
    acDownScrollButton.setLocation(WinWidth - 15, WinHeight - 30);
    add(acDownScrollButton);
    acDownScrollButton.setActionCommand("ScrollDown");
    acDownScrollButton.addActionListener(this);


    // Popup menu on TitleBar
    fipopup = new PopupMenu("FileInfo");
	Font fnt = new Font("Dialog", Font.PLAIN, 11);
	fipopup.setFont(fnt);
    MenuItem mi = new MenuItem("File Info");
    mi.addActionListener(this);
    fipopup.add(mi);
    fipopup.addSeparator();
    mi = new MenuItem("Play Item");
    mi.addActionListener(this);
    fipopup.add(mi);
    fipopup.addSeparator();
    mi = new MenuItem("Remove Item(s)");
    mi.addActionListener(this);
    fipopup.add(mi);
    this.add(fipopup);

    setSize(WinWidth, WinHeight);
    setLocation(xPos, yPos);
    //setBackground(Color.black);
    show(showit); // changed to non-deprecated code (was show())

    //pack();
  }

  /**
   * @function: initialize the pledit display
   * @date:     02/11/2002
   */
  public void initPlayList()
  {
    //for ( int i = 0 ; i < 100 ; i ++ ) selected [i] = 0 ;
    topIndex = 0;
    nextCursor();
  }

  /**
   * @function: Repaint the file list area and scroll it if necessary
   * @date:     02/07/2002
   */
  public void nextCursor()
  {
    currentSelection = playlist.getSelectedIndex();
    int n = playlist.getPlaylistSize();
    int nlines = (listarea[3] - listarea[1]) / 12;

    while (currentSelection - topIndex > nlines - 1) topIndex += 2;
    if (topIndex >= n) topIndex = n - 1;
    while (currentSelection < topIndex) topIndex -= 2;
    if (topIndex < 0) topIndex = 0;

    // reset the vertical scroll bar position
    resetScrollBar();
    repaint();
  }

  private void getPleditInfo(SkinLoader skl)
  {
    String tmp = (String) skl.getContent(pledit);
    tmp = tmp.toLowerCase();
    ByteArrayInputStream in = new ByteArrayInputStream(tmp.getBytes());
    // changed deprecated code (Scott Pennell)
    BufferedReader lin = new BufferedReader(new InputStreamReader(in));

    try
    {
      for (; ;)
      {
        String aa = lin.readLine();
        if (aa == null) break;
        if ((aa.toLowerCase()).startsWith("normalbg"))
          bgcolor = parseColor(aa);
        else if ((aa.toLowerCase()).startsWith("normal"))
          normal = parseColor(aa);
        else if ((aa.toLowerCase()).startsWith("current"))
          current = parseColor(aa);
        else if ((aa.toLowerCase()).startsWith("selectedbg"))
          selBg = parseColor(aa);
      }
    } catch (Exception e9)
    {
    }

    try
    {
      in.close();
    } catch (Exception ex)
    {
    }
  }

  private Color parseColor(String aa) throws Exception
  {
    int pos = aa.indexOf("#");
    if (pos == -1)
    {
      // Some javazoom.jlgui.player.amp.skins are buggy :-)
      pos = aa.indexOf("=");
      if (pos == -1) throw new Exception("Can not parse color!");
    }
    aa = aa.substring(pos + 1);
    int r = Integer.parseInt(aa.substring(0, 2), 16);
    int g = Integer.parseInt(aa.substring(2, 4), 16);
    int b = Integer.parseInt(aa.substring(4), 16);
    return new Color(r, g, b);
  }

  public void paint(Graphics g)
  {
    if (offScreenImage == null)
    {
      offScreenImage = createImage(WinWidth, WinHeight);
      offScreenGraphics = offScreenImage.getGraphics();
      offScreenGraphics.drawImage(titleLeft, 0, 0, this);
      offScreenGraphics.drawImage(titleStretch, 25, 0, this);
      offScreenGraphics.drawImage(titleStretch, 50, 0, this);
      offScreenGraphics.drawImage(titleStretch, 62, 0, this);
      offScreenGraphics.drawImage(titleCenter, 87, 0, this);
      offScreenGraphics.drawImage(titleStretch, 187, 0, this);
      offScreenGraphics.drawImage(titleStretch, 200, 0, this);
      offScreenGraphics.drawImage(titleStretch, 225, 0, this);
      offScreenGraphics.drawImage(titleRight, 250, 0, this);
      offScreenGraphics.drawImage(bodyLeft, 0, 20, this);
      offScreenGraphics.drawImage(bodyLeft, 0, 48, this);
      offScreenGraphics.drawImage(bodyLeft, 0, 50, this);
      offScreenGraphics.drawImage(btmLeft, 0, WinHeight - 38, this);
      // E.B Fix : 126 -> 125
      offScreenGraphics.drawImage(btmRight, 125, WinHeight - 38, this);
      titleCenter = null;
      titleLeft = null;
      titleRight = null;
      titleStretch = null;
      btmLeft = null;
      bodyLeft = null;
      btmRight = null;
      System.gc();
    }

    // E.B Fix : Draw list in offScreenGraphics now to make
    // long text fit to playlist UI.
    offScreenGraphics.setFont(new Font("Dialog", Font.PLAIN, 10));
    paintList(offScreenGraphics);
    offScreenGraphics.drawImage(bodyRight, WinWidth - 20, 20, this);
    offScreenGraphics.drawImage(bodyRight, WinWidth - 20, 48, this);
    offScreenGraphics.drawImage(bodyRight, WinWidth - 20, 50, this);
    // bodyRight = null ;
    // End Fix

    if (offScreenImage != null)
      g.drawImage(offScreenImage, 0, 0, this);
  }

  public void update(Graphics g)
  {
  	paint(g);
  }
  
  public void paintList()
  {
    if (!isVisible())
      return;

    else
      repaint();

    // E.B Fix : Draw list in offScreenGraphics.
    /*
    if ( gG == null )
    {
        gG = getGraphics () ;
        gG.setFont (new Font("Dialog", Font.PLAIN, 10)) ;
    }
    paintList (gG);
    */
  }

  private void paintList(Graphics g)
  {
    g.setColor(bgcolor);
    g.fillRect(listarea[0], listarea[1], listarea[2] - listarea[0], listarea[3] - listarea[1]);

    currentSelection = playlist.getSelectedIndex();
    int offset = currentSelection - topIndex;

    g.setColor(normal);

    int n = playlist.getPlaylistSize();
/*
        int nlines = (listarea[3] - listarea[1]) / 12 ;
        while ( currentSelection - topIndex > nlines-1 ) topIndex += 2 ;
        if ( topIndex > n ) topIndex = n - 1 ;
        while ( currentSelection < topIndex ) topIndex -= 2 ;
        if ( topIndex < 0 ) topIndex = 0 ;
*/

    for (int i = 0; i < n; i++)
    {
      if (i < topIndex) continue;
      int k = i - topIndex;

      if (listarea[1] + 12 + k * 12 > listarea[3])
        break;

      PlaylistItem pli = playlist.getItemAt(i);
      String name = pli.getFormattedName();

//            if ( selected [i] == 1 )  // draw selected item's background
      if (pli.isSelected())
      {
        g.setColor(selBg);
        g.fillRect(listarea[0] + 4, listarea[1] + 12 - 10 + k * 12,
                   listarea[2] - listarea[0] - 4, 14);
      }

      if (i == currentSelection)
        g.setColor(current);
      else
        g.setColor(normal);

      if (i + 1 >= 10)
        g.drawString((i + 1) + ".  " + name,
                     listarea[0] + 12, listarea[1] + 12 + k * 12);
      else
        g.drawString("0" + (i + 1) + ".  " + name,
                     listarea[0] + 12, listarea[1] + 12 + k * 12);
/*
            if ( selected [i] == 1 )
                g.drawString ("+", listarea[0] + 4, listarea[1] + 12 + k*12) ;
*/
      if (i == currentSelection)
        g.setColor(normal);
    }
  }

  public void actionPerformed(ActionEvent e)
  {
    String cmd = e.getActionCommand();

    int n = playlist.getPlaylistSize();

    if (cmd.equals("Scroll"))
    {
      if (acScrollBar.isMousePressed() == false)
      {
        FirstBarDrag = true;
      } else
      {
        int DeltaY = 0;
        if (FirstBarDrag == false)
        {
          DeltaY = acScrollBar.getMouseY() - XBarDrag;
          XBarDrag = acScrollBar.getMouseY() - DeltaY;
          scrollBarLocation += DeltaY;
          if (scrollBarLocation < scrollBarRange[0]) scrollBarLocation = scrollBarRange[0];
          if (scrollBarLocation > scrollBarRange[1]) scrollBarLocation = scrollBarRange[1];
          acScrollBar.setLocation(scrollBarX, scrollBarLocation);
          float dx = ((float) scrollBarLocation - scrollBarRange[0]) / (scrollBarRange[1] - scrollBarRange[0]);
          int index = (int) (dx * (n - 1));
          if (index != topIndex)
          {
            topIndex = index;
            paintList();
          }
        } else
        {
          FirstBarDrag = false;
          XBarDrag = acScrollBar.getMouseY();
        }
      }
    } else if (cmd.equals("ScrollUp"))
    {
      topIndex--;
      if(topIndex<0) topIndex=0;
      resetScrollBar();
      paintList();

    } else if (cmd.equals("ScrollDown"))
    {
      topIndex++;
      if(topIndex>n-1) topIndex=n-1;
      resetScrollBar();
      paintList();
    }
    // Add File event
    // E.B : FileSelector added as M.S did.
    else if (cmd.equals("Add File"))
    {
      addFileWnd.setVisible(false);
      Frame f = new Frame();
      f.setLocation(this.getBounds().x, this.getBounds().y + 10);
      FileSelector.setWindow(f);
      String fsFile = FileSelector.selectFile(FileSelector.OPEN, config.getExtensions(), config.getLastDir());
      fsFile = FileSelector.getFile();
      if (fsFile != null)
      {
        config.setLastDir(FileSelector.getDirectory());
        if ((!fsFile.toLowerCase().endsWith(".wsz")) && (!fsFile.toLowerCase().endsWith(".m3u")) && (!fsFile.toLowerCase().endsWith(".pls")))
        {
          PlaylistItem pli = new PlaylistItem(fsFile, config.getLastDir() + fsFile, -1, true);
          playlist.appendItem(pli);
          resetScrollBar();
          repaint();
        }
      }
    }
	// E.B : Added URL dialog.
    else if (cmd.equals("Add Url"))
    {
      addFileWnd.setVisible(false);
      UrlDialog UD = new UrlDialog("Open location", this.getBounds().x, this.getBounds().y + 10, 280, 120, null);
      UD.show();
      if (UD.getFile() != null)
      {
		displayAll();
		if (player != null) player.displayAll();
		if (player.getEqualizerUI()!= null) player.getEqualizerUI().displayAll();
        PlaylistItem pli = new PlaylistItem(UD.getFile(), UD.getURL(), -1, false);
        playlist.appendItem(pli);
        resetScrollBar();
        repaint();
      }
    }
    // John.Yang : add 02/12/2002
    else if (cmd.equals("Add Dir"))
    {
      addFileWnd.setVisible(false);
      Frame f = new Frame();
      f.setLocation(this.getBounds().x, this.getBounds().y + 10);
      FileSelector.setWindow(f);
      String fsFile = FileSelector.selectFile(FileSelector.OPEN, "*", config.getLastDir());
      fsFile = FileSelector.getDirectory();
      if (fsFile.endsWith(File.separator)) fsFile = fsFile.substring(0, fsFile.length() - 1);
      try
      {
        File dir = new File(fsFile);
        if (dir == null || !dir.isDirectory())
          return;    // we need directory!
        addDir(dir);
      } catch (Exception ex)
      {
        System.out.println(ex.getMessage());
      }
    } else if (cmd.equals("Del File"))
    {
      subFileWnd.setVisible(false);
      delSelectedItems();
    } else if (cmd.equals("Del All"))
    {
      subFileWnd.setVisible(false);
      delAllItems();
    } else if (cmd.equals("Inv Sel"))
      selFunctions(-1);
    else if (cmd.equals("Sel 0"))
      selFunctions(0);
    else if (cmd.equals("Sel All"))
      selFunctions(1);
    else if (cmd.equals("Remove Item(s)"))
      delSelectedItems();
    else if (cmd.equals("Play Item"))
    {
      int n0 = playlist.getPlaylistSize();
      PlaylistItem pli = null;
      for (int i = n0 - 1; i >= 0; i--)
      {
        pli = playlist.getItemAt(i);
        if (pli.isSelected()) break;
      }
      // Play.
      if ( (pli != null) && (pli.getTagInfo() != null))
      {
        player.pressStop();
        player.setCurrentSong(pli);
        playlist.setCursor(playlist.getIndex(pli));
        player.pressStart();
      }
    }
    else if (cmd.equals("File Info"))
    {
      int n0 = playlist.getPlaylistSize();
      PlaylistItem pli = null;
      for (int i = n0 - 1; i >= 0; i--)
      {
        pli = playlist.getItemAt(i);
        if (pli.isSelected()) break;
      }
      // Display Tag Info.
      if ( (pli != null) && (pli.getTagInfo() != null))
      {
        TagInfo taginfo = pli.getTagInfo();
        if (taginfo != null)
        {
          TagInfoFactory factory = TagInfoFactory.getInstance();
          TagInfoDialog dialog = factory.getTagInfoDialog(taginfo);
          dialog.setLocation(this.getBounds().x, this.getBounds().y);
		  dialog.show(); 
        }
      }
    }
  }

  protected void handleMouseClick(MouseEvent evt)
  {
    if (addFileWnd != null && addFileWnd.isVisible()) addFileWnd.setVisible(false);
    if (subFileWnd != null && subFileWnd.isVisible()) subFileWnd.setVisible(false);
    if (selFileWnd != null && selFileWnd.isVisible()) selFileWnd.setVisible(false);
    if (optFileWnd != null && optFileWnd.isVisible()) optFileWnd.setVisible(false);

    int x = evt.getX();
    int y = evt.getY();

    if (x >= addarea[0] && x <= addarea[2] && y >= addarea[1] && y <= addarea[3])
    {
      handleAddEvent();
      return;
    }

    if (x >= addarea[0] + gapsInButtons && x <= addarea[2] + gapsInButtons && y >= addarea[1] && y <= addarea[3])
    {
      handleSubEvent();
      return;
    }

    if (x >= addarea[0] + 2 * gapsInButtons && x <= addarea[2] + 2 * gapsInButtons && y >= addarea[1] && y <= addarea[3])
    {
      handleSelEvent();
      return;
    }

    if (x >= addarea[0] + 3 * gapsInButtons && x <= addarea[2] + 3 * gapsInButtons && y >= addarea[1] && y <= addarea[3])
    {
      handleOptEvent();
      return;
    }

    // check select action
    if (x >= listarea[0] && x <= listarea[2] && y >= listarea[1] && y <= listarea[3])
    {
      int index = getIndex(y);
      if (index != -1)
      {
        // PopUp
        if (evt.getModifiers() == MouseEvent.BUTTON3_MASK)
        {
          if (fipopup != null)
          {
            fipopup.show(evt.getComponent(), evt.getX(), evt.getY());
          }
        }
        else
        {
          PlaylistItem pli = playlist.getItemAt(index);
          if (pli != null)
          {
            pli.setSelected(!pli.isSelected());
            // CK 02/25/2002: add play on double-click
            if ( (evt.getClickCount() == 2) && (evt.getModifiers() == MouseEvent.BUTTON1_MASK))
            {
              player.pressStop();
              player.setCurrentSong(pli);
              playlist.setCursor(index);
              player.pressStart();
            }
          }
          //  selected [index] = (byte) (1 - selected [index]) ;
        }
        repaint();
      }
    }
  }

  private void handleSelEvent()
  {
    if (selFileWnd == null)
      createSelFileWnd();

    if (selFileWnd != null && selFileWnd.isVisible())
      selFileWnd.setVisible(false);
    else
    {
      // display it
      Point pt = getLocation();
      selFileWnd.setLocation(pt.x + addarea[0] + 2 * gapsInButtons, pt.y + (addarea[1] - (18 * 3)) + 20); // we got 3 rows each 18 high
      // E.B Fix : setSize for avoid narrow pack.
      selFileWnd.setSize(22, 18 * 3);
      selFileWnd.setVisible(true);
    }
  }

  private void handleOptEvent()
  {
    if (optFileWnd == null)
      createOptFileWnd();

    if (optFileWnd != null && optFileWnd.isVisible())
      optFileWnd.setVisible(false);
    else
    {
      // display it
      Point pt = getLocation();
      optFileWnd.setLocation(pt.x + addarea[0] + 3 * gapsInButtons, pt.y + (addarea[1] - (18 * 3)) + 20); // we got 3 rows each 18 high
      // E.B Fix : setSize for avoid narrow pack.
      optFileWnd.setSize(22, 18 * 3);
      optFileWnd.setVisible(true);
    }
  }

  private void handleSubEvent()
  {
    if (subFileWnd == null)
      createSubFileWnd();

    if (subFileWnd != null && subFileWnd.isVisible())
      subFileWnd.setVisible(false);
    else
    {
      // display it
      Point pt = getLocation();
      subFileWnd.setLocation(pt.x + addarea[0] + gapsInButtons, pt.y + (addarea[1] - (18 * 4)) + 20); // we got 3 rows each 18 high
      // E.B Fix : setSize for avoid narrow pack.
      subFileWnd.setSize(22, 18 * 4);
      subFileWnd.setVisible(true);
    }
  }

  private void handleAddEvent()
  {
    if (addFileWnd == null)
      createAddFileWnd();

    if (addFileWnd != null && addFileWnd.isVisible())
      addFileWnd.setVisible(false);
    else
    {
      // display it
      Point pt = getLocation();
      addFileWnd.setLocation(pt.x + addarea[0], pt.y + (addarea[1] - (18 * 3)) + 20); // we got 3 rows each 18 high
      // E.B Fix : setSize for avoid narrow pack.
      addFileWnd.setSize(22, 18 * 3);
      addFileWnd.setVisible(true);
    }
  }

  private void createOptFileWnd()
  {
    //TODO
    //optFileWnd = new Window(this);
	optFileWnd = new Panel();
    optFileWnd.setLayout(new GridLayout(3, 1, 0, 0));  // 3 rows

    ActiveComponent comp = createPLButton(154, 111); // 153 -> 154
    optFileWnd.add(comp);
    comp = createPLButton(154, 130);
    optFileWnd.add(comp);
    comp = createPLButton(154, 149);
    optFileWnd.add(comp);

    optFileWnd.setSize(22, 18 * 3);
    //optFileWnd.pack();
    optFileWnd.setVisible(false);
  }

  private void createSelFileWnd()
  {
    // TODO
    //selFileWnd = new Window(this);
	selFileWnd = new Panel();
    selFileWnd.setLayout(new GridLayout(3, 1, 0, 0));  // 3 rows

    ActiveComponent comp = createPLButton(104, 111);   // 103 -> 104
    comp.setActionCommand("Inv Sel");
    comp.addActionListener(this);
    selFileWnd.add(comp);
    comp = createPLButton(104, 130);
    comp.setActionCommand("Sel 0");
    comp.addActionListener(this);
    selFileWnd.add(comp);
    comp = createPLButton(104, 149);
    comp.setActionCommand("Sel All");
    comp.addActionListener(this);
    selFileWnd.add(comp);

    selFileWnd.setSize(22, 18 * 3);
    //selFileWnd.pack();
    selFileWnd.setVisible(false);
  }

  private void createSubFileWnd()
  {
    // TODO
    //subFileWnd = new Window(this);
	subFileWnd = new Panel();
    subFileWnd.setLayout(new GridLayout(4, 1, 0, 0));  // 4 rows

    ActiveComponent comp = createPLButton(54, 111);
    comp.setActionCommand("Del All");
    comp.addActionListener(this);
    subFileWnd.add(comp);
    comp = createPLButton(54, 130);
    subFileWnd.add(comp);
    comp = createPLButton(54, 149);
    comp.setActionCommand("Del File");
    comp.addActionListener(this);
    subFileWnd.add(comp);
    comp = createPLButton(54, 168);
    subFileWnd.add(comp);

    subFileWnd.setSize(22, 18 * 4);
    //subFileWnd.pack();
    subFileWnd.setVisible(false);
  }

  private void createAddFileWnd()
  {
    //TODO
	//addFileWnd = new Window(this);
    addFileWnd = new Panel();		
    addFileWnd.setLayout(new GridLayout(3, 1, 0, 0));  // 3 rows

    // + FILE button
    ActiveComponent comp = createPLButton(0, 149);
    comp.setActionCommand("Add File");
    comp.addActionListener(this);
    // + DIR button
    ActiveComponent comp1 = createPLButton(0, 130);
    comp1.setActionCommand("Add Dir");
    comp1.addActionListener(this);
    // + URL button
    ActiveComponent comp2 = createPLButton(0, 111);
    comp2.setActionCommand("Add Url");
    comp2.addActionListener(this);

    addFileWnd.add(comp2);
    addFileWnd.add(comp1);
    addFileWnd.add(comp);
    // E.B Fix : setSize for avoid narrow pack.
    addFileWnd.setSize(22, 18 * 3);
    //addFileWnd.pack();    
    addFileWnd.setVisible(false);
  }

  /**
   * @function: create a button in pledit window
   */
  private ActiveComponent createPLButton(int sx, int sy)
  {
    Image normal = this.createImage(22, 18);
    Image clicked = this.createImage(22, 18);

    Graphics g = normal.getGraphics();
    g.drawImage(imWinamp, 0, 0, 22, 18, sx, sy, sx + 22, sy + 18, null);

    sx += 23;

    g = clicked.getGraphics();
    g.drawImage(imWinamp, 0, 0, 22, 18, sx, sy, sx + 22, sy + 18, null);

    ActiveComponent comp = new ActiveComponent(normal, clicked, AWTEvent.MOUSE_EVENT_MASK);
    return comp;
  }

  /**
   * @function: add all Music files under this directory to play list
   * @param:
   * @author:   John Yang
   * @date:     02/12/2002
   */
  private void addDir(File fsFile)
  {
    /**
     * put all music file extension in a Vector
     */
    String ext = config.getExtensions();
    StringTokenizer st = new StringTokenizer(ext, ",");
    if (exts == null)
    {
      exts = new Vector();
      while (st.hasMoreTokens()) exts.add("." + st.nextElement());
    }
    /**
     * recursive
     */
    Thread addThread = new AddThread(fsFile);
    addThread.start();
    /**
     * Refresh thread
     */
    Thread refresh = new Thread("Refresh")
    {
      public void run()
      {
        while (isSearching)
        {
          resetScrollBar();
          repaint();
          try
          {
            Thread.sleep(4000);
          } catch (Exception ex)
          {
          }
        }
      }
    };
    refresh.start();
  }

  class AddThread extends Thread
  {
    private File fsFile;

    public AddThread(File fsFile)
    {
      super("Add");
      this.fsFile = fsFile;
    }

    public void run()
    {
      isSearching = true;
      addMusicRecursive(fsFile, 0);
      isSearching = false;
      resetScrollBar();
      repaint();
    }
  }

  private void addMusicRecursive(File rootDir, int depth)
  {
    if (rootDir == null || depth > MAXDEPTH) return; // we do not want waste time
    String[] lists = rootDir.list();
    if (lists == null) return;	
    for (int i = 0; i < lists.length; i++)
    {
      File ff = new File(rootDir + File.separator + lists[i]);
      if (ff.isDirectory()) addMusicRecursive(ff, depth + 1);
      else
      {		
        if (isMusicFile(lists[i]))
        {
		  //System.out.println("Add " + rootDir+File.separator+lists[i]) ;
          PlaylistItem pli = new PlaylistItem(lists[i], rootDir + File.separator + lists[i], -1, true);
          playlist.appendItem(pli);
        }
      }
    }
  }

  private boolean isMusicFile(String ff)
  {
    int sz = exts.size();
    for (int i = 0; i < sz; i++)
    {
      String ext = exts.elementAt(i).toString().toLowerCase();	 
      if (ext.equalsIgnoreCase(".wsz") || ext.equalsIgnoreCase(".m3u") || ext.equalsIgnoreCase(".pls")) continue;
      if (ff.toLowerCase().endsWith(exts.elementAt(i).toString().toLowerCase())) return true;
    }
    return false;
  }

  /**
   * @function: selection operation in pledit window
   * @param:    mode:  -1 --- inverse selected items
   *                    0 --- select none
   *                    1 --- select all
   */
  private void selFunctions(int mode)
  {
    selFileWnd.setVisible(false);

    int n0 = playlist.getPlaylistSize();
    if (n0 == 0) return;

    for (int i = 0; i < n0; i++)
    {
      PlaylistItem pli = playlist.getItemAt(i);
      if (pli == null) break;
      if (mode == -1)
      {       // inverse selection
        pli.setSelected(!pli.isSelected());
      } else if (mode == 0)
      { // select none
        pli.setSelected(false);
      } else if (mode == 1)
      { // select all
        pli.setSelected(true);
      }
    }
/*
        if ( mode == -1 )    // inverse selection
        {
            for ( int i = 0 ; i < n0 ; i ++ ) {
                selected [i] = (byte) (1 - selected [i]) ;
            }
        }
        if ( mode == 0 )
        {
            for ( int i = 0 ; i < n0 ; i ++ )
                selected [i] = (byte) 0 ;
        }
        if ( mode == 1 )
        {
            for ( int i = 0 ; i < n0 ; i ++ )
                selected [i] = (byte) 1 ;
        }
*/
    repaint();
  }

  /**
   * @function: remove all items in playlist
   */
  private void delAllItems()
  {
    int n0 = playlist.getPlaylistSize();
    if (n0 == 0) return;
//        for ( int i = 0 ; i < n0 ; i ++ ) selected [i] = 0 ; // unset flag
    playlist.removeAllItems();
    topIndex = 0;
    acScrollBar.setLocation(scrollBarX, scrollBarRange[0]);
    repaint();
  }

  /**
   * @function: remove selected items in playlist
   */
  private void delSelectedItems()
  {
    int n0 = playlist.getPlaylistSize();
    boolean brepaint = false;
    for (int i = n0 - 1; i >= 0; i--)
    {
//            if ( selected [i] == 1 ) // selected item
      if (playlist.getItemAt(i).isSelected())
      {
        playlist.removeItemAt(i);
//                selected [i] = 0 ;
        brepaint = true;
      }
    }

    if (brepaint)
    {
      int n = playlist.getPlaylistSize();
      if (topIndex >= n) topIndex = n - 1;
      if (topIndex < 0) topIndex = 0;

      // reset the vertical scroll bar position
      resetScrollBar();
      repaint();
    }
  }

  /**
   * @function: get the item index according to the mouse y position
   * @date:     02/11/2002
   */
  protected int getIndex(int y)
  {
    int n0 = playlist.getPlaylistSize();
    if (n0 == 0)
      return -1;

    for (int n = 0; n < 100; n++)
    {
      if (y >= listarea[1] + 12 - 10 + n * 12 && y < listarea[1] + 12 - 10 + n * 12 + 14)
      {
        if (topIndex + n > n0 - 1)
          return -1;
        return topIndex + n;
      }
    }

    return -1;
  }

  private void resetScrollBar()
  {
    int n = playlist.getPlaylistSize();
    // reset the vertical scroll bar position
    float dx = (n < 1) ? 0 : ((float) topIndex / (n - 1)) * (scrollBarRange[1] - scrollBarRange[0]);
    scrollBarLocation = scrollBarRange[0] + (int) dx;
    acScrollBar.setLocation(scrollBarX, scrollBarLocation);
  }
  
  /**
   * Force display of all components.
   */  
  public void displayAll()
  {
	acDownScrollButton.display();
	acScrollBar.display();
	acUpScrollButton.display();
	paintAll(getGraphics());  	
  }  
}

