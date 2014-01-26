package matt.util.encoding;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;

public class EncodedBufferedReader implements Closeable
{
	private MAWCharacterEncoding encoding;
	private BufferedReader br;
	boolean skipLineFeed = false;
	
	public EncodedBufferedReader(Reader reader, MAWCharacterEncoding encoding, int bufferSize)
	{
		br = new BufferedReader(reader, bufferSize);
		this.encoding = encoding;
	}
	
	public EncodedBufferedReader(Reader reader, MAWCharacterEncoding encoding)
	{
		this(reader, encoding, 8192);
	}
	
	public int read() throws IOException
	{
		String str = "";
		int i;
		while((i = br.read()) != -1)
		{
			if(i == 65535)
			{
				str += (char) i;
			}
			else
			{
				return encoding.decode(str + (char) i).charAt(0);
			}
		}
		return -1;
	}
	
	public String readLine() throws IOException
	{
		String str = "";
		int i;
		while((i = read()) != -1)
		{
			char c = (char) i;
			if(c == '\n' || c == '\r')
			{
				if(c == '\n' && skipLineFeed)
				{
					skipLineFeed = false;
					continue;
				}
				if(c == '\r')
				{
					skipLineFeed = true;
				}
				break;
			}
			str += c;
		}
		return (str.isEmpty() ? null : str);
	}
	
	@Override
	public void close() throws IOException
	{
		br.close();
	}
}