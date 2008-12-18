/*
 * MpegAudioFileReaderWorkaround.
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
package javazoom.jlgui.basicplayer;
import javazoom.spi.mpeg.sampled.file.IcyListener;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import javazoom.spi.mpeg.sampled.file.tag.IcyInputStream;
import org.tritonus.share.TDebug;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
public class MpegAudioFileReaderWorkaround extends MpegAudioFileReader
{
	/**
	 * Returns AudioInputStream from url and userAgent
	 */
	public AudioInputStream getAudioInputStream(URL url, String userAgent) throws UnsupportedAudioFileException, IOException
	{
		if (TDebug.TraceAudioFileReader)
		{
			TDebug.out("MpegAudioFileReaderWorkaround.getAudioInputStream(URL,String): begin");
		}
		long lFileLengthInBytes = AudioSystem.NOT_SPECIFIED;
		URLConnection conn = url.openConnection();
		// Tell shoucast server (if any) that SPI support shoutcast stream.
		boolean isShout = false;
		int toRead = 4;
		byte[] head = new byte[toRead];
		if (userAgent != null) conn.setRequestProperty("User-Agent", userAgent);
		conn.setRequestProperty("Accept", "*/*");
		conn.setRequestProperty("Icy-Metadata", "1");
		conn.setRequestProperty("Connection", "close");
		BufferedInputStream bInputStream = new BufferedInputStream(conn.getInputStream());
		bInputStream.mark(toRead);
		int read = bInputStream.read(head, 0, toRead);
		if ((read > 2) && (((head[0] == 'I') | (head[0] == 'i')) && ((head[1] == 'C') | (head[1] == 'c')) && ((head[2] == 'Y') | (head[2] == 'y'))))
		{
			isShout = true;
		}
		bInputStream.reset();
		InputStream inputStream = null;
		// Is is a shoutcast server ?
		if (isShout == true)
		{
			// Yes
			IcyInputStream icyStream = new IcyInputStream(bInputStream);
			icyStream.addTagParseListener(IcyListener.getInstance());
			inputStream = icyStream;
		}
		else
		{
			// No, is Icecast 2 ?
			String metaint = conn.getHeaderField("icy-metaint");
			if (metaint != null)
			{
				// Yes, it might be icecast 2 mp3 stream.
				IcyInputStream icyStream = new IcyInputStream(bInputStream, metaint);
				icyStream.addTagParseListener(IcyListener.getInstance());
				inputStream = icyStream;
			}
			else
			{
				// No
				inputStream = bInputStream;
			}
		}
		AudioInputStream audioInputStream = null;
		try
		{
			audioInputStream = getAudioInputStream(inputStream, lFileLengthInBytes);
		}
		catch (UnsupportedAudioFileException e)
		{
			inputStream.close();
			throw e;
		}
		catch (IOException e)
		{
			inputStream.close();
			throw e;
		}
		if (TDebug.TraceAudioFileReader)
		{
			TDebug.out("MpegAudioFileReaderWorkaround.getAudioInputStream(URL,String): end");
		}
		return audioInputStream;
	}
}