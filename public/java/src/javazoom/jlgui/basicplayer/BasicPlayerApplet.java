package javazoom.jlgui.basicplayer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BasicPlayerApplet extends BasicPlayer
{
	private static Log log = LogFactory.getLog(BasicPlayerApplet.class);
	
	// Applet UGLY workaround.
	public static String OGGEXTENSIONS = "ogg";
	public static String AUEXTENSIONS = "au";
	public static String WAVEXTENSIONS = "wav";
	public static boolean isOgg = false;
	public static boolean forceOgg = false;
	
	public BasicPlayerApplet()
	{
		super();
	}

	/**
	 * Inits Audio ressources from file.
	 */
	protected void initAudioInputStream(File file) throws UnsupportedAudioFileException, IOException
	{
	  // Applet UGLY workaround.
	  if ((file.getName().toLowerCase().endsWith(AUEXTENSIONS)) ||
		  (file.getName().toLowerCase().endsWith(WAVEXTENSIONS)))
	  {
		m_audioInputStream = AudioSystem.getAudioInputStream(file);
		m_audioFileFormat = AudioSystem.getAudioFileFormat(file);
		isOgg = false;
	  }
	  else if ((file.getName().toLowerCase().endsWith(OGGEXTENSIONS)) || (forceOgg==true))
	  {
		m_audioInputStream = AppletVorbisSPIWorkaround.getAudioInputStream(file);
		m_audioFileFormat = AppletVorbisSPIWorkaround.getAudioFileFormat(file);
		isOgg = true;
	  }
	  else
	  {
		m_audioInputStream = AppletMpegSPIWorkaround.getAudioInputStream(file);
		m_audioFileFormat = AppletMpegSPIWorkaround.getAudioFileFormat(file);
		isOgg = false;
	  }
	}

	/**
	 * Inits Audio ressources from URL.
	 */
	protected void initAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException
	{
	  // Applet UGLY workaround.
	  if ((url.toString().toLowerCase().endsWith(AUEXTENSIONS)) ||
	      (url.toString().toLowerCase().endsWith(WAVEXTENSIONS)))
	  {
		m_audioInputStream = AudioSystem.getAudioInputStream(url);
		m_audioFileFormat = AudioSystem.getAudioFileFormat(url);
		isOgg = false;
	  }
	  else if ((url.toString().toLowerCase().endsWith(OGGEXTENSIONS))  || (forceOgg==true))
	  {
		m_audioInputStream = AppletVorbisSPIWorkaround.getAudioInputStream(url);
		m_audioFileFormat = AppletVorbisSPIWorkaround.getAudioFileFormat(url);
		isOgg = true;
	  }
	  else
	  {
		m_audioInputStream = AppletMpegSPIWorkaround.getAudioInputStream(url);
		m_audioFileFormat = AppletMpegSPIWorkaround.getAudioFileFormat(url);
		isOgg = false;
	  }
	}

	/**
	 * Inits Audio ressources from InputStream.
	 */
	protected void initAudioInputStream(InputStream inputStream) throws UnsupportedAudioFileException, IOException
	{
	  // Applet UGLY workaround.
	  if (forceOgg==true)
	  {
		m_audioFileFormat = AppletVorbisSPIWorkaround.getAudioFileFormat(inputStream);
		m_audioInputStream = AppletVorbisSPIWorkaround.getAudioInputStream(inputStream);
		isOgg = true;
	  }
	  else
	  {
		// Try MP3 format.
		try
		{
		  m_audioFileFormat = AppletMpegSPIWorkaround.getAudioFileFormat(inputStream);
		  m_audioInputStream = AppletMpegSPIWorkaround.getAudioInputStream(inputStream);
		  isOgg = false;
		}
		catch (IOException ex)
		{
		  throw ex;
		}
		catch (UnsupportedAudioFileException ex)
		{
		  // Try Vorbis format.
		  m_audioFileFormat = AppletVorbisSPIWorkaround.getAudioFileFormat(inputStream);
		  m_audioInputStream = AppletVorbisSPIWorkaround.getAudioInputStream(inputStream);
		  isOgg = true;
		}
	  }
	}
	
	protected void createLine() throws LineUnavailableException
	{
	  log.info("Create Line");
	  if (m_line == null)
	  {
		AudioFormat sourceFormat = m_audioInputStream.getFormat();
		log.info("Create Line : Source format : " + sourceFormat.toString());
		AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
												   sourceFormat.getSampleRate(),
												   16,
												   sourceFormat.getChannels(),
												   sourceFormat.getChannels() * 2,
												   sourceFormat.getSampleRate(),
												   false);
		log.info("Create Line : Target format: " + targetFormat);
		// Keep a reference on encoded stream to progress notification.
		m_encodedaudioInputStream = m_audioInputStream; 			
		try
		{
			// Get total length in bytes of the encoded stream.
			encodedLength = m_encodedaudioInputStream.available();
		}
		catch (IOException e)
		{
			log.error("Cannot get m_encodedaudioInputStream.available()",e);
		}
		// Create decoded stream.

		// Applet UGLY workaround.
		if ((isOgg == true) || (forceOgg == true))  m_audioInputStream = AppletVorbisSPIWorkaround.getAudioInputStream(targetFormat,m_audioInputStream);
		else m_audioInputStream = AppletMpegSPIWorkaround.getAudioInputStream(targetFormat,m_audioInputStream);

		AudioFormat audioFormat = m_audioInputStream.getFormat();
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat, AudioSystem.NOT_SPECIFIED);		
		m_line = (SourceDataLine) AudioSystem.getLine(info);
		log.debug("Line AudioFormat: " + m_line.getFormat().toString());
		
		/*-- Display supported controls --*/
		Control[] c = m_line.getControls();
		for (int p = 0; p < c.length; p++)
		{
			log.debug("Controls : " + c[p].toString());
		}
		
		/*-- Is Gain Control supported ? --*/
		if (m_line.isControlSupported(FloatControl.Type.MASTER_GAIN))
		{
		  m_gainControl = (FloatControl) m_line.getControl(FloatControl.Type.MASTER_GAIN);
		  log.info("Master Gain Control : [" + m_gainControl.getMinimum() + "," + m_gainControl.getMaximum() + "] " + m_gainControl.getPrecision());
		}

		/*-- Is Pan control supported ? --*/
		if (m_line.isControlSupported(FloatControl.Type.PAN))
		{
		  m_panControl = (FloatControl) m_line.getControl(FloatControl.Type.PAN);
		  log.info("Pan Control : [" + m_panControl.getMinimum() + "," + m_panControl.getMaximum() + "] " + m_panControl.getPrecision());
		}
	  }
	}	
}
