/*
 * EqualizerApplet.
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

package javazoom.jlgui.player.amp.equalizer.ui;

import java.applet.Applet;
import java.awt.AWTEvent;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javazoom.jlgui.player.amp.PlayerApplet;
import javazoom.jlgui.player.amp.skin.ActiveComponent;
import javazoom.jlgui.player.amp.skin.SkinLoader;
import javazoom.jlgui.player.amp.util.Config;

/**
 * This class implements an equalizer UI.
 * 
 * The equalizer consists of 32 band-pass filters. 
 * Each band of the equalizer can take on a fractional value between 
 * -1.0 and +1.0.
 * At -1.0, the input signal is attenuated by 6dB, at +1.0 the signal is
 * amplified by 6dB. 
 */
public class EqualizerApplet extends Panel implements ActionListener
{
	private Image imEqualizer;
	private int WinWidth, WinHeight;
	/*-- Slider Panel members --*/
	private Image imSliders = null;
	private boolean FirstSliderDrag = true;
	private int YSliderDrag = 0;
	private ActiveComponent[] acSlider = { null, null, null, null, null, null, null, null, null, null, null };
	private Image[] sliderImage = { null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null };
	private int[][] sliderBarLocation = { { 21, 38 }, 
										  {78, 38 }, {96, 38 }, {114, 38 }, {132, 38 }, {150, 38 },
										  {168, 38 }, {186, 38 }, {204, 38 }, {222, 38 }, {240, 38 }};
	private Image[] releasedSliderImage = { null };
	private Image[] pressedSliderImage = { null };
	private int minGain = 0;
	private int maxGain = 100;
	private int[] gainValue = { 50, 
								50, 50, 50, 50, 50, 50, 50, 50, 50, 50 };
	private int deltaSlider = 50;
	private int[][] sliderLocation = { { 0, 0 }, 
		       						   {0, 0 }, {0, 0 }, {0, 0 }, {0, 0 }, {0, 0 }, 
		       						   {0, 0 }, {0, 0 }, {0, 0 }, {0, 0 }, {0, 0 }};
	private int[] sliderBounds = { 0, 0 };
	
	/*-- On/Off/Auto  --*/
	private ActiveComponent acOnOff, acAuto;
	private Image[] releasedOAImage = { null, null };
	private Image[] pressedOAImage = { null, null };
	private int[] panelOALocation = { 15, 18, 39, 18 };
	
	/*-- Presets  --*/
	private ActiveComponent acPresets;
	private Image[] releasedPresetsImage = { null };
	private Image[] pressedPresetsImage = { null };
	private int[] panelPresetsLocation = { 217, 18 };
	private Image offScreenImage;
	private Graphics offScreenGraphics;
	
	private int[] PRESET_NORMAL={50,50,50,50,50,50,50,50,50,50};
	private int[] PRESET_CLASSICAL={50,50,50,50,50,50,70,70,70,76};
	private int[] PRESET_CLUB={50,50,42,34,34,34,42,50,50,50};
	private int[] PRESET_DANCE={26,34,46,50,50,66,70,70,50,50};
	private int[] PRESET_FULLBASS={26,26,26,36,46,62,76,78,78,78};
	private int[] PRESET_FULLBASSTREBLE={34,34,50,68,62,46,28,22,18,18};
	private int[] PRESET_FULLTREBLE={78,78,78,62,42,24,8,8,8,8};
	private int[] PRESET_LAPTOP={38,22,36,60,58,46,38,24,16,14};
	private int[] PRESET_LIVE={66,50,40,36,34,34,40,42,42,42};
	private int[] PRESET_PARTY={32,32,50,50,50,50,50,50,32,32};
	private int[] PRESET_POP={56,38,32,30,38,54,56,56,54,54};
	private int[] PRESET_REGGAE={48,48,50,66,48,34,34,48,48,48};
	private int[] PRESET_ROCK={32,38,64,72,56,40,28,24,24,24};
	private int[] PRESET_TECHNO={30,34,48,66,64,48,30,24,24,28};

	private Config config = null;
	private PlayerApplet player = null;
	private Applet parent = null;

	public static final int LINEARDIST = 1;
	public static final int OVERDIST = 2;
	private float[] bands = null;
	private int[] eqgains = null;
	private int eqdist = OVERDIST;

	
	/**
	 * Constructor.
	 * @param parent
	 * @param player
	 * @param skl
	 * @param xPos
	 * @param yPos
	 * @param showit
	 */
	public EqualizerApplet(Applet parent, PlayerApplet player, SkinLoader skl, int xPos, int yPos, boolean showit)
	{
		super();
		eqgains = new int[10];
		setLayout(null);
		this.parent = parent;
		this.player = player;
		// Config feature.
		config = Config.getInstance();
		imEqualizer = skl.getImage("eqmain.bmp");
		WinWidth = 275;
		WinHeight = 116;
		offScreenImage = parent.createImage(WinWidth, WinHeight);
		offScreenGraphics = offScreenImage.getGraphics();
		offScreenGraphics.drawImage(imEqualizer, 0, 0, null);
		imSliders = parent.createImage(208, 128);
		Graphics g = imSliders.getGraphics();
		g.drawImage(imEqualizer, 0, 0, 208, 128, 13, 164, 13 + 208, 164 + 128, null);
		
		// Load last equalizer values.
		int[] vals = config.getLastEqualizer();
		if (vals != null)
		{
			for (int h=0;h<vals.length;h++)
			{
				gainValue[h] = vals[h];
			}
		}
		setSliderPanel();
		setOnOffAutoPanel();		
		setPresetsPanel();
		
		// Popup menu on TitleBar
		PopupMenu mainpopup = new PopupMenu("PresetsPopup");
		String[] presets = {"Normal","Classical","Club","Dance","Full Bass",
							"Full Bass & Treble","Full Treble","Laptop",
							"Live","Party","Pop","Reggae","Rock","Techno"};
		Font fnt = new Font("Dialog", Font.PLAIN, 11);
		mainpopup.setFont(fnt);
		for (int p=0;p<presets.length;p++)
		{
			MenuItem mi = new MenuItem(presets[p]);
			mi.addActionListener(this);
			mainpopup.add(mi);			
		}
		acPresets.setPopup(mainpopup);
		
		setSize(WinWidth, WinHeight);
		setLocation(xPos, yPos);
		//setBackground(Color.black);
		show(showit); // changed to non-deprecated code (was show())
		//pack();
	}

	/**
	 * Set sliders for equalizer.
	 */
	private void setSliderPanel()
	{
		releasedSliderImage[0] = parent.createImage(10, 11);
		Graphics g = releasedSliderImage[0].getGraphics();
		g.drawImage(imEqualizer, 0, 0, 10, 11, 0, 164, 0 + 10, 164 + 11, null);
		pressedSliderImage[0] = parent.createImage(10, 11);
		g = pressedSliderImage[0].getGraphics();
		g.drawImage(imEqualizer, 0, 0, 10, 11, 0, 176, 0 + 10, 176 + 11, null);
		for (int k = 0; k < sliderImage.length / 2; k++)
		{
			sliderImage[k] = parent.createImage(13, 63);
			g = sliderImage[k].getGraphics();
			g.drawImage(imSliders, 0, 0, 13, 63, k * 15, 0, k * 15 + 13, 0 + 63, null);
		}
		for (int k = 0; k < sliderImage.length / 2; k++)
		{
			sliderImage[k + (sliderImage.length / 2)] = parent.createImage(13, 63);
			g = sliderImage[k + (sliderImage.length / 2)].getGraphics();
			g.drawImage(imSliders, 0, 0, 13, 63, k * 15, 65, k * 15 + 13, 65 + 63, null);
		}
		// Setup sliders
		for (int i = 0; i < acSlider.length; i++)
		{
			sliderLocation[i][0] = sliderBarLocation[i][0] + 1;
			sliderLocation[i][1] = sliderBarLocation[i][1] + 1 + deltaSlider * gainValue[i] / maxGain;
			sliderBounds[0] = sliderBarLocation[i][1] + 1;
			sliderBounds[1] = sliderBarLocation[i][1] + 1 + deltaSlider;
			acSlider[i] = new ActiveComponent(releasedSliderImage[0], pressedSliderImage[0], AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
			acSlider[i].setLocation(sliderLocation[i][0], sliderLocation[i][1]);
			add(acSlider[i]);
			acSlider[i].setActionCommand("Slider");
			acSlider[i].addActionListener(this);
			offScreenGraphics.drawImage(sliderImage[(27) - ((int) Math.round(((double) gainValue[i] / (double) maxGain) * (sliderImage.length - 1)))], sliderBarLocation[i][0], sliderBarLocation[i][1], this);
		}
	}
	
	/**
	 * Set On/Off and Auto checkbox.
	 */
	public void setOnOffAutoPanel()
	{
		// On/Off
		int w = 24, h = 12;
		releasedOAImage[0] = parent.createImage(w, h);
		Graphics g = releasedOAImage[0].getGraphics();
		g.drawImage(imEqualizer, 0, 0, w, h, 10, 119, 10 + w, 119 + h, null);
		pressedOAImage[0] = parent.createImage(w, h);
		g = pressedOAImage[0].getGraphics();
		g.drawImage(imEqualizer, 0, 0, w, h, 69, 119, 69 + w, 119 + h, null);
		acOnOff = new ActiveComponent(releasedOAImage[0], pressedOAImage[0], AWTEvent.MOUSE_EVENT_MASK, true, config.isEqualizerOn());
		acOnOff.setLocation(panelOALocation[0], panelOALocation[1]);
		add(acOnOff);
		acOnOff.setActionCommand("OnOff");
		acOnOff.addActionListener(this);
		// Auto
		w = 33;
		h = 12;
		releasedOAImage[1] = parent.createImage(w, h);
		g = releasedOAImage[1].getGraphics();
		g.drawImage(imEqualizer, 0, 0, w, h, 34, 119, 34 + w, 119 + h, null);
		pressedOAImage[1] = parent.createImage(w, h);
		g = pressedOAImage[1].getGraphics();
		g.drawImage(imEqualizer, 0, 0, w, h, 93, 119, 93 + w, 119 + h, null);
		acAuto = new ActiveComponent(releasedOAImage[1], pressedOAImage[1], AWTEvent.MOUSE_EVENT_MASK, true, config.isEqualizerAuto());
		acAuto.setLocation(panelOALocation[2], panelOALocation[3]);
		add(acAuto);
		acAuto.setActionCommand("Auto");
		acAuto.addActionListener(this);
	}
	/**
	 * Set presets button.
	 */
	public void setPresetsPanel()
	{
		int w = 43, h = 12;
		releasedPresetsImage[0] = parent.createImage(w, h);
		Graphics g = releasedPresetsImage[0].getGraphics();
		g.drawImage(imEqualizer, 0, 0, w, h, 224, 164, 224 + w, 164 + h, null);
		pressedPresetsImage[0] = parent.createImage(w, h);
		g = pressedPresetsImage[0].getGraphics();
		g.drawImage(imEqualizer, 0, 0, w, h, 224, 176, 224 + w, 176 + h, null);
		acPresets = new ActiveComponent(releasedPresetsImage[0], pressedPresetsImage[0], AWTEvent.MOUSE_EVENT_MASK);
		acPresets.setLocation(panelPresetsLocation[0], panelPresetsLocation[1]);
		add(acPresets);
		acPresets.setActionCommand("Presets");
		acPresets.addActionListener(this);
	}

	public void paint(Graphics g)
	{
		if (offScreenImage != null)
			g.drawImage(offScreenImage, 0, 0, this);
	}

	public void update(Graphics g)
	{
	  paint(g);
	}
	
	/**
	 * Set bands array for equalizer.
	 * @param bands
	 */
	public void setBands(float[] bands)
	{
		this.bands = bands;
	}

	/**
	 * Apply equalizer function.
	 * @param gains
	 * @param min
	 * @param max
	 */
	public void updateBands(int[] gains, int min, int max)
	{
		if ((gains != null) && (bands != null))
		{			
			int j=0;
			float gvalj= (gains[j]*2.0f/(max-min)*1.0f) - 1.0f;
			float gvalj1 = (gains[j+1]*2.0f/(max-min)*1.0f) - 1.0f;
			// Linear distribution : 10 values => 32 values.
			if (eqdist == LINEARDIST)
			{
				float a = (gvalj1 - gvalj)*1.0f;
				float b = gvalj*1.0f - (gvalj1 - gvalj)*j;
				// x=s*x'
				float s = (gains.length-1)*1.0f/(bands.length-1)*1.0f;		
				for (int i=0;i<bands.length;i++)
				{
					float ind = s*i;
					if (ind > (j+1))
					{
						j++;
						gvalj= (gains[j]*2.0f/(max-min)*1.0f) - 1.0f;
						gvalj1 = (gains[j+1]*2.0f/(max-min)*1.0f) - 1.0f;
						a = (gvalj1 - gvalj)*1.0f;
						b = gvalj*1.0f - (gvalj1 - gvalj)*j;
					}
					// a*x+b
					bands[i] = a*i*1.0f*s + b;
					//System.err.println("Lin : i="+i+" Bands["+i+"]="+bands[i]+" a="+a+" b="+b);
				}				
			}
			// Over distribution : 10 values => 10 first value of 32 values.
			else if (eqdist == OVERDIST)
			{
				for (int i=0;i<gains.length;i++)
				{
					bands[i] = (gains[i]*2.0f/(max-min)*1.0f) - 1.0f;
					//System.err.println("Over : i="+i+" Bands["+i+"]="+bands[i]);
				}				
			}
			
		}
	}
	
	public void actionPerformed(ActionEvent e)
	{
		String cmd = e.getActionCommand();
		/*-------------------*/
		/*--     ON/OFF    --*/
		/*-------------------*/
		if (e.getActionCommand().equals("OnOff"))
		{
		  if (acOnOff.getCheckboxState())
		  {
			config.setEqualizerOn(true);			
		  } 
		  else
		  {
			config.setEqualizerOn(false);
		  }
		  synchronizeEqualizer();
		}
		/*-------------------*/
		/*--      Auto     --*/
		/*-------------------*/
		else if (e.getActionCommand().equals("Auto"))
		{
		  if (acAuto.getCheckboxState())
		  {
			config.setEqualizerAuto(true);
		  } 
		  else
		  {
			config.setEqualizerAuto(false);
		  }
		}
		/*-------------------*/
		/*--    Presets    --*/
		/*-------------------*/
		else if (e.getActionCommand().equals("Presets"))
		{
			if ((acPresets.getMouseButton() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK)
			{
				MouseEvent me = new MouseEvent(acPresets, e.getID(), 0, e.getModifiers(), acPresets.getMouseX(), acPresets.getMouseY(), 1, true);
				acPresets.processMouseEvent(me); 
			}
		}		
		else if (e.getActionCommand().equals("Normal"))
		{
			updateSliders(PRESET_NORMAL);
			synchronizeEqualizer();
		}
		else if (e.getActionCommand().equals("Classical"))
		{
			updateSliders(PRESET_CLASSICAL);
			synchronizeEqualizer();
		}
		else if (e.getActionCommand().equals("Club"))
		{
			updateSliders(PRESET_CLUB);
			synchronizeEqualizer();
		}
		else if (e.getActionCommand().equals("Dance"))
		{
			updateSliders(PRESET_DANCE);
			synchronizeEqualizer();
		}
		else if (e.getActionCommand().equals("Full Bass"))
		{
			updateSliders(PRESET_FULLBASS);
			synchronizeEqualizer();
		}
		else if (e.getActionCommand().equals("Full Bass & Treble"))
		{
			updateSliders(PRESET_FULLBASSTREBLE);
			synchronizeEqualizer();
		}
		else if (e.getActionCommand().equals("Full Treble"))
		{
			updateSliders(PRESET_FULLTREBLE);
			synchronizeEqualizer();
		}
		else if (e.getActionCommand().equals("Laptop"))
		{
			updateSliders(PRESET_LAPTOP);
			synchronizeEqualizer();
		}
		else if (e.getActionCommand().equals("Live"))
		{
			updateSliders(PRESET_LIVE);
			synchronizeEqualizer();
		}
		else if (e.getActionCommand().equals("Party"))
		{
			updateSliders(PRESET_PARTY);
			synchronizeEqualizer();
		}
		else if (e.getActionCommand().equals("Pop"))
		{
			updateSliders(PRESET_POP);
			synchronizeEqualizer();
		}
		else if (e.getActionCommand().equals("Reggae"))
		{
			updateSliders(PRESET_REGGAE);
			synchronizeEqualizer();
		}
		else if (e.getActionCommand().equals("Rock"))
		{
			updateSliders(PRESET_ROCK);
			synchronizeEqualizer();
		}
		else if (e.getActionCommand().equals("Techno"))
		{
			updateSliders(PRESET_TECHNO);
			synchronizeEqualizer();
		}
		/*------------------------------------*/
		/*--       Interact on Sliders      --*/
		/*------------------------------------*/
		else if (e.getActionCommand().equals("Slider"))
		{
			ActiveComponent src = (ActiveComponent) e.getSource();
			int i = 0;
			for (i = 0; i < acSlider.length; i++)
			{
				if (acSlider[i] == src) break;
			}
			if (acSlider[i].isMousePressed() == false)
			{
				FirstSliderDrag = true;
				repaint();
			}
			else
			{
				int DeltaY = 0;
				if (FirstSliderDrag == false)
				{
					DeltaY = acSlider[i].getMouseY() - YSliderDrag;
					YSliderDrag = acSlider[i].getMouseY() - DeltaY;
					if (sliderLocation[i][1] + DeltaY < sliderBounds[0]) sliderLocation[i][1] = sliderBounds[0];
					else if (sliderLocation[i][1] + DeltaY > sliderBounds[1]) sliderLocation[i][1] = sliderBounds[1];
					else sliderLocation[i][1] = sliderLocation[i][1] + DeltaY;
					acSlider[i].setLocation(sliderLocation[i][0], sliderLocation[i][1]);
					double a = (maxGain - minGain) / (sliderBounds[1] - sliderBounds[0]);
					gainValue[i] = (int) (a * (sliderLocation[i][1] - sliderBounds[0]) + minGain);
					offScreenGraphics.drawImage(sliderImage[(27) - ((int) Math.round(((double) gainValue[i] / (double) maxGain) * (sliderImage.length - 1)))], sliderBarLocation[i][0], sliderBarLocation[i][1], this);
					//System.out.println("Gain:"+gainValue[i]);
				}
				else
				{
					FirstSliderDrag = false;
					YSliderDrag = acSlider[i].getMouseY();
				}
			}
			// Apply equalizer values.
			synchronizeEqualizer();
		}
	}

	/**
	 * Update sliders from gains array.
	 * @param gains
	 */
	public void updateSliders(int[] gains)
	{
		if (gains != null)
		{
			for (int i=0;i<gains.length;i++)
			{
				gainValue[i+1]=gains[i];
				sliderLocation[i+1][1] = sliderBarLocation[i+1][1] + 1 + deltaSlider * gainValue[i+1] / maxGain;
				acSlider[i+1].setLocation(sliderLocation[i+1][0], sliderLocation[i+1][1]);
				offScreenGraphics.drawImage(sliderImage[(27) - ((int) Math.round(((double) gainValue[i+1] / (double) maxGain) * (sliderImage.length - 1)))], sliderBarLocation[i+1][0], sliderBarLocation[i+1][1], this);
			}
			repaint();						
		}		
	}
	
	public void synchronizeEqualizer()
	{
		config.setLastEqualizer(gainValue);
		if (config.isEqualizerOn())
		{
			for (int j=0;j<eqgains.length;j++)
			{
				eqgains[j] = -gainValue[j+1] + maxGain;
			}
			updateBands(eqgains, minGain, maxGain);			
		}
		else
		{
			for (int j=0;j<eqgains.length;j++)
			{
				eqgains[j] = (maxGain-minGain)/2;
			}
			updateBands(eqgains, minGain, maxGain);							
		}
		
	}
	/**
	 * @return
	 */
	public int getEqdist()
	{
		return eqdist;
	}

	/**
	 * @param i
	 */
	public void setEqdist(int i)
	{
		eqdist = i;
	}

	/**
	 * Simulates "On/Off" selection.
	 */
	public void pressOnOff()
	{
	  acOnOff.fireEvent();
	}
	
	/**
	 * Simulates "Auto" selection.
	 */
	public void pressAuto()
	{
	  acAuto.fireEvent();
	}
	
	/**
	 * Force display of all components.
	 */	
	public void displayAll()
	{
	  acAuto.display();
	  acOnOff.display();
	  acPresets.display();
	  if (acSlider != null)
	  {
	  	for (int i=0;i<acSlider.length;i++)
	  	{
	  		acSlider[i].display();
	  	}
	  }
	  paintAll(getGraphics());  	
	}  	
}
